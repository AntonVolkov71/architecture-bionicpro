package ru.volkov.report.application.readmodel;


import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReportMartReadModel(
        String userId,
        LocalDate periodFrom,
        LocalDate periodTo,
        LocalDateTime generatedAt,
        long telemetryEvents,
        long errorsCount,
        String crmFullName,
        String crmEmail,
        String prosthesisModel
) {
}
