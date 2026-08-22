package com.reoagms.monitoring_service.dto;

import com.reoagms.monitoring_service.common.enums.MetricType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SensorReadingResponse {
    private UUID id;
    private UUID sensorId;
    private UUID assetId;
    private UUID facilityId;
    private LocalDateTime timestamp;
    private BigDecimal value;
    private String unit;
    private MetricType metricType;
}
