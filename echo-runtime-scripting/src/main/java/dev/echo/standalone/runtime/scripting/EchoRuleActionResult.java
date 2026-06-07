package dev.echo.standalone.runtime.scripting;

import java.util.Objects;

public record EchoRuleActionResult(
        EchoRuleAction action,
        boolean applied,
        String reason
) {
    public EchoRuleActionResult {
        Objects.requireNonNull(action, "action");
        reason = EchoScriptingText.requireText(reason, "reason");
    }
}
