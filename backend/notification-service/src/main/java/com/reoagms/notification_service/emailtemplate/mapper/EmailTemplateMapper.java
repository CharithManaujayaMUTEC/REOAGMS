package com.reoagms.notification_service.emailtemplate.mapper;

import com.reoagms.notification_service.emailtemplate.dto.EmailTemplateRequest;
import com.reoagms.notification_service.emailtemplate.dto.EmailTemplateResponse;
import com.reoagms.notification_service.emailtemplate.model.EmailTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateMapper {

    public EmailTemplate toEntity(EmailTemplateRequest request) {

        return EmailTemplate.builder()
                .name(request.getName())
                .subject(request.getSubject())
                .body(request.getBody())
                .description(request.getDescription())
                .build();

    }

    public EmailTemplateResponse toResponse(EmailTemplate template) {

        return EmailTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .subject(template.getSubject())
                .body(template.getBody())
                .description(template.getDescription())
                .build();

    }

}
