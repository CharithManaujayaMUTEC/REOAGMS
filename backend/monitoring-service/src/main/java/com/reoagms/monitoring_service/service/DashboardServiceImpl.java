package com.reoagms.monitoring_service.service;

import com.reoagms.monitoring_service.common.enums.AlertSeverity;
import com.reoagms.monitoring_service.common.enums.AlertStatus;
import com.reoagms.monitoring_service.common.enums.DeviceStatus;
import com.reoagms.monitoring_service.dto.DashboardSummaryResponse;
import com.reoagms.monitoring_service.repository.AlertRepository;
import com.reoagms.monitoring_service.repository.SensorReadingRepository;
import com.reoagms.monitoring_service.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final SensorReadingRepository sensorReadingRepository;
    private final AlertRepository alertRepository;
    private final TelemetryRepository telemetryRepository;

    @Override
    public DashboardSummaryResponse getSummary() {
        Map<String, Long> alertsBySeverity = new LinkedHashMap<>();
        for (AlertSeverity severity : AlertSeverity.values()) {
            alertsBySeverity.put(severity.name(), alertRepository.countBySeverity(severity));
        }

        return DashboardSummaryResponse.builder()
                .totalReadingsLast24h(sensorReadingRepository.countByTimestampAfter(LocalDateTime.now().minusHours(24)))
                .openAlerts(alertRepository.countByStatus(AlertStatus.OPEN))
                .acknowledgedAlerts(alertRepository.countByStatus(AlertStatus.ACKNOWLEDGED))
                .resolvedAlerts(alertRepository.countByStatus(AlertStatus.RESOLVED))
                .alertsBySeverity(alertsBySeverity)
                .onlineDevices(telemetryRepository.countByStatus(DeviceStatus.ONLINE))
                .offlineDevices(telemetryRepository.countByStatus(DeviceStatus.OFFLINE))
                .warningDevices(telemetryRepository.countByStatus(DeviceStatus.WARNING))
                .build();
    }
}
