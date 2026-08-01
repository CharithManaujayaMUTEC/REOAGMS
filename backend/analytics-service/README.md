# REOAGMS Analytics & Reporting Service

The Analytics & Reporting Service is Sachinthana's independently deployable REOAGMS microservice. It calculates dashboard summaries, energy-generation reports, asset/facility performance reports, maintenance statistics, utilization statistics, and KPIs. It stores only analytics-owned data in `analytics_db`.

## Ownership boundary

This service owns:

- Dashboard configurations
- Generated energy-report snapshots
- Generated performance-report snapshots
- KPI records
- Analytics calculation and CSV-export logic

This service does not own users, facilities, assets, sensor readings, alerts, work orders, notifications, gateway routing, RabbitMQ infrastructure, Docker, Kubernetes, or frontend code. It never queries another service's database. Operational inputs are obtained through REST APIs.

## Technology

- Java 21
- Spring Boot 4.1.0
- Spring MVC
- Spring Data JPA
- Spring Validation
- Lombok
- PostgreSQL
- Maven

## Package structure

The code follows the package structure required by the development guide:

```text
com.reoagms.analytics_service
├── config
├── common
│   ├── enums
│   ├── exception
│   └── model
├── controller
├── dto
├── mapper
├── model
├── repository
├── service
└── util
```

Business rules and calculations are in the service layer. Controllers validate and delegate, repositories contain persistence operations only, and JPA entities are never returned directly.

## Manual setup

### 1. Select JDK 21

Check the active Java and Maven runtimes:

```powershell
java -version
mvn -version
```

Both should report Java 21. On the current Windows machine Maven may use an older `JAVA_HOME` even when `java` points to a newer JDK. For the current PowerShell session:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn -version
```

### 2. Create the PostgreSQL user and database

Run these as a PostgreSQL administrator. Change the password for shared or production environments.

```sql
CREATE USER analytics_user WITH PASSWORD 'analytics_password';
CREATE DATABASE analytics_db OWNER analytics_user;
```

The full reference schema is in `docs/database/analytics-service-schema.sql`. By default, Hibernate creates or updates the tables after the database itself exists.

### 3. Configure environment variables

Defaults support local development, but the following variables can override them:

| Variable | Default | Purpose |
|---|---|---|
| `ANALYTICS_SERVICE_PORT` | `8086` | Service port |
| `ANALYTICS_DB_URL` | `jdbc:postgresql://localhost:5432/analytics_db` | Analytics database URL |
| `ANALYTICS_DB_USERNAME` | `analytics_user` | Dedicated DB user |
| `ANALYTICS_DB_PASSWORD` | `analytics_password` | Dedicated DB password |
| `ANALYTICS_JPA_DDL_AUTO` | `update` | Hibernate schema behavior |
| `MONITORING_SERVICE_URL` | `http://localhost:8083` | Monitoring Service base URL |
| `ASSET_SERVICE_URL` | `http://localhost:8082` | Asset Service base URL |
| `MAINTENANCE_SERVICE_URL` | `http://localhost:8084` | Maintenance Service base URL |
| `INTEGRATION_CONNECT_TIMEOUT_SECONDS` | `3` | REST connect timeout |
| `INTEGRATION_READ_TIMEOUT_SECONDS` | `10` | REST response timeout |

Example:

```powershell
$env:ANALYTICS_DB_PASSWORD = 'your-local-password'
$env:MONITORING_SERVICE_URL = 'http://localhost:8083'
$env:ASSET_SERVICE_URL = 'http://localhost:8082'
$env:MAINTENANCE_SERVICE_URL = 'http://localhost:8084'
```

### 4. Build and run

From `backend/analytics-service`:

```powershell
mvn clean test
mvn spring-boot:run
```

The service is then available at `http://localhost:8086`. Health check:

```http
GET http://localhost:8086/actuator/health
```

## API summary

### Dashboard configuration CRUD

```http
POST   /api/v1/dashboards
GET    /api/v1/dashboards
GET    /api/v1/dashboards/{id}
PUT    /api/v1/dashboards/{id}
DELETE /api/v1/dashboards/{id}
```

### Live dashboard

```http
GET /api/v1/dashboard?facilityId={uuid}&from=2026-01-01T00:00:00&to=2026-01-02T00:00:00
```

If dates are omitted, the dashboard uses the previous 24 hours. Either `facilityId` or `assetId` is required.

### Energy reports

```http
POST   /api/v1/energy-reports
GET    /api/v1/energy-reports
GET    /api/v1/energy-reports/{id}
PUT    /api/v1/energy-reports/{id}
DELETE /api/v1/energy-reports/{id}
GET    /api/v1/energy-reports/{id}/export?format=CSV
```

Create/regenerate request:

```json
{
  "name": "Solar Farm Daily Energy",
  "facilityId": "00000000-0000-0000-0000-000000000001",
  "assetId": null,
  "periodStart": "2026-01-01T00:00:00",
  "periodEnd": "2026-01-02T00:00:00",
  "expectedEnergy": 12500.00
}
```

`expectedEnergy` is optional. When omitted, expected energy is calculated from total asset rated capacity multiplied by the reporting hours.

### Performance reports

```http
POST   /api/v1/performance-reports
GET    /api/v1/performance-reports
GET    /api/v1/performance-reports/{id}
PUT    /api/v1/performance-reports/{id}
DELETE /api/v1/performance-reports/{id}
GET    /api/v1/performance-reports/{id}/export?format=CSV
```

Request:

```json
{
  "name": "Monthly Asset Performance",
  "scopeType": "ASSET",
  "scopeId": "00000000-0000-0000-0000-000000000002",
  "periodStart": "2026-01-01T00:00:00",
  "periodEnd": "2026-02-01T00:00:00"
}
```

`scopeType` supports `ORGANIZATION`, `FACILITY`, and `ASSET`. `scopeId` is required for facility and asset scopes and must be omitted for organization scope.

### KPI CRUD

```http
POST   /api/v1/kpis
GET    /api/v1/kpis
GET    /api/v1/kpis/{id}
PUT    /api/v1/kpis/{id}
DELETE /api/v1/kpis/{id}
```

### Combined reports and statistics

```http
GET /api/v1/reports
GET /api/v1/statistics?facilityId={uuid}&from=2026-01-01T00:00:00&to=2026-02-01T00:00:00&groupBy=DAY
```

`groupBy` supports `HOUR`, `DAY`, `WEEK`, and `MONTH`.

The complete API contract is in `docs/api/analytics-service-openapi.yaml`. An importable Postman collection is in `docs/api/analytics-service.postman_collection.json`.

## Analytics formulas

All energy values are normalized to kWh. Supported input units are `Wh`, `kWh`, `MWh`, and `GWh`.

```text
Total energy = sum(normalized sensor readings)
Average energy = total energy / number of readings
Expected energy = sum(asset rated capacity in kW) × reporting hours
Variance % = (actual energy - expected energy) / expected energy × 100
Availability % = (scheduled asset-hours - downtime hours) / scheduled asset-hours × 100
Capacity factor % = actual energy / maximum rated energy × 100
Utilization % = operational assets / total assets × 100
Maintenance completion % = completed work orders / total work orders × 100
```

Efficiency currently uses capacity factor as its fallback because the planned system does not yet provide weather-adjusted or forecast energy. This must be replaced when a forecast/irradiance/wind model becomes available.

## Required upstream contracts

The service currently expects direct JSON objects/lists, matching the existing Asset Service controller style. It calls:

```http
GET {MONITORING_SERVICE_URL}/api/v1/sensor-readings?facilityId=&assetId=&from=&to=
GET {MONITORING_SERVICE_URL}/api/v1/alerts?facilityId=&assetId=&from=&to=
GET {ASSET_SERVICE_URL}/api/v1/assets
GET {ASSET_SERVICE_URL}/api/v1/assets/{id}
GET {ASSET_SERVICE_URL}/api/v1/assets/facility/{facilityId}
GET {MAINTENANCE_SERVICE_URL}/api/v1/work-orders?facilityId=&assetId=&from=&to=
```

Minimum sensor-reading response:

```json
{
  "id": "uuid",
  "sensorId": "uuid",
  "assetId": "uuid",
  "facilityId": "uuid",
  "timestamp": "2026-01-01T10:00:00",
  "value": 150.5,
  "unit": "kWh",
  "metricType": "ENERGY_GENERATION"
}
```

Minimum asset response:

```json
{
  "id": "uuid",
  "facilityId": "uuid",
  "name": "Inverter 01",
  "type": "INVERTER",
  "status": "ACTIVE",
  "ratedCapacity": 250.0
}
```

Minimum work-order response:

```json
{
  "id": "uuid",
  "facilityId": "uuid",
  "assetId": "uuid",
  "status": "COMPLETED",
  "createdAt": "2026-01-01T10:00:00",
  "completedAt": "2026-01-01T12:00:00",
  "downtimeHours": 2.0
}
```

If a dependency is unavailable or its JSON contract is incompatible, report-generation and live analytics endpoints return HTTP `503`. No partial report is persisted.

## Error response

```json
{
  "timestamp": "2026-08-01T10:30:00",
  "status": 404,
  "message": "Energy report not found: ...",
  "path": "/api/v1/energy-reports/...",
  "validationErrors": null
}
```

## Integration handoff to Charith

Charith should later:

1. Route `/api/v1/dashboard`, `/api/v1/dashboards/**`, `/api/v1/reports`, `/api/v1/energy-reports/**`, `/api/v1/performance-reports/**`, `/api/v1/kpis/**`, and `/api/v1/statistics` to `http://analytics-service:8086`.
2. Apply JWT authentication and RBAC at the gateway.
3. Allow administrators and operations managers to manage dashboard configurations and KPIs.
4. Allow operations managers and executives to view analytics and reports.
5. Add this service to Docker Compose and Kubernetes.
6. Supply production database credentials through secrets.
7. Add RabbitMQ consumers only after event contracts are agreed.

## Changes likely required later

These integration points must be reviewed when the other services are completed:

1. **Response envelopes:** `ExternalDataServiceImpl` expects raw JSON lists/objects. If another service returns `{ "success": true, "data": ... }` or a paginated object, update the integration DTO/wrapper parsing.
2. **Endpoint paths and filters:** confirm the final Monitoring and Maintenance paths and whether they accept `facilityId`, `assetId`, `from`, and `to`.
3. **Telemetry semantics:** readings must represent interval energy in Wh/kWh/MWh/GWh. If Monitoring supplies instantaneous power in kW/MW, implement time integration before summing it as energy.
4. **Timestamps:** all services currently use `LocalDateTime`. Agree on UTC and preferably migrate distributed contracts to offset-aware timestamps.
5. **Efficiency:** replace the capacity-factor fallback with weather/forecast-adjusted expected energy when that input becomes available.
6. **Security:** direct service calls are currently trusted because JWT/RBAC is assigned to the gateway owner. Add defense-in-depth validation if services will be publicly reachable.
7. **RabbitMQ:** event-based analytics snapshots can be added later without giving this service access to other databases.
8. **Schema management:** `ddl-auto=update` is suitable for the course development environment. Use Flyway/Liquibase migrations before production deployment.
9. **Pagination:** add pagination to saved report/KPI lists if data volume grows.
10. **PDF/XLSX:** CSV is currently supported. Add a team-approved library before implementing PDF or Excel export.
11. **Caching:** Redis can cache expensive dashboard calculations after performance measurements demonstrate a need.
12. **Resilience:** add retries/circuit breakers only after the team agrees on failure and retry policies.

## Troubleshooting

- `Could not create local repository at C:\.m2`: set Maven's local repository to a writable location or correct the Windows user environment.
- Maven reports Java 8: update `JAVA_HOME` to JDK 21 before invoking Maven.
- Database connection refused: start PostgreSQL and create `analytics_db`.
- `503 Service Unavailable`: start the required upstream service and verify its base URL/JSON contract.
- Empty energy results: verify Monitoring sends interval energy readings with supported units and timestamps inside the requested range.
