package com.reoagms.monitoring_service.dto;

import com.reoagms.monitoring_service.common.enums.AlertSeverity;
import com.reoagms.monitoring_service.common.enums.ConditionOperator;
import com.reoagms.monitoring_service.common.enums.MetricType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AlertRuleRequest {

    @NotBlank(message = "name is required")
    private String name;

    private UUID assetId;

    private UUID facilityId;

    @NotNull(message = "metricType is required")
    private MetricType metricType;

    @NotNull(message = "operator is required")
    private ConditionOperator operator;

    @NotNull(message = "threshold is required")
    private BigDecimal threshold;

    @NotNull(message = "severity is required")
    private AlertSeverity severity;

    private Boolean enabled;

    private String description;
}
