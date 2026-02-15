package ru.volkov.report.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.volkov.report.application.service.ReportCachingService;
import ru.volkov.report.dto.ReportLinkResponse;
import ru.volkov.report.infrastructure.persistence.auth.AuthSessionClient;

@RestController
@RequestMapping("")
public class ReportController {

    private final ReportCachingService reportCachingService;
    private final AuthSessionClient authSessionClient;

    public ReportController(ReportCachingService reportCachingService,
                            AuthSessionClient authSessionClient) {
        this.reportCachingService = reportCachingService;
        this.authSessionClient = authSessionClient;
    }

    @GetMapping("/reports")
    public ReportLinkResponse index(HttpServletRequest request) {
        var session = authSessionClient.validateOrThrow(request);
        String userId = session.userId();

        String url = reportCachingService.getOrCreateReportUrl(userId);

        return new ReportLinkResponse(url);
    }
}
