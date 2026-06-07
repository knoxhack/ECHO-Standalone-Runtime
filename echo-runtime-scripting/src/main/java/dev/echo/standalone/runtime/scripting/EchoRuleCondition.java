package dev.echo.standalone.runtime.scripting;

import java.util.Objects;

public record EchoRuleCondition(
        String conditionId,
        EchoRuleConditionType type,
        String target,
        double threshold,
        int amount
) {
    public EchoRuleCondition {
        conditionId = EchoScriptingText.requireText(conditionId, "conditionId");
        Objects.requireNonNull(type, "type");
        target = EchoScriptingText.optionalText(target);
        if (threshold < 0.0D) {
            throw new IllegalArgumentException("threshold must not be negative");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }

    public static EchoRuleCondition always(String conditionId) {
        return new EchoRuleCondition(conditionId, EchoRuleConditionType.ALWAYS, "none", 0.0D, 0);
    }

    public static EchoRuleCondition objectiveActive(String conditionId, String objectiveId) {
        return new EchoRuleCondition(conditionId, EchoRuleConditionType.OBJECTIVE_ACTIVE, objectiveId, 0.0D, 0);
    }

    public static EchoRuleCondition objectiveCompleted(String conditionId, String objectiveId) {
        return new EchoRuleCondition(conditionId, EchoRuleConditionType.OBJECTIVE_COMPLETED, objectiveId, 0.0D, 0);
    }

    public static EchoRuleCondition hydrationAtOrBelow(String conditionId, double threshold) {
        return new EchoRuleCondition(conditionId, EchoRuleConditionType.HYDRATION_AT_OR_BELOW, "none", threshold, 0);
    }

    public static EchoRuleCondition ashDensityAtLeast(String conditionId, double threshold) {
        return new EchoRuleCondition(conditionId, EchoRuleConditionType.ASH_DENSITY_AT_LEAST, "none", threshold, 0);
    }

    public static EchoRuleCondition hazardCountAtLeast(String conditionId, int amount) {
        return new EchoRuleCondition(conditionId, EchoRuleConditionType.HAZARD_COUNT_AT_LEAST, "none", 0.0D, amount);
    }

    public static EchoRuleCondition inventoryContains(String conditionId, String itemId, int amount) {
        return new EchoRuleCondition(conditionId, EchoRuleConditionType.INVENTORY_CONTAINS, itemId, 0.0D, amount);
    }
}
