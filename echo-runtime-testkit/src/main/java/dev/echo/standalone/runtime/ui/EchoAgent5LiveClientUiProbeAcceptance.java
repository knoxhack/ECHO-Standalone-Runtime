package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5LiveClientUiProbeAcceptance {
    private static final List<String> REQUIRED_SURFACES = List.of(
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

    private EchoAgent5LiveClientUiProbeAcceptance() {
    }

    public static Map<String, Object> assess(boolean scheduled, boolean executed, List<Map<String, Object>> routes) {
        List<Map<String, Object>> probeRoutes = routes == null ? List.of() : routes;
        List<String> surfaces = probeRoutes.stream()
                .map(route -> String.valueOf(route.get("surface")))
                .toList();
        boolean accepted = scheduled
                && executed
                && surfaces.equals(REQUIRED_SURFACES)
                && probeRoutes.stream().allMatch(route -> Boolean.TRUE.equals(route.get("liveSurfaceAccepted"))
                && Boolean.TRUE.equals(route.get("liveSurfaceRendered")));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("scheduled", scheduled);
        result.put("executed", executed);
        result.put("routeCount", probeRoutes.size());
        result.put("surfaces", surfaces);
        result.put("effect", accepted
                ? "live_client_ui_probe:accepted:" + probeRoutes.size()
                : "live_client_ui_probe:rejected:" + probeRoutes.size());
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    public static Map<String, Object> smoke() {
        List<Map<String, Object>> routes = REQUIRED_SURFACES.stream()
                .map(surface -> Map.<String, Object>of(
                        "surface", surface,
                        "liveSurfaceAccepted", true,
                        "liveSurfaceRendered", true
                ))
                .toList();
        Map<String, Object> accepted = assess(true, true, routes);
        Map<String, Object> rejectedNotExecuted = assess(true, false, routes);
        Map<String, Object> rejectedMissingSurface = assess(true, true, routes.subList(0, routes.size() - 1));
        return Map.of(
                "liveClientUiProbeAcceptanceSmokeClass",
                EchoAgent5LiveClientUiProbeAcceptance.class.getSimpleName(),
                "accepted", accepted,
                "rejectedNotExecuted", rejectedNotExecuted,
                "rejectedMissingSurface", rejectedMissingSurface,
                "passed", Boolean.TRUE.equals(accepted.get("accepted"))
                        && Boolean.FALSE.equals(rejectedNotExecuted.get("accepted"))
                        && Boolean.FALSE.equals(rejectedMissingSurface.get("accepted")),
                "adapterCoreBridge", true,
                "serviceCodeExecuted", true
        );
    }
}
