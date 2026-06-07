package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5ListNavigationSmoke {
    private EchoAgent5ListNavigationSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> mainMenuDown = EchoAgent5UiActionRouter.routeListNavigation("DOWN", "MAIN_MENU", 0, source);
        Map<String, Object> mainMenuDownAgain = EchoAgent5UiActionRouter.routeListNavigation(
                "DOWN",
                "MAIN_MENU",
                integer(mainMenuDown.get("selectedIndex")),
                source
        );
        Map<String, Object> settingsDown = EchoAgent5UiActionRouter.routeListNavigation("DOWN", "SETTINGS", 0, source);
        Map<String, Object> settingsDownAgain = EchoAgent5UiActionRouter.routeListNavigation(
                "DOWN",
                "SETTINGS",
                integer(settingsDown.get("selectedIndex")),
                source
        );
        Map<String, Object> pauseUp = EchoAgent5UiActionRouter.routeListNavigation("UP", "PAUSE", 0, source);

        List<String> selectedOptions = List.of(
                String.valueOf(mainMenuDown.get("selectedOption")),
                String.valueOf(mainMenuDownAgain.get("selectedOption")),
                String.valueOf(settingsDown.get("selectedOption")),
                String.valueOf(settingsDownAgain.get("selectedOption")),
                String.valueOf(pauseUp.get("selectedOption"))
        );
        List<String> effects = List.of(
                String.valueOf(mainMenuDown.get("effect")),
                String.valueOf(mainMenuDownAgain.get("effect")),
                String.valueOf(settingsDown.get("effect")),
                String.valueOf(settingsDownAgain.get("effect")),
                String.valueOf(pauseUp.get("effect"))
        );

        ArrayList<String> renderedLines = new ArrayList<>();
        renderedLines.addAll(EchoAgent5UiSurfaceRenderer.render("MAIN_MENU", state(mainMenuDownAgain), source).lines());
        renderedLines.addAll(EchoAgent5UiSurfaceRenderer.render("SETTINGS", state(settingsDownAgain), source).lines());
        renderedLines.addAll(EchoAgent5UiSurfaceRenderer.render("PAUSE", state(pauseUp), source).lines());

        boolean passed = selectedOptions.equals(List.of(
                "New Ashfall Run",
                "Settings",
                "Theme",
                "Input Mode",
                "Quit to Main Menu"
        ))
                && effects.containsAll(List.of(
                        "list:main_menu:down",
                        "list:settings:down",
                        "list:pause:up"
                ))
                && renderedLines.stream().anyMatch(line -> line.contains("Selected: Settings"))
                && renderedLines.stream().anyMatch(line -> line.contains("Selected: Input Mode"))
                && renderedLines.stream().anyMatch(line -> line.contains("Selected: Quit to Main Menu"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("listNavigationSmokeClass", EchoAgent5ListNavigationSmoke.class.getSimpleName());
        smoke.put("serviceCodeExecuted", true);
        smoke.put("adapterCoreBridge", true);
        smoke.put("selectedOptions", selectedOptions);
        smoke.put("effects", effects);
        smoke.put("renderedLines", List.copyOf(renderedLines));
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static Map<String, Object> state(Map<String, Object> navigation) {
        return Map.of(
                "selectedIndex", navigation.get("selectedIndex"),
                "selectedOption", navigation.get("selectedOption")
        );
    }

    private static int integer(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }
}
