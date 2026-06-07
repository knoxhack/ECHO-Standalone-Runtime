package dev.echo.standalone.runtime.scripting;

import java.util.Objects;

public record EchoRuleValidationIssue(
        EchoScriptingDiagnosticSeverity severity,
        String ruleId,
        String message
) {
    public EchoRuleValidationIssue {
        Objects.requireNonNull(severity, "severity");
        ruleId = EchoScriptingText.optionalText(ruleId);
        message = EchoScriptingText.requireText(message, "message");
    }
}
