-- REOAGMS Notification Service PostgreSQL reference schema
-- Run the first two statements as a PostgreSQL administrator.
-- Change the demonstration password before shared/production use.

CREATE USER notification_user WITH PASSWORD 'notification_password';
CREATE DATABASE notification_db OWNER notification_user;

-- Reconnect to notification_db as notification_user before running the remaining DDL.
-- Hibernate (ddl-auto=update) creates/updates this schema automatically in local dev;
-- this file is the reference contract for review and future Flyway/Liquibase migration.

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('EMAIL', 'SMS', 'PUSH')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'RETRYING')),
    message VARCHAR(2000) NOT NULL,
    subject VARCHAR(255),
    related_entity_id VARCHAR(255),
    retry_count INTEGER NOT NULL DEFAULT 0,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_channel ON notifications(channel);

CREATE TABLE IF NOT EXISTS email_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    subject VARCHAR(255) NOT NULL,
    body VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS sms_notifications (
    id UUID PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,
    message VARCHAR(500) NOT NULL,
    provider_response VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
