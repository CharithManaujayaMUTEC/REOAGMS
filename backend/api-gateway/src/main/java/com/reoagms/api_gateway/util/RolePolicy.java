package com.reoagms.api_gateway.util;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Role-based access control at the gateway (FR-02). Reads are open to every authenticated role;
 * writes are restricted per business area. EXECUTIVE_MANAGER is therefore read-only.
 */
@Component
public class RolePolicy {
    private static final Set<String> SAFE = Set.of("GET", "HEAD", "OPTIONS");
    private static final Set<String> ADMIN = Set.of("ADMIN");
    private static final Set<String> MANAGERS = Set.of("ADMIN", "OPERATIONS_MANAGER");
    private static final Set<String> OPERATIONS = Set.of("ADMIN", "OPERATIONS_MANAGER", "PLANT_OPERATOR");
    private static final Set<String> MAINTENANCE = Set.of("ADMIN", "OPERATIONS_MANAGER", "MAINTENANCE_ENGINEER");

    private record Rule(String prefix, Set<String> writers) { }

    private static final List<Rule> RULES = List.of(
            new Rule("/api/v1/users", ADMIN),
            new Rule("/api/v1/audit-logs", ADMIN),
            new Rule("/api/v1/assets", MANAGERS),
            new Rule("/api/v1/facilities", MANAGERS),
            new Rule("/api/v1/sensors", MANAGERS),
            new Rule("/api/v1/alert-rules", MANAGERS),
            new Rule("/api/v1/alerts", OPERATIONS),
            new Rule("/api/v1/work-orders", MAINTENANCE),
            new Rule("/api/v1/maintenance-tasks", MAINTENANCE),
            new Rule("/api/v1/technicians", MANAGERS),
            new Rule("/api/v1/maintenance-history", MAINTENANCE),
            new Rule("/api/v1/dashboards", MANAGERS),
            new Rule("/api/v1/kpis", MANAGERS),
            new Rule("/api/v1/energy-reports", MANAGERS),
            new Rule("/api/v1/performance-reports", MANAGERS),
            new Rule("/api/v1/notifications", MANAGERS),
            new Rule("/api/v1/email", MANAGERS),
            new Rule("/api/v1/sms", MANAGERS));

    public boolean isAllowed(String method, String path, String role) {
        if (SAFE.contains(method.toUpperCase())) return true;
        // Sensor/telemetry ingestion is machine traffic from any authenticated principal (e.g. the simulator).
        if (path.startsWith("/api/v1/sensor-readings") || path.startsWith("/api/v1/telemetry")) return true;
        if (role == null) return false;
        return RULES.stream().filter(r -> path.startsWith(r.prefix())).findFirst()
                .map(r -> r.writers().contains(role)).orElse(true);
    }
}
