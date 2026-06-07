package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5HotkeyBridgeSmoke {
    private EchoAgent5HotkeyBridgeSmoke() {
    }

    public static Map<String, Object> capture(
            boolean clientUiHostAttached,
            boolean hudOverlayRendered,
            String screenClass,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources dataSources
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        List<Map<String, Object>> steps = new ArrayList<>();
        steps.add(routeStep("M", "TERMINAL"));
        steps.add(routeStep("G", "INDEX"));
        steps.add(routeStep("R", "INDEX"));
        steps.add(routeStep("U", "INDEX"));
        steps.add(routeStep("B", "INDEX", "INDEX"));
        steps.add(routeStep("LEFT_ALT", "LENS"));
        steps.add(routeStep("J", "HOLOMAP"));
        steps.add(routeStep("K", "HOLOMAP"));
        steps.add(routeStep("RIGHT_BRACKET", "HOLOMAP"));
        steps.add(routeStep("LEFT_BRACKET", "HOLOMAP"));
        steps.add(routeStep("BACKSLASH", "HOLOMAP"));
        steps.add(routeStep("N", "SIGNALOS"));
        steps.add(droneStep("X"));
        steps.add(droneStep("C"));
        steps.add(droneStep("Y"));
        steps.add(droneStep("Z"));
        steps.add(droneStep("B"));
        steps.add(routeStep("ESCAPE", "PAUSE"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("hotkeyBridgeSmokeClass", EchoAgent5HotkeyBridgeSmoke.class.getSimpleName());
        smoke.put("screenClass", screenClass);
        smoke.put("steps", List.copyOf(steps));
        smoke.put("hotkeys", List.of(
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
                "X",
                "C",
                "Y",
                "Z",
                "ESCAPE"
        ));
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", steps.stream().allMatch(step -> Boolean.TRUE.equals(step.get("passed"))));
        return Map.copyOf(smoke);
    }

    private static Map<String, Object> routeStep(String key, String expectedMode) {
        return routeStep(key, "TERMINAL", expectedMode);
    }

    private static Map<String, Object> routeStep(String key, String mode, String expectedMode) {
        Map<String, Object> route = EchoAgent5UiActionRouter.routeKey(key, mode, EchoAgent5UiReference.WIKI_SCREEN);
        boolean passed = Boolean.TRUE.equals(route.get("handled"))
                && expectedMode.equals(route.get("destinationMode"));
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("key", key);
        step.put("mode", mode);
        step.put("expectedMode", expectedMode);
        step.put("destinationMode", route.get("destinationMode"));
        step.put("effect", route.get("effect"));
        step.put("routerClass", route.get("routerClass"));
        step.put("passed", passed);
        return Map.copyOf(step);
    }

    private static Map<String, Object> droneStep(String key) {
        Map<String, Object> route = EchoAgent5UiActionRouter.routeKey(key, "TERMINAL", EchoAgent5UiReference.WIKI_SCREEN);
        boolean passed = Boolean.TRUE.equals(route.get("handled"))
                && "ASHFALL_DRONE".equals(route.get("destinationMode"))
                && key.equals(route.get("ashfallDroneKey"))
                && ("route:ashfall_drone:" + key).equals(route.get("effect"));
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("key", key);
        step.put("mode", "TERMINAL");
        step.put("expectedMode", "ASHFALL_DRONE");
        step.put("destinationMode", route.get("destinationMode"));
        step.put("effect", route.get("effect"));
        step.put("ashfallDroneKey", route.get("ashfallDroneKey"));
        step.put("routerClass", route.get("routerClass"));
        step.put("passed", passed);
        return Map.copyOf(step);
    }
}
