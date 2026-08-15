package com.reoagms.monitoring_service.model;

import com.reoagms.monitoring_service.common.enums.AlertSeverity;
import com.reoagms.monitoring_service.common.enums.ConditionOperator;
import com.reoagms.monitoring_service.common.enums.MetricType;
import com.reoagms.monitoring_service.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "alert_rules")
public class AlertRule extends BaseEntity {

    @Column(nullable = false)
    private String name;

    /**
     * When null, the rule applies to every asset for the given metric type.
     */
    @Column(name = "asset_id")
    private UUID assetId;

    @Column(name = "facility_id")
    private UUID facilityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 30)
    private MetricType metricType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConditionOperator operator;

    @Column(name = "threshold_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal threshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    private String description;
}
