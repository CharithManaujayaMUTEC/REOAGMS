package com.reoagms.notification_service.notification.service;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.common.enums.NotificationStatus;
import com.reoagms.notification_service.common.exception.NotificationDispatchException;
import com.reoagms.notification_service.common.exception.ResourceNotFoundException;
import com.reoagms.notification_service.notification.dto.NotificationRequest;
import com.reoagms.notification_service.notification.dto.NotificationResponse;
import com.reoagms.notification_service.notification.mapper.NotificationMapper;
import com.reoagms.notification_service.notification.model.Notification;
import com.reoagms.notification_service.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private NotificationDispatcher emailDispatcher;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(emailDispatcher.channel()).thenReturn(NotificationChannel.EMAIL);
        service = new NotificationServiceImpl(repository, new NotificationMapper(), List.of(emailDispatcher));
    }

    private NotificationRequest emailRequest() {
        NotificationRequest request = new NotificationRequest();
        request.setRecipient("ops@reoagms.dev");
        request.setChannel(NotificationChannel.EMAIL);
        request.setSubject("Alert");
        request.setMessage("Threshold exceeded");
        return request;
    }

    @Test
    void createDispatchesAndMarksNotificationSent() {
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailDispatcher).dispatch(any(Notification.class));

        NotificationResponse response = service.create(emailRequest());

        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(response.getRecipient()).isEqualTo("ops@reoagms.dev");
        verify(emailDispatcher).dispatch(any(Notification.class));
    }

    @Test
    void createMarksNotificationFailedWhenDispatchThrows() {
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new NotificationDispatchException("SMTP down", new RuntimeException()))
                .when(emailDispatcher).dispatch(any(Notification.class));

        NotificationResponse response = service.create(emailRequest());

        assertThat(response.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(response.getRetryCount()).isEqualTo(1);
        assertThat(response.getFailureReason()).isEqualTo("SMTP down");
    }

    @Test
    void createMarksNotificationFailedWhenNoDispatcherForChannel() {
        NotificationRequest request = new NotificationRequest();
        request.setRecipient("+94771234567");
        request.setChannel(NotificationChannel.SMS);
        request.setMessage("No dispatcher registered");

        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = service.create(request);

        assertThat(response.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(response.getFailureReason()).contains("No dispatcher configured");
    }

    @Test
    void getByIdThrowsWhenNotificationMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Notification not found");
    }

    @Test
    void deleteDelegatesToRepository() {
        UUID id = UUID.randomUUID();

        service.delete(id);

        verify(repository).deleteById(id);
    }
}
