package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5LiveSurfaceRouteAcceptanceSmoke {
    private static final String SCREEN_CLASS = "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost";
    private static final List<Route> ROUTES = List.of(
            new Route("M", "TERMINAL"),
            new Route("G", "INDEX"),
            new Route("R", "INDEX"),
            new Route("U", "INDEX"),
            new Route("B", "INDEX"),
            new Route("LEFT_ALT", "LENS"),
            new Route("J", "HOLOMAP"),
            new Route("K", "HOLOMAP"),
            new Route("RIGHT_BRACKET", "HOLOMAP"),
            new Route("LEFT_BRACKET", "HOLOMAP"),
            new Route("BACKSLASH", "HOLOMAP"),
            new Route("ESCAPE", "PAUSE")
    );

    private EchoAgent5LiveSurfaceRouteAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        List<Map<String, Object>> acceptedRoutes = ROUTES.stream()
                .map(route -> route(route.key(), route.surface(), source))
                .toList();
        Map<String, Object> rejectedNoHotkey = EchoAgent5LiveSurfaceRouteAcceptance.assess(
                Map.of("handled", false, "key", "M", "surface", "TERMINAL", "hudOverlay", false),
                liveSurface("TERMINAL", true),
                physicalInput(hotkey("M"), liveSurface("TERMINAL", true)),
                render(liveSurface("TERMINAL", true), "TERMINAL", true, source)
        );
        Map<String, Object> rejectedNoSurface = EchoAgent5LiveSurfaceRouteAcceptance.assess(
                hotkey("M"),
                liveSurface("TERMINAL", false),
                physicalInput(hotkey("M"), liveSurface("TERMINAL", false)),
                render(liveSurface("TERMINAL", false), "TERMINAL", true, source)
        );
        Map<String, Object> rejectedNoRender = EchoAgent5LiveSurfaceRouteAcceptance.assess(
                hotkey("M"),
                liveSurface("TERMINAL", true),
                physicalInput(hotkey("M"), liveSurface("TERMINAL", true)),
                render(liveSurface("TERMINAL", true), "TERMINAL", false, source)
        );
        Map<String, Object> rejectedDroneAction = route("X", "ASHFALL_DRONE", source);
        boolean passed = acceptedRoutes.stream().allMatch(route -> Boolean.TRUE.equals(route.get("accepted")))
                && routeSurfaces(acceptedRoutes).equals(ROUTES.stream().map(Route::surface).toList())
                && Boolean.FALSE.equals(rejectedNoHotkey.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoSurface.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRender.get("accepted"))
                && Boolean.FALSE.equals(rejectedDroneAction.get("accepted"))
                && "ASHFALL_DRONE".equals(rejectedDroneAction.get("surface"))
                && Boolean.TRUE.equals(rejectedDroneAction.get("physicalHotkeyHandled"))
                && Boolean.TRUE.equals(rejectedDroneAction.get("liveSurfaceAccepted"))
                && Boolean.TRUE.equals(rejectedDroneAction.get("physicalInputAccepted"))
                && Boolean.FALSE.equals(rejectedDroneAction.get("liveSurfaceRendered"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveSurfaceRouteAcceptanceSmokeClass",
                EchoAgent5LiveSurfaceRouteAcceptanceSmoke.class.getSimpleName());
        smoke.put("acceptedRoutes", acceptedRoutes);
        smoke.put("routeSurfaces", routeSurfaces(acceptedRoutes));
        smoke.put("rejectedNoHotkey", rejectedNoHotkey);
        smoke.put("rejectedNoSurface", rejectedNoSurface);
        smoke.put("rejectedNoRender", rejectedNoRender);
        smoke.put("rejectedDroneAction", rejectedDroneAction);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static Map<String, Object> route(String key, String surface, EchoAgent5UiDataSources source) {
        Map<String, Object> hotkey = hotkey(key);
        Map<String, Object> liveSurface = liveSurface(surface, true);
        Map<String, Object> input = physicalInput(hotkey, liveSurface);
        Map<String, Object> render = render(liveSurface, surface, true, source);
        return EchoAgent5LiveSurfaceRouteAcceptance.assess(hotkey, liveSurface, input, render);
    }

    private static Map<String, Object> hotkey(String key) {
        Map<String, Boolean> current = new LinkedHashMap<>(EchoAgent5PhysicalHotkeyPoller.emptyState());
        current.put(key, true);
        return EchoAgent5PhysicalHotkeyPoller.poll(
                EchoAgent5PhysicalHotkeyPoller.emptyState(),
                current
        );
    }

    private static Map<String, Object> liveSurface(String surface, boolean accepted) {
        return EchoAgent5LiveSurfaceAcceptance.assess(
                accepted,
                SCREEN_CLASS,
                SCREEN_CLASS,
                surface,
                surface
        );
    }

    private static Map<String, Object> physicalInput(
            Map<String, Object> hotkey,
            Map<String, Object> liveSurface
    ) {
        return EchoAgent5PhysicalInputAcceptance.assess(hotkey, liveSurface);
    }

    private static Map<String, Object> render(
            Map<String, Object> liveSurface,
            String surface,
            boolean opened,
            EchoAgent5UiDataSources source
    ) {
        return EchoAgent5LiveSurfaceRenderAcceptance.assess(
                liveSurface,
                EchoAgent5UiHostSmokeSnapshot.capture(
                        surface,
                        opened,
                        SCREEN_CLASS,
                        "echoashfallprotocol",
                        92,
                        20,
                        1,
                        1,
                        source
                )
        );
    }

    private static List<String> routeSurfaces(List<Map<String, Object>> routes) {
        return routes.stream()
                .map(route -> String.valueOf(route.get("surface")))
                .toList();
    }

    private record Route(String key, String surface) {
    }
}
