-- REOAGMS Monitoring Service PostgreSQL reference schema
-- Run the first two statements as a PostgreSQL administrator.
-- Change the demonstration password before shared/production use.

CREATE USER monitoring_user WITH PASSWORD 'monitoring123';
CREATE DATABASE monitoring_db OWNER monitoring_user;

-- Reconnect to monitoring_db as monitoring_user before running the remaining DDL.
-- In practice, Hibernate (spring.jpa.hibernate.ddl-auto=update) creates and evolves
-- these tables automatically on application startup; this file is the reference copy.

CREATE TABLE IF NOT EXISTS sensor_readings (
    id UUID PRIMARY KEY,
    sensor_id UUID NOT NULL,
    asset_id UUID NOT NULL,
    facility_id UUID,
    recorded_at TIMESTAMP NOT NULL,
    reading_value NUMERIC(19,4) NOT NULL,
    unit VARCHAR(50),
    metric_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sensor_reading_sensor_id ON sensor_readings(sensor_id);
CREATE INDEX IF NOT EXISTS idx_sensor_reading_asset_id ON sensor_readings(asset_id);
CREATE INDEX IF NOT EXISTS idx_sensor_reading_facility_id ON sensor_readings(facility_id);
CREATE INDEX IF NOT EXISTS idx_sensor_reading_recorded_at ON sensor_readings(recorded_at);

CREATE TABLE IF NOT EXISTS alert_rules (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    asset_id UUID,
    facility_id UUID,
    metric_type VARCHAR(30) NOT NULL,
    operator VARCHAR(30) NOT NULL,
    threshold_value NUMERIC(19,4) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY,
    facility_id UUID,
    asset_id UUID NOT NULL,
    sensor_reading_id UUID,
    alert_rule_id UUID,
    metric_type VARCHAR(30),
    reading_value NUMERIC(19,4),
    threshold_value NUMERIC(19,4),
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    message VARCHAR(500) NOT NULL,
    raised_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_alert_facility_id ON alerts(facility_id);
CREATE INDEX IF NOT EXISTS idx_alert_asset_id ON alerts(asset_id);
CREATE INDEX IF NOT EXISTS idx_alert_status ON alerts(status);

CREATE TABLE IF NOT EXISTS telemetry (
    id UUID PRIMARY KEY,
    asset_id UUID NOT NULL,
    facility_id UUID,
    status VARCHAR(20) NOT NULL,
    last_seen_at TIMESTAMP NOT NULL,
    battery_level DOUBLE PRECISION,
    signal_strength DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_telemetry_asset_id ON telemetry(asset_id);
