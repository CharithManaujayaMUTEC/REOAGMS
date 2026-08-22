package com.reoagms.monitoring_service.dto;

import com.reoagms.monitoring_service.common.enums.DeviceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TelemetryRequest {

    @NotNull(message = "assetId is required")
    private UUID assetId;

    private UUID facilityId;

    @NotNull(message = "status is required")
    private DeviceStatus status;

    @NotNull(message = "lastSeenAt is required")
    private LocalDateTime lastSeenAt;

    private Double batteryLevel;

    private Double signalStrength;
}
