package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5UiDataSources {
    private static final String NOTIFICATION_ANCHOR = "top_left_safe_area";

    private EchoAgent5UiDataSources() {
    }

    public static EchoAgent5UiDataSources reference() {
        return new EchoAgent5UiDataSources();
    }

    public String terminalPrompt() {
        return "ASH>";
    }

    public String terminalCommand() {
        return EchoAgent5UiReference.TERMINAL_COMMAND;
    }

    public String terminalReadyLine() {
        return "First-Month Route Desk / Tracks early Ashfall routes from safe caches into faction signals, "
                + "hazard warnings, and relay work without sending the player into late-game danger. / Next: "
                + "Confirm the Survivor Cache Signal before leaving shelter.";
    }

    public String indexQuery() {
        return EchoAgent5UiReference.INDEX_QUERY;
    }

    public String indexResult() {
        return "Faction Field Contacts / Observe first, commit later / Crashbreak and Radwarden field signals are "
                + "early contact hooks. They should teach recognition and caution before deeper faction work.";
    }

    public String lensTarget() {
        return EchoAgent5UiReference.LENS_TARGET;
    }

    public String lensResult() {
        return "safe / Terminal wake signal detected. Index entry unlocked: Showcase Flow.";
    }

    public List<String> mainMenuOptions() {
        return List.of("Continue", "New Ashfall Run", "Settings", "Quit");
    }

    public Map<String, Object> hudValues() {
        Map<String, Object> hud = new LinkedHashMap<>();
        hud.put("health", 100);
        hud.put("hazard", "Mission signal: " + EchoAgent5UiReference.ACTIVE_MISSION_STATUS);
        hud.put("mission", EchoAgent5UiReference.ACTIVE_MISSION_OBJECTIVE);
        hud.put("notifications", notifications());
        return Map.copyOf(hud);
    }

    public List<Map<String, Object>> notifications() {
        return List.of(
                notification("echoterminal:data/echoashfallprotocol/echoterminal/pages/ashfall_first_month_routes.json",
                        "INFO", "First-Month Route Desk"),
                notification("missioncore:" + EchoAgent5UiReference.ACTIVE_MISSION_ID,
                        "INFO", EchoAgent5UiReference.ACTIVE_MISSION_TITLE)
        );
    }

    public Map<String, Object> missionLogValues() {
        Map<String, Object> mission = new LinkedHashMap<>();
        mission.put("missionId", EchoAgent5UiReference.ACTIVE_MISSION_ID);
        mission.put("title", EchoAgent5UiReference.ACTIVE_MISSION_TITLE);
        mission.put("objective", EchoAgent5UiReference.ACTIVE_MISSION_OBJECTIVE);
        mission.put("status", EchoAgent5UiReference.ACTIVE_MISSION_STATUS);
        mission.put("progress", 0.25D);
        return Map.copyOf(mission);
    }

    public Map<String, Object> settingsValues() {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("profile", EchoAgent5UiReference.SETTINGS_PROFILE);
        settings.put("theme", EchoAgent5UiReference.SETTINGS_THEME);
        settings.put("inputMode", EchoAgent5UiReference.SETTINGS_INPUT_MODE);
        settings.put("hudScale", 1.0D);
        settings.put("subtitles", true);
        return Map.copyOf(settings);
    }

    public List<String> pauseOptions() {
        return List.of("Resume", "Settings", "Save Snapshot", "Quit to Main Menu");
    }

    public Map<String, Object> pauseFlowValues(String previousScreenId) {
        Map<String, Object> pause = new LinkedHashMap<>();
        pause.put("screenId", EchoAgent5UiReference.PAUSE_FLOW_SCREEN);
        pause.put("previousScreenId", previousScreenId);
        pause.put("resumeTarget", EchoAgent5UiReference.PAUSE_RESUME_TARGET);
        pause.put("options", pauseOptions());
        return Map.copyOf(pause);
    }

    public Map<String, Object> deathRecoveryValues(String status) {
        Map<String, Object> recovery = new LinkedHashMap<>();
        recovery.put("screenId", EchoAgent5UiReference.DEATH_RECOVERY_SCREEN);
        recovery.put("recoveryPoint", EchoAgent5UiReference.RECOVERY_POINT);
        recovery.put("action", EchoAgent5UiReference.RECOVERY_ACTION);
        recovery.put("status", status);
        recovery.put("restoredHealth", 35);
        return Map.copyOf(recovery);
    }

    public List<String> holomapLines() {
        Map<String, Object> holomap = holomapValues();
        return List.of(
                "Layer: " + holomap.get("layer"),
                "Marker: " + holomap.get("marker"),
                holomapOutput()
        );
    }

    public Map<String, Object> holomapValues() {
        Map<String, Object> holomap = new LinkedHashMap<>();
        holomap.put("screenId", EchoAgent5UiReference.HOLOMAP_SCREEN);
        holomap.put("layer", "echoashfallprotocol:first_month_field_intel");
        holomap.put("layerName", "First-Month Field Intel");
        holomap.put("marker", "echoashfallprotocol:survivor_cache_signal");
        holomap.put("markerLabel", "Survivor Cache Signal");
        holomap.put("focus", "A low-risk scanner lead for the first useful field cache.");
        return Map.copyOf(holomap);
    }

    public String holomapOutput() {
        Map<String, Object> holomap = holomapValues();
        return "Layer " + holomap.get("layer")
                + " marker " + holomap.getOrDefault("markerLabel", holomap.get("marker"))
                + " [" + holomap.get("marker") + "]"
                + " focus " + holomap.get("focus");
    }

    public List<String> wikiLines() {
        Map<String, Object> wiki = wikiValues();
        return List.of(
                "Guide: " + wiki.get("guide"),
                "Page: " + wiki.get("page"),
                wikiOutput()
        );
    }

    public Map<String, Object> wikiValues() {
        Map<String, Object> wiki = new LinkedHashMap<>();
        wiki.put("screenId", EchoAgent5UiReference.WIKI_SCREEN);
        wiki.put("guide", "Guide Books");
        wiki.put("page", "Ashfall Field Manual");
        wiki.put("link", "wiki:echowiki/guides/ashfall");
        return Map.copyOf(wiki);
    }

    public String wikiOutput() {
        Map<String, Object> wiki = wikiValues();
        return "Guide " + wiki.get("guide")
                + " page " + wiki.get("page")
                + " link " + wiki.get("link");
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("terminal", Map.of(
                "command", terminalCommand(),
                "prompt", terminalPrompt(),
                "readyLine", terminalReadyLine()
        ));
        values.put("index", Map.of(
                "query", indexQuery(),
                "result", indexResult()
        ));
        values.put("lens", Map.of(
                "target", lensTarget(),
                "result", lensResult()
        ));
        values.put("hud", hudValues());
        values.put("notifications", notifications());
        values.put("missionLog", missionLogValues());
        values.put("settings", settingsValues());
        values.put("pauseFlow", pauseFlowValues(EchoAgent5UiReference.PAUSE_RESUME_TARGET));
        values.put("deathRecovery", deathRecoveryValues(EchoAgent5UiReference.RECOVERY_STATUS));
        values.put("holomap", holomapValues());
        values.put("wiki", wikiValues());
        values.put("signalos", signalOsValues());
        values.put("ashfallDrone", ashfallDroneValues());
        values.put("camera", cameraValues());
        values.put("cinematic", cinematicValues());
        values.put("mainMenu", Map.of("options", mainMenuOptions()));
        return Map.copyOf(values);
    }

    public Map<String, Object> cameraValues() {
        Map<String, Object> camera = new LinkedHashMap<>();
        camera.put("moduleId", "echocameracore");
        camera.put("mode", "over_shoulder");
        camera.put("fov", 72);
        camera.put("target", holomapValues().get("marker"));
        camera.put("adapterCoreBridge", "echocameracore:camera_frame");
        return Map.copyOf(camera);
    }

    public Map<String, Object> cinematicValues() {
        Map<String, Object> cinematic = new LinkedHashMap<>();
        cinematic.put("moduleId", "echocinematiccore");
        cinematic.put("cue", "First-Month Route Desk");
        cinematic.put("letterbox", true);
        cinematic.put("subtitle", "Tracks early Ashfall routes from safe caches into faction signals, hazard warnings, and relay work without sending the player into late-game danger.");
        cinematic.put("adapterCoreBridge", "echocinematiccore:cinematic_cue");
        return Map.copyOf(cinematic);
    }

    public Map<String, Object> signalOsValues() {
        Map<String, Object> signalos = new LinkedHashMap<>();
        signalos.put("screenId", EchoAgent5UiReference.SIGNALOS_SCREEN);
        signalos.put("title", "SignalOS Terminal");
        signalos.put("summary", "SignalOS content loaded");
        signalos.put("transport", "echonetcore:serverbound_action");
        signalos.put("packet", "SignalOsOpenTerminalPacket");
        signalos.put("status", "READY");
        return Map.copyOf(signalos);
    }

    public Map<String, Object> ashfallDroneValues() {
        Map<String, Object> drone = new LinkedHashMap<>();
        drone.put("screenId", EchoAgent5UiReference.ASHFALL_DRONE_SCREEN);
        drone.put("title", "Restore Drone Recon Support");
        drone.put("summary", "Drone support loaded");
        drone.put("intelRoute", "echoashfallprotocol:recover_drone_intel");
        drone.put("transport", "echonetcore:serverbound_action");
        drone.put("packet", "DroneCommandPacket");
        drone.put("commands", List.of("recall", "scan", "scout", "status", "toggle_assist"));
        drone.put("keys", List.of("X", "C", "Y", "Z", "B"));
        drone.put("adapterCoreBridge", "echoashfallprotocol:drone");
        return Map.copyOf(drone);
    }

    private static Map<String, Object> notification(String id, String severity, String message) {
        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("id", id);
        notification.put("severity", severity);
        notification.put("message", message);
        notification.put("anchor", NOTIFICATION_ANCHOR);
        notification.put("delivered", true);
        return Map.copyOf(notification);
    }
}
