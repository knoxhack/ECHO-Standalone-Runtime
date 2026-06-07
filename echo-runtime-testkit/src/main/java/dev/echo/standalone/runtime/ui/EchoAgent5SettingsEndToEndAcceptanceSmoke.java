package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5SettingsEndToEndAcceptanceSmoke {
    private EchoAgent5SettingsEndToEndAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> settingsAction = routeAction("SETTINGS_ACTION", "SETTINGS", "settings.open");
        Map<String, Object> liveSurface = EchoAgent5LiveSurfaceAcceptance.assess(
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "SETTINGS",
                "SETTINGS"
        );
        Map<String, Object> menuInput = menuInput("SETTINGS");
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                "SETTINGS",
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
        Map<String, Object> adjustment = EchoAgent5SettingsAdjustmentSmoke.capture(source);
        Map<String, Object> accepted = EchoAgent5SettingsEndToEndAcceptance.assess(
                settingsAction,
                menuInput,
                render,
                interaction,
                adjustment,
                source
        );
        Map<String, Object> rejectedNoInput = EchoAgent5SettingsEndToEndAcceptance.assess(
                settingsAction,
                Map.of("accepted", false, "surface", "SETTINGS"),
                render,
                interaction,
                adjustment,
                source
        );
        Map<String, Object> rejectedNoRender = EchoAgent5SettingsEndToEndAcceptance.assess(
                settingsAction,
                menuInput,
                Map.of("accepted", false, "surface", "SETTINGS"),
                interaction,
                adjustment,
                source
        );
        Map<String, Object> rejectedNoInteraction = EchoAgent5SettingsEndToEndAcceptance.assess(
                settingsAction,
                menuInput,
                render,
                Map.of("passed", false, "steps", java.util.List.of()),
                adjustment,
                source
        );
        Map<String, Object> rejectedNoAdjustment = EchoAgent5SettingsEndToEndAcceptance.assess(
                settingsAction,
                menuInput,
                render,
                interaction,
                Map.of("passed", false),
                source
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "settings_end_to_end:SETTINGS_ACTION->SETTINGS:ashfall-accessible:subtitles_off".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoInput.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRender.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoInteraction.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoAdjustment.get("accepted"));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("settingsEndToEndAcceptanceSmokeClass",
                EchoAgent5SettingsEndToEndAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoInput", rejectedNoInput);
        smoke.put("rejectedNoRender", rejectedNoRender);
        smoke.put("rejectedNoInteraction", rejectedNoInteraction);
        smoke.put("rejectedNoAdjustment", rejectedNoAdjustment);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static Map<String, Object> routeAction(String key, String surface, String action) {
        Map<String, Object> route = new LinkedHashMap<>();
        route.put("handled", true);
        route.put("key", key);
        route.put("surface", surface);
        route.put("action", action);
        route.put("source", "menu_action");
        return Map.copyOf(route);
    }

    private static Map<String, Object> menuInput(String surface) {
        return Map.of(
                "accepted", true,
                "surface", surface,
                "source", "menu_action",
                "serviceCodeExecuted", true
        );
    }
}
