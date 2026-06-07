package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5PauseEndToEndAcceptanceSmoke {
    private EchoAgent5PauseEndToEndAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> hotkey = EchoAgent5PhysicalHotkeyPoller.poll(
                EchoAgent5PhysicalHotkeyPoller.emptyState(),
                pressed("ESCAPE")
        );
        Map<String, Object> liveSurface = EchoAgent5LiveSurfaceAcceptance.assess(
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "PAUSE",
                "PAUSE"
        );
        Map<String, Object> physicalInput = EchoAgent5PhysicalInputAcceptance.assess(hotkey, liveSurface);
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                "PAUSE",
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "echoashfallprotocol",
                92,
                20,
                1,
                1,
                source,
                Map.of("previousMode", "LENS")
        );
        Map<String, Object> render = EchoAgent5LiveSurfaceRenderAcceptance.assess(liveSurface, snapshot);
        Map<String, Object> interaction = EchoAgent5UiHostInteractionSmoke.run(
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "echoashfallprotocol",
                92,
                20,
                1,
                1,
                source
        );
        Map<String, Object> option = EchoAgent5PauseOptionActivationSmoke.capture(source);
        Map<String, Object> accepted = EchoAgent5PauseEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                render,
                interaction,
                option
        );
        Map<String, Object> rejectedNoInput = EchoAgent5PauseEndToEndAcceptance.assess(
                hotkey,
                Map.of("accepted", false, "surface", "PAUSE"),
                render,
                interaction,
                option
        );
        Map<String, Object> rejectedNoRender = EchoAgent5PauseEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                Map.of("accepted", false, "surface", "PAUSE"),
                interaction,
                option
        );
        Map<String, Object> rejectedNoInteraction = EchoAgent5PauseEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                render,
                Map.of("passed", false, "steps", java.util.List.of()),
                option
        );
        Map<String, Object> rejectedNoOption = EchoAgent5PauseEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                render,
                interaction,
                Map.of("passed", false)
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "pause_end_to_end:ESCAPE->PAUSE:LENS".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoInput.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRender.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoInteraction.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoOption.get("accepted"));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("pauseEndToEndAcceptanceSmokeClass",
                EchoAgent5PauseEndToEndAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoInput", rejectedNoInput);
        smoke.put("rejectedNoRender", rejectedNoRender);
        smoke.put("rejectedNoInteraction", rejectedNoInteraction);
        smoke.put("rejectedNoOption", rejectedNoOption);
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
