package ru.volkov.report.application.service;

import org.springframework.stereotype.Service;
import ru.volkov.report.application.readmodel.ReportMartReadModel;
import ru.volkov.report.infrastructure.persistence.olap.ReportMartRepository;

@Service
public class ReportApplicationService {


    private final ReportMartRepository reportMartRepository;

    public ReportApplicationService(ReportMartRepository reportMartRepository) {
        this.reportMartRepository = reportMartRepository;
    }

    public ReportMartReadModel getLatestReport(String userId) {
        return reportMartRepository.findLatestForUser(userId);
    }
}
