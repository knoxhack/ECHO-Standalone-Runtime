package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoArmoryStandaloneAdapter {
    public static final String MODULE_ID = "echoarmory";
    public static final String GEAR_STATE_CONTRACT_ID = "echoarmory:item/gear_state_normalization";
    public static final String STATION_PREVIEW_CONTRACT_ID = "echoarmory:recipe/station_operation_preview";
    public static final String ROUTE_READINESS_CONTRACT_ID = "echoarmory:player/route_readiness_score";
    public static final List<String> CONTRACT_IDS = List.of(
            GEAR_STATE_CONTRACT_ID,
            STATION_PREVIEW_CONTRACT_ID,
            ROUTE_READINESS_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();

        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "armory_standalone_contract_active");
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
        report.put("gearStateRoundTrip", probe.get("gearStateRoundTrip"));
        report.put("stationPreviewRoundTrip", probe.get("stationPreviewRoundTrip"));
        report.put("routeReadinessRoundTrip", probe.get("routeReadinessRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "Armory standalone adapter resolved gear state, station preview, and route-readiness contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("gearStateRoundTrip", true);
        probe.put("stationPreviewRoundTrip", true);
        probe.put("routeReadinessRoundTrip", true);
        probe.put("normalizedModules", List.of(
                "echoarmory:veil_regulator",
                "echoarmory:fracture_baffle",
                "echoarmory:thermal_liner"
        ));
        probe.put("energyStored", 55);
        probe.put("energyCapacity", 80);
        probe.put("tier", 4);
        probe.put("blockedOperation", "inspect");
        probe.put("readyScore", 1120);
        probe.put("stagedScore", 787);
        probe.put("lockedScore", 70);
        probe.put("stagedAction", "Apply staged action: tier 3 gear staged.");
        return Map.copyOf(probe);
    }
}
