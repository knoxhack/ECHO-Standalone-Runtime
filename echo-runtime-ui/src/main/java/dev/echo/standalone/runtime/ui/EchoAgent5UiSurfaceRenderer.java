package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EchoAgent5UiSurfaceRenderer {
    private EchoAgent5UiSurfaceRenderer() {
    }

    public static EchoUiSurface render(String mode, Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        String normalizedMode = normalizeMode(mode);
        ArrayList<String> lines = new ArrayList<>();
        String focusPath = focusPath(normalizedMode, string(state, "previousMode", EchoAgent5UiReference.WIKI_SCREEN));
        String moduleRendererClass = "";
        switch (normalizedMode) {
            case "TERMINAL" -> moduleRendererClass = addModuleLines(
                    lines,
                    EchoAgent5UiModuleSurfaceRenderers.renderTerminal(state, source)
            );
            case "INDEX" -> moduleRendererClass = addModuleLines(
                    lines,
                    EchoAgent5UiModuleSurfaceRenderers.renderIndex(state, source)
            );
            case "LENS" -> moduleRendererClass = addModuleLines(
                    lines,
                    EchoAgent5UiModuleSurfaceRenderers.renderLens(state, source)
            );
            case "MISSION_LOG" -> moduleRendererClass = addModuleLines(
                    lines,
                    EchoAgent5UiModuleSurfaceRenderers.renderMissionLog(state, source)
            );
            case "SETTINGS" -> moduleRendererClass = addModuleLines(
                    lines,
                    EchoAgent5UiModuleSurfaceRenderers.renderSettings(state, source)
            );
            case "PAUSE" -> moduleRendererClass = addModuleLines(
                    lines,
                    EchoAgent5UiModuleSurfaceRenderers.renderPause(state, source)
            );
            case "RECOVERY" -> moduleRendererClass = addModuleLines(
                    lines,
                    EchoAgent5UiModuleSurfaceRenderers.renderRecovery(state, source)
            );
            case "HOLOMAP" -> moduleRendererClass = addModuleLines(
                    lines,
                    EchoAgent5UiModuleSurfaceRenderers.renderHolomap(state, source)
            );
            case "WIKI" -> moduleRendererClass = addModuleLines(
                    lines,
                    EchoAgent5UiModuleSurfaceRenderers.renderWiki(state, source)
            );
            case "HUD" -> moduleRendererClass = addModuleLines(
                    lines,
                    EchoAgent5UiModuleSurfaceRenderers.renderHud(state, source)
            );
            default -> moduleRendererClass = addModuleLines(
                    lines,
                    EchoAgent5UiModuleSurfaceRenderers.renderMainMenu(state, source)
            );
        }
        return new EchoUiSurface(screenId(normalizedMode), titleWithRenderer(title(normalizedMode), moduleRendererClass), List.copyOf(lines), focusPath);
    }

    private static String addModuleLines(List<String> lines, Map<String, Object> model) {
        lines.addAll(strings(model.get("lines")));
        return String.valueOf(model.get("moduleRendererClass"));
    }

    private static String titleWithRenderer(String title, String moduleRendererClass) {
        return moduleRendererClass == null || moduleRendererClass.isBlank() ? title : title + " // " + moduleRendererClass;
    }

    private static String screenId(String mode) {
        return switch (mode) {
            case "TERMINAL" -> EchoAgent5UiReference.TERMINAL_SCREEN;
            case "INDEX" -> EchoAgent5UiReference.INDEX_SCREEN;
            case "LENS" -> EchoAgent5UiReference.LENS_SCREEN;
            case "MISSION_LOG" -> EchoAgent5UiReference.MISSION_LOG_SCREEN;
            case "SETTINGS" -> EchoAgent5UiReference.SETTINGS_SCREEN;
            case "PAUSE" -> EchoAgent5UiReference.PAUSE_FLOW_SCREEN;
            case "RECOVERY" -> EchoAgent5UiReference.DEATH_RECOVERY_SCREEN;
            case "HOLOMAP" -> EchoAgent5UiReference.HOLOMAP_SCREEN;
            case "WIKI" -> EchoAgent5UiReference.WIKI_SCREEN;
            case "HUD" -> EchoAgent5UiReference.HUD_LAYER;
            default -> EchoAgent5UiReference.MAIN_MENU_SCREEN;
        };
    }

    private static String title(String mode) {
        return switch (mode) {
            case "TERMINAL" -> "Terminal";
            case "INDEX" -> "Index";
            case "LENS" -> "Lens";
            case "MISSION_LOG" -> "Mission Log";
            case "SETTINGS" -> "Settings";
            case "PAUSE" -> "Pause";
            case "RECOVERY" -> "Death Recovery";
            case "HOLOMAP" -> "HoloMap";
            case "WIKI" -> "Wiki";
            case "HUD" -> "HUD";
            default -> "ECHO Ashfall";
        };
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeMode(String mode) {
        String normalized = normalize(mode).toUpperCase(java.util.Locale.ROOT);
        return normalized.isBlank() ? "TERMINAL" : normalized;
    }

    private static String focusPath(String mode, String previousMode) {
        return switch (mode) {
            case "TERMINAL" -> "terminal:input";
            case "INDEX" -> "index:search";
            case "LENS" -> "lens:scan";
            case "RECOVERY" -> "recovery:recover";
            case "PAUSE" -> "pause:resume:" + (previousMode == null || previousMode.isBlank()
                    ? EchoAgent5UiReference.WIKI_SCREEN
                    : previousMode);
            default -> mode.toLowerCase(java.util.Locale.ROOT) + ":surface";
        };
    }

    private static String focusLabel(String focusPath, Map<String, Object> state) {
        return focusPath.equals(string(state, "focusedControl", ""))
                && (bool(state, "mouseRouted") || bool(state, "initialFocusRouted"))
                ? focusPath + " ready"
                : focusPath + " waiting";
    }

    private static String typedOrPlaceholder(String value) {
        return value == null || value.isBlank() ? "_" : value;
    }

    private static String string(Map<String, Object> values, String key, String fallback) {
        if (values == null) {
            return fallback;
        }
        Object value = values.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private static boolean bool(Map<String, Object> values, String key) {
        return values != null && Boolean.TRUE.equals(values.get(key));
    }

    @SuppressWarnings("unchecked")
    private static List<String> strings(Object value) {
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }
}
