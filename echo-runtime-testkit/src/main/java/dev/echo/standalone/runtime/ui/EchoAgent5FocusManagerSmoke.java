package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5FocusManagerSmoke {
    private EchoAgent5FocusManagerSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        ArrayList<String> widgetIds = new ArrayList<>();
        ArrayList<String> focusOrder = new ArrayList<>();
        ArrayList<String> typedEffects = new ArrayList<>();
        ArrayList<String> ignoredReasons = new ArrayList<>();
        ArrayList<String> activationKeys = new ArrayList<>();
        ArrayList<String> renderedFocusLines = new ArrayList<>();

        EchoWidget terminalWidget = new EchoWidget("agent5:terminal", "text-input", Map.of("mode", "TERMINAL"));
        EchoTextInput terminalInput = new EchoTextInput("agent5:terminal-input", "");
        EchoWidget indexWidget = new EchoWidget("agent5:index", "text-input", Map.of("mode", "INDEX"));
        EchoTextInput indexInput = new EchoTextInput("agent5:index-search", "");
        EchoButton lensButton = new EchoButton("agent5:lens-scan", "Scan", "lens-scan");
        EchoListView recoveryList = new EchoListView("agent5:recovery-actions", List.of("recover"), 0);
        widgetIds.addAll(List.of(
                terminalWidget.id(),
                terminalInput.id(),
                indexWidget.id(),
                indexInput.id(),
                lensButton.id(),
                recoveryList.id()
        ));

        String terminalFocus = EchoAgent5UiActionRouter.focusPath("TERMINAL", EchoAgent5UiReference.WIKI_SCREEN);
        String indexFocus = EchoAgent5UiActionRouter.focusPath("INDEX", EchoAgent5UiReference.WIKI_SCREEN);
        String lensFocus = EchoAgent5UiActionRouter.focusPath("LENS", EchoAgent5UiReference.WIKI_SCREEN);
        String recoveryFocus = EchoAgent5UiActionRouter.focusPath("RECOVERY", EchoAgent5UiReference.WIKI_SCREEN);
        focusOrder.addAll(List.of(terminalFocus, indexFocus, lensFocus, recoveryFocus));

        Map<String, Object> wrongTerminalFocus = EchoAgent5UiActionRouter.routeCharacter(
                "TERMINAL",
                indexFocus,
                "",
                "",
                's'
        );
        ignoredReasons.add(String.valueOf(wrongTerminalFocus.get("reason")));
        Map<String, Object> controlCharacter = EchoAgent5UiActionRouter.routeCharacter(
                "INDEX",
                indexFocus,
                "",
                "",
                '\n'
        );
        ignoredReasons.add(String.valueOf(controlCharacter.get("reason")));

        for (char character : source.terminalCommand().toCharArray()) {
            Map<String, Object> typed = EchoAgent5UiActionRouter.routeCharacter(
                    "TERMINAL",
                    terminalFocus,
                    terminalInput.value(),
                    "",
                    character
            );
            terminalInput = terminalInput.withValue(String.valueOf(typed.get("value")));
            typedEffects.add(String.valueOf(typed.get("effect")));
        }
        Map<String, Object> terminalAction = EchoAgent5UiActionRouter.activate("TERMINAL", Map.of(
                "focusedControl", terminalFocus,
                "terminalBuffer", terminalInput.value()
        ), source);
        activationKeys.add(String.valueOf(terminalAction.get("executedKey")));
        renderedFocusLines.addAll(EchoAgent5UiSurfaceRenderer.render("TERMINAL", Map.of(
                "focusedControl", terminalFocus,
                "mouseRouted", true,
                "terminalBuffer", terminalInput.value(),
                "terminalOutput", terminalAction.get("output"),
                "terminalCommandExecuted", true
        ), source).lines());

        for (char character : source.indexQuery().toCharArray()) {
            Map<String, Object> typed = EchoAgent5UiActionRouter.routeCharacter(
                    "INDEX",
                    indexFocus,
                    "",
                    indexInput.value(),
                    character
            );
            indexInput = indexInput.withValue(String.valueOf(typed.get("value")));
            typedEffects.add(String.valueOf(typed.get("effect")));
        }
        Map<String, Object> indexAction = EchoAgent5UiActionRouter.activate("INDEX", Map.of(
                "focusedControl", indexFocus,
                "indexBuffer", indexInput.value()
        ), source);
        activationKeys.add(String.valueOf(indexAction.get("executedKey")));
        renderedFocusLines.addAll(EchoAgent5UiSurfaceRenderer.render("INDEX", Map.of(
                "focusedControl", indexFocus,
                "mouseRouted", true,
                "indexBuffer", indexInput.value(),
                "indexOutput", indexAction.get("output"),
                "indexSearchExecuted", true
        ), source).lines());

        Map<String, Object> lensAction = EchoAgent5UiActionRouter.activate("LENS", Map.of(
                "focusedControl", lensFocus
        ), source);
        activationKeys.add(String.valueOf(lensAction.get("executedKey")));
        renderedFocusLines.addAll(EchoAgent5UiSurfaceRenderer.render("LENS", Map.of(
                "focusedControl", lensFocus,
                "mouseRouted", true,
                "lensOutput", lensAction.get("output"),
                "lensScanExecuted", true
        ), source).lines());

        Map<String, Object> recoveryAction = EchoAgent5UiActionRouter.activate("RECOVERY", Map.of(
                "focusedControl", recoveryFocus
        ), source);
        activationKeys.add(String.valueOf(recoveryAction.get("executedKey")));
        renderedFocusLines.addAll(EchoAgent5UiSurfaceRenderer.render("RECOVERY", Map.of(
                "focusedControl", recoveryFocus,
                "mouseRouted", true,
                "recoveryOutput", recoveryAction.get("output"),
                "recoveryActionExecuted", true
        ), source).lines());

        boolean passed = widgetIds.containsAll(List.of(
                        "agent5:terminal",
                        "agent5:terminal-input",
                        "agent5:index",
                        "agent5:index-search",
                        "agent5:lens-scan",
                        "agent5:recovery-actions"
                ))
                && "recover".equals(recoveryList.selectedRow())
                && focusOrder.equals(List.of("terminal:input", "index:search", "lens:scan", "recovery:recover"))
                && ignoredReasons.containsAll(List.of("character:unfocused", "character:control"))
                && source.terminalCommand().equals(terminalInput.value())
                && source.indexQuery().equals(indexInput.value())
                && typedEffects.stream().filter("terminal-character"::equals).count() == source.terminalCommand().length()
                && typedEffects.stream().filter("index-character"::equals).count() == source.indexQuery().length()
                && activationKeys.containsAll(List.of(
                        "terminalCommandExecuted",
                        "indexSearchExecuted",
                        "lensScanExecuted",
                        "recoveryActionExecuted"
                ))
                && renderedFocusLines.stream().anyMatch(line -> line.contains("terminal:input ready"))
                && renderedFocusLines.stream().anyMatch(line -> line.contains("index:search ready"))
                && renderedFocusLines.stream().anyMatch(line -> line.contains("lens:scan ready"))
                && renderedFocusLines.stream().anyMatch(line -> line.contains("recovery:recover ready"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("focusManagerSmokeClass", EchoAgent5FocusManagerSmoke.class.getSimpleName());
        smoke.put("widgetIds", List.copyOf(widgetIds));
        smoke.put("focusOrder", List.copyOf(focusOrder));
        smoke.put("typedEffects", List.copyOf(typedEffects));
        smoke.put("ignoredReasons", List.copyOf(ignoredReasons));
        smoke.put("activationKeys", List.copyOf(activationKeys));
        smoke.put("renderedFocusLines", List.copyOf(renderedFocusLines));
        smoke.put("terminalBuffer", terminalInput.value());
        smoke.put("indexBuffer", indexInput.value());
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }
}
