package com.reoagms.notification_service.sms.service;

import com.reoagms.notification_service.common.enums.NotificationStatus;
import com.reoagms.notification_service.exception.SMSNotificationNotFoundException;
import com.reoagms.notification_service.sms.dto.SMSNotificationRequest;
import com.reoagms.notification_service.sms.dto.SMSNotificationResponse;
import com.reoagms.notification_service.sms.mapper.SMSNotificationMapper;
import com.reoagms.notification_service.sms.model.SMSNotification;
import com.reoagms.notification_service.sms.repository.SMSNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SMSNotificationServiceImpl implements SMSNotificationService {

    private final SMSNotificationRepository repository;
    private final SMSNotificationMapper mapper;
    private final SmsSenderService smsSenderService;

    @Override
    public SMSNotificationResponse send(SMSNotificationRequest request) {

        SMSNotification sms = mapper.toEntity(request);

        try {

            smsSenderService.send(sms.getPhoneNumber(), sms.getMessage());
            sms.setStatus(NotificationStatus.SENT);

        } catch (Exception ex) {

            log.error("Failed to send SMS to {}: {}", sms.getPhoneNumber(), ex.getMessage());
            sms.setStatus(NotificationStatus.FAILED);
            sms.setRetryCount(sms.getRetryCount() + 1);

        }

        repository.save(sms);

        return mapper.toResponse(sms);

    }

    @Override
    public SMSNotificationResponse getById(UUID id) {

        SMSNotification sms = repository.findById(id)
                .orElseThrow(() ->
                        new SMSNotificationNotFoundException("SMS notification not found"));

        return mapper.toResponse(sms);

    }

    @Override
    public List<SMSNotificationResponse> getAll() {

        return repository.findAll()

                .stream()

                .map(mapper::toResponse)

                .toList();

    }

    @Override
    public void delete(UUID id) {

        repository.deleteById(id);

    }

}
