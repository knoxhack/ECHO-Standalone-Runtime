package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5PhysicalHotkeyPollingSmoke {
    private static final List<Route> ROUTES = List.of(
            new Route("M", "TERMINAL", "terminal.open"),
            new Route("G", "INDEX", "index.catalog"),
            new Route("R", "INDEX", "index.recipe"),
            new Route("U", "INDEX", "index.usage"),
            new Route("B", "INDEX", "index.bookmark"),
            new Route("LEFT_ALT", "LENS", "lens.deep_scan"),
            new Route("J", "HOLOMAP", "holomap.open"),
            new Route("K", "HOLOMAP", "holomap.toggle_minimap"),
            new Route("RIGHT_BRACKET", "HOLOMAP", "holomap.zoom_in"),
            new Route("LEFT_BRACKET", "HOLOMAP", "holomap.zoom_out"),
            new Route("BACKSLASH", "HOLOMAP", "holomap.cycle_corner"),
            new Route("N", "SIGNALOS", "signalos.terminal"),
            new Route("ESCAPE", "PAUSE", "pause.toggle"),
            new Route("X", "ASHFALL_DRONE", "ashfall.drone_recall"),
            new Route("C", "ASHFALL_DRONE", "ashfall.drone_scan"),
            new Route("Y", "ASHFALL_DRONE", "ashfall.drone_scout"),
            new Route("Z", "ASHFALL_DRONE", "ashfall.drone_status")
    );

    private EchoAgent5PhysicalHotkeyPollingSmoke() {
    }

    public static Map<String, Object> capture() {
        Map<String, Boolean> empty = EchoAgent5PhysicalHotkeyPoller.emptyState();
        List<Map<String, Object>> routedEvents = ROUTES.stream()
                .map(route -> EchoAgent5PhysicalHotkeyPoller.poll(empty, pressed(route.key())))
                .toList();
        Map<String, Object> repeat = EchoAgent5PhysicalHotkeyPoller.poll(pressed("M"), pressed("M"));
        boolean passed = Boolean.FALSE.equals(repeat.get("handled"));
        for (int i = 0; i < ROUTES.size(); i++) {
            passed = passed && matches(routedEvents.get(i), ROUTES.get(i));
        }
        Map<String, Object> bookmark = routedEvents.stream()
                .filter(event -> "B".equals(event.get("key")))
                .findFirst()
                .orElse(Map.of());
        passed = passed
                && Boolean.TRUE.equals(bookmark.get("contextual"))
                && "ASHFALL_DRONE".equals(bookmark.get("alternateSurface"))
                && "ashfall.drone_assist".equals(bookmark.get("alternateAction"))
                && "echoindex.bookmark/echoashfallprotocol.drone_assist".equals(bookmark.get("sourceConflict"));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("physicalHotkeyPollingSmokeClass", EchoAgent5PhysicalHotkeyPollingSmoke.class.getSimpleName());
        smoke.put("events", routedEvents);
        smoke.put("repeatEvent", repeat);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static boolean matches(Map<String, Object> event, Route route) {
        return Boolean.TRUE.equals(event.get("handled"))
                && route.key().equals(event.get("key"))
                && route.surface().equals(event.get("surface"))
                && route.action().equals(event.get("action"))
                && ("physical_hotkey:" + route.key() + "->" + route.surface() + ":" + route.action())
                .equals(event.get("effect"));
    }

    private static Map<String, Boolean> pressed(String key) {
        Map<String, Boolean> state = new LinkedHashMap<>(EchoAgent5PhysicalHotkeyPoller.emptyState());
        state.put(key, true);
        return Map.copyOf(state);
    }

    private record Route(String key, String surface, String action) {
    }
}
