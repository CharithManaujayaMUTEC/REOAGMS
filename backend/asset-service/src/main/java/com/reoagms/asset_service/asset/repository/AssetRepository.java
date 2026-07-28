package com.reoagms.asset_service.asset.repository;

import com.reoagms.asset_service.asset.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {

    List<Asset> findByFacilityId(UUID facilityId);

}