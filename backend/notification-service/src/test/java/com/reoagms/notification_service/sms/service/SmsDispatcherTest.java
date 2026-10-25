package com.reoagms.notification_service.sms.service;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.notification.model.Notification;
import com.reoagms.notification_service.sms.model.SmsNotification;
import com.reoagms.notification_service.sms.repository.SmsNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsDispatcherTest {

    @Mock
    private SmsNotificationRepository repository;

    @Captor
    private ArgumentCaptor<SmsNotification> captor;

    private SmsDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new SmsDispatcher(repository);
    }

    @Test
    void channelIsSms() {
        assertThat(dispatcher.channel()).isEqualTo(NotificationChannel.SMS);
    }

    @Test
    void dispatchPersistsSmsRecord() {
        Notification notification = Notification.builder()
                .recipient("+94771234567")
                .channel(NotificationChannel.SMS)
                .message("Threshold exceeded")
                .build();

        when(repository.save(any(SmsNotification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dispatcher.dispatch(notification);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPhoneNumber()).isEqualTo("+94771234567");
        assertThat(captor.getValue().getMessage()).isEqualTo("Threshold exceeded");
    }
}
