package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.common.exception.ResourceNotFoundException;
import com.reoagms.analytics_service.dto.KpiRequest;
import com.reoagms.analytics_service.dto.KpiResponse;
import com.reoagms.analytics_service.mapper.KpiMapper;
import com.reoagms.analytics_service.model.Kpi;
import com.reoagms.analytics_service.repository.KpiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiServiceImpl implements KpiService {

    private final KpiRepository repository;
    private final KpiMapper mapper;
    private final AnalyticsValidationService validationService;

    @Override
    @Transactional
    public KpiResponse create(KpiRequest request) {
        validate(request);
        Kpi kpi = mapper.toEntity(request);
        kpi.setCalculatedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(kpi));
    }

    @Override
    public List<KpiResponse> getAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public KpiResponse getById(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional
    public KpiResponse update(UUID id, KpiRequest request) {
        validate(request);
        Kpi kpi = find(id);
        mapper.update(kpi, request);
        kpi.setCalculatedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(kpi));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.delete(find(id));
    }

    private void validate(KpiRequest request) {
        validationService.validatePeriod(request.getPeriodStart(), request.getPeriodEnd());
        validationService.validateScope(request.getScopeType(), request.getScopeId());
    }

    private Kpi find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPI not found: " + id));
    }
}
