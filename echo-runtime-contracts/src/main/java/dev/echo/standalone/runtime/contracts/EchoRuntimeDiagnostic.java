package dev.echo.standalone.runtime.contracts;

import java.util.Map;
import java.util.Objects;

public record EchoRuntimeDiagnostic(
        String code,
        EchoRuntimeDiagnosticSeverity severity,
        String runtimeLayer,
        String summary,
        String detail,
        Map<String, String> attributes
) {
    public EchoRuntimeDiagnostic {
        code = requireText(code, "code");
        Objects.requireNonNull(severity, "severity");
        runtimeLayer = requireText(runtimeLayer, "runtimeLayer");
        summary = requireText(summary, "summary");
        detail = detail == null ? "" : detail;
        Objects.requireNonNull(attributes, "attributes");
        attributes = Map.copyOf(attributes);
    }

    public static EchoRuntimeDiagnostic of(
            String code,
            EchoRuntimeDiagnosticSeverity severity,
            String runtimeLayer,
            String summary
    ) {
        return new EchoRuntimeDiagnostic(code, severity, runtimeLayer, summary, "", Map.of());
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
