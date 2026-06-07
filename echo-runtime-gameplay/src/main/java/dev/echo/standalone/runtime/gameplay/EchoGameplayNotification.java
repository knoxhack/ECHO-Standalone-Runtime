package dev.echo.standalone.runtime.gameplay;

import java.util.Objects;

public record EchoGameplayNotification(
        String notificationId,
        EchoGameplayNotificationSeverity severity,
        String message,
        long tick
) {
    public EchoGameplayNotification {
        notificationId = EchoGameplayText.requireText(notificationId, "notificationId");
        Objects.requireNonNull(severity, "severity");
        message = EchoGameplayText.requireText(message, "message");
        if (tick < 0) {
            throw new IllegalArgumentException("tick must not be negative");
        }
    }
}
