package com.reoagms.notification_service.sms.service;

import com.reoagms.notification_service.sms.dto.SMSNotificationRequest;
import com.reoagms.notification_service.sms.dto.SMSNotificationResponse;

import java.util.List;
import java.util.UUID;

public interface SMSNotificationService {

    SMSNotificationResponse send(SMSNotificationRequest request);

    SMSNotificationResponse getById(UUID id);

    List<SMSNotificationResponse> getAll();

    void delete(UUID id);

}
