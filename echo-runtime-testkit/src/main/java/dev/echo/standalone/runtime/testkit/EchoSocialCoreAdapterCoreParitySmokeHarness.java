package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoSocialCoreStandaloneAdapter;

import java.util.Map;

public final class EchoSocialCoreAdapterCoreParitySmokeHarness {
    private EchoSocialCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoSocialCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "SocialCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "SocialCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("factionDataRoundTrip")),
                "SocialCore standalone adapter should preserve faction behavior");
        require(Boolean.TRUE.equals(activation.get("dialogueDataRoundTrip")),
                "SocialCore standalone adapter should preserve dialogue behavior");
        require(Boolean.TRUE.equals(activation.get("npcEntityRoundTrip")),
                "SocialCore standalone adapter should preserve NPC behavior");
        require(Boolean.TRUE.equals(activation.get("villagerReplacementRoundTrip")),
                "SocialCore standalone adapter should preserve villager replacement behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require("ashfall_settlers".equals(probe.get("factionId"))
                        && "settler_greeting".equals(probe.get("dialogueTreeId"))
                        && "unknown".equals(probe.get("rootNodeKind")),
                "SocialCore data contracts should preserve faction/dialogue normalization and node fallback");
        require("quartermaster_iris".equals(probe.get("npcProfileId"))
                        && "unknown".equals(probe.get("npcRole"))
                        && "neutral".equals(probe.get("aiHostility"))
                        && "pack_profile".equals(probe.get("replacementMode"))
                        && Boolean.FALSE.equals(probe.get("registryBlocking")),
                "SocialCore entity contracts should preserve NPC defaults, replacement mode, and registry blocking state");

        requireEntry(bridge, EchoSocialCoreStandaloneAdapter.FACTION_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "socialcore.data.faction");
        requireEntry(bridge, EchoSocialCoreStandaloneAdapter.DIALOGUE_TREE_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "socialcore.data.dialogue_tree");
        requireEntry(bridge, EchoSocialCoreStandaloneAdapter.NPC_PROFILE_CONTRACT_ID,
                EchoAdapterCoreContentKind.ENTITY, EchoAdapterCoreDomain.ENTITIES, "socialcore.entities.npc_profile");
        requireEntry(bridge, EchoSocialCoreStandaloneAdapter.VILLAGER_REPLACEMENT_CONTRACT_ID,
                EchoAdapterCoreContentKind.ENTITY, EchoAdapterCoreDomain.ENTITIES, "socialcore.entities.villager_replacement_plan");
        System.out.println("socialcore adaptercore parity smoke PASS contracts="
                + EchoSocialCoreStandaloneAdapter.CONTRACT_IDS.size());
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
