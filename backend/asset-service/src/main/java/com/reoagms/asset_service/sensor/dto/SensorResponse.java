package com.reoagms.asset_service.sensor.dto;

import com.reoagms.asset_service.common.enums.SensorStatus;
import com.reoagms.asset_service.common.enums.SensorType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class SensorResponse {

    private UUID id;

    private String name;

    private SensorType type;

    private SensorStatus status;

    private String unit;

    private String manufacturer;

    private String serialNumber;

    private LocalDate installationDate;

    private Double minThreshold;

    private Double maxThreshold;

    private UUID assetId;

}
