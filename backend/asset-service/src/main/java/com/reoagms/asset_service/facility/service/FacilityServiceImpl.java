package com.reoagms.asset_service.facility.service;

import com.reoagms.asset_service.facility.dto.FacilityRequest;
import com.reoagms.asset_service.facility.dto.FacilityResponse;
import com.reoagms.asset_service.facility.mapper.FacilityMapper;
import com.reoagms.asset_service.facility.model.Facility;
import com.reoagms.asset_service.facility.repository.FacilityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository repository;
    private final FacilityMapper mapper;

    @Override
    public FacilityResponse create(FacilityRequest request) {

        Facility facility = mapper.toEntity(request);

        repository.save(facility);

        return mapper.toResponse(facility);

    }

    @Override
    public FacilityResponse getById(UUID id) {

        Facility facility = repository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Facility not found"));

        return mapper.toResponse(facility);

    }

    @Override
    public List<FacilityResponse> getAll() {

        return repository.findAll()

                .stream()

                .map(mapper::toResponse)

                .toList();

    }

    @Override
    public FacilityResponse update(UUID id,
                                   FacilityRequest request) {

        Facility facility = repository.findById(id)

                .orElseThrow(() ->
                        new EntityNotFoundException("Facility not found"));

        facility.setName(request.getName());
        facility.setType(request.getType());
        facility.setCountry(request.getCountry());
        facility.setProvince(request.getProvince());
        facility.setLatitude(request.getLatitude());
        facility.setLongitude(request.getLongitude());
        facility.setCapacityMW(request.getCapacityMW());

        repository.save(facility);

        return mapper.toResponse(facility);

    }

    @Override
    public void delete(UUID id) {

        repository.deleteById(id);

    }

}