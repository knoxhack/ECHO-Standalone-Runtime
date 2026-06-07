package dev.echo.standalone.runtime.scripting;

import java.util.List;
import java.util.Objects;

public record EchoRuleDefinition(
        String ruleId,
        String displayName,
        EchoRuleTrigger trigger,
        int priority,
        boolean enabled,
        List<EchoRuleCondition> conditions,
        List<EchoRuleAction> actions
) {
    public EchoRuleDefinition {
        ruleId = EchoScriptingText.requireText(ruleId, "ruleId");
        displayName = EchoScriptingText.requireText(displayName, "displayName");
        Objects.requireNonNull(trigger, "trigger");
        Objects.requireNonNull(conditions, "conditions");
        Objects.requireNonNull(actions, "actions");
        conditions = List.copyOf(conditions);
        actions = List.copyOf(actions);
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("conditions must not be empty");
        }
        if (actions.isEmpty()) {
            throw new IllegalArgumentException("actions must not be empty");
        }
    }
}
