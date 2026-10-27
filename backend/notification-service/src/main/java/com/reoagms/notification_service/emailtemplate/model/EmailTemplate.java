package com.reoagms.notification_service.emailtemplate.model;

import com.reoagms.notification_service.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "email_templates")
public class EmailTemplate extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String subject;

    // Body may contain placeholders like {{recipientName}}, {{alertType}}
    @Column(nullable = false, length = 4000)
    private String body;

    private String description;

}
