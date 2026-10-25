package com.reoagms.notification_service.email.controller;

import com.reoagms.notification_service.email.dto.EmailTemplateRequest;
import com.reoagms.notification_service.email.dto.EmailTemplateResponse;
import com.reoagms.notification_service.email.service.EmailTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService templateService;

    @PostMapping
    public EmailTemplateResponse create(
            @Valid
            @RequestBody
            EmailTemplateRequest request) {

        return templateService.create(request);

    }

    @GetMapping("/{id}")
    public EmailTemplateResponse getById(
            @PathVariable UUID id) {

        return templateService.getById(id);

    }

    @GetMapping
    public List<EmailTemplateResponse> getAll() {

        return templateService.getAll();

    }

    @PutMapping("/{id}")
    public EmailTemplateResponse update(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            EmailTemplateRequest request) {

        return templateService.update(id, request);

    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id) {

        templateService.delete(id);

    }

}
