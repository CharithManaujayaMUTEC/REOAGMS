package com.reoagms.notification_service.sms.repository;

import com.reoagms.notification_service.sms.model.SMSNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SMSNotificationRepository extends JpaRepository<SMSNotification, UUID> {
}
