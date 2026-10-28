package com.reoagms.notification_service.notification.controller;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.emailtemplate.model.EmailTemplate;
import com.reoagms.notification_service.emailtemplate.repository.EmailTemplateRepository;
import com.reoagms.notification_service.exception.EmailTemplateNotFoundException;
import com.reoagms.notification_service.notification.dto.EmailSendRequest;
import com.reoagms.notification_service.notification.dto.NotificationRequest;
import com.reoagms.notification_service.notification.dto.NotificationResponse;
import com.reoagms.notification_service.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Convenience endpoint from the spec: POST /api/v1/email.
 * Accepts either a raw subject/body, or a templateName + placeholders to
 * render, then routes through NotificationService so it's tracked and
 * covered by the retry mechanism like everything else.
 */
@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final NotificationService notificationService;
    private final EmailTemplateRepository emailTemplateRepository;

    @PostMapping
    public NotificationResponse send(@Valid @RequestBody EmailSendRequest request) {

        String subject = request.getSubject();
        String body = request.getBody();

        if (request.getTemplateName() != null) {

            EmailTemplate template = emailTemplateRepository.findByName(request.getTemplateName())
                    .orElseThrow(() ->
                            new EmailTemplateNotFoundException("Email template not found"));

            subject = template.getSubject();
            body = render(template.getBody(), request.getPlaceholders());

        }

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setRecipient(request.getRecipient());
        notificationRequest.setChannel(NotificationChannel.EMAIL);
        notificationRequest.setSubject(subject);
        notificationRequest.setMessage(body);

        return notificationService.create(notificationRequest);

    }

    private String render(String template, Map<String, String> placeholders) {

        if (placeholders == null) {
            return template;
        }

        String result = template;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        return result;

    }

}
