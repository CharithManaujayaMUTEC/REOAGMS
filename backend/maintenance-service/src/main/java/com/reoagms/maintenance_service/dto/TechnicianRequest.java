package com.reoagms.maintenance_service.dto;

import com.reoagms.maintenance_service.common.enums.TechnicianAvailability;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TechnicianRequest {

    @NotBlank
    private String name;

    private String specialization;

    private String phone;

    @Email
    private String email;

    private TechnicianAvailability availability;
}
