package com.reoagms.asset_service.facility.dto;

import com.reoagms.asset_service.common.enums.FacilityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FacilityRequest {

    @NotBlank
    private String name;

    @NotNull
    private FacilityType type;

    @NotBlank
    private String country;

    @NotBlank
    private String province;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    private Double capacityMW;

}