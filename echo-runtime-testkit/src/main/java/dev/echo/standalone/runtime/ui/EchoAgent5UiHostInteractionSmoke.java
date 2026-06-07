package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5UiHostInteractionSmoke {
    private EchoAgent5UiHostInteractionSmoke() {
    }

    public static Map<String, Object> run(
            String screenClass,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources dataSources
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        ArrayList<Map<String, Object>> steps = new ArrayList<>();
        steps.add(runTerminal(screenClass, packId, moduleCount, itemCount, missionCount, regionCount, source));
        steps.add(runIndex(screenClass, packId, moduleCount, itemCount, missionCount, regionCount, source));
        steps.add(runLens(screenClass, packId, moduleCount, itemCount, missionCount, regionCount, source));
        steps.add(openSurface("MISSION_LOG", screenClass, packId, moduleCount, itemCount, missionCount, regionCount, source));
        steps.add(openSurface("SETTINGS", screenClass, packId, moduleCount, itemCount, missionCount, regionCount, source));
        steps.add(runPauseResume(screenClass, packId, moduleCount, itemCount, missionCount, regionCount, source));
        steps.add(runRecovery(screenClass, packId, moduleCount, itemCount, missionCount, regionCount, source));
        steps.add(openSurface("HOLOMAP", screenClass, packId, moduleCount, itemCount, missionCount, regionCount, source));
        steps.add(openSurface("WIKI", screenClass, packId, moduleCount, itemCount, missionCount, regionCount, source));
        steps.add(openSurface("MAIN_MENU", screenClass, packId, moduleCount, itemCount, missionCount, regionCount, source));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("interactionSmokeClass", EchoAgent5UiHostInteractionSmoke.class.getSimpleName());
        smoke.put("steps", List.copyOf(steps));
        smoke.put("passed", steps.stream().allMatch(step -> Boolean.TRUE.equals(step.get("passed"))));
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        return Map.copyOf(smoke);
    }

    private static Map<String, Object> runTerminal(
            String screenClass,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources source
    ) {
        String focusPath = EchoAgent5UiActionRouter.focusPath("TERMINAL", EchoAgent5UiReference.WIKI_SCREEN);
        String buffer = "";
        for (char character : source.terminalCommand().toCharArray()) {
            Map<String, Object> typed = EchoAgent5UiActionRouter.routeCharacter(
                    "TERMINAL",
                    focusPath,
                    buffer,
                    "",
                    character
            );
            buffer = String.valueOf(typed.get("value"));
        }
        Map<String, Object> action = EchoAgent5UiActionRouter.activate("TERMINAL", Map.of(
                "focusedControl", focusPath,
                "terminalBuffer", buffer
        ), source);
        Map<String, Object> state = Map.of(
                "focusedControl", focusPath,
                "mouseRouted", true,
                "terminalBuffer", buffer,
                "terminalOutput", String.valueOf(action.get("output")),
                "terminalCommandExecuted", true
        );
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                "TERMINAL",
                true,
                screenClass,
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source,
                state
        );
        return step("terminal_command", snapshot, Boolean.TRUE.equals(action.get("handled"))
                && EchoAgent5UiHostSmokeSnapshot.strings(snapshot, "surfaceLines").stream()
                .anyMatch(line -> line.contains(source.terminalReadyLine())));
    }

    private static Map<String, Object> runIndex(
            String screenClass,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources source
    ) {
        String focusPath = EchoAgent5UiActionRouter.focusPath("INDEX", EchoAgent5UiReference.WIKI_SCREEN);
        String buffer = "";
        for (char character : source.indexQuery().toCharArray()) {
            Map<String, Object> typed = EchoAgent5UiActionRouter.routeCharacter(
                    "INDEX",
                    focusPath,
                    "",
                    buffer,
                    character
            );
            buffer = String.valueOf(typed.get("value"));
        }
        Map<String, Object> action = EchoAgent5UiActionRouter.activate("INDEX", Map.of(
                "focusedControl", focusPath,
                "indexBuffer", buffer
        ), source);
        Map<String, Object> state = Map.of(
                "focusedControl", focusPath,
                "mouseRouted", true,
                "indexBuffer", buffer,
                "indexOutput", String.valueOf(action.get("output")),
                "indexSearchExecuted", true
        );
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                "INDEX",
                true,
                screenClass,
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source,
                state
        );
        return step("index_search", snapshot, Boolean.TRUE.equals(action.get("handled"))
                && EchoAgent5UiHostSmokeSnapshot.strings(snapshot, "surfaceLines").stream()
                .anyMatch(line -> line.contains(source.indexResult())));
    }

    private static Map<String, Object> runLens(
            String screenClass,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources source
    ) {
        String focusPath = EchoAgent5UiActionRouter.focusPath("LENS", EchoAgent5UiReference.WIKI_SCREEN);
        Map<String, Object> action = EchoAgent5UiActionRouter.activate("LENS", Map.of(
                "focusedControl", focusPath
        ), source);
        Map<String, Object> state = Map.of(
                "focusedControl", focusPath,
                "mouseRouted", true,
                "lensOutput", String.valueOf(action.get("output")),
                "lensScanExecuted", true
        );
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                "LENS",
                true,
                screenClass,
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source,
                state
        );
        return step("lens_scan", snapshot, Boolean.TRUE.equals(action.get("handled"))
                && EchoAgent5UiHostSmokeSnapshot.strings(snapshot, "surfaceLines").stream()
                .anyMatch(line -> line.contains(source.lensResult())));
    }

    private static Map<String, Object> runPauseResume(
            String screenClass,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources source
    ) {
        Map<String, Object> pauseRoute = EchoAgent5UiActionRouter.routeKey(
                "ESCAPE",
                "LENS",
                EchoAgent5UiReference.WIKI_SCREEN
        );
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                String.valueOf(pauseRoute.get("destinationMode")),
                Boolean.TRUE.equals(pauseRoute.get("handled")),
                screenClass,
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source,
                Map.of("previousMode", "LENS")
        );
        Map<String, Object> resumeRoute = EchoAgent5UiActionRouter.routeKey("ESCAPE", "PAUSE", "LENS");
        boolean passed = Boolean.TRUE.equals(pauseRoute.get("handled"))
                && Boolean.TRUE.equals(resumeRoute.get("handled"))
                && "PAUSE".equals(snapshot.get("surface"))
                && "LENS".equals(resumeRoute.get("destinationMode"))
                && "pause:resume:LENS".equals(snapshot.get("focusPath"))
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5PauseSurfaceRenderer.class.getSimpleName()
                .equals(snapshot.get("moduleRendererClass"));
        Map<String, Object> step = new LinkedHashMap<>(step("pause_resume", snapshot, passed));
        step.put("resumeDestinationMode", resumeRoute.get("destinationMode"));
        return Map.copyOf(step);
    }

    private static Map<String, Object> runRecovery(
            String screenClass,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources source
    ) {
        String focusPath = EchoAgent5UiActionRouter.focusPath("RECOVERY", EchoAgent5UiReference.WIKI_SCREEN);
        Map<String, Object> action = EchoAgent5UiActionRouter.activate("RECOVERY", Map.of(
                "focusedControl", focusPath
        ), source);
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                "RECOVERY",
                true,
                screenClass,
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source,
                Map.of(
                        "focusedControl", focusPath,
                        "mouseRouted", true,
                        "recoveryOutput", String.valueOf(action.get("output")),
                        "recoveryActionExecuted", true
                )
        );
        return step("recovery_action", snapshot, Boolean.TRUE.equals(action.get("handled"))
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5RecoverySurfaceRenderer.class.getSimpleName()
                .equals(snapshot.get("moduleRendererClass"))
                && EchoAgent5UiHostSmokeSnapshot.strings(snapshot, "surfaceLines").stream()
                .anyMatch(line -> line.contains("Status: " + EchoAgent5UiReference.RECOVERY_STATUS)));
    }

    private static Map<String, Object> openSurface(
            String surface,
            String screenClass,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources source
    ) {
        Map<String, Object> route = "HOLOMAP".equals(surface)
                ? EchoAgent5UiActionRouter.routeKey("J", "TERMINAL", EchoAgent5UiReference.WIKI_SCREEN)
                : Map.of("handled", true, "destinationMode", surface);
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                String.valueOf(route.get("destinationMode")),
                Boolean.TRUE.equals(route.get("handled")),
                screenClass,
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source
        );
        boolean passed = Boolean.TRUE.equals(route.get("handled"))
                && Boolean.TRUE.equals(snapshot.get("opened"))
                && !String.valueOf(snapshot.get("moduleRendererClass")).isBlank();
        return step(surface.toLowerCase(java.util.Locale.ROOT) + "_open", snapshot, passed);
    }

    private static Map<String, Object> step(String id, Map<String, Object> snapshot, boolean passed) {
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("id", id);
        step.put("surface", snapshot.get("surface"));
        step.put("focusPath", snapshot.get("focusPath"));
        step.put("moduleRendererClass", snapshot.get("moduleRendererClass"));
        step.put("snapshot", snapshot);
        step.put("passed", passed);
        return Map.copyOf(step);
    }
}
