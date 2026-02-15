CREATE DATABASE IF NOT EXISTS reports;

CREATE TABLE IF NOT EXISTS reports.crm_user_dim
(
    user_id String,
    full_name String,
    email String,
    updated_at DateTime
)
    ENGINE = ReplacingMergeTree(updated_at)
    ORDER BY user_id;

-- 2) MV: из raw (JSON Debezium) достаем after.*
CREATE MATERIALIZED VIEW IF NOT EXISTS reports.mv_crm_user_dim
TO reports.crm_user_dim
AS
SELECT
    JSONExtractString(payload, 'payload.after.user_id')   AS user_id,
    JSONExtractString(payload, 'payload.after.full_name') AS full_name,
    JSONExtractString(payload, 'payload.after.email')     AS email,
    now() AS updated_at
FROM reports.crm_user_raw
WHERE JSONExtractString(payload, 'payload.after.user_id') != '';
