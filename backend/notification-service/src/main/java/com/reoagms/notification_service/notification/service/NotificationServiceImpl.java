package com.reoagms.notification_service.notification.service;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.common.enums.NotificationStatus;
import com.reoagms.notification_service.common.exception.ResourceNotFoundException;
import com.reoagms.notification_service.notification.dto.NotificationRequest;
import com.reoagms.notification_service.notification.dto.NotificationResponse;
import com.reoagms.notification_service.notification.mapper.NotificationMapper;
import com.reoagms.notification_service.notification.model.Notification;
import com.reoagms.notification_service.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;
    private final List<NotificationDispatcher> dispatchers;

    private Map<NotificationChannel, NotificationDispatcher> dispatcherByChannel;

    private Map<NotificationChannel, NotificationDispatcher> dispatcherMap() {
        if (dispatcherByChannel == null) {
            dispatcherByChannel = dispatchers.stream()
                    .collect(Collectors.toMap(NotificationDispatcher::channel, d -> d));
        }
        return dispatcherByChannel;
    }

    @Override
    public NotificationResponse create(NotificationRequest request) {

        Notification notification = mapper.toEntity(request);

        repository.save(notification);

        send(notification);

        return mapper.toResponse(notification);

    }

    @Override
    public NotificationResponse getById(UUID id) {

        Notification notification = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

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
    public NotificationResponse update(UUID id, NotificationRequest request) {

        Notification notification = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setRecipient(request.getRecipient());
        notification.setChannel(request.getChannel());
        notification.setMessage(request.getMessage());
        notification.setSubject(request.getSubject());
        notification.setRelatedEntityId(request.getRelatedEntityId());

        repository.save(notification);

        return mapper.toResponse(notification);

    }

    @Override
    public void delete(UUID id) {

        repository.deleteById(id);

    }

    @Override
    public NotificationResponse retry(UUID id) {

        Notification notification = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        send(notification);

        return mapper.toResponse(notification);

    }

    private void send(Notification notification) {

        NotificationDispatcher dispatcher = dispatcherMap().get(notification.getChannel());

        if (dispatcher == null) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason("No dispatcher configured for channel " + notification.getChannel());
            repository.save(notification);
            return;
        }

        try {
            dispatcher.dispatch(notification);
            notification.setStatus(NotificationStatus.SENT);
            notification.setFailureReason(null);
        } catch (Exception ex) {
            log.warn("Failed to dispatch notification {}: {}", notification.getId(), ex.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(ex.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
        }

        repository.save(notification);

    }

}
