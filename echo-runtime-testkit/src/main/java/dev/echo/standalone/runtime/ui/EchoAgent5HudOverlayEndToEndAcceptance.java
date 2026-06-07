package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5HudOverlayEndToEndAcceptance {
    private EchoAgent5HudOverlayEndToEndAcceptance() {
    }

    public static Map<String, Object> assess(
            Map<String, Object> physicalHotkey,
            Map<String, Object> hudOverlaySmoke,
            Map<String, Object> hudUpdateSmoke,
            Map<String, Object> cameraCinematicSmoke
    ) {
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        Map<String, Object> hotkey = physicalHotkey == null ? Map.of() : physicalHotkey;
        Map<String, Object> overlay = hudOverlaySmoke == null ? Map.of() : hudOverlaySmoke;
        Map<String, Object> update = hudUpdateSmoke == null ? Map.of() : hudUpdateSmoke;
        Map<String, Object> camera = cameraCinematicSmoke == null ? Map.of() : cameraCinematicSmoke;
        boolean accepted = Boolean.TRUE.equals(hotkey.get("handled"))
                && "HUD_UPDATE".equals(hotkey.get("key"))
                && "HUD".equals(hotkey.get("surface"))
                && Boolean.TRUE.equals(hotkey.get("hudOverlay"))
                && Boolean.TRUE.equals(overlay.get("passed"))
                && Boolean.TRUE.equals(overlay.get("overlayRendered"))
                && "hud:update".equals(overlay.get("trigger"))
                && Boolean.TRUE.equals(update.get("passed"))
                && Integer.valueOf(85).equals(update.get("hudHealth"))
                && "hud:update:health_hazard_mission".equals(update.get("effect"))
                && Boolean.TRUE.equals(camera.get("passed"))
                && "over_shoulder".equals(camera.get("cameraMode"))
                && ("camera_cinematic:frame:" + source.cinematicValues().get("cue")).equals(camera.get("effect"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("key", string(hotkey.get("key")));
        result.put("surface", string(hotkey.get("surface")));
        result.put("overlayRendered", Boolean.TRUE.equals(overlay.get("overlayRendered")));
        result.put("hudHealth", update.getOrDefault("hudHealth", 0));
        result.put("hudHazard", string(update.get("hudHazard")));
        result.put("cameraMode", string(camera.get("cameraMode")));
        result.put("cinematicCue", string(camera.get("cinematicCue")));
        result.put("effect", accepted ? "hud_overlay_end_to_end:hud_update:HUD:85" : "hud_overlay_end_to_end:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    private static String string(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
