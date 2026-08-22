package com.reoagms.monitoring_service.mapper;

import com.reoagms.monitoring_service.dto.SensorReadingRequest;
import com.reoagms.monitoring_service.dto.SensorReadingResponse;
import com.reoagms.monitoring_service.model.SensorReading;
import org.springframework.stereotype.Component;

@Component
public class SensorReadingMapper {

    public SensorReading toEntity(SensorReadingRequest request) {
        return SensorReading.builder()
                .sensorId(request.getSensorId())
                .assetId(request.getAssetId())
                .facilityId(request.getFacilityId())
                .timestamp(request.getTimestamp())
                .value(request.getValue())
                .unit(request.getUnit())
                .metricType(request.getMetricType())
                .build();
    }

    public SensorReadingResponse toResponse(SensorReading entity) {
        return SensorReadingResponse.builder()
                .id(entity.getId())
                .sensorId(entity.getSensorId())
                .assetId(entity.getAssetId())
                .facilityId(entity.getFacilityId())
                .timestamp(entity.getTimestamp())
                .value(entity.getValue())
                .unit(entity.getUnit())
                .metricType(entity.getMetricType())
                .build();
    }
}
