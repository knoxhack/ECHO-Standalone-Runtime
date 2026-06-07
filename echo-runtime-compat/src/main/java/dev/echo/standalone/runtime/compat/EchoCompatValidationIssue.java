package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoCompatValidationIssue(
        EchoCompatDiagnosticSeverity severity,
        String mappingId,
        String message
) {
    public EchoCompatValidationIssue {
        Objects.requireNonNull(severity, "severity");
        mappingId = EchoCompatText.optionalText(mappingId);
        message = EchoCompatText.requireText(message, "message");
    }
}
