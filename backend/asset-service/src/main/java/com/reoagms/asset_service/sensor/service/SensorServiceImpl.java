package com.reoagms.asset_service.sensor.service;

import com.reoagms.asset_service.asset.model.Asset;
import com.reoagms.asset_service.asset.repository.AssetRepository;
import com.reoagms.asset_service.common.enums.SensorStatus;
import com.reoagms.asset_service.common.enums.SensorType;
import com.reoagms.asset_service.common.exception.InvalidRequestException;
import com.reoagms.asset_service.common.exception.ResourceNotFoundException;
import com.reoagms.asset_service.sensor.dto.SensorRequest;
import com.reoagms.asset_service.sensor.dto.SensorResponse;
import com.reoagms.asset_service.sensor.mapper.SensorMapper;
import com.reoagms.asset_service.sensor.model.Sensor;
import com.reoagms.asset_service.sensor.repository.SensorRepository;
import com.reoagms.asset_service.util.SensorSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;
    private final AssetRepository assetRepository;
    private final SensorMapper mapper;

    @Override
    public SensorResponse create(SensorRequest request) {

        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        validateThresholds(request);

        if (request.getSerialNumber() != null && !request.getSerialNumber().isBlank()
                && sensorRepository.existsBySerialNumberIgnoreCase(request.getSerialNumber())) {
            throw new InvalidRequestException("A sensor with this serial number already exists");
        }

        Sensor sensor = mapper.toEntity(request, asset);

        sensorRepository.save(sensor);

        return mapper.toResponse(sensor);
    }

    @Override
    public SensorResponse getById(UUID id) {

        Sensor sensor = sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found"));

        return mapper.toResponse(sensor);
    }

    @Override
    public Page<SensorResponse> getAll(Pageable pageable) {

        return sensorRepository.findAll(pageable)
                .map(mapper::toResponse);
    }

    @Override
    public List<SensorResponse> getByAsset(UUID assetId) {

        return sensorRepository.findByAssetId(assetId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public Page<SensorResponse> search(String name, SensorType type, SensorStatus status, UUID assetId, Pageable pageable) {

        return sensorRepository.findAll(
                        SensorSpecifications.search(name, type, status, assetId),
                        pageable)
                .map(mapper::toResponse);
    }

    @Override
    public SensorResponse update(UUID id, SensorRequest request) {

        Sensor sensor = sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found"));

        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        validateThresholds(request);

        sensor.setName(request.getName());
        sensor.setType(request.getType());
        sensor.setStatus(request.getStatus());
        sensor.setUnit(request.getUnit());
        sensor.setManufacturer(request.getManufacturer());
        sensor.setSerialNumber(request.getSerialNumber());
        sensor.setInstallationDate(request.getInstallationDate());
        sensor.setMinThreshold(request.getMinThreshold());
        sensor.setMaxThreshold(request.getMaxThreshold());
        sensor.setAsset(asset);

        sensorRepository.save(sensor);

        return mapper.toResponse(sensor);
    }

    @Override
    public void delete(UUID id) {

        if (!sensorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sensor not found");
        }

        sensorRepository.deleteById(id);

    }

    private void validateThresholds(SensorRequest request) {

        if (request.getMinThreshold() != null && request.getMaxThreshold() != null
                && request.getMinThreshold() > request.getMaxThreshold()) {
            throw new InvalidRequestException("minThreshold cannot be greater than maxThreshold");
        }

    }

}
