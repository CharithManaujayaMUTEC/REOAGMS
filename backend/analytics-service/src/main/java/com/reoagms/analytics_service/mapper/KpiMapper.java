package com.reoagms.analytics_service.mapper;

import com.reoagms.analytics_service.common.enums.KpiStatus;
import com.reoagms.analytics_service.dto.KpiRequest;
import com.reoagms.analytics_service.dto.KpiResponse;
import com.reoagms.analytics_service.model.Kpi;
import org.springframework.stereotype.Component;

@Component
public class KpiMapper {

    public Kpi toEntity(KpiRequest request) {
        Kpi entity = new Kpi();
        update(entity, request);
        return entity;
    }

    public void update(Kpi entity, KpiRequest request) {
        entity.setName(request.getName().trim());
        entity.setKpiType(request.getKpiType());
        entity.setScopeType(request.getScopeType());
        entity.setScopeId(request.getScopeId());
        entity.setPeriodStart(request.getPeriodStart());
        entity.setPeriodEnd(request.getPeriodEnd());
        entity.setActualValue(request.getActualValue());
        entity.setTargetValue(request.getTargetValue());
        entity.setUnit(request.getUnit().trim());
        entity.setKpiStatus(resolveStatus(request));
    }

    private KpiStatus resolveStatus(KpiRequest request) {
        if (request.getTargetValue() == null) {
            return KpiStatus.NO_TARGET;
        }
        int comparison = request.getActualValue().compareTo(request.getTargetValue());
        if (comparison > 0) {
            return KpiStatus.ABOVE_TARGET;
        }
        if (comparison < 0) {
            return KpiStatus.BELOW_TARGET;
        }
        return KpiStatus.ON_TARGET;
    }

    public KpiResponse toResponse(Kpi entity) {
        return KpiResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .kpiType(entity.getKpiType())
                .scopeType(entity.getScopeType())
                .scopeId(entity.getScopeId())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .actualValue(entity.getActualValue())
                .targetValue(entity.getTargetValue())
                .unit(entity.getUnit())
                .kpiStatus(entity.getKpiStatus())
                .calculatedAt(entity.getCalculatedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
