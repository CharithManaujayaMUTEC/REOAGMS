package com.reoagms.notification_service.email.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailTemplateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String subject;

    @NotBlank
    private String body;

}
