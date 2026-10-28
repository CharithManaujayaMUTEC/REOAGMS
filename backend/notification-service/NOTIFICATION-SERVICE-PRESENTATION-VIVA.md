# Notification Service Presentation and Viva Guide

## 1. Opening Explanation

You can start your presentation like this:

> The Notification Service is an independent Spring Boot microservice in the REOAGMS system. Its responsibility is to create, send, track, and retry notifications through different channels such as email, SMS, and push notifications. It uses REST APIs for communication, PostgreSQL for persistence, and Spring Data JPA for database access.

The main service is located in `backend/notification-service/`.

## 2. Architecture

```text
Client
  |
  v
REST Controllers
  |
  v
DTOs
  |
  v
Service Layer
  |
  v
Mappers
  |
  v
Repositories
  |
  v
PostgreSQL Database
```

> The service follows a layered architecture. Each layer has a specific responsibility. Controllers handle HTTP requests, services contain business logic, repositories communicate with the database, models represent database entities, and DTOs transfer data between the client and the application.

## 3. Layers

### Controller Layer

Relevant classes:

- `NotificationController`
- `EmailController`
- `SmsController`
- `EmailTemplateController`

> The controller layer is the entry point of the application. It receives REST API requests, validates the incoming data, calls the appropriate service, and returns an HTTP response.

Examples:

```text
POST /api/v1/notifications
POST /api/v1/email
POST /api/v1/sms
POST /api/v1/email-templates
```

The controllers should not contain complex business logic. Their responsibility is request handling and response generation.

### DTO Layer

Examples:

- `NotificationRequest`
- `EmailSendRequest`
- `SMSNotificationRequest`
- `NotificationResponse`
- `SMSNotificationResponse`
- `EmailTemplateRequest`
- `EmailTemplateResponse`

> DTOs, or Data Transfer Objects, define the structure of requests and responses. They prevent the application from exposing database entities directly through the API. They also allow us to apply validation rules to incoming data.

A notification request may contain:

```text
recipient
channel
subject
message
sourceService
referenceId
```

### Service Layer

Important classes:

- `NotificationServiceImpl`
- `SMSNotificationServiceImpl`
- `EmailTemplateServiceImpl`
- `EmailSenderService`
- `SmsSenderService`
- `PushSenderService`
- `NotificationRetryScheduler`

> The service layer contains the business logic. It decides how a notification should be processed based on its channel. Email notifications are sent through SMTP, SMS processing is handled through the SMS service, and push notifications currently have a provider-ready implementation.

> In this implementation, email delivery is handled by `EmailSenderService`, while email template CRUD operations are handled by `EmailTemplateServiceImpl`. Similarly, SMS notification management is handled by `SMSNotificationServiceImpl`, while the actual SMS delivery responsibility is represented by `SmsSenderService`.

### Repository Layer

Classes:

- `NotificationRepository`
- `EmailTemplateRepository`
- `SMSNotificationRepository`

> The repository layer provides database access using Spring Data JPA. It allows the service to save, retrieve, update, and delete notifications without writing SQL for standard operations.

The repository layer communicates with PostgreSQL entities such as:

- `Notification`
- `EmailTemplate`
- `SMSNotification`

### Model Layer

Classes:

- `Notification`
- `EmailTemplate`
- `SMSNotification`
- `BaseEntity`

> The model layer represents the persistent data stored in the database. `Notification` stores general notification information, `EmailTemplate` stores reusable email content, and `SMSNotification` stores SMS-specific information such as the phone number and status.

`BaseEntity` provides common fields such as:

```text
id
createdAt
updatedAt
```

### Enum Layer

Enums:

- `NotificationChannel`
- `NotificationStatus`

> Enums restrict values to a predefined set. `NotificationChannel` defines supported channels such as EMAIL, SMS, and PUSH. `NotificationStatus` defines the notification lifecycle, such as PENDING, SENT, and FAILED.

```text
NotificationChannel:
EMAIL
SMS
PUSH

NotificationStatus:
PENDING
SENT
FAILED
```

### Exception Layer

Classes:

- `GlobalExceptionHandler`
- `NotificationNotFoundException`
- `EmailTemplateNotFoundException`
- `SMSNotificationNotFoundException`
- `ApiError`

> The exception layer provides centralized error handling. Instead of handling errors separately in every controller, `GlobalExceptionHandler` converts exceptions into consistent API error responses.

Example response:

```json
{
  "message": "Notification not found",
  "status": 404
}
```

## 4. Notification Flow

```text
Client
  |
  | POST /api/v1/notifications
  v
NotificationController
  |
  v
NotificationServiceImpl
  |
  | Create Notification entity
  | Set status to PENDING
  | Save using repository
  |
  | Select delivery channel
  v
EmailSenderService / SmsSenderService / PushSenderService
  |
  v
Update status to SENT or FAILED
  |
  v
Return NotificationResponse
```

> When the client sends a notification request, the controller receives and validates it. The service converts the request into a notification model and initially assigns the `PENDING` status. The notification is saved in PostgreSQL. Based on the selected channel, the appropriate sender service is called. If delivery succeeds, the status becomes `SENT`; otherwise, it becomes `FAILED`, and the failure reason and retry count can be recorded.

## 5. Email Notifications

> Email notifications use Spring Mail and SMTP. The service supports both direct email sending and template-based email sending. An email template can contain reusable content and placeholders such as `{{recipientName}}` or `{{alertType}}`. Before sending, these placeholders are replaced with actual values.

Relevant classes:

- `EmailController`
- `EmailSenderService`
- `EmailTemplateServiceImpl`

## 6. SMS Notifications

> SMS requests contain a phone number and a message. The SMS notification is stored separately so that SMS-specific information can be tracked. The current sender class provides the integration point for connecting an external SMS provider such as Twilio or AWS SNS.

Relevant classes:

- `SmsController`
- `SMSNotificationServiceImpl`
- `SmsSenderService`

Be accurate in the viva:

> The SMS provider integration is currently a structured placeholder. The application supports SMS request handling and persistence, while a real provider can be connected through `SmsSenderService`.

## 7. Retry Handling

Relevant class:

- `NotificationRetryScheduler`

> If a notification fails, the service records the failure reason and retry count. A scheduled retry process periodically searches for failed notifications and attempts delivery again until the maximum retry limit is reached.

Configuration:

```properties
notification.retry.max-attempts=3
notification.retry.fixed-delay-ms=60000
```

This means:

- Maximum retry attempts: `3`
- Retry interval: `60 seconds`

## 8. Database

> The service uses PostgreSQL with Spring Data JPA. The main database entities are notifications, email templates, and SMS notifications. JPA maps the Java model classes to database tables, while repositories provide database operations.

Database configuration:

```text
Database: reoagms_notification
Port: 5432
Application port: 8085
```

Do not expose real passwords or secrets during the presentation. Explain that credentials should be moved to environment variables or a secrets manager in production.

## 9. API Endpoints

### Notification APIs

```text
POST   /api/v1/notifications
GET    /api/v1/notifications
GET    /api/v1/notifications/{id}
DELETE /api/v1/notifications/{id}
POST   /api/v1/notifications/{id}/retry
```

### Email APIs

```text
POST /api/v1/email
```

### SMS APIs

```text
POST   /api/v1/sms
GET    /api/v1/sms
GET    /api/v1/sms/{id}
DELETE /api/v1/sms/{id}
```

### Email Template APIs

```text
POST   /api/v1/email-templates
GET    /api/v1/email-templates
GET    /api/v1/email-templates/{id}
PUT    /api/v1/email-templates/{id}
DELETE /api/v1/email-templates/{id}
```

## 10. `.gitignore`

> The `.gitignore` file prevents generated and environment-specific files from being committed to Git. For Java projects, compiled classes, JAR files, Maven target directories, and IDE files are generated automatically, so they do not belong in source control.

Relevant entries:

```gitignore
target/
*.class
*.jar
/backend/identity-service/bin/
```

These entries mean:

- `target/` ignores Maven build output.
- `*.class` ignores compiled Java bytecode.
- `*.jar` ignores generated Java archives.
- `/backend/identity-service/bin/` ignores the generated Eclipse or IDE output directory for the identity service.
- `.env` prevents environment secrets from being committed.
- `.idea/` and `.vscode/` prevent personal IDE settings from being committed.

> The `bin/` directory is ignored because it contains generated or duplicate build and IDE artifacts. It is not part of the notification-service source code.

## 11. Common Viva Questions and Answers

### Why did you use a layered architecture?

> Layered architecture separates responsibilities. It improves maintainability, testing, readability, and allows one layer to change without heavily affecting the others.

### Why use DTOs instead of returning entities?

> DTOs prevent direct exposure of database models, provide controlled API responses, and allow request validation.

### Why use repositories?

> Repositories abstract database operations and reduce the need to write repetitive SQL code.

### Why do you need notification status?

> Status allows us to track the delivery lifecycle of a notification. It tells us whether the notification is pending, successfully sent, or failed.

### Why is the initial status `PENDING`?

> A notification may be saved before or while delivery is attempted. `PENDING` represents that delivery has not yet completed.

### What happens if email delivery fails?

> The notification is marked as failed, the failure reason is recorded, and the retry mechanism can attempt delivery again according to the retry configuration.

### Why separate SMS notifications from general notifications?

> SMS notifications have SMS-specific data, such as phone number and SMS delivery information. Separating them allows the SMS workflow to evolve independently.

### Is the SMS provider fully implemented?

> The SMS request and persistence flow is implemented, but the external provider integration is currently structured as a provider-ready component. A provider such as Twilio or AWS SNS can be connected later.

### Why use enums?

> Enums prevent invalid values and make the allowed channels and statuses explicit and type-safe.

### What is the role of `GlobalExceptionHandler`?

> It centralizes exception handling and returns consistent HTTP error responses across the API.

### What is the purpose of `NotificationRetryScheduler`?

> It periodically checks failed notifications and retries delivery until the configured maximum attempt count is reached.

## 12. Short Viva Summary

> My notification service is a Spring Boot microservice that manages email, SMS, and push notifications. It follows a layered architecture consisting of controllers, DTOs, services, repositories, models, mappers, enums, and exception handling. Controllers expose REST endpoints, services implement business logic, repositories provide PostgreSQL access, and models represent persisted data. Notifications have channels and statuses, such as EMAIL, SMS, PUSH, PENDING, SENT, and FAILED. The service also supports reusable email templates, SMTP email delivery, SMS request handling, failure tracking, and scheduled retries. Generated files such as Maven target files, compiled classes, JAR files, and IDE artifacts are excluded using `.gitignore`.
