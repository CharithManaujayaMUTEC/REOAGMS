package com.reoagms.notification_service.sms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SMSNotificationRequest {

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String message;

}
