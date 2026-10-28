package com.reoagms.notification_service.sms.mapper;

import com.reoagms.notification_service.sms.dto.SMSNotificationRequest;
import com.reoagms.notification_service.sms.dto.SMSNotificationResponse;
import com.reoagms.notification_service.sms.model.SMSNotification;
import org.springframework.stereotype.Component;

@Component
public class SMSNotificationMapper {

    public SMSNotification toEntity(SMSNotificationRequest request) {

        return SMSNotification.builder()
                .phoneNumber(request.getPhoneNumber())
                .message(request.getMessage())
                .build();

    }

    public SMSNotificationResponse toResponse(SMSNotification sms) {

        return SMSNotificationResponse.builder()
                .id(sms.getId())
                .phoneNumber(sms.getPhoneNumber())
                .message(sms.getMessage())
                .status(sms.getStatus())
                .retryCount(sms.getRetryCount())
                .build();

    }

}
