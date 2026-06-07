package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5RecoveryEndToEndAcceptanceSmoke {
    private EchoAgent5RecoveryEndToEndAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> recoveryAction = routeAction("RECOVERY_ACTION", "RECOVERY", "recovery.recover");
        Map<String, Object> liveSurface = EchoAgent5LiveSurfaceAcceptance.assess(
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "RECOVERY",
                "RECOVERY"
        );
        Map<String, Object> menuInput = menuInput("RECOVERY");
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                "RECOVERY",
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "echoashfallprotocol",
                92,
                20,
                1,
                1,
                source,
                Map.of()
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
        Map<String, Object> accepted = EchoAgent5RecoveryEndToEndAcceptance.assess(
                recoveryAction,
                menuInput,
                render,
                interaction
        );
        Map<String, Object> rejectedNoInput = EchoAgent5RecoveryEndToEndAcceptance.assess(
                recoveryAction,
                Map.of("accepted", false, "surface", "RECOVERY"),
                render,
                interaction
        );
        Map<String, Object> rejectedNoRender = EchoAgent5RecoveryEndToEndAcceptance.assess(
                recoveryAction,
                menuInput,
                Map.of("accepted", false, "surface", "RECOVERY"),
                interaction
        );
        Map<String, Object> rejectedNoInteraction = EchoAgent5RecoveryEndToEndAcceptance.assess(
                recoveryAction,
                menuInput,
                render,
                Map.of("passed", false, "steps", List.of())
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "recovery_end_to_end:RECOVERY_ACTION->RECOVERY:RECOVERED".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoInput.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRender.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoInteraction.get("accepted"));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("recoveryEndToEndAcceptanceSmokeClass",
                EchoAgent5RecoveryEndToEndAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoInput", rejectedNoInput);
        smoke.put("rejectedNoRender", rejectedNoRender);
        smoke.put("rejectedNoInteraction", rejectedNoInteraction);
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
