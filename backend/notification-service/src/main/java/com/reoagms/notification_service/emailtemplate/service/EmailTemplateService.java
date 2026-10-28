package com.reoagms.notification_service.emailtemplate.service;

import com.reoagms.notification_service.emailtemplate.dto.EmailTemplateRequest;
import com.reoagms.notification_service.emailtemplate.dto.EmailTemplateResponse;

import java.util.List;
import java.util.UUID;

public interface EmailTemplateService {

    EmailTemplateResponse create(EmailTemplateRequest request);

    EmailTemplateResponse getById(UUID id);

    List<EmailTemplateResponse> getAll();

    EmailTemplateResponse update(UUID id, EmailTemplateRequest request);

    void delete(UUID id);

}
