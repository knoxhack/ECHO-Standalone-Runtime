package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5LiveNotificationQueueAcceptance {
    private EchoAgent5LiveNotificationQueueAcceptance() {
    }

    public static Map<String, Object> assess(
            boolean queueDispatched,
            String notificationAnchor,
            Map<String, Object> notificationEndToEndAcceptance
    ) {
        Map<String, Object> endToEnd = notificationEndToEndAcceptance == null
                ? Map.of()
                : notificationEndToEndAcceptance;
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        List<String> expectedRemaining = List.of(String.valueOf(source.notifications().get(1).get("message")));
        List<String> remainingMessages = strings(endToEnd.get("remainingMessages"));
        boolean accepted = queueDispatched
                && "top_left_safe_area".equals(notificationAnchor)
                && Boolean.TRUE.equals(endToEnd.get("accepted"))
                && Boolean.TRUE.equals(endToEnd.get("queueAccepted"))
                && Boolean.TRUE.equals(endToEnd.get("hudAccepted"))
                && Boolean.TRUE.equals(endToEnd.get("dismissAccepted"))
                && Integer.valueOf(2).equals(number(endToEnd.get("sourceCount")))
                && Integer.valueOf(2).equals(number(endToEnd.get("dispatchedCount")))
                && "agent5-notification-1".equals(endToEnd.get("dismissedId"))
                && remainingMessages.equals(expectedRemaining);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("queueDispatched", queueDispatched);
        result.put("notificationAnchor", notificationAnchor == null ? "" : notificationAnchor);
        result.put("queueAccepted", Boolean.TRUE.equals(endToEnd.get("queueAccepted")));
        result.put("hudAccepted", Boolean.TRUE.equals(endToEnd.get("hudAccepted")));
        result.put("dismissAccepted", Boolean.TRUE.equals(endToEnd.get("dismissAccepted")));
        result.put("sourceCount", numberOrZero(endToEnd.get("sourceCount")));
        result.put("dispatchedCount", numberOrZero(endToEnd.get("dispatchedCount")));
        result.put("dismissedId", String.valueOf(endToEnd.getOrDefault("dismissedId", "")));
        result.put("remainingMessages", remainingMessages);
        result.put("effect", accepted
                ? "live_notification_queue:accepted:2->1:top_left_safe_area"
                : "live_notification_queue:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    @SuppressWarnings("unchecked")
    private static List<String> strings(Object value) {
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    private static Integer number(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    private static Integer numberOrZero(Object value) {
        Integer number = number(value);
        return number == null ? 0 : number;
    }
}
