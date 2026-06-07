package dev.echo.standalone.runtime.audio;

public record EchoAudioDiagnostic(
        EchoAudioDiagnosticSeverity severity,
        String message
) {
}
