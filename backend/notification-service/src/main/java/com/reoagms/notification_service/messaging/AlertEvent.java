package com.reoagms.notification_service.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AlertEvent(int schemaVersion, UUID eventId, UUID alertId, UUID facilityId, UUID assetId,
                         String metricType, BigDecimal value, BigDecimal threshold, String severity,
                         String message, LocalDateTime occurredAt) { }
