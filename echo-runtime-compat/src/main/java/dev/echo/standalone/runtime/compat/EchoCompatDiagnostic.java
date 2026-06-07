package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoCompatDiagnostic(
        EchoCompatDiagnosticSeverity severity,
        String subject,
        String message
) {
    public EchoCompatDiagnostic {
        Objects.requireNonNull(severity, "severity");
        subject = EchoCompatText.optionalText(subject);
        message = EchoCompatText.requireText(message, "message");
    }
}
