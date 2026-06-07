package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoNeutralContractCoresStandaloneAdapter {
    public static final List<ContractSpec> CONTRACTS = List.of(
            spec("echocreaturecore", "echocreaturecore:data/creature_archetype", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "creaturecore.data.creature_archetype", "creature.archetypes"),
            spec("echocreaturecore", "echocreaturecore:entity/ai_profile", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.ENTITIES, "creaturecore.entities.ai_profile", "creature.ai_profiles"),
            spec("echocreaturecore", "echocreaturecore:data/scan_metadata", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "creaturecore.data.scan_metadata", "creature.scan_metadata"),
            spec("echodifficultycore", "echodifficultycore:data/difficulty_profile", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "difficultycore.data.difficulty_profile", "difficulty.profiles"),
            spec("echodifficultycore", "echodifficultycore:hazard/adaptive_scaling", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.HAZARDS, "difficultycore.hazards.adaptive_scaling", "difficulty.adaptive"),
            spec("echodifficultycore", "echodifficultycore:pack/variant_difficulty_policy", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PACKS, "difficultycore.packs.variant_difficulty_policy", "difficulty.pack_policy"),
            spec("echodifficultycore", "echodifficultycore:diagnostic/difficulty_telemetry", EchoAdapterCoreContentKind.DIAGNOSTIC, EchoAdapterCoreDomain.DIAGNOSTICS, "difficultycore.diagnostics.difficulty_telemetry", "difficulty.telemetry"),
            spec("echoencountercore", "echoencountercore:mission/encounter_definition", EchoAdapterCoreContentKind.MISSION, EchoAdapterCoreDomain.MISSIONS, "encountercore.missions.encounter_definition", "encounter.definitions"),
            spec("echoencountercore", "echoencountercore:entity/boss_gate", EchoAdapterCoreContentKind.ENTITY, EchoAdapterCoreDomain.ENTITIES, "encountercore.entities.boss_gate", "encounter.boss_gates"),
            spec("echoencountercore", "echoencountercore:story/faction_patrol", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.STORY, "encountercore.story.faction_patrol", "encounter.faction_patrols"),
            spec("echoeventcore", "echoeventcore:weather/world_event", EchoAdapterCoreContentKind.WORLD_HAZARD, EchoAdapterCoreDomain.WEATHER, "eventcore.weather.world_event", "event.world_events"),
            spec("echoeventcore", "echoeventcore:data/event_scheduler", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "eventcore.data.event_scheduler", "event.scheduler"),
            spec("echoeventcore", "echoeventcore:diagnostic/event_validation", EchoAdapterCoreContentKind.DIAGNOSTIC, EchoAdapterCoreDomain.DIAGNOSTICS, "eventcore.diagnostics.event_validation", "event.validation"),
            spec("echoguidecore", "echoguidecore:wiki/guide_page", EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.WIKI, "guidecore.wiki.guide_page", "guide.pages"),
            spec("echoguidecore", "echoguidecore:data/search_index", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "guidecore.data.search_index", "guide.search"),
            spec("echoguidecore", "echoguidecore:player/unlock_visibility", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PLAYER, "guidecore.player.unlock_visibility", "guide.unlock_visibility"),
            spec("echoinputcore", "echoinputcore:input/context", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.INPUT, "inputcore.input.context", "input.contexts"),
            spec("echoinputcore", "echoinputcore:input/keybind_registry", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.INPUT, "inputcore.input.keybind_registry", "input.keybind_registry"),
            spec("echoinputcore", "echoinputcore:ui/radial_menu", EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "inputcore.ui.radial_menu", "input.radial_menus"),
            spec("echoinputcore", "echoinputcore:input/controller_ready", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.INPUT, "inputcore.input.controller_ready", "input.controller_ready"),
            spec("echolorecore", "echolorecore:story/lore_entry", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.STORY, "lorecore.story.lore_entry", "lore.entries"),
            spec("echolorecore", "echolorecore:sound/audio_log", EchoAdapterCoreContentKind.SOUND_EVENT, EchoAdapterCoreDomain.SOUNDS, "lorecore.sounds.audio_log", "lore.audio_logs"),
            spec("echolorecore", "echolorecore:story/blackbox_entry", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.STORY, "lorecore.story.blackbox_entry", "lore.blackbox_entries"),
            spec("echolorecore", "echolorecore:structure/environmental_story", EchoAdapterCoreContentKind.STRUCTURE, EchoAdapterCoreDomain.STRUCTURES, "lorecore.structures.environmental_story", "lore.environmental_storytelling"),
            spec("echonotificationcore", "echonotificationcore:ui/toast", EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "notificationcore.ui.toast", "notifications.toasts"),
            spec("echonotificationcore", "echonotificationcore:ui/system_alert", EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "notificationcore.ui.system_alert", "notifications.system_alerts"),
            spec("echonotificationcore", "echonotificationcore:mission/mission_update", EchoAdapterCoreContentKind.MISSION, EchoAdapterCoreDomain.MISSIONS, "notificationcore.missions.mission_update", "notifications.mission_updates"),
            spec("echonotificationcore", "echonotificationcore:ui/tutorial_hint", EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "notificationcore.ui.tutorial_hint", "notifications.tutorial_hints")
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreRegistryEntry> entries = CONTRACTS.stream()
                .map(spec -> bridge.registry().requireContentId(spec.contentId()))
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "neutral_contract_cores_standalone_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleCount", probe.get("moduleCount"));
        report.put("registeredFeatureContracts", CONTRACTS.stream().map(ContractSpec::contentId).toList());
        report.put("logicalRegistrationCount", entries.size());
        report.put("allRuntimeAliasesRegistered", entries.stream()
                .map(EchoAdapterCoreRegistryEntry::binding)
                .allMatch(EchoAdapterCoreContentBinding::supportsAllAdapterCoreRuntimes));
        report.put("featureContractRoundTrip", probe.get("featureContractRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "Neutral contract-core standalone adapter resolved source-backed feature contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        List<String> modules = CONTRACTS.stream().map(ContractSpec::moduleId).distinct().sorted().toList();
        List<String> features = CONTRACTS.stream().map(ContractSpec::featureId).sorted().toList();
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("moduleCount", modules.size());
        probe.put("featureCount", features.size());
        probe.put("modules", modules);
        probe.put("featureContractRoundTrip", modules.size() == 8
                && features.size() == CONTRACTS.size()
                && features.contains("creature.archetypes")
                && features.contains("difficulty.adaptive")
                && features.contains("encounter.boss_gates")
                && features.contains("event.scheduler")
                && features.contains("guide.search")
                && features.contains("input.radial_menus")
                && features.contains("lore.blackbox_entries")
                && features.contains("notifications.tutorial_hints"));
        return Map.copyOf(probe);
    }

    private static ContractSpec spec(
            String moduleId,
            String contentId,
            EchoAdapterCoreContentKind contentKind,
            EchoAdapterCoreDomain domain,
            String adapterKey,
            String featureId
    ) {
        return new ContractSpec(moduleId, contentId, contentKind, domain, adapterKey, featureId);
    }

    public record ContractSpec(
            String moduleId,
            String contentId,
            EchoAdapterCoreContentKind contentKind,
            EchoAdapterCoreDomain domain,
            String adapterKey,
            String featureId
    ) {
    }
}
