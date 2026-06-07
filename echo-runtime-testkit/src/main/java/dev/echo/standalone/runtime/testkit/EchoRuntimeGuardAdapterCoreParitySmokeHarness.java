package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoRuntimeGuardStandaloneAdapter;

import java.util.Map;

public final class EchoRuntimeGuardAdapterCoreParitySmokeHarness {
    private EchoRuntimeGuardAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoRuntimeGuardStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "RuntimeGuard standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "RuntimeGuard standalone adapter should register aliases for every AdapterCore runtime");
        requireEntry(
                bridge,
                EchoRuntimeGuardStandaloneAdapter.RUNTIME_HEALTH_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC,
                EchoAdapterCoreDomain.DIAGNOSTICS,
                "runtimeguard.diagnostics.runtime_health"
        );
        requireEntry(
                bridge,
                EchoRuntimeGuardStandaloneAdapter.RUNTIME_METRICS_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.DATA,
                "runtimeguard.data.runtime_metrics"
        );
        requireEntry(
                bridge,
                EchoRuntimeGuardStandaloneAdapter.NETWORK_BUDGET_CONTRACT_ID,
                EchoAdapterCoreContentKind.NETWORK_HOOK,
                EchoAdapterCoreDomain.NETWORKING,
                "runtimeguard.network.runtime_budget"
        );
        requireEntry(
                bridge,
                EchoRuntimeGuardStandaloneAdapter.ECHO_PERF_COMMAND_CONTRACT_ID,
                EchoAdapterCoreContentKind.COMMAND,
                EchoAdapterCoreDomain.COMMANDS,
                "runtimeguard.commands.echo_perf"
        );
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.DIAGNOSTICS).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoRuntimeGuardStandaloneAdapter.MODULE_ID)),
                "RuntimeGuard diagnostics domain should be backed by a standalone AdapterCore binding");
        System.out.println("runtimeguard adaptercore parity smoke PASS contracts="
                + EchoRuntimeGuardStandaloneAdapter.CONTRACT_IDS.size());
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
