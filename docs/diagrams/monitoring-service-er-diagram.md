# Monitoring Service ER Diagram

The service owns `monitoring_db` exclusively. `sensorId`, `assetId`, and `facilityId` are external references to the Asset Service — stored as plain UUID columns with no foreign key constraints, per REOAGMS's microservice data-ownership rule.

```mermaid
erDiagram
    SENSOR_READINGS {
        uuid id PK
        uuid sensor_id "external Asset Service reference"
        uuid asset_id "external Asset Service reference"
        uuid facility_id "external Asset Service reference"
        timestamp recorded_at
        numeric reading_value
        varchar unit
        varchar metric_type
        timestamp created_at
        timestamp updated_at
    }

    ALERT_RULES {
        uuid id PK
        varchar name
        uuid asset_id "external reference, null = applies to all assets"
        uuid facility_id "external reference"
        varchar metric_type
        varchar operator
        numeric threshold_value
        varchar severity
        boolean enabled
        varchar description
        timestamp created_at
        timestamp updated_at
    }

    ALERTS {
        uuid id PK
        uuid facility_id "external reference"
        uuid asset_id "external reference"
        uuid sensor_reading_id FK "nullable, set when rule-generated"
        uuid alert_rule_id FK "nullable, set when rule-generated"
        varchar metric_type
        numeric reading_value
        numeric threshold_value
        varchar severity
        varchar status
        varchar message
        timestamp raised_at
        timestamp created_at
        timestamp updated_at
    }

    TELEMETRY {
        uuid id PK
        uuid asset_id "external Asset Service reference"
        uuid facility_id "external Asset Service reference"
        varchar status
        timestamp last_seen_at
        double battery_level
        double signal_strength
        timestamp created_at
        timestamp updated_at
    }

    ALERT_RULES ||--o{ ALERTS : "breach generates"
    SENSOR_READINGS ||--o{ ALERTS : "breach generates"
```

## Notes

- `SensorReading` is the raw metric stream (temperature, voltage, power, etc.) written by field devices or a simulator.
- `Telemetry` is a separate, lower-frequency connectivity/heartbeat signal used to tell whether an asset's monitoring hardware is online — it is not a metric value and is not evaluated against `AlertRule`.
- `AlertRule` defines a threshold condition per metric type (optionally scoped to one asset); `Alert` is either raised automatically whenever a `SensorReading` breaches an enabled rule, or manually via `POST /api/v1/alerts`.
- `alert_rule_id` / `sensor_reading_id` on `Alert` are nullable because manually-raised alerts have no originating rule or reading.
