package com.reoagms.monitoring_service.model;

import com.reoagms.monitoring_service.common.enums.MetricType;
import com.reoagms.monitoring_service.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sensor_readings", indexes = {
        @Index(name = "idx_sensor_reading_sensor_id", columnList = "sensor_id"),
        @Index(name = "idx_sensor_reading_asset_id", columnList = "asset_id"),
        @Index(name = "idx_sensor_reading_facility_id", columnList = "facility_id"),
        @Index(name = "idx_sensor_reading_recorded_at", columnList = "recorded_at")
})
public class SensorReading extends BaseEntity {

    @Column(name = "sensor_id", nullable = false)
    private UUID sensorId;

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Column(name = "facility_id")
    private UUID facilityId;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "reading_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal value;

    @Column(length = 50)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MetricType metricType;
}
