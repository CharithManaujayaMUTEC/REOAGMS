package com.reoagms.monitoring_service.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Versioned integration contract published when an alert is opened. */
public record AlertEvent(
        int schemaVersion, UUID eventId, UUID alertId, UUID facilityId, UUID assetId,
        String metricType, BigDecimal value, BigDecimal threshold, String severity,
        String message, LocalDateTime occurredAt) { }
