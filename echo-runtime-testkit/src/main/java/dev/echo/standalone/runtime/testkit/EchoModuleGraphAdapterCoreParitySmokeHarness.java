package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoModuleGraphStandaloneAdapter;

import java.util.List;
import java.util.Map;

public final class EchoModuleGraphAdapterCoreParitySmokeHarness {
    private EchoModuleGraphAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoModuleGraphStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "ModuleGraph standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "ModuleGraph standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("moduleGraphRoundTrip")),
                "ModuleGraph standalone adapter should preserve graph data behavior");
        require(Boolean.TRUE.equals(activation.get("graphValidationRoundTrip")),
                "ModuleGraph standalone adapter should preserve validation behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require(probe.get("loadOrder").equals(List.of("echocore", "echomodulegraph")),
                "ModuleGraph data contract should preserve dependency load order");
        require(probe.get("duplicateModuleIds").equals(List.of("echocore")),
                "ModuleGraph validation contract should report duplicate ids");

        requireEntry(bridge, EchoModuleGraphStandaloneAdapter.MODULE_GRAPH_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "modulegraph.data.module_graph");
        requireEntry(bridge, EchoModuleGraphStandaloneAdapter.GRAPH_VALIDATION_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC, EchoAdapterCoreDomain.DIAGNOSTICS, "modulegraph.diagnostics.graph_validation");
        System.out.println("modulegraph adaptercore parity smoke PASS contracts="
                + EchoModuleGraphStandaloneAdapter.CONTRACT_IDS.size());
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
