package com.reoagms.notification_service.email.service;

import com.reoagms.notification_service.common.exception.ResourceNotFoundException;
import com.reoagms.notification_service.email.dto.EmailTemplateRequest;
import com.reoagms.notification_service.email.dto.EmailTemplateResponse;
import com.reoagms.notification_service.email.mapper.EmailTemplateMapper;
import com.reoagms.notification_service.email.model.EmailTemplate;
import com.reoagms.notification_service.email.repository.EmailTemplateRepository;
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
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found"));

        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());

        repository.save(template);

        return mapper.toResponse(template);

    }

    @Override
    public void delete(UUID id) {

        repository.deleteById(id);

    }

}
