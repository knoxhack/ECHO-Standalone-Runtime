package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoCameraCoreStandaloneAdapter;
import dev.echo.standalone.runtime.compat.EchoCinematicCoreStandaloneAdapter;
import dev.echo.standalone.runtime.ui.EchoAgent5NotificationEndToEndAcceptanceSmoke;
import dev.echo.standalone.runtime.ui.EchoAgent5NotificationQueueSmoke;
import dev.echo.standalone.runtime.ui.EchoAgent5UiDataSources;
import dev.echo.standalone.runtime.ui.EchoAgent5UiHudHostCallQueueReplaySmoke;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public final class EchoRuntimeAgent5NativeActivationParitySmokeHarness {
    private EchoRuntimeAgent5NativeActivationParitySmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path marker = args.length > 0
                ? Path.of(args[0])
                : Path.of("tmp/native-bootstrap-smoke/module-activation.json");
        String markerText = Files.readString(marker, StandardCharsets.UTF_8);
        requireAdapterCoreDescriptorProbe(markerText);
        requireAdapterCoreRuntimeBridgeGuard(markerText);

        NativeActivation camera = requireNativeActivation(
                markerText,
                "echocameracore",
                "cameracore_native_contract_active",
                3,
                List.of("renderProfileRoundTrip", "shakeSafetyRoundTrip", "inputTargetRoundTrip")
        );
        NativeActivation cinematic = requireNativeActivation(
                markerText,
                "echocinematiccore",
                "cinematiccore_native_contract_active",
                3,
                List.of("sequenceRenderRoundTrip", "pacingRenderRoundTrip", "triggerUiRoundTrip")
        );
        NativeActivation input = requireNativeActivation(
                markerText,
                "echoinputcore",
                "inputcore_native_route_priority_active",
                4,
                List.of("routePriorityExecuted", "serviceCodeExecuted")
        );
        NativeActivation terminal = requireNativeActivation(
                markerText,
                "echoterminal",
                "terminal_native_dashboard_surface_active",
                5,
                List.of("dashboardSurfaceExecuted", "serviceCodeExecuted")
        );
        NativeActivation index = requireNativeActivation(
                markerText,
                "echoindex",
                "index_native_query_service_active",
                10,
                List.of("queryServiceExecuted", "inventoryOverlayReady", "serviceCodeExecuted")
        );
        NativeActivation lens = requireNativeActivation(
                markerText,
                "echolens",
                "lens_native_field_scan_active",
                8,
                List.of("fieldScanExecuted", "serviceCodeExecuted")
        );
        NativeActivation notification = requireNativeActivation(
                markerText,
                "echonotificationcore",
                "notificationcore_native_contract_active",
                4,
                List.of("featureContractRoundTrip", "serviceCodeExecuted")
        );
        NativeActivation hud = requireNativeActivation(
                markerText,
                "echohudcore",
                "hudcore_native_snapshot_active",
                5,
                List.of("hudSnapshotExecuted", "serviceCodeExecuted")
        );
        NativeActivation holoMap = requireNativeActivation(
                markerText,
                "echoholomap",
                "holomap_native_route_snapshot_active",
                10,
                List.of("routeSnapshotExecuted", "serviceCodeExecuted")
        );
        NativeActivation wiki = requireNativeActivation(
                markerText,
                "echowiki",
                "wiki_native_contract_active",
                9,
                List.of("guideSurfaceExecuted", "serviceCodeExecuted")
        );
        NativeActivation screenCore = requireNativeActivation(
                markerText,
                "echoscreencore",
                "screencore_native_composition_active",
                8,
                List.of("screenCompositionExecuted", "serviceCodeExecuted")
        );
        NativeActivation themeCore = requireNativeActivation(
                markerText,
                "echothemecore",
                "themecore_native_theme_application_active",
                8,
                List.of("themeApplicationExecuted", "serviceCodeExecuted")
        );
        NativeActivation renderCore = requireNativeActivation(
                markerText,
                "echorendercore",
                "rendercore_native_preview_frame_active",
                8,
                List.of("previewFrameExecuted", "serviceCodeExecuted")
        );
        requireNativeServiceHandleBridge(markerText, "echohudcore", 1);
        requireNativeServiceHandleBridge(markerText, "echoholomap", 2);
        requireNativeServiceHandleBridge(markerText, "echolens", 2);
        requireNativeServiceHandleBridge(markerText, "echoscreencore", 2);
        requireNativeServiceHandleBridge(markerText, "echowiki", 2);

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> cameraStandalone = new EchoCameraCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(cameraStandalone.get("activated")),
                "CameraCore standalone adapter should activate");
        require(Boolean.TRUE.equals(cameraStandalone.get("allRuntimeAliasesRegistered")),
                "CameraCore standalone adapter should register every AdapterCore runtime alias");
        require(camera.contractCount() == EchoCameraCoreStandaloneAdapter.CONTRACT_IDS.size(),
                "CameraCore native and standalone contract counts should match");
        requireMatches(cameraStandalone, "renderProfileRoundTrip", "shakeSafetyRoundTrip", "inputTargetRoundTrip");

        Map<String, Object> cinematicStandalone = new EchoCinematicCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(cinematicStandalone.get("activated")),
                "CinematicCore standalone adapter should activate");
        require(Boolean.TRUE.equals(cinematicStandalone.get("allRuntimeAliasesRegistered")),
                "CinematicCore standalone adapter should register every AdapterCore runtime alias");
        require(cinematic.contractCount() == EchoCinematicCoreStandaloneAdapter.CONTRACT_IDS.size(),
                "CinematicCore native and standalone contract counts should match");
        requireMatches(cinematicStandalone, "sequenceRenderRoundTrip", "pacingRenderRoundTrip", "triggerUiRoundTrip");

        EchoRuntimeEchoInputCoreParitySmokeHarness.main(new String[0]);
        require(input.contractCount() == 4,
                "InputCore native activation should expose the four standalone parity contracts");
        EchoRuntimeEchoTerminalParitySmokeHarness.main(new String[0]);
        require(terminal.contractCount() == 5,
                "Terminal native activation should register the dashboard surface contracts");
        EchoRuntimeEchoIndexParitySmokeHarness.main(new String[0]);
        require(index.contractCount() == 8,
                "Index native activation should register the query service contracts");
        EchoRuntimeEchoLensParitySmokeHarness.main(new String[0]);
        require(lens.contractCount() == 8,
                "Lens native activation should register the field scan contracts");

        EchoAgent5UiDataSources dataSources = EchoAgent5UiDataSources.reference();
        Map<String, Object> notificationQueue = EchoAgent5NotificationQueueSmoke.capture(
                "agent5-native-activation-parity",
                92,
                0,
                1,
                1,
                dataSources
        );
        Map<String, Object> notificationEndToEnd = EchoAgent5NotificationEndToEndAcceptanceSmoke.capture(
                "agent5-native-activation-parity",
                92,
                0,
                1,
                1,
                dataSources
        );
        require(Boolean.TRUE.equals(notificationQueue.get("passed")),
                "NotificationCore standalone queue smoke should pass");
        require(Boolean.TRUE.equals(notificationEndToEnd.get("passed")),
                "NotificationCore standalone end-to-end notification smoke should pass");
        require(notification.contractCount() == 4,
                "NotificationCore native activation should expose four notification contracts");
        EchoRuntimeEchoHudCoreParitySmokeHarness.main(new String[0]);
        require(hud.contractCount() == 5,
                "HudCore native activation should register the HUD snapshot contracts");
        Map<String, Object> uiHudQueueReplay = EchoAgent5UiHudHostCallQueueReplaySmoke.capture();
        Map<String, Object> acceptedUiHudQueueReplay = map(uiHudQueueReplay.get("accepted"));
        require(Boolean.TRUE.equals(uiHudQueueReplay.get("passed")),
                "Agent 5 UI/HUD host-call queue standalone replay smoke should pass");
        require(Boolean.TRUE.equals(acceptedUiHudQueueReplay.get("serviceCodeExecuted")),
                "Agent 5 UI/HUD host-call queue replay should execute service behavior");
        require("EchoNativeUiHudRuntimeTarget".equals(uiHudQueueReplay.get("nativeReference")),
                "Agent 5 UI/HUD host-call queue replay should name the native runtime target reference");
        require("ui_hud_host_call_queue_replay:accepted:8".equals(acceptedUiHudQueueReplay.get("effect")),
                "Agent 5 UI/HUD host-call queue replay should accept all eight commands");
        require(Integer.valueOf(8).equals(acceptedUiHudQueueReplay.get("runtimeHostConsumedCommandCount"))
                        && Integer.valueOf(8).equals(acceptedUiHudQueueReplay.get("runtimeHostMutationCount"))
                        && Integer.valueOf(8).equals(acceptedUiHudQueueReplay.get("missionUpdateCount"))
                        && Integer.valueOf(8).equals(acceptedUiHudQueueReplay.get("saveUpdateCount"))
                        && Integer.valueOf(8).equals(acceptedUiHudQueueReplay.get("feedbackCount"))
                        && Boolean.TRUE.equals(acceptedUiHudQueueReplay.get("queueConsumedByRuntimeHost"))
                        && Boolean.TRUE.equals(acceptedUiHudQueueReplay.get("runtimeHostMutated"))
                        && Boolean.TRUE.equals(acceptedUiHudQueueReplay.get("missionUpdated"))
                        && Boolean.TRUE.equals(acceptedUiHudQueueReplay.get("saveTouched"))
                        && Boolean.TRUE.equals(acceptedUiHudQueueReplay.get("feedbackEmitted")),
                "Agent 5 UI/HUD host-call queue replay should require consumed runtime-host mutation, mission, save, and feedback evidence");
        require(Boolean.FALSE.equals(map(uiHudQueueReplay.get("rejectedMissingHazard")).get("serviceCodeExecuted"))
                        && Boolean.FALSE.equals(map(uiHudQueueReplay.get("rejectedStandaloneCopy")).get("serviceCodeExecuted"))
                        && Boolean.FALSE.equals(map(uiHudQueueReplay.get("rejectedUnexecutedCommands")).get("serviceCodeExecuted"))
                        && Boolean.FALSE.equals(map(uiHudQueueReplay.get("rejectedQueueOnly")).get("serviceCodeExecuted")),
                "Agent 5 UI/HUD host-call queue rejected fixtures should not claim service execution");
        EchoRuntimeEchoHoloMapParitySmokeHarness.main(new String[0]);
        require(holoMap.contractCount() == 10,
                "HoloMap native activation should register the route snapshot contracts");
        EchoRuntimeEchoWikiParitySmokeHarness.main(new String[0]);
        require(wiki.contractCount() == 9,
                "Wiki native activation should register the guide surface contracts");
        EchoRuntimeEchoScreenCoreParitySmokeHarness.main(new String[0]);
        require(screenCore.contractCount() == 8,
                "ScreenCore native activation should register the screen composition contracts");
        EchoRuntimeEchoThemeCoreParitySmokeHarness.main(new String[0]);
        require(themeCore.contractCount() == 8,
                "ThemeCore native activation should register the theme application contracts");
        EchoRuntimeEchoRenderCoreParitySmokeHarness.main(new String[0]);
        require(renderCore.contractCount() == 8,
                "RenderCore native activation should register the preview frame contracts");

        List<String> stages = new ArrayList<>(List.of(
                camera.stage(),
                cinematic.stage(),
                input.stage(),
                terminal.stage(),
                index.stage(),
                lens.stage(),
                notification.stage(),
                hud.stage(),
                holoMap.stage(),
                wiki.stage(),
                screenCore.stage(),
                themeCore.stage(),
                renderCore.stage()
        ));
        System.out.println("agent5 native activation parity smoke PASS modules=13 nativeStages="
                + stages
                + " marker="
                + marker);
    }

    private static void requireAdapterCoreDescriptorProbe(String markerText) {
        String block = objectBlock(markerText, "adapterCore");
        require(block.contains("\"available\": true"),
                "AdapterCore descriptor probe should discover the native adapter descriptor");
        require(block.contains("\"classLoader\": \"native_module_classpath\"")
                        || block.contains("\"classLoader\": \"bootstrap_classpath\""),
                "AdapterCore descriptor probe should record the descriptor classloader");
        require(block.contains("\"id\": \"echo_native\""),
                "AdapterCore descriptor probe should expose echo_native id");
        require(block.contains("\"runtime\": \"echo_native\""),
                "AdapterCore descriptor probe should expose echo_native runtime");
        require(block.contains("\"nativeClasspath\": true"),
                "AdapterCore descriptor probe should expose nativeClasspath=true");
    }

    private static void requireAdapterCoreRuntimeBridgeGuard(String markerText) {
        require(markerText.contains("\"adapterCoreContentBridgeActive\": true"),
                "AdapterCore content bridge should be active after native registry/content activation");
        require(markerText.contains("\"adapterCoreRuntimeBridgeActive\": false"),
                "AdapterCore runtime bridge must remain false until live Minecraft-bound runtime handlers execute");
        require(markerText.contains("\"nativeClientUiHostAttached\": false"),
                "Headless marker must not claim native client UI host attachment");
        require(markerText.contains("\"nativeHeadlessUiHostAttached\": true"),
                "Headless marker should preserve executable headless UI host attachment evidence");
        require(markerText.contains("\"nativeUiFallbackHostAttached\": true"),
                "Headless marker should preserve fallback UI host attachment evidence");
        require(markerText.contains("\"nativeBootstrapLiveClientProbeExecuted\": false"),
                "Headless marker must not claim live client probe execution");
        require(markerText.contains("\"nativeBootstrapHudProbeSent\": false"),
                "Headless marker must not claim live HUD probe dispatch");
        require(markerText.contains("\"nativeBootstrapChatProbeSent\": false"),
                "Headless marker must not claim live chat probe dispatch");
        require(markerText.contains("\"nativeGameplayHandlerExecuted\": false"),
                "Headless marker must not claim native gameplay handler execution");
        require(markerText.contains("\"nativeLiveGameplayHandlersAttached\": false"),
                "Headless marker must not claim live gameplay handler attachment");
        String runtimeBridge = objectBlock(markerText, "runtimeBridge");
        String lifecycleBridge = objectBlock(runtimeBridge, "lifecycleBridge");
        require(lifecycleBridge.contains("\"applied\": true"),
                "AdapterCore lifecycle bridge should consume safe lifecycle descriptors");
        require(lifecycleBridge.contains("\"lifecycleCodeExecuted\": false"),
                "AdapterCore lifecycle bridge should not claim live lifecycle code execution in the headless marker");
        String eventBridge = objectBlock(runtimeBridge, "eventBridge");
        require(eventBridge.contains("\"applied\": true"),
                "AdapterCore event bridge should consume safe event descriptors");
        require(eventBridge.contains("\"handlerExecuted\": false"),
                "AdapterCore event bridge should not claim live handler execution in the headless marker");
        require(eventBridge.contains("\"gameplayHandlerExecuted\": false"),
                "AdapterCore event bridge should not claim live gameplay handler execution in the headless marker");
        String serviceBridge = objectBlock(runtimeBridge, "serviceBridge");
        require(serviceBridge.contains("\"applied\": true"),
                "AdapterCore service bridge should start approved native service handles");
        require(serviceBridge.contains("\"serviceCodeExecuted\": false"),
                "AdapterCore service bridge should not claim Minecraft-bound service code execution");
        require(serviceBridge.contains("\"minecraftRuntimeAccessed\": false"),
                "AdapterCore service bridge should not claim Minecraft runtime access in the headless marker");
        require(serviceBridge.contains("\"serviceExecutionMode\": \"adaptercore_native_service_handles\""),
                "AdapterCore service bridge should report native service handle mode");
        require(maxIntValue(serviceBridge, "runtimeInitializedServiceCount") >= 11,
                "AdapterCore service bridge should initialize the expected Agent 5 service handles");
        require(maxIntValue(serviceBridge, "startedServiceCount") >= 11,
                "AdapterCore service bridge should start the expected Agent 5 service handles");
        require(!serviceBridge.contains("\"serviceCodeExecuted\": true"),
                "AdapterCore service bridge should not report executed Minecraft-bound services in headless mode");
        String ashfallGameplayBridge = objectBlock(runtimeBridge, "ashfallGameplayBridge");
        require(ashfallGameplayBridge.contains("\"gameplayHandlerExecuted\": false"),
                "Ashfall gameplay bridge should not claim live gameplay handler execution in the headless marker");
        String currentGuard = objectBlock(markerText, "lastAdapterCoreRuntimeBridgeGuardAcceptance");
        require(currentGuard.contains("\"accepted\": false"),
                "Current AdapterCore runtime bridge guard should reject the headless marker");
        require(currentGuard.contains("\"adapterCoreRuntimeBridgeActive\": false"),
                "Current AdapterCore runtime bridge guard should preserve adapterCoreRuntimeBridgeActive=false");
        require(currentGuard.contains("\"rejection\": \"adaptercore_runtime_bridge_inactive\""),
                "Current AdapterCore runtime bridge guard should reject inactive runtime bridge evidence");
        String currentLiveHostEvidence = objectBlock(markerText, "lastLiveClientHostEvidenceAcceptance");
        require(currentLiveHostEvidence.contains("\"accepted\": false"),
                "Current live client host evidence should reject the headless marker");
        require(currentLiveHostEvidence.contains("\"serviceCodeExecuted\": false"),
                "Rejected live client host evidence should not claim service execution");
        require(currentLiveHostEvidence.contains("\"clientUiHostAttached\": false"),
                "Current live client host evidence should keep clientUiHostAttached=false");
        require(currentLiveHostEvidence.contains("\"headlessOnly\": true"),
                "Current live client host evidence should identify the marker as headless-only");
        String currentHeadlessReadiness = objectBlock(markerText, "lastHeadlessUiBridgeReadinessAcceptance");
        require(currentHeadlessReadiness.contains("\"accepted\": true"),
                "Current headless UI bridge readiness should accept the executable non-live host evidence");
        require(currentHeadlessReadiness.contains("\"clientUiHostAttached\": false"),
                "Current headless UI bridge readiness should not claim live client attachment");
        require(currentHeadlessReadiness.contains("\"minecraftRuntimeAccessed\": false"),
                "Current headless UI bridge readiness should not claim Minecraft runtime access");
        require(currentHeadlessReadiness.contains("\"liveHostRejectedHonesty\": true"),
                "Current headless UI bridge readiness should prove live-host rejection honesty");
        require(currentHeadlessReadiness.contains("\"serviceCodeExecuted\": true"),
                "Current headless UI bridge readiness should prove executable UI host service behavior");
        String headlessReadinessSmoke = objectBlock(markerText, "headlessUiBridgeReadinessAcceptanceSmoke");
        require(headlessReadinessSmoke.contains("\"passed\": true"),
                "Headless UI bridge readiness smoke should pass the native acceptance contract");
        require(headlessReadinessSmoke.contains("\"serviceCodeExecuted\": true"),
                "Headless UI bridge readiness smoke should execute service behavior");
        requireRejectedServiceCodeFalse(markerText, "headlessUiBridgeReadinessAcceptanceSmoke",
                "rejectedLiveAttached", "rejectedNoTerminal", "rejectedNoHotkeys",
                "rejectedScreenMismatch", "rejectedLiveHostOverclaim");
        String liveHostEvidenceSmoke = objectBlock(markerText, "liveClientHostEvidenceAcceptanceSmoke");
        require(liveHostEvidenceSmoke.contains("\"passed\": true"),
                "Live client host evidence smoke should pass the native acceptance contract");
        require(liveHostEvidenceSmoke.contains("\"serviceCodeExecuted\": true"),
                "Accepted live client host evidence fixture should still execute service behavior");
        String rejectedHeadlessOnly = objectBlock(liveHostEvidenceSmoke, "rejectedHeadlessOnly");
        require(rejectedHeadlessOnly.contains("\"accepted\": false")
                        && rejectedHeadlessOnly.contains("\"serviceCodeExecuted\": false"),
                "Live client host evidence smoke should prove rejected fixtures do not claim service execution");
        requireRejectedServiceCodeFalse(markerText, "liveSurfaceAcceptanceSmoke", "rejectedMode", "rejectedSetScreen");
        requireRejectedServiceCodeFalse(markerText, "physicalInputAcceptanceSmoke", "rejectedSurfaceMismatch", "rejectedNoHotkey");
        requireRejectedServiceCodeFalse(markerText, "liveSurfaceRenderAcceptanceSmoke",
                "rejectedUnacceptedSurface", "rejectedRenderedSurfaceMismatch");
        requireAllRouteSurfaces(markerText, "liveSurfaceAcceptanceSmoke");
        requireAllRouteSurfaces(markerText, "physicalInputAcceptanceSmoke");
        requireAllRouteSurfaces(markerText, "liveSurfaceRenderAcceptanceSmoke");
        requireSurfaceRenderers(markerText);
        requireRejectedServiceCodeFalse(markerText, "uiHostEndToEndAcceptanceSmoke",
                "rejectedNoInput", "rejectedRender", "rejectedInteraction");
    }

    private static void requireNativeServiceHandleBridge(String markerText, String moduleId, int expectedCount) {
        String activation = moduleBlock(markerText, moduleId);
        String serviceBridge = objectBlock(activation, "serviceBridge");
        require(serviceBridge.contains("\"applied\": true"),
                moduleId + " service bridge should be applied");
        require(serviceBridge.contains("\"moduleId\": \"" + moduleId + "\""),
                moduleId + " service bridge should preserve its module id");
        require(serviceBridge.contains("\"serviceExecutionMode\": \"adaptercore_native_service_handles\""),
                moduleId + " service bridge should use native service handle mode");
        require(intValue(serviceBridge, "approvedServiceCount") == expectedCount,
                moduleId + " service bridge should approve " + expectedCount + " services");
        require(intValue(serviceBridge, "runtimeInitializedServiceCount") == expectedCount,
                moduleId + " service bridge should initialize " + expectedCount + " service handles");
        require(intValue(serviceBridge, "startedServiceCount") == expectedCount,
                moduleId + " service bridge should start " + expectedCount + " service handles");
        require(intValue(serviceBridge, "executedServiceCount") == 0,
                moduleId + " service bridge should not execute Minecraft-bound service code in headless mode");
        require(serviceBridge.contains("\"serviceCodeExecuted\": false"),
                moduleId + " service bridge should not claim service code execution");
        require(serviceBridge.contains("\"minecraftRuntimeAccessed\": false"),
                moduleId + " service bridge should not claim Minecraft runtime access");
    }

    private static void requireAllRouteSurfaces(String markerText, String smokeKey) {
        String smoke = objectBlock(markerText, smokeKey);
        String routeSurfaces = arrayBlock(smoke, "routeSurfaces");
        String acceptedRoutes = arrayBlock(smoke, "acceptedRoutes");
        for (String surface : expectedSurfaces()) {
            require(routeSurfaces.contains("\"" + surface + "\""),
                    smokeKey + " should include route surface " + surface);
            require(acceptedRoutes.contains("\"" + surface + "\""),
                    smokeKey + " should include an accepted route for " + surface);
        }
    }

    private static void requireSurfaceRenderers(String markerText) {
        String smoke = objectBlock(markerText, "liveSurfaceRenderAcceptanceSmoke");
        requireRenderer(smoke, "TERMINAL", "EchoNativeTerminalSurfaceRenderer");
        requireRenderer(smoke, "INDEX", "EchoNativeIndexSurfaceRenderer");
        requireRenderer(smoke, "LENS", "EchoNativeLensSurfaceRenderer");
        requireRenderer(smoke, "MISSION_LOG", "EchoNativeMissionLogSurfaceRenderer");
        requireRenderer(smoke, "SETTINGS", "EchoNativeSettingsSurfaceRenderer");
        requireRenderer(smoke, "PAUSE", "EchoNativePauseSurfaceRenderer");
        requireRenderer(smoke, "RECOVERY", "EchoNativeRecoverySurfaceRenderer");
        requireRenderer(smoke, "HOLOMAP", "EchoNativeHolomapSurfaceRenderer");
        requireRenderer(smoke, "WIKI", "EchoNativeWikiSurfaceRenderer");
        requireRenderer(smoke, "MAIN_MENU", "EchoNativeMainMenuSurfaceRenderer");
        requireRenderer(smoke, "HUD", "EchoNativeHudSurfaceRenderer");
    }

    private static void requireRenderer(String smoke, String surface, String rendererClass) {
        require(smoke.contains("\"surface\": \"" + surface + "\"")
                        && smoke.contains("\"moduleRendererClass\": \"" + rendererClass + "\"")
                        && smoke.contains("\"screenTitle\": \"ECHO NATIVE // " + surface + "\""),
                "Render acceptance smoke should prove " + rendererClass + " rendered " + surface);
    }

    private static List<String> expectedSurfaces() {
        return List.of(
                "TERMINAL",
                "INDEX",
                "LENS",
                "MISSION_LOG",
                "SETTINGS",
                "PAUSE",
                "RECOVERY",
                "HOLOMAP",
                "WIKI",
                "MAIN_MENU",
                "HUD"
        );
    }

    private static void requireRejectedServiceCodeFalse(String markerText, String smokeKey, String... rejectionKeys) {
        String smoke = objectBlock(markerText, smokeKey);
        require(smoke.contains("\"passed\": true"),
                smokeKey + " should pass its acceptance smoke");
        String accepted = objectBlock(smoke, "accepted");
        require(accepted.contains("\"accepted\": true") && accepted.contains("\"serviceCodeExecuted\": true"),
                smokeKey + " accepted fixture should report serviceCodeExecuted=true");
        for (String rejectionKey : rejectionKeys) {
            String rejected = objectBlock(smoke, rejectionKey);
            require(rejected.contains("\"accepted\": false")
                            && rejected.contains("\"serviceCodeExecuted\": false"),
                    smokeKey + " " + rejectionKey + " should reject without claiming service execution");
        }
    }

    private static String objectBlock(String markerText, String key) {
        int keyIndex = markerText.indexOf("\"" + key + "\"");
        if (keyIndex < 0) {
            throw new AssertionError("Missing object key " + key);
        }
        int start = markerText.indexOf('{', keyIndex);
        if (start < 0) {
            throw new AssertionError("Missing object start for " + key);
        }
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = start; index < markerText.length(); index++) {
            char current = markerText.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                continue;
            }
            if (current == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return markerText.substring(start, index + 1);
                }
            }
        }
        throw new AssertionError("Unclosed object for " + key);
    }

    private static NativeActivation requireNativeActivation(
            String markerText,
            String moduleId,
            String expectedStage,
            int expectedContractCount,
            List<String> trueKeys
    ) {
        String block = moduleBlock(markerText, moduleId);
        require(block.contains("\"activated\": true"),
                moduleId + " should be activated in the native marker");
        require(block.contains("\"nativeAdapterCodeExecuted\": true"),
                moduleId + " should execute native adapter code");
        require(block.contains("\"serviceCodeExecuted\": true"),
                moduleId + " should execute native service behavior");
        require(block.contains("\"activationStage\": \"" + expectedStage + "\""),
                moduleId + " should report activation stage " + expectedStage);
        for (String key : trueKeys) {
            require(block.contains("\"" + key + "\": true"),
                    moduleId + " should report " + key + "=true");
        }
        int contractCount = intValue(block, "logicalRegistrationCount");
        require(contractCount == expectedContractCount,
                moduleId + " should register " + expectedContractCount + " contracts");
        return new NativeActivation(moduleId, expectedStage, contractCount);
    }

    private static String moduleBlock(String markerText, String moduleId) {
        for (String block : topLevelObjects(arrayBlock(markerText, "nativeActivations"))) {
            if (block.contains("\"moduleId\": \"" + moduleId + "\"")
                    && block.contains("\"entrypoint\"")
                    && block.contains("\"activationStage\"")) {
                return block;
            }
        }
        throw new AssertionError("Missing native activation block for " + moduleId);
    }

    private static String arrayBlock(String markerText, String key) {
        int keyIndex = markerText.indexOf("\"" + key + "\"");
        if (keyIndex < 0) {
            throw new AssertionError("Missing array key " + key);
        }
        int start = markerText.indexOf('[', keyIndex);
        if (start < 0) {
            throw new AssertionError("Missing array start for " + key);
        }
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = start; index < markerText.length(); index++) {
            char current = markerText.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                continue;
            }
            if (current == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (current == '[') {
                depth++;
            } else if (current == ']') {
                depth--;
                if (depth == 0) {
                    return markerText.substring(start + 1, index);
                }
            }
        }
        throw new AssertionError("Unclosed array for " + key);
    }

    private static List<String> topLevelObjects(String arrayText) {
        java.util.ArrayList<String> objects = new java.util.ArrayList<>();
        int start = -1;
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = 0; index < arrayText.length(); index++) {
            char current = arrayText.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                continue;
            }
            if (current == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (current == '{') {
                if (depth == 0) {
                    start = index;
                }
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objects.add(arrayText.substring(start, index + 1));
                    start = -1;
                }
            }
        }
        return List.copyOf(objects);
    }

    private static int intValue(String block, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)").matcher(block);
        if (!matcher.find()) {
            throw new AssertionError("Missing integer key " + key);
        }
        return Integer.parseInt(matcher.group(1));
    }

    private static int maxIntValue(String block, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)").matcher(block);
        int max = Integer.MIN_VALUE;
        while (matcher.find()) {
            max = Math.max(max, Integer.parseInt(matcher.group(1)));
        }
        if (max == Integer.MIN_VALUE) {
            throw new AssertionError("Missing integer key " + key);
        }
        return max;
    }

    private static void requireMatches(Map<String, Object> report, String... keys) {
        for (String key : keys) {
            require(Boolean.TRUE.equals(report.get(key)),
                    "Standalone report should preserve " + key);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record NativeActivation(String moduleId, String stage, int contractCount) {
    }
}
