package com.reoagms.asset_service.sensor.dto;

import com.reoagms.asset_service.common.enums.SensorStatus;
import com.reoagms.asset_service.common.enums.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class SensorRequest {

    @NotBlank(message = "Sensor name is required")
    private String name;

    @NotNull(message = "Sensor type is required")
    private SensorType type;

    @NotNull(message = "Sensor status is required")
    private SensorStatus status;

    @NotBlank(message = "Unit is required, e.g. 'C', 'V', 'A'")
    private String unit;

    private String manufacturer;

    private String serialNumber;

    @PastOrPresent(message = "Installation date cannot be in the future")
    private LocalDate installationDate;

    private Double minThreshold;

    private Double maxThreshold;

    @NotNull(message = "assetId is required")
    private UUID assetId;

}
