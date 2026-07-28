package com.reoagms.asset_service.facility.dto;

import com.reoagms.asset_service.common.enums.FacilityType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class FacilityResponse {

    private UUID id;

    private String name;

    private FacilityType type;

    private String country;

    private String province;

    private Double latitude;

    private Double longitude;

    private Double capacityMW;

}