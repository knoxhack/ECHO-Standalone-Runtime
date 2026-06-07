package dev.echo.standalone.runtime.scripting;

import java.util.Objects;

public record EchoScriptingDiagnostic(
        EchoScriptingDiagnosticSeverity severity,
        String ruleId,
        String message
) {
    public EchoScriptingDiagnostic {
        Objects.requireNonNull(severity, "severity");
        ruleId = EchoScriptingText.optionalText(ruleId);
        message = EchoScriptingText.requireText(message, "message");
    }
}
