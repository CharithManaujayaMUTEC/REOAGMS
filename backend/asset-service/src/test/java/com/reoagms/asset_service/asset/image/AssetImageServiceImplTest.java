package com.reoagms.asset_service.asset.image;

import com.reoagms.asset_service.asset.dto.AssetImageResponse;
import com.reoagms.asset_service.asset.model.Asset;
import com.reoagms.asset_service.asset.model.AssetImage;
import com.reoagms.asset_service.asset.repository.AssetImageRepository;
import com.reoagms.asset_service.asset.repository.AssetRepository;
import com.reoagms.asset_service.common.exception.ResourceNotFoundException;
import com.reoagms.asset_service.util.AssetImageStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetImageServiceImplTest {

    @Mock
    private AssetImageRepository imageRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetImageStorage storage;

    private AssetImageServiceImpl imageService;

    @BeforeEach
    void setUp() {
        imageService = new AssetImageServiceImpl(imageRepository, assetRepository, storage);
    }

    private Asset asset(UUID id) {
        Asset asset = new Asset();
        asset.setId(id);
        asset.setName("Inverter A1");
        return asset;
    }

    @Test
    void uploadThrowsWhenAssetMissing() {
        UUID assetId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile("file", "panel.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(assetRepository.findById(assetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> imageService.upload(assetId, file, false))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(storage, never()).store(any());
    }

    @Test
    void firstUploadedImageBecomesPrimaryAutomatically() {
        UUID assetId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile("file", "panel.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset(assetId)));
        when(imageRepository.existsByAssetIdAndPrimaryTrue(assetId)).thenReturn(false);
        when(imageRepository.findByAssetId(assetId)).thenReturn(List.of());
        when(storage.store(file)).thenReturn("generated-name.jpg");
        when(imageRepository.save(any(AssetImage.class))).thenAnswer(inv -> inv.getArgument(0));

        AssetImageResponse response = imageService.upload(assetId, file, false);

        assertThat(response.isPrimary()).isTrue();
        assertThat(response.getUrl()).contains("/api/v1/assets/images/");
    }

    @Test
    void secondUploadIsNotPrimaryUnlessRequested() {
        UUID assetId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile("file", "panel2.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset(assetId)));
        when(imageRepository.existsByAssetIdAndPrimaryTrue(assetId)).thenReturn(true);
        when(storage.store(file)).thenReturn("generated-name-2.jpg");
        when(imageRepository.save(any(AssetImage.class))).thenAnswer(inv -> inv.getArgument(0));

        AssetImageResponse response = imageService.upload(assetId, file, false);

        assertThat(response.isPrimary()).isFalse();
    }

    @Test
    void deleteRemovesFileAndRecord() {
        UUID imageId = UUID.randomUUID();
        AssetImage image = AssetImage.builder()
                .asset(asset(UUID.randomUUID()))
                .storedFileName("stored.jpg")
                .originalFileName("original.jpg")
                .contentType("image/jpeg")
                .fileSizeBytes(100)
                .build();

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));

        imageService.delete(imageId);

        verify(storage).delete("stored.jpg");
        verify(imageRepository).delete(image);
    }

    @Test
    void deleteThrowsWhenImageMissing() {
        UUID imageId = UUID.randomUUID();
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> imageService.delete(imageId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(storage, never()).delete(any());
    }

}
