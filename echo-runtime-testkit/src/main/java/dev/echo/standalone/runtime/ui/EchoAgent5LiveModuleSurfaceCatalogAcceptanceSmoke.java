package dev.echo.standalone.runtime.ui;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveModuleSurfaceCatalogAcceptanceSmoke {
    private EchoAgent5LiveModuleSurfaceCatalogAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        List<Map<String, Object>> surfaces = List.of(
                EchoAgent5UiModuleSurfaceRenderers.renderTerminal(Map.of(
                        "focusedControl", "terminal:input",
                        "mouseRouted", true,
                        "terminalBuffer", source.terminalCommand(),
                        "terminalOutput", source.terminalReadyLine(),
                        "terminalCommandExecuted", true
                ), source),
                EchoAgent5UiModuleSurfaceRenderers.renderIndex(Map.of(
                        "focusedControl", "index:search",
                        "mouseRouted", true,
                        "indexBuffer", source.indexQuery(),
                        "indexOutput", source.indexResult(),
                        "indexSearchExecuted", true
                ), source),
                EchoAgent5UiModuleSurfaceRenderers.renderLens(Map.of(
                        "focusedControl", "lens:scan",
                        "mouseRouted", true,
                        "lensOutput", source.lensResult(),
                        "lensScanExecuted", true
                ), source),
                EchoAgent5UiModuleSurfaceRenderers.renderHolomap(Map.of(), source),
                EchoAgent5UiModuleSurfaceRenderers.renderWiki(Map.of(), source),
                EchoAgent5UiModuleSurfaceRenderers.renderMissionLog(Map.of(), source),
                EchoAgent5UiModuleSurfaceRenderers.renderSettings(Map.of(), source),
                EchoAgent5UiModuleSurfaceRenderers.renderPause(Map.of("previousMode", "WIKI"), source),
                EchoAgent5UiModuleSurfaceRenderers.renderRecovery(Map.of(
                        "focusedControl", "recovery:recover",
                        "mouseRouted", true,
                        "recoveryOutput", "Status: " + EchoAgent5UiReference.RECOVERY_STATUS + "    Health: 35",
                        "recoveryActionExecuted", true
                ), source),
                EchoAgent5UiModuleSurfaceRenderers.renderMainMenu(Map.of(), source),
                EchoAgent5UiModuleSurfaceRenderers.renderHud(Map.of(), source)
        );
        Map<String, Object> accepted = EchoAgent5LiveModuleSurfaceCatalogAcceptance.assess(surfaces);
        Map<String, Object> rejectedMissingHud = EchoAgent5LiveModuleSurfaceCatalogAcceptance.assess(
                surfaces.subList(0, 10)
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_module_surface_catalog:accepted:11-surfaces".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedMissingHud.get("accepted"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveModuleSurfaceCatalogAcceptanceSmokeClass",
                EchoAgent5LiveModuleSurfaceCatalogAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedMissingHud", rejectedMissingHud);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }
}
