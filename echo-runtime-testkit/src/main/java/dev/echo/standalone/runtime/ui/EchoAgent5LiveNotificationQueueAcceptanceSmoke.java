package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveNotificationQueueAcceptanceSmoke {
    private EchoAgent5LiveNotificationQueueAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> endToEnd = object(EchoAgent5NotificationEndToEndAcceptanceSmoke.capture(
                "echoashfallprotocol",
                92,
                20,
                1,
                1,
                source
        ).get("accepted"));
        Map<String, Object> accepted = EchoAgent5LiveNotificationQueueAcceptance.assess(
                true,
                "top_left_safe_area",
                endToEnd
        );
        Map<String, Object> rejectedNoDispatch = EchoAgent5LiveNotificationQueueAcceptance.assess(
                false,
                "top_left_safe_area",
                endToEnd
        );
        Map<String, Object> rejectedWrongAnchor = EchoAgent5LiveNotificationQueueAcceptance.assess(
                true,
                "bottom_right",
                endToEnd
        );
        Map<String, Object> rejectedNoEndToEnd = EchoAgent5LiveNotificationQueueAcceptance.assess(
                true,
                "top_left_safe_area",
                Map.of("accepted", false)
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_notification_queue:accepted:2->1:top_left_safe_area".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoDispatch.get("accepted"))
                && Boolean.FALSE.equals(rejectedWrongAnchor.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoEndToEnd.get("accepted"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveNotificationQueueAcceptanceSmokeClass",
                EchoAgent5LiveNotificationQueueAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoDispatch", rejectedNoDispatch);
        smoke.put("rejectedWrongAnchor", rejectedWrongAnchor);
        smoke.put("rejectedNoEndToEnd", rejectedNoEndToEnd);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }
}
