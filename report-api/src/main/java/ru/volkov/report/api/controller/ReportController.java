package ru.volkov.report.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.volkov.report.application.readmodel.ReportMartReadModel;
import ru.volkov.report.application.service.ReportApplicationService;
import ru.volkov.report.application.service.ReportCachingService;
import ru.volkov.report.dto.ReportLinkResponse;
import ru.volkov.report.infrastructure.persistence.auth.AuthSessionClient;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("")
public class ReportController {

    private final ReportCachingService reportCachingService;
    private final AuthSessionClient authSessionClient;
    private final ReportApplicationService reportApplicationService;

    public ReportController(ReportCachingService reportCachingService,
                            AuthSessionClient authSessionClient,
                            ReportApplicationService reportApplicationService) {
        this.reportCachingService = reportCachingService;
        this.authSessionClient = authSessionClient;
        this.reportApplicationService = reportApplicationService;
    }

    @GetMapping("/reports")
    public ReportLinkResponse index(HttpServletRequest request) {
        var session = authSessionClient.validateOrThrow(request);
        String userId = session.userId();

        String url = reportCachingService.getOrCreateReportUrl(userId);

        return new ReportLinkResponse(url);
    }

    @GetMapping("/reports/v2")
    public ReportMartReadModel indexV2(HttpServletRequest request) {
        var session = authSessionClient.validateOrThrow(request);
        String userId = session.userId();

        ReportMartReadModel report = reportApplicationService.getLatestReportV2(userId);

        if (report == null) {
            throw new ResponseStatusException(NOT_FOUND, "Report not ready for requested period");
        }
        return report;
    }
}
