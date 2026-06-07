package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5SettingsAdjustmentSmoke {
    private EchoAgent5SettingsAdjustmentSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> settings = source.settingsValues();
        Map<String, Object> hudScaleNavigation = EchoAgent5UiActionRouter.routeListNavigation("DOWN", "SETTINGS", 2, source);
        Map<String, Object> hudScaleAdjustment = EchoAgent5UiActionRouter.routeSettingsAdjustment(
                String.valueOf(hudScaleNavigation.get("selectedOption")),
                doubleValue(settings.get("hudScale")),
                Boolean.TRUE.equals(settings.get("subtitles"))
        );
        Map<String, Object> subtitlesNavigation = EchoAgent5UiActionRouter.routeListNavigation(
                "DOWN",
                "SETTINGS",
                integer(hudScaleNavigation.get("selectedIndex")),
                source
        );
        Map<String, Object> subtitlesAdjustment = EchoAgent5UiActionRouter.routeSettingsAdjustment(
                String.valueOf(subtitlesNavigation.get("selectedOption")),
                doubleValue(hudScaleAdjustment.get("settingsHudScale")),
                Boolean.TRUE.equals(hudScaleAdjustment.get("settingsSubtitles"))
        );
        EchoUiSurface rendered = EchoAgent5UiSurfaceRenderer.render("SETTINGS", Map.of(
                "selectedOption", subtitlesNavigation.get("selectedOption"),
                "settingsHudScale", subtitlesAdjustment.get("settingsHudScale"),
                "settingsSubtitles", subtitlesAdjustment.get("settingsSubtitles")
        ), source);

        boolean passed = Boolean.TRUE.equals(hudScaleAdjustment.get("handled"))
                && Boolean.TRUE.equals(subtitlesAdjustment.get("handled"))
                && "HUD Scale".equals(hudScaleNavigation.get("selectedOption"))
                && "Subtitles".equals(subtitlesNavigation.get("selectedOption"))
                && Double.valueOf(1.25D).equals(hudScaleAdjustment.get("settingsHudScale"))
                && Boolean.FALSE.equals(subtitlesAdjustment.get("settingsSubtitles"))
                && "settings:hud_scale".equals(hudScaleAdjustment.get("effect"))
                && "settings:subtitles".equals(subtitlesAdjustment.get("effect"))
                && rendered.lines().stream().anyMatch(line -> line.contains("Selected: Subtitles"))
                && rendered.lines().stream().anyMatch(line -> line.contains("HUD scale: 1.25    Subtitles: disabled"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("settingsAdjustmentSmokeClass", EchoAgent5SettingsAdjustmentSmoke.class.getSimpleName());
        smoke.put("serviceCodeExecuted", true);
        smoke.put("adapterCoreBridge", true);
        smoke.put("selectedOptions", List.of(hudScaleNavigation.get("selectedOption"), subtitlesNavigation.get("selectedOption")));
        smoke.put("effects", List.of(hudScaleAdjustment.get("effect"), subtitlesAdjustment.get("effect")));
        smoke.put("settingsHudScale", subtitlesAdjustment.get("settingsHudScale"));
        smoke.put("settingsSubtitles", subtitlesAdjustment.get("settingsSubtitles"));
        smoke.put("renderedLines", rendered.lines());
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static int integer(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    private static double doubleValue(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 1.0D;
    }
}
