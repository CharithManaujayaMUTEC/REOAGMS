package com.reoagms.asset_service.asset.service;

import com.reoagms.asset_service.asset.dto.AssetRequest;
import com.reoagms.asset_service.asset.dto.AssetResponse;
import com.reoagms.asset_service.asset.mapper.AssetMapper;
import com.reoagms.asset_service.asset.model.Asset;
import com.reoagms.asset_service.asset.repository.AssetRepository;
import com.reoagms.asset_service.facility.model.Facility;
import com.reoagms.asset_service.facility.repository.FacilityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final FacilityRepository facilityRepository;
    private final AssetMapper mapper;

    @Override
    public AssetResponse create(AssetRequest request) {

        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new EntityNotFoundException("Facility not found"));

        Asset asset = mapper.toEntity(request, facility);

        assetRepository.save(asset);

        return mapper.toResponse(asset);
    }

    @Override
    public AssetResponse getById(UUID id) {

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found"));

        return mapper.toResponse(asset);
    }

    @Override
    public List<AssetResponse> getAll() {

        return assetRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<AssetResponse> getByFacility(UUID facilityId) {

        return assetRepository.findByFacilityId(facilityId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public AssetResponse update(UUID id, AssetRequest request) {

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found"));

        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new EntityNotFoundException("Facility not found"));

        asset.setName(request.getName());
        asset.setType(request.getType());
        asset.setStatus(request.getStatus());
        asset.setManufacturer(request.getManufacturer());
        asset.setModel(request.getModel());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setInstallationDate(request.getInstallationDate());
        asset.setRatedCapacity(request.getRatedCapacity());
        asset.setFacility(facility);

        assetRepository.save(asset);

        return mapper.toResponse(asset);
    }

    @Override
    public void delete(UUID id) {

        assetRepository.deleteById(id);

    }

}