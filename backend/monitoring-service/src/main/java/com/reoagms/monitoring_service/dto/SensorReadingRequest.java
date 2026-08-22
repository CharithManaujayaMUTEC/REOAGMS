package com.reoagms.monitoring_service.dto;

import com.reoagms.monitoring_service.common.enums.MetricType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SensorReadingRequest {

    @NotNull(message = "sensorId is required")
    private UUID sensorId;

    @NotNull(message = "assetId is required")
    private UUID assetId;

    private UUID facilityId;

    @NotNull(message = "timestamp is required")
    private LocalDateTime timestamp;

    @NotNull(message = "value is required")
    private BigDecimal value;

    private String unit;

    @NotNull(message = "metricType is required")
    private MetricType metricType;
}
