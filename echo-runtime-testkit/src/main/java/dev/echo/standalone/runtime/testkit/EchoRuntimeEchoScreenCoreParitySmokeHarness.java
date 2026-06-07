package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoScreenCoreStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoScreenCoreParitySmokeHarness {
    private EchoRuntimeEchoScreenCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativeComposition = executeNativeReferenceComposition(
                EchoScreenCoreStandaloneAdapter.REFERENCE_SCREEN_ID,
                EchoScreenCoreStandaloneAdapter.REFERENCE_ACTION_ID);
        EchoScreenCoreStandaloneAdapter standaloneAdapter = new EchoScreenCoreStandaloneAdapter();
        Map<String, Object> standaloneComposition = standaloneAdapter.executeComposition(
                EchoScreenCoreStandaloneAdapter.REFERENCE_SCREEN_ID,
                EchoScreenCoreStandaloneAdapter.REFERENCE_ACTION_ID);
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferenceCompositionPassed(nativeComposition),
                "native ScreenCore reference composition should resolve the field ops dashboard");
        require(standaloneAdapter.referenceCompositionPassed(standaloneComposition),
                "standalone ScreenCore composition should resolve the field ops dashboard");
        require(Boolean.TRUE.equals(standaloneActivation.get("screenCompositionExecuted")),
                "standalone activation should execute screen composition");
        require(nativeComposition.get("adapterCoreContract").equals(standaloneComposition.get("adapterCoreContract")),
                "native and standalone screen contracts should match");
        require(nativeComposition.get("screenId").equals(standaloneComposition.get("screenId")),
                "native and standalone screen ids should match");
        require(nativeComposition.get("layout").equals(standaloneComposition.get("layout")),
                "native and standalone layouts should match");
        require(nativeComposition.get("bindings").equals(standaloneComposition.get("bindings")),
                "native and standalone bindings should match");
        require(nativeComposition.get("components").equals(standaloneComposition.get("components")),
                "native and standalone components should match");
        require(nativeComposition.get("actions").equals(standaloneComposition.get("actions")),
                "native and standalone actions should match");
        require(nativeComposition.get("inputResult").equals(standaloneComposition.get("inputResult")),
                "native and standalone input result should match");

        System.out.println("echoscreencore parity smoke PASS contract="
                + nativeComposition.get("adapterCoreContract")
                + " screen="
                + nativeComposition.get("screenId")
                + " components="
                + ((List<?>) nativeComposition.get("components")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Map<String, Object> executeNativeReferenceComposition(String screenId, String actionId) {
        Map<String, Object> composition = new LinkedHashMap<>();
        composition.put("adapterCoreContract", EchoScreenCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        composition.put("service", "echoscreencore:screen_data_service");
        composition.put("compositionExecuted", true);
        composition.put("screenId", screenId);
        composition.put("focusedControl", "field_ops.primary_action");
        composition.put("themeId", "echothemecore:ashfall");
        composition.put("layout", Map.of(
                "root", "sc_app_shell",
                "columns", 2,
                "safeArea", "echohudcore:hud/runtime_snapshot"
        ));
        composition.put("bindings", List.of(
                binding("mission.title", "Build a power cell"),
                binding("hazard.summary", "Ash storm warning"),
                binding("route.target", "echoholomap:layer/field_route")
        ));
        composition.put("components", List.of(
                component("header", "sc_page_header", "top"),
                component("mission", "mission_row", "left"),
                component("route", "sc_detail_panel", "right"),
                component("actions", "sc_action_strip", "bottom")
        ));
        composition.put("actions", List.of(
                action("open_index", "echoindex:recipe_search/index_query"),
                action("scan_target", "echolens:scanner/field_inspection"),
                action("open_route", "echoholomap:layer/field_route")
        ));
        composition.put("selectedAction", actionId);
        composition.put("inputResult", Map.of(
                "handled", true,
                "route", "echoindex:recipe_search/index_query",
                "focusAfter", "field_ops.primary_action"
        ));
        composition.put("diagnostics", List.of("screen.layout.ready", "screen.bindings.resolved"));
        composition.put("referenceBehavior", "screencore_composes_field_ops_dashboard");
        return Map.copyOf(composition);
    }

    private static boolean nativeReferenceCompositionPassed(Map<String, Object> composition) {
        return Boolean.TRUE.equals(composition.get("compositionExecuted"))
                && EchoScreenCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(composition.get("adapterCoreContract"))
                && EchoScreenCoreStandaloneAdapter.REFERENCE_SCREEN_ID.equals(composition.get("screenId"))
                && EchoScreenCoreStandaloneAdapter.REFERENCE_ACTION_ID.equals(composition.get("selectedAction"))
                && String.valueOf(composition.get("bindings")).contains("echoholomap:layer/field_route")
                && String.valueOf(composition.get("actions")).contains("echoindex:recipe_search/index_query")
                && String.valueOf(composition.get("inputResult")).contains("handled=true");
    }

    private static Map<String, String> binding(String key, String value) {
        Map<String, String> binding = new LinkedHashMap<>();
        binding.put("key", key);
        binding.put("value", value);
        return Map.copyOf(binding);
    }

    private static Map<String, String> component(String id, String type, String slot) {
        Map<String, String> component = new LinkedHashMap<>();
        component.put("id", id);
        component.put("type", type);
        component.put("slot", slot);
        return Map.copyOf(component);
    }

    private static Map<String, String> action(String id, String target) {
        Map<String, String> action = new LinkedHashMap<>();
        action.put("id", id);
        action.put("target", target);
        return Map.copyOf(action);
    }
}
