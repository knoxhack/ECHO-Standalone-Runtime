package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoCreatureCoreStandaloneAdapter;

import java.util.Map;

public final class EchoCreatureCoreAdapterCoreParitySmokeHarness {
    private EchoCreatureCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoCreatureCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "CreatureCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "CreatureCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("archetypeEntityRoundTrip")),
                "CreatureCore standalone adapter should preserve archetype entity behavior");
        require(Boolean.TRUE.equals(activation.get("spawnWorldgenRoundTrip")),
                "CreatureCore standalone adapter should preserve spawn worldgen behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require("ashfall_stalker".equals(probe.get("archetypeId"))
                        && "ashfall_stalker_ai".equals(probe.get("aiProfileId"))
                        && "unknown".equals(probe.get("hostility")),
                "CreatureCore entity contract should preserve archetype, AI, and hostility defaults");
        require("toxic_ruins".equals(probe.get("spawnTag"))
                        && "Jaw".equals(probe.get("particleBone")),
                "CreatureCore worldgen contract should preserve spawn tags and scan/particle anchors");

        requireEntry(bridge, EchoCreatureCoreStandaloneAdapter.ARCHETYPE_ENTITY_CONTRACT_ID,
                EchoAdapterCoreContentKind.ENTITY, EchoAdapterCoreDomain.ENTITIES, "creaturecore.entities.archetype_ai_contract");
        requireEntry(bridge, EchoCreatureCoreStandaloneAdapter.SPAWN_WORLDGEN_CONTRACT_ID,
                EchoAdapterCoreContentKind.WORLDGEN_DEFINITION, EchoAdapterCoreDomain.WORLDGEN, "creaturecore.worldgen.spawn_scan_contract");
        System.out.println("creaturecore adaptercore parity smoke PASS contracts="
                + EchoCreatureCoreStandaloneAdapter.CONTRACT_IDS.size());
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
