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

The gateway routes Identity, Asset, Monitoring, Maintenance, Analytics and Notification paths. API endpoints except `/api/v1/auth/login` and `/api/v1/auth/register` require a valid Bearer JWT at the Gateway. The frontend calls the Gateway only. This starter does not yet implement fine-grained role-based authorization across all domain APIs; add and test role policies before production.

## Frontend data caveat

The overview chart is sample/illustrative, not a claim of live energy-generation time series. Replace it with a documented analytics statistics/time-series contract before presenting it as live telemetry.
