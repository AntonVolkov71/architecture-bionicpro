package ru.volkov.report.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.clickhouse")
public record ClickHouseProperties(
        String url,
        String user,
        String password
) {
}