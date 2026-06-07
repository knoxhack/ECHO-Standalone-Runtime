package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoCurseCoreStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoCurseCoreParitySmokeHarness {
    private EchoRuntimeEchoCurseCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativeState = executeNativeReferenceStateResolution("echo-native-m17");
        EchoCurseCoreStandaloneAdapter standaloneAdapter = new EchoCurseCoreStandaloneAdapter();
        Map<String, Object> standaloneState = standaloneAdapter.executeStateResolution("echo-native-m17");
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferenceStateResolutionPassed(nativeState), "native CurseCore reference state should pass");
        require(standaloneAdapter.referenceStateResolutionPassed(standaloneState),
                "standalone CurseCore state should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("curseStateResolved")),
                "standalone activation should execute curse state resolution");
        require(nativeState.get("adapterCoreContract").equals(standaloneState.get("adapterCoreContract")),
                "native and standalone curse contracts should match");
        require(nativeState.get("playerId").equals(standaloneState.get("playerId")),
                "native and standalone player ids should match");
        require(nativeState.get("operations").equals(standaloneState.get("operations")),
                "native and standalone curse operations should match");
        require(nativeState.get("activeCurses").equals(standaloneState.get("activeCurses")),
                "native and standalone active curse states should match");
        require(nativeState.get("contractSummary").equals(standaloneState.get("contractSummary")),
                "native and standalone contract summaries should match");
        require(nativeState.get("cleansingPlan").equals(standaloneState.get("cleansingPlan")),
                "native and standalone cleansing plans should match");
        require(nativeState.get("tickEffects").equals(standaloneState.get("tickEffects")),
                "native and standalone tick effects should match");
        require(nativeState.get("events").equals(standaloneState.get("events")),
                "native and standalone curse events should match");

        System.out.println("echocursecore parity smoke PASS contract="
                + nativeState.get("adapterCoreContract")
                + " target="
                + EchoCurseCoreStandaloneAdapter.BLOOD_DEBT
                + " effects="
                + ((List<?>) nativeState.get("tickEffects")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Map<String, Object> executeNativeReferenceStateResolution(String packId) {
        Map<String, Object> resolution = new LinkedHashMap<>();
        resolution.put("adapterCoreContract", EchoCurseCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        resolution.put("service", "echocursecore:curse_state_service");
        resolution.put("curseStateResolved", true);
        resolution.put("packId", packId == null || packId.isBlank() ? "unknown" : packId);
        resolution.put("playerId", EchoCurseCoreStandaloneAdapter.REFERENCE_PLAYER_ID);
        resolution.put("operations", List.of(
                operation("apply_echo_rot", EchoCurseCoreStandaloneAdapter.ECHO_ROT, "signal_backlash", 3, 0, 3),
                operation("bind_blood_debt", EchoCurseCoreStandaloneAdapter.BLOOD_DEBT, "contract", 2, 0, 2),
                operation("cleanse_echo_rot", EchoCurseCoreStandaloneAdapter.ECHO_ROT, "ritual_cleansing", -1, 3, 2),
                operation("pay_blood_debt", EchoCurseCoreStandaloneAdapter.BLOOD_DEBT, "ledger_stabilize", -25, 80, 55)
        ));
        resolution.put("activeCurses", List.of(
                curseState(EchoCurseCoreStandaloneAdapter.ECHO_ROT, 2, false, 0),
                curseState(EchoCurseCoreStandaloneAdapter.BLOOD_DEBT, 2, true, 55)
        ));
        resolution.put("contractSummary", Map.of(
                "contractCount", 1,
                "totalDebt", 55,
                "contractResistance", 35,
                "severReadyCount", 0
        ));
        resolution.put("cleansingPlan", Map.of(
                "planCode", 2,
                "action", "pay_contract_debt",
                "target", EchoCurseCoreStandaloneAdapter.BLOOD_DEBT,
                "readiness", 10
        ));
        resolution.put("tickEffects", List.of(
                effect("minecraft:darkness", 45, 0, EchoCurseCoreStandaloneAdapter.ECHO_ROT),
                effect("minecraft:weakness", 80, 0, EchoCurseCoreStandaloneAdapter.ECHO_ROT),
                effect("minecraft:strength", 80, 0, EchoCurseCoreStandaloneAdapter.BLOOD_DEBT)
        ));
        resolution.put("events", List.of(
                event("curse.gained", EchoCurseCoreStandaloneAdapter.ECHO_ROT, 3, "signal_backlash"),
                event("curse.gained", EchoCurseCoreStandaloneAdapter.BLOOD_DEBT, 2, "contract"),
                event("curse.cleansed", EchoCurseCoreStandaloneAdapter.ECHO_ROT, 2, "ritual_cleansing"),
                event("curse.debt_paid", EchoCurseCoreStandaloneAdapter.BLOOD_DEBT, 55, "ledger_stabilize")
        ));
        resolution.put("diagnostics", List.of(
                "curse.state.loaded",
                "curse.contract.debt_recalculated",
                "curse.cleansing.plan_selected",
                "curse.tick.effects_resolved"
        ));
        resolution.put("referenceBehavior", "cursecore_resolves_persistent_player_curse_state");
        return Map.copyOf(resolution);
    }

    private static boolean nativeReferenceStateResolutionPassed(Map<String, Object> resolution) {
        return Boolean.TRUE.equals(resolution.get("curseStateResolved"))
                && EchoCurseCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(resolution.get("adapterCoreContract"))
                && EchoCurseCoreStandaloneAdapter.REFERENCE_PLAYER_ID.equals(resolution.get("playerId"))
                && String.valueOf(resolution.get("operations")).contains("cleanse_echo_rot")
                && String.valueOf(resolution.get("activeCurses")).contains("stage=2")
                && String.valueOf(resolution.get("contractSummary")).contains("totalDebt=55")
                && String.valueOf(resolution.get("contractSummary")).contains("contractResistance=35")
                && String.valueOf(resolution.get("cleansingPlan")).contains("pay_contract_debt")
                && String.valueOf(resolution.get("cleansingPlan")).contains("readiness=10")
                && String.valueOf(resolution.get("tickEffects")).contains("minecraft:weakness")
                && String.valueOf(resolution.get("diagnostics")).contains("curse.tick.effects_resolved");
    }

    private static Map<String, Object> operation(
            String id,
            String curseId,
            String source,
            int delta,
            int before,
            int after
    ) {
        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("id", id);
        operation.put("curseId", curseId);
        operation.put("source", source);
        operation.put("delta", delta);
        operation.put("before", before);
        operation.put("after", after);
        return Map.copyOf(operation);
    }

    private static Map<String, Object> curseState(String curseId, int stage, boolean contractBound, int debt) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("curseId", curseId);
        state.put("stage", stage);
        state.put("contractBound", contractBound);
        state.put("debt", debt);
        return Map.copyOf(state);
    }

    private static Map<String, Object> effect(String effectId, int durationTicks, int amplifier, String curseId) {
        Map<String, Object> effect = new LinkedHashMap<>();
        effect.put("effectId", effectId);
        effect.put("durationTicks", durationTicks);
        effect.put("amplifier", amplifier);
        effect.put("curseId", curseId);
        return Map.copyOf(effect);
    }

    private static Map<String, Object> event(String eventId, String curseId, int value, String source) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", eventId);
        event.put("curseId", curseId);
        event.put("value", value);
        event.put("source", source);
        return Map.copyOf(event);
    }
}
