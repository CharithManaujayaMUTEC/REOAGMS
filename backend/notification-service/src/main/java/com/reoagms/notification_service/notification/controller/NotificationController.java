package com.reoagms.notification_service.notification.controller;

import com.reoagms.notification_service.notification.dto.NotificationRequest;
import com.reoagms.notification_service.notification.dto.NotificationResponse;
import com.reoagms.notification_service.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public NotificationResponse create(
            @Valid
            @RequestBody
            NotificationRequest request) {

        return notificationService.create(request);

    }

    @GetMapping("/{id}")
    public NotificationResponse getById(
            @PathVariable UUID id) {

        return notificationService.getById(id);

    }

    @GetMapping
    public List<NotificationResponse> getAll() {

        return notificationService.getAll();

    }

    @PutMapping("/{id}")
    public NotificationResponse update(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            NotificationRequest request) {

        return notificationService.update(id, request);

    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id) {

        notificationService.delete(id);

    }

    @PostMapping("/{id}/retry")
    public NotificationResponse retry(
            @PathVariable UUID id) {

        return notificationService.retry(id);

    }

}
