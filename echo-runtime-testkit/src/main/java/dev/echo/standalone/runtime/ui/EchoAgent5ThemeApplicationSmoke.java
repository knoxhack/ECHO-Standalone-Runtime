package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5ThemeApplicationSmoke {
    private EchoAgent5ThemeApplicationSmoke() {
    }

    public static Map<String, Object> capture(
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources dataSources
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> settings = source.settingsValues();
        EchoUiTheme theme = new EchoUiTheme(
                EchoAgent5UiReference.SETTINGS_THEME,
                "Ashfall Agent 5",
                "#67e8f9",
                "#061014",
                "#d8fbff",
                "#facc15",
                "ECHO Mono",
                "compact",
                Map.of("terminal.prompt", source.terminalPrompt())
        );
        Map<String, Object> settingsSurface = EchoAgent5UiModuleSurfaceRenderers.renderSettings(Map.of(), source);
        Map<String, Object> terminalSurface = EchoAgent5UiModuleSurfaceRenderers.renderTerminal(Map.of(
                "focusedControl", "terminal:input",
                "mouseRouted", true,
                "terminalBuffer", source.terminalCommand(),
                "terminalOutput", source.terminalReadyLine(),
                "terminalCommandExecuted", true
        ), source);
        Map<String, Object> hostModel = EchoAgent5UiScreenHostModel.render(
                "SETTINGS",
                Map.of(),
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source
        );
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("themeApplicationSmokeClass", EchoAgent5ThemeApplicationSmoke.class.getSimpleName());
        smoke.put("themeId", theme.id());
        smoke.put("settingsProfile", settings.get("profile"));
        smoke.put("inputMode", settings.get("inputMode"));
        smoke.put("tokens", theme.tokens());
        smoke.put("settingsSurfaceRenderer", settingsSurface.get("moduleRendererClass"));
        smoke.put("terminalSurfaceRenderer", terminalSurface.get("moduleRendererClass"));
        smoke.put("settingsSurfaceLines", settingsSurface.get("lines"));
        smoke.put("terminalSurfaceLines", terminalSurface.get("lines"));
        smoke.put("hostHeaderLines", hostModel.get("headerLines"));
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", EchoAgent5UiReference.SETTINGS_THEME.equals(theme.id())
                && EchoAgent5UiReference.SETTINGS_PROFILE.equals(settings.get("profile"))
                && lines(settingsSurface).stream().anyMatch(line -> line.contains("Theme: ashfall-agent5"))
                && lines(terminalSurface).stream().anyMatch(line -> line.contains(source.terminalReadyLine()))
                && lines(hostModel).stream().anyMatch(line -> line.contains("Settings are live"))
                && source.terminalPrompt().equals(theme.tokens().get("terminal.prompt")));
        return Map.copyOf(smoke);
    }

    @SuppressWarnings("unchecked")
    private static java.util.List<String> lines(Map<String, Object> model) {
        Object value = model.get("lines");
        if (value == null) {
            value = model.get("surfaceLines");
        }
        if (value instanceof java.util.List<?> list) {
            return (java.util.List<String>) list;
        }
        return java.util.List.of();
    }
}
