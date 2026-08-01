package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.common.exception.ResourceNotFoundException;
import com.reoagms.analytics_service.dto.DashboardRequest;
import com.reoagms.analytics_service.dto.DashboardResponse;
import com.reoagms.analytics_service.mapper.DashboardMapper;
import com.reoagms.analytics_service.model.Dashboard;
import com.reoagms.analytics_service.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository repository;
    private final DashboardMapper mapper;

    @Override
    @Transactional
    public DashboardResponse create(DashboardRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Override
    public List<DashboardResponse> getAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public DashboardResponse getById(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional
    public DashboardResponse update(UUID id, DashboardRequest request) {
        Dashboard dashboard = find(id);
        mapper.update(dashboard, request);
        return mapper.toResponse(repository.save(dashboard));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.delete(find(id));
    }

    private Dashboard find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dashboard not found: " + id));
    }
}
