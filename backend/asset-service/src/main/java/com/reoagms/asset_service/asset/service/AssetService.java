package com.reoagms.asset_service.asset.service;

import com.reoagms.asset_service.asset.dto.AssetRequest;
import com.reoagms.asset_service.asset.dto.AssetResponse;

import java.util.List;
import java.util.UUID;

public interface AssetService {

    AssetResponse create(AssetRequest request);

    AssetResponse getById(UUID id);

    List<AssetResponse> getAll();

    List<AssetResponse> getByFacility(UUID facilityId);

    AssetResponse update(UUID id, AssetRequest request);

    void delete(UUID id);

}