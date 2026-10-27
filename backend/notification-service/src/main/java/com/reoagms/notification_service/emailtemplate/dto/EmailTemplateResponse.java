package com.reoagms.notification_service.emailtemplate.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EmailTemplateResponse {

    private UUID id;
    private String name;
    private String subject;
    private String body;
    private String description;

}
