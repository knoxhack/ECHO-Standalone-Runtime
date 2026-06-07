package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoDifficultyCoreStandaloneAdapter;

import java.util.Map;

public final class EchoDifficultyCoreAdapterCoreParitySmokeHarness {
    private EchoDifficultyCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoDifficultyCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "DifficultyCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "DifficultyCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("difficultyProfileRoundTrip")),
                "DifficultyCore standalone adapter should preserve profile and tuning behavior");
        require(Boolean.TRUE.equals(activation.get("packPolicyRoundTrip")),
                "DifficultyCore standalone adapter should preserve pack and server policy behavior");
        require(Boolean.TRUE.equals(activation.get("diagnosticTelemetryRoundTrip")),
                "DifficultyCore standalone adapter should preserve telemetry behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require("ashfall_hard".equals(probe.get("profileId"))
                        && "unknown".equals(probe.get("profileMode"))
                        && "hazard_intensity".equals(probe.get("tuningId")),
                "DifficultyCore data contract should preserve normalized profile, mode, and tuning ids");
        require("ashfall_beta".equals(probe.get("policyVariantId"))
                        && "server_lock".equals(probe.get("serverPolicyId")),
                "DifficultyCore pack contract should preserve variant and server policy ids");
        require("hazard_snapshot".equals(probe.get("telemetryId"))
                        && "unknown".equals(probe.get("telemetryKind"))
                        && Boolean.FALSE.equals(probe.get("registryBlocking")),
                "DifficultyCore diagnostic contract should preserve telemetry defaults and non-blocking registry");

        requireEntry(bridge, EchoDifficultyCoreStandaloneAdapter.DIFFICULTY_PROFILE_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "difficultycore.data.difficulty_profile");
        requireEntry(bridge, EchoDifficultyCoreStandaloneAdapter.PACK_VARIANT_POLICY_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PACKS, "difficultycore.packs.variant_difficulty_policy");
        requireEntry(bridge, EchoDifficultyCoreStandaloneAdapter.DIFFICULTY_TELEMETRY_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC, EchoAdapterCoreDomain.DIAGNOSTICS, "difficultycore.diagnostics.difficulty_telemetry");
        System.out.println("difficultycore adaptercore parity smoke PASS contracts="
                + EchoDifficultyCoreStandaloneAdapter.CONTRACT_IDS.size());
    }

    private static void requireEntry(
            EchoAdapterCoreStandaloneContentBridge bridge,
            String contentId,
            EchoAdapterCoreContentKind contentKind,
            EchoAdapterCoreDomain domain,
            String adapterKey
    ) {
        EchoAdapterCoreRegistryEntry entry = bridge.registry().requireContentId(contentId);
        require(entry.contentKind() == contentKind,
                contentId + " should use content kind " + contentKind);
        require(entry.domain() == domain,
                contentId + " should use AdapterCore domain " + domain.id());
        require(entry.binding().adapterKey().equals(adapterKey),
                contentId + " should expose stable adapter key " + adapterKey);
        for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
            require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                    contentId + " has unregistered runtime alias " + runtimeKind.adapterId());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
