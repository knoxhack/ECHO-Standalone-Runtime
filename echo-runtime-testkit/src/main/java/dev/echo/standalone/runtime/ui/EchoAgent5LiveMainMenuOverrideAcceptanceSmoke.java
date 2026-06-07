package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveMainMenuOverrideAcceptanceSmoke {
    private static final String SCREEN_CLASS = "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost";

    private EchoAgent5LiveMainMenuOverrideAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> override = override(true, true, "", source);
        Map<String, Object> surface = liveSurface(true, "MAIN_MENU");
        Map<String, Object> options = EchoAgent5MainMenuOptionActivationSmoke.capture(source);
        Map<String, Object> endToEnd = EchoAgent5MainMenuEndToEndAcceptance.assess(override, options);
        Map<String, Object> accepted = EchoAgent5LiveMainMenuOverrideAcceptance.assess(
                override,
                surface,
                endToEnd
        );
        Map<String, Object> rejectedNoTitle = EchoAgent5LiveMainMenuOverrideAcceptance.assess(
                override(false, false, "current_screen_not_title:PauseScreen", source),
                liveSurface(false, "MAIN_MENU"),
                endToEnd
        );
        Map<String, Object> rejectedNoSurface = EchoAgent5LiveMainMenuOverrideAcceptance.assess(
                override,
                liveSurface(false, "MAIN_MENU"),
                endToEnd
        );
        Map<String, Object> rejectedNoOptions = EchoAgent5LiveMainMenuOverrideAcceptance.assess(
                override,
                surface,
                Map.of("accepted", false, "selectedOptions", java.util.List.of())
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_main_menu_override:accepted:MAIN_MENU:4".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoTitle.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoSurface.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoOptions.get("accepted"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveMainMenuOverrideAcceptanceSmokeClass",
                EchoAgent5LiveMainMenuOverrideAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoTitle", rejectedNoTitle);
        smoke.put("rejectedNoSurface", rejectedNoSurface);
        smoke.put("rejectedNoOptions", rejectedNoOptions);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static Map<String, Object> override(
            boolean titleDetected,
            boolean attached,
            String skipReason,
            EchoAgent5UiDataSources source
    ) {
        return EchoAgent5MainMenuOverrideSmoke.capture(
                titleDetected,
                attached,
                skipReason,
                SCREEN_CLASS,
                "echoashfallprotocol",
                92,
                20,
                1,
                1,
                source
        );
    }

    private static Map<String, Object> liveSurface(boolean accepted, String surface) {
        return EchoAgent5LiveSurfaceAcceptance.assess(
                accepted,
                SCREEN_CLASS,
                SCREEN_CLASS,
                surface,
                surface
        );
    }
}
