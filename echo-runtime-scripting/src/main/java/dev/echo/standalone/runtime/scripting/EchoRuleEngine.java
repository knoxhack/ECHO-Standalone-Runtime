package dev.echo.standalone.runtime.scripting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoRuleEngine {
    private final EchoRuleRegistry registry;
    private final EchoRuleConditionEvaluator conditionEvaluator;
    private final EchoRuleActionExecutor actionExecutor;

    public EchoRuleEngine(
            EchoRuleRegistry registry,
            EchoRuleConditionEvaluator conditionEvaluator,
            EchoRuleActionExecutor actionExecutor
    ) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.conditionEvaluator = Objects.requireNonNull(conditionEvaluator, "conditionEvaluator");
        this.actionExecutor = Objects.requireNonNull(actionExecutor, "actionExecutor");
    }

    public EchoRuleExecutionReport execute(EchoRuleTrigger trigger, EchoRuleExecutionContext context) {
        Objects.requireNonNull(trigger, "trigger");
        Objects.requireNonNull(context, "context");
        EchoRuleExecutionContext triggeredContext = context.withTrigger(trigger);
        ArrayList<EchoRuleExecutionResult> results = new ArrayList<>();
        int matchedRules = 0;
        int actionCount = 0;
        int appliedActionCount = 0;
        for (EchoRuleDefinition rule : registry.byTrigger(trigger)) {
            List<EchoRuleConditionResult> conditionResults = rule.conditions().stream()
                    .map(condition -> conditionEvaluator.evaluate(condition, triggeredContext))
                    .toList();
            boolean matched = conditionResults.stream().allMatch(EchoRuleConditionResult::matched);
            ArrayList<EchoRuleActionResult> actionResults = new ArrayList<>();
            if (matched) {
                matchedRules++;
                triggeredContext.diagnostics().info(rule.ruleId(), "matched rule " + rule.displayName());
                for (EchoRuleAction action : rule.actions()) {
                    EchoRuleActionResult actionResult = actionExecutor.execute(rule, action, triggeredContext);
                    actionResults.add(actionResult);
                    actionCount++;
                    if (actionResult.applied()) {
                        appliedActionCount++;
                    }
                }
            }
            results.add(new EchoRuleExecutionResult(rule, matched, conditionResults, actionResults));
        }
        return new EchoRuleExecutionReport(
                trigger,
                results.size(),
                matchedRules,
                actionCount,
                appliedActionCount,
                results
        );
    }
}
