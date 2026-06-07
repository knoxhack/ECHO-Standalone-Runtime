package dev.echo.standalone.runtime.ui;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EchoAgent5UiParityResult(
        String runtimeId,
        boolean uiReferenceAuditSmokeSatisfied,
        boolean uiRuntimeEquivalenceAuditSmokeSatisfied,
        boolean screenCorePrimitiveExecutionSmokeSatisfied,
        boolean phase5UiParityAcceptanceSmokeSatisfied,
        boolean liveClientAttachmentAcceptanceSmokeSatisfied,
        boolean liveClientHostEvidenceAcceptanceSmokeSatisfied,
        boolean headlessUiBridgeReadinessAcceptanceSmokeSatisfied,
        boolean uiHudHostCallQueueReplaySmokeSatisfied,
        boolean adapterCoreRuntimeBridgeGuardAcceptanceSmokeSatisfied,
        boolean liveSurfaceRouteAcceptanceSmokeSatisfied,
        boolean liveTextInputAcceptanceSmokeSatisfied,
        boolean liveHudOverlayRouteAcceptanceSmokeSatisfied,
        boolean liveMainMenuOverrideAcceptanceSmokeSatisfied,
        boolean liveNotificationQueueAcceptanceSmokeSatisfied,
        boolean liveHoloMapWikiNavigationAcceptanceSmokeSatisfied,
        boolean liveSystemFlowAcceptanceSmokeSatisfied,
        boolean liveCoreToolsAcceptanceSmokeSatisfied,
        boolean liveMissionObjectiveAcceptanceSmokeSatisfied,
        boolean liveInputFocusRoutingAcceptanceSmokeSatisfied,
        boolean liveScreenStackStabilityAcceptanceSmokeSatisfied,
        boolean liveVisualFrameAcceptanceSmokeSatisfied,
        boolean liveModuleSurfaceCatalogAcceptanceSmokeSatisfied,
        boolean liveRenderCallbackAcceptanceSmokeSatisfied,
        boolean liveScreenOwnershipAcceptanceSmokeSatisfied,
        boolean livePhysicalPollLoopAcceptanceSmokeSatisfied,
        boolean livePhysicalEventTranscriptAcceptanceSmokeSatisfied,
        boolean livePhysicalRouteEffectTranscriptAcceptanceSmokeSatisfied,
        boolean liveRouteBoundTextCommandAcceptanceSmokeSatisfied,
        boolean liveRouteBoundLensScanAcceptanceSmokeSatisfied,
        boolean liveRouteBoundHudUpdateAcceptanceSmokeSatisfied,
        boolean liveRouteBoundHoloMapWikiAcceptanceSmokeSatisfied,
        boolean generatedScreenHostSmokeSatisfied,
        boolean terminalOpened,
        boolean terminalCommandExecuted,
        boolean terminalEndToEndAcceptanceSmokeSatisfied,
        boolean indexOpened,
        boolean indexSearchExecuted,
        boolean indexEndToEndAcceptanceSmokeSatisfied,
        boolean lensScanExecuted,
        boolean lensEndToEndAcceptanceSmokeSatisfied,
        boolean hudUpdated,
        boolean notificationQueueUpdated,
        boolean notificationEndToEndAcceptanceSmokeSatisfied,
        boolean missionLogOpened,
        boolean missionLogTrackedActiveMission,
        boolean missionLogUpdateSmokeSatisfied,
        boolean missionLogEndToEndAcceptanceSmokeSatisfied,
        boolean settingsOpened,
        boolean settingsAppliedProfile,
        boolean settingsEndToEndAcceptanceSmokeSatisfied,
        boolean pauseFlowOpened,
        boolean pauseFlowResumedPreviousScreen,
        boolean pauseEndToEndAcceptanceSmokeSatisfied,
        boolean deathRecoveryOpened,
        boolean deathRecoveryActionExecuted,
        boolean recoveryEndToEndAcceptanceSmokeSatisfied,
        boolean screenCoreContractSatisfied,
        boolean dataSourcesSatisfied,
        boolean inputRoutingSatisfied,
        boolean focusManagerSmokeSatisfied,
        boolean textEditingSmokeSatisfied,
        boolean mouseActivationSmokeSatisfied,
        boolean listNavigationSmokeSatisfied,
        boolean notificationDismissSmokeSatisfied,
        boolean settingsAdjustmentSmokeSatisfied,
        boolean pauseOptionActivationSmokeSatisfied,
        boolean adapterUiHandlersSatisfied,
        boolean holomapWikiHandlersSatisfied,
        boolean surfaceRenderModelsSatisfied,
        boolean surfaceRendererClassesSatisfied,
        boolean inputActionRouterClassesSatisfied,
        boolean screenHostModelsSatisfied,
        boolean screenStackExecutionSmokeSatisfied,
        boolean screenLifecycleSmokeSatisfied,
        boolean screenLifecycleActionsSatisfied,
        boolean moduleSurfaceRenderersSatisfied,
        boolean allModuleSurfaceRenderersSatisfied,
        boolean themeApplicationSmokeSatisfied,
        boolean uiHostSmokeSnapshotsSatisfied,
        boolean uiHostInteractionSmokeSatisfied,
        boolean uiHostFullSurfaceInteractionsSatisfied,
        boolean mainMenuOverrideSmokeSatisfied,
        boolean mainMenuEndToEndAcceptanceSmokeSatisfied,
        boolean hudOverlaySmokeSatisfied,
        boolean hudOverlayEndToEndAcceptanceSmokeSatisfied,
        boolean hotkeyBridgeSmokeSatisfied,
        boolean notificationQueueSmokeSatisfied,
        boolean mainMenuOptionActivationSmokeSatisfied,
        boolean initialFocusSmokeSatisfied,
        boolean hudUpdateSmokeSatisfied,
        boolean cameraCinematicSmokeSatisfied,
        boolean renderCoreLayoutSmokeSatisfied,
        boolean hostEventTranscriptSmokeSatisfied,
        boolean physicalHotkeyPollingSmokeSatisfied,
        boolean liveSurfaceAcceptanceSmokeSatisfied,
        boolean physicalInputAcceptanceSmokeSatisfied,
        boolean liveSurfaceRenderAcceptanceSmokeSatisfied,
        boolean uiHostInteractionStateAcceptanceSmokeSatisfied,
        boolean uiHostEndToEndAcceptanceSmokeSatisfied,
        boolean holoMapEndToEndAcceptanceSmokeSatisfied,
        boolean wikiEndToEndAcceptanceSmokeSatisfied,
        boolean holomapOpened,
        boolean wikiOpened,
        boolean customMainMenuOpened,
        boolean noScreenCrash,
        List<String> visitedScreenIds,
        List<String> terminalOutput,
        Map<String, Object> hudValues,
        Map<String, Object> missionLogValues,
        Map<String, Object> settingsValues,
        Map<String, Object> pauseFlowValues,
        Map<String, Object> deathRecoveryValues,
        Map<String, Object> screenCoreContractValues,
        Map<String, Object> dataSourceValues,
        List<Map<String, Object>> notifications,
        List<String> diagnostics
) {
    public EchoAgent5UiParityResult {
        runtimeId = requireText(runtimeId, "runtimeId");
        Objects.requireNonNull(visitedScreenIds, "visitedScreenIds");
        Objects.requireNonNull(terminalOutput, "terminalOutput");
        Objects.requireNonNull(hudValues, "hudValues");
        Objects.requireNonNull(missionLogValues, "missionLogValues");
        Objects.requireNonNull(settingsValues, "settingsValues");
        Objects.requireNonNull(pauseFlowValues, "pauseFlowValues");
        Objects.requireNonNull(deathRecoveryValues, "deathRecoveryValues");
        Objects.requireNonNull(screenCoreContractValues, "screenCoreContractValues");
        Objects.requireNonNull(dataSourceValues, "dataSourceValues");
        Objects.requireNonNull(notifications, "notifications");
        Objects.requireNonNull(diagnostics, "diagnostics");
        visitedScreenIds = List.copyOf(visitedScreenIds);
        terminalOutput = List.copyOf(terminalOutput);
        hudValues = Map.copyOf(hudValues);
        missionLogValues = Map.copyOf(missionLogValues);
        settingsValues = Map.copyOf(settingsValues);
        pauseFlowValues = Map.copyOf(pauseFlowValues);
        deathRecoveryValues = Map.copyOf(deathRecoveryValues);
        screenCoreContractValues = Map.copyOf(screenCoreContractValues);
        dataSourceValues = Map.copyOf(dataSourceValues);
        notifications = List.copyOf(notifications);
        diagnostics = List.copyOf(diagnostics);
    }

    public boolean passed() {
        return uiReferenceAuditSmokeSatisfied
                && uiRuntimeEquivalenceAuditSmokeSatisfied
                && screenCorePrimitiveExecutionSmokeSatisfied
                && phase5UiParityAcceptanceSmokeSatisfied
                && liveClientAttachmentAcceptanceSmokeSatisfied
                && liveClientHostEvidenceAcceptanceSmokeSatisfied
                && headlessUiBridgeReadinessAcceptanceSmokeSatisfied
                && uiHudHostCallQueueReplaySmokeSatisfied
                && adapterCoreRuntimeBridgeGuardAcceptanceSmokeSatisfied
                && liveSurfaceRouteAcceptanceSmokeSatisfied
                && liveTextInputAcceptanceSmokeSatisfied
                && liveHudOverlayRouteAcceptanceSmokeSatisfied
                && liveMainMenuOverrideAcceptanceSmokeSatisfied
                && liveNotificationQueueAcceptanceSmokeSatisfied
                && liveHoloMapWikiNavigationAcceptanceSmokeSatisfied
                && liveSystemFlowAcceptanceSmokeSatisfied
                && liveCoreToolsAcceptanceSmokeSatisfied
                && liveMissionObjectiveAcceptanceSmokeSatisfied
                && liveInputFocusRoutingAcceptanceSmokeSatisfied
                && liveScreenStackStabilityAcceptanceSmokeSatisfied
                && liveVisualFrameAcceptanceSmokeSatisfied
                && liveModuleSurfaceCatalogAcceptanceSmokeSatisfied
                && liveRenderCallbackAcceptanceSmokeSatisfied
                && liveScreenOwnershipAcceptanceSmokeSatisfied
                && livePhysicalPollLoopAcceptanceSmokeSatisfied
                && livePhysicalEventTranscriptAcceptanceSmokeSatisfied
                && livePhysicalRouteEffectTranscriptAcceptanceSmokeSatisfied
                && liveRouteBoundTextCommandAcceptanceSmokeSatisfied
                && liveRouteBoundLensScanAcceptanceSmokeSatisfied
                && liveRouteBoundHudUpdateAcceptanceSmokeSatisfied
                && liveRouteBoundHoloMapWikiAcceptanceSmokeSatisfied
                && generatedScreenHostSmokeSatisfied
                && terminalOpened
                && terminalCommandExecuted
                && terminalEndToEndAcceptanceSmokeSatisfied
                && indexOpened
                && indexSearchExecuted
                && indexEndToEndAcceptanceSmokeSatisfied
                && lensScanExecuted
                && lensEndToEndAcceptanceSmokeSatisfied
                && hudUpdated
                && notificationQueueUpdated
                && notificationEndToEndAcceptanceSmokeSatisfied
                && missionLogOpened
                && missionLogTrackedActiveMission
                && missionLogUpdateSmokeSatisfied
                && missionLogEndToEndAcceptanceSmokeSatisfied
                && settingsOpened
                && settingsAppliedProfile
                && settingsEndToEndAcceptanceSmokeSatisfied
                && pauseFlowOpened
                && pauseFlowResumedPreviousScreen
                && pauseEndToEndAcceptanceSmokeSatisfied
                && deathRecoveryOpened
                && deathRecoveryActionExecuted
                && recoveryEndToEndAcceptanceSmokeSatisfied
                && screenCoreContractSatisfied
                && dataSourcesSatisfied
                && inputRoutingSatisfied
                && focusManagerSmokeSatisfied
                && textEditingSmokeSatisfied
                && mouseActivationSmokeSatisfied
                && listNavigationSmokeSatisfied
                && notificationDismissSmokeSatisfied
                && settingsAdjustmentSmokeSatisfied
                && pauseOptionActivationSmokeSatisfied
                && adapterUiHandlersSatisfied
                && holomapWikiHandlersSatisfied
                && surfaceRenderModelsSatisfied
                && surfaceRendererClassesSatisfied
                && inputActionRouterClassesSatisfied
                && screenHostModelsSatisfied
                && screenStackExecutionSmokeSatisfied
                && screenLifecycleSmokeSatisfied
                && screenLifecycleActionsSatisfied
                && moduleSurfaceRenderersSatisfied
                && allModuleSurfaceRenderersSatisfied
                && themeApplicationSmokeSatisfied
                && uiHostSmokeSnapshotsSatisfied
                && uiHostInteractionSmokeSatisfied
                && uiHostFullSurfaceInteractionsSatisfied
                && mainMenuOverrideSmokeSatisfied
                && mainMenuEndToEndAcceptanceSmokeSatisfied
                && hudOverlaySmokeSatisfied
                && hudOverlayEndToEndAcceptanceSmokeSatisfied
                && hotkeyBridgeSmokeSatisfied
                && notificationQueueSmokeSatisfied
                && mainMenuOptionActivationSmokeSatisfied
                && initialFocusSmokeSatisfied
                && hudUpdateSmokeSatisfied
                && cameraCinematicSmokeSatisfied
                && renderCoreLayoutSmokeSatisfied
                && hostEventTranscriptSmokeSatisfied
                && physicalHotkeyPollingSmokeSatisfied
                && liveSurfaceAcceptanceSmokeSatisfied
                && physicalInputAcceptanceSmokeSatisfied
                && liveSurfaceRenderAcceptanceSmokeSatisfied
                && uiHostInteractionStateAcceptanceSmokeSatisfied
                && uiHostEndToEndAcceptanceSmokeSatisfied
                && holoMapEndToEndAcceptanceSmokeSatisfied
                && wikiEndToEndAcceptanceSmokeSatisfied
                && holomapOpened
                && wikiOpened
                && customMainMenuOpened
                && noScreenCrash
                && diagnostics.isEmpty();
    }

    public List<String> passedChecks() {
        return passed() ? EchoAgent5UiReference.parityChecks() : EchoAgent5UiReference.parityChecks().stream()
                .filter(this::checkPassed)
                .toList();
    }

    private boolean checkPassed(String check) {
        return switch (check) {
            case "ui_reference_audit_smoke_executes" -> uiReferenceAuditSmokeSatisfied;
            case "ui_runtime_equivalence_audit_smoke_executes" -> uiRuntimeEquivalenceAuditSmokeSatisfied;
            case "screencore_primitive_execution_smoke_executes" -> screenCorePrimitiveExecutionSmokeSatisfied;
            case "phase5_ui_parity_acceptance_smoke_executes" -> phase5UiParityAcceptanceSmokeSatisfied;
            case "live_client_attachment_acceptance_smoke_executes" -> liveClientAttachmentAcceptanceSmokeSatisfied;
            case "live_client_host_evidence_acceptance_smoke_executes" -> liveClientHostEvidenceAcceptanceSmokeSatisfied;
            case "headless_ui_bridge_readiness_acceptance_smoke_executes" -> headlessUiBridgeReadinessAcceptanceSmokeSatisfied;
            case "ui_hud_host_call_queue_replay_smoke_executes" -> uiHudHostCallQueueReplaySmokeSatisfied;
            case "adaptercore_runtime_bridge_guard_acceptance_smoke_executes" -> adapterCoreRuntimeBridgeGuardAcceptanceSmokeSatisfied;
            case "live_surface_route_acceptance_smoke_executes" -> liveSurfaceRouteAcceptanceSmokeSatisfied;
            case "live_text_input_acceptance_smoke_executes" -> liveTextInputAcceptanceSmokeSatisfied;
            case "live_hud_overlay_route_acceptance_smoke_executes" -> liveHudOverlayRouteAcceptanceSmokeSatisfied;
            case "live_main_menu_override_acceptance_smoke_executes" -> liveMainMenuOverrideAcceptanceSmokeSatisfied;
            case "live_notification_queue_acceptance_smoke_executes" -> liveNotificationQueueAcceptanceSmokeSatisfied;
            case "live_holomap_wiki_navigation_acceptance_smoke_executes" -> liveHoloMapWikiNavigationAcceptanceSmokeSatisfied;
            case "live_system_flow_acceptance_smoke_executes" -> liveSystemFlowAcceptanceSmokeSatisfied;
            case "live_core_tools_acceptance_smoke_executes" -> liveCoreToolsAcceptanceSmokeSatisfied;
            case "live_mission_objective_acceptance_smoke_executes" -> liveMissionObjectiveAcceptanceSmokeSatisfied;
            case "live_input_focus_routing_acceptance_smoke_executes" -> liveInputFocusRoutingAcceptanceSmokeSatisfied;
            case "live_screen_stack_stability_acceptance_smoke_executes" -> liveScreenStackStabilityAcceptanceSmokeSatisfied;
            case "live_visual_frame_acceptance_smoke_executes" -> liveVisualFrameAcceptanceSmokeSatisfied;
            case "live_module_surface_catalog_acceptance_smoke_executes" -> liveModuleSurfaceCatalogAcceptanceSmokeSatisfied;
            case "live_render_callback_acceptance_smoke_executes" -> liveRenderCallbackAcceptanceSmokeSatisfied;
            case "live_screen_ownership_acceptance_smoke_executes" -> liveScreenOwnershipAcceptanceSmokeSatisfied;
            case "live_physical_poll_loop_acceptance_smoke_executes" -> livePhysicalPollLoopAcceptanceSmokeSatisfied;
            case "live_physical_event_transcript_acceptance_smoke_executes" -> livePhysicalEventTranscriptAcceptanceSmokeSatisfied;
            case "live_physical_route_effect_transcript_acceptance_smoke_executes" -> livePhysicalRouteEffectTranscriptAcceptanceSmokeSatisfied;
            case "live_route_bound_text_command_acceptance_smoke_executes" -> liveRouteBoundTextCommandAcceptanceSmokeSatisfied;
            case "live_route_bound_lens_scan_acceptance_smoke_executes" -> liveRouteBoundLensScanAcceptanceSmokeSatisfied;
            case "live_route_bound_hud_update_acceptance_smoke_executes" -> liveRouteBoundHudUpdateAcceptanceSmokeSatisfied;
            case "live_route_bound_holomap_wiki_acceptance_smoke_executes" -> liveRouteBoundHoloMapWikiAcceptanceSmokeSatisfied;
            case "generated_screen_host_smoke_executes" -> generatedScreenHostSmokeSatisfied;
            case "terminal_opens" -> terminalOpened;
            case "terminal_command_executes" -> terminalCommandExecuted;
            case "terminal_end_to_end_acceptance_smoke_executes" -> terminalEndToEndAcceptanceSmokeSatisfied;
            case "index_opens_and_searches" -> indexOpened && indexSearchExecuted;
            case "index_end_to_end_acceptance_smoke_executes" -> indexEndToEndAcceptanceSmokeSatisfied;
            case "lens_scans_target" -> lensScanExecuted;
            case "lens_end_to_end_acceptance_smoke_executes" -> lensEndToEndAcceptanceSmokeSatisfied;
            case "hud_updates_health_hazard_mission" -> hudUpdated;
            case "notification_queue_dispatches" -> notificationQueueUpdated;
            case "notification_end_to_end_acceptance_smoke_executes" -> notificationEndToEndAcceptanceSmokeSatisfied;
            case "mission_log_opens_and_tracks_active_mission" -> missionLogOpened && missionLogTrackedActiveMission;
            case "mission_log_update_smoke_executes" -> missionLogUpdateSmokeSatisfied;
            case "mission_log_end_to_end_acceptance_smoke_executes" -> missionLogEndToEndAcceptanceSmokeSatisfied;
            case "settings_opens_and_applies_profile" -> settingsOpened && settingsAppliedProfile;
            case "settings_end_to_end_acceptance_smoke_executes" -> settingsEndToEndAcceptanceSmokeSatisfied;
            case "pause_flow_opens_and_resumes_previous_screen" -> pauseFlowOpened && pauseFlowResumedPreviousScreen;
            case "pause_end_to_end_acceptance_smoke_executes" -> pauseEndToEndAcceptanceSmokeSatisfied;
            case "death_recovery_screen_opens_and_recovers" -> deathRecoveryOpened && deathRecoveryActionExecuted;
            case "recovery_end_to_end_acceptance_smoke_executes" -> recoveryEndToEndAcceptanceSmokeSatisfied;
            case "screencore_contract_primitives_execute" -> screenCoreContractSatisfied;
            case "ui_data_sources_drive_all_agent5_surfaces" -> dataSourcesSatisfied;
            case "screen_focus_and_input_routing_execute" -> inputRoutingSatisfied;
            case "focus_manager_smoke_executes" -> focusManagerSmokeSatisfied;
            case "text_editing_smoke_executes" -> textEditingSmokeSatisfied;
            case "mouse_activation_smoke_executes" -> mouseActivationSmokeSatisfied;
            case "list_navigation_smoke_executes" -> listNavigationSmokeSatisfied;
            case "notification_dismiss_smoke_executes" -> notificationDismissSmokeSatisfied;
            case "settings_adjustment_smoke_executes" -> settingsAdjustmentSmokeSatisfied;
            case "pause_option_activation_smoke_executes" -> pauseOptionActivationSmokeSatisfied;
            case "adapter_ui_handlers_execute" -> adapterUiHandlersSatisfied;
            case "holomap_wiki_handlers_execute" -> holomapWikiHandlersSatisfied;
            case "native_surface_render_models_execute" -> surfaceRenderModelsSatisfied;
            case "surface_renderer_classes_execute" -> surfaceRendererClassesSatisfied;
            case "input_action_router_classes_execute" -> inputActionRouterClassesSatisfied;
            case "screen_host_models_execute" -> screenHostModelsSatisfied;
            case "screen_stack_execution_smoke_executes" -> screenStackExecutionSmokeSatisfied;
            case "screen_lifecycle_smoke_executes" -> screenLifecycleSmokeSatisfied;
            case "screen_lifecycle_actions_execute" -> screenLifecycleActionsSatisfied;
            case "module_surface_renderers_execute" -> moduleSurfaceRenderersSatisfied;
            case "all_module_surface_renderers_execute" -> allModuleSurfaceRenderersSatisfied;
            case "theme_application_smoke_executes" -> themeApplicationSmokeSatisfied;
            case "ui_host_smoke_snapshots_execute" -> uiHostSmokeSnapshotsSatisfied;
            case "ui_host_interaction_smoke_executes" -> uiHostInteractionSmokeSatisfied;
            case "ui_host_full_surface_interactions_execute" -> uiHostFullSurfaceInteractionsSatisfied;
            case "main_menu_override_smoke_executes" -> mainMenuOverrideSmokeSatisfied;
            case "main_menu_end_to_end_acceptance_smoke_executes" -> mainMenuEndToEndAcceptanceSmokeSatisfied;
            case "hud_overlay_smoke_executes" -> hudOverlaySmokeSatisfied;
            case "hud_overlay_end_to_end_acceptance_smoke_executes" -> hudOverlayEndToEndAcceptanceSmokeSatisfied;
            case "hotkey_bridge_smoke_executes" -> hotkeyBridgeSmokeSatisfied;
            case "notification_queue_smoke_executes" -> notificationQueueSmokeSatisfied;
            case "main_menu_option_activation_smoke_executes" -> mainMenuOptionActivationSmokeSatisfied;
            case "initial_focus_smoke_executes" -> initialFocusSmokeSatisfied;
            case "hud_update_smoke_executes" -> hudUpdateSmokeSatisfied;
            case "camera_cinematic_smoke_executes" -> cameraCinematicSmokeSatisfied;
            case "rendercore_layout_smoke_executes" -> renderCoreLayoutSmokeSatisfied;
            case "host_event_transcript_smoke_executes" -> hostEventTranscriptSmokeSatisfied;
            case "physical_hotkey_polling_smoke_executes" -> physicalHotkeyPollingSmokeSatisfied;
            case "live_surface_acceptance_smoke_executes" -> liveSurfaceAcceptanceSmokeSatisfied;
            case "physical_input_acceptance_smoke_executes" -> physicalInputAcceptanceSmokeSatisfied;
            case "live_surface_render_acceptance_smoke_executes" -> liveSurfaceRenderAcceptanceSmokeSatisfied;
            case "ui_host_interaction_state_acceptance_smoke_executes" -> uiHostInteractionStateAcceptanceSmokeSatisfied;
            case "ui_host_end_to_end_acceptance_smoke_executes" -> uiHostEndToEndAcceptanceSmokeSatisfied;
            case "holomap_end_to_end_acceptance_smoke_executes" -> holoMapEndToEndAcceptanceSmokeSatisfied;
            case "wiki_end_to_end_acceptance_smoke_executes" -> wikiEndToEndAcceptanceSmokeSatisfied;
            case "holomap_opens" -> holomapOpened;
            case "wiki_page_opens" -> wikiOpened;
            case "custom_main_menu_appears" -> customMainMenuOpened;
            case "no_screen_crash" -> noScreenCrash;
            default -> false;
        };
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
