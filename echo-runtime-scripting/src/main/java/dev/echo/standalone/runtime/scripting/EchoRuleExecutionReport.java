package dev.echo.standalone.runtime.scripting;

import java.util.List;
import java.util.Objects;

public record EchoRuleExecutionReport(
        EchoRuleTrigger trigger,
        int evaluatedRules,
        int matchedRules,
        int actionCount,
        int appliedActionCount,
        List<EchoRuleExecutionResult> results
) {
    public EchoRuleExecutionReport {
        Objects.requireNonNull(trigger, "trigger");
        if (evaluatedRules < 0 || matchedRules < 0 || actionCount < 0 || appliedActionCount < 0) {
            throw new IllegalArgumentException("report counts must not be negative");
        }
        Objects.requireNonNull(results, "results");
        results = List.copyOf(results);
    }

    public List<String> matchedRuleIds() {
        return results.stream()
                .filter(EchoRuleExecutionResult::matched)
                .map(result -> result.rule().ruleId())
                .toList();
    }
}
