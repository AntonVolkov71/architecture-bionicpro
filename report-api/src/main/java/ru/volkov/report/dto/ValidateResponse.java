package ru.volkov.report.dto;

public record ValidateResponse(
        boolean valid,
        String userId,
        java.util.List<String> roles,
        String newSid
) {
}
