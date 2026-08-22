package com.reoagms.analytics_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PerformanceMetrics {
    private BigDecimal availabilityPercentage;
    private BigDecimal efficiencyPercentage;
    private BigDecimal capacityFactorPercentage;
    private BigDecimal utilizationPercentage;
    private BigDecimal downtimeHours;
    private long alertCount;
    private long maintenanceCount;
    private long openWorkOrderCount;
    private BigDecimal maintenanceCompletionRate;
}
