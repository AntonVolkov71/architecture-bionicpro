package ru.volkov.report.infrastructure.persistence.olap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.volkov.report.application.readmodel.ReportMartReadModel;

import java.util.List;

@Repository
public class ReportMartRepository {

    private static final RowMapper<ReportMartReadModel> MAPPER = (rs, rowNum) ->
            new ReportMartReadModel(
                    rs.getString("user_id"),
                    rs.getDate("period_from").toLocalDate(),
                    rs.getDate("period_to").toLocalDate(),
                    rs.getTimestamp("generated_at").toLocalDateTime(),
                    rs.getLong("telemetry_events"),
                    rs.getLong("errors_count"),
                    rs.getString("crm_full_name"),
                    rs.getString("crm_email"),
                    rs.getString("prosthesis_model")
            );
    private final JdbcTemplate ch;

    public ReportMartRepository(@Qualifier("clickhouseJdbcTemplate") JdbcTemplate ch) {
        this.ch = ch;
    }

    public ReportMartReadModel findLatestForUser(String userId) {
        String sql = """
                    select
                      user_id, period_from, period_to, generated_at,
                      telemetry_events, errors_count,
                      crm_full_name, crm_email, prosthesis_model
                    from report_mart
                    where user_id = ?
                    order by generated_at desc
                    limit 1
                """;

        List<ReportMartReadModel> rows = ch.query(sql, MAPPER, userId);
        return rows.isEmpty() ? null : rows.get(0);
    }
}