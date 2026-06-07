package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoRemainingSystemsStandaloneAdapter;

import java.util.Map;

public final class EchoRemainingSystemsAdapterCoreParitySmokeHarness {
    private EchoRemainingSystemsAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoRemainingSystemsStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "Remaining systems standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "Remaining systems should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("featureContractRoundTrip")),
                "Remaining systems should preserve source-backed feature contracts");
        require(Integer.valueOf(11).equals(activation.get("moduleCount")),
                "Remaining systems batch should cover eleven modules");

        for (EchoRemainingSystemsStandaloneAdapter.ContractSpec spec
                : EchoRemainingSystemsStandaloneAdapter.CONTRACTS) {
            EchoAdapterCoreRegistryEntry entry = bridge.registry().requireContentId(spec.contentId());
            require(entry.contentKind() == spec.contentKind(),
                    spec.contentId() + " should use content kind " + spec.contentKind());
            require(entry.domain() == spec.domain(),
                    spec.contentId() + " should use AdapterCore domain " + spec.domain().id());
            require(entry.binding().adapterKey().equals(spec.adapterKey()),
                    spec.contentId() + " should expose stable adapter key " + spec.adapterKey());
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        spec.contentId() + " has unregistered runtime alias " + runtimeKind.adapterId());
            }
        }
        System.out.println("remaining systems adaptercore parity smoke PASS modules=11 contracts="
                + EchoRemainingSystemsStandaloneAdapter.CONTRACTS.size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
