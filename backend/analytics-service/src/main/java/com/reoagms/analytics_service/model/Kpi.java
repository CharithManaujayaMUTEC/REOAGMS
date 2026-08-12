package com.reoagms.analytics_service.model;

import com.reoagms.analytics_service.common.enums.KpiStatus;
import com.reoagms.analytics_service.common.enums.KpiType;
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
@Table(name = "kpis", indexes = {
        @Index(name = "idx_kpi_scope", columnList = "scope_type,scope_id"),
        @Index(name = "idx_kpi_type", columnList = "kpi_type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kpi extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "kpi_type", nullable = false, length = 50)
    private KpiType kpiType;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private ScopeType scopeType;

    @Column(name = "scope_id")
    private UUID scopeId;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "actual_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal actualValue;

    @Column(name = "target_value", precision = 19, scale = 4)
    private BigDecimal targetValue;

    @Column(nullable = false, length = 30)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "kpi_status", nullable = false, length = 20)
    private KpiStatus kpiStatus;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
}
