package com.reoagms.asset_service.asset.dto;

import com.reoagms.asset_service.common.enums.AssetStatus;
import com.reoagms.asset_service.common.enums.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AssetRequest {

    @NotBlank(message = "Asset name is required")
    private String name;

    @NotNull(message = "Asset type is required")
    private AssetType type;

    @NotNull(message = "Asset status is required")
    private AssetStatus status;

    private String manufacturer;

    private String model;

    private String serialNumber;

    @PastOrPresent(message = "Installation date cannot be in the future")
    private LocalDate installationDate;

    @Positive(message = "ratedCapacity must be a positive number")
    private Double ratedCapacity;

    @NotNull(message = "facilityId is required")
    private UUID facilityId;

}