package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.common.exception.DownstreamServiceException;
import com.reoagms.analytics_service.dto.AlertData;
import com.reoagms.analytics_service.dto.AssetData;
import com.reoagms.analytics_service.dto.SensorReadingData;
import com.reoagms.analytics_service.dto.WorkOrderData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ExternalDataServiceImpl implements ExternalDataService {

    private final RestClient monitoringRestClient;
    private final RestClient assetRestClient;
    private final RestClient maintenanceRestClient;

    public ExternalDataServiceImpl(
            @Qualifier("monitoringRestClient") RestClient monitoringRestClient,
            @Qualifier("assetRestClient") RestClient assetRestClient,
            @Qualifier("maintenanceRestClient") RestClient maintenanceRestClient) {
        this.monitoringRestClient = monitoringRestClient;
        this.assetRestClient = assetRestClient;
        this.maintenanceRestClient = maintenanceRestClient;
    }

    @Override
    public List<SensorReadingData> getReadings(
            UUID facilityId, UUID assetId, LocalDateTime from, LocalDateTime to) {
        try {
            List<SensorReadingData> result = monitoringRestClient.get()
                    .uri(builder -> analyticsUri(builder, "/api/v1/sensor-readings", facilityId, assetId, from, to))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return result == null ? List.of() : result;
        } catch (RestClientException ex) {
            throw new DownstreamServiceException("Monitoring Service is unavailable or returned an invalid response", ex);
        }
    }

    @Override
    public List<AlertData> getAlerts(
            UUID facilityId, UUID assetId, LocalDateTime from, LocalDateTime to) {
        try {
            List<AlertData> result = monitoringRestClient.get()
                    .uri(builder -> analyticsUri(builder, "/api/v1/alerts", facilityId, assetId, from, to))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return result == null ? List.of() : result;
        } catch (RestClientException ex) {
            throw new DownstreamServiceException("Monitoring Service alerts endpoint is unavailable or invalid", ex);
        }
    }

    @Override
    public List<AssetData> getAssets(UUID facilityId, UUID assetId) {
        try {
            if (assetId != null) {
                AssetData asset = assetRestClient.get()
                        .uri("/api/v1/assets/{id}", assetId)
                        .retrieve()
                        .body(AssetData.class);
                return asset == null ? List.of() : List.of(asset);
            }

            String path = facilityId == null
                    ? "/api/v1/assets"
                    : "/api/v1/assets/facility/" + facilityId;
            List<AssetData> result = assetRestClient.get()
                    .uri(path)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return result == null ? List.of() : result;
        } catch (RestClientException ex) {
            throw new DownstreamServiceException("Asset Service is unavailable or returned an invalid response", ex);
        }
    }

    @Override
    public List<WorkOrderData> getWorkOrders(
            UUID facilityId, UUID assetId, LocalDateTime from, LocalDateTime to) {
        try {
            List<WorkOrderData> result = maintenanceRestClient.get()
                    .uri(builder -> analyticsUri(builder, "/api/v1/work-orders", facilityId, assetId, from, to))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return result == null ? List.of() : result;
        } catch (RestClientException ex) {
            throw new DownstreamServiceException("Maintenance Service is unavailable or returned an invalid response", ex);
        }
    }

    private java.net.URI analyticsUri(
            UriBuilder builder,
            String path,
            UUID facilityId,
            UUID assetId,
            LocalDateTime from,
            LocalDateTime to) {
        builder.path(path);
        if (facilityId != null) {
            builder.queryParam("facilityId", facilityId);
        }
        if (assetId != null) {
            builder.queryParam("assetId", assetId);
        }
        if (from != null) {
            builder.queryParam("from", from);
        }
        if (to != null) {
            builder.queryParam("to", to);
        }
        return builder.build();
    }
}
