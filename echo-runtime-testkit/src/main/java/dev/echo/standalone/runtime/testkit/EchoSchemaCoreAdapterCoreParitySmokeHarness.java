package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoSchemaCoreStandaloneAdapter;

import java.util.Map;

public final class EchoSchemaCoreAdapterCoreParitySmokeHarness {
    private EchoSchemaCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoSchemaCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "SchemaCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "SchemaCore standalone adapter should register aliases for every AdapterCore runtime");
        require(((Number) activation.get("builtinSchemaCount")).intValue()
                        >= EchoSchemaCoreStandaloneAdapter.CONTRACT_IDS.size(),
                "SchemaCore standalone adapter should exercise built-in schema descriptors");
        require(Boolean.TRUE.equals(activation.get("schemaRegistryRoundTrip")),
                "SchemaCore standalone adapter should preserve schema registry behavior");
        require(Boolean.TRUE.equals(activation.get("schemaLookupRoundTrip")),
                "SchemaCore standalone adapter should preserve schema lookup behavior");
        require(Boolean.TRUE.equals(activation.get("migrationHintRoundTrip")),
                "SchemaCore standalone adapter should preserve schema migration hint behavior");
        requireEntry(
                bridge,
                EchoSchemaCoreStandaloneAdapter.SCHEMA_REGISTRY_CONTRACT_ID,
                "schemacore.data.schema_registry"
        );
        requireEntry(
                bridge,
                EchoSchemaCoreStandaloneAdapter.MOD_MANIFEST_SCHEMA_CONTRACT_ID,
                "schemacore.data.echo_mod_manifest_schema"
        );
        requireEntry(
                bridge,
                EchoSchemaCoreStandaloneAdapter.PROMPT_BUNDLE_SCHEMA_CONTRACT_ID,
                "schemacore.data.prompt_bundle_schema"
        );
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.DATA).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoSchemaCoreStandaloneAdapter.MODULE_ID)),
                "SchemaCore data domain should be backed by standalone AdapterCore bindings");
        System.out.println("schemacore adaptercore parity smoke PASS contracts="
                + EchoSchemaCoreStandaloneAdapter.CONTRACT_IDS.size());
    }

    private static void requireEntry(
            EchoAdapterCoreStandaloneContentBridge bridge,
            String contentId,
            String adapterKey
    ) {
        EchoAdapterCoreRegistryEntry entry = bridge.registry().requireContentId(contentId);
        require(entry.contentKind() == EchoAdapterCoreContentKind.DATA_COMPONENT,
                contentId + " should use data component content kind");
        require(entry.domain() == EchoAdapterCoreDomain.DATA,
                contentId + " should use AdapterCore data domain");
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
