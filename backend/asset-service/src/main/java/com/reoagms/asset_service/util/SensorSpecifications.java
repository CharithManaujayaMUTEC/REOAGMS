package com.reoagms.asset_service.util;

import com.reoagms.asset_service.common.enums.SensorStatus;
import com.reoagms.asset_service.common.enums.SensorType;
import com.reoagms.asset_service.sensor.model.Sensor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SensorSpecifications {

    private SensorSpecifications() {
    }

    public static Specification<Sensor> search(
            String name,
            SensorType type,
            SensorStatus status,
            UUID assetId) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (assetId != null) {
                predicates.add(cb.equal(root.get("asset").get("id"), assetId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));

        };

    }

}
