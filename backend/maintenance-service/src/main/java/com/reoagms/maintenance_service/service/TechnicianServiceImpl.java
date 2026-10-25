package com.reoagms.maintenance_service.service;

import com.reoagms.maintenance_service.dto.TechnicianRequest;
import com.reoagms.maintenance_service.dto.TechnicianResponse;
import com.reoagms.maintenance_service.mapper.TechnicianMapper;
import com.reoagms.maintenance_service.model.Technician;
import com.reoagms.maintenance_service.repository.TechnicianRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TechnicianServiceImpl implements TechnicianService {

    private final TechnicianRepository repository;
    private final TechnicianMapper mapper;

    @Override
    public TechnicianResponse create(TechnicianRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechnicianResponse> getAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TechnicianResponse getById(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    public TechnicianResponse update(UUID id, TechnicianRequest request) {
        Technician e = find(id);
        e.setName(request.getName());
        e.setSpecialization(request.getSpecialization());
        e.setPhone(request.getPhone());
        e.setEmail(request.getEmail());
        if (request.getAvailability() != null) {
            e.setAvailability(request.getAvailability());
        }
        return mapper.toResponse(repository.save(e));
    }

    @Override
    public void delete(UUID id) {
        repository.delete(find(id));
    }

    private Technician find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Technician not found"));
    }
}
