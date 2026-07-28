package com.reoagms.asset_service.asset.controller;

import com.reoagms.asset_service.asset.dto.AssetRequest;
import com.reoagms.asset_service.asset.dto.AssetResponse;
import com.reoagms.asset_service.asset.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    public AssetResponse create(@Valid @RequestBody AssetRequest request) {

        return assetService.create(request);

    }

    @GetMapping
    public List<AssetResponse> getAll() {

        return assetService.getAll();

    }

    @GetMapping("/{id}")
    public AssetResponse getById(@PathVariable UUID id) {

        return assetService.getById(id);

    }

    @GetMapping("/facility/{facilityId}")
    public List<AssetResponse> getByFacility(@PathVariable UUID facilityId) {

        return assetService.getByFacility(facilityId);

    }

    @PutMapping("/{id}")
    public AssetResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody AssetRequest request) {

        return assetService.update(id, request);

    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {

        assetService.delete(id);

    }

}