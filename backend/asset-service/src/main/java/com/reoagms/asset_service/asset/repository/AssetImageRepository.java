package com.reoagms.asset_service.asset.repository;

import com.reoagms.asset_service.asset.model.AssetImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetImageRepository extends JpaRepository<AssetImage, UUID> {

    List<AssetImage> findByAssetId(UUID assetId);

    Optional<AssetImage> findByStoredFileName(String storedFileName);

    boolean existsByAssetIdAndPrimaryTrue(UUID assetId);

}
