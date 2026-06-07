package dev.echo.standalone.runtime.data;

import java.util.List;
import java.util.Objects;

public record EchoDataValidationReport(
        List<EchoDataValidationIssue> issues
) {
    public EchoDataValidationReport {
        Objects.requireNonNull(issues, "issues");
        issues = List.copyOf(issues);
    }

    public boolean ok() {
        return issues.stream().noneMatch(issue -> issue.severity() == EchoDataValidationSeverity.ERROR);
    }
}
