package com.reoagms.maintenance_service.dto;

import com.reoagms.maintenance_service.common.enums.TechnicianAvailability;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TechnicianResponse {

    private UUID id;
    private String name;
    private String specialization;
    private String phone;
    private String email;
    private TechnicianAvailability availability;
}
