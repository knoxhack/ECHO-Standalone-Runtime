package dev.echo.standalone.runtime.ui;

public record EchoNotification(
        String id,
        String severity,
        String message,
        String anchor,
        boolean delivered
) {
    public EchoNotification {
        id = requireText(id, "id");
        severity = requireText(severity, "severity");
        message = requireText(message, "message");
        anchor = requireText(anchor, "anchor");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
