package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveMissionObjectiveAcceptanceSmoke {
    private EchoAgent5LiveMissionObjectiveAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> missionLog = object(EchoAgent5MissionLogEndToEndAcceptanceSmoke.capture(source)
                .get("accepted"));
        Map<String, Object> hudUpdate = EchoAgent5HudUpdateSmoke.capture(source);
        Map<String, Object> accepted = EchoAgent5LiveMissionObjectiveAcceptance.assess(
                missionLog,
                hudUpdate,
                source
        );
        Map<String, Object> rejectedNoMission = EchoAgent5LiveMissionObjectiveAcceptance.assess(
                Map.of("accepted", false),
                hudUpdate,
                source
        );
        Map<String, Object> rejectedNoHud = EchoAgent5LiveMissionObjectiveAcceptance.assess(
                missionLog,
                Map.of("passed", false),
                source
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && ("live_mission_objective:accepted:MISSION_ACTION/HUD:"
                + EchoAgent5UiReference.ACTIVE_MISSION_ID + ":UPDATED").equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoMission.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoHud.get("accepted"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveMissionObjectiveAcceptanceSmokeClass",
                EchoAgent5LiveMissionObjectiveAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoMission", rejectedNoMission);
        smoke.put("rejectedNoHud", rejectedNoHud);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }
}
