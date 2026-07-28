package com.reoagms.asset_service.asset.model;

import com.reoagms.asset_service.common.enums.AssetStatus;
import com.reoagms.asset_service.common.enums.AssetType;
import com.reoagms.asset_service.common.model.BaseEntity;
import com.reoagms.asset_service.facility.model.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="assets")
public class Asset extends BaseEntity {

    @Column(nullable=false)
    private String name;

    @Enumerated(EnumType.STRING)
    private AssetType type;

    @Enumerated(EnumType.STRING)
    private AssetStatus status;

    private String manufacturer;

    private String model;

    private String serialNumber;

    private LocalDate installationDate;

    private Double ratedCapacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="facility_id")
    private Facility facility;

}