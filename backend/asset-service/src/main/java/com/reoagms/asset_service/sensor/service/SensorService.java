package com.reoagms.asset_service.sensor.service;

import com.reoagms.asset_service.common.enums.SensorStatus;
import com.reoagms.asset_service.common.enums.SensorType;
import com.reoagms.asset_service.sensor.dto.SensorRequest;
import com.reoagms.asset_service.sensor.dto.SensorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SensorService {

    SensorResponse create(SensorRequest request);

    SensorResponse getById(UUID id);

    Page<SensorResponse> getAll(Pageable pageable);

    List<SensorResponse> getByAsset(UUID assetId);

    Page<SensorResponse> search(String name, SensorType type, SensorStatus status, UUID assetId, Pageable pageable);

    SensorResponse update(UUID id, SensorRequest request);

    void delete(UUID id);

}
