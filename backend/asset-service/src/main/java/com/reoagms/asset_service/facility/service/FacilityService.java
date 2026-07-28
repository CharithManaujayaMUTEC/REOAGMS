package com.reoagms.asset_service.facility.service;

import com.reoagms.asset_service.facility.dto.FacilityRequest;
import com.reoagms.asset_service.facility.dto.FacilityResponse;

import java.util.List;
import java.util.UUID;

public interface FacilityService {

    FacilityResponse create(FacilityRequest request);

    FacilityResponse getById(UUID id);

    List<FacilityResponse> getAll();

    FacilityResponse update(UUID id, FacilityRequest request);

    void delete(UUID id);

}