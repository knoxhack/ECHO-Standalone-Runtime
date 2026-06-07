package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.data.EchoDataRegistryStore;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelMaterialPattern;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class EchoAdapterCoreStandaloneContentBridge {
    public static final String RUNTIME_MARKER_BLOCK_ID = "echoadaptercore:runtime_marker_block";
    public static final String WATER_RATION_ITEM_ID = "echoashfallprotocol:clean_water_bottle";
    public static final String DIRTY_WATER_ITEM_ID = "echoashfallprotocol:dirty_water_bottle";
    public static final String FILTERED_WATER_ITEM_ID = "echoashfallprotocol:filtered_water_bottle";
    public static final String FIELD_RATION_ITEM_ID = "echoashfallprotocol:emergency_ration";
    public static final String FIELD_MANUAL_ITEM_ID = "echoashfallprotocol:field_manual";
    public static final String EMERGENCY_SCANNER_ITEM_ID = "echoashfallprotocol:portable_signal_scanner";
    public static final String POWER_REPAIR_KIT_ITEM_ID = "echoashfallprotocol:power_cell";
    public static final String ENERGY_CELL_ITEM_ID = "echoashfallprotocol:energy_cell";
    public static final String SCRAP_KNIFE_ITEM_ID = "echoashfallprotocol:scrap_knife";
    public static final String SCRAP_METAL_ITEM_ID = "echoashfallprotocol:scrap_metal";
    public static final String SCRAP_WIRE_ITEM_ID = "echoashfallprotocol:scrap_wire";
    public static final String SCRAP_CIRCUIT_ITEM_ID = "echoashfallprotocol:scrap_circuit";
    public static final String MACHINE_CASING_ITEM_ID = "echoashfallprotocol:machine_casing";
    public static final String GAS_MASK_ITEM_ID = "echoashfallprotocol:gas_mask";
    public static final String SCHEMATIC_FRAGMENT_ITEM_ID = "echoashfallprotocol:schematic_fragment";
    public static final String BASIC_FILTER_ITEM_ID = "echoashfallprotocol:filter_cartridge_basic";
    public static final String ADVANCED_FILTER_ITEM_ID = "echoashfallprotocol:filter_cartridge_advanced";
    public static final String DENSE_ALLOY_ITEM_ID = "echoashfallprotocol:dense_alloy_chunk";
    public static final String ALLOY_BLADE_ITEM_ID = "echoashfallprotocol:alloy_blade";
    public static final String ALLOY_HELMET_ITEM_ID = "echoashfallprotocol:alloy_helmet";
    public static final String ALLOY_CHESTPLATE_ITEM_ID = "echoashfallprotocol:alloy_chestplate";
    public static final String RELAY_SCANNER_LENS_ITEM_ID = "echoashfallprotocol:relay_scanner_lens";
    public static final String SCOUT_DRONE_ITEM_ID = "echoashfallprotocol:scout_drone_item";
    public static final String RAD_AWAY_ITEM_ID = "echoashfallprotocol:rad_away";
    public static final String STIM_PACK_ITEM_ID = "echoashfallprotocol:stim_pack";
    public static final String HAND_WARMER_ITEM_ID = "echoashfallprotocol:hand_warmer";
    public static final String THERMAL_LINER_ITEM_ID = "echoashfallprotocol:thermal_liner";
    public static final String RETURN_BEACON_ITEM_ID = "echoashfallprotocol:return_beacon";
    public static final String RETURN_KEYSTONE_ITEM_ID = "echoashfallprotocol:return_keystone";
    public static final String SHELTER_ANCHOR_BLOCK_ID = "echoashfallprotocol:ash_campfire";
    public static final String TOXIC_ASH_BLOCK_ID = "echoashfallprotocol:fallout_dust";
    public static final String SCORCHED_BASALT_BLOCK_ID = "echoashfallprotocol:scorched_ash";
    public static final String RUSTED_DEBRIS_BLOCK_ID = "echoashfallprotocol:rusted_metal_debris";
    public static final String ASH_HAZARD_MARKER_BLOCK_ID = "echoashfallprotocol:toxic_waste_barrel";
    public static final String FIELD_TERMINAL_BLOCK_ID = "echoterminal:echo_terminal";
    public static final String CRASH_CACHE_BLOCK_ID = "echoashfallprotocol:echo_cache";
    public static final String DAMAGED_POWER_NODE_BLOCK_ID = "echoashfallprotocol:power_node";
    public static final String RAIN_COLLECTOR_BLOCK_ID = "echoashfallprotocol:rain_collector";
    public static final String WATER_PURIFIER_BLOCK_ID = "echoashfallprotocol:water_purifier";
    public static final String HAND_RECYCLER_BLOCK_ID = "echoashfallprotocol:hand_recycler";
    public static final String MICRO_GENERATOR_BLOCK_ID = "echoashfallprotocol:micro_generator";
    public static final String POWER_CABLE_BLOCK_ID = "echoashfallprotocol:power_cable";
    public static final String ENERGY_METER_BLOCK_ID = "echoashfallprotocol:energy_meter";
    public static final String SCRAP_DYNAMO_BLOCK_ID = "echoashfallprotocol:scrap_dynamo";
    public static final String BATTERY_BANK_BLOCK_ID = "echoashfallprotocol:battery_bank";
    public static final String THERMAL_BURNER_BLOCK_ID = "echoashfallprotocol:thermal_burner";
    public static final String SCRAP_PRESS_BLOCK_ID = "echoashfallprotocol:scrap_press";
    public static final String ITEM_PIPE_BLOCK_ID = "echoashfallprotocol:item_pipe";
    public static final String FACTORY_CONTROLLER_BLOCK_ID = "echoashfallprotocol:factory_controller";
    public static final String RESEARCH_LAB_BLOCK_ID = "echoashfallprotocol:research_lab";
    public static final String REINFORCED_POWER_CABLE_BLOCK_ID = "echoashfallprotocol:reinforced_power_cable";
    public static final String HIGH_VOLTAGE_POWER_CABLE_BLOCK_ID =
            "echoashfallprotocol:high_voltage_power_cable";
    public static final String LOAD_DISTRIBUTOR_BLOCK_ID = "echoashfallprotocol:load_distributor";
    public static final String ORE_GRINDER_BLOCK_ID = "echoashfallprotocol:ore_grinder";
    public static final String ISOTOPE_REFINER_BLOCK_ID = "echoashfallprotocol:isotope_refiner";
    public static final String CRYSTALLINE_SYNTHESIZER_BLOCK_ID =
            "echoashfallprotocol:crystalline_synthesizer";
    public static final String DEEP_CORE_MINER_BLOCK_ID = "echoashfallprotocol:deep_core_miner";
    public static final String AUTOFEED_HOPPER_BLOCK_ID = "echoashfallprotocol:autofeed_hopper";
    public static final String CONTAMINANT_CONDENSER_BLOCK_ID =
            "echoashfallprotocol:contaminant_condenser";
    public static final String THERMAL_ARRAY_BLOCK_ID = "echoashfallprotocol:thermal_array";
    public static final String ATMOSPHERIC_SCRUBBER_BLOCK_ID = "echoashfallprotocol:atmospheric_scrubber";
    public static final String RADIATION_CLEANSER_BLOCK_ID = "echoashfallprotocol:radiation_cleanser";
    public static final String FIELD_MED_BAY_BLOCK_ID = "echoashfallprotocol:field_med_bay";
    public static final String FILTER_WORKBENCH_BLOCK_ID = "echoashfallprotocol:filter_workbench";
    public static final String RELAY_STATION_BLOCK_ID = "echoashfallprotocol:relay_station";
    public static final String SIGNAL_SCANNER_BLOCK_ID = "echoashfallprotocol:signal_scanner";
    public static final String STRUCTURE_CACHE_BLOCK_ID = "echoashfallprotocol:structure_cache";
    public static final String ECHO_CRATE_BLOCK_ID = "echoashfallprotocol:echo_crate";
    public static final String NEXUS_CORE_BLOCK_ID = "echoashfallprotocol:nexus_core";
    public static final String NEXUS_CAPACITOR_BLOCK_ID = "echoashfallprotocol:nexus_capacitor";
    public static final String SCOUT_DRONE_ENTITY_ID = "echoashfallprotocol:entity/scout_drone";
    public static final String NEXUS_SCAR_AVATAR_ENTITY_ID = "echoashfallprotocol:entity/nexus_scar_avatar";
    public static final List<String> ASHFALL_ADDITIONAL_ENTITY_IDS = List.of(
            "ash_wraith",
            "city_ruin_stalker",
            "city_stalker",
            "corruption_bloom",
            "crash_survivor",
            "crash_zone_colossus",
            "cryogenic_overseer",
            "echo_companion_drone",
            "echo_drone",
            "faction_npc",
            "feral_human",
            "glowing_ghoul",
            "gridbound_husk",
            "industrial_juggernaut",
            "irradiated_wolf",
            "mirror_command",
            "mutated_crawler",
            "nexus_nullifier",
            "nexus_scar_avatar",
            "plains_warlord",
            "rad_zombie",
            "radiation_behemoth",
            "relay_warden",
            "rust_walker",
            "severance_engine",
            "signal_leech",
            "steam_wraith",
            "toxic_hive_matriarch",
            "toxic_slime",
            "warden_boss",
            "wasteland_sentinel",
            "wild_dog"
    );
    public static final List<String> ASHFALL_SPAWN_EGG_ENTITY_IDS = List.of(
            "ash_wraith",
            "city_ruin_stalker",
            "city_stalker",
            "corruption_bloom",
            "crash_survivor",
            "crash_zone_colossus",
            "cryogenic_overseer",
            "echo_companion_drone",
            "echo_drone",
            "feral_human",
            "glowing_ghoul",
            "gridbound_husk",
            "industrial_juggernaut",
            "irradiated_wolf",
            "mirror_command",
            "mutated_crawler",
            "nexus_nullifier",
            "nexus_scar_avatar",
            "plains_warlord",
            "rad_zombie",
            "radiation_behemoth",
            "relay_warden",
            "rust_walker",
            "scavenger_bandit",
            "scout_drone",
            "severance_engine",
            "signal_leech",
            "steam_wraith",
            "toxic_hive_matriarch",
            "toxic_slime",
            "warden_boss",
            "wasteland_sentinel",
            "wild_dog"
    );
    public static final List<String> ASHFALL_ENVIRONMENT_BLOCK_IDS = List.of(
            "acid_mud",
            "acidic_sludge",
            "ash_bush",
            "ash_layer",
            "ash_stone",
            "ashen_wasteland_dirt",
            "blue_ice_crystal",
            "burnt_fern",
            "burnt_grass",
            "burnt_tall_grass",
            "burnt_wasteland_soil",
            "cable_bundle",
            "charred_wood_log",
            "concrete_chunk",
            "concrete_rubble",
            "contaminated_soil",
            "corroded_pipe",
            "cracked_asphalt",
            "cracked_earth",
            "crash_slag",
            "cryogenic_fractured_stone",
            "dead_wood_log",
            "debris_block",
            "deep_ash",
            "drop_pod_glass",
            "drop_pod_hull",
            "dry_grass",
            "dry_tall_grass",
            "echo_crystal",
            "emergency_bunk",
            "energized_fissure",
            "frozen_conduit",
            "industrial_aggregate",
            "irradiated_cactus",
            "irradiated_crust",
            "irradiated_shale",
            "mutated_bush",
            "mutated_leaves_gray",
            "mutated_leaves_purple",
            "mutated_sapling",
            "mutated_wasteland_grass_block",
            "nexus_cracked_soil",
            "nuclear_fungus",
            "nuclear_grass",
            "nuclear_tall_grass",
            "oil_stained_concrete",
            "ooze_crystal",
            "permafrost",
            "radiation_block",
            "radioactive_sludge",
            "rebar_block",
            "riftstone",
            "rubble",
            "rusted_metal_sheet",
            "rusty_wheat",
            "scattered_bones",
            "scrap_ore",
            "shattered_glass",
            "supply_crate",
            "thorn_scrub",
            "toxic_grass",
            "toxic_moss",
            "toxic_puddle",
            "toxic_slagstone",
            "toxic_tall_grass",
            "toxic_wasteland_grass_block",
            "twisted_metal",
            "uranium_crystal",
            "wasteland_dirt",
            "wasteland_grass",
            "wasteland_grass_block",
            "wasteland_reed",
            "wasteland_stone",
            "wasteland_tall_grass",
            "wasteland_trace_rubble",
            "wild_berry_bush",
            "workshop_block"
    );
    public static final String RUSTED_DEBRIS_LOOT_ID = "echoashfallprotocol:loot/blocks/rusted_metal_debris";
    public static final String CRASH_CACHE_LOOT_ID = "echoashfallprotocol:loot/blocks/echo_cache";
    public static final String DAMAGED_POWER_NODE_LOOT_ID = "echoashfallprotocol:loot/blocks/power_node";
    public static final String TOXIC_ASH_HAZARD_ID = "echoashfallprotocol:world_hazard/toxic_ash";
    public static final String HOT_ASH_HAZARD_ID = "echoashfallprotocol:world_hazard/hot_ash";
    public static final String UNSTABLE_GROUND_HAZARD_ID = "echoashfallprotocol:world_hazard/unstable_ground";
    public static final String ELECTRICAL_DISCHARGE_HAZARD_ID = "echopowergrid:world_hazard/electrical_discharge";
    public static final String EXTRACTION_STORM_HAZARD_ID = "echoashfallprotocol:world_hazard/extraction_storm";
    public static final String SURVIVAL_PROFILE_ID = "echoashfallprotocol:survival_profile/basic_needs";
    public static final String WATER_LOOP_PROFILE_ID = "echoashfallprotocol:survival_profile/emergency_water_loop";
    public static final String TOOL_PROFILE_ID = "echoashfallprotocol:tool_profile/scrap_field_tools";
    public static final String FIELD_WORKSHOP_PROFILE_ID = "echoashfallprotocol:workshop_profile/field_recycler";
    public static final String FIELD_POWER_PROFILE_ID = "echoashfallprotocol:power_profile/field_microgrid";
    public static final String MACHINE_POWER_PROFILE_ID = "echoashfallprotocol:power_profile/machine_route";
    public static final String MIDGAME_PROGRESSION_PROFILE_ID =
            "echoashfallprotocol:progression_profile/midgame_factory";
    public static final String EXPEDITION_SAFETY_PROFILE_ID =
            "echoashfallprotocol:safety_profile/expedition_hazards";
    public static final String ADVANCED_EXPEDITION_PROFILE_ID =
            "echoashfallprotocol:progression_profile/advanced_expedition";
    public static final String FIELD_RECOVERY_PROFILE_ID = "echoashfallprotocol:recovery_profile/field_recovery";
    public static final String NEXUS_ROUTE_PROFILE_ID = "echoashfallprotocol:progression_profile/nexus_route";
    public static final String STORED_ENERGY_COMPONENT_ID = "echoashfallprotocol:component/stored_energy";
    public static final String ASHFALL_TOOLTIP_COMPONENT_ID = "echoashfallprotocol:component/ashfall_tooltip";
    public static final String ALLIANCE_EFFECT_ID = "echoashfallprotocol:effect/alliance";
    public static final List<String> ASHFALL_MENU_SCREEN_IDS = List.of(
            "crystalline_synthesizer",
            "deep_core_miner",
            "filter_workbench",
            "hand_recycler",
            "isotope_refiner",
            "machine_status",
            "micro_generator",
            "ore_grinder",
            "radiation_cleanser",
            "research_lab",
            "scrap_press",
            "thermal_array",
            "thermal_burner",
            "water_purifier"
    );
    public static final String CRAFT_SCRAP_KNIFE_MISSION_ID = "echoashfallprotocol:mission/craft_scrap_knife";
    public static final String BUILD_HAND_RECYCLER_MISSION_ID = "echoashfallprotocol:mission/build_hand_recycler";
    public static final String MAKE_MACHINE_CASING_MISSION_ID = "echoashfallprotocol:mission/make_machine_casing";
    public static final String ASSEMBLE_FIELD_KIT_MISSION_ID =
            "echoashfallprotocol:mission/assemble_wasteland_field_kit";
    public static final String BUILD_MICRO_GENERATOR_MISSION_ID =
            "echoashfallprotocol:mission/build_micro_generator";
    public static final String ROUTE_POWER_CABLE_MISSION_ID = "echoashfallprotocol:mission/route_power_cable";
    public static final String INSTALL_ENERGY_METER_MISSION_ID =
            "echoashfallprotocol:mission/install_energy_meter";
    public static final String BUILD_SCRAP_DYNAMO_MISSION_ID =
            "echoashfallprotocol:mission/build_scrap_dynamo";
    public static final String CHARGE_BASIC_BATTERY_MISSION_ID =
            "echoashfallprotocol:mission/charge_basic_battery";
    public static final String BUILD_BATTERY_BANK_MISSION_ID =
            "echoashfallprotocol:mission/build_battery_bank";
    public static final String BUILD_THERMAL_BURNER_MISSION_ID =
            "echoashfallprotocol:mission/build_thermal_burner";
    public static final String EQUIP_GAS_MASK_MISSION_ID = "echoashfallprotocol:mission/equip_gas_mask";
    public static final String FIND_SCHEMATIC_FRAGMENT_MISSION_ID =
            "echoashfallprotocol:mission/find_schematic_fragment";
    public static final String FIRST_SCHEMATIC_MISSION_ID = "echoashfallprotocol:mission/first_schematic";
    public static final String BUILD_SCRAP_PRESS_MISSION_ID = "echoashfallprotocol:mission/build_scrap_press";
    public static final String INSTALL_ITEM_PIPE_MISSION_ID = "echoashfallprotocol:mission/install_item_pipe";
    public static final String BUILD_FACTORY_CONTROLLER_MISSION_ID =
            "echoashfallprotocol:mission/build_factory_controller";
    public static final String BUILD_RESEARCH_LAB_MISSION_ID = "echoashfallprotocol:mission/build_research_lab";
    public static final String UPGRADE_POWER_CABLE_MISSION_ID = "echoashfallprotocol:mission/upgrade_power_cable";
    public static final String SET_POWER_PRIORITY_MISSION_ID = "echoashfallprotocol:mission/set_power_priority";
    public static final String OVERCLOCK_MACHINE_MISSION_ID = "echoashfallprotocol:mission/overclock_machine";
    public static final String FIX_MASK_FILTER_MISSION_ID = "echoashfallprotocol:mission/fix_mask_filter";
    public static final String CRAFT_ADVANCED_FILTER_MISSION_ID =
            "echoashfallprotocol:mission/craft_advanced_filter";
    public static final String BUILD_THERMAL_ARRAY_MISSION_ID = "echoashfallprotocol:mission/build_thermal_array";
    public static final String WARM_UP_AFTER_EXPOSURE_MISSION_ID =
            "echoashfallprotocol:mission/warm_up_after_exposure";
    public static final String BUILD_ATMOSPHERIC_SCRUBBER_MISSION_ID =
            "echoashfallprotocol:mission/build_atmospheric_scrubber";
    public static final String BUILD_RADIATION_CLEANSER_MISSION_ID =
            "echoashfallprotocol:mission/build_radiation_cleanser";
    public static final String BUILD_FIELD_MED_BAY_MISSION_ID = "echoashfallprotocol:mission/build_field_med_bay";
    public static final String USE_FIELD_MED_BAY_MISSION_ID = "echoashfallprotocol:mission/use_field_med_bay";
    public static final String BUILD_FILTER_WORKBENCH_MISSION_ID =
            "echoashfallprotocol:mission/build_filter_workbench";
    public static final String BUILD_ORE_GRINDER_MISSION_ID = "echoashfallprotocol:mission/build_ore_grinder";
    public static final String FIND_DENSE_ALLOY_MISSION_ID = "echoashfallprotocol:mission/find_dense_alloy";
    public static final String BUILD_ISOTOPE_REFINER_MISSION_ID =
            "echoashfallprotocol:mission/build_isotope_refiner";
    public static final String FORGE_ALLOY_WEAPON_MISSION_ID = "echoashfallprotocol:mission/forge_alloy_weapon";
    public static final String EQUIP_ALLOY_KIT_MISSION_ID = "echoashfallprotocol:mission/equip_alloy_kit";
    public static final String ACTIVATE_RELAY_STATION_MISSION_ID =
            "echoashfallprotocol:mission/activate_relay_station";
    public static final String BUILD_SCOUT_DRONE_MISSION_ID = "echoashfallprotocol:mission/build_scout_drone";
    public static final String USE_RAD_AWAY_MISSION_ID = "echoashfallprotocol:mission/use_rad_away";
    public static final String USE_STIM_PACK_MISSION_ID = "echoashfallprotocol:mission/use_stim_pack";
    public static final String USE_HAND_WARMER_MISSION_ID = "echoashfallprotocol:mission/use_hand_warmer";
    public static final String INSTALL_THERMAL_LINER_MISSION_ID =
            "echoashfallprotocol:mission/install_thermal_liner";
    public static final String PLACE_RETURN_BEACON_MISSION_ID =
            "echoashfallprotocol:mission/place_return_beacon";
    public static final String BIND_RETURN_KEYSTONE_MISSION_ID =
            "echoashfallprotocol:mission/bind_return_keystone";
    public static final String BUILD_NEXUS_CAPACITOR_MISSION_ID =
            "echoashfallprotocol:mission/build_nexus_capacitor";
    public static final String FIND_NEXUS_CORE_MISSION_ID = "echoashfallprotocol:mission/find_nexus_core";
    public static final String AWAKEN_NEXUS_CORE_MISSION_ID = "echoashfallprotocol:mission/awaken_nexus_core";
    public static final String SCAN_PRIME_RELAYS_MISSION_ID = "echoashfallprotocol:mission/scan_prime_relays";
    public static final String RESOLVE_PRIME_RELAYS_MISSION_ID = "echoashfallprotocol:mission/resolve_prime_relays";
    public static final String STABILIZE_NEXUS_GRID_MISSION_ID =
            "echoashfallprotocol:mission/stabilize_nexus_grid";
    public static final String NEUTRALIZE_NEXUS_SCAR_AVATAR_MISSION_ID =
            "echoashfallprotocol:mission/neutralize_nexus_scar_avatar";
    public static final String REACH_DECISION_MISSION_ID = "echoashfallprotocol:mission/reach_decision";
    public static final String SCRAP_KNIFE_RECIPE_ID = "echoashfallprotocol:recipe/scrap_knife";
    public static final String SIGNALOS_TERMINAL_SURFACE_ID = "signalos:ui/terminal";
    public static final String SIGNALOS_ARCHIVE_ENTRY_ID = "signalos:archive/field_cache";
    public static final String SIGNALOS_DATA_DRIVE_ID = "signalos:data_drive/handoff_drive";
    public static final String SIGNALOS_SIGNAL_MESSAGE_ID = "signalos:signal/secure_cache";
    public static final String SIGNALOS_STORY_FLAG_ID = "signalos:story_flag/cache_secured";
    public static final String SIGNALOS_STORY_MISSION_ID = "signalos:mission/secure_cache";
    public static final String SIGNALOS_CHAPTER_UNLOCK_ID = "signalos:chapter/cache_handoff";
    public static final String SIGNALOS_PRESENCE_LINK_ID = "echopresencelink:presence/signalos_cache";
    public static final String SIGNALOS_STORY_SAVE_ID = "signalos:save/story_state";
    public static final String RELICTECH_ECHO_MIRROR_EFFECT_ID = "echorelictech:relic_effect/echo_mirror";
    public static final String SPELLCORE_SIGNAL_PULSE_ID = "echospellcore:spell/signal_pulse";
    public static final String RITUALCORE_RELIC_STABILIZATION_ID =
            "echoritualcore:ritual/relic_stabilization";
    public static final String RITUALCORE_RELIC_STABILIZED_FLAG_ID =
            "echoritualcore:story_flag/relic_stabilized";
    public static final String CURSECORE_ECHO_ROT_ID = "echocursecore:curse/echo_rot";
    public static final String RIFTWORLDS_CACHE_ECHO_ID = "echoriftworlds:rift_event/cache_echo";
    public static final String BLACKBOX_CORE_MEMORY_ARCHIVE_ID = "echoblackboxprotocol:archive/core_memory";
    public static final String ORBITAL_BLACKBOX_DATA_DRIVE_ID =
            "echoorbitalremnants:data_drive/orbital_blackbox";
    public static final String NEXUS_HANDOFF_SIGNAL_ID = "echonexusprotocol:signal/nexus_handoff";
    public static final String PRIME_ROUTE_STORY_FLAG_ID = "echoprimecore:story_flag/prime_route_unlocked";
    public static final String PRIME_ROUTE_MISSION_ID = "echoprimecore:mission/prime_route";
    public static final String STATIONFALL_ROUTE_CHAPTER_ID = "echostationfall:chapter/stationfall_route";
    public static final String PRIME_ROUTE_PRESENCE_ID = "echopresencelink:presence/prime_route";
    public static final String GRIMOIRE_ARCANE_CODEX_ARCHIVE_ID = "echogrimoire:archive/arcane_codex";
    public static final String SIGNALOS_EXAMPLE_ARCANE_CODEX_DRIVE_ID =
            "signalosexample:data_drive/arcane_codex_demo";
    public static final String ARCANA_CORE_AETHER_WAKE_SIGNAL_ID = "echoarcanacore:signal/aether_wake";
    public static final String ARCANA_CORE_CODEX_STORY_FLAG_ID =
            "echoarcanacore:story_flag/arcane_codex_unlocked";
    public static final String ARCANA_CORE_CODEX_MISSION_ID = "echoarcanacore:mission/arcane_codex_sync";
    public static final String RELICTECH_PHASE_ANCHOR_EFFECT_ID = "echorelictech:relic_effect/phase_anchor";
    public static final String ARCANE_INDEX_CODEX_CHAPTER_ID = "echoarcaneindex:chapter/arcane_codex";
    public static final String AETHERWORKS_AETHER_SYNC_PRESENCE_ID = "echoaetherworks:presence/aether_sync";

    private final EchoAdapterCoreStandaloneRegistry registry;
    private final List<EchoAdapterCoreContentBinding> bindings;
    private final EchoAdapterCoreRenderTarget renderTarget;
    private final EchoDataRegistryStore dataStore;

    private EchoAdapterCoreStandaloneContentBridge(
            EchoAdapterCoreStandaloneRegistry registry,
            EchoAdapterCoreRenderTarget renderTarget,
            EchoDataRegistryStore dataStore
    ) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.bindings = registry.bindings();
        this.renderTarget = Objects.requireNonNull(renderTarget, "renderTarget");
        this.dataStore = dataStore;
    }

    public static EchoAdapterCoreStandaloneContentBridge ashfallLive() {
        return ashfallLive(null);
    }

    public static EchoAdapterCoreStandaloneContentBridge ashfallLive(EchoDataRegistryStore dataStore) {
        return new EchoAdapterCoreStandaloneContentBridge(
                new EchoAdapterCoreStandaloneRegistry(withRemainingSystemContracts(List.of(
                        block(
                                "echoadaptercore",
                                "echoadaptercore:block/runtime_marker_block",
                                "adapter.runtime_standalone.marker_block",
                                RUNTIME_MARKER_BLOCK_ID,
                                RUNTIME_MARKER_BLOCK_ID,
                                RUNTIME_MARKER_BLOCK_ID,
                                RUNTIME_MARKER_BLOCK_ID,
                                "Runtime Marker",
                                0xFF67C7F0,
                                0xFFE8F8FF,
                                EchoVoxelMaterialPattern.MARKER_GRID,
                                0.8D
                        ),
                        virtual(
                                "signalos",
                                SIGNALOS_TERMINAL_SURFACE_ID,
                                EchoAdapterCoreContentKind.UI_SCREEN,
                                EchoAdapterCoreDomain.UI_SCREENS,
                                "story.signalos.terminal",
                                "signalos:terminal",
                                "signalos:terminal",
                                "signalos:terminal",
                                SIGNALOS_TERMINAL_SURFACE_ID,
                                "SignalOS Terminal"
                        ),
                        virtual(
                                "signalos",
                                SIGNALOS_ARCHIVE_ENTRY_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.signalos.archive.field_cache",
                                SIGNALOS_ARCHIVE_ENTRY_ID,
                                SIGNALOS_ARCHIVE_ENTRY_ID,
                                SIGNALOS_ARCHIVE_ENTRY_ID,
                                SIGNALOS_ARCHIVE_ENTRY_ID,
                                "Field Cache Archive"
                        ),
                        virtual(
                                "signalos",
                                SIGNALOS_DATA_DRIVE_ID,
                                EchoAdapterCoreContentKind.ITEM,
                                EchoAdapterCoreDomain.ITEMS,
                                "story.signalos.data_drive.handoff",
                                SIGNALOS_DATA_DRIVE_ID,
                                SIGNALOS_DATA_DRIVE_ID,
                                SIGNALOS_DATA_DRIVE_ID,
                                SIGNALOS_DATA_DRIVE_ID,
                                "Handoff Data Drive"
                        ),
                        virtual(
                                "signalos",
                                SIGNALOS_SIGNAL_MESSAGE_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.signalos.signal.secure_cache",
                                SIGNALOS_SIGNAL_MESSAGE_ID,
                                SIGNALOS_SIGNAL_MESSAGE_ID,
                                SIGNALOS_SIGNAL_MESSAGE_ID,
                                SIGNALOS_SIGNAL_MESSAGE_ID,
                                "Secure Cache Signal"
                        ),
                        virtual(
                                "signalos",
                                SIGNALOS_STORY_FLAG_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "story.signalos.flag.cache_secured",
                                SIGNALOS_STORY_FLAG_ID,
                                SIGNALOS_STORY_FLAG_ID,
                                SIGNALOS_STORY_FLAG_ID,
                                SIGNALOS_STORY_FLAG_ID,
                                "Cache Secured Story Flag"
                        ),
                        virtual(
                                "signalos",
                                SIGNALOS_STORY_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "story.signalos.mission.secure_cache",
                                SIGNALOS_STORY_MISSION_ID,
                                SIGNALOS_STORY_MISSION_ID,
                                SIGNALOS_STORY_MISSION_ID,
                                SIGNALOS_STORY_MISSION_ID,
                                "Secure Cache Mission"
                        ),
                        virtual(
                                "signalos",
                                SIGNALOS_CHAPTER_UNLOCK_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.STORY,
                                "story.signalos.chapter.cache_handoff",
                                SIGNALOS_CHAPTER_UNLOCK_ID,
                                SIGNALOS_CHAPTER_UNLOCK_ID,
                                SIGNALOS_CHAPTER_UNLOCK_ID,
                                SIGNALOS_CHAPTER_UNLOCK_ID,
                                "Cache Handoff Chapter"
                        ),
                        virtual(
                                "echopresencelink",
                                SIGNALOS_PRESENCE_LINK_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.signalos.presence.cache",
                                SIGNALOS_PRESENCE_LINK_ID,
                                SIGNALOS_PRESENCE_LINK_ID,
                                SIGNALOS_PRESENCE_LINK_ID,
                                SIGNALOS_PRESENCE_LINK_ID,
                                "SignalOS Cache Presence Link"
                        ),
                        virtual(
                                "signalos",
                                SIGNALOS_STORY_SAVE_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "story.signalos.save.state",
                                SIGNALOS_STORY_SAVE_ID,
                                SIGNALOS_STORY_SAVE_ID,
                                SIGNALOS_STORY_SAVE_ID,
                                SIGNALOS_STORY_SAVE_ID,
                                "SignalOS Story State"
                        ),
                        virtual(
                                "echorelictech",
                                RELICTECH_ECHO_MIRROR_EFFECT_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.relictech.effect.echo_mirror",
                                RELICTECH_ECHO_MIRROR_EFFECT_ID,
                                RELICTECH_ECHO_MIRROR_EFFECT_ID,
                                RELICTECH_ECHO_MIRROR_EFFECT_ID,
                                RELICTECH_ECHO_MIRROR_EFFECT_ID,
                                "Echo Mirror Relic Effect"
                        ),
                        virtual(
                                "echospellcore",
                                SPELLCORE_SIGNAL_PULSE_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.spellcore.spell.signal_pulse",
                                SPELLCORE_SIGNAL_PULSE_ID,
                                SPELLCORE_SIGNAL_PULSE_ID,
                                SPELLCORE_SIGNAL_PULSE_ID,
                                SPELLCORE_SIGNAL_PULSE_ID,
                                "Signal Pulse Spell"
                        ),
                        virtual(
                                "echoritualcore",
                                RITUALCORE_RELIC_STABILIZATION_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.ritualcore.ritual.relic_stabilization",
                                RITUALCORE_RELIC_STABILIZATION_ID,
                                RITUALCORE_RELIC_STABILIZATION_ID,
                                RITUALCORE_RELIC_STABILIZATION_ID,
                                RITUALCORE_RELIC_STABILIZATION_ID,
                                "Relic Stabilization Ritual"
                        ),
                        virtual(
                                "echoritualcore",
                                RITUALCORE_RELIC_STABILIZED_FLAG_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "story.ritualcore.flag.relic_stabilized",
                                RITUALCORE_RELIC_STABILIZED_FLAG_ID,
                                RITUALCORE_RELIC_STABILIZED_FLAG_ID,
                                RITUALCORE_RELIC_STABILIZED_FLAG_ID,
                                RITUALCORE_RELIC_STABILIZED_FLAG_ID,
                                "Relic Stabilized Ritual Flag"
                        ),
                        virtual(
                                "echocursecore",
                                CURSECORE_ECHO_ROT_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.cursecore.curse.echo_rot",
                                CURSECORE_ECHO_ROT_ID,
                                CURSECORE_ECHO_ROT_ID,
                                CURSECORE_ECHO_ROT_ID,
                                CURSECORE_ECHO_ROT_ID,
                                "Echo Rot Curse"
                        ),
                        virtual(
                                "echoriftworlds",
                                RIFTWORLDS_CACHE_ECHO_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.riftworlds.event.cache_echo",
                                RIFTWORLDS_CACHE_ECHO_ID,
                                RIFTWORLDS_CACHE_ECHO_ID,
                                RIFTWORLDS_CACHE_ECHO_ID,
                                RIFTWORLDS_CACHE_ECHO_ID,
                                "Cache Echo Rift Event"
                        ),
                        virtual(
                                "echoblackboxprotocol",
                                BLACKBOX_CORE_MEMORY_ARCHIVE_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.blackbox.archive.core_memory",
                                BLACKBOX_CORE_MEMORY_ARCHIVE_ID,
                                BLACKBOX_CORE_MEMORY_ARCHIVE_ID,
                                BLACKBOX_CORE_MEMORY_ARCHIVE_ID,
                                BLACKBOX_CORE_MEMORY_ARCHIVE_ID,
                                "Blackbox Core Memory Archive"
                        ),
                        virtual(
                                "echoorbitalremnants",
                                ORBITAL_BLACKBOX_DATA_DRIVE_ID,
                                EchoAdapterCoreContentKind.ITEM,
                                EchoAdapterCoreDomain.ITEMS,
                                "story.orbital.data_drive.blackbox",
                                ORBITAL_BLACKBOX_DATA_DRIVE_ID,
                                ORBITAL_BLACKBOX_DATA_DRIVE_ID,
                                ORBITAL_BLACKBOX_DATA_DRIVE_ID,
                                ORBITAL_BLACKBOX_DATA_DRIVE_ID,
                                "Orbital Blackbox Data Drive"
                        ),
                        virtual(
                                "echonexusprotocol",
                                NEXUS_HANDOFF_SIGNAL_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.nexus.signal.handoff",
                                NEXUS_HANDOFF_SIGNAL_ID,
                                NEXUS_HANDOFF_SIGNAL_ID,
                                NEXUS_HANDOFF_SIGNAL_ID,
                                NEXUS_HANDOFF_SIGNAL_ID,
                                "Nexus Handoff Signal"
                        ),
                        virtual(
                                "echoprimecore",
                                PRIME_ROUTE_STORY_FLAG_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "story.prime.flag.route_unlocked",
                                PRIME_ROUTE_STORY_FLAG_ID,
                                PRIME_ROUTE_STORY_FLAG_ID,
                                PRIME_ROUTE_STORY_FLAG_ID,
                                PRIME_ROUTE_STORY_FLAG_ID,
                                "Prime Route Story Flag"
                        ),
                        virtual(
                                "echoprimecore",
                                PRIME_ROUTE_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "story.prime.mission.route",
                                PRIME_ROUTE_MISSION_ID,
                                PRIME_ROUTE_MISSION_ID,
                                PRIME_ROUTE_MISSION_ID,
                                PRIME_ROUTE_MISSION_ID,
                                "Prime Route Mission"
                        ),
                        virtual(
                                "echostationfall",
                                STATIONFALL_ROUTE_CHAPTER_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.STORY,
                                "story.stationfall.chapter.route",
                                STATIONFALL_ROUTE_CHAPTER_ID,
                                STATIONFALL_ROUTE_CHAPTER_ID,
                                STATIONFALL_ROUTE_CHAPTER_ID,
                                STATIONFALL_ROUTE_CHAPTER_ID,
                                "Stationfall Route Chapter"
                        ),
                        virtual(
                                "echopresencelink",
                                PRIME_ROUTE_PRESENCE_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.presence.prime_route",
                                PRIME_ROUTE_PRESENCE_ID,
                                PRIME_ROUTE_PRESENCE_ID,
                                PRIME_ROUTE_PRESENCE_ID,
                                PRIME_ROUTE_PRESENCE_ID,
                                "Prime Route Presence Link"
                        ),
                        virtual(
                                "echogrimoire",
                                GRIMOIRE_ARCANE_CODEX_ARCHIVE_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.grimoire.archive.arcane_codex",
                                GRIMOIRE_ARCANE_CODEX_ARCHIVE_ID,
                                GRIMOIRE_ARCANE_CODEX_ARCHIVE_ID,
                                GRIMOIRE_ARCANE_CODEX_ARCHIVE_ID,
                                GRIMOIRE_ARCANE_CODEX_ARCHIVE_ID,
                                "Grimoire Arcane Codex Archive"
                        ),
                        virtual(
                                "signalosexample",
                                SIGNALOS_EXAMPLE_ARCANE_CODEX_DRIVE_ID,
                                EchoAdapterCoreContentKind.ITEM,
                                EchoAdapterCoreDomain.ITEMS,
                                "story.signalos_example.data_drive.arcane_codex",
                                SIGNALOS_EXAMPLE_ARCANE_CODEX_DRIVE_ID,
                                SIGNALOS_EXAMPLE_ARCANE_CODEX_DRIVE_ID,
                                SIGNALOS_EXAMPLE_ARCANE_CODEX_DRIVE_ID,
                                SIGNALOS_EXAMPLE_ARCANE_CODEX_DRIVE_ID,
                                "SignalOS Example Arcane Codex Data Drive"
                        ),
                        virtual(
                                "echoarcanacore",
                                ARCANA_CORE_AETHER_WAKE_SIGNAL_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.arcana_core.signal.aether_wake",
                                ARCANA_CORE_AETHER_WAKE_SIGNAL_ID,
                                ARCANA_CORE_AETHER_WAKE_SIGNAL_ID,
                                ARCANA_CORE_AETHER_WAKE_SIGNAL_ID,
                                ARCANA_CORE_AETHER_WAKE_SIGNAL_ID,
                                "Arcana Core Aether Wake Signal"
                        ),
                        virtual(
                                "echoarcanacore",
                                ARCANA_CORE_CODEX_STORY_FLAG_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "story.arcana_core.flag.arcane_codex",
                                ARCANA_CORE_CODEX_STORY_FLAG_ID,
                                ARCANA_CORE_CODEX_STORY_FLAG_ID,
                                ARCANA_CORE_CODEX_STORY_FLAG_ID,
                                ARCANA_CORE_CODEX_STORY_FLAG_ID,
                                "Arcana Core Codex Story Flag"
                        ),
                        virtual(
                                "echoarcanacore",
                                ARCANA_CORE_CODEX_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "story.arcana_core.mission.codex_sync",
                                ARCANA_CORE_CODEX_MISSION_ID,
                                ARCANA_CORE_CODEX_MISSION_ID,
                                ARCANA_CORE_CODEX_MISSION_ID,
                                ARCANA_CORE_CODEX_MISSION_ID,
                                "Arcana Core Codex Sync Mission"
                        ),
                        virtual(
                                "echorelictech",
                                RELICTECH_PHASE_ANCHOR_EFFECT_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.relictech.effect.phase_anchor",
                                RELICTECH_PHASE_ANCHOR_EFFECT_ID,
                                RELICTECH_PHASE_ANCHOR_EFFECT_ID,
                                RELICTECH_PHASE_ANCHOR_EFFECT_ID,
                                RELICTECH_PHASE_ANCHOR_EFFECT_ID,
                                "Phase Anchor Relic Effect"
                        ),
                        virtual(
                                "echoarcaneindex",
                                ARCANE_INDEX_CODEX_CHAPTER_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.STORY,
                                "story.arcane_index.chapter.arcane_codex",
                                ARCANE_INDEX_CODEX_CHAPTER_ID,
                                ARCANE_INDEX_CODEX_CHAPTER_ID,
                                ARCANE_INDEX_CODEX_CHAPTER_ID,
                                ARCANE_INDEX_CODEX_CHAPTER_ID,
                                "Arcane Index Codex Chapter"
                        ),
                        virtual(
                                "echoaetherworks",
                                AETHERWORKS_AETHER_SYNC_PRESENCE_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.aetherworks.presence.aether_sync",
                                AETHERWORKS_AETHER_SYNC_PRESENCE_ID,
                                AETHERWORKS_AETHER_SYNC_PRESENCE_ID,
                                AETHERWORKS_AETHER_SYNC_PRESENCE_ID,
                                AETHERWORKS_AETHER_SYNC_PRESENCE_ID,
                                "AetherWorks Aether Sync Presence"
                        ),
                        virtual(
                                "echometadatacore",
                                "echometadatacore:data/module_manifest",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "metadatacore.data.module_manifest",
                                "echometadatacore:module_manifest",
                                "echometadatacore:module_manifest",
                                "metadatacore:data/module_manifest",
                                "echometadatacore:data/module_manifest",
                                "MetadataCore Module Manifest"
                        ),
                        virtual(
                                "echometadatacore",
                                "echometadatacore:data/ai_metadata",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "metadatacore.data.ai_metadata",
                                "echometadatacore:ai_metadata",
                                "echometadatacore:ai_metadata",
                                "metadatacore:data/ai_metadata",
                                "echometadatacore:data/ai_metadata",
                                "MetadataCore AI Metadata"
                        ),
                        virtual(
                                "echometadatacore",
                                "echometadatacore:diagnostic/metadata_validation",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "metadatacore.diagnostics.metadata_validation",
                                "echometadatacore:metadata_validation",
                                "echometadatacore:metadata_validation",
                                "metadatacore:diagnostic/metadata_validation",
                                "echometadatacore:diagnostic/metadata_validation",
                                "MetadataCore Validation"
                        ),
                        virtual(
                                "echometadatacore",
                                "echometadatacore:pack/metadata_scan",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.PACKS,
                                "metadatacore.packs.metadata_scan",
                                "echometadatacore:metadata_scan",
                                "echometadatacore:metadata_scan",
                                "metadatacore:pack/metadata_scan",
                                "echometadatacore:pack/metadata_scan",
                                "MetadataCore Pack Metadata Scan"
                        ),
                        virtual(
                                "echomodpackcommandcenter",
                                "echomodpackcommandcenter:data/catalog",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "commandcenter.data.catalog",
                                "echomodpackcommandcenter:catalog",
                                "echomodpackcommandcenter:catalog",
                                "commandcenter:data/catalog",
                                "echomodpackcommandcenter:data/catalog",
                                "Command Center Catalog"
                        ),
                        virtual(
                                "echomodpackcommandcenter",
                                "echomodpackcommandcenter:diagnostic/readiness",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "commandcenter.diagnostics.readiness",
                                "echomodpackcommandcenter:readiness",
                                "echomodpackcommandcenter:readiness",
                                "commandcenter:diagnostic/readiness",
                                "echomodpackcommandcenter:diagnostic/readiness",
                                "Command Center Readiness"
                        ),
                        virtual(
                                "echomodpackcommandcenter",
                                "echomodpackcommandcenter:command/local_tooling",
                                EchoAdapterCoreContentKind.COMMAND,
                                EchoAdapterCoreDomain.COMMANDS,
                                "commandcenter.commands.local_tooling",
                                "echomodpackcommandcenter:local_tooling",
                                "echomodpackcommandcenter:local_tooling",
                                "commandcenter:command/local_tooling",
                                "echomodpackcommandcenter:command/local_tooling",
                                "Command Center Local Tooling"
                        ),
                        virtual(
                                "echomodpackcommandcenter",
                                "echomodpackcommandcenter:pack/launcher_metadata",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.PACKS,
                                "commandcenter.packs.launcher_metadata",
                                "echomodpackcommandcenter:launcher_metadata",
                                "echomodpackcommandcenter:launcher_metadata",
                                "commandcenter:pack/launcher_metadata",
                                "echomodpackcommandcenter:pack/launcher_metadata",
                                "Command Center Launcher Metadata"
                        ),
                        virtual(
                                "echomodpackcommandcenter",
                                "echomodpackcommandcenter:asset/report_bundle",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.ASSETS,
                                "commandcenter.assets.report_bundle",
                                "echomodpackcommandcenter:report_bundle",
                                "echomodpackcommandcenter:report_bundle",
                                "commandcenter:asset/report_bundle",
                                "echomodpackcommandcenter:asset/report_bundle",
                                "Command Center Report Bundle"
                        ),
                        virtual(
                                "echobridgecore",
                                "echobridgecore:command/safe_action",
                                EchoAdapterCoreContentKind.COMMAND,
                                EchoAdapterCoreDomain.COMMANDS,
                                "bridgecore.commands.safe_action",
                                "echobridgecore:safe_action",
                                "echobridgecore:safe_action",
                                "bridgecore:command/safe_action",
                                "echobridgecore:command/safe_action",
                                "BridgeCore Safe Action"
                        ),
                        virtual(
                                "echobridgecore",
                                "echobridgecore:network/heartbeat",
                                EchoAdapterCoreContentKind.NETWORK_HOOK,
                                EchoAdapterCoreDomain.NETWORKING,
                                "bridgecore.network.heartbeat",
                                "echobridgecore:heartbeat",
                                "echobridgecore:heartbeat",
                                "bridgecore:network/heartbeat",
                                "echobridgecore:network/heartbeat",
                                "BridgeCore Heartbeat"
                        ),
                        virtual(
                                "echocommunitybridge",
                                "echocommunitybridge:networking/server_status",
                                EchoAdapterCoreContentKind.NETWORK_HOOK,
                                EchoAdapterCoreDomain.NETWORKING,
                                "communitybridge.networking.server_status",
                                "echocommunitybridge:server_status",
                                "echocommunitybridge:server_status",
                                "communitybridge:networking/server_status",
                                "echocommunitybridge:networking/server_status",
                                "CommunityBridge Server Status"
                        ),
                        virtual(
                                "echocommunitybridge",
                                "echocommunitybridge:networking/launcher_chat",
                                EchoAdapterCoreContentKind.NETWORK_HOOK,
                                EchoAdapterCoreDomain.NETWORKING,
                                "communitybridge.networking.launcher_chat",
                                "echocommunitybridge:launcher_chat",
                                "echocommunitybridge:launcher_chat",
                                "communitybridge:networking/launcher_chat",
                                "echocommunitybridge:networking/launcher_chat",
                                "CommunityBridge Launcher Chat"
                        ),
                        virtual(
                                "echocommunitybridge",
                                "echocommunitybridge:diagnostics/discord_sanitization",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "communitybridge.diagnostics.discord_sanitization",
                                "echocommunitybridge:discord_sanitization",
                                "echocommunitybridge:discord_sanitization",
                                "communitybridge:diagnostics/discord_sanitization",
                                "echocommunitybridge:diagnostics/discord_sanitization",
                                "CommunityBridge Discord Sanitization"
                        ),
                        virtual(
                                "echocommunitybridge",
                                "echocommunitybridge:data/player_identity",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "communitybridge.data.player_identity",
                                "echocommunitybridge:player_identity",
                                "echocommunitybridge:player_identity",
                                "communitybridge:data/player_identity",
                                "echocommunitybridge:data/player_identity",
                                "CommunityBridge Player Identity"
                        ),
                        virtual(
                                "echoreportcore",
                                "echoreportcore:diagnostic/support_bundle",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "reportcore.diagnostics.support_bundle",
                                "echoreportcore:support_bundle",
                                "echoreportcore:support_bundle",
                                "reportcore:diagnostic/support_bundle",
                                "echoreportcore:diagnostic/support_bundle",
                                "ReportCore Support Bundle"
                        ),
                        virtual(
                                "echoreportcore",
                                "echoreportcore:data/release_readiness",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "reportcore.data.release_readiness",
                                "echoreportcore:release_readiness",
                                "echoreportcore:release_readiness",
                                "reportcore:data/release_readiness",
                                "echoreportcore:data/release_readiness",
                                "ReportCore Release Readiness"
                        ),
                        virtual(
                                "echomodulegraph",
                                "echomodulegraph:data/module_graph",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "modulegraph.data.module_graph",
                                "echomodulegraph:module_graph",
                                "echomodulegraph:module_graph",
                                "modulegraph:data/module_graph",
                                "echomodulegraph:data/module_graph",
                                "ModuleGraph Module Graph"
                        ),
                        virtual(
                                "echomodulegraph",
                                "echomodulegraph:diagnostic/graph_validation",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "modulegraph.diagnostics.graph_validation",
                                "echomodulegraph:graph_validation",
                                "echomodulegraph:graph_validation",
                                "modulegraph:diagnostic/graph_validation",
                                "echomodulegraph:diagnostic/graph_validation",
                                "ModuleGraph Validation"
                        ),
                        virtual(
                                "echocontentcore",
                                "echocontentcore:block/content_catalog",
                                EchoAdapterCoreContentKind.BLOCK,
                                EchoAdapterCoreDomain.BLOCKS,
                                "contentcore.blocks.content_catalog",
                                "echocontentcore:content_block_catalog",
                                "echocontentcore:content_block_catalog",
                                "contentcore:block/content_catalog",
                                "echocontentcore:block/content_catalog",
                                "ContentCore Block Catalog"
                        ),
                        virtual(
                                "echocontentcore",
                                "echocontentcore:item/content_catalog",
                                EchoAdapterCoreContentKind.ITEM,
                                EchoAdapterCoreDomain.ITEMS,
                                "contentcore.items.content_catalog",
                                "echocontentcore:content_item_catalog",
                                "echocontentcore:content_item_catalog",
                                "contentcore:item/content_catalog",
                                "echocontentcore:item/content_catalog",
                                "ContentCore Item Catalog"
                        ),
                        virtual(
                                "echocontentcore",
                                "echocontentcore:entity/content_catalog",
                                EchoAdapterCoreContentKind.ENTITY,
                                EchoAdapterCoreDomain.ENTITIES,
                                "contentcore.entities.content_catalog",
                                "echocontentcore:content_entity_catalog",
                                "echocontentcore:content_entity_catalog",
                                "contentcore:entity/content_catalog",
                                "echocontentcore:entity/content_catalog",
                                "ContentCore Entity Catalog"
                        ),
                        virtual(
                                "echocontentcore",
                                "echocontentcore:recipe/content_catalog",
                                EchoAdapterCoreContentKind.RECIPE,
                                EchoAdapterCoreDomain.RECIPES,
                                "contentcore.recipes.content_catalog",
                                "echocontentcore:content_recipe_catalog",
                                "echocontentcore:content_recipe_catalog",
                                "contentcore:recipe/content_catalog",
                                "echocontentcore:recipe/content_catalog",
                                "ContentCore Recipe Catalog"
                        ),
                        virtual(
                                "echocontentcore",
                                "echocontentcore:loot/content_catalog",
                                EchoAdapterCoreContentKind.LOOT_TABLE,
                                EchoAdapterCoreDomain.LOOT,
                                "contentcore.loot.content_catalog",
                                "echocontentcore:content_loot_catalog",
                                "echocontentcore:content_loot_catalog",
                                "contentcore:loot/content_catalog",
                                "echocontentcore:loot/content_catalog",
                                "ContentCore Loot Catalog"
                        ),
                        virtual(
                                "echocontentcore",
                                "echocontentcore:structure/content_catalog",
                                EchoAdapterCoreContentKind.STRUCTURE,
                                EchoAdapterCoreDomain.STRUCTURES,
                                "contentcore.structures.content_catalog",
                                "echocontentcore:content_structure_catalog",
                                "echocontentcore:content_structure_catalog",
                                "contentcore:structure/content_catalog",
                                "echocontentcore:structure/content_catalog",
                                "ContentCore Structure Catalog"
                        ),
                        virtual(
                                "echocontentcore",
                                "echocontentcore:data/content_registry",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "contentcore.data.content_registry",
                                "echocontentcore:content_registry",
                                "echocontentcore:content_registry",
                                "contentcore:data/content_registry",
                                "echocontentcore:data/content_registry",
                                "ContentCore Registry"
                        ),
                        virtual(
                                "echoassetcore",
                                "echoassetcore:assets/asset_registry",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.ASSETS,
                                "assetcore.assets.asset_registry",
                                "echoassetcore:asset_registry",
                                "echoassetcore:asset_registry",
                                "assetcore:assets/asset_registry",
                                "echoassetcore:assets/asset_registry",
                                "AssetCore Asset Registry"
                        ),
                        virtual(
                                "echoassetcore",
                                "echoassetcore:data/asset_validation",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "assetcore.data.asset_validation",
                                "echoassetcore:asset_validation",
                                "echoassetcore:asset_validation",
                                "assetcore:data/asset_validation",
                                "echoassetcore:data/asset_validation",
                                "AssetCore Asset Validation"
                        ),
                        virtual(
                                "echoassetcore",
                                "echoassetcore:assets/textureforge_prompts",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.ASSETS,
                                "assetcore.assets.textureforge_prompts",
                                "echoassetcore:textureforge_prompts",
                                "echoassetcore:textureforge_prompts",
                                "assetcore:assets/textureforge_prompts",
                                "echoassetcore:assets/textureforge_prompts",
                                "TextureForge Prompt Contracts"
                        ),
                        virtual(
                                "echoassetcore",
                                "echoassetcore:data/textureforge_reports",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "assetcore.data.textureforge_reports",
                                "echoassetcore:textureforge_reports",
                                "echoassetcore:textureforge_reports",
                                "assetcore:data/textureforge_reports",
                                "echoassetcore:data/textureforge_reports",
                                "TextureForge Report Contracts"
                        ),
                        virtual(
                                "echoblockworks",
                                "echoblockworks:block/block_catalog",
                                EchoAdapterCoreContentKind.BLOCK,
                                EchoAdapterCoreDomain.BLOCKS,
                                "blockworks.blocks.block_catalog",
                                "echoblockworks:block_catalog",
                                "echoblockworks:block_catalog",
                                "blockworks:block/block_catalog",
                                "echoblockworks:block/block_catalog",
                                "Blockworks Block Catalog"
                        ),
                        virtual(
                                "echoblockworks",
                                "echoblockworks:item/pattern_cutter",
                                EchoAdapterCoreContentKind.ITEM,
                                EchoAdapterCoreDomain.ITEMS,
                                "blockworks.items.pattern_cutter",
                                "echoblockworks:pattern_cutter",
                                "echoblockworks:pattern_cutter",
                                "blockworks:item/pattern_cutter",
                                "echoblockworks:item/pattern_cutter",
                                "Blockworks Pattern Cutter"
                        ),
                        virtual(
                                "echoblockworks",
                                "echoblockworks:recipe/palette_conversion",
                                EchoAdapterCoreContentKind.RECIPE,
                                EchoAdapterCoreDomain.RECIPES,
                                "blockworks.recipes.palette_conversion",
                                "echoblockworks:palette_conversion",
                                "echoblockworks:palette_conversion",
                                "blockworks:recipe/palette_conversion",
                                "echoblockworks:recipe/palette_conversion",
                                "Blockworks Palette Conversion"
                        ),
                        virtual(
                                "echoblockworks",
                                "echoblockworks:structure/showcase_sites",
                                EchoAdapterCoreContentKind.STRUCTURE,
                                EchoAdapterCoreDomain.STRUCTURES,
                                "blockworks.structures.showcase_sites",
                                "echoblockworks:showcase_sites",
                                "echoblockworks:showcase_sites",
                                "blockworks:structure/showcase_sites",
                                "echoblockworks:structure/showcase_sites",
                                "Blockworks Showcase Sites"
                        ),
                        virtual(
                                "echoblockworks",
                                "echoblockworks:worldgen/scatter_sites",
                                EchoAdapterCoreContentKind.WORLDGEN_DEFINITION,
                                EchoAdapterCoreDomain.WORLDGEN,
                                "blockworks.worldgen.scatter_sites",
                                "echoblockworks:scatter_sites",
                                "echoblockworks:scatter_sites",
                                "blockworks:worldgen/scatter_sites",
                                "echoblockworks:worldgen/scatter_sites",
                                "Blockworks Scatter Sites"
                        ),
                        virtual(
                                "echotextureforge",
                                "echotextureforge:assets/spec_registry",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.ASSETS,
                                "textureforge.assets.spec_registry",
                                "echotextureforge:spec_registry",
                                "echotextureforge:spec_registry",
                                "textureforge:assets/spec_registry",
                                "echotextureforge:assets/spec_registry",
                                "TextureForge Spec Registry"
                        ),
                        virtual(
                                "echotextureforge",
                                "echotextureforge:assets/prompt_export",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.ASSETS,
                                "textureforge.assets.prompt_export",
                                "echotextureforge:prompt_export",
                                "echotextureforge:prompt_export",
                                "textureforge:assets/prompt_export",
                                "echotextureforge:assets/prompt_export",
                                "TextureForge Prompt Export"
                        ),
                        virtual(
                                "echotextureforge",
                                "echotextureforge:data/review_state",
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.DATA,
                                "textureforge.data.review_state",
                                "echotextureforge:review_state",
                                "echotextureforge:review_state",
                                "textureforge:data/review_state",
                                "echotextureforge:data/review_state",
                                "TextureForge Review State"
                        ),
                        virtual(
                                "echotextureforge",
                                "echotextureforge:diagnostic/texture_audit",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "textureforge.diagnostics.texture_audit",
                                "echotextureforge:texture_audit",
                                "echotextureforge:texture_audit",
                                "textureforge:diagnostic/texture_audit",
                                "echotextureforge:diagnostic/texture_audit",
                                "TextureForge Texture Audit"
                        ),
                        virtual(
                                "echotextureforge",
                                "echotextureforge:ui/dashboard",
                                EchoAdapterCoreContentKind.UI_SCREEN,
                                EchoAdapterCoreDomain.UI_SCREENS,
                                "textureforge.ui.dashboard",
                                "echotextureforge:dashboard",
                                "echotextureforge:dashboard",
                                "textureforge:ui/dashboard",
                                "echotextureforge:ui/dashboard",
                                "TextureForge Dashboard"
                        ),
                        virtual(
                                "echoagentcore",
                                "echoagentcore:command/safe_command",
                                EchoAdapterCoreContentKind.COMMAND,
                                EchoAdapterCoreDomain.COMMANDS,
                                "agentcore.commands.safe_command",
                                "echoagentcore:safe_command",
                                "echoagentcore:safe_command",
                                "agentcore:command/safe_command",
                                "echoagentcore:command/safe_command",
                                "AgentCore Safe Command"
                        ),
                        virtual(
                                "echoagentcore",
                                "echoagentcore:data/task_queue",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "agentcore.data.task_queue",
                                "echoagentcore:task_queue",
                                "echoagentcore:task_queue",
                                "agentcore:data/task_queue",
                                "echoagentcore:data/task_queue",
                                "AgentCore Task Queue"
                        ),
                        virtual(
                                "echoagentcore",
                                "echoagentcore:data/prompt_bundle",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "agentcore.data.prompt_bundle",
                                "echoagentcore:prompt_bundle",
                                "echoagentcore:prompt_bundle",
                                "agentcore:data/prompt_bundle",
                                "echoagentcore:data/prompt_bundle",
                                "AgentCore Prompt Bundle"
                        ),
                        virtual(
                                "echoagentcore",
                                "echoagentcore:diagnostic/run_report",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "agentcore.diagnostics.run_report",
                                "echoagentcore:run_report",
                                "echoagentcore:run_report",
                                "agentcore:diagnostic/run_report",
                                "echoagentcore:diagnostic/run_report",
                                "AgentCore Run Report"
                        ),
                        virtual(
                                "echoagriculturereclamation",
                                "echoagriculturereclamation:block/greenhouse_machine_rules",
                                EchoAdapterCoreContentKind.BLOCK,
                                EchoAdapterCoreDomain.BLOCKS,
                                "agriculture.blocks.greenhouse_machine_rules",
                                "echoagriculturereclamation:greenhouse_machine_rules",
                                "echoagriculturereclamation:greenhouse_machine_rules",
                                "agriculture:block/greenhouse_machine_rules",
                                "echoagriculturereclamation:block/greenhouse_machine_rules",
                                "Agriculture Reclamation Greenhouse Machine Rules"
                        ),
                        virtual(
                                "echoagriculturereclamation",
                                "echoagriculturereclamation:item/seed_supply_process",
                                EchoAdapterCoreContentKind.ITEM,
                                EchoAdapterCoreDomain.ITEMS,
                                "agriculture.items.seed_supply_process",
                                "echoagriculturereclamation:seed_supply_process",
                                "echoagriculturereclamation:seed_supply_process",
                                "agriculture:item/seed_supply_process",
                                "echoagriculturereclamation:item/seed_supply_process",
                                "Agriculture Reclamation Seed Supply Process"
                        ),
                        virtual(
                                "echoagriculturereclamation",
                                "echoagriculturereclamation:ui/reclamation_process_cards",
                                EchoAdapterCoreContentKind.UI_SCREEN,
                                EchoAdapterCoreDomain.UI_SCREENS,
                                "agriculture.ui.reclamation_process_cards",
                                "echoagriculturereclamation:reclamation_process_cards",
                                "echoagriculturereclamation:reclamation_process_cards",
                                "agriculture:ui/reclamation_process_cards",
                                "echoagriculturereclamation:ui/reclamation_process_cards",
                                "Agriculture Reclamation Process Cards"
                        ),
                        virtual(
                                "echoagriculturereclamation",
                                "echoagriculturereclamation:worldgen/restoration_envelope",
                                EchoAdapterCoreContentKind.WORLDGEN_DEFINITION,
                                EchoAdapterCoreDomain.WORLDGEN,
                                "agriculture.worldgen.restoration_envelope",
                                "echoagriculturereclamation:restoration_envelope",
                                "echoagriculturereclamation:restoration_envelope",
                                "agriculture:worldgen/restoration_envelope",
                                "echoagriculturereclamation:worldgen/restoration_envelope",
                                "Agriculture Reclamation Restoration Envelope"
                        ),
                        virtual(
                                "echoarmory",
                                "echoarmory:item/gear_state_normalization",
                                EchoAdapterCoreContentKind.ITEM,
                                EchoAdapterCoreDomain.ITEMS,
                                "armory.items.gear_state_normalization",
                                "echoarmory:gear_state_normalization",
                                "echoarmory:gear_state_normalization",
                                "armory:item/gear_state_normalization",
                                "echoarmory:item/gear_state_normalization",
                                "Armory Gear State Normalization"
                        ),
                        virtual(
                                "echoarmory",
                                "echoarmory:recipe/station_operation_preview",
                                EchoAdapterCoreContentKind.RECIPE,
                                EchoAdapterCoreDomain.RECIPES,
                                "armory.recipes.station_operation_preview",
                                "echoarmory:station_operation_preview",
                                "echoarmory:station_operation_preview",
                                "armory:recipe/station_operation_preview",
                                "echoarmory:recipe/station_operation_preview",
                                "Armory Station Operation Preview"
                        ),
                        virtual(
                                "echoarmory",
                                "echoarmory:player/route_readiness_score",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.PLAYER,
                                "armory.player.route_readiness_score",
                                "echoarmory:route_readiness_score",
                                "echoarmory:route_readiness_score",
                                "armory:player/route_readiness_score",
                                "echoarmory:player/route_readiness_score",
                                "Armory Route Readiness Score"
                        ),
                        virtual(
                                "echobiomecore",
                                "echobiomecore:data/profile_contract_normalization",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "biomecore.data.profile_contract_normalization",
                                "echobiomecore:profile_contract_normalization",
                                "echobiomecore:profile_contract_normalization",
                                "biomecore:data/profile_contract_normalization",
                                "echobiomecore:data/profile_contract_normalization",
                                "BiomeCore Profile Contract Normalization"
                        ),
                        virtual(
                                "echobiomecore",
                                "echobiomecore:assets/ambient_asset_contract",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.ASSETS,
                                "biomecore.assets.ambient_asset_contract",
                                "echobiomecore:ambient_asset_contract",
                                "echobiomecore:ambient_asset_contract",
                                "biomecore:assets/ambient_asset_contract",
                                "echobiomecore:assets/ambient_asset_contract",
                                "BiomeCore Ambient Asset Contract"
                        ),
                        virtual(
                                "echobiomecore",
                                "echobiomecore:maps/holomap_layer_refs",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.MAPS,
                                "biomecore.maps.holomap_layer_refs",
                                "echobiomecore:holomap_layer_refs",
                                "echobiomecore:holomap_layer_refs",
                                "biomecore:maps/holomap_layer_refs",
                                "echobiomecore:maps/holomap_layer_refs",
                                "BiomeCore HoloMap Layer Refs"
                        ),
                        virtual(
                                "echobiomecore",
                                "echobiomecore:worldgen/hazard_overlay_envelope",
                                EchoAdapterCoreContentKind.WORLDGEN_DEFINITION,
                                EchoAdapterCoreDomain.WORLDGEN,
                                "biomecore.worldgen.hazard_overlay_envelope",
                                "echobiomecore:hazard_overlay_envelope",
                                "echobiomecore:hazard_overlay_envelope",
                                "biomecore:worldgen/hazard_overlay_envelope",
                                "echobiomecore:worldgen/hazard_overlay_envelope",
                                "BiomeCore Hazard Overlay Envelope"
                        ),
                        virtual(
                                "echobridgecore",
                                "echobridgecore:data/session_state_contract",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "bridgecore.data.session_state_contract",
                                "echobridgecore:session_state_contract",
                                "echobridgecore:session_state_contract",
                                "bridgecore:data/session_state_contract",
                                "echobridgecore:data/session_state_contract",
                                "BridgeCore Session State Contract"
                        ),
                        virtual(
                                "echobridgecore",
                                "echobridgecore:diagnostic/safe_action_gate",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "bridgecore.diagnostics.safe_action_gate",
                                "echobridgecore:safe_action_gate",
                                "echobridgecore:safe_action_gate",
                                "bridgecore:diagnostic/safe_action_gate",
                                "echobridgecore:diagnostic/safe_action_gate",
                                "BridgeCore Safe Action Gate"
                        ),
                        virtual(
                                "echobridgecore",
                                "echobridgecore:networking/local_transport_heartbeat",
                                EchoAdapterCoreContentKind.NETWORK_HOOK,
                                EchoAdapterCoreDomain.NETWORKING,
                                "bridgecore.networking.local_transport_heartbeat",
                                "echobridgecore:local_transport_heartbeat",
                                "echobridgecore:local_transport_heartbeat",
                                "bridgecore:networking/local_transport_heartbeat",
                                "echobridgecore:networking/local_transport_heartbeat",
                                "BridgeCore Local Transport Heartbeat"
                        ),
                        virtual(
                                "echocameracore",
                                "echocameracore:rendering/profile_contract_normalization",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.RENDERING,
                                "cameracore.rendering.profile_contract_normalization",
                                "echocameracore:profile_contract_normalization",
                                "echocameracore:profile_contract_normalization",
                                "cameracore:rendering/profile_contract_normalization",
                                "echocameracore:rendering/profile_contract_normalization",
                                "CameraCore Profile Contract Normalization"
                        ),
                        virtual(
                                "echocameracore",
                                "echocameracore:rendering/shake_safety_envelope",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.RENDERING,
                                "cameracore.rendering.shake_safety_envelope",
                                "echocameracore:shake_safety_envelope",
                                "echocameracore:shake_safety_envelope",
                                "cameracore:rendering/shake_safety_envelope",
                                "echocameracore:rendering/shake_safety_envelope",
                                "CameraCore Shake Safety Envelope"
                        ),
                        virtual(
                                "echocameracore",
                                "echocameracore:input/target_anchor_contract",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.INPUT,
                                "cameracore.input.target_anchor_contract",
                                "echocameracore:target_anchor_contract",
                                "echocameracore:target_anchor_contract",
                                "cameracore:input/target_anchor_contract",
                                "echocameracore:input/target_anchor_contract",
                                "CameraCore Target Anchor Contract"
                        ),
                        virtual(
                                "echocinematiccore",
                                "echocinematiccore:rendering/sequence_contract_normalization",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.RENDERING,
                                "cinematiccore.rendering.sequence_contract_normalization",
                                "echocinematiccore:sequence_contract_normalization",
                                "echocinematiccore:sequence_contract_normalization",
                                "cinematiccore:rendering/sequence_contract_normalization",
                                "echocinematiccore:rendering/sequence_contract_normalization",
                                "CinematicCore Sequence Contract Normalization"
                        ),
                        virtual(
                                "echocinematiccore",
                                "echocinematiccore:rendering/pacing_envelope",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.RENDERING,
                                "cinematiccore.rendering.pacing_envelope",
                                "echocinematiccore:pacing_envelope",
                                "echocinematiccore:pacing_envelope",
                                "cinematiccore:rendering/pacing_envelope",
                                "echocinematiccore:rendering/pacing_envelope",
                                "CinematicCore Pacing Envelope"
                        ),
                        virtual(
                                "echocinematiccore",
                                "echocinematiccore:ui/trigger_overlay_contract",
                                EchoAdapterCoreContentKind.UI_SCREEN,
                                EchoAdapterCoreDomain.UI_SCREENS,
                                "cinematiccore.ui.trigger_overlay_contract",
                                "echocinematiccore:trigger_overlay_contract",
                                "echocinematiccore:trigger_overlay_contract",
                                "cinematiccore:ui/trigger_overlay_contract",
                                "echocinematiccore:ui/trigger_overlay_contract",
                                "CinematicCore Trigger Overlay Contract"
                        ),
                        virtual(
                                "echocombatcore",
                                "echocombatcore:item/damage_weapon_trait_contract",
                                EchoAdapterCoreContentKind.ITEM,
                                EchoAdapterCoreDomain.ITEMS,
                                "combatcore.items.damage_weapon_trait_contract",
                                "echocombatcore:damage_weapon_trait_contract",
                                "echocombatcore:damage_weapon_trait_contract",
                                "combatcore:item/damage_weapon_trait_contract",
                                "echocombatcore:item/damage_weapon_trait_contract",
                                "CombatCore Damage Weapon Trait Contract"
                        ),
                        virtual(
                                "echocombatcore",
                                "echocombatcore:entity/enemy_scaling_boss_phase_contract",
                                EchoAdapterCoreContentKind.ENTITY,
                                EchoAdapterCoreDomain.ENTITIES,
                                "combatcore.entities.enemy_scaling_boss_phase_contract",
                                "echocombatcore:enemy_scaling_boss_phase_contract",
                                "echocombatcore:enemy_scaling_boss_phase_contract",
                                "combatcore:entity/enemy_scaling_boss_phase_contract",
                                "echocombatcore:entity/enemy_scaling_boss_phase_contract",
                                "CombatCore Enemy Scaling Boss Phase Contract"
                        ),
                        virtual(
                                "echocombatcore",
                                "echocombatcore:player/armor_shield_telemetry_contract",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.PLAYER,
                                "combatcore.player.armor_shield_telemetry_contract",
                                "echocombatcore:armor_shield_telemetry_contract",
                                "echocombatcore:armor_shield_telemetry_contract",
                                "combatcore:player/armor_shield_telemetry_contract",
                                "echocombatcore:player/armor_shield_telemetry_contract",
                                "CombatCore Armor Shield Telemetry Contract"
                        ),
                        virtual(
                                "echocreatorcore",
                                "echocreatorcore:command/permission_gate_contract",
                                EchoAdapterCoreContentKind.COMMAND,
                                EchoAdapterCoreDomain.COMMANDS,
                                "creatorcore.commands.permission_gate_contract",
                                "echocreatorcore:permission_gate_contract",
                                "echocreatorcore:permission_gate_contract",
                                "creatorcore:command/permission_gate_contract",
                                "echocreatorcore:command/permission_gate_contract",
                                "CreatorCore Permission Gate Contract"
                        ),
                        virtual(
                                "echocreatorcore",
                                "echocreatorcore:data/session_project_contract",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "creatorcore.data.session_project_contract",
                                "echocreatorcore:session_project_contract",
                                "echocreatorcore:session_project_contract",
                                "creatorcore:data/session_project_contract",
                                "echocreatorcore:data/session_project_contract",
                                "CreatorCore Session Project Contract"
                        ),
                        virtual(
                                "echocreatorcore",
                                "echocreatorcore:pack/project_authoring_contract",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.PACKS,
                                "creatorcore.packs.project_authoring_contract",
                                "echocreatorcore:project_authoring_contract",
                                "echocreatorcore:project_authoring_contract",
                                "creatorcore:pack/project_authoring_contract",
                                "echocreatorcore:pack/project_authoring_contract",
                                "CreatorCore Project Authoring Contract"
                        ),
                        virtual(
                                "echocreatorcore",
                                "echocreatorcore:ui/dashboard_form_contract",
                                EchoAdapterCoreContentKind.UI_SCREEN,
                                EchoAdapterCoreDomain.UI_SCREENS,
                                "creatorcore.ui.dashboard_form_contract",
                                "echocreatorcore:dashboard_form_contract",
                                "echocreatorcore:dashboard_form_contract",
                                "creatorcore:ui/dashboard_form_contract",
                                "echocreatorcore:ui/dashboard_form_contract",
                                "CreatorCore Dashboard Form Contract"
                        ),
                        virtual(
                                "echocreaturecore",
                                "echocreaturecore:entity/archetype_ai_contract",
                                EchoAdapterCoreContentKind.ENTITY,
                                EchoAdapterCoreDomain.ENTITIES,
                                "creaturecore.entities.archetype_ai_contract",
                                "echocreaturecore:archetype_ai_contract",
                                "echocreaturecore:archetype_ai_contract",
                                "creaturecore:entity/archetype_ai_contract",
                                "echocreaturecore:entity/archetype_ai_contract",
                                "CreatureCore Archetype AI Contract"
                        ),
                        virtual(
                                "echocreaturecore",
                                "echocreaturecore:worldgen/spawn_scan_contract",
                                EchoAdapterCoreContentKind.WORLDGEN_DEFINITION,
                                EchoAdapterCoreDomain.WORLDGEN,
                                "creaturecore.worldgen.spawn_scan_contract",
                                "echocreaturecore:spawn_scan_contract",
                                "echocreaturecore:spawn_scan_contract",
                                "creaturecore:worldgen/spawn_scan_contract",
                                "echocreaturecore:worldgen/spawn_scan_contract",
                                "CreatureCore Spawn Scan Contract"
                        ),
                        virtual(
                                "echocore",
                                "echocore:data/service_registry",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "core.data.service_registry",
                                "echocore:service_registry",
                                "echocore:service_registry",
                                "core:data/service_registry",
                                "echocore:data/service_registry",
                                "Core Service Registry"
                        ),
                        virtual(
                                "echocore",
                                "echocore:data/data_bus",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "core.data.data_bus",
                                "echocore:data_bus",
                                "echocore:data_bus",
                                "core:data/data_bus",
                                "echocore:data/data_bus",
                                "Core Data Bus"
                        ),
                        virtual(
                                "echocore",
                                "echocore:diagnostic/core_diagnostics",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "core.diagnostics.core_diagnostics",
                                "echocore:core_diagnostics",
                                "echocore:core_diagnostics",
                                "core:diagnostic/core_diagnostics",
                                "echocore:diagnostic/core_diagnostics",
                                "Core Diagnostics"
                        ),
                        virtual(
                                "echoruntimeguard",
                                "echoruntimeguard:diagnostic/runtime_health",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "runtimeguard.diagnostics.runtime_health",
                                "echoruntimeguard:runtime_health",
                                "echoruntimeguard:runtime_health",
                                "runtimeguard:diagnostic/runtime_health",
                                "echoruntimeguard:diagnostic/runtime_health",
                                "Runtime Health Diagnostic"
                        ),
                        virtual(
                                "echoruntimeguard",
                                "echoruntimeguard:data/runtime_metrics",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "runtimeguard.data.runtime_metrics",
                                "echoruntimeguard:runtime_metrics",
                                "echoruntimeguard:runtime_metrics",
                                "runtimeguard:data/runtime_metrics",
                                "echoruntimeguard:data/runtime_metrics",
                                "Runtime Metrics Snapshot"
                        ),
                        virtual(
                                "echoruntimeguard",
                                "echoruntimeguard:network/runtime_budget",
                                EchoAdapterCoreContentKind.NETWORK_HOOK,
                                EchoAdapterCoreDomain.NETWORKING,
                                "runtimeguard.network.runtime_budget",
                                "echoruntimeguard:runtime_budget",
                                "echoruntimeguard:runtime_budget",
                                "runtimeguard:network/runtime_budget",
                                "echoruntimeguard:network/runtime_budget",
                                "Runtime Network Budget"
                        ),
                        virtual(
                                "echoruntimeguard",
                                "echoruntimeguard:command/echo_perf",
                                EchoAdapterCoreContentKind.COMMAND,
                                EchoAdapterCoreDomain.COMMANDS,
                                "runtimeguard.commands.echo_perf",
                                "echoruntimeguard:echo_perf",
                                "echoruntimeguard:echo_perf",
                                "runtimeguard:command/echo_perf",
                                "echoruntimeguard:command/echo_perf",
                                "RuntimeGuard Echo Perf Command"
                        ),
                        virtual(
                                "echoplatformcore",
                                "echoplatformcore:data/module_identity",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "platformcore.data.module_identity",
                                "echoplatformcore:module_identity",
                                "echoplatformcore:module_identity",
                                "platformcore:data/module_identity",
                                "echoplatformcore:data/module_identity",
                                "Platform Module Identity"
                        ),
                        virtual(
                                "echoplatformcore",
                                "echoplatformcore:diagnostic/capability_report",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "platformcore.diagnostics.capability_report",
                                "echoplatformcore:capability_report",
                                "echoplatformcore:capability_report",
                                "platformcore:diagnostic/capability_report",
                                "echoplatformcore:diagnostic/capability_report",
                                "Platform Capability Report"
                        ),
                        virtual(
                                "echoplatformcore",
                                "echoplatformcore:data/trust_policy",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "platformcore.data.trust_policy",
                                "echoplatformcore:trust_policy",
                                "echoplatformcore:trust_policy",
                                "platformcore:data/trust_policy",
                                "echoplatformcore:data/trust_policy",
                                "Platform Trust Policy"
                        ),
                        virtual(
                                "echodatacore",
                                "echodatacore:system/terminal_probe",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "datacore.data.system.terminal_probe",
                                "echodatacore:terminal_probe",
                                "echodatacore:terminal_probe",
                                "datacore:data/system/terminal_probe",
                                "echodatacore:system/terminal_probe",
                                "Terminal Probe Data Key"
                        ),
                        virtual(
                                "echodatacore",
                                "echodatacore:system/player_schema_version",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.PLAYER,
                                "datacore.player.schema_version",
                                "echodatacore:player_schema_version",
                                "echodatacore:player_schema_version",
                                "datacore:player/schema_version",
                                "echodatacore:system/player_schema_version",
                                "Player Schema Version Data Key"
                        ),
                        virtual(
                                "echodatacore",
                                "echodatacore:system/world_schema_version",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.SAVES,
                                "datacore.saves.world_schema_version",
                                "echodatacore:world_schema_version",
                                "echodatacore:world_schema_version",
                                "datacore:saves/world_schema_version",
                                "echodatacore:system/world_schema_version",
                                "World Schema Version Data Key"
                        ),
                        virtual(
                                "echodatacore",
                                "echodatacore:worldcore/last_region",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.WORLDGEN,
                                "datacore.worldcore.last_region",
                                "echodatacore:last_region",
                                "echodatacore:last_region",
                                "datacore:worldcore/last_region",
                                "echodatacore:worldcore/last_region",
                                "Last WorldCore Region Data Key"
                        ),
                        virtual(
                                "echodatacore",
                                "echodatacore:worldcore/last_marker",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.MAPS,
                                "datacore.worldcore.last_marker",
                                "echodatacore:last_marker",
                                "echodatacore:last_marker",
                                "datacore:worldcore/last_marker",
                                "echodatacore:worldcore/last_marker",
                                "Last WorldCore Marker Data Key"
                        ),
                        virtual(
                                "echodatacore",
                                "echodatacore:worldcore/active_hazards",
                                EchoAdapterCoreContentKind.WORLD_HAZARD,
                                EchoAdapterCoreDomain.HAZARDS,
                                "datacore.worldcore.active_hazards",
                                "echodatacore:active_hazards",
                                "echodatacore:active_hazards",
                                "datacore:worldcore/active_hazards",
                                "echodatacore:worldcore/active_hazards",
                                "Active WorldCore Hazards Data Key"
                        ),
                        virtual(
                                "echodatacore",
                                "echodatacore:data_service",
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "datacore.service.persistence",
                                "echodatacore:data_service",
                                "echodatacore:data_service",
                                "datacore:service/persistence",
                                "echodatacore:data_service",
                                "DataCore Persistence Service"
                        ),
                        virtual(
                                "echodatacore",
                                "echodatacore:data_sync",
                                EchoAdapterCoreContentKind.NETWORK_HOOK,
                                EchoAdapterCoreDomain.NETWORKING,
                                "datacore.network.data_sync",
                                "echodatacore:data_sync",
                                "echodatacore:data_sync",
                                "datacore:network/data_sync",
                                "echodatacore:data_sync",
                                "DataCore Sync Payload"
                        ),
                        virtual(
                                "echoschemacore",
                                "echoschemacore:data/schema_registry",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "schemacore.data.schema_registry",
                                "echoschemacore:schema_registry",
                                "echoschemacore:schema_registry",
                                "schemacore:data/schema_registry",
                                "echoschemacore:data/schema_registry",
                                "Schema Registry"
                        ),
                        virtual(
                                "echoschemacore",
                                "echoschemacore:data/echo_mod_manifest_schema",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "schemacore.data.echo_mod_manifest_schema",
                                "echoschemacore:echo_mod_manifest_schema",
                                "echoschemacore:echo_mod_manifest_schema",
                                "schemacore:data/echo_mod_manifest_schema",
                                "echoschemacore:data/echo_mod_manifest_schema",
                                "ECHO Mod Manifest Schema"
                        ),
                        virtual(
                                "echoschemacore",
                                "echoschemacore:data/prompt_bundle_schema",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "schemacore.data.prompt_bundle_schema",
                                "echoschemacore:prompt_bundle_schema",
                                "echoschemacore:prompt_bundle_schema",
                                "schemacore:data/prompt_bundle_schema",
                                "echoschemacore:data/prompt_bundle_schema",
                                "Prompt Bundle Schema"
                        ),
                        virtual(
                                "echovalidationcore",
                                "echovalidationcore:data/pack_validation",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "validationcore.data.pack_validation",
                                "echovalidationcore:pack_validation",
                                "echovalidationcore:pack_validation",
                                "validationcore:data/pack_validation",
                                "echovalidationcore:data/pack_validation",
                                "Pack Validation Contract"
                        ),
                        virtual(
                                "echovalidationcore",
                                "echovalidationcore:diagnostic/diagnostic_report",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "validationcore.diagnostics.diagnostic_report",
                                "echovalidationcore:diagnostic_report",
                                "echovalidationcore:diagnostic_report",
                                "validationcore:diagnostic/diagnostic_report",
                                "echovalidationcore:diagnostic/diagnostic_report",
                                "Diagnostic Report Contract"
                        ),
                        virtual(
                                "echovalidationcore",
                                "echovalidationcore:diagnostic/repair_suggestion",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "validationcore.diagnostics.repair_suggestion",
                                "echovalidationcore:repair_suggestion",
                                "echovalidationcore:repair_suggestion",
                                "validationcore:diagnostic/repair_suggestion",
                                "echovalidationcore:diagnostic/repair_suggestion",
                                "Repair Suggestion Contract"
                        ),
                        virtual(
                                "echocreaturecore",
                                "echocreaturecore:data/creature_archetype",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "creaturecore.data.creature_archetype",
                                "echocreaturecore:creature_archetype",
                                "echocreaturecore:creature_archetype",
                                "creaturecore:data/creature_archetype",
                                "echocreaturecore:data/creature_archetype",
                                "Creature Archetype Contract"
                        ),
                        virtual(
                                "echocreaturecore",
                                "echocreaturecore:entity/ai_profile",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.ENTITIES,
                                "creaturecore.entities.ai_profile",
                                "echocreaturecore:ai_profile",
                                "echocreaturecore:ai_profile",
                                "creaturecore:entity/ai_profile",
                                "echocreaturecore:entity/ai_profile",
                                "Creature AI Profile Contract"
                        ),
                        virtual(
                                "echocreaturecore",
                                "echocreaturecore:data/scan_metadata",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "creaturecore.data.scan_metadata",
                                "echocreaturecore:scan_metadata",
                                "echocreaturecore:scan_metadata",
                                "creaturecore:data/scan_metadata",
                                "echocreaturecore:data/scan_metadata",
                                "Creature Scan Metadata Contract"
                        ),
                        virtual(
                                "echodifficultycore",
                                "echodifficultycore:data/difficulty_profile",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "difficultycore.data.difficulty_profile",
                                "echodifficultycore:difficulty_profile",
                                "echodifficultycore:difficulty_profile",
                                "difficultycore:data/difficulty_profile",
                                "echodifficultycore:data/difficulty_profile",
                                "Difficulty Profile Contract"
                        ),
                        virtual(
                                "echodifficultycore",
                                "echodifficultycore:hazard/adaptive_scaling",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.HAZARDS,
                                "difficultycore.hazards.adaptive_scaling",
                                "echodifficultycore:adaptive_scaling",
                                "echodifficultycore:adaptive_scaling",
                                "difficultycore:hazard/adaptive_scaling",
                                "echodifficultycore:hazard/adaptive_scaling",
                                "Adaptive Difficulty Scaling Contract"
                        ),
                        virtual(
                                "echodifficultycore",
                                "echodifficultycore:pack/variant_difficulty_policy",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.PACKS,
                                "difficultycore.packs.variant_difficulty_policy",
                                "echodifficultycore:variant_difficulty_policy",
                                "echodifficultycore:variant_difficulty_policy",
                                "difficultycore:pack/variant_difficulty_policy",
                                "echodifficultycore:pack/variant_difficulty_policy",
                                "Pack Variant Difficulty Policy Contract"
                        ),
                        virtual(
                                "echodifficultycore",
                                "echodifficultycore:diagnostic/difficulty_telemetry",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "difficultycore.diagnostics.difficulty_telemetry",
                                "echodifficultycore:difficulty_telemetry",
                                "echodifficultycore:difficulty_telemetry",
                                "difficultycore:diagnostic/difficulty_telemetry",
                                "echodifficultycore:diagnostic/difficulty_telemetry",
                                "Difficulty Telemetry Contract"
                        ),
                        virtual(
                                "echoencountercore",
                                "echoencountercore:mission/encounter_definition",
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "encountercore.missions.encounter_definition",
                                "echoencountercore:encounter_definition",
                                "echoencountercore:encounter_definition",
                                "encountercore:mission/encounter_definition",
                                "echoencountercore:mission/encounter_definition",
                                "Encounter Definition Contract"
                        ),
                        virtual(
                                "echoencountercore",
                                "echoencountercore:entity/boss_gate",
                                EchoAdapterCoreContentKind.ENTITY,
                                EchoAdapterCoreDomain.ENTITIES,
                                "encountercore.entities.boss_gate",
                                "echoencountercore:boss_gate",
                                "echoencountercore:boss_gate",
                                "encountercore:entity/boss_gate",
                                "echoencountercore:entity/boss_gate",
                                "Boss Gate Contract"
                        ),
                        virtual(
                                "echoencountercore",
                                "echoencountercore:story/faction_patrol",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "encountercore.story.faction_patrol",
                                "echoencountercore:faction_patrol",
                                "echoencountercore:faction_patrol",
                                "encountercore:story/faction_patrol",
                                "echoencountercore:story/faction_patrol",
                                "Faction Patrol Encounter Contract"
                        ),
                        virtual(
                                "echoeventcore",
                                "echoeventcore:weather/world_event",
                                EchoAdapterCoreContentKind.WORLD_HAZARD,
                                EchoAdapterCoreDomain.WEATHER,
                                "eventcore.weather.world_event",
                                "echoeventcore:world_event",
                                "echoeventcore:world_event",
                                "eventcore:weather/world_event",
                                "echoeventcore:weather/world_event",
                                "World Event Contract"
                        ),
                        virtual(
                                "echoeventcore",
                                "echoeventcore:data/event_scheduler",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "eventcore.data.event_scheduler",
                                "echoeventcore:event_scheduler",
                                "echoeventcore:event_scheduler",
                                "eventcore:data/event_scheduler",
                                "echoeventcore:data/event_scheduler",
                                "Event Scheduler Contract"
                        ),
                        virtual(
                                "echoeventcore",
                                "echoeventcore:diagnostic/event_validation",
                                EchoAdapterCoreContentKind.DIAGNOSTIC,
                                EchoAdapterCoreDomain.DIAGNOSTICS,
                                "eventcore.diagnostics.event_validation",
                                "echoeventcore:event_validation",
                                "echoeventcore:event_validation",
                                "eventcore:diagnostic/event_validation",
                                "echoeventcore:diagnostic/event_validation",
                                "Event Validation Contract"
                        ),
                        virtual(
                                "echofamiliarcore",
                                "echofamiliarcore:entity/familiar_companion",
                                EchoAdapterCoreContentKind.ENTITY,
                                EchoAdapterCoreDomain.ENTITIES,
                                "familiarcore.entities.familiar_companion",
                                "echofamiliarcore:familiar_companion",
                                "echofamiliarcore:familiar_companion",
                                "familiarcore:entity/familiar_companion",
                                "echofamiliarcore:entity/familiar_companion",
                                "Familiar Companion Contract"
                        ),
                        virtual(
                                "echofamiliarcore",
                                "echofamiliarcore:player/bond_progression",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.PLAYER,
                                "familiarcore.player.bond_progression",
                                "echofamiliarcore:bond_progression",
                                "echofamiliarcore:bond_progression",
                                "familiarcore:player/bond_progression",
                                "echofamiliarcore:player/bond_progression",
                                "Familiar Bond Progression Contract"
                        ),
                        virtual(
                                "echofamiliarcore",
                                "echofamiliarcore:command/familiar_command",
                                EchoAdapterCoreContentKind.COMMAND,
                                EchoAdapterCoreDomain.COMMANDS,
                                "familiarcore.commands.familiar_command",
                                "echofamiliarcore:familiar_command",
                                "echofamiliarcore:familiar_command",
                                "familiarcore:command/familiar_command",
                                "echofamiliarcore:command/familiar_command",
                                "Familiar Command Contract"
                        ),
                        virtual(
                                "echosocialcore",
                                "echosocialcore:data/dialogue_tree",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "socialcore.data.dialogue_tree",
                                "echosocialcore:dialogue_tree",
                                "echosocialcore:dialogue_tree",
                                "socialcore:data/dialogue_tree",
                                "echosocialcore:data/dialogue_tree",
                                "Social Dialogue Tree Contract"
                        ),
                        virtual(
                                "echoguidecore",
                                "echoguidecore:wiki/guide_page",
                                EchoAdapterCoreContentKind.UI_SCREEN,
                                EchoAdapterCoreDomain.WIKI,
                                "guidecore.wiki.guide_page",
                                "echoguidecore:guide_page",
                                "echoguidecore:guide_page",
                                "guidecore:wiki/guide_page",
                                "echoguidecore:wiki/guide_page",
                                "Guide Page Contract"
                        ),
                        virtual(
                                "echoguidecore",
                                "echoguidecore:data/search_index",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.DATA,
                                "guidecore.data.search_index",
                                "echoguidecore:search_index",
                                "echoguidecore:search_index",
                                "guidecore:data/search_index",
                                "echoguidecore:data/search_index",
                                "Guide Search Index Contract"
                        ),
                        virtual(
                                "echoguidecore",
                                "echoguidecore:player/unlock_visibility",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.PLAYER,
                                "guidecore.player.unlock_visibility",
                                "echoguidecore:unlock_visibility",
                                "echoguidecore:unlock_visibility",
                                "guidecore:player/unlock_visibility",
                                "echoguidecore:player/unlock_visibility",
                                "Guide Unlock Visibility Contract"
                        ),
                        virtual(
                                "echoinputcore",
                                "echoinputcore:input/context",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.INPUT,
                                "inputcore.input.context",
                                "echoinputcore:input_context",
                                "echoinputcore:input_context",
                                "inputcore:input/context",
                                "echoinputcore:input/context",
                                "Input Context Contract"
                        ),
                        virtual(
                                "echoinputcore",
                                "echoinputcore:input/keybind_registry",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.INPUT,
                                "inputcore.input.keybind_registry",
                                "echoinputcore:keybind_registry",
                                "echoinputcore:keybind_registry",
                                "inputcore:input/keybind_registry",
                                "echoinputcore:input/keybind_registry",
                                "Keybind Registry Contract"
                        ),
                        virtual(
                                "echoinputcore",
                                "echoinputcore:ui/radial_menu",
                                EchoAdapterCoreContentKind.UI_SCREEN,
                                EchoAdapterCoreDomain.UI_SCREENS,
                                "inputcore.ui.radial_menu",
                                "echoinputcore:radial_menu",
                                "echoinputcore:radial_menu",
                                "inputcore:ui/radial_menu",
                                "echoinputcore:ui/radial_menu",
                                "Radial Menu Contract"
                        ),
                        virtual(
                                "echoinputcore",
                                "echoinputcore:input/controller_ready",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.INPUT,
                                "inputcore.input.controller_ready",
                                "echoinputcore:controller_ready",
                                "echoinputcore:controller_ready",
                                "inputcore:input/controller_ready",
                                "echoinputcore:input/controller_ready",
                                "Controller Ready Contract"
                        ),
                        virtual(
                                "echolorecore",
                                "echolorecore:story/lore_entry",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "lorecore.story.lore_entry",
                                "echolorecore:lore_entry",
                                "echolorecore:lore_entry",
                                "lorecore:story/lore_entry",
                                "echolorecore:story/lore_entry",
                                "Lore Entry Contract"
                        ),
                        virtual(
                                "echolorecore",
                                "echolorecore:sound/audio_log",
                                EchoAdapterCoreContentKind.SOUND_EVENT,
                                EchoAdapterCoreDomain.SOUNDS,
                                "lorecore.sounds.audio_log",
                                "echolorecore:audio_log",
                                "echolorecore:audio_log",
                                "lorecore:sound/audio_log",
                                "echolorecore:sound/audio_log",
                                "Audio Log Contract"
                        ),
                        virtual(
                                "echolorecore",
                                "echolorecore:story/blackbox_entry",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "lorecore.story.blackbox_entry",
                                "echolorecore:blackbox_entry",
                                "echolorecore:blackbox_entry",
                                "lorecore:story/blackbox_entry",
                                "echolorecore:story/blackbox_entry",
                                "Blackbox Entry Contract"
                        ),
                        virtual(
                                "echolorecore",
                                "echolorecore:structure/environmental_story",
                                EchoAdapterCoreContentKind.STRUCTURE,
                                EchoAdapterCoreDomain.STRUCTURES,
                                "lorecore.structures.environmental_story",
                                "echolorecore:environmental_story",
                                "echolorecore:environmental_story",
                                "lorecore:structure/environmental_story",
                                "echolorecore:structure/environmental_story",
                                "Environmental Story Contract"
                        ),
                        virtual(
                                "echonotificationcore",
                                "echonotificationcore:ui/toast",
                                EchoAdapterCoreContentKind.UI_SCREEN,
                                EchoAdapterCoreDomain.UI_SCREENS,
                                "notificationcore.ui.toast",
                                "echonotificationcore:toast",
                                "echonotificationcore:toast",
                                "notificationcore:ui/toast",
                                "echonotificationcore:ui/toast",
                                "Toast Notification Contract"
                        ),
                        virtual(
                                "echonotificationcore",
                                "echonotificationcore:ui/system_alert",
                                EchoAdapterCoreContentKind.UI_SCREEN,
                                EchoAdapterCoreDomain.UI_SCREENS,
                                "notificationcore.ui.system_alert",
                                "echonotificationcore:system_alert",
                                "echonotificationcore:system_alert",
                                "notificationcore:ui/system_alert",
                                "echonotificationcore:ui/system_alert",
                                "System Alert Contract"
                        ),
                        virtual(
                                "echonotificationcore",
                                "echonotificationcore:mission/mission_update",
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "notificationcore.missions.mission_update",
                                "echonotificationcore:mission_update",
                                "echonotificationcore:mission_update",
                                "notificationcore:mission/mission_update",
                                "echonotificationcore:mission/mission_update",
                                "Mission Update Notification Contract"
                        ),
                        virtual(
                                "echonotificationcore",
                                "echonotificationcore:ui/tutorial_hint",
                                EchoAdapterCoreContentKind.UI_SCREEN,
                                EchoAdapterCoreDomain.UI_SCREENS,
                                "notificationcore.ui.tutorial_hint",
                                "echonotificationcore:tutorial_hint",
                                "echonotificationcore:tutorial_hint",
                                "notificationcore:ui/tutorial_hint",
                                "echonotificationcore:ui/tutorial_hint",
                                "Tutorial Hint Notification Contract"
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/clean_water_bottle",
                                "registry.items.water_ration",
                                WATER_RATION_ITEM_ID,
                                WATER_RATION_ITEM_ID,
                                WATER_RATION_ITEM_ID,
                                WATER_RATION_ITEM_ID,
                                "Clean Water Bottle",
                                0xFF45D6F0,
                                0xFFE4FBFF,
                                EchoVoxelMaterialPattern.WATER_RATION
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/dirty_water_bottle",
                                "registry.items.dirty_water_bottle",
                                DIRTY_WATER_ITEM_ID,
                                DIRTY_WATER_ITEM_ID,
                                DIRTY_WATER_ITEM_ID,
                                DIRTY_WATER_ITEM_ID,
                                "Dirty Water Bottle",
                                0xFF67735B,
                                0xFFB2C490,
                                EchoVoxelMaterialPattern.WATER_RATION
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/filtered_water_bottle",
                                "registry.items.filtered_water_bottle",
                                FILTERED_WATER_ITEM_ID,
                                FILTERED_WATER_ITEM_ID,
                                "ashfall:filtered_water_bottle",
                                FILTERED_WATER_ITEM_ID,
                                "Filtered Water Bottle",
                                0xFF5CB7D6,
                                0xFFDDF8FF,
                                EchoVoxelMaterialPattern.WATER_RATION
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/emergency_ration",
                                "registry.items.field_ration",
                                FIELD_RATION_ITEM_ID,
                                FIELD_RATION_ITEM_ID,
                                "ashfall:field_ration",
                                FIELD_RATION_ITEM_ID,
                                "Field Ration",
                                0xFFE2C16B,
                                0xFFFFF2B8,
                                EchoVoxelMaterialPattern.WATER_RATION
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/field_manual",
                                "registry.items.field_manual",
                                FIELD_MANUAL_ITEM_ID,
                                FIELD_MANUAL_ITEM_ID,
                                FIELD_MANUAL_ITEM_ID,
                                FIELD_MANUAL_ITEM_ID,
                                "Field Manual",
                                0xFF71D3B7,
                                0xFFE8FFF8,
                                EchoVoxelMaterialPattern.TERMINAL_GRID
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/portable_signal_scanner",
                                "registry.items.emergency_scanner",
                                EMERGENCY_SCANNER_ITEM_ID,
                                EMERGENCY_SCANNER_ITEM_ID,
                                EMERGENCY_SCANNER_ITEM_ID,
                                EMERGENCY_SCANNER_ITEM_ID,
                                "Emergency Scanner",
                                0xFF6DB6FF,
                                0xFFE0F1FF,
                                EchoVoxelMaterialPattern.TERMINAL_GRID
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/power_cell",
                                "registry.items.power_repair_kit",
                                POWER_REPAIR_KIT_ITEM_ID,
                                POWER_REPAIR_KIT_ITEM_ID,
                                "powergrid:power_repair_kit",
                                POWER_REPAIR_KIT_ITEM_ID,
                                "Power Repair Kit",
                                0xFFE8B44D,
                                0xFFFFF0A8,
                                EchoVoxelMaterialPattern.POWER_NODE
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/energy_cell",
                                "registry.items.energy_cell",
                                ENERGY_CELL_ITEM_ID,
                                ENERGY_CELL_ITEM_ID,
                                "ashfall:energy_cell",
                                ENERGY_CELL_ITEM_ID,
                                "Energy Cell",
                                0xFF4DB7E8,
                                0xFFA8F0FF,
                                EchoVoxelMaterialPattern.POWER_NODE
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/scrap_metal",
                                "registry.items.scrap_metal",
                                SCRAP_METAL_ITEM_ID,
                                SCRAP_METAL_ITEM_ID,
                                SCRAP_METAL_ITEM_ID,
                                SCRAP_METAL_ITEM_ID,
                                "Scrap Metal",
                                0xFF8B7A66,
                                0xFFC9B29B,
                                EchoVoxelMaterialPattern.RUST_PATCHES
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/scrap_wire",
                                "registry.items.scrap_wire",
                                SCRAP_WIRE_ITEM_ID,
                                SCRAP_WIRE_ITEM_ID,
                                "ashfall:scrap_wire",
                                SCRAP_WIRE_ITEM_ID,
                                "Scrap Wire",
                                0xFFB46B43,
                                0xFFFFC78A,
                                EchoVoxelMaterialPattern.RUST_PATCHES
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/scrap_circuit",
                                "registry.items.scrap_circuit",
                                SCRAP_CIRCUIT_ITEM_ID,
                                SCRAP_CIRCUIT_ITEM_ID,
                                "ashfall:scrap_circuit",
                                SCRAP_CIRCUIT_ITEM_ID,
                                "Scrap Circuit",
                                0xFF3E9D72,
                                0xFFB8F5D3,
                                EchoVoxelMaterialPattern.TERMINAL_GRID
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/machine_casing",
                                "registry.items.machine_casing",
                                MACHINE_CASING_ITEM_ID,
                                MACHINE_CASING_ITEM_ID,
                                "ashfall:machine_casing",
                                MACHINE_CASING_ITEM_ID,
                                "Machine Casing",
                                0xFF6B7884,
                                0xFFD1DAE0,
                                EchoVoxelMaterialPattern.POWER_NODE
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/scrap_knife",
                                "registry.items.scrap_knife",
                                SCRAP_KNIFE_ITEM_ID,
                                SCRAP_KNIFE_ITEM_ID,
                                "ashfall:scrap_knife",
                                SCRAP_KNIFE_ITEM_ID,
                                "Scrap Knife",
                                0xFFA9B2B9,
                                0xFFE8EEF2,
                                EchoVoxelMaterialPattern.RUST_PATCHES
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/gas_mask",
                                "registry.items.gas_mask",
                                GAS_MASK_ITEM_ID,
                                GAS_MASK_ITEM_ID,
                                "ashfall:gas_mask",
                                GAS_MASK_ITEM_ID,
                                "Gas Mask",
                                0xFF2E3A35,
                                0xFF9FB7AA,
                                EchoVoxelMaterialPattern.HAZARD_STRIPES
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/schematic_fragment",
                                "registry.items.schematic_fragment",
                                SCHEMATIC_FRAGMENT_ITEM_ID,
                                SCHEMATIC_FRAGMENT_ITEM_ID,
                                "ashfall:schematic_fragment",
                                SCHEMATIC_FRAGMENT_ITEM_ID,
                                "Schematic Fragment",
                                0xFF526D8A,
                                0xFFD7E9FF,
                                EchoVoxelMaterialPattern.TERMINAL_GRID
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/filter_cartridge_basic",
                                "registry.items.filter_cartridge_basic",
                                BASIC_FILTER_ITEM_ID,
                                BASIC_FILTER_ITEM_ID,
                                "ashfall:filter_cartridge_basic",
                                BASIC_FILTER_ITEM_ID,
                                "Basic Filter Cartridge",
                                0xFF5C6A64,
                                0xFFC7D9CE,
                                EchoVoxelMaterialPattern.HAZARD_STRIPES
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/filter_cartridge_advanced",
                                "registry.items.filter_cartridge_advanced",
                                ADVANCED_FILTER_ITEM_ID,
                                ADVANCED_FILTER_ITEM_ID,
                                "ashfall:filter_cartridge_advanced",
                                ADVANCED_FILTER_ITEM_ID,
                                "Advanced Filter Cartridge",
                                0xFF365A6B,
                                0xFFA7E7FF,
                                EchoVoxelMaterialPattern.TERMINAL_GRID
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/dense_alloy_chunk",
                                "registry.items.dense_alloy_chunk",
                                DENSE_ALLOY_ITEM_ID,
                                DENSE_ALLOY_ITEM_ID,
                                "ashfall:dense_alloy_chunk",
                                DENSE_ALLOY_ITEM_ID,
                                "Dense Alloy Chunk",
                                0xFF7E8790,
                                0xFFE8F1F7,
                                EchoVoxelMaterialPattern.POWER_NODE
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/alloy_blade",
                                "registry.items.alloy_blade",
                                ALLOY_BLADE_ITEM_ID,
                                ALLOY_BLADE_ITEM_ID,
                                "ashfall:alloy_blade",
                                ALLOY_BLADE_ITEM_ID,
                                "Alloy Blade",
                                0xFF9FAAB4,
                                0xFFFFFFFF,
                                EchoVoxelMaterialPattern.RUST_PATCHES
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/alloy_helmet",
                                "registry.items.alloy_helmet",
                                ALLOY_HELMET_ITEM_ID,
                                ALLOY_HELMET_ITEM_ID,
                                "ashfall:alloy_helmet",
                                ALLOY_HELMET_ITEM_ID,
                                "Alloy Helmet",
                                0xFF5E6C78,
                                0xFFDDEBFF,
                                EchoVoxelMaterialPattern.TERMINAL_GRID
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/alloy_chestplate",
                                "registry.items.alloy_chestplate",
                                ALLOY_CHESTPLATE_ITEM_ID,
                                ALLOY_CHESTPLATE_ITEM_ID,
                                "ashfall:alloy_chestplate",
                                ALLOY_CHESTPLATE_ITEM_ID,
                                "Alloy Chestplate",
                                0xFF566777,
                                0xFFD4E7FF,
                                EchoVoxelMaterialPattern.TERMINAL_GRID
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/relay_scanner_lens",
                                "registry.items.relay_scanner_lens",
                                RELAY_SCANNER_LENS_ITEM_ID,
                                RELAY_SCANNER_LENS_ITEM_ID,
                                "ashfall:relay_scanner_lens",
                                RELAY_SCANNER_LENS_ITEM_ID,
                                "Relay Scanner Lens",
                                0xFF48A2B8,
                                0xFFD1FAFF,
                                EchoVoxelMaterialPattern.TERMINAL_GRID
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/scout_drone_item",
                                "registry.items.scout_drone_item",
                                SCOUT_DRONE_ITEM_ID,
                                SCOUT_DRONE_ITEM_ID,
                                "ashfall:scout_drone_item",
                                SCOUT_DRONE_ITEM_ID,
                                "Scout Drone",
                                0xFF505B65,
                                0xFFDCE8F0,
                                EchoVoxelMaterialPattern.POWER_NODE
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/rad_away",
                                "registry.items.rad_away",
                                RAD_AWAY_ITEM_ID,
                                RAD_AWAY_ITEM_ID,
                                "ashfall:rad_away",
                                RAD_AWAY_ITEM_ID,
                                "Rad Away",
                                0xFF4F7D5D,
                                0xFFD8FFD9,
                                EchoVoxelMaterialPattern.HAZARD_STRIPES
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/stim_pack",
                                "registry.items.stim_pack",
                                STIM_PACK_ITEM_ID,
                                STIM_PACK_ITEM_ID,
                                "ashfall:stim_pack",
                                STIM_PACK_ITEM_ID,
                                "Stim Pack",
                                0xFF8C3848,
                                0xFFFFCDD5,
                                EchoVoxelMaterialPattern.TERMINAL_GRID
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/hand_warmer",
                                "registry.items.hand_warmer",
                                HAND_WARMER_ITEM_ID,
                                HAND_WARMER_ITEM_ID,
                                "ashfall:hand_warmer",
                                HAND_WARMER_ITEM_ID,
                                "Hand Warmer",
                                0xFF8A5B2C,
                                0xFFFFD38C,
                                EchoVoxelMaterialPattern.RUST_PATCHES
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/thermal_liner",
                                "registry.items.thermal_liner",
                                THERMAL_LINER_ITEM_ID,
                                THERMAL_LINER_ITEM_ID,
                                "ashfall:thermal_liner",
                                THERMAL_LINER_ITEM_ID,
                                "Thermal Liner",
                                0xFF5F6672,
                                0xFFFFF3C4,
                                EchoVoxelMaterialPattern.TERMINAL_GRID
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/return_beacon",
                                "registry.items.return_beacon",
                                RETURN_BEACON_ITEM_ID,
                                RETURN_BEACON_ITEM_ID,
                                "ashfall:return_beacon",
                                RETURN_BEACON_ITEM_ID,
                                "Return Beacon",
                                0xFF335D76,
                                0xFFBCEBFF,
                                EchoVoxelMaterialPattern.MARKER_GRID
                        ),
                        item(
                                "echoashfallprotocol",
                                "echoashfallprotocol:item/return_keystone",
                                "registry.items.return_keystone",
                                RETURN_KEYSTONE_ITEM_ID,
                                RETURN_KEYSTONE_ITEM_ID,
                                "ashfall:return_keystone",
                                RETURN_KEYSTONE_ITEM_ID,
                                "Return Keystone",
                                0xFF665E9D,
                                0xFFE3DCFF,
                                EchoVoxelMaterialPattern.POWER_NODE
                        ),
                        spawnEgg("ash_wraith"),
                        spawnEgg("city_ruin_stalker"),
                        spawnEgg("city_stalker"),
                        spawnEgg("corruption_bloom"),
                        spawnEgg("crash_survivor"),
                        spawnEgg("crash_zone_colossus"),
                        spawnEgg("cryogenic_overseer"),
                        spawnEgg("echo_companion_drone"),
                        spawnEgg("echo_drone"),
                        spawnEgg("feral_human"),
                        spawnEgg("glowing_ghoul"),
                        spawnEgg("gridbound_husk"),
                        spawnEgg("industrial_juggernaut"),
                        spawnEgg("irradiated_wolf"),
                        spawnEgg("mirror_command"),
                        spawnEgg("mutated_crawler"),
                        spawnEgg("nexus_nullifier"),
                        spawnEgg("nexus_scar_avatar"),
                        spawnEgg("plains_warlord"),
                        spawnEgg("rad_zombie"),
                        spawnEgg("radiation_behemoth"),
                        spawnEgg("relay_warden"),
                        spawnEgg("rust_walker"),
                        spawnEgg("scavenger_bandit"),
                        spawnEgg("scout_drone"),
                        spawnEgg("severance_engine"),
                        spawnEgg("signal_leech"),
                        spawnEgg("steam_wraith"),
                        spawnEgg("toxic_hive_matriarch"),
                        spawnEgg("toxic_slime"),
                        spawnEgg("warden_boss"),
                        spawnEgg("wasteland_sentinel"),
                        spawnEgg("wild_dog"),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/ash_campfire",
                                "registry.blocks.shelter_anchor",
                                SHELTER_ANCHOR_BLOCK_ID,
                                SHELTER_ANCHOR_BLOCK_ID,
                                "echoashfallprotocol:shelter_anchor",
                                SHELTER_ANCHOR_BLOCK_ID,
                                "Shelter Anchor",
                                0xFF5ED1A2,
                                0xFFE6FFE9,
                                EchoVoxelMaterialPattern.MARKER_GRID,
                                0.9D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/fallout_dust",
                                "registry.blocks.toxic_ash_block",
                                TOXIC_ASH_BLOCK_ID,
                                TOXIC_ASH_BLOCK_ID,
                                "echoashfallprotocol:toxic_ash_block",
                                TOXIC_ASH_BLOCK_ID,
                                "Toxic Ash",
                                0xFF786A55,
                                0xFFB59F78,
                                EchoVoxelMaterialPattern.ASH_GRAIN,
                                0.6D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/scorched_ash",
                                "registry.blocks.scorched_basalt",
                                SCORCHED_BASALT_BLOCK_ID,
                                SCORCHED_BASALT_BLOCK_ID,
                                "echoashfallprotocol:scorched_basalt",
                                SCORCHED_BASALT_BLOCK_ID,
                                "Scorched Basalt",
                                0xFF3A3D37,
                                0xFF686D63,
                                EchoVoxelMaterialPattern.BASALT_CRACKS,
                                1.8D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/rusted_metal_debris",
                                "registry.blocks.rusted_debris",
                                RUSTED_DEBRIS_BLOCK_ID,
                                RUSTED_DEBRIS_BLOCK_ID,
                                "echoashfallprotocol:rusted_debris",
                                RUSTED_DEBRIS_BLOCK_ID,
                                "Rusted Debris",
                                0xFF8E5F2B,
                                0xFFE0973D,
                                EchoVoxelMaterialPattern.RUST_PATCHES,
                                1.2D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/rain_collector",
                                "registry.blocks.rain_collector",
                                RAIN_COLLECTOR_BLOCK_ID,
                                RAIN_COLLECTOR_BLOCK_ID,
                                "echoashfallprotocol:rain_collector",
                                RAIN_COLLECTOR_BLOCK_ID,
                                "Rain Collector",
                                0xFF557D8A,
                                0xFFB8F4FF,
                                EchoVoxelMaterialPattern.WATER_RATION,
                                1.1D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/water_purifier",
                                "registry.blocks.water_purifier",
                                WATER_PURIFIER_BLOCK_ID,
                                WATER_PURIFIER_BLOCK_ID,
                                "echoashfallprotocol:water_purifier",
                                WATER_PURIFIER_BLOCK_ID,
                                "Water Purifier",
                                0xFF3E9EC8,
                                0xFFE2FBFF,
                                EchoVoxelMaterialPattern.TERMINAL_GRID,
                                1.6D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/hand_recycler",
                                "registry.blocks.hand_recycler",
                                HAND_RECYCLER_BLOCK_ID,
                                HAND_RECYCLER_BLOCK_ID,
                                "echoashfallprotocol:hand_recycler",
                                HAND_RECYCLER_BLOCK_ID,
                                "Hand Recycler",
                                0xFF536B5A,
                                0xFFA8D6B3,
                                EchoVoxelMaterialPattern.TERMINAL_GRID,
                                1.5D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/micro_generator",
                                "registry.blocks.micro_generator",
                                MICRO_GENERATOR_BLOCK_ID,
                                MICRO_GENERATOR_BLOCK_ID,
                                "echoashfallprotocol:micro_generator",
                                MICRO_GENERATOR_BLOCK_ID,
                                "Micro Generator",
                                0xFF8A6D2E,
                                0xFFFFD06A,
                                EchoVoxelMaterialPattern.POWER_NODE,
                                1.7D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/power_cable",
                                "registry.blocks.power_cable",
                                POWER_CABLE_BLOCK_ID,
                                POWER_CABLE_BLOCK_ID,
                                "echoashfallprotocol:power_cable",
                                POWER_CABLE_BLOCK_ID,
                                "Power Cable",
                                0xFF4D515A,
                                0xFFFFC65C,
                                EchoVoxelMaterialPattern.HAZARD_STRIPES,
                                0.7D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/energy_meter",
                                "registry.blocks.energy_meter",
                                ENERGY_METER_BLOCK_ID,
                                ENERGY_METER_BLOCK_ID,
                                "echoashfallprotocol:energy_meter",
                                ENERGY_METER_BLOCK_ID,
                                "Energy Meter",
                                0xFF3B8D8F,
                                0xFFBDFDFF,
                                EchoVoxelMaterialPattern.TERMINAL_GRID,
                                1.0D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/scrap_dynamo",
                                "registry.blocks.scrap_dynamo",
                                SCRAP_DYNAMO_BLOCK_ID,
                                SCRAP_DYNAMO_BLOCK_ID,
                                "echoashfallprotocol:scrap_dynamo",
                                SCRAP_DYNAMO_BLOCK_ID,
                                "Scrap Dynamo",
                                0xFF6F5A3C,
                                0xFFFFD46E,
                                EchoVoxelMaterialPattern.POWER_NODE,
                                1.8D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/battery_bank",
                                "registry.blocks.battery_bank",
                                BATTERY_BANK_BLOCK_ID,
                                BATTERY_BANK_BLOCK_ID,
                                "echoashfallprotocol:battery_bank",
                                BATTERY_BANK_BLOCK_ID,
                                "Battery Bank",
                                0xFF2F5C8F,
                                0xFFA8D8FF,
                                EchoVoxelMaterialPattern.TERMINAL_GRID,
                                1.6D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/thermal_burner",
                                "registry.blocks.thermal_burner",
                                THERMAL_BURNER_BLOCK_ID,
                                THERMAL_BURNER_BLOCK_ID,
                                "echoashfallprotocol:thermal_burner",
                                THERMAL_BURNER_BLOCK_ID,
                                "Thermal Burner",
                                0xFF8F3E2F,
                                0xFFFFA066,
                                EchoVoxelMaterialPattern.HAZARD_STRIPES,
                                1.7D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/scrap_press",
                                "registry.blocks.scrap_press",
                                SCRAP_PRESS_BLOCK_ID,
                                SCRAP_PRESS_BLOCK_ID,
                                "echoashfallprotocol:scrap_press",
                                SCRAP_PRESS_BLOCK_ID,
                                "Scrap Press",
                                0xFF5F6870,
                                0xFFC6D2DB,
                                EchoVoxelMaterialPattern.RUST_PATCHES,
                                1.9D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/item_pipe",
                                "registry.blocks.item_pipe",
                                ITEM_PIPE_BLOCK_ID,
                                ITEM_PIPE_BLOCK_ID,
                                "echoashfallprotocol:item_pipe",
                                ITEM_PIPE_BLOCK_ID,
                                "Item Pipe",
                                0xFF59606A,
                                0xFFFFC86A,
                                EchoVoxelMaterialPattern.HAZARD_STRIPES,
                                0.8D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/factory_controller",
                                "registry.blocks.factory_controller",
                                FACTORY_CONTROLLER_BLOCK_ID,
                                FACTORY_CONTROLLER_BLOCK_ID,
                                "echoashfallprotocol:factory_controller",
                                FACTORY_CONTROLLER_BLOCK_ID,
                                "Factory Controller",
                                0xFF2F4B5D,
                                0xFF91D8FF,
                                EchoVoxelMaterialPattern.TERMINAL_GRID,
                                1.8D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/research_lab",
                                "registry.blocks.research_lab",
                                RESEARCH_LAB_BLOCK_ID,
                                RESEARCH_LAB_BLOCK_ID,
                                "echoashfallprotocol:research_lab",
                                RESEARCH_LAB_BLOCK_ID,
                                "Research Lab",
                                0xFF514577,
                                0xFFD8C8FF,
                                EchoVoxelMaterialPattern.TERMINAL_GRID,
                                1.7D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/reinforced_power_cable",
                                "registry.blocks.reinforced_power_cable",
                                REINFORCED_POWER_CABLE_BLOCK_ID,
                                REINFORCED_POWER_CABLE_BLOCK_ID,
                                "echoashfallprotocol:reinforced_power_cable",
                                REINFORCED_POWER_CABLE_BLOCK_ID,
                                "Reinforced Power Cable",
                                0xFF39414D,
                                0xFFFFD477,
                                EchoVoxelMaterialPattern.POWER_NODE,
                                0.9D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/high_voltage_power_cable",
                                "registry.blocks.high_voltage_power_cable",
                                HIGH_VOLTAGE_POWER_CABLE_BLOCK_ID,
                                HIGH_VOLTAGE_POWER_CABLE_BLOCK_ID,
                                "echoashfallprotocol:high_voltage_power_cable",
                                HIGH_VOLTAGE_POWER_CABLE_BLOCK_ID,
                                "High Voltage Power Cable",
                                0xFF272D38,
                                0xFFFFF08A,
                                EchoVoxelMaterialPattern.POWER_NODE,
                                1.1D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/load_distributor",
                                "registry.blocks.load_distributor",
                                LOAD_DISTRIBUTOR_BLOCK_ID,
                                LOAD_DISTRIBUTOR_BLOCK_ID,
                                "echoashfallprotocol:load_distributor",
                                LOAD_DISTRIBUTOR_BLOCK_ID,
                                "Load Distributor",
                                0xFF344B5B,
                                0xFF8FD8FF,
                                EchoVoxelMaterialPattern.POWER_NODE,
                                1.8D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/crystalline_synthesizer",
                                "registry.blocks.crystalline_synthesizer",
                                CRYSTALLINE_SYNTHESIZER_BLOCK_ID,
                                CRYSTALLINE_SYNTHESIZER_BLOCK_ID,
                                "echoashfallprotocol:crystalline_synthesizer",
                                CRYSTALLINE_SYNTHESIZER_BLOCK_ID,
                                "Crystalline Synthesizer",
                                0xFF324B66,
                                0xFFA9E6FF,
                                EchoVoxelMaterialPattern.TERMINAL_GRID,
                                2.1D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/deep_core_miner",
                                "registry.blocks.deep_core_miner",
                                DEEP_CORE_MINER_BLOCK_ID,
                                DEEP_CORE_MINER_BLOCK_ID,
                                "echoashfallprotocol:deep_core_miner",
                                DEEP_CORE_MINER_BLOCK_ID,
                                "Deep Core Miner",
                                0xFF3E3630,
                                0xFFFFB56E,
                                EchoVoxelMaterialPattern.RUST_PATCHES,
                                2.2D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/autofeed_hopper",
                                "registry.blocks.autofeed_hopper",
                                AUTOFEED_HOPPER_BLOCK_ID,
                                AUTOFEED_HOPPER_BLOCK_ID,
                                "echoashfallprotocol:autofeed_hopper",
                                AUTOFEED_HOPPER_BLOCK_ID,
                                "Autofeed Hopper",
                                0xFF4D5B45,
                                0xFFCFEBA4,
                                EchoVoxelMaterialPattern.HAZARD_STRIPES,
                                1.4D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/contaminant_condenser",
                                "registry.blocks.contaminant_condenser",
                                CONTAMINANT_CONDENSER_BLOCK_ID,
                                CONTAMINANT_CONDENSER_BLOCK_ID,
                                "echoashfallprotocol:contaminant_condenser",
                                CONTAMINANT_CONDENSER_BLOCK_ID,
                                "Contaminant Condenser",
                                0xFF36534D,
                                0xFF98F0CA,
                                EchoVoxelMaterialPattern.HAZARD_STRIPES,
                                1.7D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/thermal_array",
                                "registry.blocks.thermal_array",
                                THERMAL_ARRAY_BLOCK_ID,
                                THERMAL_ARRAY_BLOCK_ID,
                                "echoashfallprotocol:thermal_array",
                                THERMAL_ARRAY_BLOCK_ID,
                                "Thermal Array",
                                0xFF7D4932,
                                0xFFFFB16A,
                                EchoVoxelMaterialPattern.POWER_NODE,
                                1.9D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/atmospheric_scrubber",
                                "registry.blocks.atmospheric_scrubber",
                                ATMOSPHERIC_SCRUBBER_BLOCK_ID,
                                ATMOSPHERIC_SCRUBBER_BLOCK_ID,
                                "echoashfallprotocol:atmospheric_scrubber",
                                ATMOSPHERIC_SCRUBBER_BLOCK_ID,
                                "Atmospheric Scrubber",
                                0xFF42665A,
                                0xFFA7F0D2,
                                EchoVoxelMaterialPattern.HAZARD_STRIPES,
                                1.8D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/radiation_cleanser",
                                "registry.blocks.radiation_cleanser",
                                RADIATION_CLEANSER_BLOCK_ID,
                                RADIATION_CLEANSER_BLOCK_ID,
                                "echoashfallprotocol:radiation_cleanser",
                                RADIATION_CLEANSER_BLOCK_ID,
                                "Radiation Cleanser",
                                0xFF495B39,
                                0xFFC8FF7A,
                                EchoVoxelMaterialPattern.TERMINAL_GRID,
                                1.8D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/field_med_bay",
                                "registry.blocks.field_med_bay",
                                FIELD_MED_BAY_BLOCK_ID,
                                FIELD_MED_BAY_BLOCK_ID,
                                "echoashfallprotocol:field_med_bay",
                                FIELD_MED_BAY_BLOCK_ID,
                                "Field Med Bay",
                                0xFF7A4E5F,
                                0xFFFFB6C9,
                                EchoVoxelMaterialPattern.TERMINAL_GRID,
                                1.6D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/filter_workbench",
                                "registry.blocks.filter_workbench",
                                FILTER_WORKBENCH_BLOCK_ID,
                                FILTER_WORKBENCH_BLOCK_ID,
                                "echoashfallprotocol:filter_workbench",
                                FILTER_WORKBENCH_BLOCK_ID,
                                "Filter Workbench",
                                0xFF415E5A,
                                0xFFA9F2D8,
                                EchoVoxelMaterialPattern.TERMINAL_GRID,
                                1.6D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/ore_grinder",
                                "registry.blocks.ore_grinder",
                                ORE_GRINDER_BLOCK_ID,
                                ORE_GRINDER_BLOCK_ID,
                                "echoashfallprotocol:ore_grinder",
                                ORE_GRINDER_BLOCK_ID,
                                "Ore Grinder",
                                0xFF555C62,
                                0xFFD6DEE6,
                                EchoVoxelMaterialPattern.RUST_PATCHES,
                                1.9D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/isotope_refiner",
                                "registry.blocks.isotope_refiner",
                                ISOTOPE_REFINER_BLOCK_ID,
                                ISOTOPE_REFINER_BLOCK_ID,
                                "echoashfallprotocol:isotope_refiner",
                                ISOTOPE_REFINER_BLOCK_ID,
                                "Isotope Refiner",
                                0xFF4D6645,
                                0xFFCBFF91,
                                EchoVoxelMaterialPattern.HAZARD_STRIPES,
                                1.9D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/relay_station",
                                "registry.blocks.relay_station",
                                RELAY_STATION_BLOCK_ID,
                                RELAY_STATION_BLOCK_ID,
                                "echoashfallprotocol:relay_station",
                                RELAY_STATION_BLOCK_ID,
                                "Relay Station",
                                0xFF2B5668,
                                0xFF93E7FF,
                                EchoVoxelMaterialPattern.TERMINAL_GRID,
                                1.8D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/signal_scanner",
                                "registry.blocks.signal_scanner",
                                SIGNAL_SCANNER_BLOCK_ID,
                                SIGNAL_SCANNER_BLOCK_ID,
                                "echoashfallprotocol:signal_scanner",
                                SIGNAL_SCANNER_BLOCK_ID,
                                "Signal Scanner",
                                0xFF2F6F4E,
                                0xFF9EFFC4,
                                EchoVoxelMaterialPattern.TERMINAL_GRID,
                                3.0D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/structure_cache",
                                "registry.blocks.structure_cache",
                                STRUCTURE_CACHE_BLOCK_ID,
                                STRUCTURE_CACHE_BLOCK_ID,
                                "echoashfallprotocol:structure_cache",
                                STRUCTURE_CACHE_BLOCK_ID,
                                "Structure Cache",
                                0xFF5A4432,
                                0xFFD9A96D,
                                EchoVoxelMaterialPattern.CACHE_PANEL,
                                2.0D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/echo_crate",
                                "registry.blocks.echo_crate",
                                ECHO_CRATE_BLOCK_ID,
                                ECHO_CRATE_BLOCK_ID,
                                "echoashfallprotocol:echo_crate",
                                ECHO_CRATE_BLOCK_ID,
                                "ECHO Crate",
                                0xFF4C565F,
                                0xFF9CC6E8,
                                EchoVoxelMaterialPattern.CACHE_PANEL,
                                2.0D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/nexus_core",
                                "registry.blocks.nexus_core",
                                NEXUS_CORE_BLOCK_ID,
                                NEXUS_CORE_BLOCK_ID,
                                "echoashfallprotocol:nexus_core",
                                NEXUS_CORE_BLOCK_ID,
                                "Nexus Core",
                                0xFF4D2775,
                                0xFFD58BFF,
                                EchoVoxelMaterialPattern.POWER_NODE,
                                2000.0D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/nexus_capacitor",
                                "registry.blocks.nexus_capacitor",
                                NEXUS_CAPACITOR_BLOCK_ID,
                                NEXUS_CAPACITOR_BLOCK_ID,
                                "echoashfallprotocol:nexus_capacitor",
                                NEXUS_CAPACITOR_BLOCK_ID,
                                "Nexus Capacitor",
                                0xFF4B2C68,
                                0xFFB98CFF,
                                EchoVoxelMaterialPattern.POWER_NODE,
                                6.0D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/toxic_waste_barrel",
                                "registry.blocks.ash_hazard_marker",
                                ASH_HAZARD_MARKER_BLOCK_ID,
                                ASH_HAZARD_MARKER_BLOCK_ID,
                                "echoashfallprotocol:ash_hazard_marker",
                                ASH_HAZARD_MARKER_BLOCK_ID,
                                "Ash Hazard",
                                0xFFB83E3E,
                                0xFFFFD15A,
                                EchoVoxelMaterialPattern.HAZARD_STRIPES,
                                0.3D
                        ),
                        block(
                                "echoterminal",
                                "echoterminal:block/echo_terminal",
                                "registry.blocks.field_terminal",
                                FIELD_TERMINAL_BLOCK_ID,
                                FIELD_TERMINAL_BLOCK_ID,
                                FIELD_TERMINAL_BLOCK_ID,
                                FIELD_TERMINAL_BLOCK_ID,
                                "Field Terminal",
                                0xFF45D4B4,
                                0xFFB9FFF0,
                                EchoVoxelMaterialPattern.TERMINAL_GRID,
                                1.0D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/echo_cache",
                                "registry.blocks.crash_cache",
                                CRASH_CACHE_BLOCK_ID,
                                CRASH_CACHE_BLOCK_ID,
                                "echoashfallprotocol:crash_cache",
                                CRASH_CACHE_BLOCK_ID,
                                "Crash Cache",
                                0xFF2FB9D9,
                                0xFF8BEEFF,
                                EchoVoxelMaterialPattern.CACHE_PANEL,
                                1.0D
                        ),
                        block(
                                "echoashfallprotocol",
                                "echoashfallprotocol:block/power_node",
                                "registry.blocks.damaged_power_node",
                                DAMAGED_POWER_NODE_BLOCK_ID,
                                DAMAGED_POWER_NODE_BLOCK_ID,
                                "echopowergrid:damaged_power_node",
                                DAMAGED_POWER_NODE_BLOCK_ID,
                                "Damaged Power Node",
                                0xFFE8B44D,
                                0xFFFFF0A8,
                                EchoVoxelMaterialPattern.POWER_NODE,
                                1.4D
                        ),
                        environmentBlock("acid_mud"),
                        environmentBlock("acidic_sludge"),
                        environmentBlock("ash_bush"),
                        environmentBlock("ash_layer"),
                        environmentBlock("ash_stone"),
                        environmentBlock("ashen_wasteland_dirt"),
                        environmentBlock("blue_ice_crystal"),
                        environmentBlock("burnt_fern"),
                        environmentBlock("burnt_grass"),
                        environmentBlock("burnt_tall_grass"),
                        environmentBlock("burnt_wasteland_soil"),
                        environmentBlock("cable_bundle"),
                        environmentBlock("charred_wood_log"),
                        environmentBlock("concrete_chunk"),
                        environmentBlock("concrete_rubble"),
                        environmentBlock("contaminated_soil"),
                        environmentBlock("corroded_pipe"),
                        environmentBlock("cracked_asphalt"),
                        environmentBlock("cracked_earth"),
                        environmentBlock("crash_slag"),
                        environmentBlock("cryogenic_fractured_stone"),
                        environmentBlock("dead_wood_log"),
                        environmentBlock("debris_block"),
                        environmentBlock("deep_ash"),
                        environmentBlock("drop_pod_glass"),
                        environmentBlock("drop_pod_hull"),
                        environmentBlock("dry_grass"),
                        environmentBlock("dry_tall_grass"),
                        environmentBlock("echo_crystal"),
                        environmentBlock("emergency_bunk"),
                        environmentBlock("energized_fissure"),
                        environmentBlock("frozen_conduit"),
                        environmentBlock("industrial_aggregate"),
                        environmentBlock("irradiated_cactus"),
                        environmentBlock("irradiated_crust"),
                        environmentBlock("irradiated_shale"),
                        environmentBlock("mutated_bush"),
                        environmentBlock("mutated_leaves_gray"),
                        environmentBlock("mutated_leaves_purple"),
                        environmentBlock("mutated_sapling"),
                        environmentBlock("mutated_wasteland_grass_block"),
                        environmentBlock("nexus_cracked_soil"),
                        environmentBlock("nuclear_fungus"),
                        environmentBlock("nuclear_grass"),
                        environmentBlock("nuclear_tall_grass"),
                        environmentBlock("oil_stained_concrete"),
                        environmentBlock("ooze_crystal"),
                        environmentBlock("permafrost"),
                        environmentBlock("radiation_block"),
                        environmentBlock("radioactive_sludge"),
                        environmentBlock("rebar_block"),
                        environmentBlock("riftstone"),
                        environmentBlock("rubble"),
                        environmentBlock("rusted_metal_sheet"),
                        environmentBlock("rusty_wheat"),
                        environmentBlock("scattered_bones"),
                        environmentBlock("scrap_ore"),
                        environmentBlock("shattered_glass"),
                        environmentBlock("supply_crate"),
                        environmentBlock("thorn_scrub"),
                        environmentBlock("toxic_grass"),
                        environmentBlock("toxic_moss"),
                        environmentBlock("toxic_puddle"),
                        environmentBlock("toxic_slagstone"),
                        environmentBlock("toxic_tall_grass"),
                        environmentBlock("toxic_wasteland_grass_block"),
                        environmentBlock("twisted_metal"),
                        environmentBlock("uranium_crystal"),
                        environmentBlock("wasteland_dirt"),
                        environmentBlock("wasteland_grass"),
                        environmentBlock("wasteland_grass_block"),
                        environmentBlock("wasteland_reed"),
                        environmentBlock("wasteland_stone"),
                        environmentBlock("wasteland_tall_grass"),
                        environmentBlock("wasteland_trace_rubble"),
                        environmentBlock("wild_berry_bush"),
                        environmentBlock("workshop_block"),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:world_region/crash_zone_wasteland",
                                EchoAdapterCoreContentKind.WORLD_REGION,
                                EchoAdapterCoreDomain.WORLDGEN,
                                "world.regions.crash_site",
                                "echoashfallprotocol:crash_zone_wasteland",
                                "echoashfallprotocol:crash_zone_wasteland",
                                "ashfall:crash_site",
                                "echoashfallprotocol:standalone_crash_site",
                                "Crash Site Region"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                TOXIC_ASH_HAZARD_ID,
                                EchoAdapterCoreContentKind.WORLD_HAZARD,
                                EchoAdapterCoreDomain.HAZARDS,
                                "world.hazards.toxic_ash",
                                "echoashfallprotocol:hazard/toxic_ash",
                                "echoashfallprotocol:hazard/toxic_ash",
                                "ashfall:toxic_ash",
                                "echoashfallprotocol:hazard/toxic_ash",
                                "Toxic Ash Hazard"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                HOT_ASH_HAZARD_ID,
                                EchoAdapterCoreContentKind.WORLD_HAZARD,
                                EchoAdapterCoreDomain.HAZARDS,
                                "world.hazards.hot_ash",
                                "echoashfallprotocol:hazard/hot_ash",
                                "echoashfallprotocol:hazard/hot_ash",
                                "ashfall:hot_ash",
                                "echoashfallprotocol:hazard/hot_ash",
                                "Hot Ash Hazard"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                UNSTABLE_GROUND_HAZARD_ID,
                                EchoAdapterCoreContentKind.WORLD_HAZARD,
                                EchoAdapterCoreDomain.HAZARDS,
                                "world.hazards.unstable_ground",
                                "echoashfallprotocol:hazard/unstable_ground",
                                "echoashfallprotocol:hazard/unstable_ground",
                                "ashfall:unstable_ground",
                                "echoashfallprotocol:hazard/unstable_ground",
                                "Unstable Ground Hazard"
                        ),
                        virtual(
                                "echopowergrid",
                                ELECTRICAL_DISCHARGE_HAZARD_ID,
                                EchoAdapterCoreContentKind.WORLD_HAZARD,
                                EchoAdapterCoreDomain.HAZARDS,
                                "world.hazards.electrical_discharge",
                                "echopowergrid:hazard/electrical_discharge",
                                "echopowergrid:hazard/electrical_discharge",
                                "powergrid:electrical_discharge",
                                "echopowergrid:hazard/electrical_discharge",
                                "Electrical Discharge Hazard"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                EXTRACTION_STORM_HAZARD_ID,
                                EchoAdapterCoreContentKind.WORLD_HAZARD,
                                EchoAdapterCoreDomain.HAZARDS,
                                "world.hazards.extraction_storm",
                                "echoashfallprotocol:hazard/extraction_storm",
                                "echoashfallprotocol:hazard/extraction_storm",
                                "ashfall:extraction_storm",
                                "echoashfallprotocol:hazard/extraction_storm",
                                "Extraction Storm Hazard"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:mission/secure_crash_outpost",
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.secure_crash_site",
                                "echoashfallprotocol:secure_crash_outpost",
                                "echoashfallprotocol:secure_crash_outpost",
                                "ashfall:secure_crash_site",
                                "ashfall:secure_crash_site_playable",
                                "Secure Crash Site"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:mission/drink_clean_water",
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.drink_clean_water",
                                "echoashfallprotocol:drink_clean_water",
                                "echoashfallprotocol:drink_clean_water",
                                "ashfall:drink_clean_water",
                                "echoashfallprotocol:mission/drink_clean_water",
                                "Drink Clean Water"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:mission/secure_emergency_water_loop",
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.secure_emergency_water_loop",
                                "echoashfallprotocol:secure_emergency_water_loop",
                                "echoashfallprotocol:secure_emergency_water_loop",
                                "ashfall:secure_emergency_water_loop",
                                "echoashfallprotocol:mission/secure_emergency_water_loop",
                                "Secure Emergency Water Loop"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:mission/forage_wasteland_food",
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.forage_wasteland_food",
                                "echoashfallprotocol:forage_wasteland_food",
                                "echoashfallprotocol:forage_wasteland_food",
                                "ashfall:forage_wasteland_food",
                                "echoashfallprotocol:mission/forage_wasteland_food",
                                "Forage Wasteland Food"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:mission/build_rain_collector",
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.build_rain_collector",
                                "echoashfallprotocol:build_rain_collector",
                                "echoashfallprotocol:build_rain_collector",
                                "ashfall:build_rain_collector",
                                "echoashfallprotocol:mission/build_rain_collector",
                                "Build Rain Collector"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:mission/stockpile_rations",
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.stockpile_rations",
                                "echoashfallprotocol:stockpile_rations",
                                "echoashfallprotocol:stockpile_rations",
                                "ashfall:stockpile_rations",
                                "echoashfallprotocol:mission/stockpile_rations",
                                "Stockpile Rations"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:mission/build_water_purifier",
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.build_water_purifier",
                                "echoashfallprotocol:build_water_purifier",
                                "echoashfallprotocol:build_water_purifier",
                                "ashfall:build_water_purifier",
                                "echoashfallprotocol:mission/build_water_purifier",
                                "Build Water Purifier"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:mission/stockpile_clean_water",
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.stockpile_clean_water",
                                "echoashfallprotocol:stockpile_clean_water",
                                "echoashfallprotocol:stockpile_clean_water",
                                "ashfall:stockpile_clean_water",
                                "echoashfallprotocol:mission/stockpile_clean_water",
                                "Stockpile Clean Water"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                CRAFT_SCRAP_KNIFE_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.craft_scrap_knife",
                                "echoashfallprotocol:craft_scrap_knife",
                                "echoashfallprotocol:craft_scrap_knife",
                                "ashfall:craft_scrap_knife",
                                CRAFT_SCRAP_KNIFE_MISSION_ID,
                                "Craft Scrap Knife"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                BUILD_HAND_RECYCLER_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.build_hand_recycler",
                                "echoashfallprotocol:build_hand_recycler",
                                "echoashfallprotocol:build_hand_recycler",
                                "ashfall:build_hand_recycler",
                                BUILD_HAND_RECYCLER_MISSION_ID,
                                "Build Hand Recycler"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                MAKE_MACHINE_CASING_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.make_machine_casing",
                                "echoashfallprotocol:make_machine_casing",
                                "echoashfallprotocol:make_machine_casing",
                                "ashfall:make_machine_casing",
                                MAKE_MACHINE_CASING_MISSION_ID,
                                "Make Machine Casing"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                ASSEMBLE_FIELD_KIT_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.assemble_wasteland_field_kit",
                                "echoashfallprotocol:assemble_wasteland_field_kit",
                                "echoashfallprotocol:assemble_wasteland_field_kit",
                                "ashfall:assemble_wasteland_field_kit",
                                ASSEMBLE_FIELD_KIT_MISSION_ID,
                                "Assemble Wasteland Field Kit"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                BUILD_MICRO_GENERATOR_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.build_micro_generator",
                                "echoashfallprotocol:build_micro_generator",
                                "echoashfallprotocol:build_micro_generator",
                                "ashfall:build_micro_generator",
                                BUILD_MICRO_GENERATOR_MISSION_ID,
                                "Build Micro Generator"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                ROUTE_POWER_CABLE_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.route_power_cable",
                                "echoashfallprotocol:route_power_cable",
                                "echoashfallprotocol:route_power_cable",
                                "ashfall:route_power_cable",
                                ROUTE_POWER_CABLE_MISSION_ID,
                                "Route Power Cable"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                INSTALL_ENERGY_METER_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.install_energy_meter",
                                "echoashfallprotocol:install_energy_meter",
                                "echoashfallprotocol:install_energy_meter",
                                "ashfall:install_energy_meter",
                                INSTALL_ENERGY_METER_MISSION_ID,
                                "Install Energy Meter"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                BUILD_SCRAP_DYNAMO_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.build_scrap_dynamo",
                                "echoashfallprotocol:build_scrap_dynamo",
                                "echoashfallprotocol:build_scrap_dynamo",
                                "ashfall:build_scrap_dynamo",
                                BUILD_SCRAP_DYNAMO_MISSION_ID,
                                "Build Scrap Dynamo"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                CHARGE_BASIC_BATTERY_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.charge_basic_battery",
                                "echoashfallprotocol:charge_basic_battery",
                                "echoashfallprotocol:charge_basic_battery",
                                "ashfall:charge_basic_battery",
                                CHARGE_BASIC_BATTERY_MISSION_ID,
                                "Charge Basic Battery"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                BUILD_BATTERY_BANK_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.build_battery_bank",
                                "echoashfallprotocol:build_battery_bank",
                                "echoashfallprotocol:build_battery_bank",
                                "ashfall:build_battery_bank",
                                BUILD_BATTERY_BANK_MISSION_ID,
                                "Build Battery Bank"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                BUILD_THERMAL_BURNER_MISSION_ID,
                                EchoAdapterCoreContentKind.MISSION,
                                EchoAdapterCoreDomain.MISSIONS,
                                "gameplay.missions.build_thermal_burner",
                                "echoashfallprotocol:build_thermal_burner",
                                "echoashfallprotocol:build_thermal_burner",
                                "ashfall:build_thermal_burner",
                                BUILD_THERMAL_BURNER_MISSION_ID,
                                "Build Thermal Burner"
                        ),
                        mission(EQUIP_GAS_MASK_MISSION_ID, "equip_gas_mask", "Equip Gas Mask"),
                        mission(FIND_SCHEMATIC_FRAGMENT_MISSION_ID,
                                "find_schematic_fragment",
                                "Find Schematic Fragment"),
                        mission(FIRST_SCHEMATIC_MISSION_ID, "first_schematic", "Decode First Schematic"),
                        mission(BUILD_SCRAP_PRESS_MISSION_ID, "build_scrap_press", "Build Scrap Press"),
                        mission(INSTALL_ITEM_PIPE_MISSION_ID, "install_item_pipe", "Install Item Pipe"),
                        mission(BUILD_FACTORY_CONTROLLER_MISSION_ID,
                                "build_factory_controller",
                                "Build Factory Controller"),
                        mission(BUILD_RESEARCH_LAB_MISSION_ID, "build_research_lab", "Build Research Lab"),
                        mission(UPGRADE_POWER_CABLE_MISSION_ID, "upgrade_power_cable", "Upgrade Power Cable"),
                        mission(SET_POWER_PRIORITY_MISSION_ID, "set_power_priority", "Set Power Priority"),
                        mission(OVERCLOCK_MACHINE_MISSION_ID, "overclock_machine", "Overclock Machine"),
                        mission(FIX_MASK_FILTER_MISSION_ID, "fix_mask_filter", "Fix Mask Filter"),
                        mission(CRAFT_ADVANCED_FILTER_MISSION_ID, "craft_advanced_filter", "Craft Advanced Filter"),
                        mission(BUILD_THERMAL_ARRAY_MISSION_ID, "build_thermal_array", "Build Thermal Array"),
                        mission(WARM_UP_AFTER_EXPOSURE_MISSION_ID, "warm_up_after_exposure", "Warm Up After Exposure"),
                        mission(BUILD_ATMOSPHERIC_SCRUBBER_MISSION_ID,
                                "build_atmospheric_scrubber",
                                "Build Atmospheric Scrubber"),
                        mission(BUILD_RADIATION_CLEANSER_MISSION_ID,
                                "build_radiation_cleanser",
                                "Build Radiation Cleanser"),
                        mission(BUILD_FIELD_MED_BAY_MISSION_ID, "build_field_med_bay", "Build Field Med Bay"),
                        mission(USE_FIELD_MED_BAY_MISSION_ID, "use_field_med_bay", "Use Field Med Bay"),
                        mission(BUILD_FILTER_WORKBENCH_MISSION_ID, "build_filter_workbench", "Build Filter Workbench"),
                        mission(BUILD_ORE_GRINDER_MISSION_ID, "build_ore_grinder", "Build Ore Grinder"),
                        mission(FIND_DENSE_ALLOY_MISSION_ID, "find_dense_alloy", "Find Dense Alloy"),
                        mission(BUILD_ISOTOPE_REFINER_MISSION_ID, "build_isotope_refiner", "Build Isotope Refiner"),
                        mission(FORGE_ALLOY_WEAPON_MISSION_ID, "forge_alloy_weapon", "Forge Alloy Weapon"),
                        mission(EQUIP_ALLOY_KIT_MISSION_ID, "equip_alloy_kit", "Equip Alloy Kit"),
                        mission(ACTIVATE_RELAY_STATION_MISSION_ID, "activate_relay_station", "Activate Relay Station"),
                        mission(BUILD_SCOUT_DRONE_MISSION_ID, "build_scout_drone", "Build Scout Drone"),
                        mission(USE_RAD_AWAY_MISSION_ID, "use_rad_away", "Use Rad Away"),
                        mission(USE_STIM_PACK_MISSION_ID, "use_stim_pack", "Use Stim Pack"),
                        mission(USE_HAND_WARMER_MISSION_ID, "use_hand_warmer", "Use Hand Warmer"),
                        mission(INSTALL_THERMAL_LINER_MISSION_ID,
                                "install_thermal_liner",
                                "Install Thermal Liner"),
                        mission(PLACE_RETURN_BEACON_MISSION_ID,
                                "place_return_beacon",
                                "Place Return Beacon"),
                        mission(BIND_RETURN_KEYSTONE_MISSION_ID,
                                "bind_return_keystone",
                                "Bind Return Keystone"),
                        virtual(
                                "echoashfallprotocol",
                                SCRAP_KNIFE_RECIPE_ID,
                                EchoAdapterCoreContentKind.RECIPE,
                                EchoAdapterCoreDomain.RECIPES,
                                "data.recipes.scrap_knife",
                                "echoashfallprotocol:scrap_knife",
                                "echoashfallprotocol:scrap_knife",
                                "ashfall:scrap_knife",
                                SCRAP_KNIFE_RECIPE_ID,
                                "Scrap Knife Recipe"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                TOOL_PROFILE_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "tool.profile.scrap_field_tools",
                                "echoashfallprotocol:tool/scrap_field_tools",
                                "echoashfallprotocol:tool/scrap_field_tools",
                                "ashfall:scrap_field_tools",
                                TOOL_PROFILE_ID,
                                "Scrap Field Tools Profile"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                FIELD_WORKSHOP_PROFILE_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "workshop.profile.field_recycler",
                                "echoashfallprotocol:workshop/field_recycler",
                                "echoashfallprotocol:workshop/field_recycler",
                                "ashfall:field_recycler",
                                FIELD_WORKSHOP_PROFILE_ID,
                                "Field Recycler Workshop Profile"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                FIELD_POWER_PROFILE_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "power.profile.field_microgrid",
                                "echoashfallprotocol:power/field_microgrid",
                                "echoashfallprotocol:power/field_microgrid",
                                "ashfall:field_microgrid",
                                FIELD_POWER_PROFILE_ID,
                                "Field Microgrid Power Profile"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                MACHINE_POWER_PROFILE_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "power.profile.machine_route",
                                "echoashfallprotocol:power/machine_route",
                                "echoashfallprotocol:power/machine_route",
                                "ashfall:machine_route",
                                MACHINE_POWER_PROFILE_ID,
                                "Machine Power Route Profile"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                MIDGAME_PROGRESSION_PROFILE_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "progression.profile.midgame_factory",
                                "echoashfallprotocol:progression/midgame_factory",
                                "echoashfallprotocol:progression/midgame_factory",
                                "ashfall:midgame_factory",
                                MIDGAME_PROGRESSION_PROFILE_ID,
                                "Midgame Factory Progression Profile"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                EXPEDITION_SAFETY_PROFILE_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "safety.profile.expedition_hazards",
                                "echoashfallprotocol:safety/expedition_hazards",
                                "echoashfallprotocol:safety/expedition_hazards",
                                "ashfall:expedition_hazards",
                                EXPEDITION_SAFETY_PROFILE_ID,
                                "Expedition Hazard Safety Profile"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                ADVANCED_EXPEDITION_PROFILE_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "progression.profile.advanced_expedition",
                                "echoashfallprotocol:progression/advanced_expedition",
                                "echoashfallprotocol:progression/advanced_expedition",
                                "ashfall:advanced_expedition",
                                ADVANCED_EXPEDITION_PROFILE_ID,
                                "Advanced Expedition Progression Profile"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                FIELD_RECOVERY_PROFILE_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "recovery.profile.field_recovery",
                                "echoashfallprotocol:recovery/field_recovery",
                                "echoashfallprotocol:recovery/field_recovery",
                                "ashfall:field_recovery",
                                FIELD_RECOVERY_PROFILE_ID,
                                "Field Recovery Consumables Profile"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:entity/scavenger_bandit",
                                EchoAdapterCoreContentKind.ENTITY,
                                EchoAdapterCoreDomain.ENTITIES,
                                "registry.entities.scavenger_bandit",
                                "echoashfallprotocol:scavenger_bandit",
                                "echoashfallprotocol:scavenger_bandit",
                                "ashfall:scavenger_bandit",
                                "echoashfallprotocol:entity/scavenger_bandit",
                                "Scavenger Bandit"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                SCOUT_DRONE_ENTITY_ID,
                                EchoAdapterCoreContentKind.ENTITY,
                                EchoAdapterCoreDomain.ENTITIES,
                                "registry.entities.scout_drone",
                                "echoashfallprotocol:scout_drone",
                                "echoashfallprotocol:scout_drone",
                                "ashfall:scout_drone",
                                SCOUT_DRONE_ENTITY_ID,
                                "Scout Drone"
                        ),
                        entity("ash_wraith"),
                        entity("city_ruin_stalker"),
                        entity("city_stalker"),
                        entity("corruption_bloom"),
                        entity("crash_survivor"),
                        entity("crash_zone_colossus"),
                        entity("cryogenic_overseer"),
                        entity("echo_companion_drone"),
                        entity("echo_drone"),
                        entity("faction_npc"),
                        entity("feral_human"),
                        entity("glowing_ghoul"),
                        entity("gridbound_husk"),
                        entity("industrial_juggernaut"),
                        entity("irradiated_wolf"),
                        entity("mirror_command"),
                        entity("mutated_crawler"),
                        entity("nexus_nullifier"),
                        entity("nexus_scar_avatar"),
                        entity("plains_warlord"),
                        entity("rad_zombie"),
                        entity("radiation_behemoth"),
                        entity("relay_warden"),
                        entity("rust_walker"),
                        entity("severance_engine"),
                        entity("signal_leech"),
                        entity("steam_wraith"),
                        entity("toxic_hive_matriarch"),
                        entity("toxic_slime"),
                        entity("warden_boss"),
                        entity("wasteland_sentinel"),
                        entity("wild_dog"),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:recipe/filter_cartridge_basic",
                                EchoAdapterCoreContentKind.RECIPE,
                                EchoAdapterCoreDomain.RECIPES,
                                "data.recipes.field_filter_patch",
                                "echoashfallprotocol:filter_cartridge_basic",
                                "echoashfallprotocol:filter_cartridge_basic",
                                "ashfall:field_filter_patch",
                                "echoashfallprotocol:recipe/filter_cartridge_basic",
                                "Field Filter Patch"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                RUSTED_DEBRIS_LOOT_ID,
                                EchoAdapterCoreContentKind.LOOT_TABLE,
                                EchoAdapterCoreDomain.LOOT,
                                "data.loot.rusted_debris",
                                "echoashfallprotocol:blocks/rusted_metal_debris",
                                "echoashfallprotocol:blocks/rusted_metal_debris",
                                "ashfall:rusted_debris_loot",
                                "echoashfallprotocol:loot/blocks/rusted_metal_debris",
                                "Rusted Debris Loot"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                CRASH_CACHE_LOOT_ID,
                                EchoAdapterCoreContentKind.LOOT_TABLE,
                                EchoAdapterCoreDomain.LOOT,
                                "data.loot.crash_cache",
                                "echoashfallprotocol:blocks/echo_cache",
                                "echoashfallprotocol:blocks/echo_cache",
                                "ashfall:crash_cache_loot",
                                "echoashfallprotocol:loot/blocks/echo_cache",
                                "Crash Cache Loot"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                DAMAGED_POWER_NODE_LOOT_ID,
                                EchoAdapterCoreContentKind.LOOT_TABLE,
                                EchoAdapterCoreDomain.LOOT,
                                "data.loot.damaged_power_node",
                                "echoashfallprotocol:blocks/power_node",
                                "echoashfallprotocol:blocks/power_node",
                                "powergrid:damaged_power_node_loot",
                                "echoashfallprotocol:loot/blocks/power_node",
                                "Damaged Power Node Loot"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:structure/crash_zone_wasteland",
                                EchoAdapterCoreContentKind.STRUCTURE,
                                EchoAdapterCoreDomain.STRUCTURES,
                                "world.structures.crash_site_outpost",
                                "echoashfallprotocol:crash_zone_wasteland",
                                "echoashfallprotocol:crash_zone_wasteland",
                                "ashfall:crash_site_outpost",
                                "echoashfallprotocol:structure/crash_zone_wasteland",
                                "Crash Site Outpost"
                        ),
                        virtual(
                                "echoterminal",
                                "echoterminal:ui/field_terminal",
                                EchoAdapterCoreContentKind.UI_SCREEN,
                                EchoAdapterCoreDomain.UI_SCREENS,
                                "ui.screens.field_terminal",
                                "echoterminal:field_terminal_screen",
                                "echoterminal:field_terminal_screen",
                                "ashfall:field_terminal_screen",
                                "echoterminal:ui/field_terminal",
                                "Field Terminal UI"
                        ),
                        menuScreen("crystalline_synthesizer", "Crystalline Synthesizer Menu"),
                        menuScreen("deep_core_miner", "Deep Core Miner Menu"),
                        menuScreen("filter_workbench", "Filter Workbench Menu"),
                        menuScreen("hand_recycler", "Hand Recycler Menu"),
                        menuScreen("isotope_refiner", "Isotope Refiner Menu"),
                        menuScreen("machine_status", "Machine Status Menu"),
                        menuScreen("micro_generator", "Micro Generator Menu"),
                        menuScreen("ore_grinder", "Ore Grinder Menu"),
                        menuScreen("radiation_cleanser", "Radiation Cleanser Menu"),
                        menuScreen("research_lab", "Research Lab Menu"),
                        menuScreen("scrap_press", "Scrap Press Menu"),
                        menuScreen("thermal_array", "Thermal Array Menu"),
                        menuScreen("thermal_burner", "Thermal Burner Menu"),
                        menuScreen("water_purifier", "Water Purifier Menu"),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:sound/ui.echo_message",
                                EchoAdapterCoreContentKind.SOUND_EVENT,
                                EchoAdapterCoreDomain.SOUNDS,
                                "registry.sounds.radio_static",
                                "echoashfallprotocol:ui.echo_message",
                                "echoashfallprotocol:ui.echo_message",
                                "ashfall:radio_static",
                                "echoashfallprotocol:sound/ui.echo_message",
                                "Radio Static"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                SURVIVAL_PROFILE_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "survival.profile.basic_needs",
                                "echoashfallprotocol:survival/basic_needs",
                                "echoashfallprotocol:survival/basic_needs",
                                "ashfall:basic_needs",
                                SURVIVAL_PROFILE_ID,
                                "Basic Needs Survival Profile"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                WATER_LOOP_PROFILE_ID,
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "survival.profile.emergency_water_loop",
                                "echoashfallprotocol:survival/emergency_water_loop",
                                "echoashfallprotocol:survival/emergency_water_loop",
                                "ashfall:emergency_water_loop",
                                WATER_LOOP_PROFILE_ID,
                                "Emergency Water Loop Profile"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:save/live_mission_state",
                                EchoAdapterCoreContentKind.SAVE_RECORD,
                                EchoAdapterCoreDomain.SAVES,
                                "save.records.live_mission_state",
                                "echoashfallprotocol:live_mission_state",
                                "echoashfallprotocol:live_mission_state",
                                "ashfall:live_mission_state",
                                "echoashfallprotocol:save/live_mission_state",
                                "Live Mission Save State"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                STORED_ENERGY_COMPONENT_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.SAVES,
                                "components.item.stored_energy",
                                "echoashfallprotocol:stored_energy",
                                "echoashfallprotocol:stored_energy",
                                "ashfall:component/stored_energy",
                                STORED_ENERGY_COMPONENT_ID,
                                "Stored Energy Data Component"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                ASHFALL_TOOLTIP_COMPONENT_ID,
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.SAVES,
                                "components.item.ashfall_tooltip",
                                "echoashfallprotocol:ashfall_tooltip",
                                "echoashfallprotocol:ashfall_tooltip",
                                "ashfall:component/ashfall_tooltip",
                                ASHFALL_TOOLTIP_COMPONENT_ID,
                                "Ashfall Tooltip Data Component"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                ALLIANCE_EFFECT_ID,
                                EchoAdapterCoreContentKind.STATUS_EFFECT,
                                EchoAdapterCoreDomain.PLAYER,
                                "status.effects.alliance",
                                "echoashfallprotocol:alliance",
                                "echoashfallprotocol:alliance",
                                "ashfall:status/alliance",
                                ALLIANCE_EFFECT_ID,
                                "Alliance Status Effect"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:network/live_state_sync",
                                EchoAdapterCoreContentKind.NETWORK_HOOK,
                                EchoAdapterCoreDomain.NETWORKING,
                                "network.hooks.live_state_sync",
                                "echoashfallprotocol:live_state_sync",
                                "echoashfallprotocol:live_state_sync",
                                "ashfall:live_state_sync",
                                "echoashfallprotocol:network/live_state_sync",
                                "Live State Sync"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:render/ashfall_live_scene",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.RENDERING,
                                "render.scene.ashfall_live_scene",
                                "echoashfallprotocol:ashfall_live_scene",
                                "echoashfallprotocol:ashfall_live_scene",
                                "ashfall:render/ashfall_live_scene",
                                "echoashfallprotocol:render/ashfall_live_scene",
                                "Ashfall Live Scene Render Contract"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:input/playable_controls",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.INPUT,
                                "input.controls.playable",
                                "echoashfallprotocol:playable_controls",
                                "echoashfallprotocol:playable_controls",
                                "ashfall:input/playable_controls",
                                "echoashfallprotocol:input/playable_controls",
                                "Ashfall Playable Input Controls"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:weather/extraction_storm",
                                EchoAdapterCoreContentKind.WORLDGEN_DEFINITION,
                                EchoAdapterCoreDomain.WEATHER,
                                "weather.events.extraction_storm",
                                "echoashfallprotocol:extraction_storm",
                                "echoashfallprotocol:extraction_storm",
                                "ashfall:weather/extraction_storm",
                                "echoashfallprotocol:weather/extraction_storm",
                                "Extraction Storm Weather Contract"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:machine/scrap_press_route",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.MACHINES,
                                "machines.scrap_press_route",
                                "echoashfallprotocol:scrap_press_route",
                                "echoashfallprotocol:scrap_press_route",
                                "ashfall:machine/scrap_press_route",
                                "echoashfallprotocol:machine/scrap_press_route",
                                "Scrap Press Machine Route"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:power/field_microgrid",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.POWER,
                                "power.networks.field_microgrid",
                                "echoashfallprotocol:field_microgrid",
                                "echoashfallprotocol:field_microgrid",
                                "ashfall:power/field_microgrid",
                                "echoashfallprotocol:power/field_microgrid",
                                "Field Microgrid Power Contract"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:economy/scrap_credit_trade",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.ECONOMY,
                                "economy.trade.scrap_credit",
                                "echoashfallprotocol:scrap_credit_trade",
                                "echoashfallprotocol:scrap_credit_trade",
                                "ashfall:economy/scrap_credit_trade",
                                "echoashfallprotocol:economy/scrap_credit_trade",
                                "Scrap Credit Trade Contract"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:story/secure_crash_site",
                                EchoAdapterCoreContentKind.DATA_COMPONENT,
                                EchoAdapterCoreDomain.STORY,
                                "story.chapters.secure_crash_site",
                                "echoashfallprotocol:secure_crash_site_story",
                                "echoashfallprotocol:secure_crash_site_story",
                                "ashfall:story/secure_crash_site",
                                "echoashfallprotocol:story/secure_crash_site",
                                "Secure Crash Site Story Contract"
                        ),
                        virtual(
                                "echoashfallprotocol",
                                "echoashfallprotocol:command/ashfall_status",
                                EchoAdapterCoreContentKind.COMMAND,
                                EchoAdapterCoreDomain.COMMANDS,
                                "commands.ashfall_status",
                                "echoashfallprotocol:ashfall_status",
                                "echoashfallprotocol:ashfall_status",
                                "ashfall:ashfall_status",
                                "echoashfallprotocol:command/ashfall_status",
                                "Ashfall Status Command"
                        )
                ))),
                EchoAdapterCoreRenderTarget.OPENGL,
                dataStore
        );
    }

    private String resolveContentId(String key, String defaultId) {
        if (dataStore == null) {
            return defaultId;
        }
        for (var registry : dataStore.registries()) {
            for (var definition : registry.entries()) {
                String id = definition.id();
                if (!id.equals(key) && !id.endsWith(":" + key)) {
                    continue;
                }
                Object itemId = definition.fields().get("itemId");
                if (itemId instanceof String s && !s.isBlank()) {
                    return s;
                }
                Object blockId = definition.fields().get("blockId");
                if (blockId instanceof String s && !s.isBlank()) {
                    return s;
                }
                Object entityId = definition.fields().get("entityId");
                if (entityId instanceof String s && !s.isBlank()) {
                    return s;
                }
            }
        }
        return defaultId;
    }

    private static List<EchoAdapterCoreRegistryEntry> withRemainingSystemContracts(
            List<EchoAdapterCoreRegistryEntry> entries
    ) {
        List<EchoAdapterCoreRegistryEntry> combined = new ArrayList<>(entries);
        Set<String> contentIds = new LinkedHashSet<>();
        Set<String> adapterKeys = new LinkedHashSet<>();
        for (EchoAdapterCoreRegistryEntry entry : combined) {
            contentIds.add(entry.contentId());
            adapterKeys.add(entry.binding().adapterKey());
        }
        for (EchoRemainingSystemsStandaloneAdapter.ContractSpec spec
                : EchoRemainingSystemsStandaloneAdapter.CONTRACTS) {
            if (!contentIds.add(spec.contentId()) || !adapterKeys.add(spec.adapterKey())) {
                continue;
            }
            combined.add(virtual(
                    spec.moduleId(),
                    spec.contentId(),
                    spec.contentKind(),
                    spec.domain(),
                    spec.adapterKey(),
                    spec.contentId(),
                    spec.contentId(),
                    spec.contentId(),
                    spec.contentId(),
                    spec.displayName()
            ));
        }
        return List.copyOf(combined);
    }

    public EchoAdapterCoreStandaloneRegistry registry() {
        return registry;
    }

    public EchoAdapterCoreStandaloneContentBridge withRuntimeEntry(EchoAdapterCoreRegistryEntry entry) {
        Objects.requireNonNull(entry, "entry");
        return withRuntimeEntries(List.of(entry));
    }

    public EchoAdapterCoreStandaloneContentBridge withRuntimeEntries(List<EchoAdapterCoreRegistryEntry> entries) {
        Objects.requireNonNull(entries, "entries");
        if (entries.isEmpty()) {
            return this;
        }
        return new EchoAdapterCoreStandaloneContentBridge(
                registry.withEntries(entries),
                renderTarget,
                dataStore
        );
    }

    public EchoAdapterCoreStandaloneContentBridge withRuntimeEntriesReplacingContentIds(
            List<EchoAdapterCoreRegistryEntry> entries
    ) {
        Objects.requireNonNull(entries, "entries");
        if (entries.isEmpty()) {
            return this;
        }
        return new EchoAdapterCoreStandaloneContentBridge(
                registry.withEntriesReplacingContentIds(entries),
                renderTarget,
                dataStore
        );
    }

    public List<EchoAdapterCoreContentBinding> bindings() {
        return bindings;
    }

    public int bindingCount() {
        return bindings.size();
    }

    public int readyBindingCount() {
        return (int) bindings.stream()
                .filter(EchoAdapterCoreContentBinding::standaloneReady)
                .count();
    }

    public String bindingCoverageSummary() {
        return readyBindingCount() + "/" + bindingCount() + " bindings ready";
    }

    public boolean supportsAllAdapterCoreRuntimes() {
        return bindings.stream().allMatch(EchoAdapterCoreContentBinding::supportsAllAdapterCoreRuntimes);
    }

    public List<EchoAdapterCoreDomain> missingRequiredBetaDomains() {
        return EchoAdapterCoreContractLock.requiredBetaDomains().stream()
                .filter(domain -> registry.entriesForDomain(domain).isEmpty())
                .toList();
    }

    public boolean coversEveryRequiredBetaDomain() {
        return missingRequiredBetaDomains().isEmpty();
    }

    public EchoAdapterCoreRenderTarget renderTarget() {
        return renderTarget;
    }

    public String runtimeSummary() {
        return "adaptercore:" + EchoAdapterCoreRuntimeKind.values().length + " runtimes";
    }

    public String rendererSummary() {
        return renderTarget.adapterId() + " target";
    }

    public String registrySummary() {
        return registry.summary();
    }

    public String materialAtlasSummary() {
        return registry.blocks().stream()
                .filter(entry -> entry.voxelBlock().isPresent())
                .map(entry -> entry.requireVoxelBlock().atlasKey())
                .distinct()
                .count() + " AdapterCore material atlas keys";
    }

    public EchoAdapterCoreScavengeTable scavengeTable() {
        return EchoAdapterCoreScavengeTable.ashfall(registry, this);
    }

    public EchoAdapterCoreHazardTable hazardTable() {
        return EchoAdapterCoreHazardTable.ashfall(registry);
    }

    public EchoAdapterCoreShelterProfile shelterProfile() {
        return EchoAdapterCoreShelterProfile.ashfall(registry, this);
    }

    public EchoAdapterCoreSurvivalProfile survivalProfile() {
        return EchoAdapterCoreSurvivalProfile.ashfall(registry, this);
    }

    public EchoAdapterCoreWaterLoopProfile waterLoopProfile() {
        return EchoAdapterCoreWaterLoopProfile.ashfall(registry);
    }

    public EchoAdapterCoreToolProfile toolProfile() {
        return EchoAdapterCoreToolProfile.ashfall(registry);
    }

    public EchoAdapterCoreFieldWorkshopProfile fieldWorkshopProfile() {
        return EchoAdapterCoreFieldWorkshopProfile.ashfall(registry);
    }

    public EchoAdapterCoreFieldPowerProfile fieldPowerProfile() {
        return EchoAdapterCoreFieldPowerProfile.ashfall(registry);
    }

    public EchoAdapterCoreMachinePowerProfile machinePowerProfile() {
        return EchoAdapterCoreMachinePowerProfile.ashfall(registry);
    }

    public EchoAdapterCoreMidgameProgressionProfile midgameProgressionProfile() {
        return EchoAdapterCoreMidgameProgressionProfile.ashfall(registry);
    }

    public EchoAdapterCoreExpeditionSafetyProfile expeditionSafetyProfile() {
        return EchoAdapterCoreExpeditionSafetyProfile.ashfall(registry);
    }

    public EchoAdapterCoreAdvancedExpeditionProfile advancedExpeditionProfile() {
        return EchoAdapterCoreAdvancedExpeditionProfile.ashfall(registry);
    }

    public EchoAdapterCoreFieldRecoveryProfile fieldRecoveryProfile() {
        return EchoAdapterCoreFieldRecoveryProfile.ashfall(registry);
    }

    public EchoVoxelBlock runtimeMarkerBlock() {
        return registry.requireLiveVoxelBlock(RUNTIME_MARKER_BLOCK_ID);
    }

    public EchoVoxelBlock waterRationItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("water_ration", WATER_RATION_ITEM_ID));
    }

    public EchoVoxelBlock fieldRationItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("field_ration", FIELD_RATION_ITEM_ID));
    }

    public EchoVoxelBlock fieldManualItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("field_manual", FIELD_MANUAL_ITEM_ID));
    }

    public EchoVoxelBlock dirtyWaterItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("dirty_water", DIRTY_WATER_ITEM_ID));
    }

    public EchoVoxelBlock filteredWaterItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("filtered_water", FILTERED_WATER_ITEM_ID));
    }

    public EchoVoxelBlock emergencyScannerItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("emergency_scanner", EMERGENCY_SCANNER_ITEM_ID));
    }

    public EchoVoxelBlock powerRepairKitItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("power_repair_kit", POWER_REPAIR_KIT_ITEM_ID));
    }

    public EchoVoxelBlock energyCellItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("energy_cell", ENERGY_CELL_ITEM_ID));
    }

    public EchoVoxelBlock scrapMetalItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("scrap_metal", SCRAP_METAL_ITEM_ID));
    }

    public EchoVoxelBlock scrapKnifeItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("scrap_knife", SCRAP_KNIFE_ITEM_ID));
    }

    public EchoVoxelBlock scrapWireItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("scrap_wire", SCRAP_WIRE_ITEM_ID));
    }

    public EchoVoxelBlock scrapCircuitItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("scrap_circuit", SCRAP_CIRCUIT_ITEM_ID));
    }

    public EchoVoxelBlock machineCasingItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("machine_casing", MACHINE_CASING_ITEM_ID));
    }

    public EchoVoxelBlock gasMaskItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("gas_mask", GAS_MASK_ITEM_ID));
    }

    public EchoVoxelBlock schematicFragmentItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("schematic_fragment", SCHEMATIC_FRAGMENT_ITEM_ID));
    }

    public EchoVoxelBlock basicFilterItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("basic_filter", BASIC_FILTER_ITEM_ID));
    }

    public EchoVoxelBlock advancedFilterItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("advanced_filter", ADVANCED_FILTER_ITEM_ID));
    }

    public EchoVoxelBlock denseAlloyItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("dense_alloy", DENSE_ALLOY_ITEM_ID));
    }

    public EchoVoxelBlock alloyBladeItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("alloy_blade", ALLOY_BLADE_ITEM_ID));
    }

    public EchoVoxelBlock alloyHelmetItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("alloy_helmet", ALLOY_HELMET_ITEM_ID));
    }

    public EchoVoxelBlock alloyChestplateItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("alloy_chestplate", ALLOY_CHESTPLATE_ITEM_ID));
    }

    public EchoVoxelBlock relayScannerLensItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("relay_scanner_lens", RELAY_SCANNER_LENS_ITEM_ID));
    }

    public EchoVoxelBlock scoutDroneItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("scout_drone", SCOUT_DRONE_ITEM_ID));
    }

    public EchoVoxelBlock radAwayItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("rad_away", RAD_AWAY_ITEM_ID));
    }

    public EchoVoxelBlock stimPackItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("stim_pack", STIM_PACK_ITEM_ID));
    }

    public EchoVoxelBlock handWarmerItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("hand_warmer", HAND_WARMER_ITEM_ID));
    }

    public EchoVoxelBlock thermalLinerItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("thermal_liner", THERMAL_LINER_ITEM_ID));
    }

    public EchoVoxelBlock returnBeaconItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("return_beacon", RETURN_BEACON_ITEM_ID));
    }

    public EchoVoxelBlock returnKeystoneItem() {
        return registry.requireLiveVoxelBlock(resolveContentId("return_keystone", RETURN_KEYSTONE_ITEM_ID));
    }

    public EchoVoxelBlock shelterAnchorBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("shelter_anchor", SHELTER_ANCHOR_BLOCK_ID));
    }

    public EchoVoxelBlock rainCollectorBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("rain_collector", RAIN_COLLECTOR_BLOCK_ID));
    }

    public EchoVoxelBlock waterPurifierBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("water_purifier", WATER_PURIFIER_BLOCK_ID));
    }

    public EchoVoxelBlock handRecyclerBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("hand_recycler", HAND_RECYCLER_BLOCK_ID));
    }

    public EchoVoxelBlock microGeneratorBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("micro_generator", MICRO_GENERATOR_BLOCK_ID));
    }

    public EchoVoxelBlock powerCableBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("power_cable", POWER_CABLE_BLOCK_ID));
    }

    public EchoVoxelBlock energyMeterBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("energy_meter", ENERGY_METER_BLOCK_ID));
    }

    public EchoVoxelBlock scrapDynamoBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("scrap_dynamo", SCRAP_DYNAMO_BLOCK_ID));
    }

    public EchoVoxelBlock batteryBankBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("battery_bank", BATTERY_BANK_BLOCK_ID));
    }

    public EchoVoxelBlock thermalBurnerBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("thermal_burner", THERMAL_BURNER_BLOCK_ID));
    }

    public EchoVoxelBlock scrapPressBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("scrap_press", SCRAP_PRESS_BLOCK_ID));
    }

    public EchoVoxelBlock itemPipeBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("item_pipe", ITEM_PIPE_BLOCK_ID));
    }

    public EchoVoxelBlock factoryControllerBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("factory_controller", FACTORY_CONTROLLER_BLOCK_ID));
    }

    public EchoVoxelBlock researchLabBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("research_lab", RESEARCH_LAB_BLOCK_ID));
    }

    public EchoVoxelBlock reinforcedPowerCableBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("reinforced_power_cable", REINFORCED_POWER_CABLE_BLOCK_ID));
    }

    public EchoVoxelBlock thermalArrayBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("thermal_array", THERMAL_ARRAY_BLOCK_ID));
    }

    public EchoVoxelBlock atmosphericScrubberBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("atmospheric_scrubber", ATMOSPHERIC_SCRUBBER_BLOCK_ID));
    }

    public EchoVoxelBlock radiationCleanserBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("radiation_cleanser", RADIATION_CLEANSER_BLOCK_ID));
    }

    public EchoVoxelBlock fieldMedBayBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("field_med_bay", FIELD_MED_BAY_BLOCK_ID));
    }

    public EchoVoxelBlock filterWorkbenchBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("filter_workbench", FILTER_WORKBENCH_BLOCK_ID));
    }

    public EchoVoxelBlock oreGrinderBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("ore_grinder", ORE_GRINDER_BLOCK_ID));
    }

    public EchoVoxelBlock isotopeRefinerBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("isotope_refiner", ISOTOPE_REFINER_BLOCK_ID));
    }

    public EchoVoxelBlock relayStationBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("relay_station", RELAY_STATION_BLOCK_ID));
    }

    public EchoVoxelBlock signalScannerBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("signal_scanner", SIGNAL_SCANNER_BLOCK_ID));
    }

    public EchoVoxelBlock structureCacheBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("structure_cache", STRUCTURE_CACHE_BLOCK_ID));
    }

    public EchoVoxelBlock echoCrateBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("echo_crate", ECHO_CRATE_BLOCK_ID));
    }

    public EchoVoxelBlock nexusCoreBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("nexus_core", NEXUS_CORE_BLOCK_ID));
    }

    public EchoVoxelBlock nexusCapacitorBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("nexus_capacitor", NEXUS_CAPACITOR_BLOCK_ID));
    }

    public EchoVoxelBlock rustedDebrisBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("rusted_debris", RUSTED_DEBRIS_BLOCK_ID));
    }

    public EchoVoxelBlock crashCacheBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("crash_cache", CRASH_CACHE_BLOCK_ID));
    }

    public EchoVoxelBlock damagedPowerNodeBlock() {
        return registry.requireLiveVoxelBlock(resolveContentId("damaged_power_node", DAMAGED_POWER_NODE_BLOCK_ID));
    }

    private static EchoAdapterCoreRegistryEntry block(
            String moduleId,
            String contentId,
            String adapterKey,
            String neoForgeId,
            String nativeLoaderId,
            String standaloneRuntimeId,
            String liveVoxelId,
            String displayName,
            int argb,
            int detailArgb,
            EchoVoxelMaterialPattern materialPattern,
            double hardness
    ) {
        return entry(
                moduleId,
                contentId,
                EchoAdapterCoreContentKind.BLOCK,
                EchoAdapterCoreDomain.BLOCKS,
                adapterKey,
                neoForgeId,
                nativeLoaderId,
                standaloneRuntimeId,
                liveVoxelId,
                displayName,
                new EchoVoxelBlock(
                        liveVoxelId,
                        displayName,
                        argb,
                        detailArgb,
                        contentId.replace(':', '/'),
                        materialPattern,
                        true,
                        true,
                        hardness
                )
        );
    }

    private static EchoAdapterCoreRegistryEntry environmentBlock(String blockId) {
        String namespacedBlockId = "echoashfallprotocol:" + blockId;
        return block(
                "echoashfallprotocol",
                "echoashfallprotocol:block/" + blockId,
                "registry.blocks." + blockId,
                namespacedBlockId,
                namespacedBlockId,
                namespacedBlockId,
                namespacedBlockId,
                titleCase(blockId),
                environmentColor(blockId),
                environmentDetailColor(blockId),
                environmentPattern(blockId),
                environmentHardness(blockId)
        );
    }

    private static EchoVoxelMaterialPattern environmentPattern(String blockId) {
        String normalized = blockId.toLowerCase();
        if (normalized.contains("ash")
                || normalized.contains("mud")
                || normalized.contains("sludge")
                || normalized.contains("soil")
                || normalized.contains("dirt")) {
            return EchoVoxelMaterialPattern.ASH_GRAIN;
        }
        if (normalized.contains("stone")
                || normalized.contains("basalt")
                || normalized.contains("concrete")
                || normalized.contains("rubble")
                || normalized.contains("rift")) {
            return EchoVoxelMaterialPattern.BASALT_CRACKS;
        }
        if (normalized.contains("rust")
                || normalized.contains("scrap")
                || normalized.contains("debris")
                || normalized.contains("metal")
                || normalized.contains("pipe")
                || normalized.contains("rebar")) {
            return EchoVoxelMaterialPattern.RUST_PATCHES;
        }
        if (normalized.contains("crystal")
                || normalized.contains("fissure")
                || normalized.contains("radiation")
                || normalized.contains("toxic")
                || normalized.contains("nuclear")) {
            return EchoVoxelMaterialPattern.HAZARD_STRIPES;
        }
        if (normalized.contains("bunk")
                || normalized.contains("workshop")
                || normalized.contains("supply")) {
            return EchoVoxelMaterialPattern.CACHE_PANEL;
        }
        return EchoVoxelMaterialPattern.FLAT;
    }

    private static double environmentHardness(String blockId) {
        String normalized = blockId.toLowerCase();
        if (normalized.contains("grass")
                || normalized.contains("bush")
                || normalized.contains("fern")
                || normalized.contains("reed")
                || normalized.contains("fungus")
                || normalized.contains("sapling")
                || normalized.contains("wheat")
                || normalized.contains("moss")) {
            return 0.2D;
        }
        if (normalized.contains("glass")) {
            return 0.3D;
        }
        if (normalized.contains("crystal")
                || normalized.contains("ore")
                || normalized.contains("stone")
                || normalized.contains("concrete")
                || normalized.contains("hull")
                || normalized.contains("block")) {
            return 1.5D;
        }
        return 0.8D;
    }

    private static int environmentColor(String blockId) {
        return 0xFF000000 | (blockId.hashCode() & 0x00FFFFFF);
    }

    private static int environmentDetailColor(String blockId) {
        return 0xFF000000 | ((blockId.hashCode() ^ 0x005A6C7D) & 0x00FFFFFF);
    }

    private static EchoAdapterCoreRegistryEntry item(
            String moduleId,
            String contentId,
            String adapterKey,
            String neoForgeId,
            String nativeLoaderId,
            String standaloneRuntimeId,
            String liveVoxelId,
            String displayName,
            int argb,
            int detailArgb,
            EchoVoxelMaterialPattern materialPattern
    ) {
        return entry(
                moduleId,
                contentId,
                EchoAdapterCoreContentKind.ITEM,
                EchoAdapterCoreDomain.ITEMS,
                adapterKey,
                neoForgeId,
                nativeLoaderId,
                standaloneRuntimeId,
                liveVoxelId,
                displayName,
                new EchoVoxelBlock(
                        liveVoxelId,
                        displayName,
                        argb,
                        detailArgb,
                        contentId.replace(':', '/'),
                        materialPattern,
                        false,
                        false,
                        0.0D
                )
        );
    }

    private static EchoAdapterCoreRegistryEntry spawnEgg(String entityId) {
        String itemId = entityId + "_spawn_egg";
        String namespacedItemId = "echoashfallprotocol:" + itemId;
        return item(
                "echoashfallprotocol",
                "echoashfallprotocol:item/" + itemId,
                "registry.items." + itemId,
                namespacedItemId,
                namespacedItemId,
                "ashfall:" + itemId,
                namespacedItemId,
                titleCase(entityId) + " Spawn Egg",
                0xFF4A5162,
                0xFFE1D8FF,
                EchoVoxelMaterialPattern.MARKER_GRID
        );
    }

    private static EchoAdapterCoreRegistryEntry virtual(
            String moduleId,
            String contentId,
            EchoAdapterCoreContentKind contentKind,
            EchoAdapterCoreDomain domain,
            String adapterKey,
            String neoForgeId,
            String nativeLoaderId,
            String standaloneRuntimeId,
            String liveVoxelId,
            String displayName
    ) {
        return entry(
                moduleId,
                contentId,
                contentKind,
                domain,
                adapterKey,
                neoForgeId,
                nativeLoaderId,
                standaloneRuntimeId,
                liveVoxelId,
                displayName,
                null
        );
    }

    private static EchoAdapterCoreRegistryEntry entity(String entityId) {
        String contentId = "echoashfallprotocol:entity/" + entityId;
        return virtual(
                "echoashfallprotocol",
                contentId,
                EchoAdapterCoreContentKind.ENTITY,
                EchoAdapterCoreDomain.ENTITIES,
                "registry.entities." + entityId,
                "echoashfallprotocol:" + entityId,
                "echoashfallprotocol:" + entityId,
                "ashfall:" + entityId,
                contentId,
                titleCase(entityId)
        );
    }

    private static String titleCase(String id) {
        String[] parts = id.split("_");
        StringBuilder title = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (title.length() > 0) {
                title.append(' ');
            }
            title.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                title.append(part.substring(1));
            }
        }
        return title.toString();
    }

    private static EchoAdapterCoreRegistryEntry menuScreen(String menuId, String displayName) {
        String contentId = "echoashfallprotocol:ui/" + menuId;
        return virtual(
                "echoashfallprotocol",
                contentId,
                EchoAdapterCoreContentKind.UI_SCREEN,
                EchoAdapterCoreDomain.UI_SCREENS,
                "ui.screens." + menuId,
                "echoashfallprotocol:" + menuId,
                "echoashfallprotocol:" + menuId,
                "ashfall:ui/" + menuId,
                contentId,
                displayName
        );
    }

    private static EchoAdapterCoreRegistryEntry mission(String contentId, String missionId, String displayName) {
        return virtual(
                "echoashfallprotocol",
                contentId,
                EchoAdapterCoreContentKind.MISSION,
                EchoAdapterCoreDomain.MISSIONS,
                "gameplay.missions." + missionId,
                "echoashfallprotocol:" + missionId,
                "echoashfallprotocol:" + missionId,
                "ashfall:" + missionId,
                contentId,
                displayName
        );
    }

    private static EchoAdapterCoreRegistryEntry entry(
            String moduleId,
            String contentId,
            EchoAdapterCoreContentKind contentKind,
            EchoAdapterCoreDomain domain,
            String adapterKey,
            String neoForgeId,
            String nativeLoaderId,
            String standaloneRuntimeId,
            String liveVoxelId,
            String displayName,
            EchoVoxelBlock liveVoxelBlock
    ) {
        return new EchoAdapterCoreRegistryEntry(
                binding(
                        moduleId,
                        contentId,
                        contentKind,
                        adapterKey,
                        neoForgeId,
                        nativeLoaderId,
                        standaloneRuntimeId,
                        liveVoxelId
                ),
                domain,
                displayName,
                liveVoxelBlock
        );
    }

    private static EchoAdapterCoreContentBinding binding(
            String moduleId,
            String contentId,
            EchoAdapterCoreContentKind contentKind,
            String adapterKey,
            String neoForgeId,
            String nativeLoaderId,
            String standaloneRuntimeId,
            String liveVoxelId
    ) {
        return new EchoAdapterCoreContentBinding(
                moduleId,
                contentId,
                contentKind,
                adapterKey,
                neoForgeId,
                nativeLoaderId,
                standaloneRuntimeId,
                liveVoxelId,
                true
        );
    }
}
