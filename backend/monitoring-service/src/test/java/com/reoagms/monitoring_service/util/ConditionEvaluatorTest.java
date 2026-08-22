package com.reoagms.monitoring_service.util;

import com.reoagms.monitoring_service.common.enums.ConditionOperator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionEvaluatorTest {

    @Test
    void greaterThanBreachesWhenValueExceedsThreshold() {
        assertThat(ConditionEvaluator.isBreached(
                ConditionOperator.GREATER_THAN, new BigDecimal("85"), new BigDecimal("80"))).isTrue();
        assertThat(ConditionEvaluator.isBreached(
                ConditionOperator.GREATER_THAN, new BigDecimal("80"), new BigDecimal("80"))).isFalse();
    }

    @Test
    void greaterThanOrEqualIncludesBoundary() {
        assertThat(ConditionEvaluator.isBreached(
                ConditionOperator.GREATER_THAN_OR_EQUAL, new BigDecimal("80"), new BigDecimal("80"))).isTrue();
    }

    @Test
    void lessThanBreachesWhenValueBelowThreshold() {
        assertThat(ConditionEvaluator.isBreached(
                ConditionOperator.LESS_THAN, new BigDecimal("10"), new BigDecimal("20"))).isTrue();
        assertThat(ConditionEvaluator.isBreached(
                ConditionOperator.LESS_THAN, new BigDecimal("20"), new BigDecimal("20"))).isFalse();
    }

    @Test
    void lessThanOrEqualIncludesBoundary() {
        assertThat(ConditionEvaluator.isBreached(
                ConditionOperator.LESS_THAN_OR_EQUAL, new BigDecimal("20"), new BigDecimal("20"))).isTrue();
    }

    @Test
    void equalMatchesExactValue() {
        assertThat(ConditionEvaluator.isBreached(
                ConditionOperator.EQUAL, new BigDecimal("50"), new BigDecimal("50.0"))).isTrue();
        assertThat(ConditionEvaluator.isBreached(
                ConditionOperator.EQUAL, new BigDecimal("50"), new BigDecimal("51"))).isFalse();
    }
}
