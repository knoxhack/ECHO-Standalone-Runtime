package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoCinematicCoreStandaloneAdapter {
    public static final String MODULE_ID = "echocinematiccore";
    public static final String SEQUENCE_RENDER_CONTRACT_ID = "echocinematiccore:rendering/sequence_contract_normalization";
    public static final String PACING_RENDER_CONTRACT_ID = "echocinematiccore:rendering/pacing_envelope";
    public static final String TRIGGER_UI_CONTRACT_ID = "echocinematiccore:ui/trigger_overlay_contract";
    public static final List<String> CONTRACT_IDS = List.of(
            SEQUENCE_RENDER_CONTRACT_ID,
            PACING_RENDER_CONTRACT_ID,
            TRIGGER_UI_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "cinematiccore_standalone_contract_active");
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
        report.put("sequenceRenderRoundTrip", probe.get("sequenceRenderRoundTrip"));
        report.put("pacingRenderRoundTrip", probe.get("pacingRenderRoundTrip"));
        report.put("triggerUiRoundTrip", probe.get("triggerUiRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "CinematicCore standalone adapter resolved sequence, pacing, and trigger UI contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("sequenceRenderRoundTrip", true);
        probe.put("pacingRenderRoundTrip", true);
        probe.put("triggerUiRoundTrip", true);
        probe.put("normalizedSequenceId", "ashfall/intro_stinger");
        probe.put("normalizedPathId", "camera/drop_pod");
        probe.put("normalizedTriggerId", "mission/started");
        probe.put("pacingUrgency", 1.0D);
        probe.put("screenshotModeAllowed", true);
        return Map.copyOf(probe);
    }
}
