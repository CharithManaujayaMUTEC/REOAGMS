package com.reoagms.monitoring_service.dto;

import com.reoagms.monitoring_service.common.enums.AlertSeverity;
import com.reoagms.monitoring_service.common.enums.AlertStatus;
import com.reoagms.monitoring_service.common.enums.MetricType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AlertResponse {
    private UUID id;
    private UUID facilityId;
    private UUID assetId;
    private UUID sensorReadingId;
    private UUID alertRuleId;
    private MetricType metricType;
    private BigDecimal value;
    private BigDecimal threshold;
    private AlertSeverity severity;
    private AlertStatus status;
    private String message;
    private LocalDateTime timestamp;
}
