package com.reoagms.asset_service.asset.image;

import com.reoagms.asset_service.asset.dto.AssetImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AssetImageService {

    AssetImageResponse upload(UUID assetId, MultipartFile file, boolean primary);

    List<AssetImageResponse> getByAsset(UUID assetId);

    AssetImageResponse getMetadata(UUID imageId);

    byte[] getFileBytes(UUID imageId);

    String getContentType(UUID imageId);

    AssetImageResponse setPrimary(UUID imageId);

    void delete(UUID imageId);

}
