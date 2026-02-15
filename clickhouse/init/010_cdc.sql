CREATE DATABASE IF NOT EXISTS reports;

-- 1) KafkaEngine: crm_user
CREATE TABLE IF NOT EXISTS reports.crm_user_cdc_kafka
(
    payload String
)
    ENGINE = Kafka
    SETTINGS
    kafka_broker_list = 'kafka:29092',
    kafka_topic_list = 'crm.public.crm_user',
    kafka_group_name = 'ch-crm-user',
    kafka_format = 'JSONAsString',
    kafka_num_consumers = 1;

-- 2) KafkaEngine: crm_prosthesis
CREATE TABLE IF NOT EXISTS reports.crm_prosthesis_cdc_kafka
(
    payload String
)
    ENGINE = Kafka
    SETTINGS
    kafka_broker_list = 'kafka:29092',
    kafka_topic_list = 'crm.public.crm_prosthesis',
    kafka_group_name = 'ch-crm-prosthesis',
    kafka_format = 'JSONAsString',
    kafka_num_consumers = 1;

-- 3) Raw: куда сохраняем события (стейджинг)
CREATE TABLE IF NOT EXISTS reports.crm_user_raw
(
    payload String,
    ingested_at DateTime DEFAULT now()
    )
    ENGINE = MergeTree
    ORDER BY ingested_at;

CREATE TABLE IF NOT EXISTS reports.crm_prosthesis_raw
(
    payload String,
    ingested_at DateTime DEFAULT now()
    )
    ENGINE = MergeTree
    ORDER BY ingested_at;

-- 4) MV: из KafkaEngine -> в Raw
CREATE MATERIALIZED VIEW IF NOT EXISTS reports.mv_crm_user_raw
TO reports.crm_user_raw
AS SELECT payload FROM reports.crm_user_cdc_kafka;

CREATE MATERIALIZED VIEW IF NOT EXISTS reports.mv_crm_prosthesis_raw
TO reports.crm_prosthesis_raw
AS SELECT payload FROM reports.crm_prosthesis_cdc_kafka;
