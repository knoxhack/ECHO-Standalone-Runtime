package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5TextEditingSmoke {
    private EchoAgent5TextEditingSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        ArrayList<String> editEffects = new ArrayList<>();
        ArrayList<String> activationKeys = new ArrayList<>();
        ArrayList<String> renderedLines = new ArrayList<>();

        String terminalFocus = EchoAgent5UiActionRouter.focusPath("TERMINAL", EchoAgent5UiReference.WIKI_SCREEN);
        EchoTextInput terminalInput = new EchoTextInput("agent5:terminal-input", type(
                "TERMINAL",
                terminalFocus,
                "statuz",
                "",
                source,
                editEffects
        ));
        Map<String, Object> terminalBackspace = EchoAgent5UiActionRouter.routeEditKey(
                "BACKSPACE",
                "TERMINAL",
                terminalFocus,
                terminalInput.value(),
                ""
        );
        terminalInput = terminalInput.withValue(String.valueOf(terminalBackspace.get("value")));
        editEffects.add(String.valueOf(terminalBackspace.get("effect")));
        terminalInput = terminalInput.withValue(type("TERMINAL", terminalFocus, "s", terminalInput.value(), source, editEffects));
        Map<String, Object> terminalAction = EchoAgent5UiActionRouter.activate("TERMINAL", Map.of(
                "focusedControl", terminalFocus,
                "terminalBuffer", terminalInput.value()
        ), source);
        activationKeys.add(String.valueOf(terminalAction.get("executedKey")));
        renderedLines.addAll(EchoAgent5UiSurfaceRenderer.render("TERMINAL", Map.of(
                "focusedControl", terminalFocus,
                "mouseRouted", true,
                "terminalBuffer", terminalInput.value(),
                "terminalOutput", terminalAction.get("output"),
                "terminalCommandExecuted", true
        ), source).lines());

        String indexFocus = EchoAgent5UiActionRouter.focusPath("INDEX", EchoAgent5UiReference.WIKI_SCREEN);
        EchoTextInput indexInput = new EchoTextInput("agent5:index-search", type(
                "INDEX",
                indexFocus,
                "ashx",
                "",
                source,
                editEffects
        ));
        Map<String, Object> indexBackspace = EchoAgent5UiActionRouter.routeEditKey(
                "BACKSPACE",
                "INDEX",
                indexFocus,
                "",
                indexInput.value()
        );
        indexInput = indexInput.withValue(String.valueOf(indexBackspace.get("value")));
        editEffects.add(String.valueOf(indexBackspace.get("effect")));
        indexInput = indexInput.withValue(type("INDEX", indexFocus, "fall", indexInput.value(), source, editEffects));
        Map<String, Object> indexAction = EchoAgent5UiActionRouter.activate("INDEX", Map.of(
                "focusedControl", indexFocus,
                "indexBuffer", indexInput.value()
        ), source);
        activationKeys.add(String.valueOf(indexAction.get("executedKey")));
        renderedLines.addAll(EchoAgent5UiSurfaceRenderer.render("INDEX", Map.of(
                "focusedControl", indexFocus,
                "mouseRouted", true,
                "indexBuffer", indexInput.value(),
                "indexOutput", indexAction.get("output"),
                "indexSearchExecuted", true
        ), source).lines());

        Map<String, Object> emptyBackspace = EchoAgent5UiActionRouter.routeEditKey(
                "BACKSPACE",
                "TERMINAL",
                terminalFocus,
                "",
                ""
        );
        EchoTerminalBuffer outputBuffer = new EchoTerminalBuffer(renderedLines);

        boolean passed = source.terminalCommand().equals(terminalInput.value())
                && source.indexQuery().equals(indexInput.value())
                && "".equals(emptyBackspace.get("value"))
                && editEffects.containsAll(List.of("terminal-character", "terminal-backspace", "index-character", "index-backspace"))
                && activationKeys.containsAll(List.of("terminalCommandExecuted", "indexSearchExecuted"))
                && outputBuffer.contains(source.terminalReadyLine())
                && outputBuffer.contains(source.indexResult());

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("textEditingSmokeClass", EchoAgent5TextEditingSmoke.class.getSimpleName());
        smoke.put("terminalBuffer", terminalInput.value());
        smoke.put("indexBuffer", indexInput.value());
        smoke.put("emptyBackspaceValue", emptyBackspace.get("value"));
        smoke.put("editEffects", List.copyOf(editEffects));
        smoke.put("activationKeys", List.copyOf(activationKeys));
        smoke.put("renderedLines", List.copyOf(renderedLines));
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static String type(
            String mode,
            String focus,
            String value,
            String buffer,
            EchoAgent5UiDataSources source,
            List<String> effects
    ) {
        String next = buffer;
        for (char character : value.toCharArray()) {
            Map<String, Object> typed = EchoAgent5UiActionRouter.routeCharacter(
                    mode,
                    focus,
                    "TERMINAL".equals(mode) ? next : "",
                    "INDEX".equals(mode) ? next : "",
                    character
            );
            next = String.valueOf(typed.get("value"));
            effects.add(String.valueOf(typed.get("effect")));
        }
        return next;
    }
}
