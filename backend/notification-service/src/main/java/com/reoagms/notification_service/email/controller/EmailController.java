package com.reoagms.notification_service.email.controller;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.email.dto.EmailRequest;
import com.reoagms.notification_service.notification.dto.NotificationRequest;
import com.reoagms.notification_service.notification.dto.NotificationResponse;
import com.reoagms.notification_service.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final NotificationService notificationService;

    @PostMapping
    public NotificationResponse send(
            @Valid
            @RequestBody
            EmailRequest request) {

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setRecipient(request.getTo());
        notificationRequest.setChannel(NotificationChannel.EMAIL);
        notificationRequest.setSubject(request.getSubject());
        notificationRequest.setMessage(request.getBody());
        notificationRequest.setRelatedEntityId(request.getRelatedEntityId());

        return notificationService.create(notificationRequest);

    }

}
