package com.reoagms.asset_service.sensor.mapper;

import com.reoagms.asset_service.asset.model.Asset;
import com.reoagms.asset_service.sensor.dto.SensorRequest;
import com.reoagms.asset_service.sensor.dto.SensorResponse;
import com.reoagms.asset_service.sensor.model.Sensor;
import org.springframework.stereotype.Component;

@Component
public class SensorMapper {

    public Sensor toEntity(SensorRequest request, Asset asset) {

        return Sensor.builder()
                .name(request.getName())
                .type(request.getType())
                .status(request.getStatus())
                .unit(request.getUnit())
                .manufacturer(request.getManufacturer())
                .serialNumber(request.getSerialNumber())
                .installationDate(request.getInstallationDate())
                .minThreshold(request.getMinThreshold())
                .maxThreshold(request.getMaxThreshold())
                .asset(asset)
                .build();

    }

    public SensorResponse toResponse(Sensor sensor) {

        return SensorResponse.builder()
                .id(sensor.getId())
                .name(sensor.getName())
                .type(sensor.getType())
                .status(sensor.getStatus())
                .unit(sensor.getUnit())
                .manufacturer(sensor.getManufacturer())
                .serialNumber(sensor.getSerialNumber())
                .installationDate(sensor.getInstallationDate())
                .minThreshold(sensor.getMinThreshold())
                .maxThreshold(sensor.getMaxThreshold())
                .assetId(sensor.getAsset().getId())
                .build();

    }

}
