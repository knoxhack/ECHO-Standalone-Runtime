package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5MouseActivationSmoke {
    private EchoAgent5MouseActivationSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;

        Map<String, Object> focusOnlyClick = EchoAgent5UiActionRouter.routeMouseClick(
                "TERMINAL",
                EchoAgent5UiReference.WIKI_SCREEN,
                Map.of(),
                source
        );
        Map<String, Object> terminalClick = EchoAgent5UiActionRouter.routeMouseClick(
                "TERMINAL",
                EchoAgent5UiReference.WIKI_SCREEN,
                Map.of("terminalBuffer", source.terminalCommand()),
                source
        );
        Map<String, Object> indexClick = EchoAgent5UiActionRouter.routeMouseClick(
                "INDEX",
                EchoAgent5UiReference.WIKI_SCREEN,
                Map.of("indexBuffer", source.indexQuery()),
                source
        );
        Map<String, Object> lensClick = EchoAgent5UiActionRouter.routeMouseClick(
                "LENS",
                EchoAgent5UiReference.WIKI_SCREEN,
                Map.of(),
                source
        );
        Map<String, Object> recoveryClick = EchoAgent5UiActionRouter.routeMouseClick(
                "RECOVERY",
                EchoAgent5UiReference.WIKI_SCREEN,
                Map.of(),
                source
        );

        List<String> focusPaths = List.of(
                String.valueOf(focusOnlyClick.get("focusedControl")),
                String.valueOf(terminalClick.get("focusedControl")),
                String.valueOf(indexClick.get("focusedControl")),
                String.valueOf(lensClick.get("focusedControl")),
                String.valueOf(recoveryClick.get("focusedControl"))
        );
        List<String> clickEffects = List.of(
                String.valueOf(focusOnlyClick.get("effect")),
                String.valueOf(terminalClick.get("effect")),
                String.valueOf(indexClick.get("effect")),
                String.valueOf(lensClick.get("effect")),
                String.valueOf(recoveryClick.get("effect"))
        );
        List<String> executedKeys = List.of(
                String.valueOf(terminalClick.get("executedKey")),
                String.valueOf(indexClick.get("executedKey")),
                String.valueOf(lensClick.get("executedKey")),
                String.valueOf(recoveryClick.get("executedKey"))
        );

        ArrayList<String> renderedLines = new ArrayList<>();
        renderedLines.addAll(EchoAgent5UiSurfaceRenderer.render(
                "TERMINAL",
                stateWithAction("terminalBuffer", source.terminalCommand(), terminalClick),
                source
        ).lines());
        renderedLines.addAll(EchoAgent5UiSurfaceRenderer.render(
                "INDEX",
                stateWithAction("indexBuffer", source.indexQuery(), indexClick),
                source
        ).lines());
        renderedLines.addAll(EchoAgent5UiSurfaceRenderer.render("LENS", stateWithAction(null, null, lensClick), source).lines());
        renderedLines.addAll(EchoAgent5UiSurfaceRenderer.render(
                "RECOVERY",
                stateWithAction(null, null, recoveryClick),
                source
        ).lines());

        boolean passed = Boolean.TRUE.equals(focusOnlyClick.get("handled"))
                && !focusOnlyClick.containsKey("executedKey")
                && focusPaths.containsAll(List.of(
                        "terminal:input",
                        "index:search",
                        "lens:scan",
                        "recovery:recover"
                ))
                && clickEffects.containsAll(List.of(
                        "mouse:focus:terminal",
                        "mouse:activate:terminal",
                        "mouse:activate:index",
                        "mouse:activate:lens",
                        "mouse:activate:recovery"
                ))
                && executedKeys.containsAll(List.of(
                        "terminalCommandExecuted",
                        "indexSearchExecuted",
                        "lensScanExecuted",
                        "recoveryActionExecuted"
                ))
                && renderedLines.stream().anyMatch(line -> line.contains(source.terminalReadyLine()))
                && renderedLines.stream().anyMatch(line -> line.contains(source.indexResult()))
                && renderedLines.stream().anyMatch(line -> line.contains("Scan: " + source.lensResult()))
                && renderedLines.stream().anyMatch(line -> line.contains("Status: RECOVERED"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("mouseActivationSmokeClass", EchoAgent5MouseActivationSmoke.class.getSimpleName());
        smoke.put("serviceCodeExecuted", true);
        smoke.put("adapterCoreBridge", true);
        smoke.put("focusPaths", focusPaths);
        smoke.put("clickEffects", clickEffects);
        smoke.put("executedKeys", executedKeys);
        smoke.put("renderedLines", List.copyOf(renderedLines));
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static Map<String, Object> stateWithAction(String bufferKey, Object bufferValue, Map<String, Object> click) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("focusedControl", click.get("focusedControl"));
        state.put("mouseRouted", click.get("mouseRouted"));
        if (bufferKey != null) {
            state.put(bufferKey, bufferValue);
        }
        if (click.containsKey("outputKey")) {
            state.put(String.valueOf(click.get("outputKey")), click.get("output"));
        }
        if (click.containsKey("executedKey")) {
            state.put(String.valueOf(click.get("executedKey")), true);
        }
        return Map.copyOf(state);
    }
}
