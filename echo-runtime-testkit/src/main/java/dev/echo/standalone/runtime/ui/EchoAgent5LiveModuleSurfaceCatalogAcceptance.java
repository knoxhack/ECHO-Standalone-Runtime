package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5LiveModuleSurfaceCatalogAcceptance {
    private EchoAgent5LiveModuleSurfaceCatalogAcceptance() {
    }

    public static Map<String, Object> assess(List<Map<String, Object>> surfaces) {
        List<Map<String, Object>> catalog = surfaces == null ? List.of() : surfaces;
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        boolean terminal = accepts(catalog, "echoterminal", "EchoAgent5TerminalSurfaceRenderer", "terminal:input", source.terminalReadyLine());
        boolean index = accepts(catalog, "echoindex", "EchoAgent5IndexSurfaceRenderer", "index:search", source.indexResult());
        boolean lens = accepts(catalog, "echolens", "EchoAgent5LensSurfaceRenderer", "lens:scan", source.lensResult());
        boolean holomap = accepts(catalog, "echoholomap", "EchoAgent5HolomapSurfaceRenderer", "holomap:surface",
                String.valueOf(source.holomapValues().get("marker")));
        boolean wiki = accepts(catalog, "echowiki", "EchoAgent5WikiSurfaceRenderer", "wiki:surface",
                String.valueOf(source.wikiValues().get("link")));
        boolean mission = accepts(catalog, "echoscreencore", "EchoAgent5MissionLogSurfaceRenderer",
                "mission_log:surface", EchoAgent5UiReference.ACTIVE_MISSION_TITLE);
        boolean settings = accepts(catalog, "echothemecore", "EchoAgent5SettingsSurfaceRenderer", "settings:surface", "Theme: ashfall-agent5");
        boolean pause = accepts(catalog, "echoscreencore", "EchoAgent5PauseSurfaceRenderer", "pause:resume:WIKI", "Pause flow is live");
        boolean recovery = accepts(catalog, "echoscreencore", "EchoAgent5RecoverySurfaceRenderer", "recovery:recover", "Status: RECOVERED");
        boolean mainMenu = accepts(catalog, "echoscreencore", "EchoAgent5MainMenuSurfaceRenderer", "main_menu:continue", "Custom main menu surface is live");
        boolean hud = accepts(catalog, "echohudcore", "EchoAgent5HudSurfaceRenderer", EchoAgent5UiReference.HUD_LAYER, "HUD overlay is live");
        boolean accepted = terminal && index && lens && holomap && wiki && mission && settings && pause && recovery && mainMenu && hud;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("surfaceCount", catalog.size());
        result.put("terminalAccepted", terminal);
        result.put("indexAccepted", index);
        result.put("lensAccepted", lens);
        result.put("holomapAccepted", holomap);
        result.put("wikiAccepted", wiki);
        result.put("missionAccepted", mission);
        result.put("settingsAccepted", settings);
        result.put("pauseAccepted", pause);
        result.put("recoveryAccepted", recovery);
        result.put("mainMenuAccepted", mainMenu);
        result.put("hudAccepted", hud);
        result.put("effect", accepted
                ? "live_module_surface_catalog:accepted:11-surfaces"
                : "live_module_surface_catalog:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    private static boolean accepts(
            List<Map<String, Object>> catalog,
            String moduleId,
            String rendererClass,
            String focusPath,
            String lineToken
    ) {
        return catalog.stream().anyMatch(surface -> moduleId.equals(surface.get("moduleId"))
                && rendererClass.equals(surface.get("moduleRendererClass"))
                && focusPath.equals(surface.get("focusPath"))
                && Boolean.TRUE.equals(surface.get("serviceCodeExecuted"))
                && lines(surface).stream().anyMatch(line -> line.contains(lineToken)));
    }

    @SuppressWarnings("unchecked")
    private static List<String> lines(Map<String, Object> surface) {
        Object value = surface.get("lines");
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }
}
