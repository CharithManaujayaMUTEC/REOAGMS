# Maintenance Service — REOAGMS (EC8208 Software Architecture)

Owned by Thurunu. Preventive/corrective maintenance, technician & task
assignment, completion tracking, maintenance history, and maintenance
reports. Runs as an independent Spring Boot microservice with its own
PostgreSQL database (`maintenance_db`). It never accesses another
service's database directly — cross-service links (asset, technician
identity, etc.) are UUID references only.

## Stack

- Java 21
- Spring Boot 4.1.1 (Spring Web, Spring Data JPA, Validation, Actuator)
- Lombok
- PostgreSQL
- Maven

## 1. Database setup

Run once, in `psql` (as a superuser):

```bash
psql -U postgres -f db/setup.sql
```

This creates the `maintenance_user` role and the `maintenance_db`
database. Tables are created/updated automatically by Hibernate
(`spring.jpa.hibernate.ddl-auto=update`) — no manual DDL needed.

If the database/user already exists, don't recreate them; use
`ALTER USER maintenance_user WITH PASSWORD '...'` to reset the password
instead.

## 2. Configure

`src/main/resources/application.properties` is already set up for a
local PostgreSQL instance on the default port (5432) and service port
`8082`. Update it if your team's agreed port allocation differs.

## 3. Build & run

```bash
./mvnw clean install
./mvnw spring-boot:run
```

(No `mvnw` wrapper scripts are included in this scaffold — generate
them with `mvn -N io.takari:maven:wrapper` or open the project in
IntelliJ/VS Code, which will offer to add the wrapper. Until then, use
your local `mvn` instead of `./mvnw`.)

Health check: `GET http://localhost:8082/actuator/health`

## 4. Package structure

```
src/main/java/com/reoagms/maintenance_service/
├── config
├── common/
│   ├── enums              (WorkOrderStatus, MaintenanceType, TaskStatus, TechnicianAvailability)
│   ├── exception          (GlobalExceptionHandler)
│   └── model              (BaseEntity)
├── controller
├── dto
├── mapper
├── model                  (WorkOrder, MaintenanceTask, Technician, MaintenanceHistory)
├── repository
├── service
└── util
```

Layering is strict: `Controller -> Service -> Repository`. Controllers
never return JPA entities — only DTOs.

## 5. API overview

Base path: `/api/v1`

### Work Orders — `/work-orders`
| Method | Path | Purpose |
|---|---|---|
| POST | `/work-orders` | Create (status starts at `CREATED`) |
| GET | `/work-orders` | List all |
| GET | `/work-orders/{id}` | Get one |
| PUT | `/work-orders/{id}` | Update details |
| DELETE | `/work-orders/{id}` | Delete |
| PUT | `/work-orders/{id}/assign/{technicianId}` | Assign technician → `ASSIGNED` |
| PUT | `/work-orders/{id}/start` | `ASSIGNED` → `IN_PROGRESS` |
| PUT | `/work-orders/{id}/complete` | `IN_PROGRESS` → `COMPLETED` |

Lifecycle: `CREATED → ASSIGNED → IN_PROGRESS → COMPLETED`, with
`CANCELLED` reachable from `ASSIGNED`/`IN_PROGRESS` (add a
`/cancel` endpoint when the team agrees on that transition). The
service implementation enforces valid transitions and returns
`409 Conflict` on an invalid one (e.g. starting a work order that
was never assigned).

### Maintenance Tasks — `/maintenance-tasks`
| Method | Path | Purpose |
|---|---|---|
| POST | `/maintenance-tasks` | Create (status starts at `PENDING`) |
| GET | `/maintenance-tasks` | List all, or `?workOrderId=` to filter |
| GET | `/maintenance-tasks/{id}` | Get one |
| PUT | `/maintenance-tasks/{id}` | Update details |
| DELETE | `/maintenance-tasks/{id}` | Delete |
| PUT | `/maintenance-tasks/{id}/start` | → `IN_PROGRESS` |
| PUT | `/maintenance-tasks/{id}/complete` | → `COMPLETED` |

### Technicians — `/technicians`
Only keep this module if the team's agreed design has Maintenance
Service owning technician records. If technicians actually live in
Identity/User Service, delete `Technician`, `TechnicianController`,
`TechnicianService(Impl)`, `TechnicianRepository`, `TechnicianMapper`,
and the DTOs — keep `technicianId` as a plain UUID reference
elsewhere and call the Identity/User Service API when details are
needed.

| Method | Path | Purpose |
|---|---|---|
| POST | `/technicians` | Create |
| GET | `/technicians` | List all |
| GET | `/technicians/{id}` | Get one |
| PUT | `/technicians/{id}` | Update |
| DELETE | `/technicians/{id}` | Delete |

### Maintenance History — `/maintenance-history`
| Method | Path | Purpose |
|---|---|---|
| POST | `/maintenance-history` | Record a completed activity |
| GET | `/maintenance-history` | List all |
| GET | `/maintenance-history/asset/{assetId}` | Filter by asset |
| GET | `/maintenance-history/work-order/{workOrderId}` | Filter by work order |

### Maintenance Reports — `/maintenance-reports`
| Method | Path | Purpose |
|---|---|---|
| GET | `/maintenance-reports/summary` | Aggregated counts: total/completed/overdue work orders, preventive vs corrective counts, work orders by asset, work orders by technician, and the overdue list |

Returns aggregated DTOs rather than raw entity collections, per the
guide's reporting guidance.

## 6. Testing checklist (from the team guide)

- [x] Builds with `mvn clean install`
- [ ] Starts with `mvn spring-boot:run` (verify against your local Postgres)
- [ ] PostgreSQL connection works
- [ ] WorkOrder table is created
- [ ] WorkOrder CRUD works end-to-end (Postman)
- [ ] Technician assignment / start / complete transitions work
- [ ] Invalid input rejected (400 with field errors)
- [ ] Not-found records return 404 JSON
- [ ] MaintenanceTask CRUD works
- [ ] MaintenanceHistory retrieval works
- [ ] Report API works
- [x] Controllers return DTOs, not entities
- [x] Basic unit/service test committed (`WorkOrderServiceImplTest`)
- [ ] Postman collection imported and run (see `postman/maintenance-service.postman_collection.json`)
- [x] README complete
- [x] No passwords/secrets committed (sample credentials only — change before any shared/deployed environment)

## 7. Git workflow

```bash
git checkout develop
git pull origin develop
git checkout -b feature/maintenance-service

git add .
git commit -m "feat: create maintenance service"
git push -u origin feature/maintenance-service
```

Suggested follow-up commits: `feat: add work order module`,
`feat: add maintenance task module`, `feat: add technician module`,
`feat: add maintenance history`, `feat: add maintenance reports`,
`feat: add validation and exception handling`,
`test: add maintenance service tests`,
`docs: add maintenance service README`.

## 8. Common errors

- **Package does not exist**: check the `package` declaration matches
  the folder path exactly.
- **No qualifying bean**: check `@Service`/`@Component`/`@Repository`
  and that the class sits below `com.reoagms.maintenance_service`.
- **Database connection failed**: confirm PostgreSQL is running and
  check host/port/db name/credentials.
- **Port already in use**: `netstat -ano | findstr :8082` (PowerShell).
- **Entity relation errors**: never create a JPA `@ManyToOne`/`@OneToMany`
  to an entity owned by another microservice — use a UUID field
  instead (this is already how `assetId`/`technicianId` are modeled).

## 9. Final integration hand-off (for Charith)

When ready for integration, provide: the final API list above,
sample request/response payloads, an ER diagram for
`work_orders` / `maintenance_tasks` / `technicians` /
`maintenance_history`, the Postman collection, and a proposed event
payload for RabbitMQ (e.g. `maintenance.work_order.completed`) —
without independently changing any RabbitMQ contract yourself.
