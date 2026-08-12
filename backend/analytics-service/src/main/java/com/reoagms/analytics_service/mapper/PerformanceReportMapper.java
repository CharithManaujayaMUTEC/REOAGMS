package com.reoagms.analytics_service.mapper;

import com.reoagms.analytics_service.dto.PerformanceReportResponse;
import com.reoagms.analytics_service.model.PerformanceReport;
import org.springframework.stereotype.Component;

@Component
public class PerformanceReportMapper {

    public PerformanceReportResponse toResponse(PerformanceReport entity) {
        return PerformanceReportResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .scopeType(entity.getScopeType())
                .scopeId(entity.getScopeId())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .availabilityPercentage(entity.getAvailabilityPercentage())
                .efficiencyPercentage(entity.getEfficiencyPercentage())
                .capacityFactorPercentage(entity.getCapacityFactorPercentage())
                .utilizationPercentage(entity.getUtilizationPercentage())
                .downtimeHours(entity.getDowntimeHours())
                .alertCount(entity.getAlertCount())
                .maintenanceCount(entity.getMaintenanceCount())
                .generatedAt(entity.getGeneratedAt())
                .reportStatus(entity.getReportStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
