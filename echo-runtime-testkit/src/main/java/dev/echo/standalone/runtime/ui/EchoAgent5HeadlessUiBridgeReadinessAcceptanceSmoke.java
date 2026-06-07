package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5HeadlessUiBridgeReadinessAcceptanceSmoke {
    private static final String SCREEN_CLASS = "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost";

    private EchoAgent5HeadlessUiBridgeReadinessAcceptanceSmoke() {
    }

    public static Map<String, Object> capture() {
        Map<String, Object> accepted = EchoAgent5HeadlessUiBridgeReadinessAcceptance.assess(
                fixture(false, true, true, SCREEN_CLASS, true, true),
                SCREEN_CLASS
        );
        Map<String, Object> rejectedLiveAttached = EchoAgent5HeadlessUiBridgeReadinessAcceptance.assess(
                fixture(true, true, true, SCREEN_CLASS, true, true),
                SCREEN_CLASS
        );
        Map<String, Object> rejectedNoTerminal = EchoAgent5HeadlessUiBridgeReadinessAcceptance.assess(
                fixture(false, false, true, SCREEN_CLASS, true, true),
                SCREEN_CLASS
        );
        Map<String, Object> rejectedNoHotkeys = EchoAgent5HeadlessUiBridgeReadinessAcceptance.assess(
                fixture(false, true, false, SCREEN_CLASS, true, true),
                SCREEN_CLASS
        );
        Map<String, Object> rejectedScreenMismatch = EchoAgent5HeadlessUiBridgeReadinessAcceptance.assess(
                fixture(false, true, true, "dev.echo.standalone.runtime.ui.OtherScreen", true, true),
                SCREEN_CLASS
        );
        Map<String, Object> rejectedLiveHostOverclaim = EchoAgent5HeadlessUiBridgeReadinessAcceptance.assess(
                fixture(false, true, true, SCREEN_CLASS, false, true),
                SCREEN_CLASS
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "headless_ui_bridge_readiness:accepted:EchoAgent5UiScreenHost".equals(accepted.get("effect"))
                && Boolean.TRUE.equals(accepted.get("serviceCodeExecuted"))
                && Boolean.FALSE.equals(rejectedLiveAttached.get("accepted"))
                && Boolean.FALSE.equals(rejectedLiveAttached.get("serviceCodeExecuted"))
                && Boolean.FALSE.equals(rejectedNoTerminal.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoTerminal.get("serviceCodeExecuted"))
                && Boolean.FALSE.equals(rejectedNoHotkeys.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoHotkeys.get("serviceCodeExecuted"))
                && Boolean.FALSE.equals(rejectedScreenMismatch.get("accepted"))
                && Boolean.FALSE.equals(rejectedScreenMismatch.get("serviceCodeExecuted"))
                && Boolean.FALSE.equals(rejectedLiveHostOverclaim.get("accepted"))
                && Boolean.FALSE.equals(rejectedLiveHostOverclaim.get("serviceCodeExecuted"));
        return Map.of(
                "headlessUiBridgeReadinessAcceptanceSmokeClass",
                EchoAgent5HeadlessUiBridgeReadinessAcceptanceSmoke.class.getSimpleName(),
                "accepted", accepted,
                "rejectedLiveAttached", rejectedLiveAttached,
                "rejectedNoTerminal", rejectedNoTerminal,
                "rejectedNoHotkeys", rejectedNoHotkeys,
                "rejectedScreenMismatch", rejectedScreenMismatch,
                "rejectedLiveHostOverclaim", rejectedLiveHostOverclaim,
                "adapterCoreBridge", true,
                "serviceCodeExecuted", true,
                "passed", passed
        );
    }

    private static Map<String, Object> fixture(
            boolean clientUiHostAttached,
            boolean terminalEvidence,
            boolean hotkeys,
            String screenClass,
            boolean honestLiveHostRejection,
            boolean smokeEvidence
    ) {
        Map<String, Object> bridge = new LinkedHashMap<>();
        bridge.put("installed", true);
        bridge.put("fallbackHostAttached", true);
        bridge.put("headlessUiHostAttached", true);
        bridge.put("clientUiHostAttached", clientUiHostAttached);
        bridge.put("clientThreadAccepted", clientUiHostAttached);
        bridge.put("screenClass", screenClass);
        bridge.put("hotkeys", hotkeys ? List.of(
                "M:Terminal",
                "G:Index catalog",
                "R:Index recipe",
                "U:Index usage",
                "B:Index bookmark",
                "LEFT_ALT:Lens",
                "J:HoloMap",
                "K:HoloMap minimap",
                "RIGHT_BRACKET:HoloMap zoom in",
                "LEFT_BRACKET:HoloMap zoom out",
                "BACKSLASH:HoloMap corner",
                "N:SignalOS",
                "X:Drone recall",
                "C:Drone scan",
                "Y:Drone scout",
                "Z:Drone status",
                "ESCAPE:Pause/Resume"
        ) : List.of("M:Terminal"));
        bridge.put("screenIds", List.of(
                "echo:main_menu",
                "echoterminal:terminal",
                "echoindex:index",
                "echolens:lens",
                "echohudcore:hud",
                "echonotificationcore:queue",
                "echoscreencore:mission_log",
                "echoscreencore:settings",
                "echoscreencore:pause_flow",
                "echoscreencore:death_recovery",
                "echoholomap:holomap",
                "echowiki:wiki"
        ));
        bridge.put("agent5DataSources", Map.of("terminal", Map.of("command", "status")));
        for (String key : List.of(
                "terminalFallbackReady",
                "indexFallbackReady",
                "lensFallbackReady",
                "hudFallbackReady",
                "notificationQueueReady",
                "missionLogFallbackReady",
                "settingsFallbackReady",
                "pauseFlowFallbackReady",
                "deathRecoveryFallbackReady",
                "holomapFallbackReady",
                "wikiFallbackReady",
                "customMainMenuReady",
                "screenFocusRoutingReady",
                "textInputRoutingReady",
                "mouseRoutingReady",
                "notificationQueueDispatched",
                "missionLogTracksActiveMission",
                "settingsProfileApplied",
                "pauseFlowResumesPreviousScreen",
                "deathRecoveryActionExecuted",
                "noScreenCrash"
        )) {
            bridge.put(key, terminalEvidence || !key.equals("terminalFallbackReady"));
        }
        for (String key : List.of(
                "lastTerminalEndToEndAcceptance",
                "lastIndexEndToEndAcceptance",
                "lastLensEndToEndAcceptance",
                "lastHoloMapEndToEndAcceptance",
                "lastWikiEndToEndAcceptance",
                "lastMissionLogEndToEndAcceptance",
                "lastSettingsEndToEndAcceptance",
                "lastPauseEndToEndAcceptance",
                "lastRecoveryEndToEndAcceptance",
                "lastNotificationEndToEndAcceptance",
                "lastMainMenuEndToEndAcceptance",
                "lastLiveCoreToolsAcceptance",
                "lastLiveMissionObjectiveAcceptance",
                "lastLiveSystemFlowAcceptance",
                "lastLiveHoloMapWikiNavigationAcceptance",
                "lastLiveNotificationQueueAcceptance",
                "lastUiHostInteractionStateAcceptance"
        )) {
            bridge.put(key, Map.of("accepted", terminalEvidence));
        }
        for (String key : List.of(
                "hotkeyBridgeSmoke",
                "hostEventTranscriptSmoke",
                "physicalHotkeyPollingSmoke",
                "screenLifecycleSmoke",
                "liveSurfaceAcceptanceSmoke",
                "physicalInputAcceptanceSmoke",
                "liveSurfaceRenderAcceptanceSmoke",
                "uiHostEndToEndAcceptanceSmoke"
        )) {
            bridge.put(key, Map.of("passed", smokeEvidence));
        }
        bridge.put("lastLiveClientHostEvidenceAcceptance", Map.of(
                "accepted", !honestLiveHostRejection,
                "serviceCodeExecuted", !honestLiveHostRejection,
                "headlessOnly", honestLiveHostRejection
        ));
        return bridge;
    }
}
