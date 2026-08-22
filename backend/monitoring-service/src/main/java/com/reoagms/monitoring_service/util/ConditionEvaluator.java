package com.reoagms.monitoring_service.util;

import com.reoagms.monitoring_service.common.enums.ConditionOperator;

import java.math.BigDecimal;

public final class ConditionEvaluator {

    private ConditionEvaluator() {
    }

    public static boolean isBreached(ConditionOperator operator, BigDecimal value, BigDecimal threshold) {
        int comparison = value.compareTo(threshold);
        return switch (operator) {
            case GREATER_THAN -> comparison > 0;
            case GREATER_THAN_OR_EQUAL -> comparison >= 0;
            case LESS_THAN -> comparison < 0;
            case LESS_THAN_OR_EQUAL -> comparison <= 0;
            case EQUAL -> comparison == 0;
        };
    }
}
