package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoCoreStandaloneAdapter;

import java.util.Map;

public final class EchoCoreAdapterCoreParitySmokeHarness {
    private EchoCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "EchoCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "EchoCore standalone adapter should register aliases for every AdapterCore runtime");
        requireEntry(
                bridge,
                EchoCoreStandaloneAdapter.SERVICE_REGISTRY_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.DATA,
                "core.data.service_registry"
        );
        requireEntry(
                bridge,
                EchoCoreStandaloneAdapter.DATA_BUS_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.DATA,
                "core.data.data_bus"
        );
        requireEntry(
                bridge,
                EchoCoreStandaloneAdapter.CORE_DIAGNOSTICS_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC,
                EchoAdapterCoreDomain.DIAGNOSTICS,
                "core.diagnostics.core_diagnostics"
        );
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.DATA).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoCoreStandaloneAdapter.MODULE_ID)),
                "EchoCore data domain should be backed by standalone AdapterCore bindings");
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.DIAGNOSTICS).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoCoreStandaloneAdapter.MODULE_ID)),
                "EchoCore diagnostics domain should be backed by standalone AdapterCore bindings");
        System.out.println("echocore adaptercore parity smoke PASS contracts="
                + EchoCoreStandaloneAdapter.CONTRACT_IDS.size());
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
