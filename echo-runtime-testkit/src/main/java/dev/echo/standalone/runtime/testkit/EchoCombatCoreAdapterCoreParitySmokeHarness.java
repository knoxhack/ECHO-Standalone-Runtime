package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoCombatCoreStandaloneAdapter;

import java.util.Map;

public final class EchoCombatCoreAdapterCoreParitySmokeHarness {
    private EchoCombatCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoCombatCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "CombatCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "CombatCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("damageItemRoundTrip")),
                "CombatCore standalone adapter should preserve damage and weapon trait behavior");
        require(Boolean.TRUE.equals(activation.get("entityScalingRoundTrip")),
                "CombatCore standalone adapter should preserve enemy scaling and boss phase behavior");
        require(Boolean.TRUE.equals(activation.get("playerDefenseRoundTrip")),
                "CombatCore standalone adapter should preserve armor, shield, and telemetry behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require("signal_burn".equals(probe.get("damageTypeId"))
                        && "overclocked_blades".equals(probe.get("weaponTraitId")),
                "CombatCore item contract should normalize damage and trait ids");
        require(Double.valueOf(1.0D).equals(probe.get("bossPhaseRatio")),
                "CombatCore entity contract should clamp boss phase ratio");
        require(Double.valueOf(120.0D).equals(probe.get("shieldCapacity"))
                        && "unknown".equals(probe.get("telemetryKind")),
                "CombatCore player contract should preserve shield and telemetry defaults");

        requireEntry(bridge, EchoCombatCoreStandaloneAdapter.DAMAGE_ITEM_CONTRACT_ID,
                EchoAdapterCoreContentKind.ITEM, EchoAdapterCoreDomain.ITEMS, "combatcore.items.damage_weapon_trait_contract");
        requireEntry(bridge, EchoCombatCoreStandaloneAdapter.ENTITY_SCALING_CONTRACT_ID,
                EchoAdapterCoreContentKind.ENTITY, EchoAdapterCoreDomain.ENTITIES, "combatcore.entities.enemy_scaling_boss_phase_contract");
        requireEntry(bridge, EchoCombatCoreStandaloneAdapter.PLAYER_DEFENSE_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PLAYER, "combatcore.player.armor_shield_telemetry_contract");
        System.out.println("combatcore adaptercore parity smoke PASS contracts="
                + EchoCombatCoreStandaloneAdapter.CONTRACT_IDS.size());
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
