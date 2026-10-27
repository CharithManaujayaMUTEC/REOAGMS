package com.reoagms.notification_service.emailtemplate.controller;

import com.reoagms.notification_service.emailtemplate.dto.EmailTemplateRequest;
import com.reoagms.notification_service.emailtemplate.dto.EmailTemplateResponse;
import com.reoagms.notification_service.emailtemplate.service.EmailTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @PostMapping
    public EmailTemplateResponse create(@Valid @RequestBody EmailTemplateRequest request) {
        return emailTemplateService.create(request);
    }

    @GetMapping("/{id}")
    public EmailTemplateResponse getById(@PathVariable UUID id) {
        return emailTemplateService.getById(id);
    }

    @GetMapping
    public List<EmailTemplateResponse> getAll() {
        return emailTemplateService.getAll();
    }

    @PutMapping("/{id}")
    public EmailTemplateResponse update(@PathVariable UUID id, @Valid @RequestBody EmailTemplateRequest request) {
        return emailTemplateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        emailTemplateService.delete(id);
    }

}
