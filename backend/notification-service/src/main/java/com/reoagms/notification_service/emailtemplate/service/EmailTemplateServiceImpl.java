package com.reoagms.notification_service.emailtemplate.service;

import com.reoagms.notification_service.emailtemplate.dto.EmailTemplateRequest;
import com.reoagms.notification_service.emailtemplate.dto.EmailTemplateResponse;
import com.reoagms.notification_service.emailtemplate.mapper.EmailTemplateMapper;
import com.reoagms.notification_service.emailtemplate.model.EmailTemplate;
import com.reoagms.notification_service.emailtemplate.repository.EmailTemplateRepository;
import com.reoagms.notification_service.exception.EmailTemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final EmailTemplateRepository repository;
    private final EmailTemplateMapper mapper;

    @Override
    public EmailTemplateResponse create(EmailTemplateRequest request) {

        EmailTemplate template = mapper.toEntity(request);

        repository.save(template);

        return mapper.toResponse(template);

    }

    @Override
    public EmailTemplateResponse getById(UUID id) {

        EmailTemplate template = repository.findById(id)
                .orElseThrow(() ->
                        new EmailTemplateNotFoundException("Email template not found"));

        return mapper.toResponse(template);

    }

    @Override
    public List<EmailTemplateResponse> getAll() {

        return repository.findAll()

                .stream()

                .map(mapper::toResponse)

                .toList();

    }

    @Override
    public EmailTemplateResponse update(UUID id, EmailTemplateRequest request) {

        EmailTemplate template = repository.findById(id)

                .orElseThrow(() ->
                        new EmailTemplateNotFoundException("Email template not found"));

        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        template.setDescription(request.getDescription());

        repository.save(template);

        return mapper.toResponse(template);

    }

    @Override
    public void delete(UUID id) {

        repository.deleteById(id);

    }

}
