package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5TerminalEndToEndAcceptanceSmoke {
    private EchoAgent5TerminalEndToEndAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> hotkey = EchoAgent5PhysicalHotkeyPoller.poll(
                EchoAgent5PhysicalHotkeyPoller.emptyState(),
                pressed("M")
        );
        Map<String, Object> liveSurface = EchoAgent5LiveSurfaceAcceptance.assess(
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "TERMINAL",
                "TERMINAL"
        );
        Map<String, Object> physicalInput = EchoAgent5PhysicalInputAcceptance.assess(hotkey, liveSurface);
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                "TERMINAL",
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "echoashfallprotocol",
                92,
                20,
                1,
                1,
                source
        );
        Map<String, Object> render = EchoAgent5LiveSurfaceRenderAcceptance.assess(liveSurface, snapshot);
        Map<String, Object> focus = EchoAgent5FocusManagerSmoke.capture(source);
        Map<String, Object> editing = EchoAgent5TextEditingSmoke.capture(source);
        Map<String, Object> transcript = EchoAgent5HostEventTranscriptSmoke.capture(
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "echoashfallprotocol",
                92,
                20,
                1,
                1,
                source
        );
        Map<String, Object> accepted = EchoAgent5TerminalEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                render,
                focus,
                editing,
                transcript,
                source
        );
        Map<String, Object> rejectedNoInput = EchoAgent5TerminalEndToEndAcceptance.assess(
                hotkey,
                Map.of("accepted", false, "surface", "TERMINAL"),
                render,
                focus,
                editing,
                transcript,
                source
        );
        Map<String, Object> rejectedNoRender = EchoAgent5TerminalEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                Map.of("accepted", false, "surface", "TERMINAL"),
                focus,
                editing,
                transcript,
                source
        );
        Map<String, Object> rejectedNoCommand = EchoAgent5TerminalEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                render,
                focus,
                Map.of("passed", false, "terminalBuffer", source.terminalCommand()),
                transcript,
                source
        );
        Map<String, Object> rejectedNoTranscript = EchoAgent5TerminalEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                render,
                focus,
                editing,
                Map.of("passed", false),
                source
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "terminal_end_to_end:M->TERMINAL:status".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoInput.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRender.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoCommand.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoTranscript.get("accepted"));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("terminalEndToEndAcceptanceSmokeClass",
                EchoAgent5TerminalEndToEndAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoInput", rejectedNoInput);
        smoke.put("rejectedNoRender", rejectedNoRender);
        smoke.put("rejectedNoCommand", rejectedNoCommand);
        smoke.put("rejectedNoTranscript", rejectedNoTranscript);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static Map<String, Boolean> pressed(String key) {
        Map<String, Boolean> state = new LinkedHashMap<>(EchoAgent5PhysicalHotkeyPoller.emptyState());
        state.put(key, true);
        return Map.copyOf(state);
    }
}
