package com.reoagms.notification_service.emailtemplate.repository;

import com.reoagms.notification_service.emailtemplate.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, UUID> {

    Optional<EmailTemplate> findByName(String name);

}
