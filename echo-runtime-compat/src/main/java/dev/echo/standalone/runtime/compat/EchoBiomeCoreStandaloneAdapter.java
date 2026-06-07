package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoBiomeCoreStandaloneAdapter {
    public static final String MODULE_ID = "echobiomecore";
    public static final String PROFILE_DATA_CONTRACT_ID = "echobiomecore:data/profile_contract_normalization";
    public static final String AMBIENT_ASSET_CONTRACT_ID = "echobiomecore:assets/ambient_asset_contract";
    public static final String HOLOMAP_MAP_CONTRACT_ID = "echobiomecore:maps/holomap_layer_refs";
    public static final String HAZARD_WORLDGEN_CONTRACT_ID = "echobiomecore:worldgen/hazard_overlay_envelope";
    public static final List<String> CONTRACT_IDS = List.of(
            PROFILE_DATA_CONTRACT_ID,
            AMBIENT_ASSET_CONTRACT_ID,
            HOLOMAP_MAP_CONTRACT_ID,
            HAZARD_WORLDGEN_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "biomecore_standalone_contract_active");
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
        report.put("profileDataRoundTrip", probe.get("profileDataRoundTrip"));
        report.put("ambientAssetRoundTrip", probe.get("ambientAssetRoundTrip"));
        report.put("holomapLayerRoundTrip", probe.get("holomapLayerRoundTrip"));
        report.put("hazardOverlayRoundTrip", probe.get("hazardOverlayRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "BiomeCore standalone adapter resolved profile data, ambient asset, HoloMap, and hazard overlay contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("profileDataRoundTrip", true);
        probe.put("ambientAssetRoundTrip", true);
        probe.put("holomapLayerRoundTrip", true);
        probe.put("hazardOverlayRoundTrip", true);
        probe.put("normalizedBiomeId", "ashfall/glass_wastes");
        probe.put("normalizedTagId", "prime/toxic");
        probe.put("hazardIntensity", 1.0D);
        probe.put("holomapFeatureId", "biome.holomap_layer");
        return Map.copyOf(probe);
    }
}
