package com.reoagms.monitoring_service.dto;

import com.reoagms.monitoring_service.common.enums.AlertSeverity;
import com.reoagms.monitoring_service.common.enums.ConditionOperator;
import com.reoagms.monitoring_service.common.enums.MetricType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class AlertRuleResponse {
    private UUID id;
    private String name;
    private UUID assetId;
    private UUID facilityId;
    private MetricType metricType;
    private ConditionOperator operator;
    private BigDecimal threshold;
    private AlertSeverity severity;
    private Boolean enabled;
    private String description;
}
