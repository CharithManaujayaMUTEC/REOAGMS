package com.reoagms.notification_service.notification.repository;

import com.reoagms.notification_service.common.enums.NotificationStatus;
import com.reoagms.notification_service.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByStatus(NotificationStatus status);

}
