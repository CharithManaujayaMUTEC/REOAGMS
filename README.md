# REOAGMS — Renewable Energy Operations, Asset and Grid Management System

Event-driven microservices platform for monitoring and maintaining distributed renewable-energy assets (solar, wind, hydro, tidal, battery storage). Spring Boot services behind a Spring Cloud Gateway, RabbitMQ for events, PostgreSQL per service, Redis cache, STOMP/WebSocket live push, Prometheus + Grafana, and a React operations console.

## Quick start

Requirements: Docker Desktop (Compose v2), ~8 GB RAM.

```powershell
Copy-Item .env.example .env -ErrorAction SilentlyContinue
docker compose up --build -d
docker compose ps
```

The first start takes a few minutes (Maven builds). Then open **http://localhost:5173**.
The **simulator** container seeds a demo portfolio (5 facilities, 11 assets, 32 sensors, alert rules, technicians), back-fills 24 h of history and streams live telemetry, injecting a fault roughly once a minute so the alert → work order → notification chain is visible.

### Demo accounts

All accounts share the password **`Reoagms@2026`** (override with `DEMO_PASSWORD`; disable with `REOAGMS_SEED_DEMO_USERS=false`).

| Role | Email | Can do |
|---|---|---|
| System Administrator | `admin@reoagms.com` | Everything, incl. users and audit log |
| Operations Manager | `manager@reoagms.com` | Manage assets, rules, work orders, reports |
| Plant Operator | `operator@reoagms.com` | Read everything; acknowledge/resolve alerts |
| Maintenance Engineer | `engineer@reoagms.com` | Read everything; assign/start/complete work orders |
| Executive Management | `executive@reoagms.com` | Read-only dashboards, KPIs and reports |

Self-registration from the login page always creates a *Plant Operator*; only an administrator can grant other roles.

### URLs

| Component | URL |
|---|---|
| Operations console | http://localhost:5173 |
| API Gateway | http://localhost:8080 |
| Services | Identity 8081 · Asset 8082 · Monitoring 8083 · Maintenance 8084 · Analytics 8085 · Notification 8086 |
| Grafana (login `admin` / `reoagms`; port via `GRAFANA_PORT`) | http://localhost:3000 — dashboard *REOAGMS Platform Health* |
| Prometheus | http://localhost:9090 |
| RabbitMQ management (`reoagms` / `reoagms_password`) | http://localhost:15672 |

Presentation pack (slides, 5-minute demo script and team guide as PDFs) is in `deliverables/`. Seed only: `.\scripts\seed-database.ps1`.

## How the report maps to the code

| Report requirement | Implementation |
|---|---|
| FR-01/02/03 authentication, RBAC, user management | Identity Service (JWT with `role` claim); gateway `RolePolicy` enforces write permissions per role; Administration page |
| FR-04/05/06 facilities, assets, sensors | Asset Service + *Assets* page |
| FR-07/08 telemetry ingest and history | Monitoring Service `/sensor-readings`, `/telemetry`; simulator is the data source |
| FR-09/10 threshold evaluation and alerts | `AlertServiceImpl.evaluateReading` (one open alert per rule+asset) |
| FR-11 auto work orders for critical alerts | Maintenance Service consumes `monitoring.alert.opened.v1` from RabbitMQ and opens a corrective order for HIGH/CRITICAL alerts |
| FR-12 work-order lifecycle | Kanban board: assign → start → complete |
| FR-13/14 live dashboard, WebSocket + STOMP | Notification Service broadcasts alerts on `/topic/alerts`; console subscribes via `/ws` |
| FR-15 RabbitMQ events | Topic exchange `reoagms.events`; consumers in Notification and Maintenance |
| FR-16 audit logs | Identity Service `audit_logs` (logins, failed logins, user create/delete); Administration → Audit log |
| FR-17 reports | Analytics Service energy/performance reports with CSV export |
| FR-18 REST through the gateway | All routes under `/api/v1/**` on port 8080 |
| Redis caching (§5.6) | Monitoring dashboard summary cached 10 s; degrades to the database if Redis is down |
| Prometheus / Grafana (§5.9) | All services expose `/actuator/prometheus`; Grafana ships a provisioned dashboard |

## Architecture

```
React console ──► Spring Cloud Gateway ──► Identity | Asset | Monitoring | Maintenance | Analytics | Notification
      ▲   (nginx /api, /ws)  (JWT + RBAC)        │         │         │            │             │
      └──── STOMP /ws ◄──────────────────────────┼─────────┼──── RabbitMQ (reoagms.events) ────┘
                                                 └─ PostgreSQL per service      Redis (cache)
Prometheus ──► Grafana
```

Services never read each other's databases; cross-service references are UUIDs and calls go over HTTP or RabbitMQ.

## Development

```powershell
# Backend tests (Java 21), per service
cd backend/monitoring-service; .\mvnw.cmd test

# Frontend outside Docker (proxies /api to the gateway and /ws to the notification service)
cd frontend; npm ci; npm run dev
```

Useful commands:

```powershell
docker compose logs -f simulator maintenance-service notification-service
.\scripts\check-local.ps1            # health of every service
docker compose down -v               # stop and DELETE all local data
```

Simulator tuning (`.env`): `SIM_INTERVAL_MS`, `SIM_SOLAR_MODE=real` (follows the sun; solar is 0 at night).

## Production notes

Compose passwords, the JWT secret and the demo accounts are **development defaults** — replace them, disable seeding, terminate TLS in front of nginx, and use production secret storage. SMTP credentials (`MAIL_USERNAME`, `MAIL_PASSWORD`) are required for email delivery; without them notification records are created with status `FAILED`. Kubernetes starter manifests live in `k8s/base` (see its README).
