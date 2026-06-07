package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5WikiEndToEndAcceptanceSmoke {
    private EchoAgent5WikiEndToEndAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> hotkey = Map.of("handled", true, "key", "DIRECT", "surface", "WIKI");
        Map<String, Object> liveSurface = EchoAgent5LiveSurfaceAcceptance.assess(
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "WIKI",
                "WIKI"
        );
        Map<String, Object> physicalInput = EchoAgent5PhysicalInputAcceptance.assess(hotkey, liveSurface);
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                "WIKI",
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
        Map<String, Object> accepted = EchoAgent5WikiEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                render,
                interaction,
                source
        );
        Map<String, Object> rejectedNoInput = EchoAgent5WikiEndToEndAcceptance.assess(
                hotkey,
                Map.of("accepted", false, "surface", "WIKI"),
                render,
                interaction,
                source
        );
        Map<String, Object> rejectedNoRender = EchoAgent5WikiEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                Map.of("accepted", false, "surface", "WIKI"),
                interaction,
                source
        );
        Map<String, Object> rejectedNoInteraction = EchoAgent5WikiEndToEndAcceptance.assess(
                hotkey,
                physicalInput,
                render,
                Map.of("passed", false, "steps", java.util.List.of()),
                source
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && ("wiki_end_to_end:direct:WIKI:" + source.wikiValues().get("page")).equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoInput.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRender.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoInteraction.get("accepted"));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("wikiEndToEndAcceptanceSmokeClass",
                EchoAgent5WikiEndToEndAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoInput", rejectedNoInput);
        smoke.put("rejectedNoRender", rejectedNoRender);
        smoke.put("rejectedNoInteraction", rejectedNoInteraction);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

}
