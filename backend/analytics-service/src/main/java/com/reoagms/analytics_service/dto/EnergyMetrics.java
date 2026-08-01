package com.reoagms.analytics_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EnergyMetrics {
    private BigDecimal totalEnergy;
    private BigDecimal averageEnergy;
    private BigDecimal peakEnergy;
    private BigDecimal expectedEnergy;
    private BigDecimal variancePercentage;
    private String unit;
}
