package com.reoagms.monitoring_service.model;

import com.reoagms.monitoring_service.common.enums.DeviceStatus;
import com.reoagms.monitoring_service.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Device-level connectivity/heartbeat signal, distinct from SensorReading (metric values).
 * Used to track whether an asset's monitoring hardware is online.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "telemetry", indexes = {
        @Index(name = "idx_telemetry_asset_id", columnList = "asset_id")
})
public class Telemetry extends BaseEntity {

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Column(name = "facility_id")
    private UUID facilityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceStatus status;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    @Column(name = "battery_level")
    private Double batteryLevel;

    @Column(name = "signal_strength")
    private Double signalStrength;
}
