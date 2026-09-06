package com.reoagms.notification_service.email.service;

import com.reoagms.notification_service.common.exception.ResourceNotFoundException;
import com.reoagms.notification_service.email.dto.EmailTemplateRequest;
import com.reoagms.notification_service.email.dto.EmailTemplateResponse;
import com.reoagms.notification_service.email.mapper.EmailTemplateMapper;
import com.reoagms.notification_service.email.model.EmailTemplate;
import com.reoagms.notification_service.email.repository.EmailTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceImplTest {

    @Mock
    private EmailTemplateRepository repository;

    private EmailTemplateServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmailTemplateServiceImpl(repository, new EmailTemplateMapper());
    }

    @Test
    void createPersistsAndReturnsTemplate() {
        EmailTemplateRequest request = new EmailTemplateRequest();
        request.setName("welcome");
        request.setSubject("Welcome");
        request.setBody("Hello {{name}}");

        when(repository.save(any(EmailTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailTemplateResponse response = service.create(request);

        assertThat(response.getName()).isEqualTo("welcome");
        assertThat(response.getSubject()).isEqualTo("Welcome");
        assertThat(response.getBody()).isEqualTo("Hello {{name}}");
    }

    @Test
    void getByIdThrowsWhenTemplateMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Email template not found");
    }
}
