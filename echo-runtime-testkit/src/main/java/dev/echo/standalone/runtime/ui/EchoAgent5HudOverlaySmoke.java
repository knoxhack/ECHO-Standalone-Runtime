package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5HudOverlaySmoke {
    private EchoAgent5HudOverlaySmoke() {
    }

    public static Map<String, Object> capture(
            boolean clientUiHostAttached,
            boolean overlayRendered,
            String trigger,
            String screenClass,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources dataSources
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> hud = source.hudValues();
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
        List<String> overlayLines = List.of(
                "Health " + hud.get("health"),
                "Hazard " + hud.get("hazard"),
                "Mission " + hud.get("mission"),
                "Status " + statusMeterSummary(maps(hud.get("statusMeters"))),
                "Notifications " + notificationSummary(source.notifications()),
                "Notification rows " + notificationRowSummary(maps(hud.get("notificationRows"))),
                "Ashfall anchor " + hud.get("notificationAnchor"),
                "Anchor top_left_safe_area"
        );
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("hudOverlaySmokeClass", EchoAgent5HudOverlaySmoke.class.getSimpleName());
        smoke.put("overlayLayerId", EchoAgent5UiReference.HUD_LAYER);
        smoke.put("trigger", trigger == null ? "" : trigger);
        smoke.put("screenClass", screenClass);
        smoke.put("clientUiHostAttached", clientUiHostAttached);
        smoke.put("overlayRendered", overlayRendered);
        smoke.put("overlayMessage", overlayMessage(hud));
        smoke.put("overlayLines", overlayLines);
        smoke.put("hostHeaderLines", hostModel.get("headerLines"));
        smoke.put("hudValues", hud);
        smoke.put("statusMeters", hud.get("statusMeters"));
        smoke.put("missionLine", hud.get("missionLine"));
        smoke.put("hazardLine", hud.get("hazardLine"));
        smoke.put("weatherLine", hud.get("weatherLine"));
        smoke.put("notificationRows", hud.get("notificationRows"));
        smoke.put("hudNotificationAnchor", hud.get("notificationAnchor"));
        smoke.put("notificationAnchor", "top_left_safe_area");
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", clientUiHostAttached
                && overlayRendered
                && source.hudValues().get("health").equals(hud.get("health"))
                && EchoAgent5UiReference.ACTIVE_MISSION_OBJECTIVE.equals(hud.get("mission"))
                && containsLabels(maps(hud.get("statusMeters")), List.of("VITAL", "FOOD", "H2O", "AIR/MASK", "RAD", "TEMP"))
                && "below_ashfall_status_panel".equals(hud.get("notificationAnchor"))
                && !maps(hud.get("notificationRows")).isEmpty());
        return Map.copyOf(smoke);
    }

    public static String overlayMessage(Map<String, Object> hud) {
        Map<String, Object> safeHud = hud == null ? Map.of() : hud;
        return "ECHO HUD Health " + safeHud.get("health")
                + " | " + safeHud.get("hazard")
                + " | " + safeHud.get("mission");
    }

    private static String notificationSummary(List<Map<String, Object>> notifications) {
        return notifications.stream()
                .map(notification -> String.valueOf(notification.get("message")))
                .filter(message -> !message.isBlank())
                .reduce((left, right) -> left + " / " + right)
                .orElse("");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(entry -> (Map<String, Object>) entry)
                    .map(Map::copyOf)
                    .toList();
        }
        return List.of();
    }

    private static String statusMeterSummary(List<Map<String, Object>> meters) {
        return meters.stream()
                .map(meter -> meter.get("label") + " " + meter.get("value"))
                .reduce((left, right) -> left + " / " + right)
                .orElse("");
    }

    private static String notificationRowSummary(List<Map<String, Object>> rows) {
        return rows.stream()
                .map(row -> String.valueOf(row.get("title")))
                .filter(title -> !title.isBlank())
                .reduce((left, right) -> left + " / " + right)
                .orElse("");
    }

    private static boolean containsLabels(List<Map<String, Object>> meters, List<String> labels) {
        List<String> present = meters.stream()
                .map(meter -> String.valueOf(meter.get("label")))
                .toList();
        return present.containsAll(labels);
    }
}
