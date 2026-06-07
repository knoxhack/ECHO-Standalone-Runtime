package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5UiReferenceAudit {
    private EchoAgent5UiReferenceAudit() {
    }

    public static List<Map<String, Object>> records() {
        return List.of(
                record("custom_main_menu", EchoAgent5UiReference.MAIN_MENU_SCREEN, "MAIN_MENU", "P", "mainMenu",
                        "main_menu_end_to_end_acceptance_smoke_executes"),
                record("terminal", EchoAgent5UiReference.TERMINAL_SCREEN, "TERMINAL", "M", "terminal",
                        "terminal_end_to_end_acceptance_smoke_executes"),
                record("index", EchoAgent5UiReference.INDEX_SCREEN, "INDEX", "I", "index",
                        "index_end_to_end_acceptance_smoke_executes"),
                record("lens_scanner", EchoAgent5UiReference.LENS_SCREEN, "LENS", "L", "lens",
                        "lens_end_to_end_acceptance_smoke_executes"),
                record("hud", EchoAgent5UiReference.HUD_LAYER, "HUD", "H", "hud",
                        "hud_overlay_end_to_end_acceptance_smoke_executes"),
                record("mission_log", EchoAgent5UiReference.MISSION_LOG_SCREEN, "MISSION_LOG", "J", "missionLog",
                        "mission_log_end_to_end_acceptance_smoke_executes"),
                record("notifications", EchoAgent5UiReference.NOTIFICATION_QUEUE, "HUD", "H", "notifications",
                        "notification_end_to_end_acceptance_smoke_executes"),
                record("holomap", EchoAgent5UiReference.HOLOMAP_SCREEN, "HOLOMAP", "O", "holomap",
                        "holomap_end_to_end_acceptance_smoke_executes"),
                record("wiki", EchoAgent5UiReference.WIKI_SCREEN, "WIKI", "W", "wiki",
                        "wiki_end_to_end_acceptance_smoke_executes"),
                record("settings", EchoAgent5UiReference.SETTINGS_SCREEN, "SETTINGS", "K", "settings",
                        "settings_end_to_end_acceptance_smoke_executes"),
                record("pause_flow", EchoAgent5UiReference.PAUSE_FLOW_SCREEN, "PAUSE", "ESCAPE", "pauseFlow",
                        "pause_end_to_end_acceptance_smoke_executes"),
                record("death_recovery_screen", EchoAgent5UiReference.DEATH_RECOVERY_SCREEN, "RECOVERY", "R", "deathRecovery",
                        "recovery_end_to_end_acceptance_smoke_executes")
        );
    }

    private static Map<String, Object> record(
            String behavior,
            String screenId,
            String surface,
            String routeKey,
            String dataSource,
            String acceptanceFeature
    ) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("behavior", behavior);
        record.put("screenId", screenId);
        record.put("surface", surface);
        record.put("routeKey", routeKey);
        record.put("dataSource", dataSource);
        record.put("acceptanceFeature", acceptanceFeature);
        record.put("adapterCoreBridge", true);
        return Map.copyOf(record);
    }
}
