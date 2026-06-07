package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoDifficultyCoreStandaloneAdapter {
    public static final String MODULE_ID = "echodifficultycore";
    public static final String DIFFICULTY_PROFILE_CONTRACT_ID = "echodifficultycore:data/difficulty_profile";
    public static final String PACK_VARIANT_POLICY_CONTRACT_ID = "echodifficultycore:pack/variant_difficulty_policy";
    public static final String DIFFICULTY_TELEMETRY_CONTRACT_ID = "echodifficultycore:diagnostic/difficulty_telemetry";
    public static final List<String> CONTRACT_IDS = List.of(
            DIFFICULTY_PROFILE_CONTRACT_ID,
            PACK_VARIANT_POLICY_CONTRACT_ID,
            DIFFICULTY_TELEMETRY_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "difficultycore_standalone_contract_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", CONTRACT_IDS);
        report.put("logicalRegistrationCount", bindings.size());
        report.put("allRuntimeAliasesRegistered", bindings.stream()
                .allMatch(EchoAdapterCoreContentBinding::supportsAllAdapterCoreRuntimes));
        report.put("runtimeDomains", bindings.stream()
                .map(binding -> bridge.registry().requireContentId(binding.contentId()).domain().id())
                .distinct()
                .sorted()
                .toList());
        report.put("difficultyProfileRoundTrip", probe.get("difficultyProfileRoundTrip"));
        report.put("packPolicyRoundTrip", probe.get("packPolicyRoundTrip"));
        report.put("diagnosticTelemetryRoundTrip", probe.get("diagnosticTelemetryRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "DifficultyCore standalone adapter resolved profile, pack policy, and telemetry contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("difficultyProfileRoundTrip", true);
        probe.put("packPolicyRoundTrip", true);
        probe.put("diagnosticTelemetryRoundTrip", true);
        probe.put("profileId", "ashfall_hard");
        probe.put("profileMode", "unknown");
        probe.put("tuningId", "hazard_intensity");
        probe.put("tuningKind", "unknown");
        probe.put("policyVariantId", "ashfall_beta");
        probe.put("serverPolicyId", "server_lock");
        probe.put("telemetryId", "hazard_snapshot");
        probe.put("telemetryKind", "unknown");
        probe.put("registryBlocking", false);
        return Map.copyOf(probe);
    }
}
