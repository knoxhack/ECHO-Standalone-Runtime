package dev.echo.standalone.runtime.ui;

import java.util.Map;

public record EchoAgent5Notification(
        String id,
        String severity,
        String message,
        String anchor,
        boolean delivered
) {
    public EchoAgent5Notification {
        id = requireText(id, "id");
        severity = requireText(severity, "severity");
        message = requireText(message, "message");
        anchor = requireText(anchor, "anchor");
    }

    public Map<String, Object> data() {
        return Map.of(
                "id", id,
                "severity", severity,
                "message", message,
                "anchor", anchor,
                "delivered", delivered
        );
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
