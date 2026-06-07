package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5UiHostSmokeSnapshot {
    private EchoAgent5UiHostSmokeSnapshot() {
    }

    public static Map<String, Object> capture(
            String surface,
            boolean opened,
            String screenClass,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources dataSources
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        return capture(
                surface,
                opened,
                screenClass,
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source,
                stateFor(normalizeSurface(surface), source)
        );
    }

    public static Map<String, Object> capture(
            String surface,
            boolean opened,
            String screenClass,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources dataSources,
            Map<String, Object> state
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        String normalizedSurface = normalizeSurface(surface);
        Map<String, Object> hostModel = EchoAgent5UiScreenHostModel.render(
                normalizedSurface,
                state,
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source
        );
        EchoUiSurface surfaceModel = EchoAgent5UiSurfaceRenderer.render(normalizedSurface, state, source);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("surface", normalizedSurface);
        snapshot.put("opened", opened);
        snapshot.put("screenClass", screenClass);
        snapshot.put("screenTitle", hostModel.get("screenTitle"));
        snapshot.put("headerLines", hostModel.get("headerLines"));
        snapshot.put("surfaceLines", hostModel.get("surfaceLines"));
        snapshot.put("footerLine", hostModel.get("footerLine"));
        snapshot.put("hudValues", hostModel.get("hudValues"));
        snapshot.put("notificationAnchor", hostModel.get("notificationAnchor"));
        snapshot.put("focusPath", surfaceModel.focusPath());
        snapshot.put("moduleRendererClass", moduleRenderer(normalizedSurface, state, source));
        snapshot.put("hostModelClass", hostModel.get("hostModelClass"));
        snapshot.put("adapterCoreBridge", true);
        snapshot.put("serviceCodeExecuted", true);
        snapshot.put("snapshotClass", EchoAgent5UiHostSmokeSnapshot.class.getSimpleName());
        return Map.copyOf(snapshot);
    }

    private static String moduleRenderer(String surface, Map<String, Object> state, EchoAgent5UiDataSources source) {
        Map<String, Object> model = switch (surface) {
            case "TERMINAL" -> EchoAgent5UiModuleSurfaceRenderers.renderTerminal(state, source);
            case "INDEX" -> EchoAgent5UiModuleSurfaceRenderers.renderIndex(state, source);
            case "LENS" -> EchoAgent5UiModuleSurfaceRenderers.renderLens(state, source);
            case "MISSION_LOG" -> EchoAgent5UiModuleSurfaceRenderers.renderMissionLog(state, source);
            case "SETTINGS" -> EchoAgent5UiModuleSurfaceRenderers.renderSettings(state, source);
            case "PAUSE" -> EchoAgent5UiModuleSurfaceRenderers.renderPause(state, source);
            case "RECOVERY" -> EchoAgent5UiModuleSurfaceRenderers.renderRecovery(state, source);
            case "HOLOMAP" -> EchoAgent5UiModuleSurfaceRenderers.renderHolomap(state, source);
            case "WIKI" -> EchoAgent5UiModuleSurfaceRenderers.renderWiki(state, source);
            case "MAIN_MENU" -> EchoAgent5UiModuleSurfaceRenderers.renderMainMenu(state, source);
            case "HUD" -> EchoAgent5UiModuleSurfaceRenderers.renderHud(state, source);
            default -> Map.of("moduleRendererClass", "");
        };
        return String.valueOf(model.get("moduleRendererClass"));
    }

    private static Map<String, Object> stateFor(String surface, EchoAgent5UiDataSources source) {
        return switch (surface) {
            case "TERMINAL" -> Map.of(
                    "focusedControl", "terminal:input",
                    "mouseRouted", true,
                    "terminalBuffer", source.terminalCommand(),
                    "terminalOutput", source.terminalReadyLine(),
                    "terminalCommandExecuted", true
            );
            case "INDEX" -> Map.of(
                    "focusedControl", "index:search",
                    "mouseRouted", true,
                    "indexBuffer", source.indexQuery(),
                    "indexOutput", source.indexResult(),
                    "indexSearchExecuted", true
            );
            case "LENS" -> Map.of(
                    "focusedControl", "lens:scan",
                    "mouseRouted", true,
                    "lensOutput", source.lensResult(),
                    "lensScanExecuted", true
            );
            case "RECOVERY" -> Map.of(
                    "focusedControl", "recovery:recover",
                    "mouseRouted", true,
                    "recoveryOutput", "Status: " + EchoAgent5UiReference.RECOVERY_STATUS + "    Health: 35",
                    "recoveryActionExecuted", true
            );
            case "PAUSE" -> Map.of("previousMode", EchoAgent5UiReference.WIKI_SCREEN);
            default -> Map.of();
        };
    }

    @SuppressWarnings("unchecked")
    public static List<String> strings(Map<String, Object> snapshot, String key) {
        Object value = snapshot.get(key);
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    private static String normalizeSurface(String surface) {
        String normalized = surface == null ? "" : surface.trim().toUpperCase(java.util.Locale.ROOT);
        return normalized.isBlank() ? "TERMINAL" : normalized;
    }
}
