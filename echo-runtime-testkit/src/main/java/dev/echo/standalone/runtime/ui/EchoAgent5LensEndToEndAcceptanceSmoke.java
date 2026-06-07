package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LensEndToEndAcceptanceSmoke {
    private EchoAgent5LensEndToEndAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> hotkey = EchoAgent5PhysicalHotkeyPoller.poll(
                EchoAgent5PhysicalHotkeyPoller.emptyState(),
                pressed("LEFT_ALT")
        );
        Map<String, Object> liveSurface = EchoAgent5LiveSurfaceAcceptance.assess(
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "LENS",
                "LENS"
        );
        Map<String, Object> physicalInput = EchoAgent5PhysicalInputAcceptance.assess(hotkey, liveSurface);
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                "LENS",
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
        Map<String, Object> transcript = EchoAgent5HostEventTranscriptSmoke.capture(
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "echoashfallprotocol",
                92,
                20,
                1,
                1,
                source
        );
        Map<String, Object> accepted = EchoAgent5LensEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                render,
                focus,
                transcript,
                source
        );
        Map<String, Object> rejectedNoInput = EchoAgent5LensEndToEndAcceptance.assess(
                hotkey,
                Map.of("accepted", false, "surface", "LENS"),
                render,
                focus,
                transcript,
                source
        );
        Map<String, Object> rejectedNoRender = EchoAgent5LensEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                Map.of("accepted", false, "surface", "LENS"),
                focus,
                transcript,
                source
        );
        Map<String, Object> rejectedNoScan = EchoAgent5LensEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                render,
                Map.of("passed", false, "activationKeys", java.util.List.of()),
                transcript,
                source
        );
        Map<String, Object> rejectedNoTranscript = EchoAgent5LensEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                render,
                focus,
                Map.of("passed", false),
                source
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && ("lens_end_to_end:LEFT_ALT->LENS:" + source.lensTarget()).equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoInput.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRender.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoScan.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoTranscript.get("accepted"));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("lensEndToEndAcceptanceSmokeClass",
                EchoAgent5LensEndToEndAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoInput", rejectedNoInput);
        smoke.put("rejectedNoRender", rejectedNoRender);
        smoke.put("rejectedNoScan", rejectedNoScan);
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
