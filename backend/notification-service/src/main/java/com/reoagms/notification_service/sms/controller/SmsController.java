package com.reoagms.notification_service.sms.controller;

import com.reoagms.notification_service.sms.dto.SMSNotificationRequest;
import com.reoagms.notification_service.sms.dto.SMSNotificationResponse;
import com.reoagms.notification_service.sms.service.SMSNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SMSNotificationService smsNotificationService;

    @PostMapping
    public SMSNotificationResponse send(@Valid @RequestBody SMSNotificationRequest request) {
        return smsNotificationService.send(request);
    }

    @GetMapping("/{id}")
    public SMSNotificationResponse getById(@PathVariable UUID id) {
        return smsNotificationService.getById(id);
    }

    @GetMapping
    public List<SMSNotificationResponse> getAll() {
        return smsNotificationService.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        smsNotificationService.delete(id);
    }

}
