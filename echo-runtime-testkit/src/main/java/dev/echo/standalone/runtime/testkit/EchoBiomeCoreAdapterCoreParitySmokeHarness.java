package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoBiomeCoreStandaloneAdapter;

import java.util.Map;

public final class EchoBiomeCoreAdapterCoreParitySmokeHarness {
    private EchoBiomeCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoBiomeCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "BiomeCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "BiomeCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("profileDataRoundTrip")),
                "BiomeCore standalone adapter should preserve profile data normalization");
        require(Boolean.TRUE.equals(activation.get("ambientAssetRoundTrip")),
                "BiomeCore standalone adapter should preserve ambient asset behavior");
        require(Boolean.TRUE.equals(activation.get("holomapLayerRoundTrip")),
                "BiomeCore standalone adapter should preserve HoloMap layer refs");
        require(Boolean.TRUE.equals(activation.get("hazardOverlayRoundTrip")),
                "BiomeCore standalone adapter should preserve hazard overlay behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require("ashfall/glass_wastes".equals(probe.get("normalizedBiomeId")),
                "BiomeCore data contract should normalize biome ids");
        require("prime/toxic".equals(probe.get("normalizedTagId")),
                "BiomeCore data contract should normalize biome tag ids");
        require(Double.valueOf(1.0D).equals(probe.get("hazardIntensity")),
                "BiomeCore worldgen contract should clamp hazard intensity to 0..1");
        require("biome.holomap_layer".equals(probe.get("holomapFeatureId")),
                "BiomeCore maps contract should normalize HoloMap feature refs");

        requireEntry(bridge, EchoBiomeCoreStandaloneAdapter.PROFILE_DATA_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "biomecore.data.profile_contract_normalization");
        requireEntry(bridge, EchoBiomeCoreStandaloneAdapter.AMBIENT_ASSET_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.ASSETS, "biomecore.assets.ambient_asset_contract");
        requireEntry(bridge, EchoBiomeCoreStandaloneAdapter.HOLOMAP_MAP_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.MAPS, "biomecore.maps.holomap_layer_refs");
        requireEntry(bridge, EchoBiomeCoreStandaloneAdapter.HAZARD_WORLDGEN_CONTRACT_ID,
                EchoAdapterCoreContentKind.WORLDGEN_DEFINITION, EchoAdapterCoreDomain.WORLDGEN, "biomecore.worldgen.hazard_overlay_envelope");
        System.out.println("biomecore adaptercore parity smoke PASS contracts="
                + EchoBiomeCoreStandaloneAdapter.CONTRACT_IDS.size());
    }

    private static void requireEntry(
            EchoAdapterCoreStandaloneContentBridge bridge,
            String contentId,
            EchoAdapterCoreContentKind contentKind,
            EchoAdapterCoreDomain domain,
            String adapterKey
    ) {
        EchoAdapterCoreRegistryEntry entry = bridge.registry().requireContentId(contentId);
        require(entry.contentKind() == contentKind,
                contentId + " should use content kind " + contentKind);
        require(entry.domain() == domain,
                contentId + " should use AdapterCore domain " + domain.id());
        require(entry.binding().adapterKey().equals(adapterKey),
                contentId + " should expose stable adapter key " + adapterKey);
        for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
            require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                    contentId + " has unregistered runtime alias " + runtimeKind.adapterId());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
