package dev.echo.standalone.runtime.network;

import java.util.Objects;

public record EchoNetworkDiagnostic(
        EchoNetworkDiagnosticSeverity severity,
        String message,
        String packetId
) {
    public EchoNetworkDiagnostic {
        Objects.requireNonNull(severity, "severity");
        message = EchoNetworkText.requireText(message, "message");
        if (packetId == null || packetId.isBlank()) {
            packetId = "none";
        } else {
            packetId = packetId.trim();
        }
    }
}
