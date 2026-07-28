package com.reoagms.asset_service.facility.mapper;

import com.reoagms.asset_service.facility.dto.*;
import com.reoagms.asset_service.facility.model.Facility;
import org.springframework.stereotype.Component;

@Component
public class FacilityMapper {

    public Facility toEntity(FacilityRequest request) {

        return Facility.builder()
                .name(request.getName())
                .type(request.getType())
                .country(request.getCountry())
                .province(request.getProvince())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .capacityMW(request.getCapacityMW())
                .build();

    }

    public FacilityResponse toResponse(Facility facility) {

        return FacilityResponse.builder()
                .id(facility.getId())
                .name(facility.getName())
                .type(facility.getType())
                .country(facility.getCountry())
                .province(facility.getProvince())
                .latitude(facility.getLatitude())
                .longitude(facility.getLongitude())
                .capacityMW(facility.getCapacityMW())
                .build();

    }

}