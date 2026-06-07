package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoRecoveryStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoRecoveryParitySmokeHarness {
    private EchoRuntimeEchoRecoveryParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativePlan = executeNativeReferencePlan("echo-native-m17");
        EchoRecoveryStandaloneAdapter standaloneAdapter = new EchoRecoveryStandaloneAdapter();
        Map<String, Object> standalonePlan = standaloneAdapter.executePlan("echo-native-m17");
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferencePlanPassed(nativePlan), "native Recovery reference plan should pass");
        require(standaloneAdapter.referencePlanPassed(standalonePlan), "standalone Recovery plan should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("recoveryPlanExecuted")),
                "standalone activation should execute recovery plan");
        require(nativePlan.get("adapterCoreContract").equals(standalonePlan.get("adapterCoreContract")),
                "native and standalone recovery contracts should match");
        require(nativePlan.get("graveSnapshot").equals(standalonePlan.get("graveSnapshot")),
                "native and standalone grave snapshots should match");
        require(nativePlan.get("itemRules").equals(standalonePlan.get("itemRules")),
                "native and standalone item rules should match");
        require(nativePlan.get("compassTarget").equals(standalonePlan.get("compassTarget")),
                "native and standalone compass targets should match");
        require(nativePlan.get("actions").equals(standalonePlan.get("actions")),
                "native and standalone recovery actions should match");
        require(nativePlan.get("safeMode").equals(standalonePlan.get("safeMode")),
                "native and standalone safe-mode summaries should match");

        System.out.println("echorecovery parity smoke PASS contract="
                + nativePlan.get("adapterCoreContract")
                + " grave="
                + EchoRecoveryStandaloneAdapter.REFERENCE_GRAVE_ID
                + " actions="
                + ((List<?>) nativePlan.get("actions")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Map<String, Object> executeNativeReferencePlan(String packId) {
        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("adapterCoreContract", EchoRecoveryStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        plan.put("service", "echorecovery:recovery_service");
        plan.put("recoveryPlanExecuted", true);
        plan.put("packId", packId == null || packId.isBlank() ? "unknown" : packId);
        plan.put("graveSnapshot", Map.of(
                "graveId", EchoRecoveryStandaloneAdapter.REFERENCE_GRAVE_ID,
                "ownerId", EchoRecoveryStandaloneAdapter.REFERENCE_OWNER_ID,
                "ownerName", "Ashfall Scout",
                "dimension", "echoashfallprotocol:wasteland_surface",
                "position", "118,64,-42",
                "graveTypeId", "echorecovery:ashfall_field_recovery_cache",
                "storedItemCount", 7,
                "xpStored", 23,
                "contaminated", true,
                "recovered", false
        ));
        plan.put("itemRules", List.of(
                itemRule("echoashfallprotocol:clean_water_bottle", "RECOVER_TO_GRAVE", false),
                itemRule("ashfall:return_keystone", "PROTECTED", true),
                itemRule("minecraft:rotten_flesh", "DESTROY_ON_DEATH", false)
        ));
        plan.put("compassTarget", Map.of(
                "targetId", EchoRecoveryStandaloneAdapter.REFERENCE_GRAVE_ID,
                "distanceBlocks", 214,
                "signalStatus", "weather-interference",
                "holoMapLayer", "echoholomap:layer/field_route"
        ));
        plan.put("actions", List.of(
                action("create_grave_snapshot", "CREATE_SNAPSHOT", "LOW", false),
                action("mark_compass_target", "MARK_RECOVERY_TARGET", "LOW", false),
                action("preserve_saves", "PRESERVE_SAVES", "LOW", false),
                action("recover_safe_items", "RECOVER_ITEMS", "MEDIUM", true)
        ));
        plan.put("safeMode", Map.of(
                "mode", "recovery",
                "automaticExecutionAllowed", false,
                "requiresConfirmation", true,
                "destructiveActions", 0
        ));
        plan.put("diagnostics", List.of(
                "recovery.grave.captured",
                "recovery.compass.targeted",
                "recovery.rules.applied",
                "recovery.confirmation.required"
        ));
        plan.put("referenceBehavior", "recovery_builds_field_recovery_plan");
        return Map.copyOf(plan);
    }

    private static boolean nativeReferencePlanPassed(Map<String, Object> plan) {
        return Boolean.TRUE.equals(plan.get("recoveryPlanExecuted"))
                && EchoRecoveryStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(plan.get("adapterCoreContract"))
                && String.valueOf(plan.get("graveSnapshot")).contains(EchoRecoveryStandaloneAdapter.REFERENCE_GRAVE_ID)
                && String.valueOf(plan.get("graveSnapshot")).contains("storedItemCount=7")
                && String.valueOf(plan.get("itemRules")).contains("ashfall:return_keystone")
                && String.valueOf(plan.get("compassTarget")).contains("weather-interference")
                && String.valueOf(plan.get("actions")).contains("RECOVER_ITEMS")
                && String.valueOf(plan.get("safeMode")).contains("requiresConfirmation=true")
                && String.valueOf(plan.get("diagnostics")).contains("recovery.rules.applied");
    }

    private static Map<String, Object> itemRule(String itemId, String result, boolean protectedItem) {
        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put("itemId", itemId);
        rule.put("result", result);
        rule.put("protected", protectedItem);
        return Map.copyOf(rule);
    }

    private static Map<String, Object> action(
            String id,
            String kind,
            String risk,
            boolean requiresConfirmation
    ) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("id", id);
        action.put("kind", kind);
        action.put("risk", risk);
        action.put("requiresConfirmation", requiresConfirmation);
        action.put("destructive", false);
        return Map.copyOf(action);
    }
}
