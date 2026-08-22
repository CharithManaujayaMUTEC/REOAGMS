package com.reoagms.monitoring_service.service;

import com.reoagms.monitoring_service.common.enums.AlertStatus;
import com.reoagms.monitoring_service.common.exception.InvalidRequestException;
import com.reoagms.monitoring_service.common.exception.ResourceNotFoundException;
import com.reoagms.monitoring_service.dto.AlertRequest;
import com.reoagms.monitoring_service.dto.AlertResponse;
import com.reoagms.monitoring_service.mapper.AlertMapper;
import com.reoagms.monitoring_service.model.Alert;
import com.reoagms.monitoring_service.model.AlertRule;
import com.reoagms.monitoring_service.model.SensorReading;
import com.reoagms.monitoring_service.repository.AlertRepository;
import com.reoagms.monitoring_service.repository.AlertRuleRepository;
import com.reoagms.monitoring_service.util.ConditionEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final AlertMapper mapper;

    @Override
    public AlertResponse create(AlertRequest request) {
        Alert saved = alertRepository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertResponse> getAll(UUID facilityId, UUID assetId, AlertStatus status,
                                       LocalDateTime from, LocalDateTime to) {
        Specification<Alert> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (facilityId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("facilityId"), facilityId));
            }
            if (assetId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("assetId"), assetId));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (from != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("timestamp"), from));
            }
            if (to != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("timestamp"), to));
            }
            return predicate;
        };
        return alertRepository.findAll(spec).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertResponse getById(UUID id) {
        return mapper.toResponse(findEntity(id));
    }

    @Override
    public AlertResponse acknowledge(UUID id) {
        Alert entity = findEntity(id);
        if (entity.getStatus() == AlertStatus.RESOLVED) {
            throw new InvalidRequestException("Cannot acknowledge an alert that is already resolved");
        }
        entity.setStatus(AlertStatus.ACKNOWLEDGED);
        return mapper.toResponse(alertRepository.save(entity));
    }

    @Override
    public AlertResponse resolve(UUID id) {
        Alert entity = findEntity(id);
        entity.setStatus(AlertStatus.RESOLVED);
        return mapper.toResponse(alertRepository.save(entity));
    }

    @Override
    public List<AlertResponse> evaluateReading(SensorReading reading) {
        List<AlertRule> assetRules = reading.getAssetId() != null
                ? alertRuleRepository.findByEnabledTrueAndMetricTypeAndAssetId(
                        reading.getMetricType(), reading.getAssetId())
                : List.of();
        List<AlertRule> globalRules =
                alertRuleRepository.findByEnabledTrueAndMetricTypeAndAssetIdIsNull(reading.getMetricType());

        List<AlertResponse> generated = new ArrayList<>();
        for (AlertRule rule : Stream.concat(assetRules.stream(), globalRules.stream()).toList()) {
            if (ConditionEvaluator.isBreached(rule.getOperator(), reading.getValue(), rule.getThreshold())) {
                Alert alert = Alert.builder()
                        .facilityId(reading.getFacilityId())
                        .assetId(reading.getAssetId())
                        .sensorReadingId(reading.getId())
                        .alertRuleId(rule.getId())
                        .metricType(reading.getMetricType())
                        .value(reading.getValue())
                        .threshold(rule.getThreshold())
                        .severity(rule.getSeverity())
                        .status(AlertStatus.OPEN)
                        .message(buildMessage(rule, reading))
                        .timestamp(reading.getTimestamp())
                        .build();
                generated.add(mapper.toResponse(alertRepository.save(alert)));
            }
        }
        return generated;
    }

    private String buildMessage(AlertRule rule, SensorReading reading) {
        return "%s reading %s %s breached rule '%s' (%s %s)".formatted(
                reading.getMetricType(), reading.getValue(), reading.getUnit(),
                rule.getName(), rule.getOperator(), rule.getThreshold());
    }

    private Alert findEntity(UUID id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + id));
    }
}
