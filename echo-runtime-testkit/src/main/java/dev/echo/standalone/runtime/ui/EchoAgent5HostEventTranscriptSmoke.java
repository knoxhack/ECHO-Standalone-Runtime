package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5HostEventTranscriptSmoke {
    private EchoAgent5HostEventTranscriptSmoke() {
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
        ArrayList<String> events = new ArrayList<>();
        ArrayList<String> renderedLines = new ArrayList<>();

        Map<String, Object> terminalRoute = EchoAgent5UiActionRouter.routeKey("M", EchoAgent5UiReference.WIKI_SCREEN, EchoAgent5UiReference.WIKI_SCREEN);
        events.add("key:M->" + terminalRoute.get("destinationMode"));
        Map<String, Object> terminalFocus = EchoAgent5UiActionRouter.routeInitialFocus("TERMINAL", EchoAgent5UiReference.WIKI_SCREEN);
        String terminalBuffer = "";
        for (char character : source.terminalCommand().toCharArray()) {
            Map<String, Object> typed = EchoAgent5UiActionRouter.routeCharacter(
                    "TERMINAL",
                    String.valueOf(terminalFocus.get("focusedControl")),
                    terminalBuffer,
                    "",
                    character
            );
            terminalBuffer = String.valueOf(typed.get("value"));
        }
        events.add("text:terminal:" + terminalBuffer);
        Map<String, Object> terminalAction = EchoAgent5UiActionRouter.activate("TERMINAL", Map.of(
                "focusedControl", terminalFocus.get("focusedControl"),
                "terminalBuffer", terminalBuffer
        ), source);
        events.add("enter:terminal:" + terminalAction.get("executedKey"));
        renderedLines.addAll(renderSurface("TERMINAL", state(
                terminalFocus,
                "terminalBuffer", terminalBuffer,
                "terminalOutput", String.valueOf(terminalAction.get("output")),
                "terminalCommandExecuted", true
        ), packId, moduleCount, itemCount, missionCount, regionCount, source));

        Map<String, Object> indexRoute = EchoAgent5UiActionRouter.routeKey("G", "TERMINAL", EchoAgent5UiReference.WIKI_SCREEN);
        events.add("key:G->" + indexRoute.get("destinationMode"));
        Map<String, Object> indexFocus = EchoAgent5UiActionRouter.routeInitialFocus("INDEX", EchoAgent5UiReference.WIKI_SCREEN);
        String indexBuffer = "";
        for (char character : source.indexQuery().toCharArray()) {
            Map<String, Object> typed = EchoAgent5UiActionRouter.routeCharacter(
                    "INDEX",
                    String.valueOf(indexFocus.get("focusedControl")),
                    "",
                    indexBuffer,
                    character
            );
            indexBuffer = String.valueOf(typed.get("value"));
        }
        events.add("text:index:" + indexBuffer);
        Map<String, Object> indexAction = EchoAgent5UiActionRouter.activate("INDEX", Map.of(
                "focusedControl", indexFocus.get("focusedControl"),
                "indexBuffer", indexBuffer
        ), source);
        events.add("enter:index:" + indexAction.get("executedKey"));
        renderedLines.addAll(renderSurface("INDEX", state(
                indexFocus,
                "indexBuffer", indexBuffer,
                "indexOutput", String.valueOf(indexAction.get("output")),
                "indexSearchExecuted", true
        ), packId, moduleCount, itemCount, missionCount, regionCount, source));

        Map<String, Object> lensRoute = EchoAgent5UiActionRouter.routeKey("LEFT_ALT", "INDEX", EchoAgent5UiReference.WIKI_SCREEN);
        events.add("key:LEFT_ALT->" + lensRoute.get("destinationMode"));
        Map<String, Object> lensFocus = EchoAgent5UiActionRouter.routeInitialFocus("LENS", EchoAgent5UiReference.WIKI_SCREEN);
        Map<String, Object> lensAction = EchoAgent5UiActionRouter.activate("LENS", Map.of(
                "focusedControl", lensFocus.get("focusedControl")
        ), source);
        events.add("enter:lens:" + lensAction.get("executedKey"));
        renderedLines.addAll(renderSurface("LENS", state(
                lensFocus,
                "lensOutput", String.valueOf(lensAction.get("output")),
                "lensScanExecuted", true
        ), packId, moduleCount, itemCount, missionCount, regionCount, source));

        Map<String, Object> hudUpdate = EchoAgent5UiActionRouter.routeHudUpdate(Map.of("hudHealth", 92), source);
        Map<String, Object> cameraFrame = EchoAgent5UiActionRouter.routeCameraCinematicFrame(Map.of("cinematicFrame", 0), source);
        events.add("enter:hud:" + hudUpdate.get("effect") + "+" + cameraFrame.get("effect"));
        Map<String, Object> hudState = new LinkedHashMap<>();
        hudState.putAll(hudUpdate);
        hudState.putAll(cameraFrame);
        renderedLines.addAll(renderSurface("HUD", hudState, packId, moduleCount, itemCount, missionCount, regionCount, source));

        boolean passed = Boolean.TRUE.equals(terminalAction.get("handled"))
                && Boolean.TRUE.equals(indexAction.get("handled"))
                && Boolean.TRUE.equals(lensAction.get("handled"))
                && Boolean.TRUE.equals(hudUpdate.get("handled"))
                && Boolean.TRUE.equals(cameraFrame.get("handled"))
                && renderedLines.stream().anyMatch(line -> line.contains(source.terminalReadyLine()))
                && renderedLines.stream().anyMatch(line -> line.contains(source.indexResult()))
                && renderedLines.stream().anyMatch(line -> line.contains(source.lensResult()))
                && renderedLines.stream().anyMatch(line -> line.contains("HUD overlay is live. Health 85"))
                && renderedLines.stream()
                .anyMatch(line -> line.contains("Camera over_shoulder frame 1 cue "
                        + source.cinematicValues().get("cue")));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("hostEventTranscriptSmokeClass", EchoAgent5HostEventTranscriptSmoke.class.getSimpleName());
        smoke.put("screenClass", screenClass);
        smoke.put("events", List.copyOf(events));
        smoke.put("renderedLines", List.copyOf(renderedLines));
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static List<String> renderSurface(
            String mode,
            Map<String, Object> state,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources source
    ) {
        return EchoAgent5UiHostSmokeSnapshot.strings(EchoAgent5UiScreenHostModel.render(
                mode,
                state,
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source
        ), "surfaceLines");
    }

    private static Map<String, Object> state(Map<String, Object> focus, Object... values) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("focusedControl", focus.get("focusedControl"));
        state.put("initialFocusRouted", true);
        for (int index = 0; index < values.length; index += 2) {
            state.put(String.valueOf(values[index]), values[index + 1]);
        }
        return Map.copyOf(state);
    }
}
