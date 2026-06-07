package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveHudOverlayRouteAcceptanceSmoke {
    private static final String SCREEN_CLASS = "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost";

    private EchoAgent5LiveHudOverlayRouteAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> hotkey = Map.of(
                "handled", true,
                "key", "HUD_UPDATE",
                "surface", "HUD",
                "hudOverlay", true
        );
        Map<String, Object> route = Map.of(
                "handled", true,
                "destinationMode", "HUD",
                "effect", "hud:update"
        );
        Map<String, Object> overlay = overlay(true, source);
        Map<String, Object> update = EchoAgent5HudUpdateSmoke.capture(source);
        Map<String, Object> camera = EchoAgent5CameraCinematicSmoke.capture(source);
        Map<String, Object> endToEnd = EchoAgent5HudOverlayEndToEndAcceptance.assess(
                hotkey,
                overlay,
                update,
                camera
        );
        Map<String, Object> accepted = EchoAgent5LiveHudOverlayRouteAcceptance.assess(
                hotkey,
                route,
                overlay,
                endToEnd
        );
        Map<String, Object> rejectedNoRoute = EchoAgent5LiveHudOverlayRouteAcceptance.assess(
                hotkey,
                Map.of("handled", false, "destinationMode", "", "effect", ""),
                overlay,
                endToEnd
        );
        Map<String, Object> rejectedNoOverlay = EchoAgent5LiveHudOverlayRouteAcceptance.assess(
                hotkey,
                route,
                overlay(false, source),
                endToEnd
        );
        Map<String, Object> rejectedNoEndToEnd = EchoAgent5LiveHudOverlayRouteAcceptance.assess(
                hotkey,
                route,
                overlay,
                Map.of("accepted", false, "effect", "hud_overlay_end_to_end:rejected")
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_hud_overlay_route:accepted:hud_update:HUD:85".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoRoute.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoOverlay.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoEndToEnd.get("accepted"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveHudOverlayRouteAcceptanceSmokeClass",
                EchoAgent5LiveHudOverlayRouteAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoRoute", rejectedNoRoute);
        smoke.put("rejectedNoOverlay", rejectedNoOverlay);
        smoke.put("rejectedNoEndToEnd", rejectedNoEndToEnd);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static Map<String, Object> overlay(boolean rendered, EchoAgent5UiDataSources source) {
        return EchoAgent5HudOverlaySmoke.capture(
                true,
                rendered,
                "hud:update",
                SCREEN_CLASS,
                "echoashfallprotocol",
                92,
                20,
                1,
                1,
                source
        );
    }

}
