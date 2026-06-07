package dev.echo.standalone.runtime.data;

public record EchoDataValidationIssue(
        EchoDataValidationSeverity severity,
        String code,
        String logicalId,
        String message
) {
    public EchoDataValidationIssue {
        if (severity == null) {
            throw new IllegalArgumentException("severity must not be null");
        }
        code = EchoDataPaths.requireText(code, "code");
        logicalId = logicalId == null ? "" : logicalId;
        message = EchoDataPaths.requireText(message, "message");
    }
}
