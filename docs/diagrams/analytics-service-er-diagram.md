# Analytics Service ER Diagram

The service deliberately contains no database foreign keys to users, facilities, assets, sensors, alerts, or work orders. IDs belonging to other services are external references only.

```mermaid
erDiagram
    DASHBOARDS {
        uuid id PK
        varchar name
        varchar dashboard_type
        uuid owner_user_id "external Identity Service reference"
        int default_range_days
        boolean active
        text layout_configuration
        timestamp created_at
        timestamp updated_at
    }

    ENERGY_REPORTS {
        uuid id PK
        varchar name
        uuid facility_id "external Asset Service reference"
        uuid asset_id "external Asset Service reference"
        timestamp period_start
        timestamp period_end
        numeric total_energy
        numeric average_energy
        numeric peak_energy
        numeric expected_energy
        numeric variance_percentage
        varchar unit
        timestamp generated_at
        varchar report_status
        timestamp created_at
        timestamp updated_at
    }

    PERFORMANCE_REPORTS {
        uuid id PK
        varchar name
        varchar scope_type
        uuid scope_id "external facility or asset reference"
        timestamp period_start
        timestamp period_end
        numeric availability_percentage
        numeric efficiency_percentage
        numeric capacity_factor_percentage
        numeric utilization_percentage
        numeric downtime_hours
        bigint alert_count
        bigint maintenance_count
        timestamp generated_at
        varchar report_status
        timestamp created_at
        timestamp updated_at
    }

    KPIS {
        uuid id PK
        varchar name
        varchar kpi_type
        varchar scope_type
        uuid scope_id "external facility or asset reference"
        timestamp period_start
        timestamp period_end
        numeric actual_value
        numeric target_value
        varchar unit
        varchar kpi_status
        timestamp calculated_at
        timestamp created_at
        timestamp updated_at
    }
```

The four aggregates are intentionally independent. A generated report is a historical snapshot, while a KPI can be managed independently and a dashboard stores presentation configuration only.
