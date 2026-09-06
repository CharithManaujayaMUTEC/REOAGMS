package com.reoagms.maintenance_service.service;

import com.reoagms.maintenance_service.common.enums.TaskStatus;
import com.reoagms.maintenance_service.dto.MaintenanceTaskRequest;
import com.reoagms.maintenance_service.dto.MaintenanceTaskResponse;
import com.reoagms.maintenance_service.mapper.MaintenanceTaskMapper;
import com.reoagms.maintenance_service.model.MaintenanceTask;
import com.reoagms.maintenance_service.repository.MaintenanceTaskRepository;
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
public class MaintenanceTaskServiceImpl implements MaintenanceTaskService {

    private final MaintenanceTaskRepository repository;
    private final MaintenanceTaskMapper mapper;

    @Override
    public MaintenanceTaskResponse create(MaintenanceTaskRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceTaskResponse> getAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceTaskResponse> getByWorkOrder(UUID workOrderId) {
        return repository.findByWorkOrderId(workOrderId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceTaskResponse getById(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    public MaintenanceTaskResponse update(UUID id, MaintenanceTaskRequest request) {
        MaintenanceTask e = find(id);
        e.setWorkOrderId(request.getWorkOrderId());
        e.setTitle(request.getTitle());
        e.setDescription(request.getDescription());
        e.setTechnicianId(request.getTechnicianId());
        return mapper.toResponse(repository.save(e));
    }

    @Override
    public void delete(UUID id) {
        repository.delete(find(id));
    }

    @Override
    public MaintenanceTaskResponse start(UUID id) {
        MaintenanceTask e = find(id);
        e.setStatus(TaskStatus.IN_PROGRESS);
        e.setStartedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(e));
    }

    @Override
    public MaintenanceTaskResponse complete(UUID id) {
        MaintenanceTask e = find(id);
        e.setStatus(TaskStatus.COMPLETED);
        e.setCompletedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(e));
    }

    private MaintenanceTask find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance task not found"));
    }
}
