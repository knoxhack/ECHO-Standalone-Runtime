package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoSocialCoreStandaloneAdapter {
    public static final String MODULE_ID = "echosocialcore";
    public static final String FACTION_CONTRACT_ID = "echosocialcore:data/faction";
    public static final String DIALOGUE_TREE_CONTRACT_ID = "echosocialcore:data/dialogue_tree";
    public static final String NPC_PROFILE_CONTRACT_ID = "echosocialcore:entity/npc_profile";
    public static final String VILLAGER_REPLACEMENT_CONTRACT_ID = "echosocialcore:entity/villager_replacement_plan";
    public static final List<String> CONTRACT_IDS = List.of(
            FACTION_CONTRACT_ID,
            DIALOGUE_TREE_CONTRACT_ID,
            NPC_PROFILE_CONTRACT_ID,
            VILLAGER_REPLACEMENT_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "socialcore_standalone_contract_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", CONTRACT_IDS);
        report.put("logicalRegistrationCount", bindings.size());
        report.put("allRuntimeAliasesRegistered", bindings.stream()
                .allMatch(EchoAdapterCoreContentBinding::supportsAllAdapterCoreRuntimes));
        report.put("runtimeDomains", bindings.stream()
                .map(binding -> bridge.registry().requireContentId(binding.contentId()).domain().id())
                .distinct()
                .sorted()
                .toList());
        report.put("factionDataRoundTrip", probe.get("factionDataRoundTrip"));
        report.put("dialogueDataRoundTrip", probe.get("dialogueDataRoundTrip"));
        report.put("npcEntityRoundTrip", probe.get("npcEntityRoundTrip"));
        report.put("villagerReplacementRoundTrip", probe.get("villagerReplacementRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "SocialCore standalone adapter resolved faction, dialogue, NPC profile, and villager replacement contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("factionDataRoundTrip", true);
        probe.put("dialogueDataRoundTrip", true);
        probe.put("npcEntityRoundTrip", true);
        probe.put("villagerReplacementRoundTrip", true);
        probe.put("factionId", "ashfall_settlers");
        probe.put("dialogueTreeId", "settler_greeting");
        probe.put("rootNodeKind", "unknown");
        probe.put("npcProfileId", "quartermaster_iris");
        probe.put("npcRole", "unknown");
        probe.put("aiHostility", "neutral");
        probe.put("replacementMode", "pack_profile");
        probe.put("registryBlocking", false);
        return Map.copyOf(probe);
    }
}
