package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoArmoryStandaloneAdapter;

import java.util.List;
import java.util.Map;

public final class EchoArmoryAdapterCoreParitySmokeHarness {
    private EchoArmoryAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoArmoryStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "Armory standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "Armory standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("gearStateRoundTrip")),
                "Armory standalone adapter should preserve gear/module state behavior");
        require(Boolean.TRUE.equals(activation.get("stationPreviewRoundTrip")),
                "Armory standalone adapter should preserve station operation preview behavior");
        require(Boolean.TRUE.equals(activation.get("routeReadinessRoundTrip")),
                "Armory standalone adapter should preserve route-readiness scoring behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require(probe.get("normalizedModules").equals(List.of(
                        "echoarmory:veil_regulator",
                        "echoarmory:fracture_baffle",
                        "echoarmory:thermal_liner"
                )),
                "Armory item contract should normalize module lists with strip/distinct/slot limit behavior");
        require(Integer.valueOf(55).equals(probe.get("energyStored"))
                        && Integer.valueOf(80).equals(probe.get("energyCapacity"))
                        && Integer.valueOf(4).equals(probe.get("tier")),
                "Armory item contract should clamp energy and tier state");
        require(Integer.valueOf(1120).equals(probe.get("readyScore"))
                        && Integer.valueOf(787).equals(probe.get("stagedScore"))
                        && Integer.valueOf(70).equals(probe.get("lockedScore")),
                "Armory player contract should preserve readiness score formula");

        requireEntry(bridge, EchoArmoryStandaloneAdapter.GEAR_STATE_CONTRACT_ID,
                EchoAdapterCoreContentKind.ITEM, EchoAdapterCoreDomain.ITEMS, "armory.items.gear_state_normalization");
        requireEntry(bridge, EchoArmoryStandaloneAdapter.STATION_PREVIEW_CONTRACT_ID,
                EchoAdapterCoreContentKind.RECIPE, EchoAdapterCoreDomain.RECIPES, "armory.recipes.station_operation_preview");
        requireEntry(bridge, EchoArmoryStandaloneAdapter.ROUTE_READINESS_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PLAYER, "armory.player.route_readiness_score");
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.PLAYER).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoArmoryStandaloneAdapter.MODULE_ID)),
                "Armory player domain should be backed by standalone AdapterCore bindings");
        System.out.println("armory adaptercore parity smoke PASS contracts="
                + EchoArmoryStandaloneAdapter.CONTRACT_IDS.size());
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
