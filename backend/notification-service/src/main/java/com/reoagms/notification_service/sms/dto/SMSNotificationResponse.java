package com.reoagms.notification_service.sms.dto;

import com.reoagms.notification_service.common.enums.NotificationStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SMSNotificationResponse {

    private UUID id;
    private String phoneNumber;
    private String message;
    private NotificationStatus status;
    private Integer retryCount;

}
