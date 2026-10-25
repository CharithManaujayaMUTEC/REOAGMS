package com.reoagms.notification_service.email.service;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.common.exception.NotificationDispatchException;
import com.reoagms.notification_service.notification.model.Notification;
import com.reoagms.notification_service.notification.service.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailDispatcher implements NotificationDispatcher {

    private final JavaMailSender mailSender;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void dispatch(Notification notification) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notification.getRecipient());
        message.setSubject(notification.getSubject());
        message.setText(notification.getMessage());

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            throw new NotificationDispatchException("Failed to send email to " + notification.getRecipient(), ex);
        }

    }

}
