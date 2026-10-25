package com.reoagms.notification_service.sms.repository;

import com.reoagms.notification_service.sms.model.SmsNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SmsNotificationRepository extends JpaRepository<SmsNotification, UUID> {
}
