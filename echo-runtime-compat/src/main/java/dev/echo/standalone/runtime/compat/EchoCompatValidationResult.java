package dev.echo.standalone.runtime.compat;

import java.util.List;
import java.util.Objects;

public record EchoCompatValidationResult(List<EchoCompatValidationIssue> issues) {
    public EchoCompatValidationResult {
        Objects.requireNonNull(issues, "issues");
        issues = List.copyOf(issues);
    }

    public boolean valid() {
        return errorCount() == 0;
    }

    public int warningCount() {
        return (int) issues.stream()
                .filter(issue -> issue.severity() == EchoCompatDiagnosticSeverity.WARNING)
                .count();
    }

    public int errorCount() {
        return (int) issues.stream()
                .filter(issue -> issue.severity() == EchoCompatDiagnosticSeverity.ERROR)
                .count();
    }
}
