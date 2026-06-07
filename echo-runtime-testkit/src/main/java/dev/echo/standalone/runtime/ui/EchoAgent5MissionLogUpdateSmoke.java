package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5MissionLogUpdateSmoke {
    private EchoAgent5MissionLogUpdateSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> update = EchoAgent5UiActionRouter.routeMissionLogUpdate(Map.of(
                "missionProgress", 0.25D,
                "missionStatus", EchoAgent5UiReference.ACTIVE_MISSION_STATUS
        ), source);
        EchoUiSurface rendered = EchoAgent5UiSurfaceRenderer.render("MISSION_LOG", update, source);

        boolean passed = Boolean.TRUE.equals(update.get("handled"))
                && EchoAgent5UiReference.ACTIVE_MISSION_ID.equals(update.get("missionId"))
                && Double.valueOf(0.5D).equals(update.get("missionProgress"))
                && EchoAgent5UiReference.ACTIVE_MISSION_UPDATED_STATUS.equals(update.get("missionStatus"))
                && EchoAgent5UiReference.ACTIVE_MISSION_UPDATE_LINE.equals(update.get("missionUpdateLine"))
                && ("mission:update:" + EchoAgent5UiReference.ACTIVE_MISSION_ID).equals(update.get("effect"))
                && rendered.lines().stream().anyMatch(line -> line.contains("Status: UPDATED    Progress: 50%"))
                && rendered.lines().stream()
                .anyMatch(line -> line.contains("Update: " + EchoAgent5UiReference.ACTIVE_MISSION_UPDATE_LINE));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("missionLogUpdateSmokeClass", EchoAgent5MissionLogUpdateSmoke.class.getSimpleName());
        smoke.put("serviceCodeExecuted", true);
        smoke.put("adapterCoreBridge", true);
        smoke.put("missionId", update.get("missionId"));
        smoke.put("missionStatus", update.get("missionStatus"));
        smoke.put("missionProgress", update.get("missionProgress"));
        smoke.put("effect", update.get("effect"));
        smoke.put("renderedLines", rendered.lines());
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }
}
