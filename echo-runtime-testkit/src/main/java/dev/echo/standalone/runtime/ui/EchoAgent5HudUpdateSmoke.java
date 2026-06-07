package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5HudUpdateSmoke {
    private EchoAgent5HudUpdateSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> update = EchoAgent5UiActionRouter.routeHudUpdate(Map.of("hudHealth", 92), source);
        Map<String, Object> state = Map.of(
                "hudHealth", update.get("hudHealth"),
                "hudHazard", update.get("hudHazard"),
                "hudMission", update.get("hudMission"),
                "hudUpdateOutput", update.get("hudUpdateOutput")
        );
        EchoUiSurface surface = EchoAgent5UiSurfaceRenderer.render("HUD", state, source);
        Map<String, Object> host = EchoAgent5UiScreenHostModel.render(
                "HUD",
                state,
                "ashfall",
                12,
                3,
                2,
                1,
                source
        );

        boolean passed = Boolean.TRUE.equals(update.get("handled"))
                && Integer.valueOf(85).equals(update.get("hudHealth"))
                && String.valueOf(source.hudValues().get("hazard")).equals(update.get("hudHazard"))
                && "hud:update:health_hazard_mission".equals(update.get("effect"))
                && surface.lines().stream().anyMatch(line -> line.contains("HUD overlay is live. Health 85"))
                && surface.lines().stream()
                .anyMatch(line -> line.contains("Hazard: " + source.hudValues().get("hazard")))
                && strings(host, "headerLines").stream()
                .anyMatch(line -> line.contains("HUD: Health 85 / " + source.hudValues().get("hazard")));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("hudUpdateSmokeClass", EchoAgent5HudUpdateSmoke.class.getSimpleName());
        smoke.put("serviceCodeExecuted", true);
        smoke.put("adapterCoreBridge", true);
        smoke.put("hudHealth", update.get("hudHealth"));
        smoke.put("hudHazard", update.get("hudHazard"));
        smoke.put("hudMission", update.get("hudMission"));
        smoke.put("effect", update.get("effect"));
        smoke.put("surfaceLines", surface.lines());
        smoke.put("hostHeaderLines", strings(host, "headerLines"));
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    @SuppressWarnings("unchecked")
    private static List<String> strings(Map<String, Object> model, String key) {
        Object value = model.get(key);
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }
}
