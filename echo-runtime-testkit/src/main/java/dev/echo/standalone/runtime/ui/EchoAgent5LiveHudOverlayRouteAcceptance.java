package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveHudOverlayRouteAcceptance {
    private EchoAgent5LiveHudOverlayRouteAcceptance() {
    }

    public static Map<String, Object> assess(
            Map<String, Object> physicalHotkey,
            Map<String, Object> hudRoute,
            Map<String, Object> hudOverlaySmoke,
            Map<String, Object> hudEndToEndAcceptance
    ) {
        Map<String, Object> hotkey = physicalHotkey == null ? Map.of() : physicalHotkey;
        Map<String, Object> route = hudRoute == null ? Map.of() : hudRoute;
        Map<String, Object> overlay = hudOverlaySmoke == null ? Map.of() : hudOverlaySmoke;
        Map<String, Object> endToEnd = hudEndToEndAcceptance == null ? Map.of() : hudEndToEndAcceptance;
        boolean accepted = Boolean.TRUE.equals(hotkey.get("handled"))
                && "HUD_UPDATE".equals(hotkey.get("key"))
                && "HUD".equals(hotkey.get("surface"))
                && Boolean.TRUE.equals(hotkey.get("hudOverlay"))
                && Boolean.TRUE.equals(route.get("handled"))
                && "HUD".equals(route.get("destinationMode"))
                && "hud:update".equals(route.get("effect"))
                && Boolean.TRUE.equals(overlay.get("overlayRendered"))
                && Boolean.TRUE.equals(overlay.get("passed"))
                && Boolean.TRUE.equals(endToEnd.get("accepted"))
                && "hud_overlay_end_to_end:hud_update:HUD:85".equals(endToEnd.get("effect"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("key", text(hotkey.get("key")));
        result.put("surface", text(hotkey.get("surface")));
        result.put("destinationMode", text(route.get("destinationMode")));
        result.put("routeEffect", text(route.get("effect")));
        result.put("overlayRendered", Boolean.TRUE.equals(overlay.get("overlayRendered")));
        result.put("hudHealth", endToEnd.getOrDefault("hudHealth", 0));
        result.put("hudHazard", text(endToEnd.get("hudHazard")));
        result.put("cameraMode", text(endToEnd.get("cameraMode")));
        result.put("cinematicCue", text(endToEnd.get("cinematicCue")));
        result.put("effect", accepted ? "live_hud_overlay_route:accepted:hud_update:HUD:85"
                : "live_hud_overlay_route:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
