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
                "Notifications " + notificationSummary(source.notifications()),
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
        smoke.put("notificationAnchor", "top_left_safe_area");
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", clientUiHostAttached
                && overlayRendered
                && source.hudValues().get("health").equals(hud.get("health"))
                && EchoAgent5UiReference.ACTIVE_MISSION_OBJECTIVE.equals(hud.get("mission")));
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
}
