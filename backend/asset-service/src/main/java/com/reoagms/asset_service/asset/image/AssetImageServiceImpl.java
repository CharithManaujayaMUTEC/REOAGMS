package com.reoagms.asset_service.asset.image;

import com.reoagms.asset_service.asset.dto.AssetImageResponse;
import com.reoagms.asset_service.asset.model.Asset;
import com.reoagms.asset_service.asset.model.AssetImage;
import com.reoagms.asset_service.asset.repository.AssetImageRepository;
import com.reoagms.asset_service.asset.repository.AssetRepository;
import com.reoagms.asset_service.common.exception.ResourceNotFoundException;
import com.reoagms.asset_service.util.AssetImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetImageServiceImpl implements AssetImageService {

    private final AssetImageRepository imageRepository;
    private final AssetRepository assetRepository;
    private final AssetImageStorage storage;

    @Override
    @Transactional
    public AssetImageResponse upload(UUID assetId, MultipartFile file, boolean primary) {

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        String storedFileName = storage.store(file);

        boolean makePrimary = primary || !imageRepository.existsByAssetIdAndPrimaryTrue(assetId);

        if (makePrimary) {
            imageRepository.findByAssetId(assetId)
                    .forEach(img -> {
                        if (img.isPrimary()) {
                            img.setPrimary(false);
                            imageRepository.save(img);
                        }
                    });
        }

        AssetImage image = AssetImage.builder()
                .asset(asset)
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedFileName)
                .contentType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .primary(makePrimary)
                .build();

        imageRepository.save(image);

        return toResponse(image);
    }

    @Override
    public List<AssetImageResponse> getByAsset(UUID assetId) {

        return imageRepository.findByAssetId(assetId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AssetImageResponse getMetadata(UUID imageId) {

        return toResponse(findImage(imageId));
    }

    @Override
    public byte[] getFileBytes(UUID imageId) {

        AssetImage image = findImage(imageId);

        return storage.load(image.getStoredFileName());
    }

    @Override
    public String getContentType(UUID imageId) {

        return findImage(imageId).getContentType();
    }

    @Override
    @Transactional
    public AssetImageResponse setPrimary(UUID imageId) {

        AssetImage image = findImage(imageId);

        imageRepository.findByAssetId(image.getAsset().getId())
                .forEach(img -> {
                    boolean shouldBePrimary = img.getId().equals(imageId);
                    if (img.isPrimary() != shouldBePrimary) {
                        img.setPrimary(shouldBePrimary);
                        imageRepository.save(img);
                    }
                });

        return toResponse(findImage(imageId));
    }

    @Override
    @Transactional
    public void delete(UUID imageId) {

        AssetImage image = findImage(imageId);

        storage.delete(image.getStoredFileName());

        imageRepository.delete(image);

    }

    private AssetImage findImage(UUID imageId) {

        return imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset image not found"));

    }

    private AssetImageResponse toResponse(AssetImage image) {

        return AssetImageResponse.builder()
                .id(image.getId())
                .assetId(image.getAsset().getId())
                .originalFileName(image.getOriginalFileName())
                .contentType(image.getContentType())
                .fileSizeBytes(image.getFileSizeBytes())
                .primary(image.isPrimary())
                .url("/api/v1/assets/images/" + image.getId() + "/file")
                .uploadedAt(image.getCreatedAt())
                .build();

    }

}
