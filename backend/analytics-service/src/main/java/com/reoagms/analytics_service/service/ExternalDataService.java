package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.dto.AlertData;
import com.reoagms.analytics_service.dto.AssetData;
import com.reoagms.analytics_service.dto.SensorReadingData;
import com.reoagms.analytics_service.dto.WorkOrderData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ExternalDataService {
    List<SensorReadingData> getReadings(UUID facilityId, UUID assetId, LocalDateTime from, LocalDateTime to);
    List<AlertData> getAlerts(UUID facilityId, UUID assetId, LocalDateTime from, LocalDateTime to);
    List<AssetData> getAssets(UUID facilityId, UUID assetId);
    List<WorkOrderData> getWorkOrders(UUID facilityId, UUID assetId, LocalDateTime from, LocalDateTime to);
}
