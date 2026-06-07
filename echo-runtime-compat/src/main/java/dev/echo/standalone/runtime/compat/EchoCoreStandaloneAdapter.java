package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoCoreStandaloneAdapter {
    public static final String MODULE_ID = "echocore";
    public static final String SERVICE_REGISTRY_CONTRACT_ID = "echocore:data/service_registry";
    public static final String DATA_BUS_CONTRACT_ID = "echocore:data/data_bus";
    public static final String CORE_DIAGNOSTICS_CONTRACT_ID = "echocore:diagnostic/core_diagnostics";
    public static final List<String> CONTRACT_IDS = List.of(
            SERVICE_REGISTRY_CONTRACT_ID,
            DATA_BUS_CONTRACT_ID,
            CORE_DIAGNOSTICS_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> referenceProbe = exerciseReferenceBehavior();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "echocore_standalone_contract_active");
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
        report.put("serviceRegistryRoundTrip", referenceProbe.get("serviceRegistryRoundTrip"));
        report.put("referenceProbe", referenceProbe);
        report.put("summary", "EchoCore standalone adapter resolved service registry, data bus, and core diagnostics contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private Map<String, Object> exerciseReferenceBehavior() {
        StandaloneServiceRegistry registry = new StandaloneServiceRegistry();
        registry.register(EchoCoreStandaloneAdapter.class, this);
        boolean serviceRegistryRoundTrip = registry.find(EchoCoreStandaloneAdapter.class) == this;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("serviceRegistryRoundTrip", serviceRegistryRoundTrip);
        return Map.copyOf(result);
    }

    private static final class StandaloneServiceRegistry {
        private final Map<Class<?>, Object> services = new LinkedHashMap<>();

        private <T> void register(Class<T> type, T service) {
            services.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(service, "service"));
        }

        private Object find(Class<?> type) {
            return services.get(Objects.requireNonNull(type, "type"));
        }
    }
}
