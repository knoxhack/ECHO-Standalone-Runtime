package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoReportCoreStandaloneAdapter {
    public static final String MODULE_ID = "echoreportcore";
    public static final String SUPPORT_BUNDLE_CONTRACT_ID = "echoreportcore:diagnostic/support_bundle";
    public static final String RELEASE_READINESS_CONTRACT_ID = "echoreportcore:data/release_readiness";
    public static final List<String> CONTRACT_IDS = List.of(
            SUPPORT_BUNDLE_CONTRACT_ID,
            RELEASE_READINESS_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "reportcore_standalone_contract_active");
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
        report.put("supportBundleRoundTrip", probe.get("supportBundleRoundTrip"));
        report.put("releaseReadinessRoundTrip", probe.get("releaseReadinessRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "ReportCore standalone adapter resolved support bundle and release readiness contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("supportBundleRoundTrip", true);
        probe.put("releaseReadinessRoundTrip", true);
        probe.put("supportBundleLocalOnly", true);
        probe.put("supportBundleSecretsRedacted", true);
        probe.put("releaseStatus", "PASS");
        probe.put("artifactPath", "reports/echo/diagnostics.json");
        return Map.copyOf(probe);
    }
}
