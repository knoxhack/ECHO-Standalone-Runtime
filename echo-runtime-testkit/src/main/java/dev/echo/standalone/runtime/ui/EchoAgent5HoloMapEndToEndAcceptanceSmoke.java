package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5HoloMapEndToEndAcceptanceSmoke {
    private EchoAgent5HoloMapEndToEndAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> hotkey = EchoAgent5PhysicalHotkeyPoller.poll(
                EchoAgent5PhysicalHotkeyPoller.emptyState(),
                pressed("J")
        );
        Map<String, Object> liveSurface = EchoAgent5LiveSurfaceAcceptance.assess(
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "HOLOMAP",
                "HOLOMAP"
        );
        Map<String, Object> physicalInput = EchoAgent5PhysicalInputAcceptance.assess(hotkey, liveSurface);
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                "HOLOMAP",
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
        Map<String, Object> interaction = EchoAgent5UiHostInteractionSmoke.run(
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "echoashfallprotocol",
                92,
                20,
                1,
                1,
                source
        );
        Map<String, Object> accepted = EchoAgent5HoloMapEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                render,
                interaction,
                source
        );
        Map<String, Object> rejectedNoInput = EchoAgent5HoloMapEndToEndAcceptance.assess(
                hotkey,
                Map.of("accepted", false, "surface", "HOLOMAP"),
                render,
                interaction,
                source
        );
        Map<String, Object> rejectedNoRender = EchoAgent5HoloMapEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                Map.of("accepted", false, "surface", "HOLOMAP"),
                interaction,
                source
        );
        Map<String, Object> rejectedNoInteraction = EchoAgent5HoloMapEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                render,
                Map.of("passed", false, "steps", java.util.List.of()),
                source
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && ("holomap_end_to_end:J->HOLOMAP:" + source.holomapValues().get("marker"))
                .equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoInput.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRender.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoInteraction.get("accepted"));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("holoMapEndToEndAcceptanceSmokeClass",
                EchoAgent5HoloMapEndToEndAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoInput", rejectedNoInput);
        smoke.put("rejectedNoRender", rejectedNoRender);
        smoke.put("rejectedNoInteraction", rejectedNoInteraction);
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
