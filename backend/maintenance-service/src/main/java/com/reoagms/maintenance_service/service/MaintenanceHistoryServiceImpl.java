package com.reoagms.maintenance_service.service;

import com.reoagms.maintenance_service.dto.MaintenanceHistoryRequest;
import com.reoagms.maintenance_service.dto.MaintenanceHistoryResponse;
import com.reoagms.maintenance_service.mapper.MaintenanceHistoryMapper;
import com.reoagms.maintenance_service.repository.MaintenanceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceHistoryServiceImpl implements MaintenanceHistoryService {

    private final MaintenanceHistoryRepository repository;
    private final MaintenanceHistoryMapper mapper;

    @Override
    public MaintenanceHistoryResponse create(MaintenanceHistoryRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceHistoryResponse> getAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceHistoryResponse> getByAsset(UUID assetId) {
        return repository.findByAssetId(assetId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceHistoryResponse> getByWorkOrder(UUID workOrderId) {
        return repository.findByWorkOrderId(workOrderId).stream().map(mapper::toResponse).toList();
    }
}
