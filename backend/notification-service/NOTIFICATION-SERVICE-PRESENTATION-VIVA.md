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

