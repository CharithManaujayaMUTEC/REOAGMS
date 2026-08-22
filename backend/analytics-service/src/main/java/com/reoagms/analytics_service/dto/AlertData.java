package com.reoagms.analytics_service.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AlertData {
    private UUID id;
    private UUID facilityId;
    private UUID assetId;
    private String severity;
    private String status;
    private LocalDateTime timestamp;
}
