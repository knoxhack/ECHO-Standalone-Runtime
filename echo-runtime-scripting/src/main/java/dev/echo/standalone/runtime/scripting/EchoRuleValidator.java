package dev.echo.standalone.runtime.scripting;

import dev.echo.standalone.runtime.item.EchoItemId;

import java.util.ArrayList;
import java.util.Objects;

public final class EchoRuleValidator {
    private final EchoRuleSandboxPolicy policy;

    public EchoRuleValidator(EchoRuleSandboxPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public EchoRuleValidationResult validate(EchoRuleRegistry registry, EchoRuleExecutionContext context) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(context, "context");
        ArrayList<EchoRuleValidationIssue> issues = new ArrayList<>();
        if (registry.count() > policy.maxRules()) {
            error(issues, "runtime", "rule count exceeds sandbox maximum");
        }
        for (EchoRuleDefinition rule : registry.all()) {
            validateRule(rule, context, issues);
        }
        return new EchoRuleValidationResult(issues);
    }

    private void validateRule(
            EchoRuleDefinition rule,
            EchoRuleExecutionContext context,
            ArrayList<EchoRuleValidationIssue> issues
    ) {
        if (!policy.allowedTriggers().contains(rule.trigger())) {
            error(issues, rule.ruleId(), "trigger is not allowed by sandbox policy");
        }
        if (rule.conditions().size() > policy.maxConditionsPerRule()) {
            error(issues, rule.ruleId(), "condition count exceeds sandbox maximum");
        }
        if (rule.actions().size() > policy.maxActionsPerRule()) {
            error(issues, rule.ruleId(), "action count exceeds sandbox maximum");
        }
        for (EchoRuleCondition condition : rule.conditions()) {
            if (!policy.allowedConditions().contains(condition.type())) {
                error(issues, rule.ruleId(), "condition type is not allowed: " + condition.type().name());
            }
            validateConditionTarget(rule.ruleId(), condition, context, issues);
        }
        for (EchoRuleAction action : rule.actions()) {
            if (!policy.allowedActions().contains(action.type())) {
                error(issues, rule.ruleId(), "action type is not allowed: " + action.type().name());
            }
            validateActionTarget(rule.ruleId(), action, context, issues);
        }
    }

    private static void validateConditionTarget(
            String ruleId,
            EchoRuleCondition condition,
            EchoRuleExecutionContext context,
            ArrayList<EchoRuleValidationIssue> issues
    ) {
        switch (condition.type()) {
            case OBJECTIVE_ACTIVE, OBJECTIVE_COMPLETED -> {
                if (context.gameplay().mission().objective(condition.target()).isEmpty()) {
                    error(issues, ruleId, "unknown objective target: " + condition.target());
                }
            }
            case INVENTORY_CONTAINS -> {
                if (condition.amount() <= 0) {
                    error(issues, ruleId, "inventory condition amount must be positive");
                }
                if (context.items().registry().find(new EchoItemId(condition.target())).isEmpty()) {
                    error(issues, ruleId, "unknown item target: " + condition.target());
                }
            }
            case HYDRATION_AT_OR_BELOW, ASH_DENSITY_AT_LEAST -> {
                if (condition.threshold() > 100.0D) {
                    error(issues, ruleId, "threshold must not exceed 100");
                }
            }
            case HAZARD_COUNT_AT_LEAST -> {
                if (condition.amount() <= 0) {
                    error(issues, ruleId, "hazard count condition amount must be positive");
                }
            }
            case ALWAYS -> {
            }
        }
    }

    private static void validateActionTarget(
            String ruleId,
            EchoRuleAction action,
            EchoRuleExecutionContext context,
            ArrayList<EchoRuleValidationIssue> issues
    ) {
        switch (action.type()) {
            case COMPLETE_OBJECTIVE -> {
                if (context.gameplay().mission().objective(action.target()).isEmpty()) {
                    error(issues, ruleId, "unknown objective action target: " + action.target());
                }
            }
            case AWARD_EXPERIENCE -> {
                if (action.amount() <= 0) {
                    error(issues, ruleId, "experience award amount must be positive");
                }
            }
            case ADD_MILESTONE -> {
                if (action.target().equals("none")) {
                    error(issues, ruleId, "milestone target must be set");
                }
            }
            case ADJUST_FACTION_REPUTATION -> {
                if (context.gameplay().factions().find(action.target()).isEmpty()) {
                    error(issues, ruleId, "unknown faction action target: " + action.target());
                }
            }
            case ADD_NOTIFICATION, EMIT_DIAGNOSTIC -> {
                if (action.message().equals("none")) {
                    error(issues, ruleId, "message action must include message text");
                }
            }
        }
    }

    private static void error(ArrayList<EchoRuleValidationIssue> issues, String ruleId, String message) {
        issues.add(new EchoRuleValidationIssue(EchoScriptingDiagnosticSeverity.ERROR, ruleId, message));
    }
}
