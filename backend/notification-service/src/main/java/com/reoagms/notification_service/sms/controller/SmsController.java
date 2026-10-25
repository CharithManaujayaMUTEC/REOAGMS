package com.reoagms.notification_service.sms.controller;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.notification.dto.NotificationRequest;
import com.reoagms.notification_service.notification.dto.NotificationResponse;
import com.reoagms.notification_service.notification.service.NotificationService;
import com.reoagms.notification_service.sms.dto.SmsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
public class SmsController {

    private final NotificationService notificationService;

    @PostMapping
    public NotificationResponse send(
            @Valid
            @RequestBody
            SmsRequest request) {

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setRecipient(request.getPhoneNumber());
        notificationRequest.setChannel(NotificationChannel.SMS);
        notificationRequest.setMessage(request.getMessage());
        notificationRequest.setRelatedEntityId(request.getRelatedEntityId());

        return notificationService.create(notificationRequest);

    }

}
