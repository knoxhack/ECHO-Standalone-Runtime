package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoContentCoreStandaloneAdapter;

import java.util.Map;

public final class EchoContentCoreAdapterCoreParitySmokeHarness {
    private EchoContentCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoContentCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "ContentCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "ContentCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("ownerLookupRoundTrip")),
                "ContentCore standalone adapter should preserve owner lookup behavior");
        require(Boolean.TRUE.equals(activation.get("referenceLookupRoundTrip")),
                "ContentCore standalone adapter should preserve reference lookup behavior");
        require(Boolean.TRUE.equals(activation.get("gateAvailabilityRoundTrip")),
                "ContentCore standalone adapter should preserve gate and availability behavior");
        require(Boolean.TRUE.equals(activation.get("validationIssueRoundTrip")),
                "ContentCore standalone adapter should preserve validation issue behavior");
        requireEntry(bridge, EchoContentCoreStandaloneAdapter.BLOCK_CATALOG_CONTRACT_ID,
                EchoAdapterCoreContentKind.BLOCK, EchoAdapterCoreDomain.BLOCKS, "contentcore.blocks.content_catalog");
        requireEntry(bridge, EchoContentCoreStandaloneAdapter.ITEM_CATALOG_CONTRACT_ID,
                EchoAdapterCoreContentKind.ITEM, EchoAdapterCoreDomain.ITEMS, "contentcore.items.content_catalog");
        requireEntry(bridge, EchoContentCoreStandaloneAdapter.ENTITY_CATALOG_CONTRACT_ID,
                EchoAdapterCoreContentKind.ENTITY, EchoAdapterCoreDomain.ENTITIES, "contentcore.entities.content_catalog");
        requireEntry(bridge, EchoContentCoreStandaloneAdapter.RECIPE_CATALOG_CONTRACT_ID,
                EchoAdapterCoreContentKind.RECIPE, EchoAdapterCoreDomain.RECIPES, "contentcore.recipes.content_catalog");
        requireEntry(bridge, EchoContentCoreStandaloneAdapter.LOOT_CATALOG_CONTRACT_ID,
                EchoAdapterCoreContentKind.LOOT_TABLE, EchoAdapterCoreDomain.LOOT, "contentcore.loot.content_catalog");
        requireEntry(bridge, EchoContentCoreStandaloneAdapter.STRUCTURE_CATALOG_CONTRACT_ID,
                EchoAdapterCoreContentKind.STRUCTURE, EchoAdapterCoreDomain.STRUCTURES, "contentcore.structures.content_catalog");
        requireEntry(bridge, EchoContentCoreStandaloneAdapter.CONTENT_REGISTRY_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "contentcore.data.content_registry");
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.ITEMS).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoContentCoreStandaloneAdapter.MODULE_ID)),
                "ContentCore items domain should be backed by standalone AdapterCore bindings");
        System.out.println("contentcore adaptercore parity smoke PASS contracts="
                + EchoContentCoreStandaloneAdapter.CONTRACT_IDS.size());
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
