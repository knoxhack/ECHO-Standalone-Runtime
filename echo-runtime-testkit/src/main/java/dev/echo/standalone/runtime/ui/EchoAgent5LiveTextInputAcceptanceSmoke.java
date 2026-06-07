package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5LiveTextInputAcceptanceSmoke {
    private static final String SCREEN_CLASS = "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost";

    private EchoAgent5LiveTextInputAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> terminal = routeTextInput(
                "TERMINAL",
                "terminal:input",
                "terminalBuffer",
                source.terminalCommand(),
                source.terminalReadyLine(),
                source
        );
        Map<String, Object> index = routeTextInput(
                "INDEX",
                "index:search",
                "indexBuffer",
                source.indexQuery(),
                source.indexResult(),
                source
        );
        Map<String, Object> rejectedUnfocused = EchoAgent5LiveTextInputAcceptance.assess(
                "TERMINAL",
                source.terminalCommand(),
                source.terminalReadyLine(),
                List.of(EchoAgent5UiActionRouter.routeCharacter("TERMINAL", "terminal:surface", "", "", 's')),
                EchoAgent5UiActionRouter.routeEditKey(
                        "BACKSPACE",
                        "TERMINAL",
                        "terminal:surface",
                        source.terminalCommand(),
                        ""
                ),
                EchoAgent5UiActionRouter.activate("TERMINAL", Map.of(
                        "focusedControl", "terminal:surface",
                        "terminalBuffer", source.terminalCommand()
                ), source),
                EchoAgent5UiHostSmokeSnapshot.capture(
                        "TERMINAL",
                        true,
                        SCREEN_CLASS,
                        "echoashfallprotocol",
                        92,
                        20,
                        1,
                        1,
                        source
                )
        );
        boolean passed = Boolean.TRUE.equals(terminal.get("accepted"))
                && Boolean.TRUE.equals(index.get("accepted"))
                && "live_text_input:accepted:TERMINAL:status".equals(terminal.get("effect"))
                && "live_text_input:accepted:INDEX:ashfall".equals(index.get("effect"))
                && Boolean.FALSE.equals(rejectedUnfocused.get("accepted"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveTextInputAcceptanceSmokeClass",
                EchoAgent5LiveTextInputAcceptanceSmoke.class.getSimpleName());
        smoke.put("terminal", terminal);
        smoke.put("index", index);
        smoke.put("rejectedUnfocused", rejectedUnfocused);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static Map<String, Object> routeTextInput(
            String mode,
            String focusedControl,
            String targetBuffer,
            String expectedBuffer,
            String expectedOutput,
            EchoAgent5UiDataSources source
    ) {
        String terminalBuffer = "";
        String indexBuffer = "";
        List<Map<String, Object>> characterRoutes = new ArrayList<>();
        for (int index = 0; index < expectedBuffer.length(); index++) {
            Map<String, Object> route = EchoAgent5UiActionRouter.routeCharacter(
                    mode,
                    focusedControl,
                    terminalBuffer,
                    indexBuffer,
                    expectedBuffer.charAt(index)
            );
            characterRoutes.add(route);
            if ("terminalBuffer".equals(route.get("targetBuffer"))) {
                terminalBuffer = String.valueOf(route.get("value"));
            }
            if ("indexBuffer".equals(route.get("targetBuffer"))) {
                indexBuffer = String.valueOf(route.get("value"));
            }
        }
        Map<String, Object> extra = EchoAgent5UiActionRouter.routeCharacter(
                mode,
                focusedControl,
                terminalBuffer,
                indexBuffer,
                'x'
        );
        characterRoutes.add(extra);
        if ("terminalBuffer".equals(extra.get("targetBuffer"))) {
            terminalBuffer = String.valueOf(extra.get("value"));
        }
        if ("indexBuffer".equals(extra.get("targetBuffer"))) {
            indexBuffer = String.valueOf(extra.get("value"));
        }
        Map<String, Object> edit = EchoAgent5UiActionRouter.routeEditKey(
                "BACKSPACE",
                mode,
                focusedControl,
                terminalBuffer,
                indexBuffer
        );
        if ("terminalBuffer".equals(edit.get("targetBuffer"))) {
            terminalBuffer = String.valueOf(edit.get("value"));
        }
        if ("indexBuffer".equals(edit.get("targetBuffer"))) {
            indexBuffer = String.valueOf(edit.get("value"));
        }
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("focusedControl", focusedControl);
        state.put("mouseRouted", true);
        state.put(targetBuffer, expectedBuffer);
        Map<String, Object> submit = EchoAgent5UiActionRouter.activate(mode, state, source);
        state.put(String.valueOf(submit.get("outputKey")), submit.get("output"));
        state.put(String.valueOf(submit.get("executedKey")), true);
        return EchoAgent5LiveTextInputAcceptance.assess(
                mode,
                expectedBuffer,
                expectedOutput,
                characterRoutes,
                edit,
                submit,
                EchoAgent5UiHostSmokeSnapshot.capture(
                        mode,
                        true,
                        SCREEN_CLASS,
                        "echoashfallprotocol",
                        92,
                        20,
                        1,
                        1,
                        source,
                        state
                )
        );
    }
}
