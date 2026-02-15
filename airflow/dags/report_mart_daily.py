from __future__ import annotations

import os
from datetime import datetime, timedelta, date

import psycopg2
import requests
from airflow import DAG
from airflow.operators.python import PythonOperator


POSTGRES_HOST = os.getenv("POSTGRES_HOST", "report_db")
POSTGRES_PORT = int(os.getenv("POSTGRES_PORT", "5432"))
POSTGRES_DB = os.getenv("POSTGRES_DB", "report")
POSTGRES_USER = os.getenv("POSTGRES_USER", "postgres")
POSTGRES_PASSWORD = os.getenv("POSTGRES_PASSWORD", "")

CH_HOST = os.getenv("CLICKHOUSE_HOST", "clickhouse")
CH_HTTP_PORT = int(os.getenv("CLICKHOUSE_HTTP_PORT", "8123"))
CH_USER = os.getenv("CLICKHOUSE_USER", "default")
CH_PASSWORD = os.getenv("CLICKHOUSE_PASSWORD", "")
CH_DB = os.getenv("CLICKHOUSE_DB", "reports")


def _period() -> tuple[date, date]:
    # Витрина за "последние сутки" в терминах дат (from включительно, to исключительно)
    # Пример: 2026-02-15 .. 2026-02-16
    to_d = datetime.utcnow().date() + timedelta(days=1)
    from_d = to_d - timedelta(days=1)
    return from_d, to_d


def load_to_clickhouse():
    period_from, period_to = _period()

    pg = psycopg2.connect(
        host=POSTGRES_HOST,
        port=POSTGRES_PORT,
        dbname=POSTGRES_DB,
        user=POSTGRES_USER,
        password=POSTGRES_PASSWORD,
    )

    sql = """
    select
      u.user_id,
      %s::date as period_from,
      %s::date as period_to,
      now() as generated_at,
      count(e.id)::bigint as telemetry_events,
      sum(case when e.event_type='ERROR' then 1 else 0 end)::bigint as errors_count,
      u.full_name,
      u.email,
      coalesce(max(p.model), '') as prosthesis_model
    from crm_user u
    left join telemetry_event e
      on e.user_id = u.user_id
     and e.created_at >= %s
     and e.created_at <  %s
    left join crm_prosthesis p
      on p.user_id = u.user_id
    group by u.user_id, u.full_name, u.email
    """

    with pg.cursor() as cur:
        cur.execute(sql, (period_from, period_to, period_from, period_to))
        rows = cur.fetchall()

    pg.close()

    # Нечего писать — выходим без ошибки
    if not rows:
        print("No rows to insert into ClickHouse for period:", period_from, period_to)
        return

    # ClickHouse HTTP INSERT (TSV) — ВАЖНО:
    # generated_at шлём как Unix timestamp (int seconds) — ClickHouse DateTime парсит стабильно
    lines: list[str] = []
    for r in rows:
        user_id = r[0]
        pf = r[1]  # date
        pt = r[2]  # date
        generated_at = r[3]  # datetime
        telemetry_events = int(r[4] or 0)
        errors_count = int(r[5] or 0)
        full_name = (r[6] or "").replace("\t", " ").replace("\n", " ")
        email = (r[7] or "").replace("\t", " ").replace("\n", " ")
        model = (r[8] or "").replace("\t", " ").replace("\n", " ")

        gen_ts = int(generated_at.timestamp())

        lines.append(
            f"{user_id}\t{pf}\t{pt}\t{gen_ts}\t"
            f"{telemetry_events}\t{errors_count}\t{full_name}\t{email}\t{model}"
        )

    data = "\n".join(lines) + "\n"

    url = f"http://{CH_HOST}:{CH_HTTP_PORT}/"
    query = (
        "INSERT INTO report_mart "
        "(user_id, period_from, period_to, generated_at, telemetry_events, errors_count, crm_full_name, crm_email, prosthesis_model) "
        "FORMAT TabSeparated"
    )

    params = {"database": CH_DB, "query": query}

    resp = requests.post(
        url,
        params=params,
        data=data.encode("utf-8"),
        auth=(CH_USER, CH_PASSWORD) if CH_PASSWORD else None,
        timeout=30,
    )

    if resp.status_code >= 400:
        # Это важно, чтобы видеть текст ошибки ClickHouse в логах Airflow
        raise RuntimeError(f"ClickHouse insert failed: {resp.status_code} {resp.text[:500]}")

    print(f"Inserted {len(rows)} rows into ClickHouse reports.report_mart for period {period_from}..{period_to}")


with DAG(
    dag_id="report_mart_daily",
    start_date=datetime(2025, 1, 1),
    schedule="*/5 * * * *",  # каждые 5 минут (для демо)
    catchup=False,
    default_args={"retries": 2, "retry_delay": timedelta(seconds=20)},
    tags=["reports"],
) as dag:
    PythonOperator(
        task_id="load_postgres_to_clickhouse_mart",
        python_callable=load_to_clickhouse,
    )
