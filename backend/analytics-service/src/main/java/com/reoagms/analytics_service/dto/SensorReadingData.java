package com.reoagms.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SensorReadingData {
    private UUID id;
    private UUID sensorId;
    private UUID assetId;
    private UUID facilityId;
    private LocalDateTime timestamp;
    private BigDecimal value;
    private String unit;
    private String metricType;
}
