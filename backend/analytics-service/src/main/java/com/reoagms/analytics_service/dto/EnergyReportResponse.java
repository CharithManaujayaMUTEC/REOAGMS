package com.reoagms.analytics_service.dto;

import com.reoagms.analytics_service.common.enums.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EnergyReportResponse {
    private UUID id;
    private String name;
    private UUID facilityId;
    private UUID assetId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal totalEnergy;
    private BigDecimal averageEnergy;
    private BigDecimal peakEnergy;
    private BigDecimal expectedEnergy;
    private BigDecimal variancePercentage;
    private String unit;
    private LocalDateTime generatedAt;
    private ReportStatus reportStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
