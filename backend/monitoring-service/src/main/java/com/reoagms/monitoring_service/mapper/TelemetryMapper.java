package com.reoagms.monitoring_service.mapper;

import com.reoagms.monitoring_service.dto.TelemetryRequest;
import com.reoagms.monitoring_service.dto.TelemetryResponse;
import com.reoagms.monitoring_service.model.Telemetry;
import org.springframework.stereotype.Component;

@Component
public class TelemetryMapper {

    public Telemetry toEntity(TelemetryRequest request) {
        return Telemetry.builder()
                .assetId(request.getAssetId())
                .facilityId(request.getFacilityId())
                .status(request.getStatus())
                .lastSeenAt(request.getLastSeenAt())
                .batteryLevel(request.getBatteryLevel())
                .signalStrength(request.getSignalStrength())
                .build();
    }

    public TelemetryResponse toResponse(Telemetry entity) {
        return TelemetryResponse.builder()
                .id(entity.getId())
                .assetId(entity.getAssetId())
                .facilityId(entity.getFacilityId())
                .status(entity.getStatus())
                .lastSeenAt(entity.getLastSeenAt())
                .batteryLevel(entity.getBatteryLevel())
                .signalStrength(entity.getSignalStrength())
                .build();
    }
}
