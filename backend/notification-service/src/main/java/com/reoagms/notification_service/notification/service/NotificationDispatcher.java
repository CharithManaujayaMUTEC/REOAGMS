package com.reoagms.notification_service.notification.service;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.notification.model.Notification;

public interface NotificationDispatcher {

    NotificationChannel channel();

    void dispatch(Notification notification);

}
