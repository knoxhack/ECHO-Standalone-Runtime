package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5UiHostEndToEndAcceptanceSmoke {
    private EchoAgent5UiHostEndToEndAcceptanceSmoke() {
    }

    public static Map<String, Object> capture() {
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
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
        Map<String, Object> interaction = EchoAgent5UiHostInteractionStateAcceptance.assess(
                EchoAgent5UiHostInteractionSmoke.run(
                        "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                        "echoashfallprotocol",
                        92,
                        20,
                        1,
                        1,
                        source
                )
        );
        Map<String, Object> accepted = EchoAgent5UiHostEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                liveSurface,
                render,
                interaction
        );
        Map<String, Object> rejectedNoInput = EchoAgent5UiHostEndToEndAcceptance.assess(
                hotkey,
                Map.of("accepted", false, "surface", "TERMINAL"),
                liveSurface,
                render,
                interaction
        );
        Map<String, Object> rejectedRender = EchoAgent5UiHostEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                liveSurface,
                Map.of("accepted", false, "surface", "TERMINAL"),
                interaction
        );
        Map<String, Object> rejectedInteraction = EchoAgent5UiHostEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                liveSurface,
                render,
                Map.of("accepted", false, "stepCount", 9)
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && Boolean.TRUE.equals(accepted.get("serviceCodeExecuted"))
                && "ui_host_end_to_end:M->TERMINAL:10".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoInput.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoInput.get("serviceCodeExecuted"))
                && Boolean.FALSE.equals(rejectedRender.get("accepted"))
                && Boolean.FALSE.equals(rejectedRender.get("serviceCodeExecuted"))
                && Boolean.FALSE.equals(rejectedInteraction.get("accepted"))
                && Boolean.FALSE.equals(rejectedInteraction.get("serviceCodeExecuted"));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("uiHostEndToEndAcceptanceSmokeClass",
                EchoAgent5UiHostEndToEndAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoInput", rejectedNoInput);
        smoke.put("rejectedRender", rejectedRender);
        smoke.put("rejectedInteraction", rejectedInteraction);
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
