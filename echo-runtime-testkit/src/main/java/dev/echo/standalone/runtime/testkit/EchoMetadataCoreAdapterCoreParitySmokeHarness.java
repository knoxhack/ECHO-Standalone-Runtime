package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoMetadataCoreStandaloneAdapter;

import java.util.Map;

public final class EchoMetadataCoreAdapterCoreParitySmokeHarness {
    private EchoMetadataCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoMetadataCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "MetadataCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "MetadataCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("manifestNormalizationRoundTrip")),
                "MetadataCore standalone adapter should preserve manifest normalization behavior");
        require(Boolean.TRUE.equals(activation.get("schemaValidationRoundTrip")),
                "MetadataCore standalone adapter should preserve schema validation behavior");
        require(Boolean.TRUE.equals(activation.get("conflictDetectionRoundTrip")),
                "MetadataCore standalone adapter should preserve conflict detection behavior");
        require(Boolean.TRUE.equals(activation.get("fallbackScanRoundTrip")),
                "MetadataCore standalone adapter should preserve fallback scan behavior");
        requireEntry(bridge, EchoMetadataCoreStandaloneAdapter.MODULE_MANIFEST_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "metadatacore.data.module_manifest");
        requireEntry(bridge, EchoMetadataCoreStandaloneAdapter.AI_METADATA_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "metadatacore.data.ai_metadata");
        requireEntry(bridge, EchoMetadataCoreStandaloneAdapter.METADATA_VALIDATION_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC, EchoAdapterCoreDomain.DIAGNOSTICS, "metadatacore.diagnostics.metadata_validation");
        requireEntry(bridge, EchoMetadataCoreStandaloneAdapter.PACK_METADATA_SCAN_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PACKS, "metadatacore.packs.metadata_scan");
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.PACKS).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoMetadataCoreStandaloneAdapter.MODULE_ID)),
                "MetadataCore packs domain should be backed by standalone AdapterCore bindings");
        System.out.println("metadatacore adaptercore parity smoke PASS contracts="
                + EchoMetadataCoreStandaloneAdapter.CONTRACT_IDS.size());
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
