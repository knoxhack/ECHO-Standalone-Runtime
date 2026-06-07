package dev.echo.standalone.runtime.ui;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoAgent5UiParityRuntime {
    public EchoAgent5UiParityResult run(EchoRuntimeServiceRegistry services) {
        Objects.requireNonNull(services, "services");

        ArrayList<String> diagnostics = new ArrayList<>();
        ArrayList<String> visited = new ArrayList<>();
        EchoAgent5UiDataSources dataSources = EchoAgent5UiDataSources.reference();
        Map<String, Object> uiReferenceAuditSmoke = EchoAgent5UiReferenceAuditSmoke.capture(dataSources);
        boolean uiReferenceAuditSmokeSatisfied = Boolean.TRUE.equals(uiReferenceAuditSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(uiReferenceAuditSmoke.get("passed"))
                && "EchoAgent5UiReferenceAuditSmoke".equals(uiReferenceAuditSmoke.get("uiReferenceAuditSmokeClass"))
                && Integer.valueOf(12).equals(uiReferenceAuditSmoke.get("behaviorCount"))
                && strings(uiReferenceAuditSmoke, "missingScreens").isEmpty()
                && strings(uiReferenceAuditSmoke, "missingDataSources").isEmpty()
                && strings(uiReferenceAuditSmoke, "missingAcceptanceFeatures").isEmpty();
        Map<String, Object> uiRuntimeEquivalenceAuditSmoke =
                EchoAgent5UiRuntimeEquivalenceAuditSmoke.capture(dataSources);
        boolean uiRuntimeEquivalenceAuditSmokeSatisfied =
                Boolean.TRUE.equals(uiRuntimeEquivalenceAuditSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(uiRuntimeEquivalenceAuditSmoke.get("passed"))
                        && "EchoAgent5UiRuntimeEquivalenceAuditSmoke".equals(
                        uiRuntimeEquivalenceAuditSmoke.get("uiRuntimeEquivalenceAuditSmokeClass"))
                        && Boolean.TRUE.equals(uiRuntimeEquivalenceAuditSmoke.get("screenIdsMatch"))
                        && Boolean.TRUE.equals(uiRuntimeEquivalenceAuditSmoke.get("terminalMatches"))
                        && Boolean.TRUE.equals(uiRuntimeEquivalenceAuditSmoke.get("indexMatches"))
                        && Boolean.TRUE.equals(uiRuntimeEquivalenceAuditSmoke.get("lensMatches"))
                        && Boolean.TRUE.equals(uiRuntimeEquivalenceAuditSmoke.get("hudMatches"))
                        && Boolean.TRUE.equals(uiRuntimeEquivalenceAuditSmoke.get("missionMatches"))
                        && Boolean.TRUE.equals(uiRuntimeEquivalenceAuditSmoke.get("notificationsMatch"))
                        && dataSources.terminalReadyLine().equals(uiRuntimeEquivalenceAuditSmoke.get("terminalOutput"))
                        && EchoAgent5UiReference.ACTIVE_MISSION_UPDATED_STATUS.equals(
                        uiRuntimeEquivalenceAuditSmoke.get("missionUpdateStatus"))
                        && Double.valueOf(0.5D).equals(
                        uiRuntimeEquivalenceAuditSmoke.get("missionUpdateProgress"));
        Map<String, Object> screenCorePrimitiveExecutionSmoke =
                EchoAgent5ScreenCorePrimitiveExecutionSmoke.capture(dataSources);
        boolean screenCorePrimitiveExecutionSmokeSatisfied =
                Boolean.TRUE.equals(screenCorePrimitiveExecutionSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(screenCorePrimitiveExecutionSmoke.get("passed"))
                        && "EchoAgent5ScreenCorePrimitiveExecutionSmoke".equals(
                        screenCorePrimitiveExecutionSmoke.get("screenCorePrimitiveExecutionSmokeClass"))
                        && strings(screenCorePrimitiveExecutionSmoke, "executedPrimitives").equals(List.of(
                        "EchoScreen",
                        "EchoScreenStack",
                        "EchoScreenRoute",
                        "EchoHudLayer",
                        "EchoInputAction",
                        "EchoTheme",
                        "EchoWidget",
                        "EchoTextInput",
                        "EchoButton",
                        "EchoListView",
                        "EchoTerminalBuffer",
                        "EchoNotification"
                ))
                        && EchoAgent5UiReference.TERMINAL_SCREEN.equals(screenCorePrimitiveExecutionSmoke.get("stackCurrent"))
                        && "terminal:input".equals(screenCorePrimitiveExecutionSmoke.get("routeFocusPath"))
                        && EchoAgent5UiReference.TERMINAL_COMMAND.equals(
                        screenCorePrimitiveExecutionSmoke.get("terminalInputValue"))
                        && "Settings".equals(screenCorePrimitiveExecutionSmoke.get("selectedRow"))
                        && dataSources.notifications().get(0).get("message").equals(
                        screenCorePrimitiveExecutionSmoke.get("notificationMessage"));
        Map<String, Object> phase5UiParityAcceptanceSmoke =
                EchoAgent5Phase5UiParityAcceptanceSmoke.capture(dataSources);
        boolean phase5UiParityAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(phase5UiParityAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(phase5UiParityAcceptanceSmoke.get("passed"))
                        && "EchoAgent5Phase5UiParityAcceptanceSmoke".equals(
                        phase5UiParityAcceptanceSmoke.get("phase5UiParityAcceptanceSmokeClass"))
                        && checklist(phase5UiParityAcceptanceSmoke, "checklist").equals(List.of(
                        "terminal_opens",
                        "terminal_command_executes",
                        "index_opens_and_searches",
                        "lens_scans_target",
                        "hud_updates_health_hazard_mission",
                        "holomap_opens",
                        "wiki_page_opens",
                        "custom_main_menu_appears",
                        "no_screen_crash"
                ))
                        && "terminal_end_to_end:M->TERMINAL:status".equals(
                        phase5UiParityAcceptanceSmoke.get("terminalEffect"))
                        && "index_end_to_end:G->INDEX:ashfall".equals(
                        phase5UiParityAcceptanceSmoke.get("indexEffect"))
                        && ("lens_end_to_end:LEFT_ALT->LENS:" + dataSources.lensTarget()).equals(
                        phase5UiParityAcceptanceSmoke.get("lensEffect"))
                        && "hud_overlay_end_to_end:hud_update:HUD:85".equals(
                        phase5UiParityAcceptanceSmoke.get("hudEffect"))
                        && ("holomap_end_to_end:J->HOLOMAP:" + dataSources.holomapValues().get("marker")).equals(
                        phase5UiParityAcceptanceSmoke.get("holomapEffect"))
                        && ("wiki_end_to_end:direct:WIKI:" + dataSources.wikiValues().get("page")).equals(
                        phase5UiParityAcceptanceSmoke.get("wikiEffect"))
                        && "main_menu_end_to_end:accepted:4".equals(
                        phase5UiParityAcceptanceSmoke.get("mainMenuEffect"))
                        && "ui_host_end_to_end:M->TERMINAL:10".equals(
                        phase5UiParityAcceptanceSmoke.get("uiHostEffect"));
        Map<String, Object> liveClientAttachmentAcceptanceSmoke =
                EchoAgent5LiveClientAttachmentAcceptanceSmoke.capture();
        Map<String, Object> acceptedLiveClientAttachment =
                object(liveClientAttachmentAcceptanceSmoke.get("accepted"));
        boolean liveClientAttachmentAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveClientAttachmentAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveClientAttachmentAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveClientAttachmentAcceptanceSmoke".equals(
                        liveClientAttachmentAcceptanceSmoke.get("liveClientAttachmentAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveClientAttachment.get("accepted"))
                        && "live_client_attachment:accepted:EchoAgent5UiScreenHost".equals(
                        acceptedLiveClientAttachment.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveClientAttachment.get("minecraftClientReady"))
                        && Boolean.TRUE.equals(acceptedLiveClientAttachment.get("dashboardScreenCompiled"))
                        && Boolean.TRUE.equals(acceptedLiveClientAttachment.get("clientThreadAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientAttachment.get("physicalHotkeyPollingReady"))
                        && Boolean.TRUE.equals(acceptedLiveClientAttachment.get("screenClassMatches"))
                        && Boolean.FALSE.equals(object(
                        liveClientAttachmentAcceptanceSmoke.get("rejectedNoClient")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientAttachmentAcceptanceSmoke.get("rejectedNoScreen")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientAttachmentAcceptanceSmoke.get("rejectedNoClientThread")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientAttachmentAcceptanceSmoke.get("rejectedNoWindow")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientAttachmentAcceptanceSmoke.get("rejectedScreenMismatch")).get("accepted"));
        Map<String, Object> liveClientHostEvidenceAcceptanceSmoke =
                EchoAgent5LiveClientHostEvidenceAcceptanceSmoke.capture();
        Map<String, Object> acceptedLiveClientHostEvidence =
                object(liveClientHostEvidenceAcceptanceSmoke.get("accepted"));
        boolean liveClientHostEvidenceAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveClientHostEvidenceAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveClientHostEvidenceAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveClientHostEvidenceAcceptanceSmoke".equals(
                        liveClientHostEvidenceAcceptanceSmoke.get("liveClientHostEvidenceAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("accepted"))
                        && "live_client_host_evidence:accepted:EchoAgent5UiScreenHost".equals(
                        acceptedLiveClientHostEvidence.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("clientUiHostAttached"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("clientThreadAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("liveWindowHandlePresent"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("physicalHotkeyPollingReady"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("surfaceRouteSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("textInputSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("hudRouteSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("moduleCatalogSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("windowFocusSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("actualLiveWindowFocusAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("renderCallbackSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("actualLiveRenderCallbackAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("screenOwnershipSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("actualLiveScreenOwnershipAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("physicalPollLoopSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("actualLivePhysicalPollLoopAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("physicalEventTranscriptSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get(
                        "actualLivePhysicalEventTranscriptAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get(
                        "physicalRouteEffectTranscriptSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get(
                        "actualLivePhysicalRouteEffectTranscriptAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("physicalInputCoverageSmokePassed"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLivePhysicalInputCoverageAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("actualLiveSurfaceAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("actualSurfaceRouteAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("actualPhysicalInputAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("actualLiveSurfaceRendered"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("actualUiHostEndToEndAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("actualHudOverlayRouteAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("actualTextInputAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveTextInputAcceptanceAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("textInputCoverageSmokePassed"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveTextInputCoverageAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get(
                        "routeBoundTextCommandSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get(
                        "actualLiveRouteBoundTextCommandAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get(
                        "routeBoundLensScanSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get(
                        "actualLiveRouteBoundLensScanAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get(
                        "routeBoundHudUpdateSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get(
                        "actualLiveRouteBoundHudUpdateAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get(
                        "routeBoundHoloMapWikiSmokePassed"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get(
                        "actualLiveRouteBoundHoloMapWikiAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("actualLiveClientUiProbeAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveClientInteractionProbeAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveNotificationQueueAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveMissionObjectiveAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveCoreToolsAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveSystemFlowAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveInputFocusRoutingAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveScreenStackStabilityAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveVisualFrameAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveModuleSurfaceCatalogAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveTerminalEndToEndAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveIndexEndToEndAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveLensEndToEndAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveHudOverlayEndToEndAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveHoloMapEndToEndAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveWikiEndToEndAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveMainMenuEndToEndAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveMissionLogEndToEndAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveSettingsEndToEndAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLivePauseEndToEndAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveRecoveryEndToEndAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveNotificationEndToEndAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveMainMenuOverrideAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveHoloMapWikiNavigationAccepted"))
                        && Boolean.TRUE.equals(
                        acceptedLiveClientHostEvidence.get("actualLiveClientPhase5RouteSequenceAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientHostEvidence.get("actualLivePhase5Accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly")).get("accepted"))
                        && Boolean.TRUE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly")).get("headlessOnly"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualSurfaceRouteAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualPhysicalInputAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveSurfaceRendered"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualUiHostEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualHudOverlayRouteAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualTextInputAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveTextInputAcceptanceAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveTextInputCoverageAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveRouteBoundHudUpdateAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveRouteBoundHoloMapWikiAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveClientUiProbeAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveClientInteractionProbeAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveNotificationQueueAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveMissionObjectiveAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveCoreToolsAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveSystemFlowAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveInputFocusRoutingAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveScreenStackStabilityAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveVisualFrameAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveModuleSurfaceCatalogAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveTerminalEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveIndexEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveLensEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveHudOverlayEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveHoloMapEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveWikiEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveMainMenuEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveMissionLogEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveSettingsEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLivePauseEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveRecoveryEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveNotificationEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveMainMenuOverrideAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveHoloMapWikiNavigationAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveClientPhase5RouteSequenceAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLivePhase5Accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveWindowFocusAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveRenderCallbackAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveScreenOwnershipAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLivePhysicalPollLoopAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLivePhysicalInputCoverageAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedHeadlessOnly"))
                        .get("actualLiveSurfaceAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoPhysicalInput")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoRenderedSurface")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoScreenClass")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoLiveInteraction")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoLiveInteraction"))
                        .get("actualSurfaceRouteAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoLiveSurfaceAcceptance")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoLiveSurfaceAcceptance"))
                        .get("actualLiveSurfaceAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoWindowFocus")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoWindowFocus"))
                        .get("actualLiveWindowFocusAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoRenderCallback")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoRenderCallback"))
                        .get("actualLiveRenderCallbackAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoScreenOwnership")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoScreenOwnership"))
                        .get("actualLiveScreenOwnershipAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoPhysicalPollLoop")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoPhysicalPollLoop"))
                        .get("actualLivePhysicalPollLoopAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoPhysicalInputCoverage")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoPhysicalInputCoverage"))
                        .get("actualLivePhysicalInputCoverageAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoHudOverlayInteraction")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoHudOverlayInteraction"))
                        .get("actualHudOverlayRouteAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoTextInputInteraction")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoTextInputInteraction"))
                        .get("actualTextInputAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoTextInputAcceptance")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoTextInputAcceptance"))
                        .get("actualLiveTextInputAcceptanceAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoTextInputCoverage")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoTextInputCoverage"))
                        .get("actualLiveTextInputCoverageAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoRouteBoundLensScan")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoRouteBoundLensScan"))
                        .get("actualLiveRouteBoundLensScanAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoRouteBoundHudUpdate")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoRouteBoundHudUpdate"))
                        .get("actualLiveRouteBoundHudUpdateAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoRouteBoundHoloMapWiki")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoRouteBoundHoloMapWiki"))
                        .get("actualLiveRouteBoundHoloMapWikiAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoUiProbe")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoUiProbe"))
                        .get("actualLiveClientUiProbeAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoGeneratedInteractionProbe")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoGeneratedInteractionProbe"))
                        .get("actualLiveClientInteractionProbeAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoNotificationQueue")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoNotificationQueue"))
                        .get("actualLiveNotificationQueueAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoMissionObjective")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoMissionObjective"))
                        .get("actualLiveMissionObjectiveAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoCoreTools")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoCoreTools"))
                        .get("actualLiveCoreToolsAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoSystemFlow")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoSystemFlow"))
                        .get("actualLiveSystemFlowAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoInputFocusRouting")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoInputFocusRouting"))
                        .get("actualLiveInputFocusRoutingAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoScreenStackStability")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoScreenStackStability"))
                        .get("actualLiveScreenStackStabilityAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoVisualFrame")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoVisualFrame"))
                        .get("actualLiveVisualFrameAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoModuleSurfaceCatalog")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoModuleSurfaceCatalog"))
                        .get("actualLiveModuleSurfaceCatalogAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoTerminalEndToEnd")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoTerminalEndToEnd"))
                        .get("actualLiveTerminalEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoIndexEndToEnd")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoIndexEndToEnd"))
                        .get("actualLiveIndexEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoLensEndToEnd")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoLensEndToEnd"))
                        .get("actualLiveLensEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoHudOverlayEndToEnd")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoHudOverlayEndToEnd"))
                        .get("actualLiveHudOverlayEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoHoloMapEndToEnd")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoHoloMapEndToEnd"))
                        .get("actualLiveHoloMapEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoWikiEndToEnd")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoWikiEndToEnd"))
                        .get("actualLiveWikiEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoMainMenuEndToEnd")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoMainMenuEndToEnd"))
                        .get("actualLiveMainMenuEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoMissionLogEndToEnd")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoMissionLogEndToEnd"))
                        .get("actualLiveMissionLogEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoSettingsEndToEnd")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoSettingsEndToEnd"))
                        .get("actualLiveSettingsEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoPauseEndToEnd")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoPauseEndToEnd"))
                        .get("actualLivePauseEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoRecoveryEndToEnd")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoRecoveryEndToEnd"))
                        .get("actualLiveRecoveryEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoNotificationEndToEnd")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoNotificationEndToEnd"))
                        .get("actualLiveNotificationEndToEndAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoLivePhase5")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoLivePhase5"))
                        .get("actualLivePhase5Accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoMainMenuOverride")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoMainMenuOverride"))
                        .get("actualLiveMainMenuOverrideAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoHoloMapWikiNavigation")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoHoloMapWikiNavigation"))
                        .get("actualLiveHoloMapWikiNavigationAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoPhase5RouteSequence")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientHostEvidenceAcceptanceSmoke.get("rejectedNoPhase5RouteSequence"))
                        .get("actualLiveClientPhase5RouteSequenceAccepted"));
        Map<String, Object> headlessUiBridgeReadinessAcceptanceSmoke =
                EchoAgent5HeadlessUiBridgeReadinessAcceptanceSmoke.capture();
        Map<String, Object> acceptedHeadlessUiBridgeReadiness =
                object(headlessUiBridgeReadinessAcceptanceSmoke.get("accepted"));
        boolean headlessUiBridgeReadinessAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(headlessUiBridgeReadinessAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(headlessUiBridgeReadinessAcceptanceSmoke.get("passed"))
                        && "EchoAgent5HeadlessUiBridgeReadinessAcceptanceSmoke".equals(
                        headlessUiBridgeReadinessAcceptanceSmoke.get(
                                "headlessUiBridgeReadinessAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedHeadlessUiBridgeReadiness.get("accepted"))
                        && "headless_ui_bridge_readiness:accepted:EchoAgent5UiScreenHost".equals(
                        acceptedHeadlessUiBridgeReadiness.get("effect"))
                        && Boolean.TRUE.equals(acceptedHeadlessUiBridgeReadiness.get("fallbackHostAttached"))
                        && Boolean.TRUE.equals(acceptedHeadlessUiBridgeReadiness.get("headlessUiHostAttached"))
                        && Boolean.FALSE.equals(acceptedHeadlessUiBridgeReadiness.get("clientUiHostAttached"))
                        && Boolean.FALSE.equals(acceptedHeadlessUiBridgeReadiness.get("clientThreadAccepted"))
                        && Boolean.TRUE.equals(acceptedHeadlessUiBridgeReadiness.get("readyFlagsPresent"))
                        && Boolean.TRUE.equals(acceptedHeadlessUiBridgeReadiness.get("hotkeysReady"))
                        && Boolean.TRUE.equals(acceptedHeadlessUiBridgeReadiness.get("screenIdsReady"))
                        && Boolean.TRUE.equals(acceptedHeadlessUiBridgeReadiness.get("acceptedEvidenceReady"))
                        && Boolean.TRUE.equals(acceptedHeadlessUiBridgeReadiness.get("smokeEvidenceReady"))
                        && Boolean.TRUE.equals(acceptedHeadlessUiBridgeReadiness.get("liveHostRejectedHonesty"))
                        && Boolean.FALSE.equals(acceptedHeadlessUiBridgeReadiness.get("minecraftRuntimeAccessed"))
                        && Boolean.FALSE.equals(object(
                        headlessUiBridgeReadinessAcceptanceSmoke.get("rejectedLiveAttached")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        headlessUiBridgeReadinessAcceptanceSmoke.get("rejectedLiveAttached")).get("serviceCodeExecuted"))
                        && Boolean.FALSE.equals(object(
                        headlessUiBridgeReadinessAcceptanceSmoke.get("rejectedNoTerminal")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        headlessUiBridgeReadinessAcceptanceSmoke.get("rejectedNoTerminal")).get("serviceCodeExecuted"))
                        && Boolean.FALSE.equals(object(
                        headlessUiBridgeReadinessAcceptanceSmoke.get("rejectedNoHotkeys")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        headlessUiBridgeReadinessAcceptanceSmoke.get("rejectedNoHotkeys")).get("serviceCodeExecuted"))
                        && Boolean.FALSE.equals(object(
                        headlessUiBridgeReadinessAcceptanceSmoke.get("rejectedScreenMismatch")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        headlessUiBridgeReadinessAcceptanceSmoke.get("rejectedScreenMismatch")).get("serviceCodeExecuted"))
                        && Boolean.FALSE.equals(object(
                        headlessUiBridgeReadinessAcceptanceSmoke.get("rejectedLiveHostOverclaim")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        headlessUiBridgeReadinessAcceptanceSmoke.get("rejectedLiveHostOverclaim"))
                        .get("serviceCodeExecuted"));
        Map<String, Object> uiHudHostCallQueueReplaySmoke = EchoAgent5UiHudHostCallQueueReplaySmoke.capture();
        Map<String, Object> acceptedUiHudHostCallQueueReplay = object(uiHudHostCallQueueReplaySmoke.get("accepted"));
        Map<String, Object> acceptedUiHudRuntimeSnapshot =
                object(acceptedUiHudHostCallQueueReplay.get("runtimeSnapshot"));
        boolean uiHudHostCallQueueReplaySmokeSatisfied =
                Boolean.TRUE.equals(uiHudHostCallQueueReplaySmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(uiHudHostCallQueueReplaySmoke.get("passed"))
                        && "EchoAgent5UiHudHostCallQueueReplaySmoke".equals(
                        uiHudHostCallQueueReplaySmoke.get("uiHudHostCallQueueReplaySmokeClass"))
                        && "EchoNativeUiHudRuntimeTarget".equals(uiHudHostCallQueueReplaySmoke.get("nativeReference"))
                        && Boolean.TRUE.equals(acceptedUiHudHostCallQueueReplay.get("accepted"))
                        && Boolean.TRUE.equals(acceptedUiHudHostCallQueueReplay.get("serviceCodeExecuted"))
                        && "ui_hud_host_call_queue_replay:accepted:8".equals(
                        acceptedUiHudHostCallQueueReplay.get("effect"))
                        && Integer.valueOf(8).equals(acceptedUiHudHostCallQueueReplay.get("executedCommandCount"))
                        && Integer.valueOf(8).equals(acceptedUiHudHostCallQueueReplay.get("mutatingOperationCount"))
                        && Integer.valueOf(8).equals(acceptedUiHudHostCallQueueReplay.get("runtimeHostConsumedCommandCount"))
                        && Integer.valueOf(8).equals(acceptedUiHudHostCallQueueReplay.get("runtimeHostMutationCount"))
                        && Integer.valueOf(8).equals(acceptedUiHudHostCallQueueReplay.get("missionUpdateCount"))
                        && Integer.valueOf(8).equals(acceptedUiHudHostCallQueueReplay.get("saveUpdateCount"))
                        && Integer.valueOf(8).equals(acceptedUiHudHostCallQueueReplay.get("feedbackCount"))
                        && Boolean.TRUE.equals(acceptedUiHudHostCallQueueReplay.get("queueConsumedByRuntimeHost"))
                        && Boolean.TRUE.equals(acceptedUiHudHostCallQueueReplay.get("runtimeHostMutated"))
                        && Boolean.TRUE.equals(acceptedUiHudHostCallQueueReplay.get("missionUpdated"))
                        && Boolean.TRUE.equals(acceptedUiHudHostCallQueueReplay.get("saveTouched"))
                        && Boolean.TRUE.equals(acceptedUiHudHostCallQueueReplay.get("feedbackEmitted"))
                        && Boolean.TRUE.equals(acceptedUiHudHostCallQueueReplay.get("nativeStateMutated"))
                        && EchoAgent5UiReference.ACTIVE_MISSION_OBJECTIVE.equals(
                        acceptedUiHudRuntimeSnapshot.get("missionTrackerLine"))
                        && "AIR stable; hazards marked.".equals(
                        acceptedUiHudRuntimeSnapshot.get("hazardReadoutLine"))
                        && "echoashfallprotocol:welcome_screen".equals(
                        acceptedUiHudRuntimeSnapshot.get("welcomeScreen"))
                        && "ashfall:first_ten_minutes".equals(acceptedUiHudRuntimeSnapshot.get("terminalCard"))
                        && "echowiki:ashfall".equals(acceptedUiHudRuntimeSnapshot.get("wikiGuide"))
                        && Integer.valueOf(1).equals(acceptedUiHudRuntimeSnapshot.get("lensProfileCount"))
                        && Integer.valueOf(2).equals(acceptedUiHudRuntimeSnapshot.get("holomapLayerCount"))
                        && Integer.valueOf(1).equals(acceptedUiHudRuntimeSnapshot.get("codexEntryCount"))
                        && Boolean.FALSE.equals(acceptedUiHudHostCallQueueReplay.get("minecraftRuntimeAccessed"))
                        && Boolean.FALSE.equals(object(
                        uiHudHostCallQueueReplaySmoke.get("rejectedMissingHazard")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        uiHudHostCallQueueReplaySmoke.get("rejectedMissingHazard")).get("serviceCodeExecuted"))
                        && Boolean.FALSE.equals(object(
                        uiHudHostCallQueueReplaySmoke.get("rejectedStandaloneCopy")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        uiHudHostCallQueueReplaySmoke.get("rejectedStandaloneCopy")).get("serviceCodeExecuted"))
                        && Boolean.FALSE.equals(object(
                        uiHudHostCallQueueReplaySmoke.get("rejectedUnexecutedCommands")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        uiHudHostCallQueueReplaySmoke.get("rejectedUnexecutedCommands")).get("serviceCodeExecuted"))
                        && Boolean.FALSE.equals(object(
                        uiHudHostCallQueueReplaySmoke.get("rejectedQueueOnly")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        uiHudHostCallQueueReplaySmoke.get("rejectedQueueOnly")).get("serviceCodeExecuted"));
        Map<String, Object> adapterCoreRuntimeBridgeGuardAcceptanceSmoke =
                EchoAgent5AdapterCoreRuntimeBridgeGuardAcceptance.smoke();
        Map<String, Object> acceptedAdapterCoreRuntimeBridgeGuard =
                object(adapterCoreRuntimeBridgeGuardAcceptanceSmoke.get("accepted"));
        Map<String, Object> rejectedNoRuntimeBridge =
                object(adapterCoreRuntimeBridgeGuardAcceptanceSmoke.get("rejectedNoRuntimeBridge"));
        Map<String, Object> rejectedNoHostEvidence =
                object(adapterCoreRuntimeBridgeGuardAcceptanceSmoke.get("rejectedNoHostEvidence"));
        boolean adapterCoreRuntimeBridgeGuardAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(adapterCoreRuntimeBridgeGuardAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(adapterCoreRuntimeBridgeGuardAcceptanceSmoke.get("passed"))
                        && "EchoAgent5AdapterCoreRuntimeBridgeGuardAcceptance".equals(
                        adapterCoreRuntimeBridgeGuardAcceptanceSmoke.get(
                                "adapterCoreRuntimeBridgeGuardAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedAdapterCoreRuntimeBridgeGuard.get("accepted"))
                        && "adaptercore_runtime_bridge_guard:accepted:agent5_ui".equals(
                        acceptedAdapterCoreRuntimeBridgeGuard.get("effect"))
                        && Boolean.FALSE.equals(rejectedNoRuntimeBridge.get("accepted"))
                        && "adaptercore_runtime_bridge_inactive".equals(rejectedNoRuntimeBridge.get("rejection"))
                        && Boolean.FALSE.equals(rejectedNoHostEvidence.get("accepted"))
                        && "live_client_host_evidence_not_accepted".equals(rejectedNoHostEvidence.get("rejection"));
        Map<String, Object> liveClientUiProbeAcceptanceSmoke = EchoAgent5LiveClientUiProbeAcceptance.smoke();
        Map<String, Object> acceptedLiveClientUiProbe = object(liveClientUiProbeAcceptanceSmoke.get("accepted"));
        boolean liveClientUiProbeAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveClientUiProbeAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveClientUiProbeAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveClientUiProbeAcceptance".equals(
                        liveClientUiProbeAcceptanceSmoke.get("liveClientUiProbeAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveClientUiProbe.get("accepted"))
                        && "live_client_ui_probe:accepted:11".equals(acceptedLiveClientUiProbe.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveClientUiProbe.get("scheduled"))
                        && Boolean.TRUE.equals(acceptedLiveClientUiProbe.get("executed"))
                        && Integer.valueOf(11).equals(acceptedLiveClientUiProbe.get("routeCount"))
                        && strings(acceptedLiveClientUiProbe, "surfaces").equals(List.of(
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
                ))
                        && Boolean.FALSE.equals(object(
                        liveClientUiProbeAcceptanceSmoke.get("rejectedNotExecuted")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientUiProbeAcceptanceSmoke.get("rejectedMissingSurface")).get("accepted"));
        liveClientHostEvidenceAcceptanceSmokeSatisfied =
                liveClientHostEvidenceAcceptanceSmokeSatisfied && liveClientUiProbeAcceptanceSmokeSatisfied;
        Map<String, Object> liveClientInteractionProbeAcceptanceSmoke =
                EchoAgent5LiveClientInteractionProbeAcceptance.smoke();
        Map<String, Object> acceptedLiveClientInteractionProbe =
                object(liveClientInteractionProbeAcceptanceSmoke.get("accepted"));
        boolean liveClientInteractionProbeAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveClientInteractionProbeAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveClientInteractionProbeAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveClientInteractionProbeAcceptance".equals(
                        liveClientInteractionProbeAcceptanceSmoke
                                .get("liveClientInteractionProbeAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveClientInteractionProbe.get("accepted"))
                        && "live_client_interaction_probe:accepted:11".equals(
                        acceptedLiveClientInteractionProbe.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveClientInteractionProbe.get("scheduled"))
                        && Boolean.TRUE.equals(acceptedLiveClientInteractionProbe.get("executed"))
                        && Integer.valueOf(11).equals(acceptedLiveClientInteractionProbe.get("routeCount"))
                        && strings(acceptedLiveClientInteractionProbe, "surfaces").equals(List.of(
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
                ))
                        && strings(acceptedLiveClientInteractionProbe, "interactions").equals(List.of(
                        "terminal_command",
                        "index_search",
                        "lens_scan",
                        "mission_update",
                        "settings_adjustment",
                        "pause_resume",
                        "recovery_action",
                        "mouse_focus",
                        "mouse_focus",
                        "main_menu_continue",
                        "hud_update"
                ))
                        && Boolean.FALSE.equals(object(
                        liveClientInteractionProbeAcceptanceSmoke.get("rejectedNotExecuted")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientInteractionProbeAcceptanceSmoke.get("rejectedWrongInteraction")).get("accepted"));
        liveClientHostEvidenceAcceptanceSmokeSatisfied =
                liveClientHostEvidenceAcceptanceSmokeSatisfied && liveClientInteractionProbeAcceptanceSmokeSatisfied;
        Map<String, Object> liveClientPhase5RouteSequenceAcceptanceSmoke =
                EchoAgent5LiveClientPhase5RouteSequenceAcceptance.smoke();
        Map<String, Object> acceptedLiveClientPhase5RouteSequence =
                object(liveClientPhase5RouteSequenceAcceptanceSmoke.get("accepted"));
        boolean liveClientPhase5RouteSequenceAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveClientPhase5RouteSequenceAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveClientPhase5RouteSequenceAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveClientPhase5RouteSequenceAcceptance".equals(
                        liveClientPhase5RouteSequenceAcceptanceSmoke
                                .get("liveClientPhase5RouteSequenceAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveClientPhase5RouteSequence.get("accepted"))
                        && "live_client_phase5_route_sequence:accepted:17".equals(
                        acceptedLiveClientPhase5RouteSequence.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveClientPhase5RouteSequence.get("scheduled"))
                        && Boolean.TRUE.equals(acceptedLiveClientPhase5RouteSequence.get("executed"))
                        && Integer.valueOf(17).equals(acceptedLiveClientPhase5RouteSequence.get("routeCount"))
                        && strings(acceptedLiveClientPhase5RouteSequence, "surfaces").equals(List.of(
                        "TERMINAL",
                        "INDEX",
                        "INDEX",
                        "INDEX",
                        "INDEX",
                        "LENS",
                        "HOLOMAP",
                        "HOLOMAP",
                        "HOLOMAP",
                        "HOLOMAP",
                        "HOLOMAP",
                        "SIGNALOS",
                        "ASHFALL_DRONE",
                        "ASHFALL_DRONE",
                        "ASHFALL_DRONE",
                        "ASHFALL_DRONE",
                        "PAUSE"
                ))
                        && strings(acceptedLiveClientPhase5RouteSequence, "routeTypes").equals(List.of(
                        "screen",
                        "screen",
                        "screen",
                        "screen",
                        "screen",
                        "screen",
                        "screen",
                        "screen",
                        "screen",
                        "screen",
                        "screen",
                        "screen",
                        "action",
                        "action",
                        "action",
                        "action",
                        "screen"
                ))
                        && strings(acceptedLiveClientPhase5RouteSequence, "hotkeys").equals(List.of(
                        "M",
                        "G",
                        "R",
                        "U",
                        "B",
                        "LEFT_ALT",
                        "J",
                        "K",
                        "RIGHT_BRACKET",
                        "LEFT_BRACKET",
                        "BACKSLASH",
                        "N",
                        "X",
                        "C",
                        "Y",
                        "Z",
                        "ESCAPE"
                ))
                        && Boolean.TRUE.equals(acceptedLiveClientPhase5RouteSequence.get("physicalHotkeyAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveClientPhase5RouteSequence.get("physicalPollerExecuted"))
                        && strings(acceptedLiveClientPhase5RouteSequence, "physicalHotkeySurfaces").equals(List.of(
                        "TERMINAL",
                        "INDEX",
                        "INDEX",
                        "INDEX",
                        "INDEX",
                        "LENS",
                        "HOLOMAP",
                        "HOLOMAP",
                        "HOLOMAP",
                        "HOLOMAP",
                        "HOLOMAP",
                        "SIGNALOS",
                        "ASHFALL_DRONE",
                        "ASHFALL_DRONE",
                        "ASHFALL_DRONE",
                        "ASHFALL_DRONE",
                        "PAUSE"
                ))
                        && strings(acceptedLiveClientPhase5RouteSequence, "physicalHotkeyEffects").equals(List.of(
                        "physical_hotkey:M->TERMINAL:terminal.open",
                        "physical_hotkey:G->INDEX:index.catalog",
                        "physical_hotkey:R->INDEX:index.recipe",
                        "physical_hotkey:U->INDEX:index.usage",
                        "physical_hotkey:B->INDEX:index.bookmark",
                        "physical_hotkey:LEFT_ALT->LENS:lens.deep_scan",
                        "physical_hotkey:J->HOLOMAP:holomap.open",
                        "physical_hotkey:K->HOLOMAP:holomap.toggle_minimap",
                        "physical_hotkey:RIGHT_BRACKET->HOLOMAP:holomap.zoom_in",
                        "physical_hotkey:LEFT_BRACKET->HOLOMAP:holomap.zoom_out",
                        "physical_hotkey:BACKSLASH->HOLOMAP:holomap.cycle_corner",
                        "physical_hotkey:N->SIGNALOS:signalos.terminal",
                        "physical_hotkey:X->ASHFALL_DRONE:ashfall.drone_recall",
                        "physical_hotkey:C->ASHFALL_DRONE:ashfall.drone_scan",
                        "physical_hotkey:Y->ASHFALL_DRONE:ashfall.drone_scout",
                        "physical_hotkey:Z->ASHFALL_DRONE:ashfall.drone_status",
                        "physical_hotkey:ESCAPE->PAUSE:pause.toggle"
                ))
                        && Boolean.TRUE.equals(acceptedLiveClientPhase5RouteSequence.get("noScreenCrash"))
                        && Boolean.FALSE.equals(object(
                        liveClientPhase5RouteSequenceAcceptanceSmoke.get("rejectedWrongOrder")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientPhase5RouteSequenceAcceptanceSmoke.get("rejectedWrongRouteType")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientPhase5RouteSequenceAcceptanceSmoke.get("rejectedWrongHotkey")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientPhase5RouteSequenceAcceptanceSmoke.get("rejectedNoPhysicalHotkey")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveClientPhase5RouteSequenceAcceptanceSmoke.get("rejectedCrash")).get("accepted"));
        liveClientHostEvidenceAcceptanceSmokeSatisfied =
                liveClientHostEvidenceAcceptanceSmokeSatisfied && liveClientPhase5RouteSequenceAcceptanceSmokeSatisfied;
        Map<String, Object> livePhase5AcceptanceSmoke = EchoAgent5LivePhase5Acceptance.smoke();
        Map<String, Object> acceptedLivePhase5 = object(livePhase5AcceptanceSmoke.get("accepted"));
        boolean livePhase5AcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(livePhase5AcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(livePhase5AcceptanceSmoke.get("passed"))
                        && "EchoAgent5LivePhase5Acceptance".equals(
                        livePhase5AcceptanceSmoke.get("livePhase5AcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLivePhase5.get("accepted"))
                        && "live_phase5:accepted:9".equals(acceptedLivePhase5.get("effect"))
                        && checklist(acceptedLivePhase5, "checklist").equals(List.of(
                        "terminal_opens",
                        "terminal_command_executes",
                        "index_opens_and_searches",
                        "lens_scans_target",
                        "hud_updates_health_hazard_mission",
                        "holomap_opens",
                        "wiki_page_opens",
                        "custom_main_menu_appears",
                        "no_screen_crash"
                ))
                        && Boolean.TRUE.equals(acceptedLivePhase5.get("uiProbeAccepted"))
                        && Boolean.TRUE.equals(acceptedLivePhase5.get("interactionProbeAccepted"))
                        && Boolean.TRUE.equals(acceptedLivePhase5.get("terminalTextAccepted"))
                        && Boolean.TRUE.equals(acceptedLivePhase5.get("indexTextAccepted"))
                        && Boolean.TRUE.equals(acceptedLivePhase5.get("hudOverlayAccepted"))
                        && Boolean.TRUE.equals(acceptedLivePhase5.get("mainMenuOverrideAccepted"))
                        && Boolean.TRUE.equals(acceptedLivePhase5.get("holomapWikiNavigationAccepted"))
                        && Boolean.TRUE.equals(acceptedLivePhase5.get("phase5RouteSequenceAccepted"))
                        && Boolean.TRUE.equals(acceptedLivePhase5.get("noScreenCrash"))
                        && Boolean.FALSE.equals(object(livePhase5AcceptanceSmoke.get("rejectedNoUiProbe")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        livePhase5AcceptanceSmoke.get("rejectedNoInteractionProbe")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        livePhase5AcceptanceSmoke.get("rejectedNoHudOverlay")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        livePhase5AcceptanceSmoke.get("rejectedNoMainMenuOverride")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        livePhase5AcceptanceSmoke.get("rejectedNoHoloMapWikiNavigation")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        livePhase5AcceptanceSmoke.get("rejectedNoPhase5RouteSequence")).get("accepted"));
        liveClientHostEvidenceAcceptanceSmokeSatisfied =
                liveClientHostEvidenceAcceptanceSmokeSatisfied && livePhase5AcceptanceSmokeSatisfied;
        Map<String, Object> liveSurfaceRouteAcceptanceSmoke =
                EchoAgent5LiveSurfaceRouteAcceptanceSmoke.capture(dataSources);
        boolean liveSurfaceRouteAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveSurfaceRouteAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveSurfaceRouteAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveSurfaceRouteAcceptanceSmoke".equals(
                        liveSurfaceRouteAcceptanceSmoke.get("liveSurfaceRouteAcceptanceSmokeClass"))
                        && strings(liveSurfaceRouteAcceptanceSmoke, "routeSurfaces").equals(List.of(
                        "TERMINAL",
                        "INDEX",
                        "INDEX",
                        "INDEX",
                        "INDEX",
                        "LENS",
                        "HOLOMAP",
                        "HOLOMAP",
                        "HOLOMAP",
                        "HOLOMAP",
                        "HOLOMAP",
                        "PAUSE"
                ))
                        && Boolean.FALSE.equals(object(
                        liveSurfaceRouteAcceptanceSmoke.get("rejectedDroneAction")).get("accepted"))
                        && "ASHFALL_DRONE".equals(object(
                        liveSurfaceRouteAcceptanceSmoke.get("rejectedDroneAction")).get("surface"))
                        && Boolean.TRUE.equals(object(
                        liveSurfaceRouteAcceptanceSmoke.get("rejectedDroneAction")).get("physicalHotkeyHandled"))
                        && Boolean.TRUE.equals(object(
                        liveSurfaceRouteAcceptanceSmoke.get("rejectedDroneAction")).get("liveSurfaceAccepted"))
                        && Boolean.TRUE.equals(object(
                        liveSurfaceRouteAcceptanceSmoke.get("rejectedDroneAction")).get("physicalInputAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveSurfaceRouteAcceptanceSmoke.get("rejectedDroneAction")).get("liveSurfaceRendered"))
                        && maps(liveSurfaceRouteAcceptanceSmoke.get("acceptedRoutes")).stream()
                        .allMatch(route -> Boolean.TRUE.equals(route.get("accepted"))
                                && String.valueOf(route.get("effect")).startsWith("live_surface_route:accepted:")
                                && Boolean.TRUE.equals(route.get("physicalHotkeyHandled"))
                                && Boolean.TRUE.equals(route.get("liveSurfaceAccepted"))
                                && Boolean.TRUE.equals(route.get("physicalInputAccepted"))
                                && Boolean.TRUE.equals(route.get("liveSurfaceRendered")))
                        && Boolean.FALSE.equals(object(
                        liveSurfaceRouteAcceptanceSmoke.get("rejectedNoHotkey")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveSurfaceRouteAcceptanceSmoke.get("rejectedNoSurface")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveSurfaceRouteAcceptanceSmoke.get("rejectedNoRender")).get("accepted"));
        Map<String, Object> liveTextInputAcceptanceSmoke =
                EchoAgent5LiveTextInputAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedTerminalTextInput = object(liveTextInputAcceptanceSmoke.get("terminal"));
        Map<String, Object> acceptedIndexTextInput = object(liveTextInputAcceptanceSmoke.get("index"));
        Map<String, Object> liveUiInteractionRecorderSmoke =
                EchoAgent5LiveUiInteractionRecorder.smoke(dataSources);
        boolean liveTextInputAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveTextInputAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveTextInputAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveTextInputAcceptanceSmoke".equals(
                        liveTextInputAcceptanceSmoke.get("liveTextInputAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedTerminalTextInput.get("accepted"))
                        && "live_text_input:accepted:TERMINAL:status".equals(
                        acceptedTerminalTextInput.get("effect"))
                        && EchoAgent5UiReference.TERMINAL_COMMAND.equals(
                        acceptedTerminalTextInput.get("finalBuffer"))
                        && dataSources.terminalReadyLine().equals(acceptedTerminalTextInput.get("output"))
                        && Boolean.TRUE.equals(acceptedIndexTextInput.get("accepted"))
                        && "live_text_input:accepted:INDEX:ashfall".equals(
                        acceptedIndexTextInput.get("effect"))
                        && EchoAgent5UiReference.INDEX_QUERY.equals(acceptedIndexTextInput.get("finalBuffer"))
                        && dataSources.indexResult().equals(acceptedIndexTextInput.get("output"))
                        && Boolean.TRUE.equals(liveUiInteractionRecorderSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveUiInteractionRecorderSmoke.get("passed"))
                        && strings(liveUiInteractionRecorderSmoke, "acceptedModes").equals(List.of("TERMINAL", "INDEX"))
                        && Boolean.FALSE.equals(object(
                        liveTextInputAcceptanceSmoke.get("rejectedUnfocused")).get("accepted"));
        Map<String, Object> liveHudOverlayRouteAcceptanceSmoke =
                EchoAgent5LiveHudOverlayRouteAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedLiveHudOverlayRoute =
                object(liveHudOverlayRouteAcceptanceSmoke.get("accepted"));
        boolean liveHudOverlayRouteAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveHudOverlayRouteAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveHudOverlayRouteAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveHudOverlayRouteAcceptanceSmoke".equals(
                        liveHudOverlayRouteAcceptanceSmoke.get("liveHudOverlayRouteAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveHudOverlayRoute.get("accepted"))
                        && "live_hud_overlay_route:accepted:hud_update:HUD:85".equals(
                        acceptedLiveHudOverlayRoute.get("effect"))
                        && "HUD_UPDATE".equals(acceptedLiveHudOverlayRoute.get("key"))
                        && "HUD".equals(acceptedLiveHudOverlayRoute.get("destinationMode"))
                        && Boolean.TRUE.equals(acceptedLiveHudOverlayRoute.get("overlayRendered"))
                        && Integer.valueOf(85).equals(acceptedLiveHudOverlayRoute.get("hudHealth"))
                        && Boolean.FALSE.equals(object(
                        liveHudOverlayRouteAcceptanceSmoke.get("rejectedNoRoute")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveHudOverlayRouteAcceptanceSmoke.get("rejectedNoOverlay")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveHudOverlayRouteAcceptanceSmoke.get("rejectedNoEndToEnd")).get("accepted"));
        Map<String, Object> liveMainMenuOverrideAcceptanceSmoke =
                EchoAgent5LiveMainMenuOverrideAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedLiveMainMenuOverride =
                object(liveMainMenuOverrideAcceptanceSmoke.get("accepted"));
        boolean liveMainMenuOverrideAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveMainMenuOverrideAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveMainMenuOverrideAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveMainMenuOverrideAcceptanceSmoke".equals(
                        liveMainMenuOverrideAcceptanceSmoke.get("liveMainMenuOverrideAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveMainMenuOverride.get("accepted"))
                        && "live_main_menu_override:accepted:MAIN_MENU:4".equals(
                        acceptedLiveMainMenuOverride.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveMainMenuOverride.get("titleScreenDetected"))
                        && Boolean.TRUE.equals(acceptedLiveMainMenuOverride.get("overrideAttached"))
                        && Boolean.TRUE.equals(acceptedLiveMainMenuOverride.get("liveSurfaceAccepted"))
                        && "MAIN_MENU".equals(acceptedLiveMainMenuOverride.get("surface"))
                        && Integer.valueOf(4).equals(acceptedLiveMainMenuOverride.get("optionCount"))
                        && "SETTINGS".equals(acceptedLiveMainMenuOverride.get("settingsDestination"))
                        && Boolean.TRUE.equals(acceptedLiveMainMenuOverride.get("quitRequested"))
                        && Boolean.FALSE.equals(object(
                        liveMainMenuOverrideAcceptanceSmoke.get("rejectedNoTitle")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveMainMenuOverrideAcceptanceSmoke.get("rejectedNoSurface")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveMainMenuOverrideAcceptanceSmoke.get("rejectedNoOptions")).get("accepted"));
        Map<String, Object> liveNotificationQueueAcceptanceSmoke =
                EchoAgent5LiveNotificationQueueAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedLiveNotificationQueue =
                object(liveNotificationQueueAcceptanceSmoke.get("accepted"));
        boolean liveNotificationQueueAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveNotificationQueueAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveNotificationQueueAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveNotificationQueueAcceptanceSmoke".equals(
                        liveNotificationQueueAcceptanceSmoke.get("liveNotificationQueueAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveNotificationQueue.get("accepted"))
                        && "live_notification_queue:accepted:2->1:top_left_safe_area".equals(
                        acceptedLiveNotificationQueue.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveNotificationQueue.get("queueDispatched"))
                        && "top_left_safe_area".equals(acceptedLiveNotificationQueue.get("notificationAnchor"))
                        && Boolean.TRUE.equals(acceptedLiveNotificationQueue.get("queueAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveNotificationQueue.get("hudAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveNotificationQueue.get("dismissAccepted"))
                        && Integer.valueOf(2).equals(acceptedLiveNotificationQueue.get("sourceCount"))
                        && Integer.valueOf(2).equals(acceptedLiveNotificationQueue.get("dispatchedCount"))
                        && "agent5-notification-1".equals(acceptedLiveNotificationQueue.get("dismissedId"))
                        && strings(acceptedLiveNotificationQueue, "remainingMessages")
                        .equals(List.of(String.valueOf(dataSources.notifications().get(1).get("message"))))
                        && Boolean.FALSE.equals(object(
                        liveNotificationQueueAcceptanceSmoke.get("rejectedNoDispatch")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveNotificationQueueAcceptanceSmoke.get("rejectedWrongAnchor")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveNotificationQueueAcceptanceSmoke.get("rejectedNoEndToEnd")).get("accepted"));
        Map<String, Object> liveHoloMapWikiNavigationAcceptanceSmoke =
                EchoAgent5LiveHoloMapWikiNavigationAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedLiveHoloMapWikiNavigation =
                object(liveHoloMapWikiNavigationAcceptanceSmoke.get("accepted"));
        boolean liveHoloMapWikiNavigationAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveHoloMapWikiNavigationAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveHoloMapWikiNavigationAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveHoloMapWikiNavigationAcceptanceSmoke".equals(
                        liveHoloMapWikiNavigationAcceptanceSmoke.get("liveHoloMapWikiNavigationAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveHoloMapWikiNavigation.get("accepted"))
                        && "live_holomap_wiki_navigation:accepted:J/direct".equals(
                        acceptedLiveHoloMapWikiNavigation.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveHoloMapWikiNavigation.get("holomapAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveHoloMapWikiNavigation.get("wikiAccepted"))
                        && "HOLOMAP".equals(acceptedLiveHoloMapWikiNavigation.get("holomapSurface"))
                        && "WIKI".equals(acceptedLiveHoloMapWikiNavigation.get("wikiSurface"))
                        && dataSources.holomapValues().get("layer").equals(
                        acceptedLiveHoloMapWikiNavigation.get("layer"))
                        && dataSources.holomapValues().get("marker").equals(
                        acceptedLiveHoloMapWikiNavigation.get("marker"))
                        && dataSources.wikiValues().get("guide").equals(
                        acceptedLiveHoloMapWikiNavigation.get("guide"))
                        && dataSources.wikiValues().get("page").equals(
                        acceptedLiveHoloMapWikiNavigation.get("page"))
                        && Boolean.FALSE.equals(object(
                        liveHoloMapWikiNavigationAcceptanceSmoke.get("rejectedNoHoloMap")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveHoloMapWikiNavigationAcceptanceSmoke.get("rejectedNoWiki")).get("accepted"));
        Map<String, Object> liveSystemFlowAcceptanceSmoke =
                EchoAgent5LiveSystemFlowAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedLiveSystemFlow =
                object(liveSystemFlowAcceptanceSmoke.get("accepted"));
        boolean liveSystemFlowAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveSystemFlowAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveSystemFlowAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveSystemFlowAcceptanceSmoke".equals(
                        liveSystemFlowAcceptanceSmoke.get("liveSystemFlowAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveSystemFlow.get("accepted"))
                        && "live_system_flow:accepted:SETTINGS_ACTION/ESCAPE/RECOVERY_ACTION".equals(
                        acceptedLiveSystemFlow.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveSystemFlow.get("settingsAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveSystemFlow.get("pauseAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveSystemFlow.get("recoveryAccepted"))
                        && "ashfall-accessible".equals(acceptedLiveSystemFlow.get("settingsProfile"))
                        && Double.valueOf(1.25D).equals(acceptedLiveSystemFlow.get("settingsHudScale"))
                        && Boolean.FALSE.equals(acceptedLiveSystemFlow.get("settingsSubtitles"))
                        && "LENS".equals(acceptedLiveSystemFlow.get("pauseResumeDestination"))
                        && "recovery:recover".equals(acceptedLiveSystemFlow.get("recoveryFocusPath"))
                        && Boolean.FALSE.equals(object(
                        liveSystemFlowAcceptanceSmoke.get("rejectedNoSettings")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveSystemFlowAcceptanceSmoke.get("rejectedNoPause")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveSystemFlowAcceptanceSmoke.get("rejectedNoRecovery")).get("accepted"));
        Map<String, Object> liveCoreToolsAcceptanceSmoke =
                EchoAgent5LiveCoreToolsAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedLiveCoreTools =
                object(liveCoreToolsAcceptanceSmoke.get("accepted"));
        boolean liveCoreToolsAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveCoreToolsAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveCoreToolsAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveCoreToolsAcceptanceSmoke".equals(
                        liveCoreToolsAcceptanceSmoke.get("liveCoreToolsAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveCoreTools.get("accepted"))
                        && "live_core_tools:accepted:M/G/LEFT_ALT".equals(acceptedLiveCoreTools.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveCoreTools.get("terminalAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveCoreTools.get("indexAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveCoreTools.get("lensAccepted"))
                        && dataSources.terminalCommand().equals(acceptedLiveCoreTools.get("terminalCommand"))
                        && dataSources.indexQuery().equals(acceptedLiveCoreTools.get("indexQuery"))
                        && dataSources.lensTarget().equals(acceptedLiveCoreTools.get("lensTarget"))
                        && Boolean.FALSE.equals(object(
                        liveCoreToolsAcceptanceSmoke.get("rejectedNoTerminal")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveCoreToolsAcceptanceSmoke.get("rejectedNoIndex")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveCoreToolsAcceptanceSmoke.get("rejectedNoLens")).get("accepted"));
        Map<String, Object> liveMissionObjectiveAcceptanceSmoke =
                EchoAgent5LiveMissionObjectiveAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedLiveMissionObjective =
                object(liveMissionObjectiveAcceptanceSmoke.get("accepted"));
        boolean liveMissionObjectiveAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveMissionObjectiveAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveMissionObjectiveAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveMissionObjectiveAcceptanceSmoke".equals(
                        liveMissionObjectiveAcceptanceSmoke.get("liveMissionObjectiveAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveMissionObjective.get("accepted"))
                        && ("live_mission_objective:accepted:MISSION_ACTION/HUD:"
                        + EchoAgent5UiReference.ACTIVE_MISSION_ID + ":UPDATED").equals(
                        acceptedLiveMissionObjective.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveMissionObjective.get("missionAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveMissionObjective.get("hudAccepted"))
                        && EchoAgent5UiReference.ACTIVE_MISSION_ID.equals(
                        acceptedLiveMissionObjective.get("missionId"))
                        && EchoAgent5UiReference.ACTIVE_MISSION_UPDATED_STATUS.equals(
                        acceptedLiveMissionObjective.get("missionStatus"))
                        && Double.valueOf(0.5D).equals(acceptedLiveMissionObjective.get("missionProgress"))
                        && Integer.valueOf(85).equals(acceptedLiveMissionObjective.get("hudHealth"))
                        && dataSources.hudValues().get("hazard").equals(acceptedLiveMissionObjective.get("hudHazard"))
                        && EchoAgent5UiReference.ACTIVE_MISSION_OBJECTIVE.equals(
                        acceptedLiveMissionObjective.get("hudMission"))
                        && Boolean.FALSE.equals(object(
                        liveMissionObjectiveAcceptanceSmoke.get("rejectedNoMission")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveMissionObjectiveAcceptanceSmoke.get("rejectedNoHud")).get("accepted"));
        Map<String, Object> liveInputFocusRoutingAcceptanceSmoke =
                EchoAgent5LiveInputFocusRoutingAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedLiveInputFocusRouting =
                object(liveInputFocusRoutingAcceptanceSmoke.get("accepted"));
        boolean liveInputFocusRoutingAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveInputFocusRoutingAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveInputFocusRoutingAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveInputFocusRoutingAcceptanceSmoke".equals(
                        liveInputFocusRoutingAcceptanceSmoke.get("liveInputFocusRoutingAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveInputFocusRouting.get("accepted"))
                        && "live_input_focus_routing:accepted:focus/text/mouse/list".equals(
                        acceptedLiveInputFocusRouting.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveInputFocusRouting.get("focusAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveInputFocusRouting.get("editingAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveInputFocusRouting.get("mouseAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveInputFocusRouting.get("listAccepted"))
                        && dataSources.terminalCommand().equals(acceptedLiveInputFocusRouting.get("terminalBuffer"))
                        && dataSources.indexQuery().equals(acceptedLiveInputFocusRouting.get("indexBuffer"))
                        && strings(acceptedLiveInputFocusRouting, "selectedOptions").equals(List.of(
                        "New Ashfall Run",
                        "Settings",
                        "Theme",
                        "Input Mode",
                        "Quit to Main Menu"
                ))
                        && Boolean.FALSE.equals(object(
                        liveInputFocusRoutingAcceptanceSmoke.get("rejectedNoFocus")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveInputFocusRoutingAcceptanceSmoke.get("rejectedNoEditing")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveInputFocusRoutingAcceptanceSmoke.get("rejectedNoMouse")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveInputFocusRoutingAcceptanceSmoke.get("rejectedNoList")).get("accepted"));
        Map<String, Object> liveScreenStackStabilityAcceptanceSmoke =
                EchoAgent5LiveScreenStackStabilityAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedLiveScreenStackStability =
                object(liveScreenStackStabilityAcceptanceSmoke.get("accepted"));
        boolean liveScreenStackStabilityAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveScreenStackStabilityAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveScreenStackStabilityAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveScreenStackStabilityAcceptanceSmoke".equals(
                        liveScreenStackStabilityAcceptanceSmoke.get("liveScreenStackStabilityAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveScreenStackStability.get("accepted"))
                        && "live_screen_stack_stability:accepted:10-surfaces:no-crash".equals(
                        acceptedLiveScreenStackStability.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveScreenStackStability.get("stackAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveScreenStackStability.get("lifecycleAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveScreenStackStability.get("interactionAccepted"))
                        && "MAIN_MENU".equals(acceptedLiveScreenStackStability.get("finalCurrentMode"))
                        && Integer.valueOf(1).equals(acceptedLiveScreenStackStability.get("finalStackSize"))
                        && "LENS".equals(acceptedLiveScreenStackStability.get("resumeMode"))
                        && Integer.valueOf(10).equals(acceptedLiveScreenStackStability.get("interactionStepCount"))
                        && Boolean.FALSE.equals(object(
                        liveScreenStackStabilityAcceptanceSmoke.get("rejectedNoStack")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveScreenStackStabilityAcceptanceSmoke.get("rejectedNoLifecycle")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveScreenStackStabilityAcceptanceSmoke.get("rejectedNoInteraction")).get("accepted"));
        Map<String, Object> liveVisualFrameAcceptanceSmoke =
                EchoAgent5LiveVisualFrameAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedLiveVisualFrame =
                object(liveVisualFrameAcceptanceSmoke.get("accepted"));
        boolean liveVisualFrameAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveVisualFrameAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveVisualFrameAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveVisualFrameAcceptanceSmoke".equals(
                        liveVisualFrameAcceptanceSmoke.get("liveVisualFrameAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveVisualFrame.get("accepted"))
                        && "live_visual_frame:accepted:theme/render/camera/hud".equals(
                        acceptedLiveVisualFrame.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveVisualFrame.get("themeAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveVisualFrame.get("layoutAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveVisualFrame.get("cameraAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveVisualFrame.get("hudAccepted"))
                        && EchoAgent5UiReference.SETTINGS_THEME.equals(acceptedLiveVisualFrame.get("themeId"))
                        && Integer.valueOf(620).equals(acceptedLiveVisualFrame.get("desktopPanelW"))
                        && Integer.valueOf(300).equals(acceptedLiveVisualFrame.get("compactPanelW"))
                        && "over_shoulder".equals(acceptedLiveVisualFrame.get("cameraMode"))
                        && dataSources.cinematicValues().get("cue").equals(
                        acceptedLiveVisualFrame.get("cinematicCue"))
                        && EchoAgent5UiReference.HUD_LAYER.equals(acceptedLiveVisualFrame.get("overlayLayerId"))
                        && Boolean.FALSE.equals(object(
                        liveVisualFrameAcceptanceSmoke.get("rejectedNoTheme")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveVisualFrameAcceptanceSmoke.get("rejectedNoLayout")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveVisualFrameAcceptanceSmoke.get("rejectedNoCamera")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveVisualFrameAcceptanceSmoke.get("rejectedNoHud")).get("accepted"));
        Map<String, Object> liveModuleSurfaceCatalogAcceptanceSmoke =
                EchoAgent5LiveModuleSurfaceCatalogAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedLiveModuleSurfaceCatalog =
                object(liveModuleSurfaceCatalogAcceptanceSmoke.get("accepted"));
        boolean liveModuleSurfaceCatalogAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveModuleSurfaceCatalogAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveModuleSurfaceCatalogAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveModuleSurfaceCatalogAcceptanceSmoke".equals(
                        liveModuleSurfaceCatalogAcceptanceSmoke.get("liveModuleSurfaceCatalogAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLiveModuleSurfaceCatalog.get("accepted"))
                        && "live_module_surface_catalog:accepted:11-surfaces".equals(
                        acceptedLiveModuleSurfaceCatalog.get("effect"))
                        && Integer.valueOf(11).equals(acceptedLiveModuleSurfaceCatalog.get("surfaceCount"))
                        && Boolean.TRUE.equals(acceptedLiveModuleSurfaceCatalog.get("terminalAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveModuleSurfaceCatalog.get("indexAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveModuleSurfaceCatalog.get("lensAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveModuleSurfaceCatalog.get("holomapAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveModuleSurfaceCatalog.get("wikiAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveModuleSurfaceCatalog.get("missionAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveModuleSurfaceCatalog.get("settingsAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveModuleSurfaceCatalog.get("pauseAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveModuleSurfaceCatalog.get("recoveryAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveModuleSurfaceCatalog.get("mainMenuAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveModuleSurfaceCatalog.get("hudAccepted"))
                        && Boolean.FALSE.equals(object(
                        liveModuleSurfaceCatalogAcceptanceSmoke.get("rejectedMissingHud")).get("accepted"));
        Map<String, Object> liveRenderCallbackAcceptanceSmoke = EchoAgent5LiveRenderCallbackAcceptance.smoke();
        Map<String, Object> acceptedLiveRenderCallback = object(liveRenderCallbackAcceptanceSmoke.get("accepted"));
        boolean liveRenderCallbackAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveRenderCallbackAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveRenderCallbackAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveRenderCallbackAcceptance".equals(
                        liveRenderCallbackAcceptanceSmoke.get("liveRenderCallbackAcceptanceClass"))
                        && Boolean.TRUE.equals(acceptedLiveRenderCallback.get("accepted"))
                        && "live_render_callback:accepted:TERMINAL".equals(acceptedLiveRenderCallback.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveRenderCallback.get("callbackExecuted"))
                        && Integer.valueOf(1).equals(acceptedLiveRenderCallback.get("callbackCount"))
                        && Integer.valueOf(5).equals(acceptedLiveRenderCallback.get("lineCount"))
                        && Integer.valueOf(1280).equals(acceptedLiveRenderCallback.get("width"))
                        && Integer.valueOf(720).equals(acceptedLiveRenderCallback.get("height"))
                        && Boolean.FALSE.equals(object(
                        liveRenderCallbackAcceptanceSmoke.get("rejectedNoSurface")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveRenderCallbackAcceptanceSmoke.get("rejectedNoCallback")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveRenderCallbackAcceptanceSmoke.get("rejectedWrongMode")).get("accepted"));
        Map<String, Object> liveScreenOwnershipAcceptanceSmoke = EchoAgent5LiveScreenOwnershipAcceptance.smoke();
        Map<String, Object> acceptedLiveScreenOwnership = object(liveScreenOwnershipAcceptanceSmoke.get("accepted"));
        boolean liveScreenOwnershipAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveScreenOwnershipAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveScreenOwnershipAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveScreenOwnershipAcceptance".equals(
                        liveScreenOwnershipAcceptanceSmoke.get("liveScreenOwnershipAcceptanceClass"))
                        && Boolean.TRUE.equals(acceptedLiveScreenOwnership.get("accepted"))
                        && "live_screen_ownership:accepted:TERMINAL".equals(acceptedLiveScreenOwnership.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveScreenOwnership.get("surfaceAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveScreenOwnership.get("currentScreenIsGeneratedInstance"))
                        && "TERMINAL".equals(acceptedLiveScreenOwnership.get("currentMode"))
                        && "TERMINAL".equals(acceptedLiveScreenOwnership.get("expectedMode"))
                        && Boolean.FALSE.equals(object(
                        liveScreenOwnershipAcceptanceSmoke.get("rejectedNoSurface")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveScreenOwnershipAcceptanceSmoke.get("rejectedWrongInstance")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveScreenOwnershipAcceptanceSmoke.get("rejectedWrongMode")).get("accepted"));
        Map<String, Object> livePhysicalPollLoopAcceptanceSmoke = EchoAgent5LivePhysicalPollLoopAcceptance.smoke();
        Map<String, Object> acceptedLivePhysicalPollLoop = object(livePhysicalPollLoopAcceptanceSmoke.get("accepted"));
        boolean livePhysicalPollLoopAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(livePhysicalPollLoopAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(livePhysicalPollLoopAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LivePhysicalPollLoopAcceptance".equals(
                        livePhysicalPollLoopAcceptanceSmoke.get("livePhysicalPollLoopAcceptanceClass"))
                        && Boolean.TRUE.equals(acceptedLivePhysicalPollLoop.get("accepted"))
                        && "live_physical_poll_loop:accepted:3".equals(acceptedLivePhysicalPollLoop.get("effect"))
                        && Boolean.TRUE.equals(acceptedLivePhysicalPollLoop.get("windowHandlePresent"))
                        && Boolean.TRUE.equals(acceptedLivePhysicalPollLoop.get("focusChecked"))
                        && Integer.valueOf(3).equals(acceptedLivePhysicalPollLoop.get("pollIterations"))
                        && Integer.valueOf(51).equals(acceptedLivePhysicalPollLoop.get("keySamples"))
                        && Integer.valueOf(17).equals(acceptedLivePhysicalPollLoop.get("hotkeyCount"))
                        && Boolean.FALSE.equals(object(
                        livePhysicalPollLoopAcceptanceSmoke.get("rejectedNoWindow")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        livePhysicalPollLoopAcceptanceSmoke.get("rejectedNoFocusCheck")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        livePhysicalPollLoopAcceptanceSmoke.get("rejectedTooFewIterations")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        livePhysicalPollLoopAcceptanceSmoke.get("rejectedTooFewSamples")).get("accepted"));
        Map<String, Object> livePhysicalEventTranscriptAcceptanceSmoke =
                EchoAgent5LivePhysicalEventTranscriptAcceptance.smoke();
        Map<String, Object> acceptedLivePhysicalEventTranscript =
                object(livePhysicalEventTranscriptAcceptanceSmoke.get("accepted"));
        boolean livePhysicalEventTranscriptAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(livePhysicalEventTranscriptAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(livePhysicalEventTranscriptAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LivePhysicalEventTranscriptAcceptance".equals(
                        livePhysicalEventTranscriptAcceptanceSmoke.get(
                                "livePhysicalEventTranscriptAcceptanceClass"))
                        && Boolean.TRUE.equals(acceptedLivePhysicalEventTranscript.get("accepted"))
                        && "live_physical_event_transcript:accepted:17".equals(
                        acceptedLivePhysicalEventTranscript.get("effect"))
                        && Integer.valueOf(17).equals(acceptedLivePhysicalEventTranscript.get("eventCount"))
                        && Boolean.TRUE.equals(acceptedLivePhysicalEventTranscript.get("sequenceOrdered"))
                        && Boolean.TRUE.equals(acceptedLivePhysicalEventTranscript.get("pollMetricsPresent"))
                        && strings(acceptedLivePhysicalEventTranscript, "observedKeys").equals(List.of(
                        "M",
                        "G",
                        "R",
                        "U",
                        "B",
                        "LEFT_ALT",
                        "J",
                        "K",
                        "RIGHT_BRACKET",
                        "LEFT_BRACKET",
                        "BACKSLASH",
                        "N",
                        "ESCAPE",
                        "X",
                        "C",
                        "Y",
                        "Z"
                ))
                        && Boolean.FALSE.equals(object(
                        livePhysicalEventTranscriptAcceptanceSmoke.get("rejectedMissingSequence")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        livePhysicalEventTranscriptAcceptanceSmoke.get("rejectedUnordered")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        livePhysicalEventTranscriptAcceptanceSmoke.get("rejectedNoPollMetrics")).get("accepted"));
        Map<String, Object> livePhysicalRouteEffectTranscriptAcceptanceSmoke =
                EchoAgent5LivePhysicalRouteEffectTranscriptAcceptance.smoke();
        Map<String, Object> acceptedLivePhysicalRouteEffectTranscript =
                object(livePhysicalRouteEffectTranscriptAcceptanceSmoke.get("accepted"));
        boolean livePhysicalRouteEffectTranscriptAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(livePhysicalRouteEffectTranscriptAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(livePhysicalRouteEffectTranscriptAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LivePhysicalRouteEffectTranscriptAcceptance".equals(
                        livePhysicalRouteEffectTranscriptAcceptanceSmoke.get(
                                "livePhysicalRouteEffectTranscriptAcceptanceClass"))
                        && Boolean.TRUE.equals(acceptedLivePhysicalRouteEffectTranscript.get("accepted"))
                        && "live_physical_route_effect_transcript:accepted:17".equals(
                        acceptedLivePhysicalRouteEffectTranscript.get("effect"))
                        && Integer.valueOf(17).equals(acceptedLivePhysicalRouteEffectTranscript.get("eventCount"))
                        && strings(acceptedLivePhysicalRouteEffectTranscript, "observedKeys").equals(List.of(
                        "M",
                        "G",
                        "R",
                        "U",
                        "B",
                        "LEFT_ALT",
                        "J",
                        "K",
                        "RIGHT_BRACKET",
                        "LEFT_BRACKET",
                        "BACKSLASH",
                        "N",
                        "ESCAPE",
                        "X",
                        "C",
                        "Y",
                        "Z"
                ))
                        && Boolean.FALSE.equals(object(
                        livePhysicalRouteEffectTranscriptAcceptanceSmoke.get("rejectedNoSurfaceEffect"))
                        .get("accepted"))
                        && Boolean.FALSE.equals(object(
                        livePhysicalRouteEffectTranscriptAcceptanceSmoke.get("rejectedNoRouteEffect")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        livePhysicalRouteEffectTranscriptAcceptanceSmoke.get("rejectedNoSampleMetrics"))
                        .get("accepted"));
        Map<String, Object> liveRouteBoundTextCommandAcceptanceSmoke =
                EchoAgent5LiveRouteBoundTextCommandAcceptance.smoke();
        Map<String, Object> acceptedLiveRouteBoundTextCommand =
                object(liveRouteBoundTextCommandAcceptanceSmoke.get("accepted"));
        boolean liveRouteBoundTextCommandAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveRouteBoundTextCommandAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveRouteBoundTextCommandAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveRouteBoundTextCommandAcceptance".equals(
                        liveRouteBoundTextCommandAcceptanceSmoke.get(
                                "liveRouteBoundTextCommandAcceptanceClass"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundTextCommand.get("accepted"))
                        && "live_route_bound_text_command:accepted:terminal+index".equals(
                        acceptedLiveRouteBoundTextCommand.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundTextCommand.get("terminalAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundTextCommand.get("indexAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundTextCommand.get("routeBound"))
                        && strings(acceptedLiveRouteBoundTextCommand, "observedKeys").containsAll(List.of("M", "G"))
                        && Boolean.FALSE.equals(object(
                        liveRouteBoundTextCommandAcceptanceSmoke.get("rejectedNoTerminal")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveRouteBoundTextCommandAcceptanceSmoke.get("rejectedNoIndex")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveRouteBoundTextCommandAcceptanceSmoke.get("rejectedNoRoute")).get("accepted"));
        Map<String, Object> liveRouteBoundLensScanAcceptanceSmoke =
                EchoAgent5LiveRouteBoundLensScanAcceptance.smoke();
        Map<String, Object> acceptedLiveRouteBoundLensScan =
                object(liveRouteBoundLensScanAcceptanceSmoke.get("accepted"));
        boolean liveRouteBoundLensScanAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveRouteBoundLensScanAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveRouteBoundLensScanAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveRouteBoundLensScanAcceptance".equals(
                        liveRouteBoundLensScanAcceptanceSmoke.get(
                                "liveRouteBoundLensScanAcceptanceClass"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundLensScan.get("accepted"))
                        && "live_route_bound_lens_scan:accepted:LEFT_ALT->LENS".equals(
                        acceptedLiveRouteBoundLensScan.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundLensScan.get("lensAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundLensScan.get("routeBound"))
                        && dataSources.lensTarget().equals(acceptedLiveRouteBoundLensScan.get("target"))
                        && dataSources.lensResult().equals(acceptedLiveRouteBoundLensScan.get("result"))
                        && strings(acceptedLiveRouteBoundLensScan, "observedKeys").contains("LEFT_ALT")
                        && Boolean.FALSE.equals(object(
                        liveRouteBoundLensScanAcceptanceSmoke.get("rejectedNoLens")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveRouteBoundLensScanAcceptanceSmoke.get("rejectedNoRoute")).get("accepted"));
        Map<String, Object> liveRouteBoundHudUpdateAcceptanceSmoke =
                EchoAgent5LiveRouteBoundHudUpdateAcceptance.smoke();
        Map<String, Object> acceptedLiveRouteBoundHudUpdate =
                object(liveRouteBoundHudUpdateAcceptanceSmoke.get("accepted"));
        boolean liveRouteBoundHudUpdateAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveRouteBoundHudUpdateAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveRouteBoundHudUpdateAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveRouteBoundHudUpdateAcceptance".equals(
                        liveRouteBoundHudUpdateAcceptanceSmoke.get(
                                "liveRouteBoundHudUpdateAcceptanceClass"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundHudUpdate.get("accepted"))
                        && "live_route_bound_hud_update:accepted:hud_update".equals(
                        acceptedLiveRouteBoundHudUpdate.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundHudUpdate.get("hudAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundHudUpdate.get("routeBound"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundHudUpdate.get("overlayRendered"))
                        && Integer.valueOf(85).equals(acceptedLiveRouteBoundHudUpdate.get("hudHealth"))
                        && "over_shoulder".equals(acceptedLiveRouteBoundHudUpdate.get("cameraMode"))
                        && dataSources.cinematicValues().get("cue").equals(
                        acceptedLiveRouteBoundHudUpdate.get("cinematicCue"))
                        && "HUD_UPDATE".equals(acceptedLiveRouteBoundHudUpdate.get("key"))
                        && Boolean.FALSE.equals(object(
                        liveRouteBoundHudUpdateAcceptanceSmoke.get("rejectedNoHudUpdate")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveRouteBoundHudUpdateAcceptanceSmoke.get("rejectedNoRoute")).get("accepted"));
        Map<String, Object> liveRouteBoundHoloMapWikiAcceptanceSmoke =
                EchoAgent5LiveRouteBoundHoloMapWikiAcceptance.smoke();
        Map<String, Object> acceptedLiveRouteBoundHoloMapWiki =
                object(liveRouteBoundHoloMapWikiAcceptanceSmoke.get("accepted"));
        boolean liveRouteBoundHoloMapWikiAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveRouteBoundHoloMapWikiAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveRouteBoundHoloMapWikiAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveRouteBoundHoloMapWikiAcceptance".equals(
                        liveRouteBoundHoloMapWikiAcceptanceSmoke.get(
                                "liveRouteBoundHoloMapWikiAcceptanceClass"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundHoloMapWiki.get("accepted"))
                        && "live_route_bound_holomap_wiki:accepted:J/direct".equals(
                        acceptedLiveRouteBoundHoloMapWiki.get("effect"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundHoloMapWiki.get("holomapAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundHoloMapWiki.get("wikiAccepted"))
                        && Boolean.TRUE.equals(acceptedLiveRouteBoundHoloMapWiki.get("routeBound"))
                        && "HOLOMAP".equals(acceptedLiveRouteBoundHoloMapWiki.get("holomapSurface"))
                        && "WIKI".equals(acceptedLiveRouteBoundHoloMapWiki.get("wikiSurface"))
                        && "J".equals(acceptedLiveRouteBoundHoloMapWiki.get("holomapKey"))
                        && "DIRECT".equals(acceptedLiveRouteBoundHoloMapWiki.get("wikiKey"))
                        && dataSources.holomapValues().get("layer").equals(
                        acceptedLiveRouteBoundHoloMapWiki.get("layer"))
                        && dataSources.holomapValues().get("marker").equals(
                        acceptedLiveRouteBoundHoloMapWiki.get("marker"))
                        && dataSources.wikiValues().get("guide").equals(
                        acceptedLiveRouteBoundHoloMapWiki.get("guide"))
                        && dataSources.wikiValues().get("page").equals(
                        acceptedLiveRouteBoundHoloMapWiki.get("page"))
                        && dataSources.wikiValues().get("link").equals(
                        acceptedLiveRouteBoundHoloMapWiki.get("link"))
                        && strings(acceptedLiveRouteBoundHoloMapWiki, "observedKeys").contains("J")
                        && Boolean.FALSE.equals(object(
                        liveRouteBoundHoloMapWikiAcceptanceSmoke.get("rejectedNoHoloMap")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveRouteBoundHoloMapWikiAcceptanceSmoke.get("rejectedNoWiki")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveRouteBoundHoloMapWikiAcceptanceSmoke.get("rejectedNoRoute")).get("accepted"));
        Map<String, Object> generatedScreenHostSmoke = EchoAgent5GeneratedScreenHostSmoke.capture();
        boolean generatedScreenHostSmokeSatisfied =
                Boolean.TRUE.equals(generatedScreenHostSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(generatedScreenHostSmoke.get("passed"))
                        && "EchoAgent5GeneratedScreenHostSmoke".equals(
                        generatedScreenHostSmoke.get("generatedScreenHostSmokeClass"))
                        && "EchoAgent5GeneratedScreenHost".equals(generatedScreenHostSmoke.get("generatedHostClass"))
                        && "dev.echo.nativeplatform.generated.EchoNativeDashboardScreen".equals(
                        generatedScreenHostSmoke.get("nativeReferenceClass"))
                        && "generated_screen_host:accepted:11-surfaces".equals(
                        generatedScreenHostSmoke.get("effect"))
                        && strings(generatedScreenHostSmoke, "renderedModes").containsAll(List.of(
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
                ))
                        && strings(generatedScreenHostSmoke, "renderedTitles").contains("ECHO NATIVE // HUD")
                        && strings(generatedScreenHostSmoke, "renderedTitles").contains("ECHO NATIVE // TERMINAL")
                        && strings(generatedScreenHostSmoke, "checks").stream()
                        .allMatch(check -> check.endsWith("=PASS"));
        EchoUiTheme theme = new EchoUiTheme(
                "ashfall-agent5",
                "Ashfall Agent 5",
                "#67e8f9",
                "#061014",
                "#d8fbff",
                "#facc15",
                "ECHO Mono",
                "compact",
                Map.of("terminal.prompt", dataSources.terminalPrompt())
        );
        EchoAgent5ScreenCoreContract screenCoreContract = EchoAgent5ScreenCoreContract.runtime();
        EchoTheme screenCoreTheme = new EchoTheme(EchoAgent5UiReference.SETTINGS_THEME, theme);
        EchoInputAction terminalStatusAction = new EchoInputAction(
                "agent5:terminal_status",
                "Enter",
                dataSources.terminalCommand()
        );
        EchoTextInput indexInput = new EchoTextInput("agent5:index_query", "").withValue(dataSources.indexQuery());
        EchoButton recoveryButton = new EchoButton(
                "agent5:recover",
                "Recover",
                EchoAgent5UiReference.RECOVERY_ACTION
        );

        EchoStaticScreen mainMenu = new EchoStaticScreen(
                EchoAgent5UiReference.MAIN_MENU_SCREEN,
                "ECHO Ashfall",
                dataSources.mainMenuOptions(),
                "main-menu:continue"
        );
        EchoScreenStack screenCoreStack = new EchoScreenStack();
        screenCoreStack.push(mainMenu);
        EchoScreenRoute mainMenuRoute = new EchoScreenRoute(
                mainMenu.id(),
                "main-menu:continue",
                mainMenu.focusPath()
        );
        EchoUiRuntimeResult ui = new EchoUiRuntime().boot(services, mainMenu, theme);
        visited.add(currentScreenId(ui, diagnostics));
        boolean customMainMenuOpened = EchoAgent5UiReference.MAIN_MENU_SCREEN.equals(currentScreenId(ui, diagnostics));

        EchoTerminalShell terminalShell = new EchoTerminalShell();
        terminalShell.promptPrefix(theme.tokens().get("terminal.prompt"));
        EchoTerminalScreen terminalScreen = new EchoTerminalScreen(
                EchoAgent5UiReference.TERMINAL_SCREEN,
                "Terminal",
                terminalShell
        );
        ui.screenStack().push(terminalScreen);
        visited.add(currentScreenId(ui, diagnostics));
        boolean terminalOpened = EchoAgent5UiReference.TERMINAL_SCREEN.equals(currentScreenId(ui, diagnostics));
        EchoUiInputResult terminalResult = ui.dispatch(terminalStatusAction.event(10));
        boolean terminalCommandExecuted = terminalResult.handled()
                && terminalShell.outputLines().stream().anyMatch(line -> line.contains(dataSources.terminalReadyLine()));
        EchoTerminalBuffer terminalBuffer = new EchoTerminalBuffer(terminalShell.outputLines());

        SearchScreen indexScreen = new SearchScreen(
                EchoAgent5UiReference.INDEX_SCREEN,
                "Index",
                dataSources.indexQuery(),
                dataSources.indexResult()
        );
        ui.screenStack().push(indexScreen);
        visited.add(currentScreenId(ui, diagnostics));
        boolean indexOpened = EchoAgent5UiReference.INDEX_SCREEN.equals(currentScreenId(ui, diagnostics));
        EchoUiInputResult indexResult = ui.dispatch(EchoUiInputEvent.command(20, indexInput.value()));
        boolean indexSearchExecuted = indexResult.handled() && indexScreen.lastResult().equals(dataSources.indexResult());

        ScanScreen lensScreen = new ScanScreen(
                EchoAgent5UiReference.LENS_SCREEN,
                dataSources.lensTarget(),
                dataSources.lensResult()
        );
        ui.screenStack().push(lensScreen);
        visited.add(currentScreenId(ui, diagnostics));
        EchoUiInputResult lensResult = ui.dispatch(EchoUiInputEvent.command(30, "scan " + dataSources.lensTarget()));
        boolean lensScanExecuted = lensResult.handled() && lensScreen.scanned()
                && dataSources.lensResult().equals(lensScreen.scanResult());

        Map<String, Object> hudValues = dataSources.hudValues();
        EchoAgent5NotificationQueue notificationQueue = new EchoAgent5NotificationQueue();
        for (Map<String, Object> notificationSource : dataSources.notifications()) {
            notificationQueue.enqueue(
                    String.valueOf(notificationSource.get("severity")),
                    String.valueOf(notificationSource.get("message")),
                    String.valueOf(notificationSource.get("anchor"))
            );
        }
        EchoHudLayer hudLayer = new EchoHudLayer(EchoAgent5UiReference.HUD_LAYER, hudValues);
        boolean hudUpdated = hudLayer.ready();
        boolean notificationQueueUpdated = notificationQueue.data().size() == dataSources.notifications().size()
                && notificationQueue.data().stream().allMatch(entry -> Boolean.TRUE.equals(entry.get("delivered")))
                && notificationQueue.data().stream().map(entry -> String.valueOf(entry.get("message"))).toList()
                .equals(dataSources.notifications().stream()
                        .map(entry -> String.valueOf(entry.get("message")))
                        .toList())
                && notificationQueue.data().stream().map(entry -> String.valueOf(entry.get("severity"))).toList()
                .equals(dataSources.notifications().stream()
                        .map(entry -> String.valueOf(entry.get("severity")))
                        .toList())
                && notificationQueue.data().stream().map(entry -> String.valueOf(entry.get("anchor"))).toList()
                .equals(dataSources.notifications().stream()
                        .map(entry -> String.valueOf(entry.get("anchor")))
                        .toList());
        EchoNotification notification = new EchoNotification(
                "agent5-notification-contract",
                String.valueOf(dataSources.notifications().getFirst().get("severity")),
                String.valueOf(dataSources.notifications().getFirst().get("message")),
                String.valueOf(dataSources.notifications().getFirst().get("anchor")),
                notificationQueue.deliveredTo(String.valueOf(dataSources.notifications().getFirst().get("anchor")))
        );

        Map<String, Object> missionLogValues = dataSources.missionLogValues();
        EchoStaticScreen missionLogScreen = new EchoStaticScreen(
                EchoAgent5UiReference.MISSION_LOG_SCREEN,
                "Mission Log",
                List.of(
                        String.valueOf(missionLogValues.get("title")),
                        String.valueOf(missionLogValues.get("objective")),
                        String.valueOf(missionLogValues.get("status"))
                ),
                "mission-log:track:" + EchoAgent5UiReference.ACTIVE_MISSION_ID
        );
        ui.screenStack().push(missionLogScreen);
        visited.add(currentScreenId(ui, diagnostics));
        boolean missionLogOpened = EchoAgent5UiReference.MISSION_LOG_SCREEN.equals(currentScreenId(ui, diagnostics));
        boolean missionLogTrackedActiveMission = missionLogOpened
                && EchoAgent5UiReference.ACTIVE_MISSION_ID.equals(missionLogValues.get("missionId"))
                && EchoAgent5UiReference.ACTIVE_MISSION_OBJECTIVE.equals(hudValues.get("mission"));

        Map<String, Object> settingsValues = dataSources.settingsValues();
        EchoStaticScreen settingsScreen = new EchoStaticScreen(
                EchoAgent5UiReference.SETTINGS_SCREEN,
                "Settings",
                List.of(
                        "Profile: " + settingsValues.get("profile"),
                        "Theme: " + settingsValues.get("theme"),
                        "Input: " + settingsValues.get("inputMode")
                ),
                "settings:profile:" + settingsValues.get("profile")
        );
        ui.screenStack().push(settingsScreen);
        visited.add(currentScreenId(ui, diagnostics));
        boolean settingsOpened = EchoAgent5UiReference.SETTINGS_SCREEN.equals(currentScreenId(ui, diagnostics));
        boolean settingsAppliedProfile = settingsOpened
                && EchoAgent5UiReference.SETTINGS_PROFILE.equals(settingsValues.get("profile"))
                && EchoAgent5UiReference.SETTINGS_THEME.equals(settingsValues.get("theme"))
                && EchoAgent5UiReference.SETTINGS_INPUT_MODE.equals(settingsValues.get("inputMode"))
                && Boolean.TRUE.equals(settingsValues.get("subtitles"));

        EchoStaticScreen holomapScreen = new EchoStaticScreen(
                EchoAgent5UiReference.HOLOMAP_SCREEN,
                "HoloMap",
                dataSources.holomapLines(),
                "holomap:marker:" + dataSources.holomapValues().get("marker")
        );
        ui.screenStack().push(holomapScreen);
        visited.add(currentScreenId(ui, diagnostics));
        boolean holomapOpened = EchoAgent5UiReference.HOLOMAP_SCREEN.equals(currentScreenId(ui, diagnostics));

        EchoStaticScreen wikiScreen = new EchoStaticScreen(
                EchoAgent5UiReference.WIKI_SCREEN,
                "Wiki",
                dataSources.wikiLines(),
                String.valueOf(dataSources.wikiValues().get("link"))
        );
        ui.screenStack().push(wikiScreen);
        visited.add(currentScreenId(ui, diagnostics));
        boolean wikiOpened = EchoAgent5UiReference.WIKI_SCREEN.equals(currentScreenId(ui, diagnostics));

        String pausePreviousScreen = currentScreenId(ui, diagnostics);
        Map<String, Object> pauseFlowValues = dataSources.pauseFlowValues(pausePreviousScreen);
        EchoListView pauseOptions = new EchoListView(
                "agent5:pause_options",
                dataSources.pauseOptions(),
                0
        );
        EchoStaticScreen pauseFlowScreen = new EchoStaticScreen(
                EchoAgent5UiReference.PAUSE_FLOW_SCREEN,
                "Pause",
                dataSources.pauseOptions(),
                "pause:resume:" + pausePreviousScreen
        );
        ui.screenStack().push(pauseFlowScreen);
        visited.add(currentScreenId(ui, diagnostics));
        boolean pauseFlowOpened = EchoAgent5UiReference.PAUSE_FLOW_SCREEN.equals(currentScreenId(ui, diagnostics));
        ui.screenStack().pop();
        String resumedScreen = currentScreenId(ui, diagnostics);
        visited.add(resumedScreen);
        boolean pauseFlowResumedPreviousScreen = pauseFlowOpened
                && pausePreviousScreen.equals(EchoAgent5UiReference.PAUSE_RESUME_TARGET)
                && pausePreviousScreen.equals(resumedScreen);

        Map<String, Object> deathRecoveryValues = new LinkedHashMap<>(dataSources.deathRecoveryValues("WAITING"));
        RecoveryScreen deathRecoveryScreen = new RecoveryScreen(
                EchoAgent5UiReference.DEATH_RECOVERY_SCREEN,
                String.valueOf(deathRecoveryValues.get("recoveryPoint"))
        );
        ui.screenStack().push(deathRecoveryScreen);
        visited.add(currentScreenId(ui, diagnostics));
        boolean deathRecoveryOpened = EchoAgent5UiReference.DEATH_RECOVERY_SCREEN.equals(currentScreenId(ui, diagnostics));
        EchoUiInputResult deathRecoveryResult = ui.dispatch(EchoUiInputEvent.command(40, recoveryButton.action()));
        if (deathRecoveryResult.handled()) {
            deathRecoveryValues.put("status", EchoAgent5UiReference.RECOVERY_STATUS);
        }
        boolean deathRecoveryActionExecuted = deathRecoveryOpened
                && deathRecoveryScreen.recovered()
                && EchoAgent5UiReference.RECOVERY_STATUS.equals(deathRecoveryValues.get("status"));
        EchoStaticScreen signalOsScreen = new EchoStaticScreen(
                EchoAgent5UiReference.SIGNALOS_SCREEN,
                String.valueOf(dataSources.signalOsValues().get("title")),
                List.of(String.valueOf(dataSources.signalOsValues().get("summary"))),
                "signalos:terminal"
        );
        ui.screenStack().push(signalOsScreen);
        visited.add(currentScreenId(ui, diagnostics));
        EchoStaticScreen ashfallDroneScreen = new EchoStaticScreen(
                EchoAgent5UiReference.ASHFALL_DRONE_SCREEN,
                String.valueOf(dataSources.ashfallDroneValues().get("title")),
                List.of(String.valueOf(dataSources.ashfallDroneValues().get("summary"))),
                "ashfall_drone:surface"
        );
        ui.screenStack().push(ashfallDroneScreen);
        visited.add(currentScreenId(ui, diagnostics));
        EchoWidget missionWidget = new EchoWidget(
                "agent5:mission_widget",
                "mission_tracker",
                missionLogValues
        );
        Map<String, Object> screenCoreContractValues = new LinkedHashMap<>();
        screenCoreContractValues.put("primitives", screenCoreContract.primitives());
        screenCoreContractValues.put("runtimeBindings", screenCoreContract.runtimeBindings());
        screenCoreContractValues.put("mainMenuRoute", mainMenuRoute.route());
        screenCoreContractValues.put("screenStackCurrent", screenCoreStack.current().map(EchoScreen::id).orElse(""));
        screenCoreContractValues.put("theme", screenCoreTheme.id());
        screenCoreContractValues.put("terminalBufferLines", terminalBuffer.lines().size());
        screenCoreContractValues.put("pauseSelectedOption", pauseOptions.selectedRow());
        screenCoreContractValues.put("notificationDelivered", notification.delivered());
        screenCoreContractValues.put("missionWidgetKind", missionWidget.kind());
        boolean screenCoreContractSatisfied = screenCoreContract.satisfied()
                && EchoAgent5UiReference.MAIN_MENU_SCREEN.equals(screenCoreContractValues.get("screenStackCurrent"))
                && terminalBuffer.contains(dataSources.terminalReadyLine())
                && "Resume".equals(pauseOptions.selectedRow())
                && notification.delivered()
                && EchoAgent5UiReference.RECOVERY_ACTION.equals(recoveryButton.action());
        Map<String, Object> dataSourceValues = dataSources.snapshot();
        boolean dataSourcesSatisfied = terminalShell.history().contains(dataSources.terminalCommand())
                && terminalShell.outputLines().stream().anyMatch(line -> line.contains(dataSources.terminalReadyLine()))
                && indexScreen.lastResult().equals(dataSources.indexResult())
                && lensScreen.scanResult().equals(dataSources.lensResult())
                && hudValues.equals(dataSources.hudValues())
                && notificationQueueUpdated
                && missionLogValues.equals(dataSources.missionLogValues())
                && settingsValues.equals(dataSources.settingsValues())
                && pauseFlowValues.equals(dataSources.pauseFlowValues(pausePreviousScreen))
                && deathRecoveryValues.equals(dataSources.deathRecoveryValues(EchoAgent5UiReference.RECOVERY_STATUS))
                && holomapScreen.lines().containsAll(dataSources.holomapLines())
                && wikiScreen.lines().containsAll(dataSources.wikiLines())
                && object(dataSourceValues.get("camera")).equals(dataSources.cameraValues())
                && object(dataSourceValues.get("cinematic")).equals(dataSources.cinematicValues())
                && mainMenu.lines().containsAll(dataSources.mainMenuOptions());
        EchoUiContext inputContext = new EchoUiContext(ui.screenStack(), ui.modalStack(), ui.themeRuntime());
        boolean inputRoutingSatisfied = "terminal:input".equals(terminalScreen.render(inputContext).focusPath())
                && terminalResult.effects().contains("terminal-command:" + dataSources.terminalCommand())
                && "index:search".equals(indexScreen.render(inputContext).focusPath())
                && indexResult.effects().contains("index-search:" + dataSources.indexQuery())
                && "lens:scan".equals(lensScreen.render(inputContext).focusPath())
                && lensResult.effects().contains("lens-scan:" + dataSources.lensTarget())
                && deathRecoveryResult.effects().contains("death-recovery:" + EchoAgent5UiReference.RECOVERY_POINT)
                && ("pause:resume:" + pausePreviousScreen).equals(pauseFlowScreen.focusPath());
        Map<String, Object> focusManagerSmoke = EchoAgent5FocusManagerSmoke.capture(dataSources);
        boolean focusManagerSmokeSatisfied = Boolean.TRUE.equals(focusManagerSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(focusManagerSmoke.get("passed"))
                && "EchoAgent5FocusManagerSmoke".equals(focusManagerSmoke.get("focusManagerSmokeClass"))
                && strings(focusManagerSmoke, "widgetIds").containsAll(List.of(
                        "agent5:terminal",
                        "agent5:terminal-input",
                        "agent5:index",
                        "agent5:index-search",
                        "agent5:lens-scan",
                        "agent5:recovery-actions"
                ))
                && strings(focusManagerSmoke, "focusOrder").equals(List.of(
                        "terminal:input",
                        "index:search",
                        "lens:scan",
                        "recovery:recover"
                ))
                && strings(focusManagerSmoke, "ignoredReasons").containsAll(List.of("character:unfocused", "character:control"))
                && dataSources.terminalCommand().equals(focusManagerSmoke.get("terminalBuffer"))
                && dataSources.indexQuery().equals(focusManagerSmoke.get("indexBuffer"))
                && strings(focusManagerSmoke, "activationKeys").containsAll(List.of(
                        "terminalCommandExecuted",
                        "indexSearchExecuted",
                        "lensScanExecuted",
                        "recoveryActionExecuted"
                ))
                && strings(focusManagerSmoke, "renderedFocusLines").stream()
                        .anyMatch(line -> line.contains("terminal:input ready"))
                && strings(focusManagerSmoke, "renderedFocusLines").stream()
                        .anyMatch(line -> line.contains("index:search ready"))
                && strings(focusManagerSmoke, "renderedFocusLines").stream()
                        .anyMatch(line -> line.contains("lens:scan ready"))
                && strings(focusManagerSmoke, "renderedFocusLines").stream()
                        .anyMatch(line -> line.contains("recovery:recover ready"));
        Map<String, Object> initialFocusSmoke = EchoAgent5InitialFocusSmoke.capture(dataSources);
        boolean initialFocusSmokeSatisfied = Boolean.TRUE.equals(initialFocusSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(initialFocusSmoke.get("passed"))
                && "EchoAgent5InitialFocusSmoke".equals(initialFocusSmoke.get("initialFocusSmokeClass"))
                && strings(initialFocusSmoke, "focusPaths").equals(List.of(
                        "terminal:input",
                        "index:search",
                        "lens:scan",
                        "recovery:recover"
                ))
                && strings(initialFocusSmoke, "effects").equals(List.of(
                        "focus:initial:terminal",
                        "focus:initial:index",
                        "focus:initial:lens",
                        "focus:initial:recovery"
                ))
                && dataSources.terminalCommand().equals(initialFocusSmoke.get("terminalBuffer"))
                && dataSources.indexQuery().equals(initialFocusSmoke.get("indexBuffer"))
                && strings(initialFocusSmoke, "executedKeys").containsAll(List.of(
                        "lensScanExecuted",
                        "recoveryActionExecuted"
                ))
                && strings(initialFocusSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains("terminal:input ready"))
                && strings(initialFocusSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains("lens:scan ready"));
        Map<String, Object> textEditingSmoke = EchoAgent5TextEditingSmoke.capture(dataSources);
        boolean textEditingSmokeSatisfied = Boolean.TRUE.equals(textEditingSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(textEditingSmoke.get("passed"))
                && "EchoAgent5TextEditingSmoke".equals(textEditingSmoke.get("textEditingSmokeClass"))
                && dataSources.terminalCommand().equals(textEditingSmoke.get("terminalBuffer"))
                && dataSources.indexQuery().equals(textEditingSmoke.get("indexBuffer"))
                && "".equals(textEditingSmoke.get("emptyBackspaceValue"))
                && strings(textEditingSmoke, "editEffects").containsAll(List.of(
                        "terminal-character",
                        "terminal-backspace",
                        "index-character",
                        "index-backspace"
                ))
                && strings(textEditingSmoke, "activationKeys").containsAll(List.of(
                        "terminalCommandExecuted",
                        "indexSearchExecuted"
                ))
                && strings(textEditingSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains(dataSources.terminalReadyLine()))
                && strings(textEditingSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains(dataSources.indexResult()));
        Map<String, Object> mouseActivationSmoke = EchoAgent5MouseActivationSmoke.capture(dataSources);
        boolean mouseActivationSmokeSatisfied = Boolean.TRUE.equals(mouseActivationSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(mouseActivationSmoke.get("passed"))
                && "EchoAgent5MouseActivationSmoke".equals(mouseActivationSmoke.get("mouseActivationSmokeClass"))
                && strings(mouseActivationSmoke, "focusPaths").containsAll(List.of(
                        "terminal:input",
                        "index:search",
                        "lens:scan",
                        "recovery:recover"
                ))
                && strings(mouseActivationSmoke, "clickEffects").containsAll(List.of(
                        "mouse:focus:terminal",
                        "mouse:activate:terminal",
                        "mouse:activate:index",
                        "mouse:activate:lens",
                        "mouse:activate:recovery"
                ))
                && strings(mouseActivationSmoke, "executedKeys").containsAll(List.of(
                        "terminalCommandExecuted",
                        "indexSearchExecuted",
                        "lensScanExecuted",
                        "recoveryActionExecuted"
                ))
                && strings(mouseActivationSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains(dataSources.terminalReadyLine()))
                && strings(mouseActivationSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains(dataSources.indexResult()))
                && strings(mouseActivationSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains("Scan: " + dataSources.lensResult()))
                && strings(mouseActivationSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains("Status: " + EchoAgent5UiReference.RECOVERY_STATUS));
        Map<String, Object> listNavigationSmoke = EchoAgent5ListNavigationSmoke.capture(dataSources);
        boolean listNavigationSmokeSatisfied = Boolean.TRUE.equals(listNavigationSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(listNavigationSmoke.get("passed"))
                && "EchoAgent5ListNavigationSmoke".equals(listNavigationSmoke.get("listNavigationSmokeClass"))
                && strings(listNavigationSmoke, "selectedOptions").equals(List.of(
                        "New Ashfall Run",
                        "Settings",
                        "Theme",
                        "Input Mode",
                        "Quit to Main Menu"
                ))
                && strings(listNavigationSmoke, "effects").containsAll(List.of(
                        "list:main_menu:down",
                        "list:settings:down",
                        "list:pause:up"
                ))
                && strings(listNavigationSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains("Selected: Settings"))
                && strings(listNavigationSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains("Selected: Input Mode"))
                && strings(listNavigationSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains("Selected: Quit to Main Menu"));
        Map<String, Object> notificationDismissSmoke = EchoAgent5NotificationDismissSmoke.capture(
                "ashfall",
                12,
                3,
                2,
                1,
                dataSources
        );
        boolean notificationDismissSmokeSatisfied = Boolean.TRUE.equals(notificationDismissSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(notificationDismissSmoke.get("passed"))
                && "EchoAgent5NotificationDismissSmoke".equals(notificationDismissSmoke.get("notificationDismissSmokeClass"))
                && "agent5-notification-1".equals(notificationDismissSmoke.get("dismissedId"))
                && dataSources.notifications().get(0).get("message").equals(
                notificationDismissSmoke.get("dismissedMessage"))
                && "notification:dismiss-oldest".equals(notificationDismissSmoke.get("effect"))
                && strings(notificationDismissSmoke, "remainingMessages").equals(
                List.of(String.valueOf(dataSources.notifications().get(1).get("message"))))
                && strings(notificationDismissSmoke, "afterHeaderLines").stream()
                        .anyMatch(line -> line.contains(
                                "Notifications: " + dataSources.notifications().get(1).get("message")));
        Map<String, Object> settingsAdjustmentSmoke = EchoAgent5SettingsAdjustmentSmoke.capture(dataSources);
        boolean settingsAdjustmentSmokeSatisfied = Boolean.TRUE.equals(settingsAdjustmentSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(settingsAdjustmentSmoke.get("passed"))
                && "EchoAgent5SettingsAdjustmentSmoke".equals(settingsAdjustmentSmoke.get("settingsAdjustmentSmokeClass"))
                && strings(settingsAdjustmentSmoke, "selectedOptions").equals(List.of("HUD Scale", "Subtitles"))
                && strings(settingsAdjustmentSmoke, "effects").equals(List.of("settings:hud_scale", "settings:subtitles"))
                && Double.valueOf(1.25D).equals(settingsAdjustmentSmoke.get("settingsHudScale"))
                && Boolean.FALSE.equals(settingsAdjustmentSmoke.get("settingsSubtitles"))
                && strings(settingsAdjustmentSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains("HUD scale: 1.25    Subtitles: disabled"));
        Map<String, Object> settingsEndToEndAcceptanceSmoke =
                EchoAgent5SettingsEndToEndAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedSettingsEndToEnd = object(settingsEndToEndAcceptanceSmoke.get("accepted"));
        boolean settingsEndToEndAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(settingsEndToEndAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(settingsEndToEndAcceptanceSmoke.get("passed"))
                        && "EchoAgent5SettingsEndToEndAcceptanceSmoke".equals(
                        settingsEndToEndAcceptanceSmoke.get("settingsEndToEndAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedSettingsEndToEnd.get("accepted"))
                        && "settings_end_to_end:SETTINGS_ACTION->SETTINGS:ashfall-accessible:subtitles_off".equals(
                        acceptedSettingsEndToEnd.get("effect"))
                        && Boolean.TRUE.equals(acceptedSettingsEndToEnd.get("physicalInputAccepted"))
                        && Boolean.TRUE.equals(acceptedSettingsEndToEnd.get("renderAccepted"))
                        && Boolean.TRUE.equals(acceptedSettingsEndToEnd.get("interactionAccepted"))
                        && Boolean.TRUE.equals(acceptedSettingsEndToEnd.get("adjustmentAccepted"))
                        && Boolean.TRUE.equals(acceptedSettingsEndToEnd.get("settingsRendered"))
                        && Boolean.FALSE.equals(object(
                        settingsEndToEndAcceptanceSmoke.get("rejectedNoInput")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        settingsEndToEndAcceptanceSmoke.get("rejectedNoRender")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        settingsEndToEndAcceptanceSmoke.get("rejectedNoInteraction")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        settingsEndToEndAcceptanceSmoke.get("rejectedNoAdjustment")).get("accepted"));
        Map<String, Object> pauseOptionActivationSmoke = EchoAgent5PauseOptionActivationSmoke.capture(dataSources);
        boolean pauseOptionActivationSmokeSatisfied = Boolean.TRUE.equals(pauseOptionActivationSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(pauseOptionActivationSmoke.get("passed"))
                && "EchoAgent5PauseOptionActivationSmoke".equals(pauseOptionActivationSmoke.get("pauseOptionActivationSmokeClass"))
                && strings(pauseOptionActivationSmoke, "selectedOptions").equals(List.of("Resume", "Settings", "Quit to Main Menu"))
                && strings(pauseOptionActivationSmoke, "destinations").equals(List.of("LENS", "SETTINGS", "MAIN_MENU"))
                && strings(pauseOptionActivationSmoke, "effects").equals(List.of("pause:resume", "pause:settings", "pause:main_menu"))
                && strings(pauseOptionActivationSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains("Selected: Settings"));
        Map<String, Object> pauseEndToEndAcceptanceSmoke =
                EchoAgent5PauseEndToEndAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedPauseEndToEnd = object(pauseEndToEndAcceptanceSmoke.get("accepted"));
        boolean pauseEndToEndAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(pauseEndToEndAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(pauseEndToEndAcceptanceSmoke.get("passed"))
                        && "EchoAgent5PauseEndToEndAcceptanceSmoke".equals(
                        pauseEndToEndAcceptanceSmoke.get("pauseEndToEndAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedPauseEndToEnd.get("accepted"))
                        && "pause_end_to_end:ESCAPE->PAUSE:LENS".equals(acceptedPauseEndToEnd.get("effect"))
                        && Boolean.TRUE.equals(acceptedPauseEndToEnd.get("physicalInputAccepted"))
                        && Boolean.TRUE.equals(acceptedPauseEndToEnd.get("renderAccepted"))
                        && Boolean.TRUE.equals(acceptedPauseEndToEnd.get("interactionAccepted"))
                        && Boolean.TRUE.equals(acceptedPauseEndToEnd.get("optionAccepted"))
                        && Boolean.TRUE.equals(acceptedPauseEndToEnd.get("pauseRendered"))
                        && Boolean.FALSE.equals(object(
                        pauseEndToEndAcceptanceSmoke.get("rejectedNoInput")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        pauseEndToEndAcceptanceSmoke.get("rejectedNoRender")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        pauseEndToEndAcceptanceSmoke.get("rejectedNoInteraction")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        pauseEndToEndAcceptanceSmoke.get("rejectedNoOption")).get("accepted"));
        Map<String, Object> recoveryEndToEndAcceptanceSmoke =
                EchoAgent5RecoveryEndToEndAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedRecoveryEndToEnd = object(recoveryEndToEndAcceptanceSmoke.get("accepted"));
        boolean recoveryEndToEndAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(recoveryEndToEndAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(recoveryEndToEndAcceptanceSmoke.get("passed"))
                        && "EchoAgent5RecoveryEndToEndAcceptanceSmoke".equals(
                        recoveryEndToEndAcceptanceSmoke.get("recoveryEndToEndAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedRecoveryEndToEnd.get("accepted"))
                        && "recovery_end_to_end:RECOVERY_ACTION->RECOVERY:RECOVERED".equals(
                        acceptedRecoveryEndToEnd.get("effect"))
                        && Boolean.TRUE.equals(acceptedRecoveryEndToEnd.get("physicalInputAccepted"))
                        && Boolean.TRUE.equals(acceptedRecoveryEndToEnd.get("renderAccepted"))
                        && Boolean.TRUE.equals(acceptedRecoveryEndToEnd.get("interactionAccepted"))
                        && Boolean.TRUE.equals(acceptedRecoveryEndToEnd.get("recoveryRendered"))
                        && Boolean.FALSE.equals(object(
                        recoveryEndToEndAcceptanceSmoke.get("rejectedNoInput")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        recoveryEndToEndAcceptanceSmoke.get("rejectedNoRender")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        recoveryEndToEndAcceptanceSmoke.get("rejectedNoInteraction")).get("accepted"));
        Map<String, Object> missionLogUpdateSmoke = EchoAgent5MissionLogUpdateSmoke.capture(dataSources);
        boolean missionLogUpdateSmokeSatisfied = Boolean.TRUE.equals(missionLogUpdateSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(missionLogUpdateSmoke.get("passed"))
                && "EchoAgent5MissionLogUpdateSmoke".equals(missionLogUpdateSmoke.get("missionLogUpdateSmokeClass"))
                && EchoAgent5UiReference.ACTIVE_MISSION_ID.equals(missionLogUpdateSmoke.get("missionId"))
                && EchoAgent5UiReference.ACTIVE_MISSION_UPDATED_STATUS.equals(missionLogUpdateSmoke.get("missionStatus"))
                && Double.valueOf(0.5D).equals(missionLogUpdateSmoke.get("missionProgress"))
                && ("mission:update:" + EchoAgent5UiReference.ACTIVE_MISSION_ID).equals(missionLogUpdateSmoke.get("effect"))
                && strings(missionLogUpdateSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains("Status: UPDATED    Progress: 50%"))
                && strings(missionLogUpdateSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains("Update: " + EchoAgent5UiReference.ACTIVE_MISSION_UPDATE_LINE));
        Map<String, Object> missionLogEndToEndAcceptanceSmoke =
                EchoAgent5MissionLogEndToEndAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedMissionLogEndToEnd = object(missionLogEndToEndAcceptanceSmoke.get("accepted"));
        boolean missionLogEndToEndAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(missionLogEndToEndAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(missionLogEndToEndAcceptanceSmoke.get("passed"))
                        && "EchoAgent5MissionLogEndToEndAcceptanceSmoke".equals(
                        missionLogEndToEndAcceptanceSmoke.get("missionLogEndToEndAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedMissionLogEndToEnd.get("accepted"))
                        && ("mission_log_end_to_end:MISSION_ACTION->MISSION_LOG:"
                        + EchoAgent5UiReference.ACTIVE_MISSION_ID + ":UPDATED").equals(
                        acceptedMissionLogEndToEnd.get("effect"))
                        && Boolean.TRUE.equals(acceptedMissionLogEndToEnd.get("physicalInputAccepted"))
                        && Boolean.TRUE.equals(acceptedMissionLogEndToEnd.get("renderAccepted"))
                        && Boolean.TRUE.equals(acceptedMissionLogEndToEnd.get("interactionAccepted"))
                        && Boolean.TRUE.equals(acceptedMissionLogEndToEnd.get("updateAccepted"))
                        && Boolean.TRUE.equals(acceptedMissionLogEndToEnd.get("missionLogRendered"))
                        && Boolean.FALSE.equals(object(
                        missionLogEndToEndAcceptanceSmoke.get("rejectedNoInput")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        missionLogEndToEndAcceptanceSmoke.get("rejectedNoRender")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        missionLogEndToEndAcceptanceSmoke.get("rejectedNoInteraction")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        missionLogEndToEndAcceptanceSmoke.get("rejectedNoUpdate")).get("accepted"));
        boolean adapterUiHandlersSatisfied = terminalCommandExecuted
                && indexSearchExecuted
                && lensScanExecuted
                && hudUpdated
                && notificationQueueUpdated
                && deathRecoveryActionExecuted;
        boolean holomapWikiHandlersSatisfied = holomapOpened
                && wikiOpened
                && holomapScreen.lines().contains(dataSources.holomapOutput())
                && wikiScreen.lines().contains(dataSources.wikiOutput())
                && ("holomap:marker:" + dataSources.holomapValues().get("marker")).equals(holomapScreen.focusPath())
                && String.valueOf(dataSources.wikiValues().get("link")).equals(wikiScreen.focusPath());
        boolean surfaceRenderModelsSatisfied = terminalScreen.render(inputContext).lines().contains(dataSources.terminalReadyLine())
                && indexScreen.render(inputContext).lines().contains(dataSources.indexResult())
                && lensScreen.render(inputContext).lines().contains("Scan: " + dataSources.lensResult())
                && missionLogScreen.render(inputContext).lines().contains(String.valueOf(missionLogValues.get("objective")))
                && settingsScreen.render(inputContext).lines().contains("Theme: " + settingsValues.get("theme"))
                && holomapScreen.render(inputContext).lines().contains(dataSources.holomapOutput())
                && wikiScreen.render(inputContext).lines().contains(dataSources.wikiOutput())
                && pauseFlowScreen.render(inputContext).focusPath().equals("pause:resume:" + pausePreviousScreen);
        boolean surfaceRendererClassesSatisfied = EchoAgent5UiSurfaceRenderer.render("TERMINAL", Map.of(
                "focusedControl", "terminal:input",
                "mouseRouted", true,
                "terminalBuffer", dataSources.terminalCommand(),
                "terminalOutput", dataSources.terminalReadyLine(),
                "terminalCommandExecuted", true
        ), dataSources).lines().stream().anyMatch(line -> line.contains(dataSources.terminalReadyLine()))
                && EchoAgent5UiSurfaceRenderer.render("INDEX", Map.of(
                        "indexBuffer", dataSources.indexQuery(),
                        "indexOutput", dataSources.indexResult(),
                        "indexSearchExecuted", true
                ), dataSources).lines().stream().anyMatch(line -> line.contains(dataSources.indexResult()))
                && EchoAgent5UiSurfaceRenderer.render("LENS", Map.of(
                        "lensOutput", dataSources.lensResult(),
                        "lensScanExecuted", true
                ), dataSources).lines().contains("Scan: " + dataSources.lensResult())
                && EchoAgent5UiSurfaceRenderer.render("HOLOMAP", Map.of(), dataSources).lines().contains(dataSources.holomapOutput())
                && EchoAgent5UiSurfaceRenderer.render("WIKI", Map.of(), dataSources).lines().contains(dataSources.wikiOutput())
                && EchoAgent5UiSurfaceRenderer.render("PAUSE", Map.of(
                        "previousMode", pausePreviousScreen
                ), dataSources).focusPath().equals("pause:resume:" + pausePreviousScreen)
                && EchoAgent5UiSurfaceRenderer.render("MAIN_MENU", Map.of(), dataSources)
                        .lines()
                        .contains(String.join(", ", dataSources.mainMenuOptions())
                                + " route through the standalone UI host.");
        Map<String, Object> routerTyped = EchoAgent5UiActionRouter.routeCharacter(
                "TERMINAL",
                "terminal:input",
                "statu",
                "",
                's'
        );
        Map<String, Object> routerInitialFocus = EchoAgent5UiActionRouter.routeInitialFocus(
                "TERMINAL",
                EchoAgent5UiReference.WIKI_SCREEN
        );
        Map<String, Object> routerTerminal = EchoAgent5UiActionRouter.activate("TERMINAL", Map.of(
                "focusedControl", "terminal:input",
                "terminalBuffer", dataSources.terminalCommand()
        ), dataSources);
        Map<String, Object> routerIndex = EchoAgent5UiActionRouter.activate("INDEX", Map.of(
                "focusedControl", "index:search",
                "indexBuffer", dataSources.indexQuery()
        ), dataSources);
        Map<String, Object> routerLens = EchoAgent5UiActionRouter.activate("LENS", Map.of(
                "focusedControl", "lens:scan"
        ), dataSources);
        Map<String, Object> routerMouse = EchoAgent5UiActionRouter.routeMouseClick("LENS", EchoAgent5UiReference.WIKI_SCREEN, Map.of(), dataSources);
        Map<String, Object> routerList = EchoAgent5UiActionRouter.routeListNavigation("DOWN", "SETTINGS", 1, dataSources);
        Map<String, Object> routerNotification = EchoAgent5UiActionRouter.routeNotificationDismiss(dataSources.notifications(), dataSources);
        Map<String, Object> routerSettings = EchoAgent5UiActionRouter.routeSettingsAdjustment("Subtitles", 1.25D, true);
        Map<String, Object> routerPauseOption = EchoAgent5UiActionRouter.routePauseOption("Settings", "LENS");
        Map<String, Object> routerMainMenuOption = EchoAgent5UiActionRouter.routeMainMenuOption("Settings");
        Map<String, Object> routerMission = EchoAgent5UiActionRouter.routeMissionLogUpdate(Map.of(
                "missionProgress", 0.25D,
                "missionStatus", EchoAgent5UiReference.ACTIVE_MISSION_STATUS
        ), dataSources);
        Map<String, Object> routerEscape = EchoAgent5UiActionRouter.routeKey("ESCAPE", "TERMINAL", EchoAgent5UiReference.WIKI_SCREEN);
        Map<String, Object> routerHud = EchoAgent5UiActionRouter.routeKey("H", "TERMINAL", EchoAgent5UiReference.WIKI_SCREEN);
        Map<String, Object> routerHudUpdate = EchoAgent5UiActionRouter.routeHudUpdate(Map.of("hudHealth", 92), dataSources);
        Map<String, Object> routerCameraCinematic = EchoAgent5UiActionRouter.routeCameraCinematicFrame(
                Map.of("cinematicFrame", 0),
                dataSources
        );
        boolean inputActionRouterClassesSatisfied = "terminal:input".equals(EchoAgent5UiActionRouter.focusPath(
                "TERMINAL",
                EchoAgent5UiReference.WIKI_SCREEN
        ))
                && Boolean.TRUE.equals(routerTyped.get("handled"))
                && dataSources.terminalCommand().equals(routerTyped.get("value"))
                && "EchoAgent5UiActionRouter".equals(routerTyped.get("routerClass"))
                && Boolean.TRUE.equals(routerInitialFocus.get("handled"))
                && "terminal:input".equals(routerInitialFocus.get("focusedControl"))
                && "focus:initial:terminal".equals(routerInitialFocus.get("effect"))
                && Boolean.TRUE.equals(routerTerminal.get("handled"))
                && dataSources.terminalReadyLine().equals(routerTerminal.get("output"))
                && "terminalCommandExecuted".equals(routerTerminal.get("executedKey"))
                && Boolean.TRUE.equals(routerIndex.get("handled"))
                && dataSources.indexResult().equals(routerIndex.get("output"))
                && "indexSearchExecuted".equals(routerIndex.get("executedKey"))
                && Boolean.TRUE.equals(routerLens.get("handled"))
                && dataSources.lensResult().equals(routerLens.get("output"))
                && "lensScanExecuted".equals(routerLens.get("executedKey"))
                && Boolean.TRUE.equals(routerMouse.get("handled"))
                && "lens:scan".equals(routerMouse.get("focusedControl"))
                && "lensScanExecuted".equals(routerMouse.get("executedKey"))
                && Boolean.TRUE.equals(routerList.get("handled"))
                && "Input Mode".equals(routerList.get("selectedOption"))
                && "list:settings:down".equals(routerList.get("effect"))
                && Boolean.TRUE.equals(routerNotification.get("handled"))
                && dataSources.notifications().get(0).get("id").equals(routerNotification.get("dismissedId"))
                && dataSources.notifications().get(0).get("message").equals(routerNotification.get("dismissedMessage"))
                && "notification:dismiss-oldest".equals(routerNotification.get("effect"))
                && Boolean.TRUE.equals(routerSettings.get("handled"))
                && Boolean.FALSE.equals(routerSettings.get("settingsSubtitles"))
                && "settings:subtitles".equals(routerSettings.get("effect"))
                && Boolean.TRUE.equals(routerPauseOption.get("handled"))
                && "SETTINGS".equals(routerPauseOption.get("destinationMode"))
                && "pause:settings".equals(routerPauseOption.get("effect"))
                && Boolean.TRUE.equals(routerMainMenuOption.get("handled"))
                && EchoAgent5UiReference.SETTINGS_SCREEN.equals(routerMainMenuOption.get("destinationMode"))
                && "main_menu:settings".equals(routerMainMenuOption.get("effect"))
                && Boolean.TRUE.equals(routerMission.get("handled"))
                && EchoAgent5UiReference.ACTIVE_MISSION_UPDATED_STATUS.equals(routerMission.get("missionStatus"))
                && Double.valueOf(0.5D).equals(routerMission.get("missionProgress"))
                && ("mission:update:" + EchoAgent5UiReference.ACTIVE_MISSION_ID).equals(routerMission.get("effect"))
                && Boolean.TRUE.equals(routerEscape.get("handled"))
                && "PAUSE".equals(routerEscape.get("destinationMode"))
                && "TERMINAL".equals(routerEscape.get("destinationPreviousMode"))
                && Boolean.TRUE.equals(routerHudUpdate.get("handled"))
                && Integer.valueOf(85).equals(routerHudUpdate.get("hudHealth"))
                && "hud:update:health_hazard_mission".equals(routerHudUpdate.get("effect"))
                && Boolean.TRUE.equals(routerCameraCinematic.get("handled"))
                && "over_shoulder".equals(routerCameraCinematic.get("cameraMode"))
                && Integer.valueOf(1).equals(routerCameraCinematic.get("cinematicFrame"))
                && ("camera_cinematic:frame:" + dataSources.cinematicValues().get("cue")).equals(
                routerCameraCinematic.get("effect"));
        Map<String, Object> screenHostModel = EchoAgent5UiScreenHostModel.render("TERMINAL", Map.of(
                "focusedControl", "terminal:input",
                "mouseRouted", true,
                "terminalBuffer", dataSources.terminalCommand(),
                "terminalOutput", dataSources.terminalReadyLine(),
                "terminalCommandExecuted", true
        ), "ashfall", 12, 3, 2, 1, dataSources);
        boolean screenHostModelsSatisfied = Boolean.TRUE.equals(screenHostModel.get("serviceCodeExecuted"))
                && "EchoAgent5UiScreenHostModel".equals(screenHostModel.get("hostModelClass"))
                && "ECHO NATIVE // TERMINAL".equals(screenHostModel.get("screenTitle"))
                && strings(screenHostModel, "headerLines").stream()
                        .anyMatch(line -> line.contains("Health " + dataSources.hudValues().get("health")))
                && strings(screenHostModel, "headerLines").stream()
                        .anyMatch(line -> line.contains(
                                String.valueOf(dataSources.notifications().get(0).get("message"))))
                && strings(screenHostModel, "surfaceLines").stream().anyMatch(line -> line.contains(dataSources.terminalReadyLine()))
                && String.valueOf(screenHostModel.get("footerLine")).contains("J/K/RIGHT_BRACKET/LEFT_BRACKET/BACKSLASH HoloMap")
                && String.valueOf(screenHostModel.get("footerLine")).contains("N SignalOS")
                && String.valueOf(screenHostModel.get("footerLine")).contains("X/C/Y/Z Drone");
        Map<String, Object> screenStackSmoke = EchoAgent5ScreenStackSmoke.capture(
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "ashfall",
                12,
                3,
                2,
                1,
                dataSources
        );
        boolean screenStackExecutionSmokeSatisfied = Boolean.TRUE.equals(screenStackSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(screenStackSmoke.get("passed"))
                && "EchoAgent5ScreenStackSmoke".equals(screenStackSmoke.get("screenStackSmokeClass"))
                && strings(screenStackSmoke, "events").containsAll(List.of(
                        "push:MAIN_MENU",
                        "push:TERMINAL",
                        "push:INDEX",
                        "push:LENS",
                        "push:PAUSE",
                        "pop:PAUSE",
                        "replace:SETTINGS",
                        "replace:LENS",
                        "push:RECOVERY",
                        "pop:RECOVERY",
                        "empty-pop"
                ))
                && strings(screenStackSmoke, "currentModes").containsAll(List.of(
                        "MAIN_MENU",
                        "TERMINAL",
                        "INDEX",
                        "LENS",
                        "PAUSE",
                        "SETTINGS",
                        "RECOVERY"
                ))
                && strings(screenStackSmoke, "routeFocusPaths").containsAll(List.of(
                        "terminal:input",
                        "index:search",
                        "lens:scan",
                        "pause:resume:LENS",
                        "recovery:recover"
                ))
                && "LENS".equals(screenStackSmoke.get("resumeMode"))
                && Boolean.TRUE.equals(screenStackSmoke.get("emptyPopSafe"))
                && "MAIN_MENU".equals(screenStackSmoke.get("finalCurrentMode"))
                && Integer.valueOf(1).equals(screenStackSmoke.get("finalStackSize"))
                && strings(screenStackSmoke, "screenTitles").containsAll(List.of(
                        "ECHO NATIVE // MAIN_MENU",
                        "ECHO NATIVE // PAUSE",
                        "ECHO NATIVE // RECOVERY"
                ));
        Map<String, Object> screenLifecycleSmoke = EchoAgent5ScreenLifecycleSmoke.capture(
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "ashfall",
                12,
                3,
                2,
                1,
                dataSources
        );
        boolean screenLifecycleSmokeSatisfied = Boolean.TRUE.equals(screenLifecycleSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(screenLifecycleSmoke.get("passed"))
                && "EchoAgent5ScreenLifecycleSmoke".equals(screenLifecycleSmoke.get("screenLifecycleSmokeClass"))
                && strings(screenLifecycleSmoke, "visitedModes").containsAll(List.of(
                        "MAIN_MENU",
                        "TERMINAL",
                        "INDEX",
                        "LENS",
                        "PAUSE",
                        "RECOVERY"
                ))
                && strings(screenLifecycleSmoke, "routeEffects").containsAll(List.of(
                        "route:terminal",
                        "route:index",
                        "route:lens",
                        "route:escape"
                ))
                && "LENS".equals(screenLifecycleSmoke.get("pausePreviousMode"))
                && "LENS".equals(screenLifecycleSmoke.get("resumeMode"))
                && "terminal:input".equals(screenLifecycleSmoke.get("terminalFocusPath"))
                && "index:search".equals(screenLifecycleSmoke.get("indexFocusPath"))
                && "lens:scan".equals(screenLifecycleSmoke.get("lensFocusPath"))
                && "recovery:recover".equals(screenLifecycleSmoke.get("recoveryFocusPath"))
                && strings(screenLifecycleSmoke, "screenTitles").contains("ECHO NATIVE // PAUSE");
        boolean screenLifecycleActionsSatisfied = strings(screenLifecycleSmoke, "actionExecutedKeys").containsAll(List.of(
                        "terminalCommandExecuted",
                        "indexSearchExecuted",
                        "lensScanExecuted",
                        "recoveryActionExecuted"
                ))
                && strings(screenLifecycleSmoke, "actionOutputs").containsAll(List.of(
                        dataSources.terminalReadyLine(),
                        dataSources.indexResult(),
                        dataSources.lensResult(),
                        "Status: " + EchoAgent5UiReference.RECOVERY_STATUS + "    Health: 35"
                ))
                && strings(screenLifecycleSmoke, "actionSurfaceLines").stream()
                        .anyMatch(line -> line.contains(dataSources.terminalCommand() + " -> " + dataSources.terminalReadyLine()))
                && strings(screenLifecycleSmoke, "actionSurfaceLines").stream()
                        .anyMatch(line -> line.contains(dataSources.indexQuery() + " -> " + dataSources.indexResult()))
                && strings(screenLifecycleSmoke, "actionSurfaceLines").stream()
                        .anyMatch(line -> line.contains(dataSources.lensResult()))
                && strings(screenLifecycleSmoke, "actionSurfaceLines").stream()
                        .anyMatch(line -> line.contains("Status: " + EchoAgent5UiReference.RECOVERY_STATUS));
        Map<String, Object> terminalModuleRenderer = EchoAgent5UiModuleSurfaceRenderers.renderTerminal(Map.of(
                "focusedControl", "terminal:input",
                "mouseRouted", true,
                "terminalBuffer", dataSources.terminalCommand(),
                "terminalOutput", dataSources.terminalReadyLine(),
                "terminalCommandExecuted", true
        ), dataSources);
        Map<String, Object> indexModuleRenderer = EchoAgent5UiModuleSurfaceRenderers.renderIndex(Map.of(
                "indexBuffer", dataSources.indexQuery(),
                "indexOutput", dataSources.indexResult(),
                "indexSearchExecuted", true
        ), dataSources);
        Map<String, Object> lensModuleRenderer = EchoAgent5UiModuleSurfaceRenderers.renderLens(Map.of(
                "lensOutput", dataSources.lensResult(),
                "lensScanExecuted", true
        ), dataSources);
        Map<String, Object> holomapModuleRenderer = EchoAgent5UiModuleSurfaceRenderers.renderHolomap(Map.of(), dataSources);
        Map<String, Object> wikiModuleRenderer = EchoAgent5UiModuleSurfaceRenderers.renderWiki(Map.of(), dataSources);
        Map<String, Object> missionLogModuleRenderer = EchoAgent5UiModuleSurfaceRenderers.renderMissionLog(Map.of(), dataSources);
        Map<String, Object> settingsModuleRenderer = EchoAgent5UiModuleSurfaceRenderers.renderSettings(Map.of(), dataSources);
        Map<String, Object> pauseModuleRenderer = EchoAgent5UiModuleSurfaceRenderers.renderPause(Map.of(
                "previousMode", EchoAgent5UiReference.WIKI_SCREEN
        ), dataSources);
        Map<String, Object> recoveryModuleRenderer = EchoAgent5UiModuleSurfaceRenderers.renderRecovery(Map.of(
                "focusedControl", "recovery:recover",
                "mouseRouted", true
        ), dataSources);
        Map<String, Object> mainMenuModuleRenderer = EchoAgent5UiModuleSurfaceRenderers.renderMainMenu(Map.of(), dataSources);
        Map<String, Object> hudModuleRenderer = EchoAgent5UiModuleSurfaceRenderers.renderHud(Map.of(), dataSources);
        boolean moduleSurfaceRenderersSatisfied =
                EchoAgent5UiModuleSurfaceRenderers.EchoAgent5TerminalSurfaceRenderer.class.getSimpleName()
                        .equals(terminalModuleRenderer.get("moduleRendererClass"))
                && strings(terminalModuleRenderer, "lines").stream().anyMatch(line -> line.contains(dataSources.terminalReadyLine()))
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5IndexSurfaceRenderer.class.getSimpleName()
                        .equals(indexModuleRenderer.get("moduleRendererClass"))
                && strings(indexModuleRenderer, "lines").stream().anyMatch(line -> line.contains(dataSources.indexResult()))
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5LensSurfaceRenderer.class.getSimpleName()
                        .equals(lensModuleRenderer.get("moduleRendererClass"))
                && strings(lensModuleRenderer, "lines").contains("Scan: " + dataSources.lensResult())
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5HolomapSurfaceRenderer.class.getSimpleName()
                        .equals(holomapModuleRenderer.get("moduleRendererClass"))
                && strings(holomapModuleRenderer, "lines").contains(dataSources.holomapOutput())
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5WikiSurfaceRenderer.class.getSimpleName()
                        .equals(wikiModuleRenderer.get("moduleRendererClass"))
                && strings(wikiModuleRenderer, "lines").contains(dataSources.wikiOutput());
        boolean allModuleSurfaceRenderersSatisfied = moduleSurfaceRenderersSatisfied
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5MissionLogSurfaceRenderer.class.getSimpleName()
                        .equals(missionLogModuleRenderer.get("moduleRendererClass"))
                && strings(missionLogModuleRenderer, "lines").stream()
                        .anyMatch(line -> line.contains(EchoAgent5UiReference.ACTIVE_MISSION_TITLE))
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5SettingsSurfaceRenderer.class.getSimpleName()
                        .equals(settingsModuleRenderer.get("moduleRendererClass"))
                && strings(settingsModuleRenderer, "lines").stream()
                        .anyMatch(line -> line.contains(EchoAgent5UiReference.SETTINGS_THEME))
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5PauseSurfaceRenderer.class.getSimpleName()
                        .equals(pauseModuleRenderer.get("moduleRendererClass"))
                && ("pause:resume:" + EchoAgent5UiReference.WIKI_SCREEN).equals(pauseModuleRenderer.get("focusPath"))
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5RecoverySurfaceRenderer.class.getSimpleName()
                        .equals(recoveryModuleRenderer.get("moduleRendererClass"))
                && strings(recoveryModuleRenderer, "lines").stream()
                        .anyMatch(line -> line.contains(EchoAgent5UiReference.RECOVERY_POINT))
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5MainMenuSurfaceRenderer.class.getSimpleName()
                        .equals(mainMenuModuleRenderer.get("moduleRendererClass"))
                && strings(mainMenuModuleRenderer, "lines").stream().anyMatch(line -> line.contains("Continue"))
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5HudSurfaceRenderer.class.getSimpleName()
                        .equals(hudModuleRenderer.get("moduleRendererClass"))
                && strings(hudModuleRenderer, "lines").stream()
                        .anyMatch(line -> line.contains("Health " + dataSources.hudValues().get("health")))
                && EchoAgent5UiSurfaceRenderer.render("MISSION_LOG", Map.of(), dataSources).title()
                        .contains("EchoAgent5MissionLogSurfaceRenderer")
                && EchoAgent5UiSurfaceRenderer.render("SETTINGS", Map.of(), dataSources).title()
                        .contains("EchoAgent5SettingsSurfaceRenderer")
                && EchoAgent5UiSurfaceRenderer.render("PAUSE", Map.of(
                        "previousMode", EchoAgent5UiReference.WIKI_SCREEN
                ), dataSources).title().contains("EchoAgent5PauseSurfaceRenderer")
                && EchoAgent5UiSurfaceRenderer.render("RECOVERY", Map.of(), dataSources).title()
                        .contains("EchoAgent5RecoverySurfaceRenderer")
                && EchoAgent5UiSurfaceRenderer.render("MAIN_MENU", Map.of(), dataSources).title()
                        .contains("EchoAgent5MainMenuSurfaceRenderer")
                && EchoAgent5UiSurfaceRenderer.render("HUD", Map.of(), dataSources).title()
                        .contains("EchoAgent5HudSurfaceRenderer");
        Map<String, Object> themeApplicationSmoke = EchoAgent5ThemeApplicationSmoke.capture(
                "ashfall",
                12,
                3,
                2,
                1,
                dataSources
        );
        boolean themeApplicationSmokeSatisfied = Boolean.TRUE.equals(themeApplicationSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(themeApplicationSmoke.get("passed"))
                && "EchoAgent5ThemeApplicationSmoke".equals(themeApplicationSmoke.get("themeApplicationSmokeClass"))
                && EchoAgent5UiReference.SETTINGS_THEME.equals(themeApplicationSmoke.get("themeId"))
                && EchoAgent5UiReference.SETTINGS_PROFILE.equals(themeApplicationSmoke.get("settingsProfile"))
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5SettingsSurfaceRenderer.class.getSimpleName()
                        .equals(themeApplicationSmoke.get("settingsSurfaceRenderer"))
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5TerminalSurfaceRenderer.class.getSimpleName()
                        .equals(themeApplicationSmoke.get("terminalSurfaceRenderer"))
                && strings(themeApplicationSmoke, "settingsSurfaceLines").stream()
                        .anyMatch(line -> line.contains("Theme: " + EchoAgent5UiReference.SETTINGS_THEME))
                && strings(themeApplicationSmoke, "terminalSurfaceLines").stream()
                        .anyMatch(line -> line.contains(dataSources.terminalReadyLine()))
                && dataSources.terminalPrompt().equals(object(themeApplicationSmoke.get("tokens")).get("terminal.prompt"));
        Map<String, Object> terminalHostSmoke = EchoAgent5UiHostSmokeSnapshot.capture(
                "TERMINAL",
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "ashfall",
                12,
                3,
                2,
                1,
                dataSources
        );
        Map<String, Object> holomapHostSmoke = EchoAgent5UiHostSmokeSnapshot.capture(
                "HOLOMAP",
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "ashfall",
                12,
                3,
                2,
                1,
                dataSources
        );
        boolean uiHostSmokeSnapshotsSatisfied = Boolean.TRUE.equals(terminalHostSmoke.get("serviceCodeExecuted"))
                && "EchoAgent5UiHostSmokeSnapshot".equals(terminalHostSmoke.get("snapshotClass"))
                && "TERMINAL".equals(terminalHostSmoke.get("surface"))
                && "terminal:input".equals(terminalHostSmoke.get("focusPath"))
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5TerminalSurfaceRenderer.class.getSimpleName()
                        .equals(terminalHostSmoke.get("moduleRendererClass"))
                && EchoAgent5UiHostSmokeSnapshot.strings(terminalHostSmoke, "surfaceLines").stream()
                        .anyMatch(line -> line.contains(dataSources.terminalReadyLine()))
                && EchoAgent5UiHostSmokeSnapshot.strings(terminalHostSmoke, "headerLines").stream()
                        .anyMatch(line -> line.contains("Health " + dataSources.hudValues().get("health")))
                && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5HolomapSurfaceRenderer.class.getSimpleName()
                        .equals(holomapHostSmoke.get("moduleRendererClass"))
                && EchoAgent5UiHostSmokeSnapshot.strings(holomapHostSmoke, "surfaceLines").stream()
                        .anyMatch(line -> line.contains(
                                String.valueOf(dataSources.holomapValues().get("marker"))));
        Map<String, Object> interactionSmoke = EchoAgent5UiHostInteractionSmoke.run(
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "ashfall",
                12,
                3,
                2,
                1,
                dataSources
        );
        List<Map<String, Object>> interactionSteps = maps(interactionSmoke.get("steps"));
        boolean uiHostInteractionSmokeSatisfied = Boolean.TRUE.equals(interactionSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(interactionSmoke.get("passed"))
                && "EchoAgent5UiHostInteractionSmoke".equals(interactionSmoke.get("interactionSmokeClass"))
                && interactionSteps.size() >= 5
                && interactionSteps.stream().anyMatch(step -> "terminal_command".equals(step.get("id"))
                        && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5TerminalSurfaceRenderer.class.getSimpleName()
                        .equals(step.get("moduleRendererClass")))
                && interactionSteps.stream().anyMatch(step -> "index_search".equals(step.get("id"))
                        && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5IndexSurfaceRenderer.class.getSimpleName()
                        .equals(step.get("moduleRendererClass")))
                && interactionSteps.stream().anyMatch(step -> "lens_scan".equals(step.get("id"))
                        && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5LensSurfaceRenderer.class.getSimpleName()
                        .equals(step.get("moduleRendererClass")))
                && interactionSteps.stream().anyMatch(step -> "holomap_open".equals(step.get("id"))
                        && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5HolomapSurfaceRenderer.class.getSimpleName()
                        .equals(step.get("moduleRendererClass")))
                && interactionSteps.stream().anyMatch(step -> "wiki_open".equals(step.get("id"))
                        && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5WikiSurfaceRenderer.class.getSimpleName()
                        .equals(step.get("moduleRendererClass")));
        boolean uiHostFullSurfaceInteractionsSatisfied = uiHostInteractionSmokeSatisfied
                && interactionSteps.size() == 10
                && interactionSteps.stream().anyMatch(step -> "mission_log_open".equals(step.get("id"))
                        && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5MissionLogSurfaceRenderer.class.getSimpleName()
                        .equals(step.get("moduleRendererClass")))
                && interactionSteps.stream().anyMatch(step -> "settings_open".equals(step.get("id"))
                        && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5SettingsSurfaceRenderer.class.getSimpleName()
                        .equals(step.get("moduleRendererClass")))
                && interactionSteps.stream().anyMatch(step -> "pause_resume".equals(step.get("id"))
                        && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5PauseSurfaceRenderer.class.getSimpleName()
                        .equals(step.get("moduleRendererClass"))
                        && "LENS".equals(step.get("resumeDestinationMode")))
                && interactionSteps.stream().anyMatch(step -> "recovery_action".equals(step.get("id"))
                        && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5RecoverySurfaceRenderer.class.getSimpleName()
                        .equals(step.get("moduleRendererClass")))
                && interactionSteps.stream().anyMatch(step -> "main_menu_open".equals(step.get("id"))
                        && EchoAgent5UiModuleSurfaceRenderers.EchoAgent5MainMenuSurfaceRenderer.class.getSimpleName()
                        .equals(step.get("moduleRendererClass")));
        Map<String, Object> mainMenuOverrideAttached = EchoAgent5MainMenuOverrideSmoke.capture(
                true,
                true,
                "",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "ashfall",
                12,
                3,
                2,
                1,
                dataSources
        );
        Map<String, Object> mainMenuOverrideSkipped = EchoAgent5MainMenuOverrideSmoke.capture(
                false,
                false,
                "current_screen_not_title:example",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "ashfall",
                12,
                3,
                2,
                1,
                dataSources
        );
        boolean mainMenuOverrideSmokeSatisfied = Boolean.TRUE.equals(mainMenuOverrideAttached.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(mainMenuOverrideAttached.get("passed"))
                && Boolean.TRUE.equals(mainMenuOverrideAttached.get("guardSatisfied"))
                && "guarded_title_screen_replacement".equals(mainMenuOverrideAttached.get("strategy"))
                && "EchoAgent5MainMenuOverrideSmoke".equals(mainMenuOverrideAttached.get("mainMenuOverrideSmokeClass"))
                && String.valueOf(mainMenuOverrideAttached.get("screenTitle")).contains("MAIN_MENU")
                && strings(mainMenuOverrideAttached, "surfaceLines").stream()
                        .anyMatch(line -> line.contains("Custom main menu surface is live"))
                && Boolean.TRUE.equals(mainMenuOverrideSkipped.get("passed"))
                && "current_screen_not_title:example".equals(mainMenuOverrideSkipped.get("skipReason"));
        Map<String, Object> hudOverlaySmoke = EchoAgent5HudOverlaySmoke.capture(
                true,
                true,
                "hud:update",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "ashfall",
                12,
                3,
                2,
                1,
                dataSources
        );
        boolean hudOverlaySmokeSatisfied = Boolean.TRUE.equals(hudOverlaySmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(hudOverlaySmoke.get("passed"))
                && "EchoAgent5HudOverlaySmoke".equals(hudOverlaySmoke.get("hudOverlaySmokeClass"))
                && EchoAgent5UiReference.HUD_LAYER.equals(hudOverlaySmoke.get("overlayLayerId"))
                && "hud:update".equals(hudOverlaySmoke.get("trigger"))
                && String.valueOf(hudOverlaySmoke.get("overlayMessage"))
                        .contains("Health " + dataSources.hudValues().get("health"))
                && String.valueOf(hudOverlaySmoke.get("overlayMessage"))
                        .contains(EchoAgent5UiReference.ACTIVE_MISSION_OBJECTIVE)
                && strings(hudOverlaySmoke, "overlayLines").stream()
                        .anyMatch(line -> line.contains(String.valueOf(hudValues.get("hazard"))))
                && strings(hudOverlaySmoke, "overlayLines").stream()
                        .anyMatch(line -> line.contains(
                                String.valueOf(dataSources.notifications().get(0).get("message"))))
                && "top_left_safe_area".equals(hudOverlaySmoke.get("notificationAnchor"))
                && strings(hudOverlaySmoke, "hostHeaderLines").stream()
                        .anyMatch(line -> line.contains("HUD: Health " + dataSources.hudValues().get("health")));
        Map<String, Object> hudOverlayEndToEndAcceptanceSmoke =
                EchoAgent5HudOverlayEndToEndAcceptanceSmoke.capture();
        Map<String, Object> acceptedHudOverlayEndToEnd =
                object(hudOverlayEndToEndAcceptanceSmoke.get("accepted"));
        boolean hudOverlayEndToEndAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(hudOverlayEndToEndAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(hudOverlayEndToEndAcceptanceSmoke.get("passed"))
                        && "EchoAgent5HudOverlayEndToEndAcceptanceSmoke".equals(
                        hudOverlayEndToEndAcceptanceSmoke.get("hudOverlayEndToEndAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedHudOverlayEndToEnd.get("accepted"))
                        && "hud_overlay_end_to_end:hud_update:HUD:85".equals(acceptedHudOverlayEndToEnd.get("effect"))
                        && Boolean.TRUE.equals(acceptedHudOverlayEndToEnd.get("overlayRendered"))
                        && Integer.valueOf(85).equals(acceptedHudOverlayEndToEnd.get("hudHealth"))
                        && "over_shoulder".equals(acceptedHudOverlayEndToEnd.get("cameraMode"))
                        && Boolean.FALSE.equals(object(
                        hudOverlayEndToEndAcceptanceSmoke.get("rejectedNoOverlay")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        hudOverlayEndToEndAcceptanceSmoke.get("rejectedNoHudUpdate")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        hudOverlayEndToEndAcceptanceSmoke.get("rejectedNoCamera")).get("accepted"));
        Map<String, Object> hotkeyBridgeSmoke = EchoAgent5HotkeyBridgeSmoke.capture(
                true,
                true,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "ashfall",
                12,
                3,
                2,
                1,
                dataSources
        );
        List<Map<String, Object>> hotkeySteps = maps(hotkeyBridgeSmoke.get("steps"));
        boolean hotkeyBridgeSmokeSatisfied = Boolean.TRUE.equals(hotkeyBridgeSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(hotkeyBridgeSmoke.get("passed"))
                && "EchoAgent5HotkeyBridgeSmoke".equals(hotkeyBridgeSmoke.get("hotkeyBridgeSmokeClass"))
                && hotkeySteps.size() == 18
                && hotkeySteps.stream().anyMatch(step -> "M".equals(step.get("key"))
                        && "TERMINAL".equals(step.get("destinationMode"))
                        && Boolean.TRUE.equals(step.get("passed")))
                && hotkeySteps.stream().anyMatch(step -> "G".equals(step.get("key"))
                        && "INDEX".equals(step.get("destinationMode"))
                        && Boolean.TRUE.equals(step.get("passed")))
                && hotkeySteps.stream().anyMatch(step -> "LEFT_ALT".equals(step.get("key"))
                        && "LENS".equals(step.get("destinationMode"))
                        && Boolean.TRUE.equals(step.get("passed")))
                && hotkeySteps.stream().anyMatch(step -> "J".equals(step.get("key"))
                        && "HOLOMAP".equals(step.get("destinationMode"))
                        && Boolean.TRUE.equals(step.get("passed")))
                && hotkeySteps.stream().anyMatch(step -> "N".equals(step.get("key"))
                        && "SIGNALOS".equals(step.get("destinationMode"))
                        && Boolean.TRUE.equals(step.get("passed")))
                && hotkeySteps.stream().anyMatch(step -> "B".equals(step.get("key"))
                        && "INDEX".equals(step.get("mode"))
                        && "INDEX".equals(step.get("destinationMode"))
                        && Boolean.TRUE.equals(step.get("passed")))
                && hotkeySteps.stream().anyMatch(step -> "B".equals(step.get("key"))
                        && "TERMINAL".equals(step.get("mode"))
                        && "ASHFALL_DRONE".equals(step.get("destinationMode"))
                        && "ASHFALL_DRONE".equals(step.get("expectedMode"))
                        && "B".equals(step.get("ashfallDroneKey"))
                        && Boolean.TRUE.equals(step.get("passed")))
                && hotkeySteps.stream().anyMatch(step -> "X".equals(step.get("key"))
                        && "ASHFALL_DRONE".equals(step.get("destinationMode"))
                        && "ASHFALL_DRONE".equals(step.get("expectedMode"))
                        && "X".equals(step.get("ashfallDroneKey"))
                        && Boolean.TRUE.equals(step.get("passed")))
                && hotkeySteps.stream().anyMatch(step -> "ESCAPE".equals(step.get("key"))
                        && "PAUSE".equals(step.get("destinationMode"))
                        && Boolean.TRUE.equals(step.get("passed")));
        Map<String, Object> notificationQueueSmoke = EchoAgent5NotificationQueueSmoke.capture(
                "ashfall",
                12,
                3,
                2,
                1,
                dataSources
        );
        boolean notificationQueueSmokeSatisfied = Boolean.TRUE.equals(notificationQueueSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(notificationQueueSmoke.get("passed"))
                && "EchoAgent5NotificationQueueSmoke".equals(notificationQueueSmoke.get("notificationQueueSmokeClass"))
                && EchoAgent5UiReference.NOTIFICATION_QUEUE.equals(notificationQueueSmoke.get("queueId"))
                && Integer.valueOf(2).equals(notificationQueueSmoke.get("sourceCount"))
                && Integer.valueOf(2).equals(notificationQueueSmoke.get("dispatchedCount"))
                && Boolean.TRUE.equals(notificationQueueSmoke.get("delivered"))
                && Boolean.TRUE.equals(notificationQueueSmoke.get("anchored"))
                && strings(notificationQueueSmoke, "messages")
                        .equals(dataSources.notifications().stream()
                                .map(entry -> String.valueOf(entry.get("message")))
                                .toList())
                && strings(notificationQueueSmoke, "severities").equals(dataSources.notifications().stream()
                        .map(entry -> String.valueOf(entry.get("severity")))
                        .toList())
                && strings(notificationQueueSmoke, "hostHeaderLines").stream()
                        .anyMatch(line -> line.contains(dataSources.notifications().stream()
                                .map(entry -> String.valueOf(entry.get("message")))
                                .reduce((left, right) -> left + " / " + right)
                                .orElse("")));
        Map<String, Object> notificationEndToEndAcceptanceSmoke =
                EchoAgent5NotificationEndToEndAcceptanceSmoke.capture(
                        "ashfall",
                        12,
                        3,
                        2,
                        1,
                        dataSources
                );
        Map<String, Object> acceptedNotificationEndToEnd =
                object(notificationEndToEndAcceptanceSmoke.get("accepted"));
        boolean notificationEndToEndAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(notificationEndToEndAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(notificationEndToEndAcceptanceSmoke.get("passed"))
                        && "EchoAgent5NotificationEndToEndAcceptanceSmoke".equals(
                        notificationEndToEndAcceptanceSmoke.get("notificationEndToEndAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedNotificationEndToEnd.get("accepted"))
                        && "notification_end_to_end:queue->hud:drop-oldest".equals(
                        acceptedNotificationEndToEnd.get("effect"))
                        && Boolean.TRUE.equals(acceptedNotificationEndToEnd.get("queueAccepted"))
                        && Boolean.TRUE.equals(acceptedNotificationEndToEnd.get("hudAccepted"))
                        && Boolean.TRUE.equals(acceptedNotificationEndToEnd.get("dismissAccepted"))
                        && "agent5-notification-1".equals(acceptedNotificationEndToEnd.get("dismissedId"))
                        && strings(acceptedNotificationEndToEnd, "remainingMessages")
                        .equals(List.of(String.valueOf(dataSources.notifications().get(1).get("message"))))
                        && Boolean.FALSE.equals(object(
                        notificationEndToEndAcceptanceSmoke.get("rejectedNoQueue")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        notificationEndToEndAcceptanceSmoke.get("rejectedNoDismiss")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        notificationEndToEndAcceptanceSmoke.get("rejectedNoHud")).get("accepted"));
        Map<String, Object> hudUpdateSmoke = EchoAgent5HudUpdateSmoke.capture(dataSources);
        boolean hudUpdateSmokeSatisfied = Boolean.TRUE.equals(hudUpdateSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(hudUpdateSmoke.get("passed"))
                && "EchoAgent5HudUpdateSmoke".equals(hudUpdateSmoke.get("hudUpdateSmokeClass"))
                && Integer.valueOf(85).equals(hudUpdateSmoke.get("hudHealth"))
                && dataSources.hudValues().get("hazard").equals(hudUpdateSmoke.get("hudHazard"))
                && "hud:update:health_hazard_mission".equals(hudUpdateSmoke.get("effect"))
                && strings(hudUpdateSmoke, "surfaceLines").stream()
                        .anyMatch(line -> line.contains("HUD overlay is live. Health 85"))
                && strings(hudUpdateSmoke, "hostHeaderLines").stream()
                        .anyMatch(line -> line.contains("HUD: Health 85 / " + dataSources.hudValues().get("hazard")));
        Map<String, Object> cameraCinematicSmoke = EchoAgent5CameraCinematicSmoke.capture(dataSources);
        boolean cameraCinematicSmokeSatisfied = Boolean.TRUE.equals(cameraCinematicSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(cameraCinematicSmoke.get("passed"))
                && "EchoAgent5CameraCinematicSmoke".equals(cameraCinematicSmoke.get("cameraCinematicSmokeClass"))
                && "over_shoulder".equals(cameraCinematicSmoke.get("cameraMode"))
                && Integer.valueOf(72).equals(cameraCinematicSmoke.get("cameraFov"))
                && dataSources.cinematicValues().get("cue").equals(cameraCinematicSmoke.get("cinematicCue"))
                && Integer.valueOf(1).equals(cameraCinematicSmoke.get("cinematicFrame"))
                && Boolean.TRUE.equals(cameraCinematicSmoke.get("cinematicLetterbox"))
                && ("camera_cinematic:frame:" + dataSources.cinematicValues().get("cue")).equals(
                cameraCinematicSmoke.get("effect"))
                && strings(cameraCinematicSmoke, "surfaceLines").stream()
                        .anyMatch(line -> line.contains(
                                "Camera over_shoulder frame 1 cue " + dataSources.cinematicValues().get("cue")))
                && strings(cameraCinematicSmoke, "hostSurfaceLines").stream()
                        .anyMatch(line -> line.contains("Letterbox: active"));
        Map<String, Object> renderCoreLayoutSmoke = EchoAgent5RenderCoreLayoutSmoke.capture();
        boolean renderCoreLayoutSmokeSatisfied = Boolean.TRUE.equals(renderCoreLayoutSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(renderCoreLayoutSmoke.get("passed"))
                && "EchoAgent5RenderCoreLayoutSmoke".equals(renderCoreLayoutSmoke.get("renderCoreLayoutSmokeClass"))
                && Integer.valueOf(620).equals(renderCoreLayoutSmoke.get("desktopPanelW"))
                && Integer.valueOf(300).equals(renderCoreLayoutSmoke.get("compactPanelW"))
                && Integer.valueOf(80).compareTo((Integer) renderCoreLayoutSmoke.get("compactTextMaxWidth")) <= 0
                && !maps(renderCoreLayoutSmoke.get("layouts")).isEmpty();
        Map<String, Object> hostEventTranscriptSmoke = EchoAgent5HostEventTranscriptSmoke.capture(
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "ashfall",
                12,
                3,
                2,
                1,
                dataSources
        );
        boolean hostEventTranscriptSmokeSatisfied = Boolean.TRUE.equals(hostEventTranscriptSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(hostEventTranscriptSmoke.get("passed"))
                && "EchoAgent5HostEventTranscriptSmoke".equals(hostEventTranscriptSmoke.get("hostEventTranscriptSmokeClass"))
                && strings(hostEventTranscriptSmoke, "events").contains("key:M->TERMINAL")
                && strings(hostEventTranscriptSmoke, "events").contains("text:terminal:" + dataSources.terminalCommand())
                && strings(hostEventTranscriptSmoke, "events").contains("text:index:" + dataSources.indexQuery())
                && strings(hostEventTranscriptSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains(dataSources.terminalReadyLine()))
                && strings(hostEventTranscriptSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains(dataSources.indexResult()))
                && strings(hostEventTranscriptSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains(dataSources.lensResult()))
                && strings(hostEventTranscriptSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains("HUD overlay is live. Health 85"));
        Map<String, Object> terminalEndToEndAcceptanceSmoke =
                EchoAgent5TerminalEndToEndAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedTerminalEndToEnd = object(terminalEndToEndAcceptanceSmoke.get("accepted"));
        boolean terminalEndToEndAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(terminalEndToEndAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(terminalEndToEndAcceptanceSmoke.get("passed"))
                        && "EchoAgent5TerminalEndToEndAcceptanceSmoke".equals(
                        terminalEndToEndAcceptanceSmoke.get("terminalEndToEndAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedTerminalEndToEnd.get("accepted"))
                        && "terminal_end_to_end:M->TERMINAL:status".equals(acceptedTerminalEndToEnd.get("effect"))
                        && Boolean.TRUE.equals(acceptedTerminalEndToEnd.get("physicalInputAccepted"))
                        && Boolean.TRUE.equals(acceptedTerminalEndToEnd.get("renderAccepted"))
                        && Boolean.TRUE.equals(acceptedTerminalEndToEnd.get("commandExecuted"))
                        && Boolean.TRUE.equals(acceptedTerminalEndToEnd.get("terminalRendered"))
                        && Boolean.FALSE.equals(object(
                        terminalEndToEndAcceptanceSmoke.get("rejectedNoInput")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        terminalEndToEndAcceptanceSmoke.get("rejectedNoRender")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        terminalEndToEndAcceptanceSmoke.get("rejectedNoCommand")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        terminalEndToEndAcceptanceSmoke.get("rejectedNoTranscript")).get("accepted"));
        Map<String, Object> indexEndToEndAcceptanceSmoke =
                EchoAgent5IndexEndToEndAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedIndexEndToEnd = object(indexEndToEndAcceptanceSmoke.get("accepted"));
        boolean indexEndToEndAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(indexEndToEndAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(indexEndToEndAcceptanceSmoke.get("passed"))
                        && "EchoAgent5IndexEndToEndAcceptanceSmoke".equals(
                        indexEndToEndAcceptanceSmoke.get("indexEndToEndAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedIndexEndToEnd.get("accepted"))
                        && "index_end_to_end:G->INDEX:ashfall".equals(acceptedIndexEndToEnd.get("effect"))
                        && Boolean.TRUE.equals(acceptedIndexEndToEnd.get("physicalInputAccepted"))
                        && Boolean.TRUE.equals(acceptedIndexEndToEnd.get("renderAccepted"))
                        && Boolean.TRUE.equals(acceptedIndexEndToEnd.get("searchExecuted"))
                        && Boolean.TRUE.equals(acceptedIndexEndToEnd.get("indexRendered"))
                        && Boolean.FALSE.equals(object(
                        indexEndToEndAcceptanceSmoke.get("rejectedNoInput")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        indexEndToEndAcceptanceSmoke.get("rejectedNoRender")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        indexEndToEndAcceptanceSmoke.get("rejectedNoSearch")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        indexEndToEndAcceptanceSmoke.get("rejectedNoTranscript")).get("accepted"));
        Map<String, Object> lensEndToEndAcceptanceSmoke =
                EchoAgent5LensEndToEndAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedLensEndToEnd = object(lensEndToEndAcceptanceSmoke.get("accepted"));
        boolean lensEndToEndAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(lensEndToEndAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(lensEndToEndAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LensEndToEndAcceptanceSmoke".equals(
                        lensEndToEndAcceptanceSmoke.get("lensEndToEndAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedLensEndToEnd.get("accepted"))
                        && ("lens_end_to_end:LEFT_ALT->LENS:" + dataSources.lensTarget()).equals(
                        acceptedLensEndToEnd.get("effect"))
                        && Boolean.TRUE.equals(acceptedLensEndToEnd.get("physicalInputAccepted"))
                        && Boolean.TRUE.equals(acceptedLensEndToEnd.get("renderAccepted"))
                        && Boolean.TRUE.equals(acceptedLensEndToEnd.get("scanExecuted"))
                        && Boolean.TRUE.equals(acceptedLensEndToEnd.get("lensRendered"))
                        && Boolean.FALSE.equals(object(
                        lensEndToEndAcceptanceSmoke.get("rejectedNoInput")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        lensEndToEndAcceptanceSmoke.get("rejectedNoRender")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        lensEndToEndAcceptanceSmoke.get("rejectedNoScan")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        lensEndToEndAcceptanceSmoke.get("rejectedNoTranscript")).get("accepted"));
        Map<String, Object> physicalHotkeyPollingSmoke = EchoAgent5PhysicalHotkeyPollingSmoke.capture();
        boolean physicalHotkeyPollingSmokeSatisfied =
                Boolean.TRUE.equals(physicalHotkeyPollingSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(physicalHotkeyPollingSmoke.get("passed"))
                        && "EchoAgent5PhysicalHotkeyPollingSmoke".equals(
                        physicalHotkeyPollingSmoke.get("physicalHotkeyPollingSmokeClass"))
                        && containsPhysicalHotkeyEffects(physicalHotkeyPollingSmoke, List.of(
                        Map.entry("M", "TERMINAL"),
                        Map.entry("G", "INDEX"),
                        Map.entry("R", "INDEX"),
                        Map.entry("U", "INDEX"),
                        Map.entry("B", "INDEX"),
                        Map.entry("LEFT_ALT", "LENS"),
                        Map.entry("J", "HOLOMAP"),
                        Map.entry("K", "HOLOMAP"),
                        Map.entry("RIGHT_BRACKET", "HOLOMAP"),
                        Map.entry("LEFT_BRACKET", "HOLOMAP"),
                        Map.entry("BACKSLASH", "HOLOMAP"),
                        Map.entry("N", "SIGNALOS"),
                        Map.entry("ESCAPE", "PAUSE"),
                        Map.entry("X", "ASHFALL_DRONE"),
                        Map.entry("C", "ASHFALL_DRONE"),
                        Map.entry("Y", "ASHFALL_DRONE"),
                        Map.entry("Z", "ASHFALL_DRONE")
                ))
                        && "physical_hotkey:none".equals(
                        object(physicalHotkeyPollingSmoke.get("repeatEvent")).get("effect"));
        Map<String, Object> liveSurfaceAcceptanceSmoke = EchoAgent5LiveSurfaceAcceptanceSmoke.capture();
        boolean liveSurfaceAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveSurfaceAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveSurfaceAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveSurfaceAcceptanceSmoke".equals(
                        liveSurfaceAcceptanceSmoke.get("liveSurfaceAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(object(liveSurfaceAcceptanceSmoke.get("accepted")).get("accepted"))
                        && "live_surface:accepted:TERMINAL".equals(
                        object(liveSurfaceAcceptanceSmoke.get("accepted")).get("effect"))
                        && strings(liveSurfaceAcceptanceSmoke, "routeSurfaces").equals(List.of(
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
                ))
                        && maps(liveSurfaceAcceptanceSmoke.get("acceptedRoutes")).stream()
                        .allMatch(route -> Boolean.TRUE.equals(route.get("accepted"))
                                && String.valueOf(route.get("effect")).startsWith("live_surface:accepted:"))
                        && Boolean.FALSE.equals(object(liveSurfaceAcceptanceSmoke.get("rejectedMode")).get("accepted"))
                        && Boolean.FALSE.equals(object(liveSurfaceAcceptanceSmoke.get("rejectedSetScreen")).get("accepted"));
        Map<String, Object> physicalInputAcceptanceSmoke = EchoAgent5PhysicalInputAcceptanceSmoke.capture();
        boolean physicalInputAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(physicalInputAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(physicalInputAcceptanceSmoke.get("passed"))
                        && "EchoAgent5PhysicalInputAcceptanceSmoke".equals(
                        physicalInputAcceptanceSmoke.get("physicalInputAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(object(physicalInputAcceptanceSmoke.get("accepted")).get("accepted"))
                        && "physical_input_acceptance:M->TERMINAL".equals(
                        object(physicalInputAcceptanceSmoke.get("accepted")).get("effect"))
                        && strings(physicalInputAcceptanceSmoke, "routeSurfaces").equals(List.of(
                        "TERMINAL",
                        "INDEX",
                        "INDEX",
                        "INDEX",
                        "INDEX",
                        "LENS",
                        "HOLOMAP",
                        "HOLOMAP",
                        "HOLOMAP",
                        "HOLOMAP",
                        "HOLOMAP",
                        "SIGNALOS",
                        "PAUSE",
                        "ASHFALL_DRONE",
                        "ASHFALL_DRONE",
                        "ASHFALL_DRONE",
                        "ASHFALL_DRONE"
                ))
                        && maps(physicalInputAcceptanceSmoke.get("acceptedRoutes")).stream()
                        .allMatch(route -> Boolean.TRUE.equals(route.get("accepted"))
                                && String.valueOf(route.get("effect")).startsWith("physical_input_acceptance:"))
                        && Boolean.FALSE.equals(object(
                        physicalInputAcceptanceSmoke.get("rejectedSurfaceMismatch")).get("accepted"))
                        && Boolean.FALSE.equals(object(physicalInputAcceptanceSmoke.get("rejectedNoHotkey")).get("accepted"));
        Map<String, Object> liveSurfaceRenderAcceptanceSmoke = EchoAgent5LiveSurfaceRenderAcceptanceSmoke.capture();
        boolean liveSurfaceRenderAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(liveSurfaceRenderAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(liveSurfaceRenderAcceptanceSmoke.get("passed"))
                        && "EchoAgent5LiveSurfaceRenderAcceptanceSmoke".equals(
                        liveSurfaceRenderAcceptanceSmoke.get("liveSurfaceRenderAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(object(liveSurfaceRenderAcceptanceSmoke.get("accepted")).get("accepted"))
                        && "live_surface_render:accepted:TERMINAL".equals(
                        object(liveSurfaceRenderAcceptanceSmoke.get("accepted")).get("effect"))
                        && strings(liveSurfaceRenderAcceptanceSmoke, "routeSurfaces").equals(List.of(
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
                ))
                        && maps(liveSurfaceRenderAcceptanceSmoke.get("acceptedRoutes")).stream()
                        .allMatch(route -> Boolean.TRUE.equals(route.get("accepted"))
                                && String.valueOf(route.get("effect")).startsWith("live_surface_render:accepted:")
                                && Integer.parseInt(String.valueOf(route.get("renderedLineCount"))) > 0)
                        && Boolean.FALSE.equals(object(
                        liveSurfaceRenderAcceptanceSmoke.get("rejectedUnacceptedSurface")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        liveSurfaceRenderAcceptanceSmoke.get("rejectedRenderedSurfaceMismatch")).get("accepted"));
        Map<String, Object> uiHostInteractionStateAcceptanceSmoke =
                EchoAgent5UiHostInteractionStateAcceptanceSmoke.capture();
        Map<String, Object> acceptedInteractionState = object(uiHostInteractionStateAcceptanceSmoke.get("accepted"));
        boolean uiHostInteractionStateAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(uiHostInteractionStateAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(uiHostInteractionStateAcceptanceSmoke.get("passed"))
                        && "EchoAgent5UiHostInteractionStateAcceptanceSmoke".equals(
                        uiHostInteractionStateAcceptanceSmoke.get("uiHostInteractionStateAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedInteractionState.get("accepted"))
                        && "ui_host_interaction_state:accepted:10".equals(acceptedInteractionState.get("effect"))
                        && Boolean.TRUE.equals(acceptedInteractionState.get("terminalAccepted"))
                        && Boolean.TRUE.equals(acceptedInteractionState.get("indexAccepted"))
                        && Boolean.TRUE.equals(acceptedInteractionState.get("lensAccepted"))
                        && Boolean.TRUE.equals(acceptedInteractionState.get("holomapAccepted"))
                        && Boolean.TRUE.equals(acceptedInteractionState.get("wikiAccepted"))
                        && Boolean.FALSE.equals(object(
                        uiHostInteractionStateAcceptanceSmoke.get("rejectedMissingStep")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        uiHostInteractionStateAcceptanceSmoke.get("rejectedFailedStep")).get("accepted"));
        Map<String, Object> uiHostEndToEndAcceptanceSmoke = EchoAgent5UiHostEndToEndAcceptanceSmoke.capture();
        Map<String, Object> acceptedEndToEnd = object(uiHostEndToEndAcceptanceSmoke.get("accepted"));
        boolean uiHostEndToEndAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(uiHostEndToEndAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(uiHostEndToEndAcceptanceSmoke.get("passed"))
                        && "EchoAgent5UiHostEndToEndAcceptanceSmoke".equals(
                        uiHostEndToEndAcceptanceSmoke.get("uiHostEndToEndAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedEndToEnd.get("accepted"))
                        && "ui_host_end_to_end:M->TERMINAL:10".equals(acceptedEndToEnd.get("effect"))
                        && Boolean.TRUE.equals(acceptedEndToEnd.get("physicalInputAccepted"))
                        && Boolean.TRUE.equals(acceptedEndToEnd.get("liveSurfaceAccepted"))
                        && Boolean.TRUE.equals(acceptedEndToEnd.get("renderAccepted"))
                        && Boolean.TRUE.equals(acceptedEndToEnd.get("interactionStateAccepted"))
                        && Boolean.FALSE.equals(object(uiHostEndToEndAcceptanceSmoke.get("rejectedNoInput")).get("accepted"))
                        && Boolean.FALSE.equals(object(uiHostEndToEndAcceptanceSmoke.get("rejectedRender")).get("accepted"))
                        && Boolean.FALSE.equals(object(uiHostEndToEndAcceptanceSmoke.get("rejectedInteraction")).get("accepted"));
        Map<String, Object> holoMapEndToEndAcceptanceSmoke =
                EchoAgent5HoloMapEndToEndAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedHoloMapEndToEnd = object(holoMapEndToEndAcceptanceSmoke.get("accepted"));
        boolean holoMapEndToEndAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(holoMapEndToEndAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(holoMapEndToEndAcceptanceSmoke.get("passed"))
                        && "EchoAgent5HoloMapEndToEndAcceptanceSmoke".equals(
                        holoMapEndToEndAcceptanceSmoke.get("holoMapEndToEndAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedHoloMapEndToEnd.get("accepted"))
                        && ("holomap_end_to_end:J->HOLOMAP:" + dataSources.holomapValues().get("marker")).equals(
                        acceptedHoloMapEndToEnd.get("effect"))
                        && Boolean.TRUE.equals(acceptedHoloMapEndToEnd.get("physicalInputAccepted"))
                        && Boolean.TRUE.equals(acceptedHoloMapEndToEnd.get("renderAccepted"))
                        && Boolean.TRUE.equals(acceptedHoloMapEndToEnd.get("interactionAccepted"))
                        && Boolean.TRUE.equals(acceptedHoloMapEndToEnd.get("holomapRendered"))
                        && Boolean.FALSE.equals(object(
                        holoMapEndToEndAcceptanceSmoke.get("rejectedNoInput")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        holoMapEndToEndAcceptanceSmoke.get("rejectedNoRender")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        holoMapEndToEndAcceptanceSmoke.get("rejectedNoInteraction")).get("accepted"));
        Map<String, Object> wikiEndToEndAcceptanceSmoke =
                EchoAgent5WikiEndToEndAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedWikiEndToEnd = object(wikiEndToEndAcceptanceSmoke.get("accepted"));
        boolean wikiEndToEndAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(wikiEndToEndAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(wikiEndToEndAcceptanceSmoke.get("passed"))
                        && "EchoAgent5WikiEndToEndAcceptanceSmoke".equals(
                        wikiEndToEndAcceptanceSmoke.get("wikiEndToEndAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedWikiEndToEnd.get("accepted"))
                        && ("wiki_end_to_end:direct:WIKI:" + dataSources.wikiValues().get("page")).equals(
                        acceptedWikiEndToEnd.get("effect"))
                        && Boolean.TRUE.equals(acceptedWikiEndToEnd.get("physicalInputAccepted"))
                        && Boolean.TRUE.equals(acceptedWikiEndToEnd.get("renderAccepted"))
                        && Boolean.TRUE.equals(acceptedWikiEndToEnd.get("interactionAccepted"))
                        && Boolean.TRUE.equals(acceptedWikiEndToEnd.get("wikiRendered"))
                        && Boolean.FALSE.equals(object(
                        wikiEndToEndAcceptanceSmoke.get("rejectedNoInput")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        wikiEndToEndAcceptanceSmoke.get("rejectedNoRender")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        wikiEndToEndAcceptanceSmoke.get("rejectedNoInteraction")).get("accepted"));
        Map<String, Object> mainMenuOptionActivationSmoke = EchoAgent5MainMenuOptionActivationSmoke.capture(dataSources);
        boolean mainMenuOptionActivationSmokeSatisfied = Boolean.TRUE.equals(mainMenuOptionActivationSmoke.get("serviceCodeExecuted"))
                && Boolean.TRUE.equals(mainMenuOptionActivationSmoke.get("passed"))
                && "EchoAgent5MainMenuOptionActivationSmoke".equals(mainMenuOptionActivationSmoke.get("mainMenuOptionActivationSmokeClass"))
                && strings(mainMenuOptionActivationSmoke, "selectedOptions").equals(List.of("Continue", "New Ashfall Run", "Settings", "Quit"))
                && strings(mainMenuOptionActivationSmoke, "destinations").equals(List.of(
                        EchoAgent5UiReference.WIKI_SCREEN,
                        EchoAgent5UiReference.MISSION_LOG_SCREEN,
                        EchoAgent5UiReference.SETTINGS_SCREEN,
                        EchoAgent5UiReference.MAIN_MENU_SCREEN
                ))
                && strings(mainMenuOptionActivationSmoke, "effects").equals(List.of(
                        "main_menu:continue",
                        "main_menu:new_run",
                        "main_menu:settings",
                        "main_menu:quit_requested"
                ))
                && Boolean.TRUE.equals(mainMenuOptionActivationSmoke.get("quitRequested"))
                && strings(mainMenuOptionActivationSmoke, "renderedLines").stream()
                        .anyMatch(line -> line.contains("Action: Settings selected: opening Settings"));
        Map<String, Object> mainMenuEndToEndAcceptanceSmoke =
                EchoAgent5MainMenuEndToEndAcceptanceSmoke.capture(dataSources);
        Map<String, Object> acceptedMainMenuEndToEnd = object(mainMenuEndToEndAcceptanceSmoke.get("accepted"));
        boolean mainMenuEndToEndAcceptanceSmokeSatisfied =
                Boolean.TRUE.equals(mainMenuEndToEndAcceptanceSmoke.get("serviceCodeExecuted"))
                        && Boolean.TRUE.equals(mainMenuEndToEndAcceptanceSmoke.get("passed"))
                        && "EchoAgent5MainMenuEndToEndAcceptanceSmoke".equals(
                        mainMenuEndToEndAcceptanceSmoke.get("mainMenuEndToEndAcceptanceSmokeClass"))
                        && Boolean.TRUE.equals(acceptedMainMenuEndToEnd.get("accepted"))
                        && "main_menu_end_to_end:accepted:4".equals(acceptedMainMenuEndToEnd.get("effect"))
                        && EchoAgent5UiReference.SETTINGS_SCREEN.equals(
                        acceptedMainMenuEndToEnd.get("settingsDestination"))
                        && Boolean.TRUE.equals(acceptedMainMenuEndToEnd.get("quitRequested"))
                        && strings(acceptedMainMenuEndToEnd, "selectedOptions")
                        .equals(List.of("Continue", "New Ashfall Run", "Settings", "Quit"))
                        && Boolean.FALSE.equals(object(
                        mainMenuEndToEndAcceptanceSmoke.get("rejectedNoOverride")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        mainMenuEndToEndAcceptanceSmoke.get("rejectedNoOptions")).get("accepted"))
                        && Boolean.FALSE.equals(object(
                        mainMenuEndToEndAcceptanceSmoke.get("rejectedNoQuit")).get("accepted"));

        boolean noScreenCrash = visited.containsAll(EchoAgent5UiReference.screenIds().stream()
                .filter(id -> !EchoAgent5UiReference.HUD_LAYER.equals(id))
                .filter(id -> !EchoAgent5UiReference.NOTIFICATION_QUEUE.equals(id))
                .toList());

        addDiagnostic(!uiReferenceAuditSmokeSatisfied, diagnostics,
                "Agent 5 UI reference audit smoke did not cover every Phase 1 behavior.");
        addDiagnostic(!uiRuntimeEquivalenceAuditSmokeSatisfied, diagnostics,
                "Agent 5 UI runtime equivalence audit smoke did not match Phase 4 values.");
        addDiagnostic(!screenCorePrimitiveExecutionSmokeSatisfied, diagnostics,
                "Agent 5 ScreenCore primitive execution smoke did not execute all primitives.");
        addDiagnostic(!phase5UiParityAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 Phase 5 UI parity acceptance smoke did not pass the full done checklist.");
        addDiagnostic(!liveClientAttachmentAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live-client attachment acceptance smoke did not verify all attachment prerequisites.");
        addDiagnostic(!liveClientHostEvidenceAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live-client host evidence acceptance smoke did not reject headless-only evidence.");
        addDiagnostic(!headlessUiBridgeReadinessAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 headless UI bridge readiness acceptance smoke did not verify executable non-live host evidence.");
        addDiagnostic(!uiHudHostCallQueueReplaySmokeSatisfied, diagnostics,
                "Agent 5 UI/HUD host-call queue replay smoke did not match the native AdapterCore runtime target.");
        addDiagnostic(!adapterCoreRuntimeBridgeGuardAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 AdapterCore runtime bridge guard smoke did not reject inactive runtime bridge evidence.");
        addDiagnostic(!liveClientUiProbeAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live-client UI probe acceptance smoke did not verify all live surface probes.");
        addDiagnostic(!liveClientInteractionProbeAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live-client interaction probe acceptance smoke did not verify all generated-screen actions.");
        addDiagnostic(!liveClientPhase5RouteSequenceAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live-client Phase 5 route-sequence acceptance smoke did not verify ordered physical-keybound screen routes and HUD overlay routing.");
        addDiagnostic(!livePhase5AcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live Phase 5 acceptance smoke did not verify actual UI done evidence.");
        addDiagnostic(!liveSurfaceRouteAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live surface route acceptance smoke did not verify hotkey to rendered surface routing.");
        addDiagnostic(!liveTextInputAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live text input acceptance smoke did not verify typed Terminal/Index input routing.");
        addDiagnostic(!liveHudOverlayRouteAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live HUD overlay route acceptance smoke did not verify H hotkey HUD overlay routing.");
        addDiagnostic(!liveMainMenuOverrideAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live main-menu override acceptance smoke did not verify title override, live surface, and option routing.");
        addDiagnostic(!liveNotificationQueueAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live notification queue acceptance smoke did not verify dispatch, HUD anchoring, and dismiss routing.");
        addDiagnostic(!liveHoloMapWikiNavigationAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live HoloMap/Wiki navigation acceptance smoke did not verify O/W routing and module outputs.");
        addDiagnostic(!liveSystemFlowAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live system flow acceptance smoke did not verify Settings, Pause, and Recovery live chains.");
        addDiagnostic(!liveCoreToolsAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live core tools acceptance smoke did not verify Terminal, Index, and Lens live chains.");
        addDiagnostic(!liveMissionObjectiveAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live mission objective acceptance smoke did not verify Mission Log and HUD mission state.");
        addDiagnostic(!liveInputFocusRoutingAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live input focus routing acceptance smoke did not verify focus, typing, mouse, and list routing.");
        addDiagnostic(!liveScreenStackStabilityAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live screen stack stability acceptance smoke did not verify stack lifecycle and no-crash interactions.");
        addDiagnostic(!liveVisualFrameAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live visual frame acceptance smoke did not verify theme, render layout, camera, cinematic, and HUD frame.");
        addDiagnostic(!liveModuleSurfaceCatalogAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live module surface catalog acceptance smoke did not verify all module-owned surface renderers.");
        addDiagnostic(!liveRenderCallbackAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live render callback acceptance smoke did not verify generated-screen render callback execution.");
        addDiagnostic(!liveScreenOwnershipAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live screen ownership acceptance smoke did not verify exact current-screen instance ownership.");
        addDiagnostic(!livePhysicalPollLoopAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live physical poll-loop acceptance smoke did not verify repeated GLFW key sampling.");
        addDiagnostic(!livePhysicalEventTranscriptAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live physical event transcript acceptance smoke did not verify ordered sampled input events.");
        addDiagnostic(!livePhysicalRouteEffectTranscriptAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live physical route-effect transcript acceptance smoke did not verify sampled UI outcomes.");
        addDiagnostic(!liveRouteBoundTextCommandAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live route-bound text command acceptance smoke did not verify Terminal/Index commands against routed screens.");
        addDiagnostic(!liveRouteBoundLensScanAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live route-bound Lens scan acceptance smoke did not verify Lens scan output against routed L evidence.");
        addDiagnostic(!liveRouteBoundHudUpdateAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live route-bound HUD update acceptance smoke did not verify HUD overlay output against routed H evidence.");
        addDiagnostic(!liveRouteBoundHoloMapWikiAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live route-bound HoloMap/Wiki acceptance smoke did not verify navigation output against routed O/W evidence.");
        addDiagnostic(!generatedScreenHostSmokeSatisfied, diagnostics,
                "Agent 5 standalone generated-screen host smoke did not execute the native generated dashboard shape.");
        addDiagnostic(!terminalOpened, diagnostics, "Terminal did not open.");
        addDiagnostic(!terminalCommandExecuted, diagnostics, "Terminal command did not execute.");
        addDiagnostic(!terminalEndToEndAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 terminal end-to-end acceptance smoke did not execute.");
        addDiagnostic(!indexOpened || !indexSearchExecuted, diagnostics, "Index did not open and search.");
        addDiagnostic(!indexEndToEndAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 Index end-to-end acceptance smoke did not execute.");
        addDiagnostic(!lensScanExecuted, diagnostics, "Lens did not scan target.");
        addDiagnostic(!lensEndToEndAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 Lens end-to-end acceptance smoke did not execute.");
        addDiagnostic(!hudUpdated, diagnostics, "HUD values did not update.");
        addDiagnostic(!notificationQueueUpdated, diagnostics, "Notification queue did not dispatch.");
        addDiagnostic(!notificationEndToEndAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 notification end-to-end acceptance smoke did not execute.");
        addDiagnostic(!missionLogOpened || !missionLogTrackedActiveMission, diagnostics,
                "Mission log did not open or track the active mission.");
        addDiagnostic(!missionLogUpdateSmokeSatisfied, diagnostics,
                "Agent 5 mission log update smoke did not execute.");
        addDiagnostic(!missionLogEndToEndAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 mission log end-to-end acceptance smoke did not execute.");
        addDiagnostic(!settingsOpened || !settingsAppliedProfile, diagnostics,
                "Settings did not open or apply the reference profile.");
        addDiagnostic(!settingsEndToEndAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 settings end-to-end acceptance smoke did not execute.");
        addDiagnostic(!holomapOpened, diagnostics, "HoloMap did not open.");
        addDiagnostic(!holoMapEndToEndAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 HoloMap end-to-end acceptance smoke did not execute.");
        addDiagnostic(!wikiOpened, diagnostics, "Wiki did not open.");
        addDiagnostic(!wikiEndToEndAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 Wiki end-to-end acceptance smoke did not execute.");
        addDiagnostic(!pauseFlowOpened || !pauseFlowResumedPreviousScreen, diagnostics,
                "Pause flow did not open or resume the previous screen.");
        addDiagnostic(!deathRecoveryOpened || !deathRecoveryActionExecuted, diagnostics,
                "Death/recovery screen did not open or recover.");
        addDiagnostic(!recoveryEndToEndAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 recovery end-to-end acceptance smoke did not execute.");
        addDiagnostic(!screenCoreContractSatisfied, diagnostics,
                "ScreenCore contract primitives did not execute.");
        addDiagnostic(!dataSourcesSatisfied, diagnostics,
                "Agent 5 UI surfaces did not consume the shared data-source contract.");
        addDiagnostic(!inputRoutingSatisfied, diagnostics,
                "Agent 5 UI focus paths or input routing did not execute.");
        addDiagnostic(!focusManagerSmokeSatisfied, diagnostics,
                "Agent 5 focus manager smoke did not execute.");
        addDiagnostic(!initialFocusSmokeSatisfied, diagnostics,
                "Agent 5 initial focus smoke did not execute.");
        addDiagnostic(!textEditingSmokeSatisfied, diagnostics,
                "Agent 5 text editing smoke did not execute.");
        addDiagnostic(!mouseActivationSmokeSatisfied, diagnostics,
                "Agent 5 mouse activation smoke did not execute.");
        addDiagnostic(!listNavigationSmokeSatisfied, diagnostics,
                "Agent 5 list navigation smoke did not execute.");
        addDiagnostic(!notificationDismissSmokeSatisfied, diagnostics,
                "Agent 5 notification dismiss smoke did not execute.");
        addDiagnostic(!settingsAdjustmentSmokeSatisfied, diagnostics,
                "Agent 5 settings adjustment smoke did not execute.");
        addDiagnostic(!pauseOptionActivationSmokeSatisfied, diagnostics,
                "Agent 5 pause option activation smoke did not execute.");
        addDiagnostic(!pauseEndToEndAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 pause end-to-end acceptance smoke did not execute.");
        addDiagnostic(!adapterUiHandlersSatisfied, diagnostics,
                "Agent 5 AdapterCore UI handlers did not execute.");
        addDiagnostic(!holomapWikiHandlersSatisfied, diagnostics,
                "Agent 5 HoloMap/Wiki handlers did not execute.");
        addDiagnostic(!surfaceRenderModelsSatisfied, diagnostics,
                "Agent 5 surface render models did not execute.");
        addDiagnostic(!surfaceRendererClassesSatisfied, diagnostics,
                "Agent 5 surface renderer classes did not execute.");
        addDiagnostic(!inputActionRouterClassesSatisfied, diagnostics,
                "Agent 5 input action router classes did not execute.");
        addDiagnostic(!screenHostModelsSatisfied, diagnostics,
                "Agent 5 screen host models did not execute.");
        addDiagnostic(!screenStackExecutionSmokeSatisfied, diagnostics,
                "Agent 5 screen stack execution smoke did not execute.");
        addDiagnostic(!screenLifecycleSmokeSatisfied, diagnostics,
                "Agent 5 screen lifecycle smoke did not execute.");
        addDiagnostic(!screenLifecycleActionsSatisfied, diagnostics,
                "Agent 5 screen lifecycle actions did not execute.");
        addDiagnostic(!moduleSurfaceRenderersSatisfied, diagnostics,
                "Agent 5 module surface renderers did not execute.");
        addDiagnostic(!allModuleSurfaceRenderersSatisfied, diagnostics,
                "All Agent 5 module surface renderers did not execute.");
        addDiagnostic(!themeApplicationSmokeSatisfied, diagnostics,
                "Agent 5 theme application smoke did not execute.");
        addDiagnostic(!uiHostSmokeSnapshotsSatisfied, diagnostics,
                "Agent 5 UI host smoke snapshots did not execute.");
        addDiagnostic(!uiHostInteractionSmokeSatisfied, diagnostics,
                "Agent 5 UI host interaction smoke did not execute.");
        addDiagnostic(!uiHostFullSurfaceInteractionsSatisfied, diagnostics,
                "Agent 5 UI host full surface interaction smoke did not execute.");
        addDiagnostic(!mainMenuOverrideSmokeSatisfied, diagnostics,
                "Agent 5 main-menu override smoke did not execute.");
        addDiagnostic(!hudOverlaySmokeSatisfied, diagnostics,
                "Agent 5 HUD overlay smoke did not execute.");
        addDiagnostic(!hudOverlayEndToEndAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 HUD overlay end-to-end acceptance smoke did not execute.");
        addDiagnostic(!hotkeyBridgeSmokeSatisfied, diagnostics,
                "Agent 5 hotkey bridge smoke did not execute.");
        addDiagnostic(!notificationQueueSmokeSatisfied, diagnostics,
                "Agent 5 notification queue smoke did not execute.");
        addDiagnostic(!hudUpdateSmokeSatisfied, diagnostics,
                "Agent 5 HUD update smoke did not execute.");
        addDiagnostic(!cameraCinematicSmokeSatisfied, diagnostics,
                "Agent 5 camera/cinematic smoke did not execute.");
        addDiagnostic(!renderCoreLayoutSmokeSatisfied, diagnostics,
                "Agent 5 rendercore layout smoke did not execute.");
        addDiagnostic(!hostEventTranscriptSmokeSatisfied, diagnostics,
                "Agent 5 host event transcript smoke did not execute.");
        addDiagnostic(!physicalHotkeyPollingSmokeSatisfied, diagnostics,
                "Agent 5 physical hotkey polling smoke did not execute.");
        addDiagnostic(!liveSurfaceAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live surface acceptance smoke did not execute.");
        addDiagnostic(!physicalInputAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 physical input acceptance smoke did not execute.");
        addDiagnostic(!liveSurfaceRenderAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 live surface render acceptance smoke did not execute.");
        addDiagnostic(!uiHostInteractionStateAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 UI host interaction state acceptance smoke did not execute.");
        addDiagnostic(!uiHostEndToEndAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 UI host end-to-end acceptance smoke did not execute.");
        addDiagnostic(!mainMenuOptionActivationSmokeSatisfied, diagnostics,
                "Agent 5 main-menu option activation smoke did not execute.");
        addDiagnostic(!mainMenuEndToEndAcceptanceSmokeSatisfied, diagnostics,
                "Agent 5 main-menu end-to-end acceptance smoke did not execute.");
        addDiagnostic(!customMainMenuOpened, diagnostics, "Custom main menu did not appear.");
        addDiagnostic(!noScreenCrash, diagnostics, "Screen stack crashed or missed required screens.");

        EchoAgent5UiParityResult result = new EchoAgent5UiParityResult(
                "echo-standalone-runtime",
                uiReferenceAuditSmokeSatisfied,
                uiRuntimeEquivalenceAuditSmokeSatisfied,
                screenCorePrimitiveExecutionSmokeSatisfied,
                phase5UiParityAcceptanceSmokeSatisfied,
                liveClientAttachmentAcceptanceSmokeSatisfied,
                liveClientHostEvidenceAcceptanceSmokeSatisfied,
                headlessUiBridgeReadinessAcceptanceSmokeSatisfied,
                uiHudHostCallQueueReplaySmokeSatisfied,
                adapterCoreRuntimeBridgeGuardAcceptanceSmokeSatisfied,
                liveSurfaceRouteAcceptanceSmokeSatisfied,
                liveTextInputAcceptanceSmokeSatisfied,
                liveHudOverlayRouteAcceptanceSmokeSatisfied,
                liveMainMenuOverrideAcceptanceSmokeSatisfied,
                liveNotificationQueueAcceptanceSmokeSatisfied,
                liveHoloMapWikiNavigationAcceptanceSmokeSatisfied,
                liveSystemFlowAcceptanceSmokeSatisfied,
                liveCoreToolsAcceptanceSmokeSatisfied,
                liveMissionObjectiveAcceptanceSmokeSatisfied,
                liveInputFocusRoutingAcceptanceSmokeSatisfied,
                liveScreenStackStabilityAcceptanceSmokeSatisfied,
                liveVisualFrameAcceptanceSmokeSatisfied,
                liveModuleSurfaceCatalogAcceptanceSmokeSatisfied,
                liveRenderCallbackAcceptanceSmokeSatisfied,
                liveScreenOwnershipAcceptanceSmokeSatisfied,
                livePhysicalPollLoopAcceptanceSmokeSatisfied,
                livePhysicalEventTranscriptAcceptanceSmokeSatisfied,
                livePhysicalRouteEffectTranscriptAcceptanceSmokeSatisfied,
                liveRouteBoundTextCommandAcceptanceSmokeSatisfied,
                liveRouteBoundLensScanAcceptanceSmokeSatisfied,
                liveRouteBoundHudUpdateAcceptanceSmokeSatisfied,
                liveRouteBoundHoloMapWikiAcceptanceSmokeSatisfied,
                generatedScreenHostSmokeSatisfied,
                terminalOpened,
                terminalCommandExecuted,
                terminalEndToEndAcceptanceSmokeSatisfied,
                indexOpened,
                indexSearchExecuted,
                indexEndToEndAcceptanceSmokeSatisfied,
                lensScanExecuted,
                lensEndToEndAcceptanceSmokeSatisfied,
                hudUpdated,
                notificationQueueUpdated,
                notificationEndToEndAcceptanceSmokeSatisfied,
                missionLogOpened,
                missionLogTrackedActiveMission,
                missionLogUpdateSmokeSatisfied,
                missionLogEndToEndAcceptanceSmokeSatisfied,
                settingsOpened,
                settingsAppliedProfile,
                settingsEndToEndAcceptanceSmokeSatisfied,
                pauseFlowOpened,
                pauseFlowResumedPreviousScreen,
                pauseEndToEndAcceptanceSmokeSatisfied,
                deathRecoveryOpened,
                deathRecoveryActionExecuted,
                recoveryEndToEndAcceptanceSmokeSatisfied,
                screenCoreContractSatisfied,
                dataSourcesSatisfied,
                inputRoutingSatisfied,
                focusManagerSmokeSatisfied,
                textEditingSmokeSatisfied,
                mouseActivationSmokeSatisfied,
                listNavigationSmokeSatisfied,
                notificationDismissSmokeSatisfied,
                settingsAdjustmentSmokeSatisfied,
                pauseOptionActivationSmokeSatisfied,
                adapterUiHandlersSatisfied,
                holomapWikiHandlersSatisfied,
                surfaceRenderModelsSatisfied,
                surfaceRendererClassesSatisfied,
                inputActionRouterClassesSatisfied,
                screenHostModelsSatisfied,
                screenStackExecutionSmokeSatisfied,
                screenLifecycleSmokeSatisfied,
                screenLifecycleActionsSatisfied,
                moduleSurfaceRenderersSatisfied,
                allModuleSurfaceRenderersSatisfied,
                themeApplicationSmokeSatisfied,
                uiHostSmokeSnapshotsSatisfied,
                uiHostInteractionSmokeSatisfied,
                uiHostFullSurfaceInteractionsSatisfied,
                mainMenuOverrideSmokeSatisfied,
                mainMenuEndToEndAcceptanceSmokeSatisfied,
                hudOverlaySmokeSatisfied,
                hudOverlayEndToEndAcceptanceSmokeSatisfied,
                hotkeyBridgeSmokeSatisfied,
                notificationQueueSmokeSatisfied,
                mainMenuOptionActivationSmokeSatisfied,
                initialFocusSmokeSatisfied,
                hudUpdateSmokeSatisfied,
                cameraCinematicSmokeSatisfied,
                renderCoreLayoutSmokeSatisfied,
                hostEventTranscriptSmokeSatisfied,
                physicalHotkeyPollingSmokeSatisfied,
                liveSurfaceAcceptanceSmokeSatisfied,
                physicalInputAcceptanceSmokeSatisfied,
                liveSurfaceRenderAcceptanceSmokeSatisfied,
                uiHostInteractionStateAcceptanceSmokeSatisfied,
                uiHostEndToEndAcceptanceSmokeSatisfied,
                holoMapEndToEndAcceptanceSmokeSatisfied,
                wikiEndToEndAcceptanceSmokeSatisfied,
                holomapOpened,
                wikiOpened,
                customMainMenuOpened,
                noScreenCrash,
                visited,
                terminalShell.outputLines(),
                hudValues,
                missionLogValues,
                settingsValues,
                pauseFlowValues,
                deathRecoveryValues,
                screenCoreContractValues,
                dataSourceValues,
                notificationQueue.data(),
                diagnostics
        );
        services.register(EchoAgent5UiParityResult.class, result);
        return result;
    }

    private static String currentScreenId(EchoUiRuntimeResult ui, List<String> diagnostics) {
        return ui.screenStack().current()
                .map(EchoUiScreen::id)
                .orElseGet(() -> {
                    diagnostics.add("Screen stack is empty.");
                    return "";
                });
    }

    private static void addDiagnostic(boolean failed, List<String> diagnostics, String message) {
        if (failed) {
            diagnostics.add(message);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> strings(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private static List<String> checklist(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(entry -> (Map<String, Object>) entry)
                    .filter(entry -> Boolean.TRUE.equals(entry.get("passed")))
                    .map(entry -> String.valueOf(entry.get("id")))
                    .toList();
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) {
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    private static boolean containsPhysicalHotkeyEffects(Map<String, Object> smoke,
                                                         List<Map.Entry<String, String>> expectedRoutes) {
        List<Map<String, Object>> events = maps(smoke.get("events"));
        for (Map.Entry<String, String> expected : expectedRoutes) {
            String effectPrefix = "physical_hotkey:" + expected.getKey() + "->" + expected.getValue() + ":";
            boolean matched = events.stream()
                    .anyMatch(event -> String.valueOf(event.get("effect")).startsWith(effectPrefix)
                            && expected.getValue().equals(event.get("surface")));
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private static final class SearchScreen implements EchoUiScreen {
        private final String id;
        private final String title;
        private final String expectedQuery;
        private final String result;
        private String lastResult = "";

        private SearchScreen(String id, String title, String expectedQuery, String result) {
            this.id = id;
            this.title = title;
            this.expectedQuery = expectedQuery;
            this.result = result;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String title() {
            return title;
        }

        @Override
        public EchoUiSurface render(EchoUiContext context) {
            return new EchoUiSurface(id, title, List.of("Search: " + expectedQuery, lastResult), "index:search");
        }

        @Override
        public EchoUiInputResult handleInput(EchoUiInputEvent event, EchoUiContext context) {
            if (!expectedQuery.equalsIgnoreCase(event.value().trim())) {
                return EchoUiInputResult.ignored(id);
            }
            lastResult = result;
            return EchoUiInputResult.handled(id, List.of("index-search:" + expectedQuery));
        }

        private String lastResult() {
            return lastResult;
        }
    }

    private static final class ScanScreen implements EchoUiScreen {
        private final String id;
        private final String targetId;
        private final String result;
        private boolean scanned;
        private String scanResult = "";

        private ScanScreen(String id, String targetId, String result) {
            this.id = id;
            this.targetId = targetId;
            this.result = result;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String title() {
            return "Lens";
        }

        @Override
        public EchoUiSurface render(EchoUiContext context) {
            return new EchoUiSurface(id, title(), List.of(
                    "Target: " + targetId,
                    scanned ? "Scan: " + result : "Scan: waiting"
            ), "lens:scan");
        }

        @Override
        public EchoUiInputResult handleInput(EchoUiInputEvent event, EchoUiContext context) {
            if (!event.value().contains(targetId)) {
                return EchoUiInputResult.ignored(id);
            }
            scanned = true;
            scanResult = result;
            return EchoUiInputResult.handled(id, List.of("lens-scan:" + targetId));
        }

        private boolean scanned() {
            return scanned;
        }

        private String scanResult() {
            return scanResult;
        }
    }

    private static final class RecoveryScreen implements EchoUiScreen {
        private final String id;
        private final String recoveryPoint;
        private boolean recovered;

        private RecoveryScreen(String id, String recoveryPoint) {
            this.id = id;
            this.recoveryPoint = recoveryPoint;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String title() {
            return "Death Recovery";
        }

        @Override
        public EchoUiSurface render(EchoUiContext context) {
            return new EchoUiSurface(id, title(), List.of(
                    "Recovery point: " + recoveryPoint,
                    recovered ? "Status: " + EchoAgent5UiReference.RECOVERY_STATUS : "Status: WAITING"
            ), "recovery:" + EchoAgent5UiReference.RECOVERY_ACTION);
        }

        @Override
        public EchoUiInputResult handleInput(EchoUiInputEvent event, EchoUiContext context) {
            if (!EchoAgent5UiReference.RECOVERY_ACTION.equalsIgnoreCase(event.value().trim())) {
                return EchoUiInputResult.ignored(id);
            }
            recovered = true;
            return EchoUiInputResult.handled(id, List.of("death-recovery:" + recoveryPoint));
        }

        private boolean recovered() {
            return recovered;
        }
    }
}
