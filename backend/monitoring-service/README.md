# Monitoring Service

> **Service owner:** Govindu | **Port:** `8083` | **DB:** `monitoring_db` (PostgreSQL)
> **Stack:** Java 21 · Spring Boot 4.1.0 · Spring MVC · Spring Data JPA · Lombok · Maven

---

## 1. Service Responsibility & Boundary

The Monitoring Service is the real-time data-ingestion and alerting layer of REOAGMS. It:

1. Receives sensor readings from field devices (or a simulator) and stores them as a time series.
2. Tracks device connectivity/heartbeat separately from metric values (`Telemetry`).
3. Evaluates configurable threshold rules (`AlertRule`) against every incoming reading and automatically raises `Alert`s when a rule is breached — alerts can also be raised manually.
4. Exposes historical/dashboard read APIs that Analytics Service and the frontend consume.

**It never touches another service's database.** `sensorId`, `assetId`, and `facilityId` are plain `UUID` columns with no foreign keys — they are owned by Asset Service.

```
monitoring_db owns:        sensor_readings · alert_rules · alerts · telemetry
monitoring_db does NOT own: facilities · assets · sensors (Asset Service)
```

### Contract with Analytics Service

Analytics Service pulls cross-service data via `GET` calls (see `ExternalDataServiceImpl` in `analytics-service`). To keep that contract intact, two endpoints intentionally return a **plain JSON array**, not the `ApiResponse` envelope used elsewhere in this service:

| Endpoint | Query params | Consumed by |
|---|---|---|
| `GET /api/v1/sensor-readings` | `facilityId`, `assetId`, `sensorId`, `metricType`, `from`, `to` | Analytics Service, dashboards |
| `GET /api/v1/alerts` | `facilityId`, `assetId`, `status`, `from`, `to` | Analytics Service, dashboards |

Do not wrap these two list responses without updating `ExternalDataServiceImpl` in `analytics-service` at the same time.

---

## 2. Package Architecture

```
com.reoagms.monitoring_service
├── MonitoringServiceApplication.java   ← Spring Boot entry point
│
├── config/
│   └── JpaAuditConfig.java             ← Enables @CreatedDate / @LastModifiedDate
│
├── common/
│   ├── enums/                          ← MetricType, ConditionOperator, AlertSeverity,
│   │                                      AlertStatus, DeviceStatus
│   ├── exception/                      ← ResourceNotFoundException, InvalidRequestException,
│   │                                      GlobalExceptionHandler
│   └── model/
│       ├── BaseEntity.java             ← UUID PK + createdAt + updatedAt (JPA Auditing)
│       ├── ApiResponse<T>.java         ← Standard success envelope
│       └── ErrorResponse.java          ← Standard error envelope
│
├── model/            ← JPA @Entity classes (4 aggregates)
│   ├── SensorReading.java
│   ├── AlertRule.java
│   ├── Alert.java
│   └── Telemetry.java
│
├── repository/        ← Spring Data JPA repositories (+ Specification support)
├── dto/                ← Request/Response DTOs, never expose entities
├── mapper/             ← Entity <-> DTO conversion
├── service/            ← Business logic, incl. rule evaluation
├── controller/         ← REST endpoints only; validate + delegate
└── util/
    ├── ConditionEvaluator.java          ← Threshold comparison logic
    └── SensorReadingSpecifications.java ← Dynamic JPA Criteria filters
```

---

## 3. Domain Model

- **SensorReading** — one metric value (temperature, voltage, power, …) at a point in time for a sensor/asset.
- **Telemetry** — a lower-frequency device connectivity/heartbeat signal (`ONLINE` / `OFFLINE` / `WARNING`), separate from metric values.
- **AlertRule** — a threshold condition (`metricType` + `operator` + `threshold`) optionally scoped to one asset; when `assetId` is null it applies to every asset for that metric.
- **Alert** — raised automatically when a `SensorReading` breaches an enabled `AlertRule`, or manually via `POST /api/v1/alerts`. Lifecycle: `OPEN → ACKNOWLEDGED → RESOLVED`.

See [`docs/diagrams/monitoring-service-er-diagram.md`](../../docs/diagrams/monitoring-service-er-diagram.md) for the full ER diagram and [`docs/database/monitoring-service-schema.sql`](../../docs/database/monitoring-service-schema.sql) for the reference DDL.

### Alert generation flow

```
POST /api/v1/sensor-readings
        │
        ▼
SensorReadingServiceImpl.create()
        │  saves the reading
        ▼
AlertServiceImpl.evaluateReading()
        │  loads enabled AlertRules matching (assetId, metricType)
        │  + global rules (assetId = null) for that metricType
        ▼
ConditionEvaluator.isBreached(operator, value, threshold)
        │  for each breached rule
        ▼
Alert saved with status = OPEN, severity = rule.severity
```

---

## 4. Running Locally

```bash
# 1. Create the database (see docs/database/monitoring-service-schema.sql)
CREATE USER monitoring_user WITH PASSWORD 'monitoring123';
CREATE DATABASE monitoring_db OWNER monitoring_user;

# 2. Build & test (uses an in-memory H2 database, no Postgres needed for tests)
./mvnw clean install

# 3. Run (connects to the Postgres database above)
./mvnw spring-boot:run
# http://localhost:8083/api/v1
```

All configuration in `application.properties` is overridable via environment variables (`MONITORING_SERVICE_PORT`, `MONITORING_DB_URL`, `MONITORING_DB_USERNAME`, `MONITORING_DB_PASSWORD`, `MONITORING_JPA_DDL_AUTO`, `MONITORING_SHOW_SQL`) for Docker/Kubernetes integration.

---

## 5. REST API Summary

Full contract: [`docs/api/monitoring-service-openapi.yaml`](../../docs/api/monitoring-service-openapi.yaml). Postman collection: [`docs/api/monitoring-service.postman_collection.json`](../../docs/api/monitoring-service.postman_collection.json).

| Resource | Endpoints |
|---|---|
| Sensor Readings | `POST /api/v1/sensor-readings`, `GET /api/v1/sensor-readings`, `GET /api/v1/sensor-readings/{id}`, `GET /api/v1/sensor-readings/sensor/{sensorId}`, `GET /api/v1/sensor-readings/sensor/{sensorId}/latest`, `GET /api/v1/sensor-readings/asset/{assetId}` |
| Alert Rules | `POST /api/v1/alert-rules`, `GET /api/v1/alert-rules`, `GET /api/v1/alert-rules/{id}`, `PUT /api/v1/alert-rules/{id}`, `DELETE /api/v1/alert-rules/{id}` |
| Alerts | `POST /api/v1/alerts`, `GET /api/v1/alerts`, `GET /api/v1/alerts/{id}`, `PUT /api/v1/alerts/{id}/acknowledge`, `PUT /api/v1/alerts/{id}/resolve` |
| Telemetry | `POST /api/v1/telemetry`, `GET /api/v1/telemetry`, `GET /api/v1/telemetry/asset/{assetId}`, `GET /api/v1/telemetry/asset/{assetId}/latest` |
| Dashboard | `GET /api/v1/dashboard/summary` |

### Example: ingest a reading that breaches a rule

```http
POST http://localhost:8083/api/v1/sensor-readings
Content-Type: application/json

{
  "sensorId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "assetId": "3fa85f64-5717-4562-b3fc-2c963f66afa7",
  "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa8",
  "timestamp": "2026-08-14T12:00:00",
  "value": 95.0,
  "unit": "C",
  "metricType": "TEMPERATURE"
}
```

If an enabled `AlertRule` exists for `(assetId, TEMPERATURE)` with `GREATER_THAN 80`, an `Alert` with `status=OPEN` is created automatically and is visible via `GET /api/v1/alerts?status=OPEN`.

---

## 6. Testing

```bash
./mvnw test
```

- `ConditionEvaluatorTest` — unit tests for every threshold operator.
- `AlertServiceImplTest` — verifies rule evaluation raises/does not raise alerts correctly (Mockito).
- `SensorReadingControllerTest` — MockMvc tests confirming the list/create endpoints return the plain-array/plain-object shape Analytics Service depends on, and that 404s produce the standard error envelope.
- `MonitoringServiceApplicationTests` — Spring context load test against an in-memory H2 database (`src/test/resources/application-test.properties`), so CI does not require a running Postgres instance.

---

## 7. Definition of Done Checklist

- [x] Independent PostgreSQL database (`monitoring_db`)
- [x] Complete CRUD/API functionality for all 4 entities
- [x] DTO + mapper architecture (entities never exposed)
- [x] Service / repository / controller layered architecture
- [x] Bean Validation (`jakarta.validation`) + `GlobalExceptionHandler`
- [x] Unit and MockMvc tests
- [x] README (this file)
- [x] Postman collection
- [x] Database ER diagram
- [x] OpenAPI documentation
- [ ] Feature branch pushed for review
- [ ] API Gateway / RabbitMQ integration (owned by Charith, Final Integration phase)
