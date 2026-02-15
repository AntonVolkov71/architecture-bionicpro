package ru.volkov.report.application.common;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        int page,
        int pageSize,
        long totalElements,
        int totalPages
) {
}