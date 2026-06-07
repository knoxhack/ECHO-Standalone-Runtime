package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5LiveClientPhase5RouteSequenceAcceptance {
    private static final List<Route> REQUIRED_ROUTES = List.of(
            new Route("M", "TERMINAL", "screen"),
            new Route("G", "INDEX", "screen"),
            new Route("R", "INDEX", "screen"),
            new Route("U", "INDEX", "screen"),
            new Route("B", "INDEX", "screen"),
            new Route("LEFT_ALT", "LENS", "screen"),
            new Route("J", "HOLOMAP", "screen"),
            new Route("K", "HOLOMAP", "screen"),
            new Route("RIGHT_BRACKET", "HOLOMAP", "screen"),
            new Route("LEFT_BRACKET", "HOLOMAP", "screen"),
            new Route("BACKSLASH", "HOLOMAP", "screen"),
            new Route("N", "SIGNALOS", "screen"),
            new Route("X", "ASHFALL_DRONE", "action"),
            new Route("C", "ASHFALL_DRONE", "action"),
            new Route("Y", "ASHFALL_DRONE", "action"),
            new Route("Z", "ASHFALL_DRONE", "action"),
            new Route("ESCAPE", "PAUSE", "screen")
    );
    private static final List<String> REQUIRED_SURFACES = REQUIRED_ROUTES.stream()
            .map(Route::surface)
            .toList();
    private static final List<String> REQUIRED_HOTKEYS = REQUIRED_ROUTES.stream()
            .map(Route::hotkey)
            .toList();

    private EchoAgent5LiveClientPhase5RouteSequenceAcceptance() {
    }

    public static Map<String, Object> assess(
            boolean scheduled,
            boolean executed,
            List<Map<String, Object>> routes,
            boolean noScreenCrash
    ) {
        List<Map<String, Object>> sequence = routes == null ? List.of() : routes;
        List<String> surfaces = sequence.stream()
                .map(route -> String.valueOf(route.get("surface")))
                .toList();
        List<String> routeTypes = sequence.stream()
                .map(route -> String.valueOf(route.get("routeType")))
                .toList();
        List<String> hotkeys = sequence.stream()
                .map(route -> String.valueOf(route.get("hotkey")))
                .toList();
        List<String> physicalHotkeyEffects = sequence.stream()
                .map(route -> String.valueOf(route.get("physicalHotkeyEffect")))
                .toList();
        List<String> physicalHotkeySurfaces = sequence.stream()
                .map(route -> String.valueOf(route.get("physicalHotkeySurface")))
                .toList();
        boolean physicalHotkeyAccepted = sequence.stream()
                .allMatch(route -> Boolean.TRUE.equals(route.get("physicalHotkeyAccepted")));
        boolean physicalPollerExecuted = sequence.stream()
                .allMatch(route -> Boolean.TRUE.equals(route.get("physicalPollerExecuted")));
        boolean accepted = scheduled
                && executed
                && noScreenCrash
                && surfaces.equals(REQUIRED_SURFACES)
                && hotkeys.equals(REQUIRED_HOTKEYS)
                && physicalHotkeyAccepted
                && physicalPollerExecuted
                && physicalHotkeySurfaces.equals(REQUIRED_SURFACES)
                && sequence.stream().allMatch(EchoAgent5LiveClientPhase5RouteSequenceAcceptance::routeAccepted);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("scheduled", scheduled);
        result.put("executed", executed);
        result.put("routeCount", sequence.size());
        result.put("surfaces", surfaces);
        result.put("routeTypes", routeTypes);
        result.put("hotkeys", hotkeys);
        result.put("physicalHotkeyAccepted", physicalHotkeyAccepted);
        result.put("physicalPollerExecuted", physicalPollerExecuted);
        result.put("physicalHotkeySurfaces", physicalHotkeySurfaces);
        result.put("physicalHotkeyEffects", physicalHotkeyEffects);
        result.put("noScreenCrash", noScreenCrash);
        result.put("effect", accepted
                ? "live_client_phase5_route_sequence:accepted:" + sequence.size()
                : "live_client_phase5_route_sequence:rejected:" + sequence.size());
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    public static Map<String, Object> smoke() {
        List<Map<String, Object>> routes = REQUIRED_ROUTES.stream()
                .map(EchoAgent5LiveClientPhase5RouteSequenceAcceptance::route)
                .toList();
        Map<String, Object> accepted = assess(true, true, routes, true);
        Map<String, Object> rejectedWrongOrder = assess(true, true, List.of(
                route(REQUIRED_ROUTES.get(1)),
                route(REQUIRED_ROUTES.get(0)),
                route(REQUIRED_ROUTES.get(2)),
                route(REQUIRED_ROUTES.get(3)),
                route(REQUIRED_ROUTES.get(4)),
                route(REQUIRED_ROUTES.get(5)),
                route(REQUIRED_ROUTES.get(6)),
                route(REQUIRED_ROUTES.get(7)),
                route(REQUIRED_ROUTES.get(8)),
                route(REQUIRED_ROUTES.get(9)),
                route(REQUIRED_ROUTES.get(10)),
                route(REQUIRED_ROUTES.get(11)),
                route(REQUIRED_ROUTES.get(12)),
                route(REQUIRED_ROUTES.get(13)),
                route(REQUIRED_ROUTES.get(14)),
                route(REQUIRED_ROUTES.get(15)),
                route(REQUIRED_ROUTES.get(16))
        ), true);
        List<Map<String, Object>> wrongTypeRoutes = new java.util.ArrayList<>(routes);
        wrongTypeRoutes.set(12, route(new Route("X", "ASHFALL_DRONE", "screen")));
        Map<String, Object> rejectedWrongRouteType = assess(true, true, wrongTypeRoutes, true);
        List<Map<String, Object>> wrongHotkeyRoutes = new java.util.ArrayList<>(routes);
        wrongHotkeyRoutes.set(6, Map.of(
                "surface", "HOLOMAP",
                "hotkey", "X",
                "routeType", "screen",
                "physicalHotkeyAccepted", true,
                "physicalPollerExecuted", true,
                "physicalHotkeySurface", "ASHFALL_DRONE",
                "physicalHotkeyEffect", "physical_hotkey:X->ASHFALL_DRONE:ashfall.drone_recall",
                "routeAccepted", true,
                "renderAccepted", true
        ));
        Map<String, Object> rejectedWrongHotkey = assess(true, true, wrongHotkeyRoutes, true);
        List<Map<String, Object>> noPhysicalHotkeyRoutes = new java.util.ArrayList<>(routes);
        noPhysicalHotkeyRoutes.set(5, Map.of(
                "surface", "LENS",
                "hotkey", "Q",
                "routeType", "screen",
                "physicalHotkeyAccepted", false,
                "physicalPollerExecuted", true,
                "physicalHotkeySurface", "",
                "physicalHotkeyEffect", "physical_hotkey:none",
                "routeAccepted", true,
                "renderAccepted", true
        ));
        Map<String, Object> rejectedNoPhysicalHotkey = assess(true, true, noPhysicalHotkeyRoutes, true);
        Map<String, Object> rejectedCrash = assess(true, true, routes, false);
        return Map.of(
                "liveClientPhase5RouteSequenceAcceptanceSmokeClass",
                EchoAgent5LiveClientPhase5RouteSequenceAcceptance.class.getSimpleName(),
                "accepted", accepted,
                "rejectedWrongOrder", rejectedWrongOrder,
                "rejectedWrongRouteType", rejectedWrongRouteType,
                "rejectedWrongHotkey", rejectedWrongHotkey,
                "rejectedNoPhysicalHotkey", rejectedNoPhysicalHotkey,
                "rejectedCrash", rejectedCrash,
                "passed", Boolean.TRUE.equals(accepted.get("accepted"))
                        && Boolean.FALSE.equals(rejectedWrongOrder.get("accepted"))
                        && Boolean.FALSE.equals(rejectedWrongRouteType.get("accepted"))
                        && Boolean.FALSE.equals(rejectedWrongHotkey.get("accepted"))
                        && Boolean.FALSE.equals(rejectedNoPhysicalHotkey.get("accepted"))
                        && Boolean.FALSE.equals(rejectedCrash.get("accepted")),
                "adapterCoreBridge", true,
                "serviceCodeExecuted", true
        );
    }

    private static Map<String, Object> route(Route route) {
        Map<String, Object> physicalHotkey = physicalHotkey(route.hotkey());
        return Map.of(
                "surface", route.surface(),
                "hotkey", route.hotkey(),
                "routeType", route.routeType(),
                "physicalHotkeyAccepted", Boolean.TRUE.equals(physicalHotkey.get("handled")),
                "physicalPollerExecuted", Boolean.TRUE.equals(physicalHotkey.get("serviceCodeExecuted")),
                "physicalHotkeySurface", String.valueOf(physicalHotkey.get("surface")),
                "physicalHotkeyEffect", String.valueOf(physicalHotkey.get("effect")),
                "routeAccepted", true,
                "renderAccepted", true
        );
    }

    private static boolean routeAccepted(Map<String, Object> route) {
        String surface = String.valueOf(route.get("surface"));
        String routeType = String.valueOf(route.get("routeType"));
        String hotkey = String.valueOf(route.get("hotkey"));
        String physicalHotkeySurface = String.valueOf(route.get("physicalHotkeySurface"));
        String physicalHotkeyEffect = String.valueOf(route.get("physicalHotkeyEffect"));
        Route expected = routeFor(hotkey);
        boolean expectedRouteType = expected != null && expected.routeType().equals(routeType);
        return expected != null
                && expected.surface().equals(surface)
                && Boolean.TRUE.equals(route.get("physicalHotkeyAccepted"))
                && Boolean.TRUE.equals(route.get("physicalPollerExecuted"))
                && surface.equals(physicalHotkeySurface)
                && physicalHotkeyEffect(hotkey, surface).equals(physicalHotkeyEffect)
                && expectedRouteType
                && Boolean.TRUE.equals(route.get("routeAccepted"))
                && Boolean.TRUE.equals(route.get("renderAccepted"));
    }

    private static Map<String, Object> physicalHotkey(String key) {
        Map<String, Boolean> current = new LinkedHashMap<>(EchoAgent5PhysicalHotkeyPoller.emptyState());
        current.put(key, true);
        return EchoAgent5PhysicalHotkeyPoller.poll(
                EchoAgent5PhysicalHotkeyPoller.emptyState(),
                Map.copyOf(current)
        );
    }

    private static Route routeFor(String hotkey) {
        return REQUIRED_ROUTES.stream()
                .filter(route -> route.hotkey().equals(hotkey))
                .findFirst()
                .orElse(null);
    }

    private static String physicalHotkeyEffect(String hotkey, String surface) {
        return "physical_hotkey:" + hotkey + "->" + surface + ":" + actionFor(hotkey);
    }

    private static String actionFor(String hotkey) {
        return switch (hotkey) {
            case "M" -> "terminal.open";
            case "G" -> "index.catalog";
            case "R" -> "index.recipe";
            case "U" -> "index.usage";
            case "B" -> "index.bookmark";
            case "LEFT_ALT" -> "lens.deep_scan";
            case "J" -> "holomap.open";
            case "K" -> "holomap.toggle_minimap";
            case "RIGHT_BRACKET" -> "holomap.zoom_in";
            case "LEFT_BRACKET" -> "holomap.zoom_out";
            case "BACKSLASH" -> "holomap.cycle_corner";
            case "N" -> "signalos.terminal";
            case "X" -> "ashfall.drone_recall";
            case "C" -> "ashfall.drone_scan";
            case "Y" -> "ashfall.drone_scout";
            case "Z" -> "ashfall.drone_status";
            case "ESCAPE" -> "pause.toggle";
            default -> "";
        };
    }

    private record Route(String hotkey, String surface, String routeType) {
    }
}
