package com.reoagms.analytics_service.mapper;

import com.reoagms.analytics_service.dto.EnergyReportResponse;
import com.reoagms.analytics_service.model.EnergyReport;
import org.springframework.stereotype.Component;

@Component
public class EnergyReportMapper {

    public EnergyReportResponse toResponse(EnergyReport entity) {
        return EnergyReportResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .facilityId(entity.getFacilityId())
                .assetId(entity.getAssetId())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .totalEnergy(entity.getTotalEnergy())
                .averageEnergy(entity.getAverageEnergy())
                .peakEnergy(entity.getPeakEnergy())
                .expectedEnergy(entity.getExpectedEnergy())
                .variancePercentage(entity.getVariancePercentage())
                .unit(entity.getUnit())
                .generatedAt(entity.getGeneratedAt())
                .reportStatus(entity.getReportStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
