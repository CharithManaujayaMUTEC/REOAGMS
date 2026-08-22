package com.reoagms.monitoring_service.model;

import com.reoagms.monitoring_service.common.enums.AlertSeverity;
import com.reoagms.monitoring_service.common.enums.AlertStatus;
import com.reoagms.monitoring_service.common.enums.MetricType;
import com.reoagms.monitoring_service.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_facility_id", columnList = "facility_id"),
        @Index(name = "idx_alert_asset_id", columnList = "asset_id"),
        @Index(name = "idx_alert_status", columnList = "status")
})
public class Alert extends BaseEntity {

    @Column(name = "facility_id")
    private UUID facilityId;

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Column(name = "sensor_reading_id")
    private UUID sensorReadingId;

    @Column(name = "alert_rule_id")
    private UUID alertRuleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", length = 30)
    private MetricType metricType;

    @Column(name = "reading_value", precision = 19, scale = 4)
    private BigDecimal value;

    @Column(name = "threshold_value", precision = 19, scale = 4)
    private BigDecimal threshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "raised_at", nullable = false)
    private LocalDateTime timestamp;
}
