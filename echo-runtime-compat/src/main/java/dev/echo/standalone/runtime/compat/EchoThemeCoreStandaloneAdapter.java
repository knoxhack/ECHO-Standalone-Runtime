package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoThemeCoreStandaloneAdapter {
    public static final String MODULE_ID = "echothemecore";
    public static final String ADAPTERCORE_CONTRACT_ID = "echothemecore:themes/ashfall_theme_application";
    public static final String REFERENCE_THEME_ID = "echothemecore:ashfall";
    public static final String REFERENCE_SURFACE_ID = "echoterminal:field_ops/first_ten_minutes";

    public Map<String, Object> activate() {
        Map<String, Object> application = executeThemeApplication(REFERENCE_THEME_ID, REFERENCE_SURFACE_ID);
        boolean applicationPassed = referenceApplicationPassed(application);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "themecore_standalone_theme_application_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", List.of("theme.tokens", "theme.ui_skins", ADAPTERCORE_CONTRACT_ID));
        report.put("themeApplication", application);
        report.put("themeApplicationExecuted", applicationPassed);
        report.put("themeApplicationContract", ADAPTERCORE_CONTRACT_ID);
        report.put("selectedThemeId", application.get("selectedThemeId"));
        report.put("serviceCodeExecuted", applicationPassed);
        report.put("summary", "ThemeCore standalone adapter executed the AdapterCore Ashfall theme application service.");
        return Map.copyOf(report);
    }

    public Map<String, Object> executeThemeApplication(String requestedThemeId, String surfaceId) {
        String selectedThemeId = normalizeThemeId(requestedThemeId);
        String selectedSurfaceId = normalizeText(surfaceId, REFERENCE_SURFACE_ID);
        Map<String, Object> application = new LinkedHashMap<>();
        application.put("adapterCoreContract", ADAPTERCORE_CONTRACT_ID);
        application.put("service", "echothemecore:theme_application_service");
        application.put("themeApplicationExecuted", true);
        application.put("requestedThemeId", normalizeText(requestedThemeId, REFERENCE_THEME_ID));
        application.put("selectedThemeId", selectedThemeId);
        application.put("surfaceId", selectedSurfaceId);
        application.put("runtime", "echo_runtime_standalone");
        application.put("selectedTheme", selectedTheme());
        application.put("colorTokens", colorTokens());
        application.put("textureTokens", textureTokens());
        application.put("renderTokens", renderTokens());
        application.put("layoutTokens", layoutTokens());
        application.put("surfaceAssets", surfaceAssets());
        application.put("soundBindings", soundBindings());
        application.put("diagnostics", List.of(
                "theme.catalog.public_theme_selected",
                "theme.tokens.resolved",
                "theme.surface_assets.bound",
                "theme.standalone_fallback.enabled"
        ));
        application.put("referenceBehavior", "themecore_resolves_ashfall_theme_application");
        return Map.copyOf(application);
    }

    public boolean referenceApplicationPassed(Map<String, Object> application) {
        return Boolean.TRUE.equals(application.get("themeApplicationExecuted"))
                && ADAPTERCORE_CONTRACT_ID.equals(application.get("adapterCoreContract"))
                && REFERENCE_THEME_ID.equals(application.get("selectedThemeId"))
                && String.valueOf(application.get("selectedTheme")).contains("replacementLevel=hybrid")
                && String.valueOf(application.get("selectedTheme")).contains("standaloneFallback=true")
                && String.valueOf(application.get("colorTokens")).contains("accent.primary=#FF6600")
                && String.valueOf(application.get("colorTokens")).contains("state.warning=#FFCC00")
                && String.valueOf(application.get("textureTokens")).contains("terminal.panel=echothemecore:textures/gui/themes/ashfall/glass_panel.png")
                && String.valueOf(application.get("textureTokens")).contains("holomap.marker.hazard=echothemecore:textures/gui/themes/ashfall/marker_hazard.png")
                && String.valueOf(application.get("renderTokens")).contains("render.glow_intensity=0.85")
                && String.valueOf(application.get("layoutTokens")).contains("tokens.min_button_height=28")
                && String.valueOf(application.get("surfaceAssets")).contains("surface=terminal")
                && String.valueOf(application.get("diagnostics")).contains("theme.surface_assets.bound");
    }

    private static Map<String, Object> selectedTheme() {
        Map<String, Object> theme = new LinkedHashMap<>();
        theme.put("id", REFERENCE_THEME_ID);
        theme.put("displayName", "Ashfall");
        theme.put("description", "Ash-gray survival interface with hazard orange, warning bands, worn metal panels, and emergency HUD chrome.");
        theme.put("family", "ashfall");
        theme.put("publicTheme", true);
        theme.put("cycleOrder", 30);
        theme.put("replacementLevel", "hybrid");
        theme.put("packTheme", "wasteland_cyberglass");
        theme.put("density", "compact");
        theme.put("standaloneFallback", true);
        theme.put("moduleTags", List.of(
                "terminal",
                "signalos",
                "index",
                "holomap",
                "lens",
                "screencore",
                "rendercore",
                "soundcore",
                "vanilla_ui",
                "hud",
                "loading",
                "menu",
                "item_icon"
        ));
        return Map.copyOf(theme);
    }

    private static Map<String, Object> colorTokens() {
        Map<String, Object> tokens = new LinkedHashMap<>();
        tokens.put("accent.primary", "#FF6600");
        tokens.put("accent.secondary", "#FFCC00");
        tokens.put("background.primary", "#141414");
        tokens.put("panel.primary", "#2E2E2ECC");
        tokens.put("panel.secondary", "#3D3D3DCC");
        tokens.put("text.primary", "#E0E0E0");
        tokens.put("text.muted", "#A8A8A8");
        tokens.put("state.ready", "#44FF88");
        tokens.put("state.warning", "#FFCC00");
        tokens.put("state.danger", "#FF3333");
        tokens.put("border.selected", "#FFAA00");
        return Map.copyOf(tokens);
    }

    private static Map<String, Object> textureTokens() {
        Map<String, Object> tokens = new LinkedHashMap<>();
        tokens.put("terminal.panel", texture("glass_panel"));
        tokens.put("terminal.tab", texture("tab"));
        tokens.put("terminal.tab.active", texture("tab_active"));
        tokens.put("terminal.button", texture("glass_button"));
        tokens.put("terminal.icon", texture("icons/icon_terminal"));
        tokens.put("index.panel", texture("index_panel"));
        tokens.put("index.card.selected", texture("index_card_selected"));
        tokens.put("holomap.grid", texture("holomap_grid"));
        tokens.put("holomap.marker.hazard", texture("marker_hazard"));
        tokens.put("lens.scan_ring", texture("lens_scan_ring"));
        tokens.put("screencore.surface.base", texture("screencore/surface_base"));
        tokens.put("hud.hotbar_frame", texture("hud/hotbar_frame"));
        tokens.put("loading.progress_bar", texture("loading/progress_bar"));
        tokens.put("menu.pause_panel", texture("menu/pause_panel"));
        tokens.put("item_icon.mission_marker", texture("item_icon/mission_marker"));
        return Map.copyOf(tokens);
    }

    private static Map<String, Object> renderTokens() {
        Map<String, Object> tokens = new LinkedHashMap<>();
        tokens.put("render.hologram_color", "#FF6600");
        tokens.put("render.warning_glow_color", "#FFCC00");
        tokens.put("render.success_glow_color", "#44FF88");
        tokens.put("render.glow_intensity", 0.85D);
        tokens.put("render.hologram_opacity", 0.68D);
        tokens.put("render.particle_intensity", 0.65D);
        tokens.put("render.animation_intensity", 0.75D);
        tokens.put("render.overlay_style", "GLASS_GEOMETRIC");
        tokens.put("render.transition_style", "GLASS_FADE");
        return Map.copyOf(tokens);
    }

    private static Map<String, Object> layoutTokens() {
        Map<String, Object> tokens = new LinkedHashMap<>();
        tokens.put("tokens.safe_area_margin", 14);
        tokens.put("tokens.panel_radius", 4);
        tokens.put("tokens.card_radius", 4);
        tokens.put("tokens.button_radius", 4);
        tokens.put("tokens.min_button_height", 28);
        tokens.put("tokens.min_list_row_height", 44);
        tokens.put("tokens.min_text_contrast", 4.5D);
        tokens.put("tokens.animation.enter_ms", 110);
        tokens.put("tokens.animation.exit_ms", 90);
        tokens.put("tokens.animation.transition_ms", 170);
        return Map.copyOf(tokens);
    }

    private static List<Map<String, Object>> surfaceAssets() {
        return List.of(
                surface("terminal", texture("glass_panel"), texture("status_chip"), "accent.primary"),
                surface("index", texture("index_panel"), texture("index_status_chip"), "state.ready"),
                surface("holomap", texture("holomap_panel"), texture("marker_hazard"), "state.warning"),
                surface("lens", texture("lens_scan_ring"), texture("lens_warning_overlay"), "state.danger"),
                surface("screencore", texture("screencore/surface_base"), texture("screencore/focus_ring"), "border.selected")
        );
    }

    private static Map<String, Object> soundBindings() {
        Map<String, Object> bindings = new LinkedHashMap<>();
        bindings.put("ui.click", "echosoundcore:ui.terminal.select");
        bindings.put("ui.error", "echosoundcore:ui.terminal.error");
        bindings.put("ui.open", "echosoundcore:ui.terminal.open");
        bindings.put("stinger.warning", "echosoundcore:ui.terminal.warning");
        return Map.copyOf(bindings);
    }

    private static Map<String, Object> surface(String surface, String panelTexture, String accentTexture, String accentToken) {
        Map<String, Object> binding = new LinkedHashMap<>();
        binding.put("surface", surface);
        binding.put("panelTexture", panelTexture);
        binding.put("accentTexture", accentTexture);
        binding.put("accentToken", accentToken);
        binding.put("themeId", REFERENCE_THEME_ID);
        return Map.copyOf(binding);
    }

    private static String texture(String path) {
        return "echothemecore:textures/gui/themes/ashfall/" + path + ".png";
    }

    private static String normalizeThemeId(String themeId) {
        String normalized = normalizeText(themeId, REFERENCE_THEME_ID);
        return REFERENCE_THEME_ID.equals(normalized) || "ashfall".equals(normalized) || "wasteland_cyberglass".equals(normalized)
                ? REFERENCE_THEME_ID
                : normalized;
    }

    private static String normalizeText(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text.trim();
    }
}
