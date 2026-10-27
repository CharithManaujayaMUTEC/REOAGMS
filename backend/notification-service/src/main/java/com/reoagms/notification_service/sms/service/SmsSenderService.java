package com.reoagms.notification_service.sms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Placeholder SMS gateway integration. Swap the body of send() for a real
 * provider (e.g. Twilio, AWS SNS) once credentials are available.
 */
@Slf4j
@Service
public class SmsSenderService {

    public void send(String phoneNumber, String message) {

        // TODO: integrate real SMS provider
        log.info("SMS sent to {}: {}", phoneNumber, message);

    }

}
