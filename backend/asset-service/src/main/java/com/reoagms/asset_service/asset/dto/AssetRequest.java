package com.reoagms.asset_service.asset.dto;

import com.reoagms.asset_service.common.enums.AssetStatus;
import com.reoagms.asset_service.common.enums.AssetType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AssetRequest {

    private String name;

    private AssetType type;

    private AssetStatus status;

    private String manufacturer;

    private String model;

    private String serialNumber;

    private LocalDate installationDate;

    private Double ratedCapacity;

    @NotNull
    private UUID facilityId;

}