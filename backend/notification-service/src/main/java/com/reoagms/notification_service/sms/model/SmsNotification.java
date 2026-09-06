package com.reoagms.notification_service.sms.model;

import com.reoagms.notification_service.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sms_notifications")
public class SmsNotification extends BaseEntity {

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 500)
    private String message;

    private String providerResponse;

}
