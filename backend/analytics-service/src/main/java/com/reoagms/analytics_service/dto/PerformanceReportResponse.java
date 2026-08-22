package com.reoagms.analytics_service.dto;

import com.reoagms.analytics_service.common.enums.ReportStatus;
import com.reoagms.analytics_service.common.enums.ScopeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PerformanceReportResponse {
    private UUID id;
    private String name;
    private ScopeType scopeType;
    private UUID scopeId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal availabilityPercentage;
    private BigDecimal efficiencyPercentage;
    private BigDecimal capacityFactorPercentage;
    private BigDecimal utilizationPercentage;
    private BigDecimal downtimeHours;
    private Long alertCount;
    private Long maintenanceCount;
    private LocalDateTime generatedAt;
    private ReportStatus reportStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
