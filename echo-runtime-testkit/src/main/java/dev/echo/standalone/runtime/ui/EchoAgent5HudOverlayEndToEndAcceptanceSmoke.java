package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5HudOverlayEndToEndAcceptanceSmoke {
    private EchoAgent5HudOverlayEndToEndAcceptanceSmoke() {
    }

    public static Map<String, Object> capture() {
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        Map<String, Object> hotkey = Map.of(
                "handled", true,
                "key", "HUD_UPDATE",
                "surface", "HUD",
                "hudOverlay", true
        );
        Map<String, Object> overlay = EchoAgent5HudOverlaySmoke.capture(
                true,
                true,
                "hud:update",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "echoashfallprotocol",
                92,
                20,
                1,
                1,
                source
        );
        Map<String, Object> update = EchoAgent5HudUpdateSmoke.capture(source);
        Map<String, Object> camera = EchoAgent5CameraCinematicSmoke.capture(source);
        Map<String, Object> accepted = EchoAgent5HudOverlayEndToEndAcceptance.assess(
                hotkey,
                overlay,
                update,
                camera
        );
        Map<String, Object> rejectedNoOverlay = EchoAgent5HudOverlayEndToEndAcceptance.assess(
                hotkey,
                EchoAgent5HudOverlaySmoke.capture(
                        true,
                        false,
                        "hud:update",
                        "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                        "echoashfallprotocol",
                        92,
                        20,
                        1,
                        1,
                        source
                ),
                update,
                camera
        );
        Map<String, Object> rejectedNoHudUpdate = EchoAgent5HudOverlayEndToEndAcceptance.assess(
                hotkey,
                overlay,
                Map.of("passed", false, "hudHealth", 92, "effect", ""),
                camera
        );
        Map<String, Object> rejectedNoCamera = EchoAgent5HudOverlayEndToEndAcceptance.assess(
                hotkey,
                overlay,
                update,
                Map.of("passed", false, "cameraMode", "", "effect", "")
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "hud_overlay_end_to_end:hud_update:HUD:85".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoOverlay.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoHudUpdate.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoCamera.get("accepted"));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("hudOverlayEndToEndAcceptanceSmokeClass",
                EchoAgent5HudOverlayEndToEndAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoOverlay", rejectedNoOverlay);
        smoke.put("rejectedNoHudUpdate", rejectedNoHudUpdate);
        smoke.put("rejectedNoCamera", rejectedNoCamera);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

}
