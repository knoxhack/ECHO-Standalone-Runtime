package dev.echo.standalone.runtime.scripting;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public record EchoRuleSandboxPolicy(
        String policyId,
        boolean arbitraryCodeAllowed,
        int maxRules,
        int maxConditionsPerRule,
        int maxActionsPerRule,
        Set<EchoRuleTrigger> allowedTriggers,
        Set<EchoRuleConditionType> allowedConditions,
        Set<EchoRuleActionType> allowedActions
) {
    public EchoRuleSandboxPolicy {
        policyId = EchoScriptingText.requireText(policyId, "policyId");
        if (maxRules <= 0) {
            throw new IllegalArgumentException("maxRules must be positive");
        }
        if (maxConditionsPerRule <= 0) {
            throw new IllegalArgumentException("maxConditionsPerRule must be positive");
        }
        if (maxActionsPerRule <= 0) {
            throw new IllegalArgumentException("maxActionsPerRule must be positive");
        }
        Objects.requireNonNull(allowedTriggers, "allowedTriggers");
        Objects.requireNonNull(allowedConditions, "allowedConditions");
        Objects.requireNonNull(allowedActions, "allowedActions");
        allowedTriggers = Set.copyOf(allowedTriggers);
        allowedConditions = Set.copyOf(allowedConditions);
        allowedActions = Set.copyOf(allowedActions);
        if (allowedTriggers.isEmpty() || allowedConditions.isEmpty() || allowedActions.isEmpty()) {
            throw new IllegalArgumentException("allowed trigger, condition, and action sets must not be empty");
        }
    }

    public static EchoRuleSandboxPolicy declarativeOnly() {
        return new EchoRuleSandboxPolicy(
                "echo:declarative_rules_only",
                false,
                32,
                8,
                8,
                EnumSet.allOf(EchoRuleTrigger.class),
                EnumSet.allOf(EchoRuleConditionType.class),
                EnumSet.allOf(EchoRuleActionType.class)
        );
    }
}
