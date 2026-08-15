package com.reoagms.monitoring_service.service;

import com.reoagms.monitoring_service.dto.AlertRuleRequest;
import com.reoagms.monitoring_service.dto.AlertRuleResponse;

import java.util.List;
import java.util.UUID;

public interface AlertRuleService {
    AlertRuleResponse create(AlertRuleRequest request);

    List<AlertRuleResponse> getAll();

    AlertRuleResponse getById(UUID id);

    AlertRuleResponse update(UUID id, AlertRuleRequest request);

    void delete(UUID id);
}
