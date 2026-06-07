package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EchoAgent5NotificationQueue {
    private final ArrayList<EchoAgent5Notification> notifications = new ArrayList<>();

    public EchoAgent5Notification enqueue(String severity, String message, String anchor) {
        EchoAgent5Notification notification = new EchoAgent5Notification(
                "agent5-notification-" + (notifications.size() + 1),
                severity,
                message,
                anchor,
                true
        );
        notifications.add(notification);
        return notification;
    }

    public List<EchoAgent5Notification> notifications() {
        return List.copyOf(notifications);
    }

    public List<Map<String, Object>> data() {
        return notifications.stream()
                .map(EchoAgent5Notification::data)
                .toList();
    }

    public Map<String, Object> dismissOldest() {
        Map<String, Object> dismissed = EchoAgent5UiActionRouter.routeNotificationDismiss(data(), EchoAgent5UiDataSources.reference());
        if (Boolean.TRUE.equals(dismissed.get("handled")) && !notifications.isEmpty()) {
            notifications.remove(0);
        }
        return dismissed;
    }

    public boolean deliveredTo(String anchor) {
        return notifications.stream()
                .anyMatch(notification -> notification.delivered() && notification.anchor().equals(anchor));
    }
}
