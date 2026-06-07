package dev.echo.standalone.runtime.ui;

import java.util.List;

public final class EchoAgent5UiReference {
    public static final String MAIN_MENU_SCREEN = "echo:main_menu";
    public static final String TERMINAL_SCREEN = "echoterminal:terminal";
    public static final String INDEX_SCREEN = "echoindex:index";
    public static final String LENS_SCREEN = "echolens:lens";
    public static final String HUD_LAYER = "echohudcore:hud";
    public static final String MISSION_LOG_SCREEN = "echoscreencore:mission_log";
    public static final String SETTINGS_SCREEN = "echoscreencore:settings";
    public static final String PAUSE_FLOW_SCREEN = "echoscreencore:pause_flow";
    public static final String DEATH_RECOVERY_SCREEN = "echoscreencore:death_recovery";
    public static final String HOLOMAP_SCREEN = "echoholomap:holomap";
    public static final String WIKI_SCREEN = "echowiki:wiki";
    public static final String NOTIFICATION_QUEUE = "echonotificationcore:queue";
    public static final String SIGNALOS_SCREEN = "signalos:terminal";
    public static final String ASHFALL_DRONE_SCREEN = "echoashfallprotocol:drone";

    public static final String TERMINAL_COMMAND = "status";
    public static final String INDEX_QUERY = "ashfall";
    public static final String LENS_TARGET = "drop_pod_terminal";
    public static final String ACTIVE_MISSION_ID = "echoashfallprotocol:secure_crash_outpost";
    public static final String ACTIVE_MISSION_TITLE = "Anchor Pod Outpost";
    public static final String ACTIVE_MISSION_OBJECTIVE = "Place an Ash Campfire near the crash site";
    public static final String ACTIVE_MISSION_STATUS = "TRACKED";
    public static final String ACTIVE_MISSION_UPDATED_STATUS = "UPDATED";
    public static final String ACTIVE_MISSION_UPDATE_LINE = "Place an Ash Campfire near the crash site";
    public static final String SETTINGS_PROFILE = "ashfall-accessible";
    public static final String SETTINGS_THEME = "ashfall-agent5";
    public static final String SETTINGS_INPUT_MODE = "keyboard_mouse";
    public static final String PAUSE_RESUME_TARGET = WIKI_SCREEN;
    public static final String RECOVERY_ACTION = "recover";
    public static final String RECOVERY_POINT = "echorecovery:ashfall_field_recovery_cache";
    public static final String RECOVERY_STATUS = "RECOVERED";

    private EchoAgent5UiReference() {
    }

    public static List<String> screenIds() {
        return List.of(
                MAIN_MENU_SCREEN,
                TERMINAL_SCREEN,
                INDEX_SCREEN,
                LENS_SCREEN,
                HUD_LAYER,
                NOTIFICATION_QUEUE,
                MISSION_LOG_SCREEN,
                SETTINGS_SCREEN,
                PAUSE_FLOW_SCREEN,
                DEATH_RECOVERY_SCREEN,
                HOLOMAP_SCREEN,
                WIKI_SCREEN,
                SIGNALOS_SCREEN,
                ASHFALL_DRONE_SCREEN
        );
    }

    public static List<String> parityChecks() {
        return List.of(
                "ui_reference_audit_smoke_executes",
                "ui_runtime_equivalence_audit_smoke_executes",
                "screencore_primitive_execution_smoke_executes",
                "phase5_ui_parity_acceptance_smoke_executes",
                "live_client_attachment_acceptance_smoke_executes",
                "live_client_host_evidence_acceptance_smoke_executes",
                "headless_ui_bridge_readiness_acceptance_smoke_executes",
                "ui_hud_host_call_queue_replay_smoke_executes",
                "adaptercore_runtime_bridge_guard_acceptance_smoke_executes",
                "live_surface_route_acceptance_smoke_executes",
                "live_text_input_acceptance_smoke_executes",
                "live_hud_overlay_route_acceptance_smoke_executes",
                "live_main_menu_override_acceptance_smoke_executes",
                "live_notification_queue_acceptance_smoke_executes",
                "live_holomap_wiki_navigation_acceptance_smoke_executes",
                "live_system_flow_acceptance_smoke_executes",
                "live_core_tools_acceptance_smoke_executes",
                "live_mission_objective_acceptance_smoke_executes",
                "live_input_focus_routing_acceptance_smoke_executes",
                "live_screen_stack_stability_acceptance_smoke_executes",
                "live_visual_frame_acceptance_smoke_executes",
                "live_module_surface_catalog_acceptance_smoke_executes",
                "live_render_callback_acceptance_smoke_executes",
                "live_screen_ownership_acceptance_smoke_executes",
                "live_physical_poll_loop_acceptance_smoke_executes",
                "live_physical_event_transcript_acceptance_smoke_executes",
                "live_physical_route_effect_transcript_acceptance_smoke_executes",
                "live_route_bound_text_command_acceptance_smoke_executes",
                "live_route_bound_lens_scan_acceptance_smoke_executes",
                "live_route_bound_hud_update_acceptance_smoke_executes",
                "live_route_bound_holomap_wiki_acceptance_smoke_executes",
                "generated_screen_host_smoke_executes",
                "terminal_opens",
                "terminal_command_executes",
                "terminal_end_to_end_acceptance_smoke_executes",
                "index_opens_and_searches",
                "index_end_to_end_acceptance_smoke_executes",
                "lens_scans_target",
                "lens_end_to_end_acceptance_smoke_executes",
                "hud_updates_health_hazard_mission",
                "notification_queue_dispatches",
                "notification_end_to_end_acceptance_smoke_executes",
                "mission_log_opens_and_tracks_active_mission",
                "mission_log_update_smoke_executes",
                "mission_log_end_to_end_acceptance_smoke_executes",
                "settings_opens_and_applies_profile",
                "settings_end_to_end_acceptance_smoke_executes",
                "pause_flow_opens_and_resumes_previous_screen",
                "pause_end_to_end_acceptance_smoke_executes",
                "death_recovery_screen_opens_and_recovers",
                "recovery_end_to_end_acceptance_smoke_executes",
                "screencore_contract_primitives_execute",
                "ui_data_sources_drive_all_agent5_surfaces",
                "screen_focus_and_input_routing_execute",
                "focus_manager_smoke_executes",
                "text_editing_smoke_executes",
                "mouse_activation_smoke_executes",
                "list_navigation_smoke_executes",
                "notification_dismiss_smoke_executes",
                "settings_adjustment_smoke_executes",
                "pause_option_activation_smoke_executes",
                "adapter_ui_handlers_execute",
                "holomap_wiki_handlers_execute",
                "native_surface_render_models_execute",
                "surface_renderer_classes_execute",
                "input_action_router_classes_execute",
                "screen_host_models_execute",
                "screen_stack_execution_smoke_executes",
                "screen_lifecycle_smoke_executes",
                "screen_lifecycle_actions_execute",
                "module_surface_renderers_execute",
                "all_module_surface_renderers_execute",
                "theme_application_smoke_executes",
                "ui_host_smoke_snapshots_execute",
                "ui_host_interaction_smoke_executes",
                "ui_host_full_surface_interactions_execute",
                "main_menu_override_smoke_executes",
                "main_menu_end_to_end_acceptance_smoke_executes",
                "hud_overlay_smoke_executes",
                "hud_overlay_end_to_end_acceptance_smoke_executes",
                "hotkey_bridge_smoke_executes",
                "notification_queue_smoke_executes",
                "main_menu_option_activation_smoke_executes",
                "initial_focus_smoke_executes",
                "hud_update_smoke_executes",
                "camera_cinematic_smoke_executes",
                "rendercore_layout_smoke_executes",
                "host_event_transcript_smoke_executes",
                "physical_hotkey_polling_smoke_executes",
                "live_surface_acceptance_smoke_executes",
                "physical_input_acceptance_smoke_executes",
                "live_surface_render_acceptance_smoke_executes",
                "ui_host_interaction_state_acceptance_smoke_executes",
                "ui_host_end_to_end_acceptance_smoke_executes",
                "holomap_end_to_end_acceptance_smoke_executes",
                "wiki_end_to_end_acceptance_smoke_executes",
                "holomap_opens",
                "wiki_page_opens",
                "custom_main_menu_appears",
                "no_screen_crash"
        );
    }
}
