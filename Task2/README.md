## Сервис отчетов

- добавили валидация через auth
    - перед выдачей данных вызывает bionicpro-auth POST /sessions/validate
    - bionicpro-auth проверяет sid из cookie, при необходимости делает refresh access-token и rotation sid,
        - возвращает { valid, userId, roles, newSid }
    - неавторизованный (нет cookie sid / sid неизвестен) получает 401
    - авторизованный проходит и может запросить отчёт
        - ![report_not_auth.png](result/report_not_auth.png)


- добавили сервисы
    - clickhouse
        - OLAP БД - аналитика агрегация
    - airflow_db
        - Postgres для Airflow
    - airflow
        - оркестратор расписаний - он же планировщик - он же запускатор задач - он же..
        - по расписанию наполняет витрину в Clickhouse из каких-либо источников (тута Postgres)

- создали файлы
    - report-db/init/001_sources.sql
        - демо-данные, ибо реальных сервисов "CRM", "телеметрия" нету
            - crm_user, crm_prosthesis — демо "CRM"
            - telemetry_event — демо "телеметрия"
    - clickhouse/init/001_mart.sql
        - таблицу витрины reports.report_mart
        - готовый отчет для API типа быстро
    - airflow/dags/report_mart_daily.py
        - это DAG берет данные из Postgres (телеметрия, срм) агрегирует и пишет в кликхаус в report_mart

- запускаем докер
    - данные в report_db есть
        - ![report_db_mock.png](result/report_db_mock.png)
    - DAG скрипт в airflow имеется
        - изначально падал скрипт
            - время парсил неккоректно
            - нельзя было попасть в кликхаус permission доступ
                - убрал в компосе volume для кликхауса
                - теперь работает dag
                    - ![airflow_dag.png](result/airflow_dag.png)
                - и видно данные в кликхаусе если запросить в браузере
                - ![clickhouse_vitrina.png](result/clickhouse_vitrina.png)
    - подкрутили API запрос в кликхаус
        - по умолчанию за поcледение сутки, дабы не трогать фронт (не усложнять from/to)
        - ![front-report-api.png](result/front-report-api.png)
        - другой пользователь который тоже был проинициализорован
            - ![front-report-api_auth_user.png](result/front-report-api_auth_user.png)
        - а для пользователя prothetic2@example.com" нету данных
            - ![front-report-api_not-auth_user.png.png](result/front-report-api_not-auth_user.png.png)

- итого
    - /reports — возвращает последний подготовленный отчёт для текущего пользователя из витрины reports.report_mart (
      ClickHouse).
    - “Генерация” в онлайне не выполняет вычисления, а читает из витрины; наполнение витрины делает Airflow по
      расписанию.
    - Если пользователь запрашивает период/данные, которых ещё нет в витрине — API возвращает 404 Report not ready
    - UI кнопка вызывает /reports и получает подготовленный отчёт из OLAP.
    - Airflow отвечает за подготовку данных, API — за выдачу.