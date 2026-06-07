package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoDataCoreStandaloneAdapter;

import java.util.Map;

public final class EchoDataCoreAdapterCoreParitySmokeHarness {
    private EchoDataCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoDataCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "DataCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "DataCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Integer.valueOf(6).equals(activation.get("dataKeyCount")),
                "DataCore standalone adapter should expose six built-in data key contracts");
        require(Integer.valueOf(1).equals(activation.get("serviceContractCount")),
                "DataCore standalone adapter should expose the data service contract");
        require(Integer.valueOf(1).equals(activation.get("networkContractCount")),
                "DataCore standalone adapter should expose the data sync payload contract");
        require(Boolean.TRUE.equals(activation.get("dataServiceRoundTrip")),
                "DataCore standalone adapter should exercise the data service runtime");
        require(Boolean.TRUE.equals(activation.get("builtinKeyRoundTrip")),
                "DataCore standalone adapter should register built-in keys in the runtime");
        require(Boolean.TRUE.equals(activation.get("metadataReloadRoundTrip")),
                "DataCore standalone adapter should exercise metadata reload behavior");
        require(Boolean.TRUE.equals(activation.get("diagnosticsRoundTrip")),
                "DataCore standalone adapter should expose runtime diagnostics");
        require(Boolean.TRUE.equals(activation.get("syncBridgeRoundTrip")),
                "DataCore standalone adapter should expose the sync bridge runtime");

        requireEntry(
                bridge,
                EchoDataCoreStandaloneAdapter.TERMINAL_PROBE_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.DATA,
                "datacore.data.system.terminal_probe"
        );
        requireEntry(
                bridge,
                EchoDataCoreStandaloneAdapter.PLAYER_SCHEMA_VERSION_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.PLAYER,
                "datacore.player.schema_version"
        );
        requireEntry(
                bridge,
                EchoDataCoreStandaloneAdapter.WORLD_SCHEMA_VERSION_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.SAVES,
                "datacore.saves.world_schema_version"
        );
        requireEntry(
                bridge,
                EchoDataCoreStandaloneAdapter.LAST_REGION_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.WORLDGEN,
                "datacore.worldcore.last_region"
        );
        requireEntry(
                bridge,
                EchoDataCoreStandaloneAdapter.LAST_MARKER_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.MAPS,
                "datacore.worldcore.last_marker"
        );
        requireEntry(
                bridge,
                EchoDataCoreStandaloneAdapter.ACTIVE_HAZARDS_CONTRACT_ID,
                EchoAdapterCoreContentKind.WORLD_HAZARD,
                EchoAdapterCoreDomain.HAZARDS,
                "datacore.worldcore.active_hazards"
        );
        requireEntry(
                bridge,
                EchoDataCoreStandaloneAdapter.DATA_SERVICE_CONTRACT_ID,
                EchoAdapterCoreContentKind.SAVE_RECORD,
                EchoAdapterCoreDomain.SAVES,
                "datacore.service.persistence"
        );
        requireEntry(
                bridge,
                EchoDataCoreStandaloneAdapter.DATA_SYNC_CONTRACT_ID,
                EchoAdapterCoreContentKind.NETWORK_HOOK,
                EchoAdapterCoreDomain.NETWORKING,
                "datacore.network.data_sync"
        );

        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.SAVES).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoDataCoreStandaloneAdapter.MODULE_ID)),
                "DataCore saves domain should be backed by standalone AdapterCore bindings");
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.NETWORKING).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoDataCoreStandaloneAdapter.MODULE_ID)),
                "DataCore networking domain should be backed by standalone AdapterCore bindings");

        System.out.println("datacore adaptercore parity smoke PASS contracts="
                + EchoDataCoreStandaloneAdapter.CONTRACT_IDS.size());
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
