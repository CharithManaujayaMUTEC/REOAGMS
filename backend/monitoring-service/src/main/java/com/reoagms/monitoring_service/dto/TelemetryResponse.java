package com.reoagms.monitoring_service.dto;

import com.reoagms.monitoring_service.common.enums.DeviceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TelemetryResponse {
    private UUID id;
    private UUID assetId;
    private UUID facilityId;
    private DeviceStatus status;
    private LocalDateTime lastSeenAt;
    private Double batteryLevel;
    private Double signalStrength;
}
