package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5UiScreenHostModel {
    private static final String FOOTER_LINE =
            "M Terminal  G/R/U/B Index  LEFT_ALT Lens  J/K/RIGHT_BRACKET/LEFT_BRACKET/BACKSLASH HoloMap  N SignalOS  X/C/Y/Z Drone  Enter action  ESCAPE pause/resume";

    private EchoAgent5UiScreenHostModel() {
    }

    public static Map<String, Object> render(
            String mode,
            Map<String, Object> state,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources dataSources
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        String normalizedMode = normalizeMode(mode);
        Map<String, Object> hud = hudValues(state, source.hudValues());
        EchoUiSurface surface = EchoAgent5UiSurfaceRenderer.render(normalizedMode, state, source);
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("screenTitle", "ECHO NATIVE // " + normalizedMode);
        model.put("headerLines", List.of(
                "Pack: " + fallback(packId, "ashfall") + "     Host: standalone UI host",
                "Registered content: " + itemCount + " items/blocks",
                "Modules discovered: " + moduleCount,
                "Ashfall route data: " + missionCount + " missions / " + regionCount + " regions",
                "Notifications: " + notificationSummary(notifications(state, source)),
                "HUD: Health " + hud.get("health") + " / " + hud.get("hazard")
        ));
        model.put("surfaceLines", surface.lines());
        model.put("footerLine", FOOTER_LINE);
        model.put("hudValues", hud);
        model.put("notificationAnchor", "top_left_safe_area");
        model.put("adapterCoreBridge", true);
        model.put("serviceCodeExecuted", true);
        model.put("hostModelClass", EchoAgent5UiScreenHostModel.class.getSimpleName());
        return Map.copyOf(model);
    }

    private static String notificationSummary(List<Map<String, Object>> notifications) {
        return notifications.stream()
                .map(notification -> String.valueOf(notification.get("message")))
                .filter(message -> !message.isBlank())
                .reduce((left, right) -> left + " / " + right)
                .orElse("");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> notifications(Map<String, Object> state, EchoAgent5UiDataSources source) {
        if (state != null && state.get("notifications") instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(entry -> (Map<String, Object>) entry)
                    .map(Map::copyOf)
                    .toList();
        }
        return source.notifications();
    }

    private static String normalizeMode(String mode) {
        String normalized = mode == null ? "" : mode.trim().toUpperCase(java.util.Locale.ROOT);
        return normalized.isBlank() ? "TERMINAL" : normalized;
    }

    private static String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static Object stateValue(Map<String, Object> state, String key, Object fallback) {
        if (state == null || !state.containsKey(key)) {
            return fallback;
        }
        return state.get(key);
    }

    private static Map<String, Object> hudValues(Map<String, Object> state, Map<String, Object> fallbackHud) {
        Map<String, Object> hud = new LinkedHashMap<>(fallbackHud);
        hud.put("health", stateValue(state, "hudHealth", fallbackHud.get("health")));
        hud.put("hazard", stateValue(state, "hudHazard", fallbackHud.get("hazard")));
        hud.put("mission", stateValue(state, "hudMission", fallbackHud.get("mission")));
        return Map.copyOf(hud);
    }
}
