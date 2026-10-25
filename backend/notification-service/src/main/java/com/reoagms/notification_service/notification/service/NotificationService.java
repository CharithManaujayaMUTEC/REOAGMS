package com.reoagms.notification_service.notification.service;

import com.reoagms.notification_service.notification.dto.NotificationRequest;
import com.reoagms.notification_service.notification.dto.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponse create(NotificationRequest request);

    NotificationResponse getById(UUID id);

    List<NotificationResponse> getAll();

    NotificationResponse update(UUID id, NotificationRequest request);

    void delete(UUID id);

    NotificationResponse retry(UUID id);

}
