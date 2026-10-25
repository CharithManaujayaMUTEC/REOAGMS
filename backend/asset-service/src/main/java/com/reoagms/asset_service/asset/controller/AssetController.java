package com.reoagms.asset_service.asset.controller;

import com.reoagms.asset_service.asset.dto.AssetRequest;
import com.reoagms.asset_service.asset.dto.AssetResponse;
import com.reoagms.asset_service.asset.service.AssetService;
import com.reoagms.asset_service.common.enums.AssetStatus;
import com.reoagms.asset_service.common.enums.AssetType;
import com.reoagms.asset_service.common.model.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    public ResponseEntity<AssetResponse> create(@Valid @RequestBody AssetRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.create(request));

    }

    /**
     * Paginated + sortable listing, e.g. GET /api/v1/assets?page=0&size=20&sort=name,asc
     */
    @GetMapping
    public PageResponse<AssetResponse> getAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        return PageResponse.from(assetService.getAll(pageable));

    }

    /**
     * Dynamic search/filter across name, type, status, manufacturer and facility,
     * with the same pagination support as the base listing endpoint.
     * e.g. GET /api/v1/assets/search?name=inverter&status=ACTIVE&page=0&size=10
     */
    @GetMapping("/search")
    public PageResponse<AssetResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) AssetType type,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) UUID facilityId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        return PageResponse.from(
                assetService.search(name, type, status, manufacturer, facilityId, pageable));

    }

    @GetMapping("/{id}")
    public AssetResponse getById(@PathVariable UUID id) {

        return assetService.getById(id);

    }

    @GetMapping("/facility/{facilityId}")
    public List<AssetResponse> getByFacility(@PathVariable UUID facilityId) {

        return assetService.getByFacility(facilityId);

    }

    @GetMapping("/facility/{facilityId}/page")
    public PageResponse<AssetResponse> getByFacilityPaged(
            @PathVariable UUID facilityId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        return PageResponse.from(assetService.getByFacility(facilityId, pageable));

    }

    @PutMapping("/{id}")
    public AssetResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody AssetRequest request) {

        return assetService.update(id, request);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        assetService.delete(id);

        return ResponseEntity.noContent().build();

    }

}
