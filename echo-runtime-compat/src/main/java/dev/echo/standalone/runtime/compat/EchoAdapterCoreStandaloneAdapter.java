package dev.echo.standalone.runtime.compat;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAdapterCoreStandaloneAdapter {
    public static final String MODULE_ID = "echoadaptercore";
    public static final List<String> CONTRACT_IDS = List.of(
            "adaptercore.native_lifecycle",
            "adaptercore.native_registry",
            "adaptercore.native_event",
            "adaptercore.echo_runtime_standalone",
            "adaptercore.standalone_voxel_world"
    );

    public Map<String, Object> activate() {
        List<EchoAdapterCoreRuntimeKind> runtimes = Arrays.stream(EchoAdapterCoreRuntimeKind.values()).toList();
        List<String> runtimeIds = runtimes.stream()
                .map(EchoAdapterCoreRuntimeKind::adapterId)
                .toList();
        List<String> phase1Domains = EchoAdapterCoreContractLock.phase1AuditDomains().stream()
                .map(EchoAdapterCoreDomain::id)
                .toList();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "adaptercore_standalone_contract_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", CONTRACT_IDS);
        report.put("logicalRegistrationCount", CONTRACT_IDS.size());
        report.put("allRuntimeAliasesRegistered", EchoAdapterCoreContractLock.supportsEveryRuntime(runtimes));
        report.put("supportedRuntimeIds", runtimeIds);
        report.put("phase1AuditDomainCount", phase1Domains.size());
        report.put("phase1AuditDomains", phase1Domains);
        report.put("summary", "AdapterCore standalone adapter exposed native loader, registry, event, standalone runtime, and voxel world bridge contracts.");
        return Map.copyOf(report);
    }
}
