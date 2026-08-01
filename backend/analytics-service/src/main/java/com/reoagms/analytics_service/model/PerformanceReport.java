package com.reoagms.analytics_service.model;

import com.reoagms.analytics_service.common.enums.ReportStatus;
import com.reoagms.analytics_service.common.enums.ScopeType;
import com.reoagms.analytics_service.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "performance_reports", indexes = {
        @Index(name = "idx_performance_report_scope", columnList = "scope_type,scope_id"),
        @Index(name = "idx_performance_report_period", columnList = "period_start,period_end")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReport extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private ScopeType scopeType;

    @Column(name = "scope_id")
    private UUID scopeId;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "availability_percentage", nullable = false, precision = 9, scale = 4)
    private BigDecimal availabilityPercentage;

    @Column(name = "efficiency_percentage", nullable = false, precision = 9, scale = 4)
    private BigDecimal efficiencyPercentage;

    @Column(name = "capacity_factor_percentage", nullable = false, precision = 9, scale = 4)
    private BigDecimal capacityFactorPercentage;

    @Column(name = "utilization_percentage", nullable = false, precision = 9, scale = 4)
    private BigDecimal utilizationPercentage;

    @Column(name = "downtime_hours", nullable = false, precision = 12, scale = 4)
    private BigDecimal downtimeHours;

    @Column(name = "alert_count", nullable = false)
    private Long alertCount;

    @Column(name = "maintenance_count", nullable = false)
    private Long maintenanceCount;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false, length = 20)
    private ReportStatus reportStatus;
}
