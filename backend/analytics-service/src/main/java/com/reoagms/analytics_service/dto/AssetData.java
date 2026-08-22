package com.reoagms.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AssetData {
    private UUID id;
    private UUID facilityId;
    private String name;
    private String type;
    private String status;
    private BigDecimal ratedCapacity;
}
