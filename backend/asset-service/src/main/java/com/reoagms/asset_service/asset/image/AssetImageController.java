package com.reoagms.asset_service.asset.image;

import com.reoagms.asset_service.asset.dto.AssetImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AssetImageController {

    private final AssetImageService assetImageService;

    @PostMapping(value = "/api/v1/assets/{assetId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssetImageResponse> upload(
            @PathVariable UUID assetId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean primary) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assetImageService.upload(assetId, file, primary));

    }

    @GetMapping("/api/v1/assets/{assetId}/images")
    public List<AssetImageResponse> getByAsset(@PathVariable UUID assetId) {

        return assetImageService.getByAsset(assetId);

    }

    @GetMapping("/api/v1/assets/images/{imageId}")
    public AssetImageResponse getMetadata(@PathVariable UUID imageId) {

        return assetImageService.getMetadata(imageId);

    }

    @GetMapping("/api/v1/assets/images/{imageId}/file")
    public ResponseEntity<byte[]> getFile(@PathVariable UUID imageId) {

        byte[] bytes = assetImageService.getFileBytes(imageId);
        String contentType = assetImageService.getContentType(imageId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);

    }

    @PutMapping("/api/v1/assets/images/{imageId}/primary")
    public AssetImageResponse setPrimary(@PathVariable UUID imageId) {

        return assetImageService.setPrimary(imageId);

    }

    @DeleteMapping("/api/v1/assets/images/{imageId}")
    public ResponseEntity<Void> delete(@PathVariable UUID imageId) {

        assetImageService.delete(imageId);

        return ResponseEntity.noContent().build();

    }

}
