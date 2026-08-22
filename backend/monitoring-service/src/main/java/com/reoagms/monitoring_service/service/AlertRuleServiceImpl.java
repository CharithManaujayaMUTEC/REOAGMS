package com.reoagms.monitoring_service.service;

import com.reoagms.monitoring_service.common.exception.ResourceNotFoundException;
import com.reoagms.monitoring_service.dto.AlertRuleRequest;
import com.reoagms.monitoring_service.dto.AlertRuleResponse;
import com.reoagms.monitoring_service.mapper.AlertRuleMapper;
import com.reoagms.monitoring_service.model.AlertRule;
import com.reoagms.monitoring_service.repository.AlertRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertRuleServiceImpl implements AlertRuleService {

    private final AlertRuleRepository repository;
    private final AlertRuleMapper mapper;

    @Override
    public AlertRuleResponse create(AlertRuleRequest request) {
        AlertRule saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertRuleResponse> getAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertRuleResponse getById(UUID id) {
        return mapper.toResponse(findEntity(id));
    }

    @Override
    public AlertRuleResponse update(UUID id, AlertRuleRequest request) {
        AlertRule entity = findEntity(id);
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Alert rule not found: " + id);
        }
        repository.deleteById(id);
    }

    private AlertRule findEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert rule not found: " + id));
    }
}
