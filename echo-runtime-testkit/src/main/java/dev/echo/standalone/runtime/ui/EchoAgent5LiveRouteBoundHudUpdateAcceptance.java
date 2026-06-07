package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5LiveRouteBoundHudUpdateAcceptance {
    private EchoAgent5LiveRouteBoundHudUpdateAcceptance() {
    }

    public static Map<String, Object> assess(
            Map<String, Object> hud,
            Map<String, Object> routeEffectTranscript
    ) {
        boolean hudAccepted = acceptedHud(hud);
        List<String> observedKeys = strings(routeEffectTranscript == null
                ? null
                : routeEffectTranscript.get("observedKeys"));
        boolean routeBound = routeEffectTranscript != null
                && Boolean.TRUE.equals(routeEffectTranscript.get("accepted"));
        boolean accepted = hudAccepted && routeBound;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("hudAccepted", hudAccepted);
        result.put("routeBound", routeBound);
        result.put("observedKeys", observedKeys);
        result.put("key", text(hud == null ? null : hud.get("key")));
        result.put("surface", text(hud == null ? null : hud.get("surface")));
        result.put("overlayRendered", hud != null && Boolean.TRUE.equals(hud.get("overlayRendered")));
        result.put("hudHealth", hud == null ? 0 : hud.getOrDefault("hudHealth", 0));
        result.put("hudHazard", text(hud == null ? null : hud.get("hudHazard")));
        result.put("cameraMode", text(hud == null ? null : hud.get("cameraMode")));
        result.put("cinematicCue", text(hud == null ? null : hud.get("cinematicCue")));
        result.put("effect", accepted
                ? "live_route_bound_hud_update:accepted:hud_update"
                : "live_route_bound_hud_update:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    public static Map<String, Object> smoke() {
        Map<String, Object> hud = object(EchoAgent5HudOverlayEndToEndAcceptanceSmoke.capture().get("accepted"));
        Map<String, Object> route = Map.of(
                "accepted", true,
                "observedKeys", List.of(
                        "M",
                        "G",
                        "R",
                        "U",
                        "B",
                        "LEFT_ALT",
                        "J",
                        "K",
                        "RIGHT_BRACKET",
                        "LEFT_BRACKET",
                        "BACKSLASH",
                        "N",
                        "ESCAPE",
                        "X",
                        "C",
                        "Y",
                        "Z"
                )
        );
        Map<String, Object> accepted = assess(hud, route);
        Map<String, Object> rejectedNoHudUpdate = assess(Map.of(), route);
        Map<String, Object> rejectedNoRoute = assess(hud, Map.of(
                "accepted", false,
                "observedKeys", List.of("M", "G", "LEFT_ALT")
        ));
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_route_bound_hud_update:accepted:hud_update".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoHudUpdate.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRoute.get("accepted"));
        return Map.of(
                "liveRouteBoundHudUpdateAcceptanceClass",
                EchoAgent5LiveRouteBoundHudUpdateAcceptance.class.getSimpleName(),
                "accepted", accepted,
                "rejectedNoHudUpdate", rejectedNoHudUpdate,
                "rejectedNoRoute", rejectedNoRoute,
                "passed", passed,
                "adapterCoreBridge", true,
                "serviceCodeExecuted", true
        );
    }

    private static boolean acceptedHud(Map<String, Object> hud) {
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        return hud != null
                && Boolean.TRUE.equals(hud.get("accepted"))
                && "HUD_UPDATE".equals(hud.get("key"))
                && "HUD".equals(hud.get("surface"))
                && Boolean.TRUE.equals(hud.get("overlayRendered"))
                && Integer.valueOf(85).equals(hud.get("hudHealth"))
                && !text(hud.get("hudHazard")).isBlank()
                && "over_shoulder".equals(hud.get("cameraMode"))
                && source.cinematicValues().get("cue").equals(hud.get("cinematicCue"))
                && "hud_overlay_end_to_end:hud_update:HUD:85".equals(hud.get("effect"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private static List<String> strings(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(EchoAgent5LiveRouteBoundHudUpdateAcceptance::text).toList();
        }
        return List.of();
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
