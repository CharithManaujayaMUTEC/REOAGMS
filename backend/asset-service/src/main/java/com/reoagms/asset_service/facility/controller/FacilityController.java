package com.reoagms.asset_service.facility.controller;

import com.reoagms.asset_service.facility.dto.FacilityRequest;
import com.reoagms.asset_service.facility.dto.FacilityResponse;
import com.reoagms.asset_service.facility.service.FacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @PostMapping
    public FacilityResponse create(
            @Valid
            @RequestBody
            FacilityRequest request) {

        return facilityService.create(request);

    }

    @GetMapping("/{id}")
    public FacilityResponse getById(
            @PathVariable UUID id) {

        return facilityService.getById(id);

    }

    @GetMapping
    public List<FacilityResponse> getAll() {

        return facilityService.getAll();

    }

    @PutMapping("/{id}")
    public FacilityResponse update(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            FacilityRequest request) {

        return facilityService.update(id, request);

    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id) {

        facilityService.delete(id);

    }

}