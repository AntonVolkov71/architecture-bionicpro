package ru.volkov.report.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.volkov.report.application.readmodel.ReportMartReadModel;
import ru.volkov.report.application.service.ReportApplicationService;
import ru.volkov.report.infrastructure.persistence.auth.AuthSessionClient;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("")
public class ReportController {

    private final ReportApplicationService reportApplicationService;
    private final AuthSessionClient authSessionClient;

    public ReportController(ReportApplicationService reportApplicationService,
                            AuthSessionClient authSessionClient) {
        this.reportApplicationService = reportApplicationService;
        this.authSessionClient = authSessionClient;
    }

    @GetMapping("/reports")
    public ReportMartReadModel index(HttpServletRequest request) {
        var session = authSessionClient.validateOrThrow(request);
        String userId = session.userId();

        ReportMartReadModel report = reportApplicationService.getLatestReport(userId);

        if (report == null) {
            throw new ResponseStatusException(NOT_FOUND, "Report not ready for requested period");
        }

        return report;
    }
}
