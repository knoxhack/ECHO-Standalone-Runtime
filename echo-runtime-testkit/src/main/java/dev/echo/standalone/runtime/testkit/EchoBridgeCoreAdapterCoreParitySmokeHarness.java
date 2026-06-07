package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoBridgeCoreStandaloneAdapter;

import java.util.Map;

public final class EchoBridgeCoreAdapterCoreParitySmokeHarness {
    private EchoBridgeCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoBridgeCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "BridgeCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "BridgeCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("sessionDataRoundTrip")),
                "BridgeCore standalone adapter should preserve session data behavior");
        require(Boolean.TRUE.equals(activation.get("safeActionGateRoundTrip")),
                "BridgeCore standalone adapter should preserve safe-action diagnostic behavior");
        require(Boolean.TRUE.equals(activation.get("localTransportRoundTrip")),
                "BridgeCore standalone adapter should preserve local transport behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require("dev-bridge-01".equals(probe.get("normalizedSessionId")),
                "BridgeCore data contract should normalize session ids");
        require(Boolean.TRUE.equals(probe.get("requiresConfirmation"))
                        && Boolean.TRUE.equals(probe.get("safeActionExpiredAt20"))
                        && Boolean.TRUE.equals(probe.get("controlRedacted")),
                "BridgeCore diagnostics contract should require confirmation and redact control results");
        require("dev-bridge-01.events".equals(probe.get("heartbeatCursor")),
                "BridgeCore networking contract should default local heartbeat cursor ids");

        requireEntry(bridge, EchoBridgeCoreStandaloneAdapter.SESSION_DATA_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "bridgecore.data.session_state_contract");
        requireEntry(bridge, EchoBridgeCoreStandaloneAdapter.SAFE_ACTION_DIAGNOSTIC_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC, EchoAdapterCoreDomain.DIAGNOSTICS, "bridgecore.diagnostics.safe_action_gate");
        requireEntry(bridge, EchoBridgeCoreStandaloneAdapter.LOCAL_TRANSPORT_CONTRACT_ID,
                EchoAdapterCoreContentKind.NETWORK_HOOK, EchoAdapterCoreDomain.NETWORKING, "bridgecore.networking.local_transport_heartbeat");
        System.out.println("bridgecore adaptercore parity smoke PASS contracts="
                + EchoBridgeCoreStandaloneAdapter.CONTRACT_IDS.size());
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
