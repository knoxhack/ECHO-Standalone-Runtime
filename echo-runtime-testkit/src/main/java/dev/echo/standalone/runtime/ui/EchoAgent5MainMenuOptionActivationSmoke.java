package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5MainMenuOptionActivationSmoke {
    private EchoAgent5MainMenuOptionActivationSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> continueRoute = EchoAgent5UiActionRouter.routeMainMenuOption("Continue");
        Map<String, Object> newRunRoute = EchoAgent5UiActionRouter.routeMainMenuOption("New Ashfall Run");
        Map<String, Object> settingsRoute = EchoAgent5UiActionRouter.routeMainMenuOption("Settings");
        Map<String, Object> quitRoute = EchoAgent5UiActionRouter.routeMainMenuOption("Quit");
        EchoUiSurface rendered = EchoAgent5UiSurfaceRenderer.render("MAIN_MENU", Map.of(
                "selectedOption", settingsRoute.get("selectedOption"),
                "mainMenuOutput", settingsRoute.get("mainMenuOutput")
        ), source);

        List<String> selectedOptions = List.of(
                String.valueOf(continueRoute.get("selectedOption")),
                String.valueOf(newRunRoute.get("selectedOption")),
                String.valueOf(settingsRoute.get("selectedOption")),
                String.valueOf(quitRoute.get("selectedOption"))
        );
        List<String> destinations = List.of(
                String.valueOf(continueRoute.get("destinationMode")),
                String.valueOf(newRunRoute.get("destinationMode")),
                String.valueOf(settingsRoute.get("destinationMode")),
                String.valueOf(quitRoute.get("destinationMode"))
        );
        List<String> effects = List.of(
                String.valueOf(continueRoute.get("effect")),
                String.valueOf(newRunRoute.get("effect")),
                String.valueOf(settingsRoute.get("effect")),
                String.valueOf(quitRoute.get("effect"))
        );

        boolean passed = List.of(continueRoute, newRunRoute, settingsRoute, quitRoute).stream()
                .allMatch(route -> Boolean.TRUE.equals(route.get("handled")))
                && selectedOptions.equals(List.of("Continue", "New Ashfall Run", "Settings", "Quit"))
                && destinations.equals(List.of(
                        EchoAgent5UiReference.WIKI_SCREEN,
                        EchoAgent5UiReference.MISSION_LOG_SCREEN,
                        EchoAgent5UiReference.SETTINGS_SCREEN,
                        EchoAgent5UiReference.MAIN_MENU_SCREEN
                ))
                && effects.equals(List.of("main_menu:continue", "main_menu:new_run", "main_menu:settings", "main_menu:quit_requested"))
                && Boolean.TRUE.equals(quitRoute.get("quitRequested"))
                && rendered.lines().stream().anyMatch(line -> line.contains("Selected: Settings"))
                && rendered.lines().stream().anyMatch(line -> line.contains("Action: Settings selected: opening Settings"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("mainMenuOptionActivationSmokeClass", EchoAgent5MainMenuOptionActivationSmoke.class.getSimpleName());
        smoke.put("serviceCodeExecuted", true);
        smoke.put("adapterCoreBridge", true);
        smoke.put("selectedOptions", selectedOptions);
        smoke.put("destinations", destinations);
        smoke.put("effects", effects);
        smoke.put("quitRequested", quitRoute.get("quitRequested"));
        smoke.put("renderedLines", rendered.lines());
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }
}
