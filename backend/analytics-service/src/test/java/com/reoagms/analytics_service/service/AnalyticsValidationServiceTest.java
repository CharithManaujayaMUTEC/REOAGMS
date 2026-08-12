package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.common.enums.ScopeType;
import com.reoagms.analytics_service.common.exception.InvalidAnalyticsRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalyticsValidationServiceTest {

    private AnalyticsValidationService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsValidationService();
    }

    @Test
    void acceptsValidPeriodAndAssetScope() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);

        assertThatCode(() -> service.validatePeriod(from, from.plusDays(1))).doesNotThrowAnyException();
        assertThatCode(() -> service.validateScope(ScopeType.ASSET, UUID.randomUUID()))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsReversedPeriod() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 2, 0, 0);

        assertThatThrownBy(() -> service.validatePeriod(from, from.minusDays(1)))
                .isInstanceOf(InvalidAnalyticsRequestException.class)
                .hasMessageContaining("periodStart");
    }

    @Test
    void requiresScopeIdForFacilityScope() {
        assertThatThrownBy(() -> service.validateScope(ScopeType.FACILITY, null))
                .isInstanceOf(InvalidAnalyticsRequestException.class)
                .hasMessageContaining("scopeId");
    }
}
