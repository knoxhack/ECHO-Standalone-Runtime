package dev.echo.standalone.runtime.scripting;

import java.util.List;
import java.util.Objects;

public record EchoRuleValidationResult(List<EchoRuleValidationIssue> issues) {
    public EchoRuleValidationResult {
        Objects.requireNonNull(issues, "issues");
        issues = List.copyOf(issues);
    }

    public boolean valid() {
        return errorCount() == 0;
    }

    public int warningCount() {
        return (int) issues.stream()
                .filter(issue -> issue.severity() == EchoScriptingDiagnosticSeverity.WARNING)
                .count();
    }

    public int errorCount() {
        return (int) issues.stream()
                .filter(issue -> issue.severity() == EchoScriptingDiagnosticSeverity.ERROR)
                .count();
    }
}
