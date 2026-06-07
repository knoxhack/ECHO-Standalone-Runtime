package dev.echo.standalone.runtime.render;

import java.util.Objects;

public record EchoRenderDiagnostic(
        EchoRenderDiagnosticSeverity severity,
        String message
) {
    public EchoRenderDiagnostic {
        Objects.requireNonNull(severity, "severity");
        message = EchoRenderText.requireText(message, "message");
    }
}
