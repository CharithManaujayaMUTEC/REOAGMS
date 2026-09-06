package com.reoagms.maintenance_service.mapper;

import com.reoagms.maintenance_service.common.enums.TechnicianAvailability;
import com.reoagms.maintenance_service.dto.TechnicianRequest;
import com.reoagms.maintenance_service.dto.TechnicianResponse;
import com.reoagms.maintenance_service.model.Technician;
import org.springframework.stereotype.Component;

@Component
public class TechnicianMapper {

    public Technician toEntity(TechnicianRequest request) {
        return Technician.builder()
                .name(request.getName())
                .specialization(request.getSpecialization())
                .phone(request.getPhone())
                .email(request.getEmail())
                .availability(request.getAvailability() != null
                        ? request.getAvailability()
                        : TechnicianAvailability.AVAILABLE)
                .build();
    }

    public TechnicianResponse toResponse(Technician e) {
        return TechnicianResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .specialization(e.getSpecialization())
                .phone(e.getPhone())
                .email(e.getEmail())
                .availability(e.getAvailability())
                .build();
    }
}
