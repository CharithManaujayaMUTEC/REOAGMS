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

