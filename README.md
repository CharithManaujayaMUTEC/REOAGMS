# REOAGMS — Renewable Energy Operations, Asset and Grid Management System

REOAGMS is a microservices-based system for renewable-energy asset management, sensor monitoring, alerting, maintenance workflows, notifications and analytics. The repository contains Spring Boot services, PostgreSQL databases per service, an API Gateway, RabbitMQ, Prometheus/Grafana and a React/Vite operations console.

## Architecture and ports

| Component | Local URL / host port |
|---|---|
| React operations console | http://localhost:5173 |
| API Gateway | http://localhost:8080 |
| Identity Service | http://localhost:8081 |
| Asset Service | http://localhost:8082 |
| Monitoring Service | http://localhost:8083 |
| Maintenance Service | http://localhost:8084 |
| Analytics Service | http://localhost:8085 |
| Notification Service | http://localhost:8086 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |
| RabbitMQ management | http://localhost:15672 (development credentials: reoagms / reoagms_password) |
| PostgreSQL host ports | 5433–5438; each service has a separate database |

Internal service-to-service calls must use Docker DNS names and HTTP APIs. A service must not query another service's database directly. UUIDs are used for cross-service references.

## Requirements

- Docker Desktop with Docker Compose v2 enabled
- At least 8 GB RAM recommended for the full stack
- Optional: Java 21 and Node.js 22 if running a service outside Docker

## Run the complete development stack (PowerShell)

From the repository root:

```powershell
Copy-Item .env.example .env -ErrorAction SilentlyContinue
docker compose config
docker compose up --build -d
docker compose ps
```

Open http://localhost:5173. The Vite development server proxies `/api` to the API Gateway container. Login uses the Identity Service API. If you choose the demo-dashboard link, only the UI is bypassed; protected backend endpoints remain protected.

Check service health:

```powershell
$ports = 8080,8081,8082,8083,8084,8085,8086
foreach ($port in $ports) {
  try { $r = Invoke-RestMethod "http://localhost:$port/actuator/health"; Write-Host "$port : $($r.status)" }
  catch { Write-Host "$port : FAILED - $($_.Exception.Message)" }
}
```

View logs:

```powershell
docker compose logs -f api-gateway identity-service asset-service monitoring-service maintenance-service analytics-service notification-service frontend
```

Stop the stack:

```powershell
docker compose down
```

To reset all local database data (DESTRUCTIVE):

```powershell
docker compose down -v
docker compose up --build -d
```

## Frontend development outside Docker

```powershell
cd frontend
Copy-Item .env.example .env
npm install
npm run dev
```

Set `VITE_GATEWAY_PROXY=http://localhost:8080` in `frontend/.env` for this mode.

## Backend tests

Run from each service directory (Java 21 required):

```powershell
cd backend/monitoring-service
.\mvnw.cmd test
```

Repeat for `identity-service`, `asset-service`, `maintenance-service`, `analytics-service`, `notification-service`, and `api-gateway`. A successful image build is not a substitute for running service and end-to-end tests.

## RabbitMQ alert flow

Monitoring publishes schema-versioned `monitoring.alert.opened.v1` events to the durable `reoagms.events` topic exchange when alerts are created or generated from breached sensor readings. Notification consumes from `notification.alert-opened.v1` and creates a notification record. Configure `MAIL_USERNAME`, `MAIL_PASSWORD`, and `ALERT_NOTIFICATION_RECIPIENT` in the root `.env` to enable actual email delivery. Without valid SMTP credentials, event consumption may still create a failed notification record; that is expected in an unconfigured development environment.

## Environment variables and secrets

See `.env.example`. The Compose passwords and fallback JWT key are development-only. Do not deploy these defaults to a shared or production environment. Use unique strong credentials/secrets, restrictive database exposure, TLS, and production-grade secret storage. `.env` is ignored by Git.

## Important integration notes

- Service-specific DTOs define the accepted request payload. The frontend's generic create form is a developer utility, not a substitute for schema-driven forms.
- Some dashboard chart data is explicitly illustrative until the Monitoring/Analytics APIs expose the agreed time-series contract. KPI cards use summary values where available and fall back to counts from fetched lists.
- API paths are routed through the Gateway, but each service's exact CRUD operations and authorization rules must be verified against its controller and tests.
- Production readiness still requires review of JWT validation, authorization policies, SMTP/SMS credentials, migrations, retry/dead-letter behaviour, backups, rate limiting, and Kubernetes manifests.

## Project structure

```text
backend/
  api-gateway/
  identity-service/
  asset-service/
  monitoring-service/
  maintenance-service/
  analytics-service/
  notification-service/
frontend/
infrastructure/prometheus/
infrastructure/grafana/
k8s/base/
docs/api/
docs/database/
```

## Kubernetes starter

A single-replica Kubernetes starter is provided under `k8s/base` for the services, separate PostgreSQL databases, RabbitMQ and frontend. Read `k8s/base/README.md` first: build/publish the referenced images and replace development secrets before applying. It is not a production cluster configuration.
