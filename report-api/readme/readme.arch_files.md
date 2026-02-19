```
src/main/java/com/integral/auth
  ├─ app/ (по факту корневой пакет, Application.java)
  ├─ shared/        # утилиты, мапперы, общие классы
  ├─ modules/
  │   ├─ auth/
  │   │   ├─ AuthService.java
  │   │   ├─ TokenService.java
  │   │   ├─ entity/UserEntity.java
  │   │   └─ dto/LoginRequest.java
  │   └─ user/
  ├─ infra/
  │   ├─ mq/
  │   └─ schedulers/
  ├─ config/
  └─ bootstrap/
```