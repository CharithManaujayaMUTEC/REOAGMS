package com.reoagms.monitoring_service.mapper;

import com.reoagms.monitoring_service.dto.AlertRuleRequest;
import com.reoagms.monitoring_service.dto.AlertRuleResponse;
import com.reoagms.monitoring_service.model.AlertRule;
import org.springframework.stereotype.Component;

@Component
public class AlertRuleMapper {

    public AlertRule toEntity(AlertRuleRequest request) {
        return AlertRule.builder()
                .name(request.getName())
                .assetId(request.getAssetId())
                .facilityId(request.getFacilityId())
                .metricType(request.getMetricType())
                .operator(request.getOperator())
                .threshold(request.getThreshold())
                .severity(request.getSeverity())
                .enabled(request.getEnabled() == null || request.getEnabled())
                .description(request.getDescription())
                .build();
    }

    public void updateEntity(AlertRule entity, AlertRuleRequest request) {
        entity.setName(request.getName());
        entity.setAssetId(request.getAssetId());
        entity.setFacilityId(request.getFacilityId());
        entity.setMetricType(request.getMetricType());
        entity.setOperator(request.getOperator());
        entity.setThreshold(request.getThreshold());
        entity.setSeverity(request.getSeverity());
        entity.setEnabled(request.getEnabled() == null || request.getEnabled());
        entity.setDescription(request.getDescription());
    }

    public AlertRuleResponse toResponse(AlertRule entity) {
        return AlertRuleResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .assetId(entity.getAssetId())
                .facilityId(entity.getFacilityId())
                .metricType(entity.getMetricType())
                .operator(entity.getOperator())
                .threshold(entity.getThreshold())
                .severity(entity.getSeverity())
                .enabled(entity.getEnabled())
                .description(entity.getDescription())
                .build();
    }
}
