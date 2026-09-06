package com.reoagms.asset_service.util;

import com.reoagms.asset_service.asset.model.Asset;
import com.reoagms.asset_service.common.enums.AssetStatus;
import com.reoagms.asset_service.common.enums.AssetType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builds a dynamic Specification<Asset> from optional search criteria.
 * Any parameter left null is simply skipped, so callers can supply as few
 * or as many filters as they need.
 */
public final class AssetSpecifications {

    private AssetSpecifications() {
    }

    public static Specification<Asset> search(
            String name,
            AssetType type,
            AssetStatus status,
            String manufacturer,
            UUID facilityId) {

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

            if (manufacturer != null && !manufacturer.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("manufacturer")), "%" + manufacturer.toLowerCase() + "%"));
            }

            if (facilityId != null) {
                predicates.add(cb.equal(root.get("facility").get("id"), facilityId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));

        };

    }

}
