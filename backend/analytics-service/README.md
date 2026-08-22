# Analytics Service 

> **Service owner:** Sachinthana | **Port:** `8086` | **DB:** `analytics_db` (PostgreSQL)
> **Stack:** Java 21 · Spring Boot 4.1.0 · Spring MVC · Spring Data JPA · Lombok · Maven

---

## 1. Service Responsibility & Boundary

This is a **pure analytics microservice**. Its only job is to:

1. Pull raw operational data from three upstream services via REST.
2. Run calculations in-process.
3. Persist the resulting *snapshots* in its own `analytics_db`.
4. Serve those snapshots and on-demand live summaries through its own REST API.

**It never touches another service's database.** All cross-service references (facility IDs, asset IDs, user IDs) are stored as plain `UUID` columns with **no foreign key constraints** — intentionally. This is the correct pattern for microservice data ownership.

```
analytics_db owns:    dashboards · energy_reports · performance_reports · kpis
analytics_db does NOT own:  users · facilities · assets · sensors · alerts · work orders
```

---

## 2. Package Architecture

```
com.reoagms.analytics_service
├── AnalyticsServiceApplication.java   ← Spring Boot entry point
│
├── config/
│   ├── JpaAuditConfig.java            ← Enables @CreatedDate / @LastModifiedDate
│   └── RestClientConfig.java          ← Three named RestClient beans (monitoring/asset/maintenance)
│
├── common/
│   ├── enums/                         ← AggregationPeriod, DashboardType, ExportFormat,
│   │                                     KpiStatus, KpiType, ReportStatus, ScopeType
│   ├── exception/                     ← Domain exceptions + GlobalExceptionHandler
│   └── model/
│       ├── BaseEntity.java            ← UUID PK + createdAt + updatedAt (JPA Auditing)
│       ├── ApiResponse<T>.java        ← Standard success envelope
│       └── ErrorResponse.java         ← Standard error envelope
│
├── model/           ← JPA @Entity classes (4 aggregates)
├── repository/      ← JpaRepository interfaces (plain, no custom queries yet)
├── dto/             ← Request/Response POJOs (Lombok @Data @Builder)
├── mapper/          ← Manual mappers (no MapStruct) — entity ↔ DTO
├── service/         ← Business logic (interfaces + Impl classes)
│   ├── AnalyticsCalculationService    ← Pure math, no DB, no network
│   ├── AnalyticsValidationService     ← Pure guard clauses
│   ├── ExternalDataService(Impl)      ← All outbound REST calls
│   ├── AnalyticsQueryService(Impl)    ← Live dashboard + statistics
│   ├── DashboardService(Impl)         ← Dashboard config CRUD
│   ├── EnergyReportService(Impl)      ← Energy report lifecycle
│   ├── PerformanceReportService(Impl) ← Performance report lifecycle
│   └── KpiService(Impl)              ← KPI CRUD
├── controller/      ← 5 REST controllers
└── util/
    └── CsvExportUtil.java             ← RFC-4180 CSV writer (single-record, UTF-8)
```

### Design patterns used

| Pattern | Where |
|---|---|
| Service Interface + Impl | Every service class — enables mocking in tests |
| `@Transactional(readOnly = true)` at class, `@Transactional` on mutating methods | All service impls |
| Builder pattern | All DTOs and ER entities (Lombok `@Builder`) |
| Named `@Qualifier` beans | `RestClientConfig` — three separate `RestClient` instances |
| `@RestControllerAdvice` | `GlobalExceptionHandler` — centralised error mapping |

---

## 3. The Four Domain Aggregates

### 3.1 `Dashboard` — Presentation configuration
Stores layout/type preferences per user. **Does not store live data.** Fields: `name`, `description`, `dashboardType` (EXECUTIVE / OPERATIONS / MAINTENANCE / ASSET_PERFORMANCE), `ownerUserId`, `defaultRangeDays`, `active`, `layoutConfiguration` (LOB / JSON string).

> **Key design note:** `layoutConfiguration` is a free-form `@Lob` string. When the frontend matures, this should be constrained to a versioned JSON schema.

### 3.2 `EnergyReport` — Energy snapshot
A persisted snapshot of energy metrics for a time period, scoped to **either** a `facilityId` **or** an `assetId`.

| Column | Type | Notes |
|---|---|---|
| `total_energy` | `numeric(19,4)` | kWh |
| `average_energy` | `numeric(19,4)` | kWh per reading |
| `peak_energy` | `numeric(19,4)` | kWh single reading max |
| `expected_energy` | `numeric(19,4)` | From request or auto-calculated |
| `variance_percentage` | `numeric(9,4)` | Can be negative |
| `report_status` | `varchar(20)` | Always `GENERATED` currently |

**DB indexes:** `facility_id`, `asset_id`, `(period_start, period_end)`.

### 3.3 `PerformanceReport` — Operational health snapshot
Supports three scopes via `ScopeType`: `ORGANIZATION`, `FACILITY`, `ASSET`.

| Column | Type | Notes |
|---|---|---|
| `availability_percentage` | `numeric(9,4)` | Clamped 0–100 |
| `efficiency_percentage` | `numeric(9,4)` | Currently = capacity factor (see §4) |
| `capacity_factor_percentage` | `numeric(9,4)` | actual/expected × 100 |
| `utilization_percentage` | `numeric(9,4)` | operational assets / total |
| `downtime_hours` | `numeric(12,4)` | Capped at scheduled hours |
| `alert_count` | `bigint` | Total alerts in period |
| `maintenance_count` | `bigint` | Total work orders in period |

### 3.4 `Kpi` — Manually managed KPI record
Not auto-calculated — the caller supplies `actualValue` and optionally `targetValue`. The service sets `calculatedAt` on save/update.

**KPI types available:**
`TOTAL_ENERGY`, `AVERAGE_ENERGY`, `PEAK_ENERGY`, `AVAILABILITY`, `EFFICIENCY`, `CAPACITY_FACTOR`, `UTILIZATION`, `DOWNTIME_HOURS`, `ALERT_COUNT`, `OPEN_WORK_ORDER_COUNT`, `MAINTENANCE_COMPLETION_RATE`

**KPI statuses:** `ON_TARGET`, `AT_RISK`, `BELOW_TARGET` — these are currently set by the caller, not auto-computed.

---

## 4. Analytics Calculation Engine

All math lives in [`AnalyticsCalculationService`](file:///c:/Users/some1/Downloads/REOAGMS/REOAGMS/backend/analytics-service/src/main/java/com/reoagms/analytics_service/service/AnalyticsCalculationService.java) — a pure `@Service` with **no DB access, no network calls**. This is a clean separation of concerns.

### Energy formulas (all values normalized to kWh first)

```
Unit normalization:
  Wh  → kWh   ÷ 1,000
  kWh → kWh   (passthrough)
  MWh → kWh   × 1,000
  GWh → kWh   × 1,000,000

Total energy         = Σ(normalized readings)
Average energy       = total / readingCount                     (0 if no readings)
Peak energy          = max(normalized readings)
Expected energy      = Σ(asset.ratedCapacity kW) × periodHours (if not supplied in request)
Variance %           = (actual - expected) / expected × 100     (0 if expected = 0)
```

### Performance formulas

```
Scheduled hours      = Duration(from→to).hours × assetCount     (min 1 asset)
Downtime hours       = Σ(workOrder.downtimeHours) capped at scheduledHours
Availability %       = (scheduledHours - downtime) / scheduledHours × 100  → clamped 0–100
Capacity factor %    = totalEnergy / expectedEnergy × 100       → clamped 0–100
Efficiency %         = capacityFactor (same value — known limitation, see §7)
Utilization %        = operationalAssets / totalAssets × 100    → clamped 0–100
Completion rate %    = completedWorkOrders / totalWorkOrders × 100

Operational statuses accepted: ACTIVE | OPERATIONAL | RUNNING
Completed statuses accepted:   COMPLETED | CLOSED | DONE
```

### Time-series grouping

```
HOUR  → truncate to :00:00
DAY   → start of day (midnight)
WEEK  → Monday of the week
MONTH → 1st of the month
```

Auto-selection in dashboard: periods ≤ 2 days → `HOUR`, longer → `DAY`.

### Precision

All arithmetic uses `BigDecimal` with `RoundingMode.HALF_UP` at scale `4` (stored) and `6` (intermediate). All percentages are clamped via `clampPercentage()` (0–100 guard).

---

## 5. External Integration Layer

### REST client setup ([`RestClientConfig`](file:///c:/Users/some1/Downloads/REOAGMS/REOAGMS/backend/analytics-service/src/main/java/com/reoagms/analytics_service/config/RestClientConfig.java))

Three **named** `RestClient` beans share one `JdkClientHttpRequestFactory` (Java 21 `HttpClient`):

| Bean qualifier | Base URL property | Default |
|---|---|---|
| `monitoringRestClient` | `MONITORING_SERVICE_URL` | `http://localhost:8083` |
| `assetRestClient` | `ASSET_SERVICE_URL` | `http://localhost:8082` |
| `maintenanceRestClient` | `MAINTENANCE_SERVICE_URL` | `http://localhost:8084` |

Connect timeout: `3s` · Read timeout: `10s`

### Outbound calls ([`ExternalDataServiceImpl`](file:///c:/Users/some1/Downloads/REOAGMS/REOAGMS/backend/analytics-service/src/main/java/com/reoagms/analytics_service/service/ExternalDataServiceImpl.java))

| Method | Upstream | Path |
|---|---|---|
| `getReadings(facilityId, assetId, from, to)` | Monitoring | `GET /api/v1/sensor-readings` |
| `getAlerts(facilityId, assetId, from, to)` | Monitoring | `GET /api/v1/alerts` |
| `getAssets(facilityId, assetId)` | Asset | `GET /api/v1/assets`, `/assets/{id}`, `/assets/facility/{id}` |
| `getWorkOrders(facilityId, assetId, from, to)` | Maintenance | `GET /api/v1/work-orders` |

**Failure handling:** Any `RestClientException` is caught and re-thrown as `DownstreamServiceException`, which the `GlobalExceptionHandler` maps to **HTTP 503**. No partial results are persisted.

**Critical contract assumption:** The service expects raw JSON arrays/objects — **not** wrapped in `{ "success": true, "data": [...] }`. If other services adopt an envelope pattern, `ExternalDataServiceImpl` must be updated.

---

## 6. API Surface (5 Controllers)

### `AnalyticsController` — `/api/v1`
| Endpoint | Description |
|---|---|
| `GET /dashboard?facilityId&assetId&from&to` | Live dashboard summary (calls all 4 upstreams) |
| `GET /reports` | Combined list of all energy + performance reports |
| `GET /statistics?facilityId&assetId&from&to&groupBy` | Time-series energy stats with groupBy |

### `DashboardController` — `/api/v1/dashboards`
Standard CRUD. `POST`, `GET`, `GET/{id}`, `PUT/{id}`, `DELETE/{id}`.

### `EnergyReportController` — `/api/v1/energy-reports`
Standard CRUD + `GET/{id}/export?format=CSV`. On `POST`/`PUT`, the service calls upstreams and recalculates.

### `PerformanceReportController` — `/api/v1/performance-reports`
Same pattern as energy reports.

### `KpiController` — `/api/v1/kpis`
Standard CRUD. No upstream calls — KPI values are supplied by the caller.

### Response envelope
All success responses use `ApiResponse<T>` with `{ "success": true, "message": "...", "data": T }`.
All error responses use `ErrorResponse` with `{ "timestamp", "status", "message", "path", "validationErrors" }`.

---

## 7. Validation Layer

[`AnalyticsValidationService`](file:///c:/Users/some1/Downloads/REOAGMS/REOAGMS/backend/analytics-service/src/main/java/com/reoagms/analytics_service/service/AnalyticsValidationService.java) enforces:

- **Period rules:** both `from`/`to` required; `from` must be before `to`; max span = **366 days**.
- **Energy scope:** at least one of `facilityId` or `assetId` must be provided.
- **Scope rules:** `FACILITY`/`ASSET` scopes require `scopeId`; `ORGANIZATION` must omit `scopeId`.

Bean Validation (`@Valid`) handles field-level rules in DTOs (null checks, size limits).

---

## 8. Error Handling Matrix

| Exception class | HTTP Status | Trigger |
|---|---|---|
| `ResourceNotFoundException` | `404 Not Found` | Entity not found by ID |
| `InvalidAnalyticsRequestException` | `400 Bad Request` | Business rule violations |
| `UnsupportedExportFormatException` | `400 Bad Request` | Export format not supported |
| `DownstreamServiceException` | `503 Service Unavailable` | Any upstream REST failure |
| `MethodArgumentNotValidException` | `400 Bad Request` | `@Valid` DTO constraint failure |
| `ConstraintViolationException` | `400 Bad Request` | JPA/Jakarta constraint failure |
| `HttpMessageNotReadableException` | `400 Bad Request` | Malformed JSON body |
| `Exception` (catch-all) | `500 Internal Server Error` | Unexpected errors |

---

## 9. Data Flow — Report Generation

```
Client POST /api/v1/energy-reports
    │
    ▼
EnergyReportController.create(@Valid request)
    │
    ▼
EnergyReportServiceImpl.generate(report, request)
    ├─ AnalyticsValidationService.validatePeriod(...)
    ├─ AnalyticsValidationService.validateEnergyScope(...)
    ├─ ExternalDataService.getAssets(facilityId, assetId)       ──→ Asset Service
    ├─ ExternalDataService.getReadings(facilityId, assetId, ...) ──→ Monitoring Service
    └─ AnalyticsCalculationService.calculateEnergy(...)
         │
         ▼
    EnergyReport entity populated
         │
         ▼
    EnergyReportRepository.save(report)
         │
         ▼
    EnergyReportMapper.toResponse(saved entity)
         │
         ▼
Client ← 201 Created { ApiResponse<EnergyReportResponse> }

On any upstream failure → DownstreamServiceException → 503, nothing persisted
```

---

## 10. Database Schema Notes

All four tables inherit from `BaseEntity`:

```
id          UUID  PRIMARY KEY  (Hibernate @UuidGenerator — random UUID v4)
created_at  TIMESTAMP  NOT NULL  (JPA @CreatedDate, not updatable)
updated_at  TIMESTAMP           (JPA @LastModifiedDate)
```

**Schema management:** `ddl-auto=update` — Hibernate auto-creates/alters tables. **Not production safe.**

**Indexes defined:**
- `energy_reports`: `facility_id`, `asset_id`, `(period_start, period_end)`
- `performance_reports`: `(scope_type, scope_id)`, `(period_start, period_end)`
- `kpis`: `(scope_type, scope_id)`, `kpi_type`

**No composite unique constraints exist.** Duplicate reports for the same period and scope are allowed by the schema.

---

## 11. Test Coverage (Current State)

| Test class | Layer | What's tested |
|---|---|---|
| `AnalyticsCalculationServiceTest` | Unit | All calculation formulas, unit normalization, edge cases |
| `AnalyticsValidationServiceTest` | Unit | All guard clauses, period limits, scope rules |
| `EnergyReportServiceImplTest` | Unit | Report generation with mocked upstreams |
| `DashboardControllerTest` | MVC slice | Controller binding and response structure |
| `RestClientConfigTest` | Integration | Bean wiring and timeout configuration |

**Coverage gaps:** `PerformanceReportServiceImpl`, `KpiServiceImpl`, `AnalyticsQueryServiceImpl`, export logic, and `ExternalDataServiceImpl` have no dedicated tests.

---

## 12. Known Technical Debt & Future Implementation Guide

> These are the exact points you need to understand before adding features.

### 🔴 HIGH PRIORITY — Will break in production

| # | Issue | Where | Fix |
|---|---|---|---|
| 1 | `ddl-auto=update` | `application.properties` | Replace with **Flyway** or Liquibase before any production deployment |
| 2 | No response-envelope handling | `ExternalDataServiceImpl` | Add wrapper parsing if other services use `{ "success": true, "data": [...] }` |
| 3 | `LocalDateTime` (no timezone) | All timestamps | Agree on UTC across all services; migrate to `OffsetDateTime` |
| 4 | No security on direct calls | All controllers | Add JWT validation at service level as defence-in-depth when services are reachable without gateway |

### 🟡 MEDIUM PRIORITY — Feature gaps

| # | Issue | Location | Fix |
|---|---|---|---|
| 5 | Efficiency = Capacity Factor | `AnalyticsCalculationService` L116 | Replace with weather/forecast-adjusted calculation when irradiance data is available |
| 6 | KPI status manually supplied | `KpiServiceImpl` | Auto-compute `ON_TARGET / AT_RISK / BELOW_TARGET` by comparing `actualValue` vs `targetValue` |
| 7 | CSV export only produces 1 row | `CsvExportUtil.singleRecord()` | When implementing multi-row export (e.g., time-series), add a `multiRecord()` method |
| 8 | No pagination on list endpoints | All `getAll()` service methods | Add `Page<T>` + `Pageable` parameter to all list endpoints |
| 9 | `layoutConfiguration` is a raw string | `Dashboard` entity | Define and validate a versioned JSON schema |

### 🟢 LOW PRIORITY — Future enhancements

| # | Enhancement | Notes |
|---|---|---|
| 10 | RabbitMQ consumers | Add event-driven snapshot generation after event contracts are agreed |
| 11 | Redis caching | Cache `getDashboard()` results; expensive — calls 4 upstreams synchronously |
| 12 | Circuit breakers / retries | Use Resilience4j after team agrees on failure policies |
| 13 | PDF/XLSX export | Add Apache POI or similar — `ExportFormat` enum already exists |
| 14 | Custom repository queries | Add `findByFacilityIdAndPeriod()`, `findByScopeAndPeriod()` etc. when search/filter UI is needed |
| 15 | Duplicate report prevention | Add unique constraint on `(facility_id, asset_id, period_start, period_end)` |

---

## 13. Quick Reference — How to Add a New Feature

### Adding a new report field
1. Add column to the JPA entity (`model/`)
2. Add field to the response DTO (`dto/`)
3. Update the mapper (`mapper/`)
4. Update the calculation in `AnalyticsCalculationService` if the value is calculated
5. Update `CsvExportUtil` call in the service impl's `exportCsv()` method
6. Write a Flyway migration script for the new column

### Adding a new upstream data source
1. Add a new `RestClient` bean in `RestClientConfig.java`
2. Add method to `ExternalDataService` interface
3. Implement in `ExternalDataServiceImpl` following the existing try/catch pattern
4. Add the new DTO in `dto/` matching the upstream's JSON contract
5. Call it from the relevant service impl

### Adding a new endpoint
1. Add method to service interface + impl
2. Add `@GetMapping` / `@PostMapping` in the relevant controller
3. Wrap response in `ApiResponse.success(...)` for GET, `ResponseEntity.status(201).body(...)` for POST
4. Add Bean Validation annotations to request DTOs
5. Add validation logic to `AnalyticsValidationService` if there are business rules

### Gateway integration checklist (for Charith)
```
Routes to register for analytics-service (http://analytics-service:8086):
  /api/v1/dashboard
  /api/v1/dashboards/**
  /api/v1/reports
  /api/v1/energy-reports/**
  /api/v1/performance-reports/**
  /api/v1/kpis/**
  /api/v1/statistics

RBAC roles needed:
  ADMIN / OPERATIONS_MANAGER → manage dashboards, KPIs, generate reports
  OPERATIONS_MANAGER / EXECUTIVE → view analytics and reports
```

---

## 14. Architecture Summary Diagram

```
                    ┌──────────────────────────────────┐
                    │       analytics-service           │
                    │           :8086                   │
                    │                                   │
  Client/Gateway ──►│  REST Controllers (5)             │
                    │   ├─ AnalyticsController          │
                    │   ├─ DashboardController          │
                    │   ├─ EnergyReportController       │
                    │   ├─ PerformanceReportController  │
                    │   └─ KpiController                │
                    │            │                      │
                    │  Service Layer                    │
                    │   ├─ AnalyticsQueryService        │
                    │   ├─ EnergyReportService   ──────►│──► Asset Service :8082
                    │   ├─ PerformanceReportService ───►│──► Monitoring Service :8083
                    │   ├─ DashboardService             │──► Maintenance Service :8084
                    │   ├─ KpiService                   │
                    │   ├─ [ExternalDataService]        │
                    │   ├─ [CalculationService]         │
                    │   └─ [ValidationService]          │
                    │            │                      │
                    │  Repository Layer                 │
                    │   ├─ DashboardRepository          │
                    │   ├─ EnergyReportRepository       │
                    │   ├─ PerformanceReportRepository  │
                    │   └─ KpiRepository                │
                    │            │                      │
                    └────────────┼──────────────────────┘
                                 │
                                 ▼
                          analytics_db (PostgreSQL)
                    ┌────────────────────────────┐
                    │ dashboards                 │
                    │ energy_reports             │
                    │ performance_reports        │
                    │ kpis                       │
                    └────────────────────────────┘
```


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
