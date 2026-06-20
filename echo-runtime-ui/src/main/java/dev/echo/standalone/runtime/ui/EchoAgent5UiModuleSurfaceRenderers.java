package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5UiModuleSurfaceRenderers {
    private EchoAgent5UiModuleSurfaceRenderers() {
    }

    public static Map<String, Object> renderTerminal(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        return EchoAgent5TerminalSurfaceRenderer.render(state, dataSources);
    }

    public static Map<String, Object> renderIndex(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        return EchoAgent5IndexSurfaceRenderer.render(state, dataSources);
    }

    public static Map<String, Object> renderLens(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        return EchoAgent5LensSurfaceRenderer.render(state, dataSources);
    }

    public static Map<String, Object> renderHolomap(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        return EchoAgent5HolomapSurfaceRenderer.render(state, dataSources);
    }

    public static Map<String, Object> renderWiki(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        return EchoAgent5WikiSurfaceRenderer.render(state, dataSources);
    }

    public static Map<String, Object> renderMissionLog(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        return EchoAgent5MissionLogSurfaceRenderer.render(state, dataSources);
    }

    public static Map<String, Object> renderSettings(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        return EchoAgent5SettingsSurfaceRenderer.render(state, dataSources);
    }

    public static Map<String, Object> renderPause(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        return EchoAgent5PauseSurfaceRenderer.render(state, dataSources);
    }

    public static Map<String, Object> renderRecovery(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        return EchoAgent5RecoverySurfaceRenderer.render(state, dataSources);
    }

    public static Map<String, Object> renderMainMenu(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        return EchoAgent5MainMenuSurfaceRenderer.render(state, dataSources);
    }

    public static Map<String, Object> renderHud(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        return EchoAgent5HudSurfaceRenderer.render(state, dataSources);
    }

    static final class EchoAgent5TerminalSurfaceRenderer {
        private EchoAgent5TerminalSurfaceRenderer() {
        }

        static Map<String, Object> render(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        String focusPath = "terminal:input";
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Terminal is live. Click focus, type " + dataSources.terminalCommand() + ", then press Enter.");
        lines.add("Focus: " + focusLabel(focusPath, state)
                + "    Input: " + typedOrPlaceholder(string(state, "terminalBuffer", "")));
        boolean executed = bool(state, "terminalCommandExecuted");
        String output = string(state, "terminalOutput", "awaiting command input");
        lines.add(executed ? dataSources.terminalCommand() + " -> " + output + " host=standalone-runtime" : output);
            return moduleModel("echoterminal", EchoAgent5TerminalSurfaceRenderer.class.getSimpleName(), focusPath, lines);
        }
    }

    static final class EchoAgent5IndexSurfaceRenderer {
        private EchoAgent5IndexSurfaceRenderer() {
        }

        static Map<String, Object> render(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        String focusPath = "index:search";
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Index is live. Click focus, type " + dataSources.indexQuery() + ", then press Enter.");
        lines.add("Focus: " + focusLabel(focusPath, state)
                + "    Query: " + typedOrPlaceholder(string(state, "indexBuffer", "")));
        boolean executed = bool(state, "indexSearchExecuted");
        String output = string(state, "indexOutput", "search field focused");
        lines.add(executed ? dataSources.indexQuery() + " -> " + output + "; first 10 minutes, field scanner, safe routes" : output);
            return moduleModel("echoindex", EchoAgent5IndexSurfaceRenderer.class.getSimpleName(), focusPath, lines);
        }
    }

    static final class EchoAgent5LensSurfaceRenderer {
        private EchoAgent5LensSurfaceRenderer() {
        }

        static Map<String, Object> render(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        String focusPath = "lens:scan";
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Lens is live. Press Enter to scan " + dataSources.lensTarget());
        lines.add("Focus: " + focusLabel(focusPath, state));
        boolean executed = bool(state, "lensScanExecuted");
        String output = string(state, "lensOutput", "target awaiting scan");
        lines.add(executed ? "Scan: " + output : output);
            return moduleModel("echolens", EchoAgent5LensSurfaceRenderer.class.getSimpleName(), focusPath, lines);
        }
    }

    static final class EchoAgent5HolomapSurfaceRenderer {
        private EchoAgent5HolomapSurfaceRenderer() {
        }

        static Map<String, Object> render(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        Map<String, Object> holomap = dataSources.holomapValues();
        ArrayList<String> lines = new ArrayList<>();
        lines.add("HoloMap is live. Layers: " + holomap.get("layer") + " / " + holomap.get("marker"));
        lines.add("Waypoint focus: " + holomap.get("focus"));
        lines.add(string(state, "holomapOutput", dataSources.holomapOutput()));
            return moduleModel("echoholomap", EchoAgent5HolomapSurfaceRenderer.class.getSimpleName(), "holomap:surface", lines);
        }
    }

    static final class EchoAgent5WikiSurfaceRenderer {
        private EchoAgent5WikiSurfaceRenderer() {
        }

        static Map<String, Object> render(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        Map<String, Object> wiki = dataSources.wikiValues();
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Wiki is live. Page: " + wiki.get("guide") + " / " + wiki.get("page"));
        lines.add("Guide link opened from the same AdapterCore UI contract.");
        lines.add(string(state, "wikiOutput", dataSources.wikiOutput()));
            return moduleModel("echowiki", EchoAgent5WikiSurfaceRenderer.class.getSimpleName(), "wiki:surface", lines);
        }
    }

    static final class EchoAgent5MissionLogSurfaceRenderer {
        private EchoAgent5MissionLogSurfaceRenderer() {
        }

        static Map<String, Object> render(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
            Map<String, Object> mission = dataSources.missionLogValues();
            String status = string(state, "missionStatus", String.valueOf(mission.get("status")));
            double progress = doubleValue(stateValue(state, "missionProgress", mission.get("progress")), 0.25D);
            String updateLine = string(state, "missionUpdateLine", "");
            ArrayList<String> lines = new ArrayList<>();
            lines.add("Mission Log is live. Tracking: " + mission.get("title"));
            lines.add("Objective: " + mission.get("objective"));
            lines.add("Status: " + status + "    Progress: " + percent(progress));
            if (!updateLine.isBlank()) {
                lines.add("Update: " + updateLine);
            }
            return moduleModel("echoscreencore", EchoAgent5MissionLogSurfaceRenderer.class.getSimpleName(),
                    "mission_log:surface", lines);
        }
    }

    static final class EchoAgent5SettingsSurfaceRenderer {
        private EchoAgent5SettingsSurfaceRenderer() {
        }

        static Map<String, Object> render(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
            Map<String, Object> settings = dataSources.settingsValues();
            ArrayList<String> lines = new ArrayList<>();
            lines.add("Settings are live. Profile: " + settings.get("profile"));
            lines.add("Theme: " + settings.get("theme") + "    Input: " + settings.get("inputMode"));
            lines.add("Selected: " + selectedOption(state, List.of("Profile", "Theme", "Input Mode", "HUD Scale", "Subtitles")));
            lines.add("HUD scale: " + decimal(stateValue(state, "settingsHudScale", settings.get("hudScale")))
                    + "    Subtitles: " + (booleanValue(stateValue(state, "settingsSubtitles", settings.get("subtitles"))) ? "enabled" : "disabled"));
            return moduleModel("echothemecore", EchoAgent5SettingsSurfaceRenderer.class.getSimpleName(),
                    "settings:surface", lines);
        }
    }

    static final class EchoAgent5PauseSurfaceRenderer {
        private EchoAgent5PauseSurfaceRenderer() {
        }

        static Map<String, Object> render(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
            String previousMode = string(state, "previousMode", EchoAgent5UiReference.WIKI_SCREEN);
            ArrayList<String> lines = new ArrayList<>();
            lines.add("Pause flow is live. Previous screen: " + previousMode);
            lines.add("Selected: " + selectedOption(state, dataSources.pauseOptions()));
            lines.add("Resume, Settings, Save Snapshot, and Quit to Main Menu are routed.");
            lines.add("Press Esc to resume the previous Agent 5 screen.");
            return moduleModel("echoscreencore", EchoAgent5PauseSurfaceRenderer.class.getSimpleName(),
                    "pause:resume:" + previousMode, lines);
        }
    }

    static final class EchoAgent5RecoverySurfaceRenderer {
        private EchoAgent5RecoverySurfaceRenderer() {
        }

        static Map<String, Object> render(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
            Map<String, Object> recovery = dataSources.deathRecoveryValues("WAITING");
            String focusPath = "recovery:recover";
            ArrayList<String> lines = new ArrayList<>();
            lines.add("Death Recovery is live. Press Enter to recover.");
            lines.add("Recovery point: " + recovery.get("recoveryPoint"));
            lines.add("Focus: " + focusLabel(focusPath, state));
            lines.add(string(state, "recoveryOutput", "Status: WAITING"));
            return moduleModel("echoscreencore", EchoAgent5RecoverySurfaceRenderer.class.getSimpleName(), focusPath, lines);
        }
    }

    static final class EchoAgent5MainMenuSurfaceRenderer {
        private EchoAgent5MainMenuSurfaceRenderer() {
        }

        static Map<String, Object> render(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
            ArrayList<String> lines = new ArrayList<>();
            lines.add("Custom main menu surface is live.");
            lines.add("Selected: " + selectedOption(state, dataSources.mainMenuOptions()));
            lines.add(String.join(", ", dataSources.mainMenuOptions()) + " route through the standalone UI host.");
            String output = string(state, "mainMenuOutput", "");
            if (!output.isBlank()) {
                lines.add("Action: " + output);
            }
            return moduleModel("echoscreencore", EchoAgent5MainMenuSurfaceRenderer.class.getSimpleName(),
                    "main_menu:continue", lines);
        }
    }

    static final class EchoAgent5HudSurfaceRenderer {
        private EchoAgent5HudSurfaceRenderer() {
        }

        static Map<String, Object> render(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
            Map<String, Object> hud = dataSources.hudValues();
            Object health = stateValue(state, "hudHealth", hud.get("health"));
            Object hazard = stateValue(state, "hudHazard", hud.get("hazard"));
            Object mission = stateValue(state, "hudMission", hud.get("mission"));
            List<Map<String, Object>> statusMeters = maps(hud.get("statusMeters"));
            List<Map<String, Object>> notificationRows = maps(hud.get("notificationRows"));
            String output = string(state, "hudUpdateOutput", "");
            ArrayList<String> lines = new ArrayList<>();
            lines.add("HUD overlay is live. Health " + health);
            lines.add("Hazard: " + hazard);
            lines.add("Mission: " + mission);
            lines.add("Ashfall status: " + meterSummary(statusMeters));
            lines.add(String.valueOf(hud.get("missionLine")));
            lines.add(String.valueOf(hud.get("hazardLine")));
            lines.add(String.valueOf(hud.get("weatherLine")));
            lines.add("Notifications below panel: " + notificationRowSummary(notificationRows)
                    + " @ " + hud.get("notificationAnchor"));
            if (!output.isBlank()) {
                lines.add(output);
            }
            String cinematicOutput = string(state, "cinematicOutput", "");
            if (!cinematicOutput.isBlank()) {
                lines.add(cinematicOutput);
                lines.add("Cinematic: " + string(state, "cinematicCue", "")
                        + "    Camera: " + string(state, "cameraMode", "")
                        + " fov " + stateValue(state, "cameraFov", ""));
                if (bool(state, "cinematicLetterbox")) {
                    lines.add("Letterbox: active    Subtitle: " + string(state, "cinematicSubtitle", ""));
                }
            }
            Map<String, Object> model = new LinkedHashMap<>(moduleModel("echohudcore",
                    EchoAgent5HudSurfaceRenderer.class.getSimpleName(), EchoAgent5UiReference.HUD_LAYER, lines));
            model.put("statusMeters", statusMeters);
            model.put("missionLine", hud.get("missionLine"));
            model.put("hazardLine", hud.get("hazardLine"));
            model.put("weatherLine", hud.get("weatherLine"));
            model.put("notificationRows", notificationRows);
            model.put("notificationAnchor", hud.get("notificationAnchor"));
            return Map.copyOf(model);
        }
    }

    private static Map<String, Object> moduleModel(
            String moduleId,
            String rendererClass,
            String focusPath,
            List<String> lines
    ) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("moduleId", moduleId);
        model.put("moduleRendererClass", rendererClass);
        model.put("focusPath", focusPath);
        model.put("lines", List.copyOf(lines));
        model.put("adapterCoreBridge", true);
        model.put("serviceCodeExecuted", true);
        return Map.copyOf(model);
    }

    private static String focusLabel(String focusPath, Map<String, Object> state) {
        return focusPath.equals(string(state, "focusedControl", ""))
                && (bool(state, "mouseRouted") || bool(state, "initialFocusRouted"))
                ? focusPath + " ready"
                : focusPath + " waiting";
    }

    private static String typedOrPlaceholder(String value) {
        return value == null || value.isBlank() ? "_" : value;
    }

    private static String selectedOption(Map<String, Object> state, List<String> options) {
        String explicit = string(state, "selectedOption", "");
        if (!explicit.isBlank()) {
            return explicit;
        }
        if (options.isEmpty()) {
            return "";
        }
        int selectedIndex = integer(state == null ? null : state.get("selectedIndex"));
        if (selectedIndex < 0 || selectedIndex >= options.size()) {
            selectedIndex = 0;
        }
        return options.get(selectedIndex);
    }

    private static int integer(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static Object stateValue(Map<String, Object> state, String key, Object fallback) {
        if (state == null || !state.containsKey(key)) {
            return fallback;
        }
        return state.get(key);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(entry -> (Map<String, Object>) entry)
                    .map(Map::copyOf)
                    .toList();
        }
        return List.of();
    }

    private static String meterSummary(List<Map<String, Object>> meters) {
        return meters.stream()
                .map(meter -> meter.get("label") + " " + meter.get("value"))
                .reduce((left, right) -> left + " / " + right)
                .orElse("");
    }

    private static String notificationRowSummary(List<Map<String, Object>> rows) {
        return rows.stream()
                .map(row -> String.valueOf(row.get("title")))
                .filter(title -> !title.isBlank())
                .reduce((left, right) -> left + " / " + right)
                .orElse("");
    }

    private static String decimal(Object value) {
        if (value instanceof Number number) {
            return String.valueOf(number.doubleValue());
        }
        return value == null ? "0.0" : String.valueOf(value);
    }

    private static double doubleValue(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String string) {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private static String percent(double value) {
        return Math.round(value * 100.0D) + "%";
    }

    private static boolean booleanValue(Object value) {
        return Boolean.TRUE.equals(value);
    }

    private static String string(Map<String, Object> values, String key, String fallback) {
        if (values == null) {
            return fallback;
        }
        Object value = values.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private static boolean bool(Map<String, Object> values, String key) {
        return values != null && Boolean.TRUE.equals(values.get(key));
    }

    private static boolean bool(Object value) {
        return Boolean.TRUE.equals(value);
    }
}
