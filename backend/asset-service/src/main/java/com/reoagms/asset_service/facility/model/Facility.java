package com.reoagms.asset_service.facility.model;

import com.reoagms.asset_service.asset.model.Asset;
import com.reoagms.asset_service.common.enums.FacilityType;
import com.reoagms.asset_service.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "facilities")
public class Facility extends BaseEntity {

    @Column(nullable = false,length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    private FacilityType type;

    private String country;

    private String province;

    private Double latitude;

    private Double longitude;

    private Double capacityMW;

    @Builder.Default
    @OneToMany(
            mappedBy = "facility",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Asset> assets = new ArrayList<>();

}