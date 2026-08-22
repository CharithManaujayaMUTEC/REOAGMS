package com.reoagms.monitoring_service.dto;

import com.reoagms.monitoring_service.common.enums.AlertSeverity;
import com.reoagms.monitoring_service.common.enums.MetricType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload for manually raising an alert (e.g. from an operator or another service),
 * as opposed to alerts generated automatically from AlertRule evaluation.
 */
@Data
public class AlertRequest {

    @NotNull(message = "assetId is required")
    private UUID assetId;

    private UUID facilityId;

    private MetricType metricType;

    private BigDecimal value;

    @NotNull(message = "severity is required")
    private AlertSeverity severity;

    @NotBlank(message = "message is required")
    private String message;
}
