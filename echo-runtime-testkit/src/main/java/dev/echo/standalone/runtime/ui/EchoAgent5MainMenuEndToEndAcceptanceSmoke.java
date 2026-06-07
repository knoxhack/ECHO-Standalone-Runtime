package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5MainMenuEndToEndAcceptanceSmoke {
    private EchoAgent5MainMenuEndToEndAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> override = EchoAgent5MainMenuOverrideSmoke.capture(
                true,
                true,
                "",
                "dev.echo.nativeplatform.generated.EchoNativeDashboardScreen",
                "ashfall",
                12,
                3,
                2,
                1,
                source
        );
        Map<String, Object> options = EchoAgent5MainMenuOptionActivationSmoke.capture(source);
        Map<String, Object> accepted = EchoAgent5MainMenuEndToEndAcceptance.assess(override, options);
        Map<String, Object> rejectedNoOverride = EchoAgent5MainMenuEndToEndAcceptance.assess(
                EchoAgent5MainMenuOverrideSmoke.capture(
                        false,
                        false,
                        "current_screen_not_title:example",
                        "dev.echo.nativeplatform.generated.EchoNativeDashboardScreen",
                        "ashfall",
                        12,
                        3,
                        2,
                        1,
                        source
                ),
                options
        );
        Map<String, Object> rejectedNoOptions = EchoAgent5MainMenuEndToEndAcceptance.assess(
                override,
                rejectedOptions()
        );
        Map<String, Object> rejectedNoQuit = EchoAgent5MainMenuEndToEndAcceptance.assess(
                override,
                withoutQuit(options)
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "main_menu_end_to_end:accepted:4".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoOverride.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoOptions.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoQuit.get("accepted"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("mainMenuEndToEndAcceptanceSmokeClass",
                EchoAgent5MainMenuEndToEndAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoOverride", rejectedNoOverride);
        smoke.put("rejectedNoOptions", rejectedNoOptions);
        smoke.put("rejectedNoQuit", rejectedNoQuit);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static Map<String, Object> rejectedOptions() {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("passed", false);
        options.put("selectedOptions", List.of());
        options.put("destinations", List.of());
        options.put("effects", List.of());
        options.put("quitRequested", false);
        options.put("renderedLines", List.of());
        return Map.copyOf(options);
    }

    private static Map<String, Object> withoutQuit(Map<String, Object> options) {
        Map<String, Object> copy = new LinkedHashMap<>(options);
        copy.put("quitRequested", false);
        copy.put("effects", List.of("main_menu:continue", "main_menu:new_run", "main_menu:settings", ""));
        copy.put("passed", false);
        return Map.copyOf(copy);
    }
}
