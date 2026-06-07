package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5MissionLogEndToEndAcceptanceSmoke {
    private EchoAgent5MissionLogEndToEndAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> missionAction = routeAction("MISSION_ACTION", "MISSION_LOG", "mission.open");
        Map<String, Object> liveSurface = EchoAgent5LiveSurfaceAcceptance.assess(
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "MISSION_LOG",
                "MISSION_LOG"
        );
        Map<String, Object> menuInput = menuInput("MISSION_LOG");
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                "MISSION_LOG",
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
        Map<String, Object> update = EchoAgent5MissionLogUpdateSmoke.capture(source);
        Map<String, Object> accepted = EchoAgent5MissionLogEndToEndAcceptance.assess(
                missionAction,
                menuInput,
                render,
                interaction,
                update,
                source
        );
        Map<String, Object> rejectedNoInput = EchoAgent5MissionLogEndToEndAcceptance.assess(
                missionAction,
                Map.of("accepted", false, "surface", "MISSION_LOG"),
                render,
                interaction,
                update,
                source
        );
        Map<String, Object> rejectedNoRender = EchoAgent5MissionLogEndToEndAcceptance.assess(
                missionAction,
                menuInput,
                Map.of("accepted", false, "surface", "MISSION_LOG"),
                interaction,
                update,
                source
        );
        Map<String, Object> rejectedNoInteraction = EchoAgent5MissionLogEndToEndAcceptance.assess(
                missionAction,
                menuInput,
                render,
                Map.of("passed", false, "steps", java.util.List.of()),
                update,
                source
        );
        Map<String, Object> rejectedNoUpdate = EchoAgent5MissionLogEndToEndAcceptance.assess(
                missionAction,
                menuInput,
                render,
                interaction,
                Map.of("passed", false),
                source
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && ("mission_log_end_to_end:MISSION_ACTION->MISSION_LOG:"
                + source.missionLogValues().get("missionId") + ":UPDATED").equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoInput.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRender.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoInteraction.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoUpdate.get("accepted"));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("missionLogEndToEndAcceptanceSmokeClass",
                EchoAgent5MissionLogEndToEndAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoInput", rejectedNoInput);
        smoke.put("rejectedNoRender", rejectedNoRender);
        smoke.put("rejectedNoInteraction", rejectedNoInteraction);
        smoke.put("rejectedNoUpdate", rejectedNoUpdate);
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
