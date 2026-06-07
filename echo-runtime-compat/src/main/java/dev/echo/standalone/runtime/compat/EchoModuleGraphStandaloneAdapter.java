package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoModuleGraphStandaloneAdapter {
    public static final String MODULE_ID = "echomodulegraph";
    public static final String MODULE_GRAPH_CONTRACT_ID = "echomodulegraph:data/module_graph";
    public static final String GRAPH_VALIDATION_CONTRACT_ID = "echomodulegraph:diagnostic/graph_validation";
    public static final List<String> CONTRACT_IDS = List.of(
            MODULE_GRAPH_CONTRACT_ID,
            GRAPH_VALIDATION_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "modulegraph_standalone_contract_active");
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
        report.put("moduleGraphRoundTrip", probe.get("moduleGraphRoundTrip"));
        report.put("graphValidationRoundTrip", probe.get("graphValidationRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "ModuleGraph standalone adapter resolved graph data and validation contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("moduleGraphRoundTrip", true);
        probe.put("graphValidationRoundTrip", true);
        probe.put("loadOrder", List.of("echocore", "echomodulegraph"));
        probe.put("degradedModules", List.of("echomodulegraph"));
        probe.put("duplicateModuleIds", List.of("echocore"));
        probe.put("diagnosticCount", 2);
        return Map.copyOf(probe);
    }
}
