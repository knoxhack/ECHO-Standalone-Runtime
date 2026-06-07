package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5ScreenStackSmoke {
    private EchoAgent5ScreenStackSmoke() {
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
        EchoScreenStack stack = new EchoScreenStack();
        ArrayList<String> events = new ArrayList<>();
        ArrayList<String> currentModes = new ArrayList<>();
        ArrayList<Integer> stackSizes = new ArrayList<>();
        ArrayList<String> routeFocusPaths = new ArrayList<>();
        ArrayList<String> screenTitles = new ArrayList<>();
        ArrayList<Boolean> hostExecutions = new ArrayList<>();

        push(stack, "MAIN_MENU", events, currentModes, stackSizes);
        renderCurrent(stack, "WIKI", packId, moduleCount, itemCount, missionCount, regionCount, source,
                screenTitles, hostExecutions);

        pushRoute(stack, "M", events, currentModes, stackSizes, routeFocusPaths);
        renderCurrent(stack, "WIKI", packId, moduleCount, itemCount, missionCount, regionCount, source,
                screenTitles, hostExecutions);
        pushRoute(stack, "G", events, currentModes, stackSizes, routeFocusPaths);
        pushRoute(stack, "LEFT_ALT", events, currentModes, stackSizes, routeFocusPaths);

        Map<String, Object> pauseRoute = EchoAgent5UiActionRouter.routeKey("ESCAPE", currentMode(stack), EchoAgent5UiReference.WIKI_SCREEN);
        push(stack, String.valueOf(pauseRoute.get("destinationMode")), events, currentModes, stackSizes);
        routeFocusPaths.add(EchoAgent5UiActionRouter.focusPath(currentMode(stack), "LENS"));
        renderCurrent(stack, "LENS", packId, moduleCount, itemCount, missionCount, regionCount, source,
                screenTitles, hostExecutions);

        String poppedPause = pop(stack, events, currentModes, stackSizes);
        String resumeMode = currentMode(stack);
        replace(stack, "SETTINGS", events, currentModes, stackSizes);
        renderCurrent(stack, EchoAgent5UiReference.WIKI_SCREEN, packId, moduleCount, itemCount, missionCount, regionCount, source,
                screenTitles, hostExecutions);
        replace(stack, "LENS", events, currentModes, stackSizes);

        pushDirect(stack, "RECOVERY", events, currentModes, stackSizes, routeFocusPaths);
        renderCurrent(stack, EchoAgent5UiReference.WIKI_SCREEN, packId, moduleCount, itemCount, missionCount, regionCount, source,
                screenTitles, hostExecutions);
        String poppedRecovery = pop(stack, events, currentModes, stackSizes);

        pushDirect(stack, "MAIN_MENU", events, currentModes, stackSizes, routeFocusPaths);
        renderCurrent(stack, EchoAgent5UiReference.WIKI_SCREEN, packId, moduleCount, itemCount, missionCount, regionCount, source,
                screenTitles, hostExecutions);

        while (stack.size() > 0) {
            pop(stack, events, currentModes, stackSizes);
        }
        boolean emptyPopSafe = pop(stack, events, currentModes, stackSizes).isBlank();
        push(stack, "MAIN_MENU", events, currentModes, stackSizes);

        boolean passed = events.containsAll(List.of(
                        "push:MAIN_MENU",
                        "push:TERMINAL",
                        "push:INDEX",
                        "push:LENS",
                        "push:PAUSE",
                        "pop:PAUSE",
                        "replace:SETTINGS",
                        "replace:LENS",
                        "push:RECOVERY",
                        "pop:RECOVERY",
                        "push:MAIN_MENU",
                        "empty-pop"
                ))
                && "PAUSE".equals(poppedPause)
                && "LENS".equals(resumeMode)
                && "RECOVERY".equals(poppedRecovery)
                && emptyPopSafe
                && currentModes.containsAll(List.of("MAIN_MENU", "TERMINAL", "INDEX", "LENS", "PAUSE", "SETTINGS", "RECOVERY"))
                && routeFocusPaths.containsAll(List.of("terminal:input", "index:search", "lens:scan", "pause:resume:LENS", "recovery:recover"))
                && screenTitles.containsAll(List.of("ECHO NATIVE // MAIN_MENU", "ECHO NATIVE // PAUSE", "ECHO NATIVE // RECOVERY"))
                && hostExecutions.stream().allMatch(Boolean.TRUE::equals)
                && stack.size() == 1
                && "MAIN_MENU".equals(currentMode(stack));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("screenStackSmokeClass", EchoAgent5ScreenStackSmoke.class.getSimpleName());
        smoke.put("screenClass", screenClass);
        smoke.put("events", List.copyOf(events));
        smoke.put("currentModes", List.copyOf(currentModes));
        smoke.put("stackSizes", List.copyOf(stackSizes));
        smoke.put("routeFocusPaths", List.copyOf(routeFocusPaths));
        smoke.put("screenTitles", List.copyOf(screenTitles));
        smoke.put("resumeMode", resumeMode);
        smoke.put("emptyPopSafe", emptyPopSafe);
        smoke.put("finalCurrentMode", currentMode(stack));
        smoke.put("finalStackSize", stack.size());
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static void pushRoute(
            EchoScreenStack stack,
            String key,
            List<String> events,
            List<String> currentModes,
            List<Integer> stackSizes,
            List<String> routeFocusPaths
    ) {
        Map<String, Object> route = EchoAgent5UiActionRouter.routeKey(key, currentMode(stack), EchoAgent5UiReference.WIKI_SCREEN);
        String destination = String.valueOf(route.get("destinationMode"));
        EchoScreenRoute screenRoute = new EchoScreenRoute(screenId(destination), "route:" + key, EchoAgent5UiActionRouter.focusPath(destination, EchoAgent5UiReference.WIKI_SCREEN));
        push(stack, destination, events, currentModes, stackSizes);
        routeFocusPaths.add(screenRoute.focusPath());
    }

    private static void pushDirect(
            EchoScreenStack stack,
            String mode,
            List<String> events,
            List<String> currentModes,
            List<Integer> stackSizes,
            List<String> routeFocusPaths
    ) {
        push(stack, mode, events, currentModes, stackSizes);
        routeFocusPaths.add(EchoAgent5UiActionRouter.focusPath(mode, EchoAgent5UiReference.WIKI_SCREEN));
    }

    private static void renderCurrent(
            EchoScreenStack stack,
            String previousMode,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources dataSources,
            List<String> screenTitles,
            List<Boolean> hostExecutions
    ) {
        Map<String, Object> hostModel = EchoAgent5UiScreenHostModel.render(
                currentMode(stack),
                Map.of(
                        "previousMode", previousMode,
                        "focusedControl", EchoAgent5UiActionRouter.focusPath(currentMode(stack), previousMode),
                        "terminalBuffer", dataSources.terminalCommand(),
                        "indexBuffer", dataSources.indexQuery(),
                        "mouseRouted", true
                ),
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                dataSources
        );
        screenTitles.add(String.valueOf(hostModel.get("screenTitle")));
        hostExecutions.add(Boolean.TRUE.equals(hostModel.get("serviceCodeExecuted")));
    }

    private static void push(EchoScreenStack stack, String mode, List<String> events, List<String> currentModes, List<Integer> stackSizes) {
        stack.push(screen(mode));
        events.add("push:" + mode);
        currentModes.add(currentMode(stack));
        stackSizes.add(stack.size());
    }

    private static String pop(EchoScreenStack stack, List<String> events, List<String> currentModes, List<Integer> stackSizes) {
        String removed = stack.pop().map(EchoAgent5ScreenStackSmoke::mode).orElse("");
        events.add(removed.isBlank() ? "empty-pop" : "pop:" + removed);
        currentModes.add(currentMode(stack));
        stackSizes.add(stack.size());
        return removed;
    }

    private static void replace(EchoScreenStack stack, String mode, List<String> events, List<String> currentModes, List<Integer> stackSizes) {
        stack.replace(screen(mode));
        events.add("replace:" + mode);
        currentModes.add(currentMode(stack));
        stackSizes.add(stack.size());
    }

    private static EchoScreen screen(String mode) {
        return new EchoStaticScreen(screenId(mode), "ECHO NATIVE // " + mode, List.of(mode), EchoAgent5UiActionRouter.focusPath(mode, EchoAgent5UiReference.WIKI_SCREEN));
    }

    private static String currentMode(EchoScreenStack stack) {
        return stack.current().map(EchoAgent5ScreenStackSmoke::mode).orElse("");
    }

    private static String mode(EchoScreen screen) {
        return switch (screen.id()) {
            case EchoAgent5UiReference.MAIN_MENU_SCREEN -> "MAIN_MENU";
            case EchoAgent5UiReference.TERMINAL_SCREEN -> "TERMINAL";
            case EchoAgent5UiReference.INDEX_SCREEN -> "INDEX";
            case EchoAgent5UiReference.LENS_SCREEN -> "LENS";
            case EchoAgent5UiReference.MISSION_LOG_SCREEN -> "MISSION_LOG";
            case EchoAgent5UiReference.SETTINGS_SCREEN -> "SETTINGS";
            case EchoAgent5UiReference.PAUSE_FLOW_SCREEN -> "PAUSE";
            case EchoAgent5UiReference.DEATH_RECOVERY_SCREEN -> "RECOVERY";
            case EchoAgent5UiReference.HOLOMAP_SCREEN -> "HOLOMAP";
            case EchoAgent5UiReference.WIKI_SCREEN -> "WIKI";
            default -> "";
        };
    }

    private static String screenId(String mode) {
        return switch (mode) {
            case "MAIN_MENU" -> EchoAgent5UiReference.MAIN_MENU_SCREEN;
            case "TERMINAL" -> EchoAgent5UiReference.TERMINAL_SCREEN;
            case "INDEX" -> EchoAgent5UiReference.INDEX_SCREEN;
            case "LENS" -> EchoAgent5UiReference.LENS_SCREEN;
            case "MISSION_LOG" -> EchoAgent5UiReference.MISSION_LOG_SCREEN;
            case "SETTINGS" -> EchoAgent5UiReference.SETTINGS_SCREEN;
            case "PAUSE" -> EchoAgent5UiReference.PAUSE_FLOW_SCREEN;
            case "RECOVERY" -> EchoAgent5UiReference.DEATH_RECOVERY_SCREEN;
            case "HOLOMAP" -> EchoAgent5UiReference.HOLOMAP_SCREEN;
            case "WIKI" -> EchoAgent5UiReference.WIKI_SCREEN;
            default -> EchoAgent5UiReference.MAIN_MENU_SCREEN;
        };
    }
}
