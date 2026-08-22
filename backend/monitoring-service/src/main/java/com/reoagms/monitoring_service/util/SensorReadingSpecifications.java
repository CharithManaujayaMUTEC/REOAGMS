package com.reoagms.monitoring_service.util;

import com.reoagms.monitoring_service.common.enums.MetricType;
import com.reoagms.monitoring_service.model.SensorReading;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public final class SensorReadingSpecifications {

    private SensorReadingSpecifications() {
    }

    public static Specification<SensorReading> filter(
            UUID facilityId, UUID assetId, UUID sensorId, MetricType metricType,
            LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (facilityId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("facilityId"), facilityId));
            }
            if (assetId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("assetId"), assetId));
            }
            if (sensorId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("sensorId"), sensorId));
            }
            if (metricType != null) {
                predicates = cb.and(predicates, cb.equal(root.get("metricType"), metricType));
            }
            if (from != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("timestamp"), from));
            }
            if (to != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("timestamp"), to));
            }
            return predicates;
        };
    }
}
