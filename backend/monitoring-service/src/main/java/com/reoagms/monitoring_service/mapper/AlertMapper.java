package com.reoagms.monitoring_service.mapper;

import com.reoagms.monitoring_service.common.enums.AlertStatus;
import com.reoagms.monitoring_service.dto.AlertRequest;
import com.reoagms.monitoring_service.dto.AlertResponse;
import com.reoagms.monitoring_service.model.Alert;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AlertMapper {

    public Alert toEntity(AlertRequest request) {
        return Alert.builder()
                .assetId(request.getAssetId())
                .facilityId(request.getFacilityId())
                .metricType(request.getMetricType())
                .value(request.getValue())
                .severity(request.getSeverity())
                .status(AlertStatus.OPEN)
                .message(request.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public AlertResponse toResponse(Alert entity) {
        return AlertResponse.builder()
                .id(entity.getId())
                .facilityId(entity.getFacilityId())
                .assetId(entity.getAssetId())
                .sensorReadingId(entity.getSensorReadingId())
                .alertRuleId(entity.getAlertRuleId())
                .metricType(entity.getMetricType())
                .value(entity.getValue())
                .threshold(entity.getThreshold())
                .severity(entity.getSeverity())
                .status(entity.getStatus())
                .message(entity.getMessage())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
