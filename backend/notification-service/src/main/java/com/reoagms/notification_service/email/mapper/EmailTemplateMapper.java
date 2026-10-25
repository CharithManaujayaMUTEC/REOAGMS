package com.reoagms.notification_service.email.mapper;

import com.reoagms.notification_service.email.dto.EmailTemplateRequest;
import com.reoagms.notification_service.email.dto.EmailTemplateResponse;
import com.reoagms.notification_service.email.model.EmailTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateMapper {

    public EmailTemplate toEntity(EmailTemplateRequest request) {

        return EmailTemplate.builder()
                .name(request.getName())
                .subject(request.getSubject())
                .body(request.getBody())
                .build();

    }

    public EmailTemplateResponse toResponse(EmailTemplate template) {

        return EmailTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .subject(template.getSubject())
                .body(template.getBody())
                .build();

    }

}
