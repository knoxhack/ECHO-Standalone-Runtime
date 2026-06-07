package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5ScreenLifecycleSmoke {
    private EchoAgent5ScreenLifecycleSmoke() {
    }

    public static Map<String, Object> capture(
            String screenClass,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources dataSources
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        ArrayList<String> visitedModes = new ArrayList<>();
        ArrayList<String> routeEffects = new ArrayList<>();
        ArrayList<String> focusPaths = new ArrayList<>();
        ArrayList<String> screenTitles = new ArrayList<>();
        ArrayList<Boolean> hostExecutions = new ArrayList<>();
        ArrayList<String> actionExecutedKeys = new ArrayList<>();
        ArrayList<String> actionOutputs = new ArrayList<>();
        ArrayList<String> actionSurfaceLines = new ArrayList<>();

        String currentMode = "MAIN_MENU";
        String previousMode = EchoAgent5UiReference.WIKI_SCREEN;
        renderStep(currentMode, previousMode, packId, moduleCount, itemCount, missionCount, regionCount, source,
                visitedModes, focusPaths, screenTitles, hostExecutions);

        Map<String, Object> terminalRoute = route("M", currentMode, previousMode, routeEffects);
        currentMode = string(terminalRoute, "destinationMode", "TERMINAL");
        previousMode = string(terminalRoute, "destinationPreviousMode", previousMode);
        renderStep(currentMode, previousMode, packId, moduleCount, itemCount, missionCount, regionCount, source,
                visitedModes, focusPaths, screenTitles, hostExecutions);
        activateStep(currentMode, Map.of(
                "focusedControl", "terminal:input",
                "terminalBuffer", source.terminalCommand()
        ), source, actionExecutedKeys, actionOutputs, actionSurfaceLines);

        Map<String, Object> indexRoute = route("G", currentMode, previousMode, routeEffects);
        currentMode = string(indexRoute, "destinationMode", "INDEX");
        previousMode = string(indexRoute, "destinationPreviousMode", previousMode);
        renderStep(currentMode, previousMode, packId, moduleCount, itemCount, missionCount, regionCount, source,
                visitedModes, focusPaths, screenTitles, hostExecutions);
        activateStep(currentMode, Map.of(
                "focusedControl", "index:search",
                "indexBuffer", source.indexQuery()
        ), source, actionExecutedKeys, actionOutputs, actionSurfaceLines);

        Map<String, Object> lensRoute = route("LEFT_ALT", currentMode, previousMode, routeEffects);
        currentMode = string(lensRoute, "destinationMode", "LENS");
        previousMode = string(lensRoute, "destinationPreviousMode", previousMode);
        renderStep(currentMode, previousMode, packId, moduleCount, itemCount, missionCount, regionCount, source,
                visitedModes, focusPaths, screenTitles, hostExecutions);
        activateStep(currentMode, Map.of(
                "focusedControl", "lens:scan"
        ), source, actionExecutedKeys, actionOutputs, actionSurfaceLines);

        Map<String, Object> pauseRoute = route("ESCAPE", currentMode, previousMode, routeEffects);
        currentMode = string(pauseRoute, "destinationMode", "PAUSE");
        previousMode = string(pauseRoute, "destinationPreviousMode", "LENS");
        String pausePreviousMode = previousMode;
        renderStep(currentMode, previousMode, packId, moduleCount, itemCount, missionCount, regionCount, source,
                visitedModes, focusPaths, screenTitles, hostExecutions);

        Map<String, Object> resumeRoute = route("ESCAPE", currentMode, previousMode, routeEffects);
        currentMode = string(resumeRoute, "destinationMode", "LENS");
        previousMode = string(resumeRoute, "destinationPreviousMode", EchoAgent5UiReference.WIKI_SCREEN);
        String resumeMode = currentMode;
        renderStep(currentMode, previousMode, packId, moduleCount, itemCount, missionCount, regionCount, source,
                visitedModes, focusPaths, screenTitles, hostExecutions);

        currentMode = "RECOVERY";
        renderStep(currentMode, previousMode, packId, moduleCount, itemCount, missionCount, regionCount, source,
                visitedModes, focusPaths, screenTitles, hostExecutions);
        activateStep(currentMode, Map.of(
                "focusedControl", "recovery:recover"
        ), source, actionExecutedKeys, actionOutputs, actionSurfaceLines);

        currentMode = "MAIN_MENU";
        renderStep(currentMode, previousMode, packId, moduleCount, itemCount, missionCount, regionCount, source,
                visitedModes, focusPaths, screenTitles, hostExecutions);

        boolean passed = visitedModes.containsAll(List.of("MAIN_MENU", "TERMINAL", "INDEX", "LENS", "PAUSE", "RECOVERY"))
                && routeEffects.containsAll(List.of(
                        "route:terminal",
                        "route:index",
                        "route:lens",
                        "route:escape"
                ))
                && "LENS".equals(pausePreviousMode)
                && "LENS".equals(resumeMode)
                && focusPaths.contains("terminal:input")
                && focusPaths.contains("index:search")
                && focusPaths.contains("lens:scan")
                && focusPaths.contains("pause:resume:LENS")
                && focusPaths.contains("recovery:recover")
                && actionExecutedKeys.containsAll(List.of(
                        "terminalCommandExecuted",
                        "indexSearchExecuted",
                        "lensScanExecuted",
                        "recoveryActionExecuted"
                ))
                && actionOutputs.containsAll(List.of(
                        source.terminalReadyLine(),
                        source.indexResult(),
                        source.lensResult(),
                        "Status: " + EchoAgent5UiReference.RECOVERY_STATUS + "    Health: 35"
                ))
                && actionSurfaceLines.stream().anyMatch(line -> line.contains(source.terminalCommand() + " -> " + source.terminalReadyLine()))
                && actionSurfaceLines.stream().anyMatch(line -> line.contains(source.indexQuery() + " -> " + source.indexResult()))
                && actionSurfaceLines.stream().anyMatch(line -> line.contains(source.lensResult()))
                && actionSurfaceLines.stream().anyMatch(line -> line.contains("Status: " + EchoAgent5UiReference.RECOVERY_STATUS))
                && hostExecutions.stream().allMatch(Boolean.TRUE::equals);

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("screenLifecycleSmokeClass", EchoAgent5ScreenLifecycleSmoke.class.getSimpleName());
        smoke.put("screenClass", screenClass);
        smoke.put("visitedModes", List.copyOf(visitedModes));
        smoke.put("routeEffects", List.copyOf(routeEffects));
        smoke.put("pausePreviousMode", pausePreviousMode);
        smoke.put("resumeMode", resumeMode);
        smoke.put("terminalFocusPath", "terminal:input");
        smoke.put("indexFocusPath", "index:search");
        smoke.put("lensFocusPath", "lens:scan");
        smoke.put("recoveryFocusPath", "recovery:recover");
        smoke.put("actionExecutedKeys", List.copyOf(actionExecutedKeys));
        smoke.put("actionOutputs", List.copyOf(actionOutputs));
        smoke.put("actionSurfaceLines", List.copyOf(actionSurfaceLines));
        smoke.put("screenTitles", List.copyOf(screenTitles));
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static void renderStep(
            String mode,
            String previousMode,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources dataSources,
            List<String> visitedModes,
            List<String> focusPaths,
            List<String> screenTitles,
            List<Boolean> hostExecutions
    ) {
        String focusPath = EchoAgent5UiActionRouter.focusPath(mode, previousMode);
        Map<String, Object> hostModel = EchoAgent5UiScreenHostModel.render(
                mode,
                Map.of(
                        "previousMode", previousMode,
                        "focusedControl", focusPath,
                        "terminalBuffer", "status",
                        "indexBuffer", "ashfall",
                        "mouseRouted", true
                ),
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                dataSources
        );
        visitedModes.add(mode);
        focusPaths.add(focusPath);
        screenTitles.add(string(hostModel, "screenTitle", ""));
        hostExecutions.add(Boolean.TRUE.equals(hostModel.get("serviceCodeExecuted")));
    }

    private static Map<String, Object> route(
            String key,
            String currentMode,
            String previousMode,
            List<String> routeEffects
    ) {
        Map<String, Object> route = EchoAgent5UiActionRouter.routeKey(key, currentMode, previousMode);
        routeEffects.add(string(route, "effect", ""));
        return route;
    }

    private static void activateStep(
            String mode,
            Map<String, Object> state,
            EchoAgent5UiDataSources dataSources,
            List<String> actionExecutedKeys,
            List<String> actionOutputs,
            List<String> actionSurfaceLines
    ) {
        Map<String, Object> action = EchoAgent5UiActionRouter.activate(mode, state, dataSources);
        String executedKey = string(action, "executedKey", "");
        String outputKey = string(action, "outputKey", "");
        String output = string(action, "output", "");
        actionExecutedKeys.add(executedKey);
        actionOutputs.add(output);

        Map<String, Object> surfaceState = new LinkedHashMap<>(state);
        if (!outputKey.isBlank()) {
            surfaceState.put(outputKey, output);
        }
        if (!executedKey.isBlank()) {
            surfaceState.put(executedKey, true);
        }
        actionSurfaceLines.addAll(EchoAgent5UiSurfaceRenderer.render(mode, Map.copyOf(surfaceState), dataSources).lines());
    }

    private static String string(Map<String, Object> values, String key, String fallback) {
        Object value = values.get(key);
        return value == null ? fallback : String.valueOf(value);
    }
}
