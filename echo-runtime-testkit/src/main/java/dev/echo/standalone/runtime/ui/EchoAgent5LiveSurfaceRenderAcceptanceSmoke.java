package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5LiveSurfaceRenderAcceptanceSmoke {
    private static final String SCREEN_CLASS = "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost";
    private static final List<String> SURFACES = List.of(
            "TERMINAL",
            "INDEX",
            "LENS",
            "MISSION_LOG",
            "SETTINGS",
            "PAUSE",
            "RECOVERY",
            "HOLOMAP",
            "WIKI",
            "MAIN_MENU",
            "HUD"
    );

    private EchoAgent5LiveSurfaceRenderAcceptanceSmoke() {
    }

    public static Map<String, Object> capture() {
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        List<Map<String, Object>> acceptedRoutes = SURFACES.stream()
                .map(surface -> accepted(surface, source))
                .toList();
        Map<String, Object> accepted = acceptedRoutes.get(0);
        Map<String, Object> terminalAcceptance = liveSurface("TERMINAL", true);
        Map<String, Object> terminalSnapshot = snapshot("TERMINAL", true, source);
        Map<String, Object> rejectedUnacceptedSurface = EchoAgent5LiveSurfaceRenderAcceptance.assess(
                liveSurface("TERMINAL", false),
                terminalSnapshot
        );
        Map<String, Object> rejectedRenderedSurfaceMismatch = EchoAgent5LiveSurfaceRenderAcceptance.assess(
                terminalAcceptance,
                snapshot("INDEX", true, source)
        );
        boolean passed = acceptedRoutes.stream().allMatch(route -> Boolean.TRUE.equals(route.get("accepted")))
                && acceptedRoutes.stream().allMatch(route -> Boolean.TRUE.equals(route.get("serviceCodeExecuted")))
                && routeSurfaces(acceptedRoutes).equals(SURFACES)
                && "live_surface_render:accepted:TERMINAL".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedUnacceptedSurface.get("accepted"))
                && Boolean.FALSE.equals(rejectedUnacceptedSurface.get("serviceCodeExecuted"))
                && Boolean.FALSE.equals(rejectedRenderedSurfaceMismatch.get("accepted"))
                && Boolean.FALSE.equals(rejectedRenderedSurfaceMismatch.get("serviceCodeExecuted"));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveSurfaceRenderAcceptanceSmokeClass",
                EchoAgent5LiveSurfaceRenderAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("acceptedRoutes", acceptedRoutes);
        smoke.put("routeSurfaces", routeSurfaces(acceptedRoutes));
        smoke.put("rejectedUnacceptedSurface", rejectedUnacceptedSurface);
        smoke.put("rejectedRenderedSurfaceMismatch", rejectedRenderedSurfaceMismatch);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static Map<String, Object> accepted(String surface, EchoAgent5UiDataSources source) {
        return EchoAgent5LiveSurfaceRenderAcceptance.assess(
                liveSurface(surface, true),
                snapshot(surface, true, source)
        );
    }

    private static Map<String, Object> liveSurface(String surface, boolean setScreenInvoked) {
        return EchoAgent5LiveSurfaceAcceptance.assess(
                setScreenInvoked,
                SCREEN_CLASS,
                SCREEN_CLASS,
                surface,
                surface
        );
    }

    private static Map<String, Object> snapshot(String surface, boolean opened, EchoAgent5UiDataSources source) {
        return EchoAgent5UiHostSmokeSnapshot.capture(
                surface,
                opened,
                SCREEN_CLASS,
                "echoashfallprotocol",
                92,
                20,
                1,
                1,
                source
        );
    }

    private static List<String> routeSurfaces(List<Map<String, Object>> routes) {
        return routes.stream()
                .map(route -> String.valueOf(route.get("surface")))
                .toList();
    }
}
