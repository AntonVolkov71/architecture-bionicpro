package ru.volkov.report.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.volkov.report.application.readmodel.ReportMartReadModel;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ReportCachingService {

    private final S3Client s3;
    private final ReportApplicationService reportApplicationService;
    private final ObjectMapper objectMapper;

    private final String bucket;
    private final String cdnBaseUrl;

    public ReportCachingService(
            S3Client s3,
            ReportApplicationService reportApplicationService,
            ObjectMapper objectMapper,
            @Value("${app.s3.bucket}") String bucket,
            @Value("${app.cdn.base-url}") String cdnBaseUrl
    ) {
        this.s3 = s3;
        this.reportApplicationService = reportApplicationService;
        this.objectMapper = objectMapper;
        this.bucket = bucket;
        this.cdnBaseUrl = cdnBaseUrl;
    }

    public String getOrCreateReportUrl(String userId) {
        String objectKey = buildDailyKey(userId, LocalDate.now()); // упрощённо как у тебя "последние сутки"

        if (exists(objectKey)) {
            return cdnUrl(objectKey);
        }

        ReportMartReadModel report = reportApplicationService.getLatestReport(userId);
        if (report == null) {
            throw new ResponseStatusException(NOT_FOUND, "Report not ready for requested period");
        }

        putJson(objectKey, report);
        return cdnUrl(objectKey);
    }

    private String buildDailyKey(String userId, LocalDate date) {
        return "reports/" + userId + "/daily/" + date + ".json";
    }

    private boolean exists(String key) {
        try {
            s3.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            // Minio может вернуть 404 как S3Exception
            if (e.statusCode() == 404) return false;
            throw e;
        }
    }

    private void putJson(String key, Object value) {
        try {
            byte[] bytes = objectMapper.writeValueAsString(value).getBytes(StandardCharsets.UTF_8);
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromBytes(bytes)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to store report in S3", e);
        }
    }

    private String cdnUrl(String key) {
        // cdnBaseUrl = http://localhost:8088/reports
        return cdnBaseUrl.replaceAll("/+$", "") + "/" + key;
    }
}