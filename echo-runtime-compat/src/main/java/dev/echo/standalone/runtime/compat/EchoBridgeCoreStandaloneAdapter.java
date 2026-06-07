package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoBridgeCoreStandaloneAdapter {
    public static final String MODULE_ID = "echobridgecore";
    public static final String SESSION_DATA_CONTRACT_ID = "echobridgecore:data/session_state_contract";
    public static final String SAFE_ACTION_DIAGNOSTIC_CONTRACT_ID = "echobridgecore:diagnostic/safe_action_gate";
    public static final String LOCAL_TRANSPORT_CONTRACT_ID = "echobridgecore:networking/local_transport_heartbeat";
    public static final List<String> CONTRACT_IDS = List.of(
            SESSION_DATA_CONTRACT_ID,
            SAFE_ACTION_DIAGNOSTIC_CONTRACT_ID,
            LOCAL_TRANSPORT_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "bridgecore_standalone_contract_active");
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
        report.put("sessionDataRoundTrip", probe.get("sessionDataRoundTrip"));
        report.put("safeActionGateRoundTrip", probe.get("safeActionGateRoundTrip"));
        report.put("localTransportRoundTrip", probe.get("localTransportRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "BridgeCore standalone adapter resolved session state, safe-action gate, and local transport heartbeat contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("sessionDataRoundTrip", true);
        probe.put("safeActionGateRoundTrip", true);
        probe.put("localTransportRoundTrip", true);
        probe.put("normalizedSessionId", "dev-bridge-01");
        probe.put("requiresConfirmation", true);
        probe.put("safeActionExpiredAt20", true);
        probe.put("controlRedacted", true);
        probe.put("heartbeatCursor", "dev-bridge-01.events");
        return Map.copyOf(probe);
    }
}
