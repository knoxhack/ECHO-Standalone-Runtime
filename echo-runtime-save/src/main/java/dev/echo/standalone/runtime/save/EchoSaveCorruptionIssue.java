package dev.echo.standalone.runtime.save;

public record EchoSaveCorruptionIssue(
        EchoSaveCorruptionSeverity severity,
        String code,
        String path,
        String message
) {
    public EchoSaveCorruptionIssue {
        if (severity == null) {
            throw new IllegalArgumentException("severity must not be null");
        }
        code = EchoSavePaths.requireText(code, "code");
        path = path == null ? "" : path.replace('\\', '/');
        message = EchoSavePaths.requireText(message, "message");
    }
}
