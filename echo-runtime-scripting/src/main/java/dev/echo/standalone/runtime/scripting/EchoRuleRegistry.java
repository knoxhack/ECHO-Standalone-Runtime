package dev.echo.standalone.runtime.scripting;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoRuleRegistry {
    private final LinkedHashMap<String, EchoRuleDefinition> rules = new LinkedHashMap<>();

    public synchronized void register(EchoRuleDefinition rule) {
        Objects.requireNonNull(rule, "rule");
        if (rules.containsKey(rule.ruleId())) {
            throw new IllegalArgumentException("Duplicate rule id: " + rule.ruleId());
        }
        rules.put(rule.ruleId(), rule);
    }

    public synchronized Optional<EchoRuleDefinition> find(String ruleId) {
        String normalized = EchoScriptingText.requireText(ruleId, "ruleId");
        return Optional.ofNullable(rules.get(normalized));
    }

    public synchronized EchoRuleDefinition require(String ruleId) {
        String normalized = EchoScriptingText.requireText(ruleId, "ruleId");
        EchoRuleDefinition rule = rules.get(normalized);
        if (rule == null) {
            throw new IllegalArgumentException("Unknown rule id: " + normalized);
        }
        return rule;
    }

    public synchronized List<EchoRuleDefinition> all() {
        return rules.values().stream()
                .sorted(Comparator.comparingInt(EchoRuleDefinition::priority)
                        .thenComparing(EchoRuleDefinition::ruleId))
                .toList();
    }

    public synchronized List<EchoRuleDefinition> byTrigger(EchoRuleTrigger trigger) {
        Objects.requireNonNull(trigger, "trigger");
        return all().stream()
                .filter(EchoRuleDefinition::enabled)
                .filter(rule -> rule.trigger() == trigger)
                .toList();
    }

    public synchronized int count() {
        return rules.size();
    }
}
