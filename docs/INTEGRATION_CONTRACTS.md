# REOAGMS integration contracts

## Network and ownership

Each service owns its own PostgreSQL database. Cross-service relationships use UUIDs; services integrate through HTTP APIs or RabbitMQ events and never join/query another service's database. Use Docker DNS names for service URLs inside Compose.

## Alert event v1

- Exchange: `reoagms.events` (durable topic exchange)
- Routing key: `monitoring.alert.opened.v1`
- Consumer queue: `notification.alert-opened.v1` (durable)
- Dead-letter queue: `notification.alert-opened.v1.dlq`
- Publisher: Monitoring Service when an alert is created directly or generated from a breached sensor reading
- Consumer: Notification Service, which creates an EMAIL notification record
- Payload fields: `schemaVersion`, `eventId`, `alertId`, `facilityId`, `assetId`, `metricType`, `value`, `threshold`, `severity`, `message`, `occurredAt`

The consumer validates the schema version and required alert ID. Failed messages are retried by listener policy and then dead-lettered. Before production, add idempotency keyed by `eventId` (or a unique inbox table) to prevent duplicate notifications after redelivery. Email delivery requires valid SMTP settings; the development recipient defaults to `operations@example.com` and must be overridden.

## API gateway

The gateway routes Identity, Asset, Monitoring, Maintenance, Analytics and Notification paths. Every endpoint except `/api/v1/auth/login` and `/api/v1/auth/register` requires a valid Bearer JWT. The token carries a `role` claim and the gateway's `RolePolicy` restricts write operations per role (read access is open to all authenticated roles; ingestion of readings and telemetry is allowed for any authenticated principal). Identity additionally enforces ADMIN-only access to `/users` and `/audit-logs`. The frontend calls the gateway only; the STOMP endpoint `/ws` is proxied by nginx to the Notification Service.

## Maintenance consumer

Queue `maintenance.alert-opened.v1` is bound to the same routing key. For `HIGH` and `CRITICAL` alerts the Maintenance Service opens a CORRECTIVE work order, unless the asset already has an active corrective order or the alert id (stored in the description) was already handled.

## Real-time channel

The Notification Service publishes consumed alert events on STOMP topic `/topic/alerts` and in-app notifications on `/topic/notifications` (endpoint `/ws`).
