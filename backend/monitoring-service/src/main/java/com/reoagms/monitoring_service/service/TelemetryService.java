package com.reoagms.monitoring_service.service;

import com.reoagms.monitoring_service.dto.TelemetryRequest;
import com.reoagms.monitoring_service.dto.TelemetryResponse;

import java.util.List;
import java.util.UUID;

public interface TelemetryService {

    TelemetryResponse record(TelemetryRequest request);

    List<TelemetryResponse> getAll();

    List<TelemetryResponse> getByAsset(UUID assetId);

    TelemetryResponse getLatestForAsset(UUID assetId);
}
