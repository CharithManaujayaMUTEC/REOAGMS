package com.reoagms.asset_service.sensor.model;

import com.reoagms.asset_service.asset.model.Asset;
import com.reoagms.asset_service.common.enums.SensorStatus;
import com.reoagms.asset_service.common.enums.SensorType;
import com.reoagms.asset_service.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sensors")
public class Sensor extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private SensorType type;

    @Enumerated(EnumType.STRING)
    private SensorStatus status;

    private String unit;

    private String manufacturer;

    private String serialNumber;

    private LocalDate installationDate;

    private Double minThreshold;

    private Double maxThreshold;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

}
