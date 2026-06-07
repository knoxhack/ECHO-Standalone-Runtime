package dev.echo.standalone.runtime.gameplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoNotificationLog {
    private final ArrayList<EchoGameplayNotification> notifications = new ArrayList<>();

    public synchronized EchoGameplayNotification add(
            EchoGameplayNotificationSeverity severity,
            String message,
            long tick
    ) {
        Objects.requireNonNull(severity, "severity");
        String id = String.format(java.util.Locale.ROOT, "notification-%03d", notifications.size() + 1);
        EchoGameplayNotification notification = new EchoGameplayNotification(id, severity, message, tick);
        notifications.add(notification);
        return notification;
    }

    public synchronized List<EchoGameplayNotification> all() {
        return List.copyOf(notifications);
    }

    public synchronized int count() {
        return notifications.size();
    }
}
