package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.common.enums.ScopeType;
import com.reoagms.analytics_service.common.exception.InvalidAnalyticsRequestException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AnalyticsValidationService {

    private static final long MAX_REPORT_DAYS = 366;

    public void validatePeriod(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new InvalidAnalyticsRequestException("Both periodStart and periodEnd are required");
        }
        if (!from.isBefore(to)) {
            throw new InvalidAnalyticsRequestException("periodStart must be before periodEnd");
        }
        if (Duration.between(from, to).toDays() > MAX_REPORT_DAYS) {
            throw new InvalidAnalyticsRequestException("Reporting period cannot exceed 366 days");
        }
    }

    public void validateEnergyScope(UUID facilityId, UUID assetId) {
        if (facilityId == null && assetId == null) {
            throw new InvalidAnalyticsRequestException("Either facilityId or assetId is required");
        }
    }

    public void validateScope(ScopeType scopeType, UUID scopeId) {
        if (scopeType == null) {
            throw new InvalidAnalyticsRequestException("scopeType is required");
        }
        if (scopeType != ScopeType.ORGANIZATION && scopeId == null) {
            throw new InvalidAnalyticsRequestException("scopeId is required for FACILITY and ASSET scopes");
        }
        if (scopeType == ScopeType.ORGANIZATION && scopeId != null) {
            throw new InvalidAnalyticsRequestException("scopeId must be omitted for ORGANIZATION scope");
        }
    }
}
