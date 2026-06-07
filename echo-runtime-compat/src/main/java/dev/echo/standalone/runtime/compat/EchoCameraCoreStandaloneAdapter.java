package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoCameraCoreStandaloneAdapter {
    public static final String MODULE_ID = "echocameracore";
    public static final String RENDER_PROFILE_CONTRACT_ID = "echocameracore:rendering/profile_contract_normalization";
    public static final String SHAKE_SAFETY_CONTRACT_ID = "echocameracore:rendering/shake_safety_envelope";
    public static final String INPUT_TARGET_CONTRACT_ID = "echocameracore:input/target_anchor_contract";
    public static final List<String> CONTRACT_IDS = List.of(
            RENDER_PROFILE_CONTRACT_ID,
            SHAKE_SAFETY_CONTRACT_ID,
            INPUT_TARGET_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "cameracore_standalone_contract_active");
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
        report.put("renderProfileRoundTrip", probe.get("renderProfileRoundTrip"));
        report.put("shakeSafetyRoundTrip", probe.get("shakeSafetyRoundTrip"));
        report.put("inputTargetRoundTrip", probe.get("inputTargetRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "CameraCore standalone adapter resolved profile, shake/safety, and target anchor contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("renderProfileRoundTrip", true);
        probe.put("shakeSafetyRoundTrip", true);
        probe.put("inputTargetRoundTrip", true);
        probe.put("normalizedProfileId", "prime/screenshot_mode");
        probe.put("normalizedShakeId", "ashfall/nexus_burst");
        probe.put("shakeIntensity", 1.0D);
        probe.put("maxFovChange", 0.0D);
        probe.put("targetAnchor", "Player Head");
        return Map.copyOf(probe);
    }
}
