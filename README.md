# Спринт 9

## Задание 1

- перенос аутентификации в сервис bionicpro-auth
    - единая точка аутентификации
    - сам общается с Kyacloak
    - хранит у себя в оперативке ID сессии и токены (access, refresh), выдает только ID
    - делает ротацию SID
    - фронт логиниться через сервис auth
    - заложено для будущего валидация запросов от других сервисов по куке с SID
- Keycloak
    - подключили Ldap - внешние пользователи
    - добавили MFA - обязательно одноразовый пароль (Microsoft Authenticator)
    - добавили способ входа - Yandex OAuth
- [README.md](Task1/README.md)

## Задание 2

- реализовали
    - UI -> API (валидация) -> Clickhouse
    - Airflow (расписание) -> Report_DB -> Витрина Clickhouse
    - [README.md](Task2/README.md)

### Задание 3

- UI запрос данных в API
    - если данные есть в кеше через CDN отдаем ссылку
    - если в кеше нету, запрашиваем в CLickhouse пишем из в S3 отдаем ссылку для UI
    - [Readme.md](Task3/Readme.md)

### Задание 4

- реfлизовали Change Data Capture
    - следим за изменениями в апи репортов
    - через коннектор Debezium оптарвляем через Kafka в Clickhouse
    - берем с новой ручкии /reports/v2 - с витрины Clickhouse
    - [README.md](Task4/README.md)