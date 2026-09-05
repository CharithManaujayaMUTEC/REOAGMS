package com.reoagms.asset_service.asset.model;

import com.reoagms.asset_service.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "asset_images")
public class AssetImage extends BaseEntity {

    @Column(nullable = false)
    private String originalFileName;

    /**
     * Randomly generated name the file is actually stored under on disk,
     * to avoid collisions and path traversal issues.
     */
    @Column(nullable = false, unique = true)
    private String storedFileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long fileSizeBytes;

    @Builder.Default
    private boolean primary = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

}
