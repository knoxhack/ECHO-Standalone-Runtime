package dev.echo.standalone.runtime.scripting;

import java.util.List;
import java.util.Objects;

public record EchoRuleExecutionResult(
        EchoRuleDefinition rule,
        boolean matched,
        List<EchoRuleConditionResult> conditionResults,
        List<EchoRuleActionResult> actionResults
) {
    public EchoRuleExecutionResult {
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(conditionResults, "conditionResults");
        Objects.requireNonNull(actionResults, "actionResults");
        conditionResults = List.copyOf(conditionResults);
        actionResults = List.copyOf(actionResults);
    }
}
