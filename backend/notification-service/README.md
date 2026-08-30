# Notification Service

> **Service owner:** Yasiru | **Port:** `8086` | **DB:** `notification_db` (PostgreSQL)
> **Stack:** Java 21 · Spring Boot 4.1.0 · Spring MVC · Spring Data JPA · Spring Mail · Lombok · Maven

---

## 1. Service Responsibility

Sends and tracks outbound notifications for REOAGMS on behalf of the other services (e.g. alerts from Monitoring, work-order updates from Maintenance). It owns three concerns:

1. **Notification lifecycle** — every send attempt is recorded as a `Notification` row (`PENDING → SENT` or `FAILED → RETRYING`).
2. **Channel dispatch** — pluggable dispatchers per channel (`EMAIL`, `SMS`, `PUSH` reserved).
3. **Retry** — a scheduled job resends `FAILED` notifications up to a configured attempt cap.

It never reads another service's database — upstream services call this service's REST API to trigger a notification.

```
notification_db owns:  notifications · email_templates · sms_notifications
```

---

## 2. Package Structure

```
com.reoagms.notification_service
├── NotificationServiceApplication.java   ← Spring Boot entry point (@EnableScheduling)
│
├── common/
│   ├── enums/        ← NotificationChannel, NotificationStatus
│   ├── exception/     ← ResourceNotFoundException, NotificationDispatchException, GlobalExceptionHandler
│   └── model/         ← BaseEntity (UUID pk + createdAt/updatedAt), ErrorResponse
│
├── notification/       ← Core aggregate: CRUD + dispatch + retry
│   ├── model/Notification.java
│   ├── dto/            ← NotificationRequest, NotificationResponse
│   ├── mapper/NotificationMapper.java
│   ├── repository/NotificationRepository.java
│   ├── service/
│   │   ├── NotificationDispatcher.java        ← interface implemented per channel
│   │   ├── NotificationService(Impl).java      ← CRUD + orchestrates dispatch
│   │   └── NotificationRetryScheduler.java     ← @Scheduled retry sweep
│   └── controller/NotificationController.java
│
├── email/               ← Email channel
│   ├── model/EmailTemplate.java
│   ├── dto/             ← EmailRequest, EmailTemplateRequest/Response
│   ├── mapper/EmailTemplateMapper.java
│   ├── repository/EmailTemplateRepository.java
│   ├── service/
│   │   ├── EmailDispatcher.java                ← implements NotificationDispatcher (JavaMailSender)
│   │   └── EmailTemplateService(Impl).java
│   └── controller/
│       ├── EmailController.java                ← POST /api/v1/email (send)
│       └── EmailTemplateController.java        ← CRUD /api/v1/email-templates
│
└── sms/                 ← SMS channel
    ├── model/SmsNotification.java
    ├── dto/SmsRequest.java
    ├── repository/SmsNotificationRepository.java
    ├── service/SmsDispatcher.java              ← implements NotificationDispatcher (stub, logs + persists)
    └── controller/SmsController.java           ← POST /api/v1/sms (send)
```

### Design pattern: dispatch by channel

`NotificationServiceImpl` holds every `NotificationDispatcher` bean and picks the one matching `Notification.channel` at send time. Adding a new channel (e.g. `PUSH`) means: add the enum value, add a `NotificationDispatcher` implementation, done — no changes to `NotificationServiceImpl`.

---

## 3. Entities

All extend `BaseEntity` (UUID pk, `createdAt`, `updatedAt`).

### `Notification`
| Field | Notes |
|---|---|
| `recipient` | email address or phone number |
| `channel` | `EMAIL` / `SMS` / `PUSH` |
| `status` | `PENDING` / `SENT` / `FAILED` / `RETRYING` |
| `message`, `subject` | subject is unused for SMS |
| `relatedEntityId` | free-text id of the source record (e.g. an alert id) |
| `retryCount`, `failureReason` | populated by the dispatch/retry pipeline |

### `EmailTemplate`
`name` (unique), `subject`, `body`. CRUD only for now — `EmailController.send` takes a subject/body directly rather than resolving a template; wiring template lookup into `EmailController` is a natural next step.

### `SmsNotification`
Local record of each simulated SMS send (`phoneNumber`, `message`, `providerResponse`). Written by `SmsDispatcher`.

---

## 4. API Surface

### Notifications — `/api/v1/notifications`
```http
POST   /api/v1/notifications          create + immediately attempt dispatch
GET    /api/v1/notifications
GET    /api/v1/notifications/{id}
PUT    /api/v1/notifications/{id}
DELETE /api/v1/notifications/{id}
POST   /api/v1/notifications/{id}/retry
```

Create request:
```json
{
  "recipient": "ops@reoagms.dev",
  "channel": "EMAIL",
  "subject": "High temperature alert",
  "message": "Sensor S-104 exceeded threshold.",
  "relatedEntityId": "alert-8f21"
}
```

### Email — `/api/v1/email`, `/api/v1/email-templates`
```http
POST   /api/v1/email                  send an ad-hoc email (creates + dispatches a Notification)
POST   /api/v1/email-templates
GET    /api/v1/email-templates
GET    /api/v1/email-templates/{id}
PUT    /api/v1/email-templates/{id}
DELETE /api/v1/email-templates/{id}
```

### SMS — `/api/v1/sms`
```http
POST /api/v1/sms                      send an SMS (creates + dispatches a Notification)
```

### Error response
```json
{
  "timestamp": "2026-08-30T02:20:00",
  "status": 404,
  "message": "Notification not found",
  "path": "/api/v1/notifications/...",
  "validationErrors": null
}
```

| Exception | HTTP status |
|---|---|
| `ResourceNotFoundException` | 404 |
| `NotificationDispatchException` | 503 |
| `MethodArgumentNotValidException` / `ConstraintViolationException` | 400 |
| `HttpMessageNotReadableException` | 400 |
| `Exception` (catch-all) | 500 |

---

## 5. Send & Retry Flow

```
POST /api/v1/notifications (or /email, /sms)
    │
    ▼
NotificationServiceImpl.create()
    ├─ save Notification (status = PENDING)
    └─ send()
         ├─ look up NotificationDispatcher for the channel
         ├─ dispatcher.dispatch(notification)
         ├─ success → status = SENT
         └─ failure → status = FAILED, retryCount++, failureReason set

NotificationRetryScheduler (fixed-delay job)
    └─ for every Notification with status = FAILED and retryCount < max-attempts:
         status = RETRYING → NotificationService.retry(id) → send() again
```

`EmailDispatcher` sends via `JavaMailSender` (SMTP). `SmsDispatcher` is currently a stub — it logs the message and writes an `SmsNotification` row instead of calling a real provider; swap its body for a Twilio (or similar) call once credentials are available.

---

## 6. Configuration

`src/main/resources/application.properties`:

| Property | Default | Purpose |
|---|---|---|
| `server.port` | `8086` | Service port |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/notification_db` | overridden by `SPRING_DATASOURCE_URL` in Docker |
| `spring.mail.host` / `port` | `smtp.gmail.com` / `587` | SMTP server |
| `spring.mail.username` / `password` | `${MAIL_USERNAME}` / `${MAIL_PASSWORD}` | SMTP credentials — required for real email sending |
| `notification.retry.max-attempts` | `3` | retry cap |
| `notification.retry.fixed-delay-ms` | `60000` | retry sweep interval |
| `management.health.mail.enabled` | `false` | prevents `/actuator/health` reporting `DOWN` just because SMTP creds aren't set |
| `spring.autoconfigure.exclude` | `AdminServerNotifierAutoConfiguration` | avoids a boot failure caused by `spring-boot-admin-starter-server` + `spring-boot-starter-mail` both being on the classpath (this service is not an Admin Server) |

Set real SMTP credentials via environment variables rather than committing them:
```powershell
$env:MAIL_USERNAME = 'you@gmail.com'
$env:MAIL_PASSWORD = 'app-password'
```

---

## 7. Running Locally

### Database
```sql
CREATE USER notification_user WITH PASSWORD 'notification_password';
CREATE DATABASE notification_db OWNER notification_user;
```
(Or use the Docker Compose service — see below — which provisions this automatically.)

### Build & run
From `backend/notification-service`:
```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'   # adjust to your JDK 21 install
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Health check: `GET http://localhost:8086/actuator/health`

### Docker Compose
From the repo root:
```powershell
docker compose up -d notification-db notification-service
```
This starts Postgres on host port `5437` and the service on `8086`, wired together on the `reoagms-network`. Set `MAIL_USERNAME`/`MAIL_PASSWORD` in your shell before `docker compose up` if you want real email delivery — otherwise the service still starts fine and email sends will fail gracefully (recorded as `FAILED`, retried by the scheduler, no crash).

---

## 8. Tests

Unit and MockMvc-slice tests live under `src/test/java`, covering `NotificationServiceImpl` (dispatch success/failure/no-dispatcher paths), `NotificationController` (validation + error mapping), `EmailTemplateServiceImpl`, `SmsDispatcher`, and a context-load smoke test (`NotificationServiceApplicationTests`, backed by an in-memory H2 database so it doesn't need a live Postgres).

```powershell
.\mvnw.cmd clean test
```

## 9. Postman Collection

An importable collection covering every endpoint (notifications CRUD + retry, email send + template CRUD, SMS send, health check) is at [`docs/notification-service.postman_collection.json`](docs/notification-service.postman_collection.json). It auto-captures `notificationId`/`emailTemplateId` from create responses into collection variables for the follow-up requests.

## 10. Database ER Diagram & Reference Schema

- ER diagram: [`docs/diagrams/notification-service-er-diagram.md`](../../docs/diagrams/notification-service-er-diagram.md)
- Reference SQL schema: [`docs/database/notification-service-schema.sql`](../../docs/database/notification-service-schema.sql)

Both are generated to match the actual JPA entities exactly (column lengths, nullability, defaults) — cross-check against `notification/model/Notification.java`, `email/model/EmailTemplate.java`, and `sms/model/SmsNotification.java` if entities change.

---

## 11. Known Limitations / Next Steps

- `SmsDispatcher` is a stub — no real SMS provider is wired in yet.
- `EmailController.send` doesn't resolve an `EmailTemplate` by name/placeholders — it takes subject/body directly. Template-based sending needs to be added once a placeholder syntax is agreed.
- No push notification dispatcher yet (`PUSH` exists as an enum value only).
- `ddl-auto=update` is fine for coursework but not production — migrate to Flyway/Liquibase before a real deployment.
- No RabbitMQ consumer yet — once Monitoring/Maintenance publish events, this service should consume them directly instead of requiring synchronous REST calls from those services.
- No JWT/RBAC enforced at this service directly — relies on the API Gateway for auth (defense-in-depth can be added later).

## Integration handoff to Charith

Route the following to `http://notification-service:8086` at the gateway:
```
/api/v1/notifications/**
/api/v1/email/**
/api/v1/email-templates/**
/api/v1/sms/**
```
