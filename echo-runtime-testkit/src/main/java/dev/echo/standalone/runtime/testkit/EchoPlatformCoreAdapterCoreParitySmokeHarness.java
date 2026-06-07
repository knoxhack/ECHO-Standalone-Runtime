package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoPlatformCoreStandaloneAdapter;

import java.util.Map;

public final class EchoPlatformCoreAdapterCoreParitySmokeHarness {
    private EchoPlatformCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoPlatformCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "PlatformCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "PlatformCore standalone adapter should register aliases for every AdapterCore runtime");
        requireEntry(
                bridge,
                EchoPlatformCoreStandaloneAdapter.MODULE_IDENTITY_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.DATA,
                "platformcore.data.module_identity"
        );
        requireEntry(
                bridge,
                EchoPlatformCoreStandaloneAdapter.CAPABILITY_REPORT_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC,
                EchoAdapterCoreDomain.DIAGNOSTICS,
                "platformcore.diagnostics.capability_report"
        );
        requireEntry(
                bridge,
                EchoPlatformCoreStandaloneAdapter.TRUST_POLICY_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.DATA,
                "platformcore.data.trust_policy"
        );
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.DATA).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoPlatformCoreStandaloneAdapter.MODULE_ID)),
                "PlatformCore data domain should be backed by standalone AdapterCore bindings");
        System.out.println("platformcore adaptercore parity smoke PASS contracts="
                + EchoPlatformCoreStandaloneAdapter.CONTRACT_IDS.size());
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
