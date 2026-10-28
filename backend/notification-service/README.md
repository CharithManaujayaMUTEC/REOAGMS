# Notification Service

The Notification Service is the REOAGMS microservice responsible for creating,
delivering, tracking, and retrying notifications. It supports email, SMS, and
push notification channels and stores notification data in PostgreSQL.

## Service Information

- **Application name:** `notification-service`
- **Port:** `8085`
- **Database:** PostgreSQL database `reoagms_notification`
- **Base package:** `com.reoagms.notification_service`

## My Contribution

My contribution to REOAGMS is the Notification Service. I implemented the
service as an independent Spring Boot microservice with the following
responsibilities:

- Designed the notification service structure using controllers, DTOs, models,
  repositories, mappers, and service classes.
- Implemented notification creation, retrieval, listing, deletion, and manual
  retry operations.
- Implemented support for email, SMS, and push notification channels.
- Implemented email delivery using Spring Mail and SMTP.
- Implemented SMS notification request handling and delivery service structure.
- Implemented push notification service structure for future provider
  integration.
- Implemented reusable email templates with placeholder replacement, such as
  `{{recipientName}}` and `{{alertType}}`.
- Implemented notification statuses including pending, sent, and failed.
- Implemented failure tracking through retry counts and failure reasons.
- Implemented scheduled retry processing for failed notifications.
- Added request validation using Jakarta Bean Validation.
- Added PostgreSQL persistence using Spring Data JPA and Hibernate.
- Added Actuator configuration for service monitoring.

## Features

### General Notifications

Notifications contain:

- Recipient
- Channel
- Subject
- Message
- Delivery status
- Retry count
- Failure reason
- Source service
- Reference ID
- Created and updated timestamps

### Email Notifications

Email notifications can be sent directly with a subject and message or through
an email template. Templates support placeholder replacement before delivery.

### SMS Notifications

SMS requests contain a phone number and message. The service tracks the SMS
status and retry count in PostgreSQL.

### Push Notifications

The push notification service is currently a provider-ready implementation.
The integration point can be connected to a provider such as Firebase Cloud
Messaging or Apple Push Notification service.

### Retry Mechanism

When delivery fails, the notification is marked as failed, the retry count is
increased, and the failure reason is recorded. A scheduled job checks failed
notifications and retries them until the configured maximum attempt count is
reached.

Default retry configuration:

```properties
notification.retry.max-attempts=3
notification.retry.fixed-delay-ms=60000
```

## API Endpoints

### Notifications

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/notifications` | Create and dispatch a notification |
| `GET` | `/api/v1/notifications` | Get all notifications |
| `GET` | `/api/v1/notifications/{id}` | Get a notification by ID |
| `DELETE` | `/api/v1/notifications/{id}` | Delete a notification |
| `POST` | `/api/v1/notifications/{id}/retry` | Retry a notification manually |

### Email

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/email` | Send a direct or template-based email |

### SMS

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/sms` | Send an SMS notification |
| `GET` | `/api/v1/sms` | Get all SMS notifications |
| `GET` | `/api/v1/sms/{id}` | Get an SMS notification by ID |
| `DELETE` | `/api/v1/sms/{id}` | Delete an SMS notification |

### Email Templates

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/email-templates` | Create an email template |
| `GET` | `/api/v1/email-templates` | Get all email templates |
| `GET` | `/api/v1/email-templates/{id}` | Get a template by ID |
| `PUT` | `/api/v1/email-templates/{id}` | Update an email template |
| `DELETE` | `/api/v1/email-templates/{id}` | Delete an email template |

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| API | Spring Web MVC / REST |
| Persistence | Spring Data JPA and Hibernate |
| Database | PostgreSQL |
| Email | Spring Boot Starter Mail and SMTP |
| Validation | Jakarta Bean Validation |
| Monitoring | Spring Boot Actuator |
| Boilerplate reduction | Project Lombok |
| Build tool | Maven Wrapper |
| Testing | JUnit 5 and Spring Boot test starters |

## Configuration

The service configuration is located in
`src/main/resources/application.properties`.

Important settings include:

```properties
server.port=8085
spring.datasource.url=jdbc:postgresql://localhost:5432/reoagms_notification
spring.mail.host=smtp.gmail.com
spring.mail.port=587
```

Set real SMTP credentials before sending email notifications:

```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

## Running Locally

Create the PostgreSQL database first:

```sql
CREATE DATABASE reoagms_notification;
```

From the `notification-service` directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The service will be available at:

```text
http://localhost:8085
```

## Testing

Run the service tests with:

```powershell
.\mvnw.cmd test
```

The current test suite includes a Spring application context test. Endpoint,
delivery, retry, and repository integration tests can be added as the service
is extended.

## Future Integrations

- Connect SMS delivery to a provider such as Twilio or AWS SNS.
- Connect push delivery to Firebase Cloud Messaging or APNs.
- Move SMTP credentials and database credentials to environment variables or a
  secrets manager.
- Add integration tests for email, SMS, retry, and template workflows.
