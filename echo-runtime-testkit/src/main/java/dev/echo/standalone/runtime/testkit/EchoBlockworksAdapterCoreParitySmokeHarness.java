package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoBlockworksStandaloneAdapter;

import java.util.Map;

public final class EchoBlockworksAdapterCoreParitySmokeHarness {
    private EchoBlockworksAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoBlockworksStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "Blockworks standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "Blockworks standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("blockCatalogRoundTrip")),
                "Blockworks standalone adapter should preserve block catalog behavior");
        require(Boolean.TRUE.equals(activation.get("patternCutterRoundTrip")),
                "Blockworks standalone adapter should preserve pattern cutter behavior");
        require(Boolean.TRUE.equals(activation.get("paletteConversionRoundTrip")),
                "Blockworks standalone adapter should preserve palette conversion behavior");
        require(Boolean.TRUE.equals(activation.get("showcaseSiteRoundTrip")),
                "Blockworks standalone adapter should preserve showcase site behavior");
        require(Boolean.TRUE.equals(activation.get("worldgenSiteRoundTrip")),
                "Blockworks standalone adapter should preserve worldgen site behavior");
        requireEntry(bridge, EchoBlockworksStandaloneAdapter.BLOCK_CATALOG_CONTRACT_ID,
                EchoAdapterCoreContentKind.BLOCK, EchoAdapterCoreDomain.BLOCKS, "blockworks.blocks.block_catalog");
        requireEntry(bridge, EchoBlockworksStandaloneAdapter.PATTERN_CUTTER_CONTRACT_ID,
                EchoAdapterCoreContentKind.ITEM, EchoAdapterCoreDomain.ITEMS, "blockworks.items.pattern_cutter");
        requireEntry(bridge, EchoBlockworksStandaloneAdapter.PALETTE_CONVERSION_CONTRACT_ID,
                EchoAdapterCoreContentKind.RECIPE, EchoAdapterCoreDomain.RECIPES, "blockworks.recipes.palette_conversion");
        requireEntry(bridge, EchoBlockworksStandaloneAdapter.SHOWCASE_SITES_CONTRACT_ID,
                EchoAdapterCoreContentKind.STRUCTURE, EchoAdapterCoreDomain.STRUCTURES, "blockworks.structures.showcase_sites");
        requireEntry(bridge, EchoBlockworksStandaloneAdapter.SCATTER_SITES_CONTRACT_ID,
                EchoAdapterCoreContentKind.WORLDGEN_DEFINITION, EchoAdapterCoreDomain.WORLDGEN, "blockworks.worldgen.scatter_sites");
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.WORLDGEN).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoBlockworksStandaloneAdapter.MODULE_ID)),
                "Blockworks worldgen domain should be backed by standalone AdapterCore bindings");
        System.out.println("blockworks adaptercore parity smoke PASS contracts="
                + EchoBlockworksStandaloneAdapter.CONTRACT_IDS.size());
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
