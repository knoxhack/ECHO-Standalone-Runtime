package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoFamiliarCoreStandaloneAdapter;

import java.util.Map;

public final class EchoFamiliarCoreAdapterCoreParitySmokeHarness {
    private EchoFamiliarCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoFamiliarCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "FamiliarCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "FamiliarCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("companionEntityRoundTrip")),
                "FamiliarCore standalone adapter should preserve companion entity behavior");
        require(Boolean.TRUE.equals(activation.get("bondProgressionRoundTrip")),
                "FamiliarCore standalone adapter should preserve bond progression behavior");
        require(Boolean.TRUE.equals(activation.get("commandMenuRoundTrip")),
                "FamiliarCore standalone adapter should preserve command behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require("echofamiliarcore:familiar/spirit_drone".equals(probe.get("activeFamiliar"))
                        && "Spirit Drone".equals(probe.get("companionTitle")),
                "FamiliarCore entity contract should preserve starter familiar id and title");
        require(Integer.valueOf(5).equals(probe.get("bondLevel"))
                        && Integer.valueOf(240).equals(probe.get("nextLevelExperience"))
                        && "guardian signal chassis".equals(probe.get("evolutionForm"))
                        && "hardlight guard".equals(probe.get("evolutionAbility"))
                        && Integer.valueOf(69).equals(probe.get("evolutionPower")),
                "FamiliarCore player contract should preserve bond and evolution math");
        require("Defend".equals(probe.get("commandName")),
                "FamiliarCore command contract should preserve command menu names");

        requireEntry(bridge, EchoFamiliarCoreStandaloneAdapter.FAMILIAR_COMPANION_CONTRACT_ID,
                EchoAdapterCoreContentKind.ENTITY, EchoAdapterCoreDomain.ENTITIES, "familiarcore.entities.familiar_companion");
        requireEntry(bridge, EchoFamiliarCoreStandaloneAdapter.BOND_PROGRESSION_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PLAYER, "familiarcore.player.bond_progression");
        requireEntry(bridge, EchoFamiliarCoreStandaloneAdapter.FAMILIAR_COMMAND_CONTRACT_ID,
                EchoAdapterCoreContentKind.COMMAND, EchoAdapterCoreDomain.COMMANDS, "familiarcore.commands.familiar_command");
        System.out.println("familiarcore adaptercore parity smoke PASS contracts="
                + EchoFamiliarCoreStandaloneAdapter.CONTRACT_IDS.size());
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
