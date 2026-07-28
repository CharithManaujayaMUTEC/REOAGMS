package com.reoagms.asset_service.asset.mapper;

import com.reoagms.asset_service.asset.dto.AssetRequest;
import com.reoagms.asset_service.asset.dto.AssetResponse;
import com.reoagms.asset_service.asset.model.Asset;
import com.reoagms.asset_service.facility.model.Facility;
import org.springframework.stereotype.Component;

@Component
public class AssetMapper {

    public Asset toEntity(AssetRequest request, Facility facility) {

        return Asset.builder()
                .name(request.getName())
                .type(request.getType())
                .status(request.getStatus())
                .manufacturer(request.getManufacturer())
                .model(request.getModel())
                .serialNumber(request.getSerialNumber())
                .installationDate(request.getInstallationDate())
                .ratedCapacity(request.getRatedCapacity())
                .facility(facility)
                .build();

    }

    public AssetResponse toResponse(Asset asset) {

        return AssetResponse.builder()
                .id(asset.getId())
                .name(asset.getName())
                .type(asset.getType())
                .status(asset.getStatus())
                .manufacturer(asset.getManufacturer())
                .model(asset.getModel())
                .serialNumber(asset.getSerialNumber())
                .installationDate(asset.getInstallationDate())
                .ratedCapacity(asset.getRatedCapacity())
                .facilityId(asset.getFacility().getId())
                .build();

    }

}