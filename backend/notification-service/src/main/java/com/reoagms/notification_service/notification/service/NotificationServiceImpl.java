package com.reoagms.notification_service.notification.service;

import com.reoagms.notification_service.common.enums.NotificationStatus;
import com.reoagms.notification_service.exception.NotificationNotFoundException;
import com.reoagms.notification_service.notification.dto.NotificationRequest;
import com.reoagms.notification_service.notification.dto.NotificationResponse;
import com.reoagms.notification_service.notification.mapper.NotificationMapper;
import com.reoagms.notification_service.notification.model.Notification;
import com.reoagms.notification_service.notification.repository.NotificationRepository;
import com.reoagms.notification_service.sms.service.SmsSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;
    private final EmailSenderService emailSenderService;
    private final SmsSenderService smsSenderService;
    private final PushSenderService pushSenderService;

    @Override
    public NotificationResponse create(NotificationRequest request) {

        Notification notification = mapper.toEntity(request);

        dispatch(notification);

        repository.save(notification);

        return mapper.toResponse(notification);

    }

    @Override
    public NotificationResponse getById(UUID id) {

        Notification notification = repository.findById(id)
                .orElseThrow(() ->
                        new NotificationNotFoundException("Notification not found"));

        return mapper.toResponse(notification);

    }

    @Override
    public List<NotificationResponse> getAll() {

        return repository.findAll()

                .stream()

                .map(mapper::toResponse)

                .toList();

    }

    @Override
    public void delete(UUID id) {

        repository.deleteById(id);

    }

    @Override
    public NotificationResponse retry(UUID id) {

        Notification notification = repository.findById(id)
                .orElseThrow(() ->
                        new NotificationNotFoundException("Notification not found"));

        dispatch(notification);

        repository.save(notification);

        return mapper.toResponse(notification);

    }

    /**
     * Attempts to send via the appropriate channel. On failure, increments
     * retryCount and records the failure reason instead of throwing, so
     * callers (including the scheduled retry job) can decide what to do next.
     */
    private void dispatch(Notification notification) {

        try {

            switch (notification.getChannel()) {

                case EMAIL -> emailSenderService.send(
                        notification.getRecipient(),
                        notification.getSubject(),
                        notification.getMessage());

                case SMS -> smsSenderService.send(
                        notification.getRecipient(),
                        notification.getMessage());

                case PUSH -> pushSenderService.send(
                        notification.getRecipient(),
                        notification.getSubject(),
                        notification.getMessage());

            }

            notification.setStatus(NotificationStatus.SENT);
            notification.setFailureReason(null);

        } catch (Exception ex) {

            log.error("Failed to send notification {}: {}", notification.getId(), ex.getMessage());

            notification.setStatus(NotificationStatus.FAILED);
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setFailureReason(ex.getMessage());

        }

    }

}
