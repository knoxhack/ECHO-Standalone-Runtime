package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5NotificationQueueSmoke {
    private EchoAgent5NotificationQueueSmoke() {
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
        Map<String, Object> hostModel = EchoAgent5UiScreenHostModel.render(
                "TERMINAL",
                Map.of(),
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source
        );
        List<Map<String, Object>> data = queue.data();
        List<String> messages = data.stream()
                .map(notification -> String.valueOf(notification.get("message")))
                .toList();
        List<String> severities = data.stream()
                .map(notification -> String.valueOf(notification.get("severity")))
                .toList();
        boolean delivered = data.stream().allMatch(notification -> Boolean.TRUE.equals(notification.get("delivered")));
        boolean anchored = data.stream().allMatch(notification -> "top_left_safe_area".equals(notification.get("anchor")));
        List<String> expectedMessages = source.notifications().stream()
                .map(notification -> String.valueOf(notification.get("message")))
                .toList();
        List<String> expectedSeverities = source.notifications().stream()
                .map(notification -> String.valueOf(notification.get("severity")))
                .toList();
        String expectedHeader = String.join(" / ", expectedMessages);
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("notificationQueueSmokeClass", EchoAgent5NotificationQueueSmoke.class.getSimpleName());
        smoke.put("queueId", EchoAgent5UiReference.NOTIFICATION_QUEUE);
        smoke.put("sourceCount", source.notifications().size());
        smoke.put("dispatchedCount", data.size());
        smoke.put("messages", messages);
        smoke.put("severities", severities);
        smoke.put("anchor", "top_left_safe_area");
        smoke.put("delivered", delivered);
        smoke.put("anchored", anchored);
        smoke.put("hostHeaderLines", hostModel.get("headerLines"));
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", source.notifications().size() == 2
                && data.size() == source.notifications().size()
                && delivered
                && anchored
                && messages.equals(expectedMessages)
                && severities.equals(expectedSeverities)
                && strings(hostModel.get("headerLines")).stream()
                        .anyMatch(line -> line.contains(expectedHeader)));
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
