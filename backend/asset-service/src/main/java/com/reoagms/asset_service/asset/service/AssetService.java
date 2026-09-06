package com.reoagms.asset_service.asset.service;

import com.reoagms.asset_service.asset.dto.AssetRequest;
import com.reoagms.asset_service.asset.dto.AssetResponse;
import com.reoagms.asset_service.common.enums.AssetStatus;
import com.reoagms.asset_service.common.enums.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AssetService {

    AssetResponse create(AssetRequest request);

    AssetResponse getById(UUID id);

    List<AssetResponse> getAll();

    Page<AssetResponse> getAll(Pageable pageable);

    List<AssetResponse> getByFacility(UUID facilityId);

    Page<AssetResponse> getByFacility(UUID facilityId, Pageable pageable);

    Page<AssetResponse> search(
            String name,
            AssetType type,
            AssetStatus status,
            String manufacturer,
            UUID facilityId,
            Pageable pageable);

    AssetResponse update(UUID id, AssetRequest request);

    void delete(UUID id);

}