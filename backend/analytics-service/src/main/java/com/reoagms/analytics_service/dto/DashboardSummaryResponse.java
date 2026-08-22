package com.reoagms.analytics_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DashboardSummaryResponse {
    private UUID facilityId;
    private UUID assetId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal totalEnergy;
    private BigDecimal averageEnergy;
    private BigDecimal peakEnergy;
    private String energyUnit;
    private BigDecimal availabilityPercentage;
    private BigDecimal efficiencyPercentage;
    private BigDecimal capacityFactorPercentage;
    private BigDecimal utilizationPercentage;
    private long activeAlertCount;
    private long openWorkOrderCount;
    private BigDecimal maintenanceCompletionRate;
    private List<TimeSeriesPoint> energyTrend;
}
