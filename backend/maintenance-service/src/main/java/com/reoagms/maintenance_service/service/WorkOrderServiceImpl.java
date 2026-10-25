package com.reoagms.maintenance_service.service;

import com.reoagms.maintenance_service.common.enums.WorkOrderStatus;
import com.reoagms.maintenance_service.dto.WorkOrderRequest;
import com.reoagms.maintenance_service.dto.WorkOrderResponse;
import com.reoagms.maintenance_service.mapper.WorkOrderMapper;
import com.reoagms.maintenance_service.model.WorkOrder;
import com.reoagms.maintenance_service.repository.WorkOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository repository;
    private final WorkOrderMapper mapper;

    @Override
    public WorkOrderResponse create(WorkOrderRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkOrderResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrderResponse getById(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    public WorkOrderResponse update(UUID id, WorkOrderRequest request) {
        WorkOrder e = find(id);

        e.setAssetId(request.getAssetId());
        e.setTechnicianId(request.getTechnicianId());
        e.setTitle(request.getTitle());
        e.setDescription(request.getDescription());
        e.setMaintenanceType(request.getMaintenanceType());
        e.setScheduledAt(request.getScheduledAt());

        return mapper.toResponse(repository.save(e));
    }

    @Override
    public void delete(UUID id) {
        repository.delete(find(id));
    }

    @Override
    public WorkOrderResponse assignTechnician(UUID id, UUID technicianId) {
        WorkOrder e = find(id);
        if (e.getStatus() == WorkOrderStatus.COMPLETED || e.getStatus() == WorkOrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot assign a technician to a " + e.getStatus() + " work order");
        }
        e.setTechnicianId(technicianId);
        e.setStatus(WorkOrderStatus.ASSIGNED);
        return mapper.toResponse(repository.save(e));
    }

    @Override
    public WorkOrderResponse start(UUID id) {
        WorkOrder e = find(id);
        if (e.getStatus() != WorkOrderStatus.ASSIGNED) {
            throw new IllegalStateException("Work order must be ASSIGNED before it can be started");
        }
        e.setStatus(WorkOrderStatus.IN_PROGRESS);
        return mapper.toResponse(repository.save(e));
    }

    @Override
    public WorkOrderResponse complete(UUID id) {
        WorkOrder e = find(id);
        if (e.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Work order must be IN_PROGRESS before it can be completed");
        }
        e.setStatus(WorkOrderStatus.COMPLETED);
        e.setCompletedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(e));
    }

    private WorkOrder find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Work order not found"));
    }
}
