package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5NotificationDismissSmoke {
    private EchoAgent5NotificationDismissSmoke() {
    }

    public static Map<String, Object> capture(
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources dataSources
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        EchoAgent5NotificationQueue queue = new EchoAgent5NotificationQueue();
        for (Map<String, Object> notification : source.notifications()) {
            queue.enqueue(
                    String.valueOf(notification.get("severity")),
                    String.valueOf(notification.get("message")),
                    String.valueOf(notification.get("anchor"))
            );
        }
        Map<String, Object> before = EchoAgent5UiScreenHostModel.render(
                "TERMINAL",
                Map.of("notifications", queue.data()),
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source
        );
        Map<String, Object> dismissed = queue.dismissOldest();
        Map<String, Object> after = EchoAgent5UiScreenHostModel.render(
                "TERMINAL",
                Map.of("notifications", queue.data()),
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source
        );
        List<String> beforeHeader = strings(before.get("headerLines"));
        List<String> afterHeader = strings(after.get("headerLines"));
        List<String> expectedMessages = source.notifications().stream()
                .map(notification -> String.valueOf(notification.get("message")))
                .toList();
        String expectedBeforeHeader = String.join(" / ", expectedMessages);
        String expectedRemaining = expectedMessages.size() > 1 ? expectedMessages.get(1) : "";
        boolean passed = Boolean.TRUE.equals(dismissed.get("handled"))
                && "agent5-notification-1".equals(dismissed.get("dismissedId"))
                && expectedMessages.get(0).equals(dismissed.get("dismissedMessage"))
                && strings(dismissed.get("remainingMessages")).equals(List.of(expectedRemaining))
                && queue.data().size() == 1
                && beforeHeader.stream().anyMatch(line -> line.contains(expectedBeforeHeader))
                && afterHeader.stream().anyMatch(line -> line.contains("Notifications: " + expectedRemaining))
                && afterHeader.stream().noneMatch(line -> line.contains(expectedMessages.get(0) + " /"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("notificationDismissSmokeClass", EchoAgent5NotificationDismissSmoke.class.getSimpleName());
        smoke.put("serviceCodeExecuted", true);
        smoke.put("adapterCoreBridge", true);
        smoke.put("dismissedId", dismissed.get("dismissedId"));
        smoke.put("dismissedMessage", dismissed.get("dismissedMessage"));
        smoke.put("remainingMessages", dismissed.get("remainingMessages"));
        smoke.put("beforeHeaderLines", beforeHeader);
        smoke.put("afterHeaderLines", afterHeader);
        smoke.put("effect", dismissed.get("effect"));
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    @SuppressWarnings("unchecked")
    private static List<String> strings(Object value) {
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }
}
