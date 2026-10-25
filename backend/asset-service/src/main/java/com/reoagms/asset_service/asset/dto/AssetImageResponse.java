package com.reoagms.asset_service.asset.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AssetImageResponse {

    private UUID id;

    private UUID assetId;

    private String originalFileName;

    private String contentType;

    private long fileSizeBytes;

    private boolean primary;

    /**
     * Relative URL the client can GET to fetch the raw image bytes.
     */
    private String url;

    private LocalDateTime uploadedAt;

}
