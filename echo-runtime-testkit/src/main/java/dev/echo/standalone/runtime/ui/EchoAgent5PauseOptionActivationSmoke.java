package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5PauseOptionActivationSmoke {
    private EchoAgent5PauseOptionActivationSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> resume = EchoAgent5UiActionRouter.routePauseOption("Resume", "LENS");
        Map<String, Object> settings = EchoAgent5UiActionRouter.routePauseOption("Settings", "LENS");
        Map<String, Object> quit = EchoAgent5UiActionRouter.routePauseOption("Quit to Main Menu", "LENS");

        List<String> selectedOptions = List.of(
                String.valueOf(resume.get("selectedOption")),
                String.valueOf(settings.get("selectedOption")),
                String.valueOf(quit.get("selectedOption"))
        );
        List<String> destinations = List.of(
                String.valueOf(resume.get("destinationMode")),
                String.valueOf(settings.get("destinationMode")),
                String.valueOf(quit.get("destinationMode"))
        );
        List<String> effects = List.of(
                String.valueOf(resume.get("effect")),
                String.valueOf(settings.get("effect")),
                String.valueOf(quit.get("effect"))
        );
        EchoUiSurface renderedPause = EchoAgent5UiSurfaceRenderer.render("PAUSE", Map.of(
                "previousMode", "LENS",
                "selectedOption", "Settings"
        ), source);

        boolean passed = Boolean.TRUE.equals(resume.get("handled"))
                && Boolean.TRUE.equals(settings.get("handled"))
                && Boolean.TRUE.equals(quit.get("handled"))
                && selectedOptions.equals(List.of("Resume", "Settings", "Quit to Main Menu"))
                && destinations.equals(List.of("LENS", "SETTINGS", "MAIN_MENU"))
                && effects.equals(List.of("pause:resume", "pause:settings", "pause:main_menu"))
                && renderedPause.lines().stream().anyMatch(line -> line.contains("Selected: Settings"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("pauseOptionActivationSmokeClass", EchoAgent5PauseOptionActivationSmoke.class.getSimpleName());
        smoke.put("serviceCodeExecuted", true);
        smoke.put("adapterCoreBridge", true);
        smoke.put("selectedOptions", selectedOptions);
        smoke.put("destinations", destinations);
        smoke.put("effects", effects);
        smoke.put("renderedLines", renderedPause.lines());
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }
}
