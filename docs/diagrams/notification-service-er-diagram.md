# Notification Service ER Diagram

The service contains no database foreign keys to other services' data. `relatedEntityId` on `notifications` is a free-text external reference only (e.g. an alert id from Monitoring), not a foreign key.

```mermaid
erDiagram
    NOTIFICATIONS {
        uuid id PK
        varchar recipient
        varchar channel "EMAIL | SMS | PUSH"
        varchar status "PENDING | SENT | FAILED | RETRYING"
        varchar message
        varchar subject
        varchar related_entity_id "external reference, e.g. an alert id"
        int retry_count
        varchar failure_reason
        timestamp created_at
        timestamp updated_at
    }

    EMAIL_TEMPLATES {
        uuid id PK
        varchar name "unique"
        varchar subject
        varchar body
        timestamp created_at
        timestamp updated_at
    }

    SMS_NOTIFICATIONS {
        uuid id PK
        varchar phone_number
        varchar message
        varchar provider_response
        timestamp created_at
        timestamp updated_at
    }
```

The three tables are intentionally independent — there is no foreign key between `sms_notifications`/`email_templates` and `notifications`. `SmsNotification` is a dispatch-side log written by `SmsDispatcher` for each SMS send attempt; `EmailTemplate` is reusable content managed via its own CRUD API and not yet wired to `Notification` creation (see README §10 — known limitation).
