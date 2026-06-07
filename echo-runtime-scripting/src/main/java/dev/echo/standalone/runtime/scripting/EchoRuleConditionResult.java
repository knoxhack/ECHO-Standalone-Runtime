package dev.echo.standalone.runtime.scripting;

import java.util.Objects;

public record EchoRuleConditionResult(
        EchoRuleCondition condition,
        boolean matched,
        String actual
) {
    public EchoRuleConditionResult {
        Objects.requireNonNull(condition, "condition");
        actual = EchoScriptingText.requireText(actual, "actual");
    }
}
