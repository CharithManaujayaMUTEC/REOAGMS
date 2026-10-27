# REOAGMS

REOAGMS is a backend platform for managing renewable-energy facilities, their
assets, user access, and operational notifications. The application is built
as a set of Spring Boot microservices with an API gateway as the main entry
point.

## Architecture

```text
Client applications
				|
				v
API Gateway :8080
	 |       |        |
	 v       v        v
Identity  Asset   Notification
:8081    :8082      :8085
	 |       |          |
PostgreSQL databases  PostgreSQL database
```

The gateway routes requests, applies CORS rules, and validates JWT bearer
tokens. The services are independently deployable Spring Boot applications.

## Services

### API Gateway

- Runs on port `8080`.
- Routes identity requests to the identity service.
- Allows public authentication routes: `/api/v1/auth/login` and
  `/api/v1/auth/register`.
- Validates JWT tokens for secured routes.
- Provides CORS configuration for local frontends on ports `3000` and `5173`.

### Identity Service

- Runs on port `8081`.
- User registration and login through `/api/v1/auth`.
- JWT-based stateless authentication.
- BCrypt password hashing.
- Role-based authorization for users and profiles.
- User management through `/api/v1/users`.
- Current-user profile through `/api/v1/profile/me`.
- Persists users and roles in PostgreSQL.

### Asset Service

- Runs on port `8082`.
- CRUD operations for facilities through `/api/v1/facilities`.
- CRUD operations for assets through `/api/v1/assets`.
- Links assets to facilities and supports lookup by facility.
- Persists facility and asset data in PostgreSQL.

### Notification Service

- Runs on port `8085`.
- Sends and tracks email, SMS, and push notifications.
- Provides notification CRUD and manual retry through
  `/api/v1/notifications`.
- Provides email delivery through `/api/v1/email`.
- Provides SMS operations through `/api/v1/sms`.
- Provides reusable email templates through `/api/v1/email-templates`.
- Tracks notification status, failure reasons, and retry counts.
- Automatically retries failed notifications on a scheduled interval.
- Uses SMTP for email; SMS and push integrations are currently placeholders.

## Tech Stack

| Area                  | Technology                                              |
| --------------------- | ------------------------------------------------------- |
| Language              | Java 21                                                 |
| Framework             | Spring Boot 4.1.0                                       |
| API                   | Spring Web MVC / REST                                   |
| Gateway               | Spring Cloud Gateway 2025.1.2                           |
| Security              | Spring Security, JWT, BCrypt                            |
| JWT library           | JJWT 0.12.7                                             |
| Persistence           | Spring Data JPA, Hibernate                              |
| Database              | PostgreSQL                                              |
| Validation            | Jakarta Bean Validation                                 |
| Email                 | Spring Boot Starter Mail / SMTP                         |
| Monitoring            | Spring Boot Actuator and Spring Boot Admin dependencies |
| Boilerplate reduction | Project Lombok                                          |
| Build tool            | Maven with Maven Wrapper                                |
| Testing               | JUnit 5 and Spring Boot test starters                   |

## Project Structure

```text
backend/
	api-gateway/
	identity-service/
	asset-service/
	notification-service/
```

Each service follows a layered structure with controllers, DTOs, services,
repositories, entities/models, mappers, configuration, and exception
handling where applicable.

## Requirements

- Java 21 or later
- PostgreSQL
- Maven is optional because each service includes `mvnw` and `mvnw.cmd`

## Local Database Setup

Create the databases used by the services:

```sql
CREATE DATABASE reoagms;
CREATE DATABASE reoagms_asset;
CREATE DATABASE reoagms_notification;
```

The default local configuration expects PostgreSQL username `postgres` and
password `password`. Update each service's
`src/main/resources/application.properties` for your local environment.

## Running the Services

Open a terminal for each service and run:

```powershell
cd backend\identity-service
\.\mvnw.cmd spring-boot:run
```

```powershell
cd backend\asset-service
\.\mvnw.cmd spring-boot:run
```

```powershell
cd backend\notification-service
\.\mvnw.cmd spring-boot:run
```

```powershell
cd backend\api-gateway
\.\mvnw.cmd spring-boot:run
```

The gateway is intended to be the main client-facing entry point at
`http://localhost:8080`.

## Testing

Run the tests from a service directory:

```powershell
\.\mvnw.cmd test
```

The current test suites include Spring application context smoke tests. More
endpoint, security, persistence, and integration tests should be added as the
platform grows.

## My Contribution

Based on the implementation currently present in this repository, my work is
focused on backend development and includes:

- Designing the microservice structure and service boundaries.
- Implementing the API gateway, route handling, CORS configuration, and JWT
  request validation.
- Implementing user registration, login, JWT authentication, password
  hashing, roles, and authorization rules.
- Implementing facility and asset management with REST APIs, DTOs, services,
  repositories, validation, and PostgreSQL persistence.
- Implementing notification management for email, SMS, and push channels.
- Implementing email templates, SMTP email sending, notification status
  tracking, failure handling, and scheduled retries.
- Configuring Maven builds, Java 21, Spring Boot Actuator, PostgreSQL, and
  application ports for local development.

This contribution summary is based on the source code in the checkout. Git
commit history is not available here, so individual ownership of each file
cannot be independently verified.

## Security Notes

The repository contains development-only database credentials, SMTP
placeholders, and JWT secret values in local properties files. Replace these
with environment variables or a secrets manager before deploying the system.
