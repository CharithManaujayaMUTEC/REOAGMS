package com.reoagms.monitoring_service.service;

import com.reoagms.monitoring_service.common.enums.MetricType;
import com.reoagms.monitoring_service.dto.SensorReadingRequest;
import com.reoagms.monitoring_service.dto.SensorReadingResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SensorReadingService {

    SensorReadingResponse create(SensorReadingRequest request);

    List<SensorReadingResponse> getAll(UUID facilityId, UUID assetId, UUID sensorId,
                                        MetricType metricType, LocalDateTime from, LocalDateTime to);

    SensorReadingResponse getById(UUID id);

    List<SensorReadingResponse> getBySensor(UUID sensorId);

    List<SensorReadingResponse> getByAsset(UUID assetId);

    SensorReadingResponse getLatestForSensor(UUID sensorId);
}
