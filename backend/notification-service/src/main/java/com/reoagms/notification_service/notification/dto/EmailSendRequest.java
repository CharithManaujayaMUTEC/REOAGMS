package com.reoagms.notification_service.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class EmailSendRequest {

    @NotBlank
    @Email
    private String recipient;

    // Either send raw subject/body, or a templateName + placeholder values
    private String subject;

    private String body;

    private String templateName;

    private Map<String, String> placeholders;

}
