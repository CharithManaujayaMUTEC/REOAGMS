package com.reoagms.monitoring_service.service;

import com.reoagms.monitoring_service.common.enums.AlertStatus;
import com.reoagms.monitoring_service.dto.AlertRequest;
import com.reoagms.monitoring_service.dto.AlertResponse;
import com.reoagms.monitoring_service.model.SensorReading;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AlertService {

    AlertResponse create(AlertRequest request);

    List<AlertResponse> getAll(UUID facilityId, UUID assetId, AlertStatus status,
                                LocalDateTime from, LocalDateTime to);

    AlertResponse getById(UUID id);

    AlertResponse acknowledge(UUID id);

    AlertResponse resolve(UUID id);

    /**
     * Evaluates all enabled alert rules matching the reading's asset/metric and
     * raises an alert for every breached rule.
     */
    List<AlertResponse> evaluateReading(SensorReading reading);
}
