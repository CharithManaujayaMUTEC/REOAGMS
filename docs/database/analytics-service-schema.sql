-- REOAGMS Analytics Service PostgreSQL reference schema
-- Run the first two statements as a PostgreSQL administrator.
-- Change the demonstration password before shared/production use.

CREATE USER analytics_user WITH PASSWORD 'analytics_password';
CREATE DATABASE analytics_db OWNER analytics_user;

-- Reconnect to analytics_db as analytics_user before running the remaining DDL.

CREATE TABLE IF NOT EXISTS dashboards (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    dashboard_type VARCHAR(40) NOT NULL,
    owner_user_id UUID,
    default_range_days INTEGER NOT NULL CHECK (default_range_days BETWEEN 1 AND 365),
    active BOOLEAN NOT NULL,
    layout_configuration TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS energy_reports (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    facility_id UUID,
    asset_id UUID,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    total_energy NUMERIC(19,4) NOT NULL,
    average_energy NUMERIC(19,4) NOT NULL,
    peak_energy NUMERIC(19,4) NOT NULL,
    expected_energy NUMERIC(19,4) NOT NULL,
    variance_percentage NUMERIC(9,4) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    report_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT chk_energy_period CHECK (period_start < period_end),
    CONSTRAINT chk_energy_scope CHECK (facility_id IS NOT NULL OR asset_id IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_energy_report_facility ON energy_reports(facility_id);
CREATE INDEX IF NOT EXISTS idx_energy_report_asset ON energy_reports(asset_id);
CREATE INDEX IF NOT EXISTS idx_energy_report_period ON energy_reports(period_start, period_end);

CREATE TABLE IF NOT EXISTS performance_reports (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    scope_type VARCHAR(20) NOT NULL,
    scope_id UUID,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    availability_percentage NUMERIC(9,4) NOT NULL,
    efficiency_percentage NUMERIC(9,4) NOT NULL,
    capacity_factor_percentage NUMERIC(9,4) NOT NULL,
    utilization_percentage NUMERIC(9,4) NOT NULL,
    downtime_hours NUMERIC(12,4) NOT NULL,
    alert_count BIGINT NOT NULL,
    maintenance_count BIGINT NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    report_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT chk_performance_period CHECK (period_start < period_end),
    CONSTRAINT chk_performance_scope CHECK (
        (scope_type = 'ORGANIZATION' AND scope_id IS NULL)
        OR (scope_type IN ('FACILITY', 'ASSET') AND scope_id IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_performance_report_scope ON performance_reports(scope_type, scope_id);
CREATE INDEX IF NOT EXISTS idx_performance_report_period ON performance_reports(period_start, period_end);

CREATE TABLE IF NOT EXISTS kpis (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    kpi_type VARCHAR(50) NOT NULL,
    scope_type VARCHAR(20) NOT NULL,
    scope_id UUID,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    actual_value NUMERIC(19,4) NOT NULL CHECK (actual_value >= 0),
    target_value NUMERIC(19,4) CHECK (target_value IS NULL OR target_value >= 0),
    unit VARCHAR(30) NOT NULL,
    kpi_status VARCHAR(20) NOT NULL,
    calculated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT chk_kpi_period CHECK (period_start < period_end),
    CONSTRAINT chk_kpi_scope CHECK (
        (scope_type = 'ORGANIZATION' AND scope_id IS NULL)
        OR (scope_type IN ('FACILITY', 'ASSET') AND scope_id IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_kpi_scope ON kpis(scope_type, scope_id);
CREATE INDEX IF NOT EXISTS idx_kpi_type ON kpis(kpi_type);
