package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5InitialFocusSmoke {
    private EchoAgent5InitialFocusSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> terminalFocus = EchoAgent5UiActionRouter.routeInitialFocus("TERMINAL", EchoAgent5UiReference.WIKI_SCREEN);
        Map<String, Object> indexFocus = EchoAgent5UiActionRouter.routeInitialFocus("INDEX", EchoAgent5UiReference.WIKI_SCREEN);
        Map<String, Object> lensFocus = EchoAgent5UiActionRouter.routeInitialFocus("LENS", EchoAgent5UiReference.WIKI_SCREEN);
        Map<String, Object> recoveryFocus = EchoAgent5UiActionRouter.routeInitialFocus("RECOVERY", EchoAgent5UiReference.WIKI_SCREEN);

        Map<String, Object> terminalTyped = EchoAgent5UiActionRouter.routeCharacter(
                "TERMINAL",
                String.valueOf(terminalFocus.get("focusedControl")),
                "statu",
                "",
                's'
        );
        Map<String, Object> indexTyped = EchoAgent5UiActionRouter.routeCharacter(
                "INDEX",
                String.valueOf(indexFocus.get("focusedControl")),
                "",
                "ashfal",
                'l'
        );
        Map<String, Object> lensAction = EchoAgent5UiActionRouter.activate("LENS", Map.of(
                "focusedControl", lensFocus.get("focusedControl")
        ), source);
        Map<String, Object> recoveryAction = EchoAgent5UiActionRouter.activate("RECOVERY", Map.of(
                "focusedControl", recoveryFocus.get("focusedControl")
        ), source);
        EchoUiSurface renderedTerminal = EchoAgent5UiSurfaceRenderer.render("TERMINAL", Map.of(
                "focusedControl", terminalFocus.get("focusedControl"),
                "initialFocusRouted", true
        ), source);
        EchoUiSurface renderedLens = EchoAgent5UiSurfaceRenderer.render("LENS", Map.of(
                "focusedControl", lensFocus.get("focusedControl"),
                "initialFocusRouted", true,
                "lensOutput", lensAction.get("output"),
                "lensScanExecuted", true
        ), source);

        List<String> focusPaths = List.of(
                String.valueOf(terminalFocus.get("focusedControl")),
                String.valueOf(indexFocus.get("focusedControl")),
                String.valueOf(lensFocus.get("focusedControl")),
                String.valueOf(recoveryFocus.get("focusedControl"))
        );
        List<String> effects = List.of(
                String.valueOf(terminalFocus.get("effect")),
                String.valueOf(indexFocus.get("effect")),
                String.valueOf(lensFocus.get("effect")),
                String.valueOf(recoveryFocus.get("effect"))
        );
        boolean passed = focusPaths.equals(List.of("terminal:input", "index:search", "lens:scan", "recovery:recover"))
                && effects.equals(List.of("focus:initial:terminal", "focus:initial:index", "focus:initial:lens", "focus:initial:recovery"))
                && source.terminalCommand().equals(terminalTyped.get("value"))
                && source.indexQuery().equals(indexTyped.get("value"))
                && "lensScanExecuted".equals(lensAction.get("executedKey"))
                && "recoveryActionExecuted".equals(recoveryAction.get("executedKey"))
                && renderedTerminal.lines().stream().anyMatch(line -> line.contains("terminal:input ready"))
                && renderedLens.lines().stream().anyMatch(line -> line.contains("lens:scan ready"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("initialFocusSmokeClass", EchoAgent5InitialFocusSmoke.class.getSimpleName());
        smoke.put("serviceCodeExecuted", true);
        smoke.put("adapterCoreBridge", true);
        smoke.put("focusPaths", focusPaths);
        smoke.put("effects", effects);
        smoke.put("terminalBuffer", terminalTyped.get("value"));
        smoke.put("indexBuffer", indexTyped.get("value"));
        smoke.put("executedKeys", List.of(lensAction.get("executedKey"), recoveryAction.get("executedKey")));
        smoke.put("renderedLines", List.of(renderedTerminal.lines(), renderedLens.lines()).stream().flatMap(List::stream).toList());
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }
}
