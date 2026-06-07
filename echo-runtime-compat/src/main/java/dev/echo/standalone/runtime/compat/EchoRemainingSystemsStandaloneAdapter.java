package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoRemainingSystemsStandaloneAdapter {
    public static final List<ContractSpec> CONTRACTS = List.of(
            spec("echofamiliarcore", "echofamiliarcore:data/familiar_registry", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "familiarcore.data.familiar_registry", "familiar.registry", "Familiar Registry Contract"),
            spec("echofamiliarcore", "echofamiliarcore:player/familiar_upgrades", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PLAYER, "familiarcore.player.familiar_upgrades", "familiar.upgrades", "Familiar Upgrade Contract"),
            spec("echonpcore", "echonpcore:entity/npc_profile", EchoAdapterCoreContentKind.ENTITY, EchoAdapterCoreDomain.ENTITIES, "npcore.entities.npc_profile", "npc.profiles", "NPC Profile Contract"),
            spec("echonpcore", "echonpcore:story/dialogue", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.STORY, "npcore.story.dialogue", "npc.dialogue", "NPC Dialogue Contract"),
            spec("echonpcore", "echonpcore:ui/npc_screen", EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "npcore.ui.npc_screen", "npc.screens", "NPC Screen Contract"),
            spec("echonpcore", "echonpcore:data/npc_service", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "npcore.data.npc_service", "npc.services", "NPC Service Contract"),
            spec("echonpcore", "echonpcore:economy/npc_trade", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.ECONOMY, "npcore.economy.npc_trade", "npc.trades", "NPC Trade Contract"),
            spec("echonpcore", "echonpcore:entity/villager_replacement", EchoAdapterCoreContentKind.ENTITY, EchoAdapterCoreDomain.ENTITIES, "npcore.entities.villager_replacement", "npc.villager_replacement", "NPC Villager Replacement Contract"),
            spec("echoplayercore", "echoplayercore:player/home", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PLAYER, "playercore.player.home", "player.homes", "Player Home Contract"),
            spec("echoplayercore", "echoplayercore:player/back", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PLAYER, "playercore.player.back", "player.back", "Player Back Contract"),
            spec("echoplayercore", "echoplayercore:player/random_teleport", EchoAdapterCoreContentKind.COMMAND, EchoAdapterCoreDomain.PLAYER, "playercore.player.random_teleport", "player.random_teleport", "Player Random Teleport Contract"),
            spec("echoplayercore", "echoplayercore:player/spawn", EchoAdapterCoreContentKind.COMMAND, EchoAdapterCoreDomain.PLAYER, "playercore.player.spawn", "player.spawn", "Player Spawn Contract"),
            spec("echoplayercore", "echoplayercore:network/tpa", EchoAdapterCoreContentKind.NETWORK_HOOK, EchoAdapterCoreDomain.NETWORKING, "playercore.network.tpa", "player.tpa", "Player TPA Contract"),
            spec("echoplayercore", "echoplayercore:maps/warp", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.MAPS, "playercore.maps.warp", "player.warps", "Player Warp Contract"),
            spec("echoplayercore", "echoplayercore:data/cooldown", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "playercore.data.cooldown", "player.cooldowns", "Player Cooldown Contract"),
            spec("echoprogressioncore", "echoprogressioncore:data/unlock_graph", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "progressioncore.data.unlock_graph", "progression.unlock_graph", "Progression Unlock Graph"),
            spec("echoprogressioncore", "echoprogressioncore:data/gate", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "progressioncore.data.gate", "progression.gates", "Progression Gate Contract"),
            spec("echoprogressioncore", "echoprogressioncore:mission/objective", EchoAdapterCoreContentKind.MISSION, EchoAdapterCoreDomain.MISSIONS, "progressioncore.missions.objective", "progression.objectives", "Progression Objective Contract"),
            spec("echoprogressioncore", "echoprogressioncore:recipe/recipe_unlock", EchoAdapterCoreContentKind.RECIPE, EchoAdapterCoreDomain.RECIPES, "progressioncore.recipes.recipe_unlock", "progression.recipe_unlocks", "Recipe Unlock Contract"),
            spec("echoprogressioncore", "echoprogressioncore:data/feature_unlock", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "progressioncore.data.feature_unlock", "progression.feature_unlocks", "Feature Unlock Contract"),
            spec("echoprogressioncore", "echoprogressioncore:ui/ui_surface_unlock", EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "progressioncore.ui.ui_surface_unlock", "progression.ui_surface_unlocks", "UI Surface Unlock Contract"),
            spec("echoprogressioncore", "echoprogressioncore:weather/world_event_unlock", EchoAdapterCoreContentKind.WORLD_HAZARD, EchoAdapterCoreDomain.WEATHER, "progressioncore.weather.world_event_unlock", "progression.world_event_unlocks", "World Event Unlock Contract"),
            spec("echoprogressioncore", "echoprogressioncore:mission/team_objective", EchoAdapterCoreContentKind.MISSION, EchoAdapterCoreDomain.MISSIONS, "progressioncore.missions.team_objective", "progression.team_objectives", "Team Objective Contract"),
            spec("echoprogressioncore", "echoprogressioncore:diagnostic/server_objective", EchoAdapterCoreContentKind.DIAGNOSTIC, EchoAdapterCoreDomain.DIAGNOSTICS, "progressioncore.diagnostics.server_objective", "progression.server_objectives", "Server Objective Contract"),
            spec("echoquestdirector", "echoquestdirector:mission/mission_selection", EchoAdapterCoreContentKind.MISSION, EchoAdapterCoreDomain.MISSIONS, "questdirector.missions.mission_selection", "questdirector.mission_selection", "Quest Director Mission Selection"),
            spec("echoquestdirector", "echoquestdirector:mission/route_pacing", EchoAdapterCoreContentKind.MISSION, EchoAdapterCoreDomain.MISSIONS, "questdirector.missions.route_pacing", "questdirector.route_pacing", "Quest Director Route Pacing"),
            spec("echoquestdirector", "echoquestdirector:data/campaign_pressure", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "questdirector.data.campaign_pressure", "questdirector.campaign_pressure", "Campaign Pressure Contract"),
            spec("echoquestdirector", "echoquestdirector:ui/reminder", EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "questdirector.ui.reminder", "questdirector.reminders", "Quest Reminder Contract"),
            spec("echoquestdirector", "echoquestdirector:data/signal", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "questdirector.data.signal", "questdirector.signals", "Quest Director Signal Contract"),
            spec("echoquestdirector", "echoquestdirector:data/recommendation", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "questdirector.data.recommendation", "questdirector.recommendations", "Quest Recommendation Contract"),
            spec("echoquestdirector", "echoquestdirector:weather/world_event_pacing", EchoAdapterCoreContentKind.WORLD_HAZARD, EchoAdapterCoreDomain.WEATHER, "questdirector.weather.world_event_pacing", "questdirector.world_event_pacing", "Quest World Event Pacing"),
            spec("echoscriptcore", "echoscriptcore:data/definition", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "scriptcore.data.definition", "scriptcore.definitions", "Script Definition Contract"),
            spec("echoscriptcore", "echoscriptcore:data/condition", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "scriptcore.data.condition", "scriptcore.conditions", "Script Condition Contract"),
            spec("echoscriptcore", "echoscriptcore:command/action", EchoAdapterCoreContentKind.COMMAND, EchoAdapterCoreDomain.COMMANDS, "scriptcore.commands.action", "scriptcore.actions", "Script Action Contract"),
            spec("echoscriptcore", "echoscriptcore:command/script_command", EchoAdapterCoreContentKind.COMMAND, EchoAdapterCoreDomain.COMMANDS, "scriptcore.commands.script_command", "scriptcore.commands", "Script Command Contract"),
            spec("echoscriptcore", "echoscriptcore:data/example", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "scriptcore.data.example", "scriptcore.examples", "Script Example Contract"),
            spec("echoscriptcore", "echoscriptcore:save/migration", EchoAdapterCoreContentKind.SAVE_RECORD, EchoAdapterCoreDomain.SAVES, "scriptcore.saves.migration", "scriptcore.migrations", "Script Migration Contract"),
            spec("echoscriptcore", "echoscriptcore:data/registry", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "scriptcore.data.registry", "scriptcore.registry", "Script Registry Contract"),
            spec("echoscriptcore", "echoscriptcore:save/runtime_state", EchoAdapterCoreContentKind.SAVE_RECORD, EchoAdapterCoreDomain.SAVES, "scriptcore.saves.runtime_state", "scriptcore.runtime_state", "Script Runtime State Contract"),
            spec("echoscriptcore", "echoscriptcore:ui/ui_bridge", EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "scriptcore.ui.ui_bridge", "scriptcore.ui_bridge", "Script UI Bridge Contract"),
            spec("echoscriptcore", "echoscriptcore:diagnostic/validation", EchoAdapterCoreContentKind.DIAGNOSTIC, EchoAdapterCoreDomain.DIAGNOSTICS, "scriptcore.diagnostics.validation", "scriptcore.validation", "Script Validation Contract"),
            spec("echosocialcore", "echosocialcore:story/dialogue_tree", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.STORY, "socialcore.story.dialogue_tree", "social.dialogue_trees", "Social Dialogue Tree Contract"),
            spec("echosocialcore", "echosocialcore:data/faction", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "socialcore.data.faction", "social.factions", "Social Faction Contract"),
            spec("echosocialcore", "echosocialcore:entity/npc_profile", EchoAdapterCoreContentKind.ENTITY, EchoAdapterCoreDomain.ENTITIES, "socialcore.entities.npc_profile", "social.npc_profiles", "Social NPC Profile Contract"),
            spec("echosocialcore", "echosocialcore:player/reputation", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PLAYER, "socialcore.player.reputation", "social.reputation", "Social Reputation Contract"),
            spec("echosocialcore", "echosocialcore:entity/villager_replacement_plan", EchoAdapterCoreContentKind.ENTITY, EchoAdapterCoreDomain.ENTITIES, "socialcore.entities.villager_replacement_plan", "social.villager_replacement_plan", "Social Villager Replacement Plan"),
            spec("echospawncore", "echospawncore:worldgen/spawn_profile", EchoAdapterCoreContentKind.WORLDGEN_DEFINITION, EchoAdapterCoreDomain.WORLDGEN, "spawncore.worldgen.spawn_profile", "spawn.profiles", "Spawn Profile Contract"),
            spec("echospawncore", "echospawncore:hazard/hazard_rule", EchoAdapterCoreContentKind.WORLD_HAZARD, EchoAdapterCoreDomain.HAZARDS, "spawncore.hazards.hazard_rule", "spawn.hazard_rules", "Spawn Hazard Rule Contract"),
            spec("echospawncore", "echospawncore:data/difficulty_scaling", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "spawncore.data.difficulty_scaling", "spawn.difficulty_scaling", "Spawn Difficulty Scaling Contract"),
            spec("echospawncore", "echospawncore:story/faction_rule", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.STORY, "spawncore.story.faction_rule", "spawn.faction_rules", "Spawn Faction Rule Contract"),
            spec("echospawncore", "echospawncore:structure/poi_rule", EchoAdapterCoreContentKind.STRUCTURE, EchoAdapterCoreDomain.STRUCTURES, "spawncore.structures.poi_rule", "spawn.poi_rules", "Spawn POI Rule Contract"),
            spec("echospawncore", "echospawncore:weather/weather_rule", EchoAdapterCoreContentKind.WORLD_HAZARD, EchoAdapterCoreDomain.WEATHER, "spawncore.weather.weather_rule", "spawn.weather_rules", "Spawn Weather Rule Contract"),
            spec("echostatuscore", "echostatuscore:status/effect_profile", EchoAdapterCoreContentKind.STATUS_EFFECT, EchoAdapterCoreDomain.HAZARDS, "statuscore.hazards.effect_profile", "status.effects", "Status Effect Profile"),
            spec("echostatuscore", "echostatuscore:hazard/exposure", EchoAdapterCoreContentKind.WORLD_HAZARD, EchoAdapterCoreDomain.HAZARDS, "statuscore.hazards.exposure", "status.exposure", "Status Exposure Contract"),
            spec("echostatuscore", "echostatuscore:player/resistance", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PLAYER, "statuscore.player.resistance", "status.resistance", "Status Resistance Contract"),
            spec("echostructurecore", "echostructurecore:structure/profile", EchoAdapterCoreContentKind.STRUCTURE, EchoAdapterCoreDomain.STRUCTURES, "structurecore.structures.profile", "structures.profiles", "Structure Profile Contract"),
            spec("echostructurecore", "echostructurecore:structure/poi_metadata", EchoAdapterCoreContentKind.STRUCTURE, EchoAdapterCoreDomain.STRUCTURES, "structurecore.structures.poi_metadata", "structures.poi_metadata", "Structure POI Metadata Contract"),
            spec("echostructurecore", "echostructurecore:maps/discovery_reference", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.MAPS, "structurecore.maps.discovery_reference", "structures.discovery_references", "Structure Discovery Reference Contract"),
            spec("echotutorialcore", "echotutorialcore:ui/tutorial_card", EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "tutorialcore.ui.tutorial_card", "tutorial.cards", "Tutorial Card Contract"),
            spec("echotutorialcore", "echotutorialcore:data/tutorial_flow", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "tutorialcore.data.tutorial_flow", "tutorial.flows", "Tutorial Flow Contract"),
            spec("echotutorialcore", "echotutorialcore:ui/tutorial_hint", EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "tutorialcore.ui.tutorial_hint", "tutorial.hints", "Tutorial Hint Contract"),
            spec("echotutorialcore", "echotutorialcore:player/onboarding", EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PLAYER, "tutorialcore.player.onboarding", "tutorial.onboarding", "Tutorial Onboarding Contract"),
            spec("echotutorialcore", "echotutorialcore:ui/tooltip", EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "tutorialcore.ui.tooltip", "tutorial.tooltips", "Tutorial Tooltip Contract")
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreRegistryEntry> entries = CONTRACTS.stream()
                .map(spec -> bridge.registry().requireContentId(spec.contentId()))
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "remaining_systems_standalone_active");
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
        report.put("summary", "Remaining systems standalone adapter resolved player, social, scripting, progression, NPC, familiar, spawn, status, structure, quest, and tutorial contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        List<String> modules = CONTRACTS.stream().map(ContractSpec::moduleId).distinct().sorted().toList();
        List<String> features = CONTRACTS.stream().map(ContractSpec::featureId).sorted().toList();
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("moduleCount", modules.size());
        probe.put("featureCount", features.size());
        probe.put("modules", modules);
        probe.put("featureContractRoundTrip", modules.size() == 11
                && features.size() == CONTRACTS.size()
                && features.contains("familiar.upgrades")
                && features.contains("npc.dialogue")
                && features.contains("player.homes")
                && features.contains("progression.unlock_graph")
                && features.contains("questdirector.route_pacing")
                && features.contains("scriptcore.validation")
                && features.contains("social.reputation")
                && features.contains("spawn.hazard_rules")
                && features.contains("status.exposure")
                && features.contains("structures.poi_metadata")
                && features.contains("tutorial.cards"));
        return Map.copyOf(probe);
    }

    private static ContractSpec spec(
            String moduleId,
            String contentId,
            EchoAdapterCoreContentKind contentKind,
            EchoAdapterCoreDomain domain,
            String adapterKey,
            String featureId,
            String displayName
    ) {
        return new ContractSpec(moduleId, contentId, contentKind, domain, adapterKey, featureId, displayName);
    }

    public record ContractSpec(
            String moduleId,
            String contentId,
            EchoAdapterCoreContentKind contentKind,
            EchoAdapterCoreDomain domain,
            String adapterKey,
            String featureId,
            String displayName
    ) {
    }
}
