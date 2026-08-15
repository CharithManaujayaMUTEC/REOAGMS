package com.reoagms.monitoring_service.service;

import com.reoagms.monitoring_service.common.enums.MetricType;
import com.reoagms.monitoring_service.common.exception.ResourceNotFoundException;
import com.reoagms.monitoring_service.dto.SensorReadingRequest;
import com.reoagms.monitoring_service.dto.SensorReadingResponse;
import com.reoagms.monitoring_service.mapper.SensorReadingMapper;
import com.reoagms.monitoring_service.model.SensorReading;
import com.reoagms.monitoring_service.repository.SensorReadingRepository;
import com.reoagms.monitoring_service.util.SensorReadingSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SensorReadingServiceImpl implements SensorReadingService {

    private final SensorReadingRepository repository;
    private final SensorReadingMapper mapper;
    private final AlertService alertService;

    @Override
    public SensorReadingResponse create(SensorReadingRequest request) {
        SensorReading saved = repository.save(mapper.toEntity(request));
        alertService.evaluateReading(saved);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getAll(UUID facilityId, UUID assetId, UUID sensorId,
                                               MetricType metricType, LocalDateTime from, LocalDateTime to) {
        var spec = SensorReadingSpecifications.filter(facilityId, assetId, sensorId, metricType, from, to);
        return repository.findAll(spec, Sort.by(Sort.Direction.DESC, "timestamp"))
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SensorReadingResponse getById(UUID id) {
        return mapper.toResponse(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getBySensor(UUID sensorId) {
        return repository.findBySensorId(sensorId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getByAsset(UUID assetId) {
        return repository.findByAssetId(assetId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SensorReadingResponse getLatestForSensor(UUID sensorId) {
        SensorReading reading = repository.findFirstBySensorIdOrderByTimestampDesc(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("No readings found for sensor: " + sensorId));
        return mapper.toResponse(reading);
    }

    private SensorReading findEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor reading not found: " + id));
    }
}
