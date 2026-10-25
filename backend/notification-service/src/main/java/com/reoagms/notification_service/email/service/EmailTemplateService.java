package com.reoagms.notification_service.email.service;

import com.reoagms.notification_service.email.dto.EmailTemplateRequest;
import com.reoagms.notification_service.email.dto.EmailTemplateResponse;

import java.util.List;
import java.util.UUID;

public interface EmailTemplateService {

    EmailTemplateResponse create(EmailTemplateRequest request);

    EmailTemplateResponse getById(UUID id);

    List<EmailTemplateResponse> getAll();

    EmailTemplateResponse update(UUID id, EmailTemplateRequest request);

    void delete(UUID id);

}
