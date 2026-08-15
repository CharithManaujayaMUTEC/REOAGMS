package com.reoagms.monitoring_service.service;

import com.reoagms.monitoring_service.common.exception.ResourceNotFoundException;
import com.reoagms.monitoring_service.dto.TelemetryRequest;
import com.reoagms.monitoring_service.dto.TelemetryResponse;
import com.reoagms.monitoring_service.mapper.TelemetryMapper;
import com.reoagms.monitoring_service.model.Telemetry;
import com.reoagms.monitoring_service.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TelemetryServiceImpl implements TelemetryService {

    private final TelemetryRepository repository;
    private final TelemetryMapper mapper;

    @Override
    public TelemetryResponse record(TelemetryRequest request) {
        Telemetry saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TelemetryResponse> getAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TelemetryResponse> getByAsset(UUID assetId) {
        return repository.findByAssetId(assetId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TelemetryResponse getLatestForAsset(UUID assetId) {
        Telemetry entity = repository.findFirstByAssetIdOrderByLastSeenAtDesc(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("No telemetry found for asset: " + assetId));
        return mapper.toResponse(entity);
    }
}
