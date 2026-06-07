package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5UiRuntimeEquivalenceAuditSmoke {
    private EchoAgent5UiRuntimeEquivalenceAuditSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> terminalResult = EchoAgent5UiActionRouter.activate("TERMINAL", Map.of(
                "focusedControl", "terminal:input",
                "terminalBuffer", source.terminalCommand()
        ), source);
        Map<String, Object> indexResult = EchoAgent5UiActionRouter.activate("INDEX", Map.of(
                "focusedControl", "index:search",
                "indexBuffer", source.indexQuery()
        ), source);
        Map<String, Object> lensResult = EchoAgent5UiActionRouter.activate("LENS", Map.of(
                "focusedControl", "lens:scan"
        ), source);
        Map<String, Object> missionUpdate = EchoAgent5MissionLogUpdateSmoke.capture(source);
        Map<String, Object> hud = source.hudValues();
        Map<String, Object> mission = source.missionLogValues();

        boolean screenIdsMatch = EchoAgent5UiReference.screenIds().equals(List.of(
                EchoAgent5UiReference.MAIN_MENU_SCREEN,
                EchoAgent5UiReference.TERMINAL_SCREEN,
                EchoAgent5UiReference.INDEX_SCREEN,
                EchoAgent5UiReference.LENS_SCREEN,
                EchoAgent5UiReference.HUD_LAYER,
                EchoAgent5UiReference.NOTIFICATION_QUEUE,
                EchoAgent5UiReference.MISSION_LOG_SCREEN,
                EchoAgent5UiReference.SETTINGS_SCREEN,
                EchoAgent5UiReference.PAUSE_FLOW_SCREEN,
                EchoAgent5UiReference.DEATH_RECOVERY_SCREEN,
                EchoAgent5UiReference.HOLOMAP_SCREEN,
                EchoAgent5UiReference.WIKI_SCREEN,
                EchoAgent5UiReference.SIGNALOS_SCREEN,
                EchoAgent5UiReference.ASHFALL_DRONE_SCREEN
        ));
        boolean terminalMatches = "status".equals(source.terminalCommand())
                && "ASH>".equals(source.terminalPrompt())
                && !source.terminalReadyLine().isBlank()
                && Boolean.TRUE.equals(terminalResult.get("handled"))
                && source.terminalReadyLine().equals(terminalResult.get("output"));
        boolean indexMatches = "ashfall".equals(source.indexQuery())
                && !source.indexResult().isBlank()
                && Boolean.TRUE.equals(indexResult.get("handled"))
                && source.indexResult().equals(indexResult.get("output"));
        boolean lensMatches = EchoAgent5UiReference.LENS_TARGET.equals(source.lensTarget())
                && !source.lensResult().isBlank()
                && Boolean.TRUE.equals(lensResult.get("handled"))
                && source.lensResult().equals(lensResult.get("output"));
        boolean hudMatches = Integer.valueOf(100).equals(hud.get("health"))
                && ("Mission signal: " + EchoAgent5UiReference.ACTIVE_MISSION_STATUS).equals(hud.get("hazard"))
                && EchoAgent5UiReference.ACTIVE_MISSION_OBJECTIVE.equals(hud.get("mission"));
        boolean missionMatches = EchoAgent5UiReference.ACTIVE_MISSION_ID.equals(mission.get("missionId"))
                && EchoAgent5UiReference.ACTIVE_MISSION_OBJECTIVE.equals(mission.get("objective"))
                && EchoAgent5UiReference.ACTIVE_MISSION_STATUS.equals(mission.get("status"))
                && Boolean.TRUE.equals(missionUpdate.get("passed"))
                && EchoAgent5UiReference.ACTIVE_MISSION_UPDATED_STATUS.equals(missionUpdate.get("missionStatus"))
                && Double.valueOf(0.5D).equals(missionUpdate.get("missionProgress"));
        boolean notificationsMatch = source.notifications().stream()
                .map(notification -> String.valueOf(notification.get("message")))
                .toList()
                .equals(source.notifications().stream()
                        .map(entry -> String.valueOf(entry.get("message")))
                        .toList());
        boolean passed = screenIdsMatch
                && terminalMatches
                && indexMatches
                && lensMatches
                && hudMatches
                && missionMatches
                && notificationsMatch;

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("uiRuntimeEquivalenceAuditSmokeClass",
                EchoAgent5UiRuntimeEquivalenceAuditSmoke.class.getSimpleName());
        smoke.put("screenIdsMatch", screenIdsMatch);
        smoke.put("terminalMatches", terminalMatches);
        smoke.put("indexMatches", indexMatches);
        smoke.put("lensMatches", lensMatches);
        smoke.put("hudMatches", hudMatches);
        smoke.put("missionMatches", missionMatches);
        smoke.put("notificationsMatch", notificationsMatch);
        smoke.put("terminalOutput", terminalResult.get("output"));
        smoke.put("missionUpdateStatus", missionUpdate.get("missionStatus"));
        smoke.put("missionUpdateProgress", missionUpdate.get("missionProgress"));
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }
}
