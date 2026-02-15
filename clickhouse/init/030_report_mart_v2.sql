CREATE OR REPLACE VIEW reports.report_mart_v2 AS
SELECT
    m.user_id,
    m.period_from,
    m.period_to,
    m.generated_at,
    m.telemetry_events,
    m.errors_count,
    u.full_name  AS crm_full_name,
    u.email      AS crm_email,
    p.model      AS prosthesis_model
FROM reports.report_mart AS m
         LEFT JOIN (SELECT * FROM reports.crm_user_dim FINAL) AS u
                   ON u.user_id = m.user_id
         LEFT JOIN (SELECT * FROM reports.crm_prosthesis_dim FINAL) AS p
                   ON p.user_id = m.user_id
;
