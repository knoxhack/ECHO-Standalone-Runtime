package dev.echo.standalone.runtime.compat;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class EchoAshfallGameplayContracts {
    public static final String ASHFALL_SAVE_SCHEMA_ID = "echoashfallprotocol:adaptercore/save/live_mission_state";
    public static final String LIVE_MISSION_STATE_CONTRACT_ID = "echoashfallprotocol:save/live_mission_state";
    public static final int CURRENT_SAVE_VERSION = 1;

    private static final List<String> REQUIRED_SAVE_FIELDS = List.of(
            "contractId",
            "contractSchema",
            "contractVersion",
            "shelterBuilt",
            "scannerUsed",
            "terminalOnline",
            "waterUsed",
            "foodUsed",
            "crossedAsh",
            "hazardCleared",
            "scavengedSupplies",
            "cacheRecovered",
            "powerNodeDiscovered",
            "powerRepairStarted",
            "powerTerminalConfirmed",
            "powerRepaired",
            "extractionArmed",
            "extracted",
            "rainCollectorBuilt",
            "waterPurifierBuilt",
            "emergencyWaterLoopSecured",
            "foragedFood",
            "cleanWaterStockpiled",
            "rationsStockpiled",
            "scrapKnifeCrafted",
            "toolAssistedMining",
            "handRecyclerBuilt",
            "machineCasingMade",
            "wastelandFieldKitAssembled",
            "microGeneratorBuilt",
            "powerCableRouted",
            "energyMeterInstalled",
            "scrapDynamoBuilt",
            "basicBatteryCharged",
            "batteryBankBuilt",
            "thermalBurnerBuilt",
            "gasMaskEquipped",
            "schematicFragmentFound",
            "firstSchematicDecoded",
            "scrapPressBuilt",
            "itemPipeInstalled",
            "factoryControllerBuilt",
            "researchLabBuilt",
            "powerCableUpgraded",
            "powerPrioritySet",
            "machineOverclocked",
            "basicFilterFixed",
            "advancedFilterCrafted",
            "thermalArrayBuilt",
            "warmedAfterExposure",
            "atmosphericScrubberBuilt",
            "radiationCleanserBuilt",
            "fieldMedBayBuilt",
            "fieldMedBayUsed",
            "filterWorkbenchBuilt",
            "oreGrinderBuilt",
            "denseAlloyFound",
            "isotopeRefinerBuilt",
            "alloyWeaponForged",
            "alloyKitEquipped",
            "relayStationActivated",
            "scoutDroneBuilt",
            "radAwayUsed",
            "stimPackUsed",
            "handWarmerUsed",
            "thermalLinerInstalled",
            "returnBeaconPlaced",
            "returnKeystoneBound",
            "waterRations",
            "foodRations",
            "repairKits",
            "scavengedSupplyCaches",
            "scrapMetal",
            "scrapWire",
            "scrapCircuit",
            "machineCasings",
            "fieldPowerGenerated",
            "powerCableSegments",
            "energyMeterReadings",
            "machinePowerGenerated",
            "storedEnergyCells",
            "thermalBurnerHeat",
            "schematicFragments",
            "itemPipeSegments",
            "upgradedPowerCableSegments",
            "overclockHeat",
            "basicFilterCharges",
            "advancedFilterCharges",
            "warmthRecoveredSeconds",
            "radiationCleanserCycles",
            "fieldMedBayTreatments",
            "denseAlloyChunks",
            "alloyWeaponDurability",
            "relaySignalStrength",
            "scoutDroneRangeMeters",
            "radAwayDoses",
            "stimPackDoses",
            "handWarmerCharges",
            "thermalLinerWarmthSeconds",
            "returnBeaconSignalStrength",
            "returnKeystoneCharges",
            "scrapKnifeDurability",
            "toolMiningBlocksBroken",
            "dirtyWaterBottles",
            "filteredWaterBottles",
            "cleanWaterStockpile",
            "foodRationStockpile",
            "rainCollectorCollections",
            "waterPurifierCycles",
            "foragedFoodBundles",
            "scavengedLootKeys",
            "playerHealth",
            "hydration",
            "hunger",
            "ashExposure",
            "hydrationRecovered",
            "hungerRecovered",
            "dehydrationDamagePulses",
            "starvationDamagePulses",
            "toxicAshExposureSeconds",
            "hotAshExposureSeconds",
            "unstableGroundStrikes",
            "electricalDischargeHits",
            "extractionStormExposureSeconds",
            "shelterIntegrity",
            "shelterRestSeconds",
            "shelterStormDamage",
            "shelterX",
            "shelterY",
            "shelterZ",
            "powerRebootSeconds",
            "extractionCountdownSeconds",
            "survivalSeconds",
            "lastMessage"
    );

    private EchoAshfallGameplayContracts() {
    }

    public static List<String> requiredSaveFields() {
        return REQUIRED_SAVE_FIELDS;
    }

    public static List<EchoAshfallGameplayFeatureContract> ashfall(
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        Objects.requireNonNull(bridge, "bridge");
        ArrayList<EchoAshfallGameplayFeatureContract> rows = new ArrayList<>();
        rows.add(row(bridge, "voxel.block.toxic_ash", EchoAshfallGameplayFeatureKind.BLOCKS,
                "echoashfallprotocol:block/fallout_dust",
                "Voxel block identity, material atlas, and hardness for toxic ash/fallout dust."));
        rows.add(row(bridge, "voxel.block.scorched_basalt", EchoAshfallGameplayFeatureKind.BLOCKS,
                "echoashfallprotocol:block/scorched_ash",
                "Voxel block identity, material atlas, and hardness for scorched ash terrain."));
        rows.add(row(bridge, "voxel.block.rusted_debris", EchoAshfallGameplayFeatureKind.BLOCKS,
                "echoashfallprotocol:block/rusted_metal_debris",
                "Scavengable crash debris block with AdapterCore voxel identity."));
        rows.add(row(bridge, "voxel.block.ash_hazard_marker", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/toxic_waste_barrel",
                "Breakable hazard target used by hazard-clear objectives."));
        rows.add(row(bridge, "voxel.block.shelter_anchor", EchoAshfallGameplayFeatureKind.SHELTER_LOGIC,
                "echoashfallprotocol:block/ash_campfire",
                "Shelter zone anchor with AdapterCore profile, integrity, rest, and save-backed coordinates.",
                List.of("shelterBuilt", "shelterIntegrity", "shelterRestSeconds",
                        "shelterStormDamage", "shelterX", "shelterY", "shelterZ")));
        rows.add(row(bridge, "voxel.block.crash_cache", EchoAshfallGameplayFeatureKind.LOOT_SCAVENGING,
                "echoashfallprotocol:block/echo_cache",
                "Cache block used by terminal unlocks and scavenge rewards."));
        rows.add(row(bridge, "terminal.block.field_terminal", EchoAshfallGameplayFeatureKind.TERMINALS,
                "echoterminal:block/echo_terminal",
                "Field terminal block interaction target."));
        rows.add(row(bridge, "power.block.damaged_power_node", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/power_node",
                "Repairable power node target."));
        rows.add(row(bridge, "voxel.block.rain_collector", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:block/rain_collector",
                "Rain collector infrastructure target for dirty-water collection."));
        rows.add(row(bridge, "voxel.block.water_purifier", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:block/water_purifier",
                "Water purifier infrastructure target for clean-water stockpiles."));
        rows.add(row(bridge, "voxel.block.hand_recycler", EchoAshfallGameplayFeatureKind.BLOCKS,
                "echoashfallprotocol:block/hand_recycler",
                "Hand recycler field workshop block for scrap conversion.",
                List.of("handRecyclerBuilt")));
        rows.add(row(bridge, "power.block.micro_generator", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/micro_generator",
                "Micro generator field power block placed before damaged node repair.",
                List.of("microGeneratorBuilt", "fieldPowerGenerated")));
        rows.add(row(bridge, "power.block.power_cable", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/power_cable",
                "Power cable route block used to connect field generation to the damaged node.",
                List.of("powerCableRouted", "powerCableSegments")));
        rows.add(row(bridge, "power.block.energy_meter", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/energy_meter",
                "Energy meter block proves field microgrid output before node repair.",
                List.of("energyMeterInstalled", "energyMeterReadings")));
        rows.add(row(bridge, "power.block.scrap_dynamo", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/scrap_dynamo",
                "Scrap dynamo starts the AdapterCore machine power route after field power is verified.",
                List.of("scrapDynamoBuilt", "machinePowerGenerated")));
        rows.add(row(bridge, "power.block.battery_bank", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/battery_bank",
                "Battery bank buffers charged energy cells for the extraction power repair route.",
                List.of("batteryBankBuilt", "storedEnergyCells")));
        rows.add(row(bridge, "power.block.thermal_burner", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/thermal_burner",
                "Thermal burner stabilizes the high-load power repair route.",
                List.of("thermalBurnerBuilt", "thermalBurnerHeat")));
        rows.add(row(bridge, "factory.block.scrap_press", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/scrap_press",
                "Scrap press starts the protected factory upgrade route before extraction power repair.",
                List.of("scrapPressBuilt")));
        rows.add(row(bridge, "factory.block.item_pipe", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/item_pipe",
                "Item pipe segments route pressed materials between factory machines.",
                List.of("itemPipeInstalled", "itemPipeSegments")));
        rows.add(row(bridge, "factory.block.factory_controller", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/factory_controller",
                "Factory controller coordinates the upgraded machine route and power priority.",
                List.of("factoryControllerBuilt", "powerPrioritySet")));
        rows.add(row(bridge, "factory.block.research_lab", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/research_lab",
                "Research lab decodes the first schematic before reinforced power upgrades.",
                List.of("researchLabBuilt", "firstSchematicDecoded")));
        rows.add(row(bridge, "factory.block.reinforced_power_cable", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/reinforced_power_cable",
                "Reinforced cable segments carry overclocked machine load to the damaged node.",
                List.of("powerCableUpgraded", "upgradedPowerCableSegments")));
        rows.add(row(bridge, "power.block.high_voltage_power_cable", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/high_voltage_power_cable",
                "High-voltage cable is an AdapterCore machine-power route target for full-game load transfer."));
        rows.add(row(bridge, "machine.block.filter_workbench", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/filter_workbench",
                "Filter workbench places the AdapterCore advanced expedition crafting station.",
                List.of("filterWorkbenchBuilt")));
        rows.add(row(bridge, "power.block.load_distributor", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/load_distributor",
                "Load distributor routes AdapterCore machine power priority modes across factory consumers.",
                List.of("powerPrioritySet")));
        rows.add(row(bridge, "machine.block.filter_workbench", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/filter_workbench",
                "Filter workbench maps expedition treatment into AdapterCore advanced relay preparation.",
                List.of("filterWorkbenchBuilt", "basicFilterCharges")));
        rows.add(row(bridge, "machine.block.ore_grinder", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/ore_grinder",
                "Ore grinder is an AdapterCore powered processor endpoint for dense alloy recovery.",
                List.of("oreGrinderBuilt", "denseAlloyFound", "denseAlloyChunks")));
        rows.add(row(bridge, "machine.block.isotope_refiner", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/isotope_refiner",
                "Isotope refiner maps powered catalyst processing and contamination risk into AdapterCore machine contracts.",
                List.of("isotopeRefinerBuilt", "radiationCleanserCycles")));
        rows.add(row(bridge, "machine.block.relay_station", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/relay_station",
                "Relay station maps alloy kit and scanner lens progression into AdapterCore extraction-route preparation.",
                List.of("relayStationActivated", "relaySignalStrength")));
        rows.add(row(bridge, "exploration.block.signal_scanner", EchoAshfallGameplayFeatureKind.MISSIONS,
                "echoashfallprotocol:block/signal_scanner",
                "Stationary signal scanner maps NeoForge POI detection and route scans into an AdapterCore block identity."));
        rows.add(row(bridge, "loot.block.structure_cache", EchoAshfallGameplayFeatureKind.LOOT_SCAVENGING,
                "echoashfallprotocol:block/structure_cache",
                "Structure cache maps procedural POI cache placement and shared loot access into AdapterCore block identity.",
                List.of("scavengedSupplyCaches", "scavengedLootKeys")));
        rows.add(row(bridge, "loot.block.echo_crate", EchoAshfallGameplayFeatureKind.LOOT_SCAVENGING,
                "echoashfallprotocol:block/echo_crate",
                "ECHO crate maps NeoForge container loot into an AdapterCore scavenge container target.",
                List.of("scavengedSupplyCaches", "scavengedLootKeys")));
        rows.add(row(bridge, "nexus.block.nexus_core", EchoAshfallGameplayFeatureKind.MISSIONS,
                "echoashfallprotocol:block/nexus_core",
                "Nexus core maps final route discovery, awakening, and decision objectives to a stable AdapterCore block target."));
        rows.add(row(bridge, "power.block.nexus_capacitor", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/nexus_capacitor",
                "Nexus capacitor maps late-game FE storage and high-voltage power stabilization into AdapterCore power identity."));
        for (String blockId : EchoAdapterCoreStandaloneContentBridge.ASHFALL_ENVIRONMENT_BLOCK_IDS) {
            rows.add(row(bridge, "voxel.block." + blockId, EchoAshfallGameplayFeatureKind.BLOCKS,
                    "echoashfallprotocol:block/" + blockId,
                    "NeoForge block registry row resolves to an AdapterCore voxel identity with portable hardness."));
        }
        rows.add(row(bridge, "machine.block.crystalline_synthesizer", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/crystalline_synthesizer",
                "Crystalline synthesizer maps phased powered processing into AdapterCore machine contracts."));
        rows.add(row(bridge, "machine.block.deep_core_miner", EchoAshfallGameplayFeatureKind.LOOT_SCAVENGING,
                "echoashfallprotocol:block/deep_core_miner",
                "Deep core miner maps depth-gated powered resource generation into AdapterCore contracts."));
        rows.add(row(bridge, "machine.block.autofeed_hopper", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:block/autofeed_hopper",
                "Autofeed hopper maps powered player feeding support into AdapterCore survival contracts."));
        rows.add(row(bridge, "machine.block.contaminant_condenser", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/contaminant_condenser",
                "Contaminant condenser maps toxic-puddle conversion into AdapterCore world hazard cleanup contracts."));
        rows.add(row(bridge, "safety.block.thermal_array", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/thermal_array",
                "Thermal array block provides AdapterCore cold-exposure recovery for expedition hazards.",
                List.of("thermalArrayBuilt", "warmedAfterExposure", "warmthRecoveredSeconds")));
        rows.add(row(bridge, "safety.block.atmospheric_scrubber", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/atmospheric_scrubber",
                "Atmospheric scrubber block maps toxic ash mitigation into the expedition safety profile.",
                List.of("atmosphericScrubberBuilt", "basicFilterFixed", "advancedFilterCrafted")));
        rows.add(row(bridge, "safety.block.radiation_cleanser", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/radiation_cleanser",
                "Radiation cleanser block clears exposure cycles through AdapterCore hazard state.",
                List.of("radiationCleanserBuilt", "radiationCleanserCycles")));
        rows.add(row(bridge, "safety.block.field_med_bay", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                "echoashfallprotocol:block/field_med_bay",
                "Field med bay block maps field treatment into AdapterCore survival recovery state.",
                List.of("fieldMedBayBuilt", "fieldMedBayUsed", "fieldMedBayTreatments")));
        rows.add(row(bridge, "block_entity.water_purifier", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:block/water_purifier",
                "NeoForge water purifier block entity maps to AdapterCore water-loop state and purifier cycle fields.",
                List.of("waterPurifierBuilt", "waterPurifierCycles", "filteredWaterBottles", "cleanWaterStockpile")));
        rows.add(row(bridge, "block_entity.rain_collector", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:block/rain_collector",
                "NeoForge rain collector block entity maps to AdapterCore dirty-water collection state.",
                List.of("rainCollectorBuilt", "rainCollectorCollections", "dirtyWaterBottles")));
        rows.add(row(bridge, "block_entity.hand_recycler", EchoAshfallGameplayFeatureKind.ITEMS,
                "echoashfallprotocol:block/hand_recycler",
                "NeoForge hand recycler block entity maps to AdapterCore field workshop conversion state.",
                List.of("handRecyclerBuilt", "machineCasingMade", "machineCasings")));
        rows.add(row(bridge, "block_entity.micro_generator", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/micro_generator",
                "NeoForge micro generator block entity maps to AdapterCore field microgrid output state.",
                List.of("microGeneratorBuilt", "fieldPowerGenerated")));
        rows.add(row(bridge, "block_entity.power_cable", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/power_cable",
                "NeoForge power cable block entity maps to AdapterCore routed cable segment state.",
                List.of("powerCableRouted", "powerCableSegments")));
        rows.add(row(bridge, "block_entity.damaged_power_node", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/power_node",
                "NeoForge power node block entity maps to AdapterCore repair and terminal restoration state.",
                List.of("powerNodeDiscovered", "powerRepairStarted", "powerTerminalConfirmed", "powerRepaired")));
        rows.add(row(bridge, "block_entity.scrap_dynamo", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/scrap_dynamo",
                "NeoForge scrap dynamo block entity maps to AdapterCore machine power generation state.",
                List.of("scrapDynamoBuilt", "machinePowerGenerated")));
        rows.add(row(bridge, "block_entity.battery_bank", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/battery_bank",
                "NeoForge battery bank block entity maps to AdapterCore energy cell storage state.",
                List.of("basicBatteryCharged", "batteryBankBuilt", "storedEnergyCells")));
        rows.add(row(bridge, "block_entity.thermal_burner", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/thermal_burner",
                "NeoForge thermal burner block entity maps to AdapterCore high-load heat state.",
                List.of("thermalBurnerBuilt", "thermalBurnerHeat")));
        rows.add(row(bridge, "block_entity.scrap_press", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/scrap_press",
                "NeoForge scrap press block entity maps to AdapterCore midgame factory progression state.",
                List.of("scrapPressBuilt")));
        rows.add(row(bridge, "block_entity.item_pipe", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/item_pipe",
                "NeoForge item pipe block entity maps to AdapterCore factory segment state.",
                List.of("itemPipeInstalled", "itemPipeSegments")));
        rows.add(row(bridge, "block_entity.factory_controller", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/factory_controller",
                "NeoForge factory controller block entity maps to AdapterCore factory coordination and priority state.",
                List.of("factoryControllerBuilt", "powerPrioritySet")));
        rows.add(row(bridge, "block_entity.filter_workbench", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/filter_workbench",
                "NeoForge filter workbench block entity maps to AdapterCore advanced expedition crafting state.",
                List.of("filterWorkbenchBuilt")));
        rows.add(row(bridge, "block_entity.load_distributor", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/load_distributor",
                "NeoForge load distributor block entity maps to AdapterCore power router priority contracts.",
                List.of("powerPrioritySet")));
        rows.add(row(bridge, "block_entity.filter_workbench", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/filter_workbench",
                "NeoForge filter workbench block entity maps to AdapterCore advanced expedition preparation state.",
                List.of("filterWorkbenchBuilt", "basicFilterCharges")));
        rows.add(row(bridge, "block_entity.ore_grinder", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/ore_grinder",
                "NeoForge ore grinder block entity maps to AdapterCore dense alloy processing state.",
                List.of("oreGrinderBuilt", "denseAlloyFound", "denseAlloyChunks")));
        rows.add(row(bridge, "block_entity.isotope_refiner", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/isotope_refiner",
                "NeoForge isotope refiner block entity maps to AdapterCore powered catalyst and contamination state.",
                List.of("isotopeRefinerBuilt", "radiationCleanserCycles")));
        rows.add(row(bridge, "block_entity.relay_station", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/relay_station",
                "NeoForge relay station block entity maps to AdapterCore relay signal state.",
                List.of("relayStationActivated", "relaySignalStrength")));
        rows.add(row(bridge, "block_entity.signal_scanner", EchoAshfallGameplayFeatureKind.MISSIONS,
                "echoashfallprotocol:block/signal_scanner",
                "NeoForge signal scanner block entity maps to AdapterCore stationary route-scan state."));
        rows.add(row(bridge, "block_entity.structure_cache", EchoAshfallGameplayFeatureKind.LOOT_SCAVENGING,
                "echoashfallprotocol:block/structure_cache",
                "NeoForge structure cache block entity maps to AdapterCore shared scavenge-cache state.",
                List.of("scavengedSupplyCaches", "scavengedLootKeys")));
        rows.add(row(bridge, "block_entity.echo_container", EchoAshfallGameplayFeatureKind.LOOT_SCAVENGING,
                "echoashfallprotocol:block/echo_crate",
                "NeoForge ECHO container block entity maps ECHO cache, crate, and supply-crate containers to AdapterCore scavenge targets.",
                List.of("scavengedSupplyCaches", "scavengedLootKeys")));
        rows.add(row(bridge, "block_entity.nexus_core", EchoAshfallGameplayFeatureKind.MISSIONS,
                "echoashfallprotocol:block/nexus_core",
                "NeoForge Nexus core block entity maps core awakening and route-decision state to an AdapterCore mission target."));
        rows.add(row(bridge, "block_entity.nexus_capacitor", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/nexus_capacitor",
                "NeoForge Nexus capacitor block entity maps late-game energy storage to AdapterCore power contracts."));
        rows.add(row(bridge, "block_entity.crystalline_synthesizer", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:block/crystalline_synthesizer",
                "NeoForge crystalline synthesizer block entity maps to AdapterCore phased processor contracts."));
        rows.add(row(bridge, "block_entity.deep_core_miner", EchoAshfallGameplayFeatureKind.LOOT_SCAVENGING,
                "echoashfallprotocol:block/deep_core_miner",
                "NeoForge deep core miner block entity maps to AdapterCore depth-gated resource generation contracts."));
        rows.add(row(bridge, "block_entity.autofeed_hopper", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:block/autofeed_hopper",
                "NeoForge autofeed hopper block entity maps to AdapterCore powered player feeding support contracts."));
        rows.add(row(bridge, "block_entity.contaminant_condenser", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/contaminant_condenser",
                "NeoForge contaminant condenser block entity maps to AdapterCore toxic-puddle conversion contracts."));
        rows.add(row(bridge, "block_entity.thermal_array", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/thermal_array",
                "NeoForge thermal array block entity maps to AdapterCore warmth recovery state.",
                List.of("thermalArrayBuilt", "warmedAfterExposure", "warmthRecoveredSeconds")));
        rows.add(row(bridge, "block_entity.atmospheric_scrubber", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/atmospheric_scrubber",
                "NeoForge atmospheric scrubber block entity maps to AdapterCore filter and toxic ash mitigation state.",
                List.of("atmosphericScrubberBuilt", "basicFilterFixed", "advancedFilterCrafted")));
        rows.add(row(bridge, "block_entity.radiation_cleanser", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:block/radiation_cleanser",
                "NeoForge radiation cleanser block entity maps to AdapterCore cleanser cycle state.",
                List.of("radiationCleanserBuilt", "radiationCleanserCycles")));
        rows.add(row(bridge, "block_entity.field_med_bay", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                "echoashfallprotocol:block/field_med_bay",
                "NeoForge field med bay block entity maps to AdapterCore field treatment state.",
                List.of("fieldMedBayBuilt", "fieldMedBayUsed", "fieldMedBayTreatments")));
        rows.add(row(bridge, "item.water_ration", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:item/clean_water_bottle",
                "Water ration item stack and use contract."));
        rows.add(row(bridge, "item.dirty_water_bottle", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:item/dirty_water_bottle",
                "Dirty water bottle stack produced by rain collector flow."));
        rows.add(row(bridge, "item.filtered_water_bottle", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:item/filtered_water_bottle",
                "Filtered water bottle stack represented by purifier cycle output."));
        rows.add(row(bridge, "item.field_ration", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:item/emergency_ration",
                "Field ration item stack and hunger recovery contract."));
        rows.add(row(bridge, "item.scrap_metal", EchoAshfallGameplayFeatureKind.ITEMS,
                "echoashfallprotocol:item/scrap_metal",
                "Scrap metal item stack recovered from AdapterCore debris mining.",
                List.of("scrapMetal")));
        rows.add(row(bridge, "item.scrap_wire", EchoAshfallGameplayFeatureKind.ITEMS,
                "echoashfallprotocol:item/scrap_wire",
                "Scrap wire field kit ingredient recovered through AdapterCore workshop progression.",
                List.of("scrapWire")));
        rows.add(row(bridge, "item.scrap_circuit", EchoAshfallGameplayFeatureKind.ITEMS,
                "echoashfallprotocol:item/scrap_circuit",
                "Scrap circuit field kit ingredient recovered through AdapterCore workshop progression.",
                List.of("scrapCircuit")));
        rows.add(row(bridge, "item.machine_casing", EchoAshfallGameplayFeatureKind.ITEMS,
                "echoashfallprotocol:item/machine_casing",
                "Machine casing produced from scrap in the field workshop chain.",
                List.of("machineCasingMade", "machineCasings")));
        rows.add(row(bridge, "item.scrap_knife", EchoAshfallGameplayFeatureKind.TOOLS,
                "echoashfallprotocol:item/scrap_knife",
                "Scrap knife item used by AdapterCore tool profile mining.",
                List.of("scrapKnifeCrafted", "scrapKnifeDurability", "toolMiningBlocksBroken")));
        rows.add(row(bridge, "item.gas_mask", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:item/gas_mask",
                "Gas mask item gates protected midgame factory work in toxic Ashfall conditions.",
                List.of("gasMaskEquipped")));
        rows.add(row(bridge, "item.schematic_fragment", EchoAshfallGameplayFeatureKind.ITEMS,
                "echoashfallprotocol:item/schematic_fragment",
                "Schematic fragment unlocks the first factory blueprint and research lab route.",
                List.of("schematicFragmentFound", "schematicFragments", "firstSchematicDecoded")));
        rows.add(row(bridge, "item.basic_filter_cartridge", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:item/filter_cartridge_basic",
                "Basic filter cartridge item repairs the gas mask route through AdapterCore safety state.",
                List.of("basicFilterFixed", "basicFilterCharges")));
        rows.add(row(bridge, "item.advanced_filter_cartridge", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:item/filter_cartridge_advanced",
                "Advanced filter cartridge item extends expedition hazard mitigation through AdapterCore safety state.",
                List.of("advancedFilterCrafted", "advancedFilterCharges")));
        rows.add(row(bridge, "item.dense_alloy_chunk", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:item/dense_alloy_chunk",
                "Dense alloy chunk item is produced by AdapterCore ore grinding and consumed by alloy progression.",
                List.of("denseAlloyFound", "denseAlloyChunks")));
        rows.add(row(bridge, "item.alloy_blade", EchoAshfallGameplayFeatureKind.TOOLS,
                "echoashfallprotocol:item/alloy_blade",
                "Alloy blade item maps full-game weapon crafting into AdapterCore advanced expedition state.",
                List.of("alloyWeaponForged", "alloyWeaponDurability", "denseAlloyChunks")));
        rows.add(row(bridge, "item.alloy_helmet", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:item/alloy_helmet",
                "Alloy helmet item participates in AdapterCore expedition armor kit gating.",
                List.of("alloyKitEquipped")));
        rows.add(row(bridge, "item.alloy_chestplate", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:item/alloy_chestplate",
                "Alloy chestplate item participates in AdapterCore expedition armor kit gating.",
                List.of("alloyKitEquipped")));
        rows.add(row(bridge, "item.relay_scanner_lens", EchoAshfallGameplayFeatureKind.TOOLS,
                "echoashfallprotocol:item/relay_scanner_lens",
                "Relay scanner lens item activates the AdapterCore relay station route.",
                List.of("relayStationActivated", "relaySignalStrength")));
        rows.add(row(bridge, "item.scout_drone", EchoAshfallGameplayFeatureKind.MISSIONS,
                "echoashfallprotocol:item/scout_drone_item",
                "Scout drone item builds an AdapterCore entity/runtime target for advanced expedition scouting.",
                List.of("scoutDroneBuilt", "scoutDroneRangeMeters")));
        rows.add(row(bridge, "item.dense_alloy_chunk", EchoAshfallGameplayFeatureKind.ITEMS,
                "echoashfallprotocol:item/dense_alloy_chunk",
                "Dense alloy chunk item is recovered by the AdapterCore ore grinder and consumed by alloy forging.",
                List.of("denseAlloyFound", "denseAlloyChunks")));
        rows.add(row(bridge, "item.alloy_blade", EchoAshfallGameplayFeatureKind.TOOLS,
                "echoashfallprotocol:item/alloy_blade",
                "Alloy blade item maps the advanced weapon forge step into AdapterCore live mission state.",
                List.of("alloyWeaponForged", "alloyWeaponDurability", "denseAlloyChunks")));
        rows.add(row(bridge, "item.alloy_helmet", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                "echoashfallprotocol:item/alloy_helmet",
                "Alloy helmet item participates in the AdapterCore advanced route armor kit.",
                List.of("alloyKitEquipped")));
        rows.add(row(bridge, "item.alloy_chestplate", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                "echoashfallprotocol:item/alloy_chestplate",
                "Alloy chestplate item participates in the AdapterCore advanced route armor kit.",
                List.of("alloyKitEquipped")));
        rows.add(row(bridge, "item.relay_scanner_lens", EchoAshfallGameplayFeatureKind.TOOLS,
                "echoashfallprotocol:item/relay_scanner_lens",
                "Relay scanner lens item activates the AdapterCore relay station route.",
                List.of("relayStationActivated", "relaySignalStrength")));
        rows.add(row(bridge, "item.scout_drone", EchoAshfallGameplayFeatureKind.TOOLS,
                "echoashfallprotocol:item/scout_drone_item",
                "Scout drone item maps route survey prep into AdapterCore mission and save state.",
                List.of("scoutDroneBuilt", "scoutDroneRangeMeters")));
        rows.add(row(bridge, "item.rad_away", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                "echoashfallprotocol:item/rad_away",
                "Rad away item clears ash exposure through AdapterCore field recovery state.",
                List.of("radAwayUsed", "radAwayDoses", "ashExposure", "toxicAshExposureSeconds")));
        rows.add(row(bridge, "item.stim_pack", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                "echoashfallprotocol:item/stim_pack",
                "Stim pack item restores health through AdapterCore field recovery state.",
                List.of("stimPackUsed", "stimPackDoses", "playerHealth")));
        rows.add(row(bridge, "item.hand_warmer", EchoAshfallGameplayFeatureKind.HAZARDS,
                "echoashfallprotocol:item/hand_warmer",
                "Hand warmer item extends thermal recovery through AdapterCore warmth state.",
                List.of("handWarmerUsed", "handWarmerCharges", "warmthRecoveredSeconds")));
        rows.add(row(bridge, "item.thermal_liner", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                "echoashfallprotocol:item/thermal_liner",
                "Thermal liner item upgrades the alloy kit with persistent warmth buffer state.",
                List.of("thermalLinerInstalled", "thermalLinerWarmthSeconds", "warmthRecoveredSeconds")));
        rows.add(row(bridge, "item.return_beacon", EchoAshfallGameplayFeatureKind.EXTRACTION_LOGIC,
                "echoashfallprotocol:item/return_beacon",
                "Return beacon item maps fallback extraction signal readiness into AdapterCore state.",
                List.of("returnBeaconPlaced", "returnBeaconSignalStrength")));
        rows.add(row(bridge, "item.return_keystone", EchoAshfallGameplayFeatureKind.EXTRACTION_LOGIC,
                "echoashfallprotocol:item/return_keystone",
                "Return keystone item binds the beacon fallback through AdapterCore save state.",
                List.of("returnKeystoneBound", "returnKeystoneCharges")));
        rows.add(row(bridge, "survival.profile.basic_needs", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                EchoAdapterCoreStandaloneContentBridge.SURVIVAL_PROFILE_ID,
                "AdapterCore basic-needs profile owns ration recovery, movement drain, and deprivation damage thresholds.",
                List.of("hydrationRecovered", "hungerRecovered",
                        "dehydrationDamagePulses", "starvationDamagePulses")));
        rows.add(row(bridge, "survival.profile.emergency_water_loop", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                EchoAdapterCoreStandaloneContentBridge.WATER_LOOP_PROFILE_ID,
                "AdapterCore emergency water-loop profile owns rain collection, purifier output, forage rewards, and stockpile targets.",
                List.of("rainCollectorBuilt", "waterPurifierBuilt", "emergencyWaterLoopSecured",
                        "foragedFood", "cleanWaterStockpiled", "rationsStockpiled",
                        "dirtyWaterBottles", "cleanWaterStockpile", "foodRationStockpile")));
        rows.add(row(bridge, "tool.profile.scrap_field_tools", EchoAshfallGameplayFeatureKind.TOOLS,
                EchoAdapterCoreStandaloneContentBridge.TOOL_PROFILE_ID,
                "AdapterCore tool profile owns scrap knife recipe cost, speed multiplier, and durability.",
                List.of("scrapKnifeCrafted", "toolAssistedMining",
                        "scrapMetal", "scrapKnifeDurability", "toolMiningBlocksBroken")));
        rows.add(row(bridge, "workshop.profile.field_recycler", EchoAshfallGameplayFeatureKind.ITEMS,
                EchoAdapterCoreStandaloneContentBridge.FIELD_WORKSHOP_PROFILE_ID,
                "AdapterCore field workshop profile owns hand recycler placement, casing conversion, and field-kit ingredients.",
                List.of("handRecyclerBuilt", "machineCasingMade", "wastelandFieldKitAssembled",
                        "scrapWire", "scrapCircuit", "machineCasings")));
        rows.add(row(bridge, "power.profile.field_microgrid", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.FIELD_POWER_PROFILE_ID,
                "AdapterCore field power profile owns micro generator placement, cable routing, and meter readings.",
                List.of("microGeneratorBuilt", "powerCableRouted", "energyMeterInstalled",
                        "fieldPowerGenerated", "powerCableSegments", "energyMeterReadings")));
        rows.add(row(bridge, "power.profile.machine_route", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.MACHINE_POWER_PROFILE_ID,
                "AdapterCore machine power profile owns scrap dynamo, energy cell charge, battery bank, and thermal burner progression.",
                List.of("scrapDynamoBuilt", "basicBatteryCharged", "batteryBankBuilt", "thermalBurnerBuilt",
                        "machinePowerGenerated", "storedEnergyCells", "thermalBurnerHeat")));
        rows.add(row(bridge, "progression.profile.midgame_factory", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.MIDGAME_PROGRESSION_PROFILE_ID,
                "AdapterCore midgame factory profile owns protective gear, schematic, factory blocks, upgraded cable, priority, and overclock gates.",
                List.of("gasMaskEquipped", "schematicFragmentFound", "firstSchematicDecoded",
                        "scrapPressBuilt", "itemPipeInstalled", "factoryControllerBuilt", "researchLabBuilt",
                        "powerCableUpgraded", "powerPrioritySet", "machineOverclocked",
                        "schematicFragments", "itemPipeSegments", "upgradedPowerCableSegments", "overclockHeat")));
        rows.add(row(bridge, "safety.profile.expedition_hazards", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.EXPEDITION_SAFETY_PROFILE_ID,
                "AdapterCore expedition safety profile owns filter repair, cold recovery, scrubber, cleanser, and med bay gates.",
                List.of("basicFilterFixed", "advancedFilterCrafted", "thermalArrayBuilt", "warmedAfterExposure",
                        "atmosphericScrubberBuilt", "radiationCleanserBuilt", "fieldMedBayBuilt", "fieldMedBayUsed",
                        "basicFilterCharges", "advancedFilterCharges", "warmthRecoveredSeconds",
                        "radiationCleanserCycles", "fieldMedBayTreatments")));
        rows.add(row(bridge, "progression.profile.advanced_expedition", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.ADVANCED_EXPEDITION_PROFILE_ID,
                "AdapterCore advanced expedition profile owns filter workbench, ore grinding, dense alloy, isotope refining, relay activation, and scout drone gates.",
                List.of("filterWorkbenchBuilt", "oreGrinderBuilt", "denseAlloyFound", "isotopeRefinerBuilt",
                        "alloyWeaponForged", "alloyKitEquipped", "relayStationActivated", "scoutDroneBuilt",
                        "denseAlloyChunks", "alloyWeaponDurability", "relaySignalStrength", "scoutDroneRangeMeters")));
        rows.add(row(bridge, "recovery.profile.field_recovery", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                EchoAdapterCoreStandaloneContentBridge.FIELD_RECOVERY_PROFILE_ID,
                "AdapterCore field recovery profile owns anti-rad, healing, warmth, return beacon, and keystone use.",
                List.of("radAwayUsed", "stimPackUsed", "handWarmerUsed", "thermalLinerInstalled",
                        "returnBeaconPlaced", "returnKeystoneBound", "radAwayDoses", "stimPackDoses",
                        "handWarmerCharges", "thermalLinerWarmthSeconds", "returnBeaconSignalStrength",
                        "returnKeystoneCharges")));
        rows.add(row(bridge, "tool.emergency_scanner", EchoAshfallGameplayFeatureKind.TOOLS,
                "echoashfallprotocol:item/portable_signal_scanner",
                "Emergency scanner item use contract for mission target discovery."));
        rows.add(row(bridge, "item.power_repair_kit", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:item/power_cell",
                "Power-cell item consumed by damaged power node objectives."));
        rows.add(row(bridge, "item.energy_cell", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                "echoashfallprotocol:item/energy_cell",
                "Energy cell item is charged by the scrap dynamo and stored in the battery bank.",
                List.of("basicBatteryCharged", "storedEnergyCells")));
        rows.add(row(bridge, "entity.scavenger_bandit", EchoAshfallGameplayFeatureKind.MISSIONS,
                "echoashfallprotocol:entity/scavenger_bandit",
                "Mission encounter entity target."));
        rows.add(row(bridge, "entity.scout_drone", EchoAshfallGameplayFeatureKind.MISSIONS,
                EchoAdapterCoreStandaloneContentBridge.SCOUT_DRONE_ENTITY_ID,
                "Scout drone entity target is registered through AdapterCore instead of NeoForge entity classes.",
                List.of("scoutDroneBuilt", "scoutDroneRangeMeters")));
        for (String entityId : EchoAdapterCoreStandaloneContentBridge.ASHFALL_ADDITIONAL_ENTITY_IDS) {
            rows.add(row(bridge, "entity." + entityId, EchoAshfallGameplayFeatureKind.MISSIONS,
                    "echoashfallprotocol:entity/" + entityId,
                    "NeoForge entity registry target resolves through AdapterCore for Native and Standalone."));
        }
        for (String entityId : EchoAdapterCoreStandaloneContentBridge.ASHFALL_SPAWN_EGG_ENTITY_IDS) {
            String itemId = entityId + "_spawn_egg";
            rows.add(row(bridge, "item." + itemId, EchoAshfallGameplayFeatureKind.ITEMS,
                    "echoashfallprotocol:item/" + itemId,
                    "NeoForge spawn egg item resolves to an AdapterCore entity spawn target."));
        }
        rows.add(row(bridge, "recipe.field_filter_patch", EchoAshfallGameplayFeatureKind.ITEMS,
                "echoashfallprotocol:recipe/filter_cartridge_basic",
                "Recipe/data contract for field-filter crafting parity."));
        rows.add(row(bridge, "recipe.scrap_knife", EchoAshfallGameplayFeatureKind.TOOLS,
                EchoAdapterCoreStandaloneContentBridge.SCRAP_KNIFE_RECIPE_ID,
                "Scrap knife recipe target mapped into AdapterCore tool progression.",
                List.of("scrapMetal", "scrapKnifeCrafted")));
        rows.add(row(bridge, "loot.rusted_debris", EchoAshfallGameplayFeatureKind.LOOT_SCAVENGING,
                EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_LOOT_ID,
                "Rusted debris scavenge table contract for field-ration recovery."));
        rows.add(row(bridge, "loot.crash_cache", EchoAshfallGameplayFeatureKind.LOOT_SCAVENGING,
                EchoAdapterCoreStandaloneContentBridge.CRASH_CACHE_LOOT_ID,
                "Crash cache loot/scavenge table contract."));
        rows.add(row(bridge, "loot.damaged_power_node", EchoAshfallGameplayFeatureKind.LOOT_SCAVENGING,
                EchoAdapterCoreStandaloneContentBridge.DAMAGED_POWER_NODE_LOOT_ID,
                "Damaged power node scavenge table contract for repair-kit recovery."));
        rows.add(row(bridge, "structure.crash_site_outpost", EchoAshfallGameplayFeatureKind.WORLD_REGIONS,
                "echoashfallprotocol:structure/crash_zone_wasteland",
                "Crash-site outpost structure target for both worldgen runtimes."));
        rows.add(row(bridge, "ui.field_terminal", EchoAshfallGameplayFeatureKind.TERMINALS,
                "echoterminal:ui/field_terminal",
                "Field terminal UI target."));
        for (String menuId : EchoAdapterCoreStandaloneContentBridge.ASHFALL_MENU_SCREEN_IDS) {
            rows.add(row(bridge, "ui.menu." + menuId, EchoAshfallGameplayFeatureKind.TERMINALS,
                    "echoashfallprotocol:ui/" + menuId,
                    "NeoForge MenuType " + menuId + " resolves to an AdapterCore UI screen target for Native and Standalone."));
        }
        rows.add(row(bridge, "sound.radio_static", EchoAshfallGameplayFeatureKind.TERMINALS,
                "echoashfallprotocol:sound/ui.echo_message",
                "Terminal and mission feedback sound event."));
        rows.add(row(bridge, "mission.secure_crash_site", EchoAshfallGameplayFeatureKind.MISSIONS,
                "echoashfallprotocol:mission/secure_crash_outpost",
                "Shared mission objective contract for the playable crash-site route."));
        rows.add(row(bridge, "mission.drink_clean_water", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:mission/drink_clean_water",
                "Clean-water use mission maps to water ration recovery and save fields.",
                List.of("waterUsed", "hydrationRecovered")));
        rows.add(row(bridge, "mission.secure_emergency_water_loop", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:mission/secure_emergency_water_loop",
                "Emergency dirty-water loop mission maps rain collection into save-backed state.",
                List.of("emergencyWaterLoopSecured", "dirtyWaterBottles", "rainCollectorCollections")));
        rows.add(row(bridge, "mission.forage_wasteland_food", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:mission/forage_wasteland_food",
                "Forage mission produces a ration buffer through AdapterCore water-loop profile.",
                List.of("foragedFood", "foragedFoodBundles", "foodRationStockpile")));
        rows.add(row(bridge, "mission.build_rain_collector", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:mission/build_rain_collector",
                "Rain collector placement mission targets the AdapterCore rain collector block.",
                List.of("rainCollectorBuilt")));
        rows.add(row(bridge, "mission.stockpile_rations", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:mission/stockpile_rations",
                "Ration stockpile mission tracks forage/scavenge reserve counts.",
                List.of("rationsStockpiled", "foodRationStockpile")));
        rows.add(row(bridge, "mission.build_water_purifier", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:mission/build_water_purifier",
                "Water purifier placement mission targets the AdapterCore purifier block.",
                List.of("waterPurifierBuilt")));
        rows.add(row(bridge, "mission.stockpile_clean_water", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                "echoashfallprotocol:mission/stockpile_clean_water",
                "Clean-water stockpile mission tracks purifier cycles and reserve count.",
                List.of("cleanWaterStockpiled", "cleanWaterStockpile", "waterPurifierCycles")));
        rows.add(row(bridge, "mission.craft_scrap_knife", EchoAshfallGameplayFeatureKind.TOOLS,
                EchoAdapterCoreStandaloneContentBridge.CRAFT_SCRAP_KNIFE_MISSION_ID,
                "Craft scrap knife mission maps scavenged scrap into a durable mining tool.",
                List.of("scrapKnifeCrafted", "scrapMetal", "scrapKnifeDurability")));
        rows.add(row(bridge, "mission.build_hand_recycler", EchoAshfallGameplayFeatureKind.ITEMS,
                EchoAdapterCoreStandaloneContentBridge.BUILD_HAND_RECYCLER_MISSION_ID,
                "Build hand recycler mission maps field workshop placement to AdapterCore state.",
                List.of("handRecyclerBuilt")));
        rows.add(row(bridge, "mission.make_machine_casing", EchoAshfallGameplayFeatureKind.ITEMS,
                EchoAdapterCoreStandaloneContentBridge.MAKE_MACHINE_CASING_MISSION_ID,
                "Make machine casing mission converts recovered scrap through the hand recycler.",
                List.of("machineCasingMade", "machineCasings", "scrapMetal")));
        rows.add(row(bridge, "mission.assemble_wasteland_field_kit", EchoAshfallGameplayFeatureKind.ITEMS,
                EchoAdapterCoreStandaloneContentBridge.ASSEMBLE_FIELD_KIT_MISSION_ID,
                "Wasteland field kit mission consumes casing, wire, and circuit before the power route.",
                List.of("wastelandFieldKitAssembled", "scrapWire", "scrapCircuit", "machineCasings")));
        rows.add(row(bridge, "mission.build_micro_generator", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.BUILD_MICRO_GENERATOR_MISSION_ID,
                "Build micro generator mission consumes the field kit into an AdapterCore power source.",
                List.of("microGeneratorBuilt", "wastelandFieldKitAssembled", "fieldPowerGenerated")));
        rows.add(row(bridge, "mission.route_power_cable", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.ROUTE_POWER_CABLE_MISSION_ID,
                "Route power cable mission tracks multiple AdapterCore cable segments before node repair.",
                List.of("powerCableRouted", "powerCableSegments")));
        rows.add(row(bridge, "mission.install_energy_meter", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.INSTALL_ENERGY_METER_MISSION_ID,
                "Install energy meter mission records a live reading from the field microgrid.",
                List.of("energyMeterInstalled", "energyMeterReadings", "fieldPowerGenerated")));
        rows.add(row(bridge, "mission.build_scrap_dynamo", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.BUILD_SCRAP_DYNAMO_MISSION_ID,
                "Build scrap dynamo mission starts the AdapterCore machine power route.",
                List.of("scrapDynamoBuilt", "machinePowerGenerated")));
        rows.add(row(bridge, "mission.charge_basic_battery", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.CHARGE_BASIC_BATTERY_MISSION_ID,
                "Charge basic battery mission turns the AdapterCore energy cell item into stored machine power.",
                List.of("basicBatteryCharged", "storedEnergyCells")));
        rows.add(row(bridge, "mission.build_battery_bank", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.BUILD_BATTERY_BANK_MISSION_ID,
                "Build battery bank mission creates a stored-energy buffer before repair load.",
                List.of("batteryBankBuilt", "storedEnergyCells")));
        rows.add(row(bridge, "mission.build_thermal_burner", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.BUILD_THERMAL_BURNER_MISSION_ID,
                "Build thermal burner mission stabilizes the high-load repair route.",
                List.of("thermalBurnerBuilt", "thermalBurnerHeat")));
        rows.add(row(bridge, "mission.equip_gas_mask", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.EQUIP_GAS_MASK_MISSION_ID,
                "Equip gas mask mission proves the protected midgame route starts from AdapterCore item use.",
                List.of("gasMaskEquipped")));
        rows.add(row(bridge, "mission.find_schematic_fragment", EchoAshfallGameplayFeatureKind.ITEMS,
                EchoAdapterCoreStandaloneContentBridge.FIND_SCHEMATIC_FRAGMENT_MISSION_ID,
                "Find schematic fragment mission records blueprint discovery through the AdapterCore item catalog.",
                List.of("schematicFragmentFound", "schematicFragments")));
        rows.add(row(bridge, "mission.first_schematic", EchoAshfallGameplayFeatureKind.ITEMS,
                EchoAdapterCoreStandaloneContentBridge.FIRST_SCHEMATIC_MISSION_ID,
                "First schematic mission decodes the recovered fragment before factory construction.",
                List.of("firstSchematicDecoded", "schematicFragments")));
        rows.add(row(bridge, "mission.build_scrap_press", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.BUILD_SCRAP_PRESS_MISSION_ID,
                "Build scrap press mission places the first factory machine in the power route.",
                List.of("scrapPressBuilt")));
        rows.add(row(bridge, "mission.install_item_pipe", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.INSTALL_ITEM_PIPE_MISSION_ID,
                "Install item pipe mission tracks multiple factory routing segments.",
                List.of("itemPipeInstalled", "itemPipeSegments")));
        rows.add(row(bridge, "mission.build_factory_controller", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.BUILD_FACTORY_CONTROLLER_MISSION_ID,
                "Build factory controller mission creates the control point for upgraded power priority.",
                List.of("factoryControllerBuilt")));
        rows.add(row(bridge, "mission.build_research_lab", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.BUILD_RESEARCH_LAB_MISSION_ID,
                "Build research lab mission establishes the schematic research station.",
                List.of("researchLabBuilt")));
        rows.add(row(bridge, "mission.upgrade_power_cable", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.UPGRADE_POWER_CABLE_MISSION_ID,
                "Upgrade power cable mission requires reinforced segments for the overclocked load.",
                List.of("powerCableUpgraded", "upgradedPowerCableSegments")));
        rows.add(row(bridge, "mission.set_power_priority", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.SET_POWER_PRIORITY_MISSION_ID,
                "Set power priority mission records controller configuration before repair.",
                List.of("powerPrioritySet")));
        rows.add(row(bridge, "mission.overclock_machine", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.OVERCLOCK_MACHINE_MISSION_ID,
                "Overclock machine mission proves the factory route can supply extraction repair load.",
                List.of("machineOverclocked", "overclockHeat")));
        rows.add(row(bridge, "mission.fix_mask_filter", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.FIX_MASK_FILTER_MISSION_ID,
                "Fix mask filter mission converts a basic cartridge into AdapterCore safety charges.",
                List.of("basicFilterFixed", "basicFilterCharges")));
        rows.add(row(bridge, "mission.craft_advanced_filter", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.CRAFT_ADVANCED_FILTER_MISSION_ID,
                "Craft advanced filter mission upgrades expedition hazard mitigation through AdapterCore state.",
                List.of("advancedFilterCrafted", "advancedFilterCharges")));
        rows.add(row(bridge, "mission.build_thermal_array", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.BUILD_THERMAL_ARRAY_MISSION_ID,
                "Build thermal array mission unlocks cold-exposure recovery for the expedition route.",
                List.of("thermalArrayBuilt")));
        rows.add(row(bridge, "mission.warm_up_after_exposure", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.WARM_UP_AFTER_EXPOSURE_MISSION_ID,
                "Warm up after exposure mission records AdapterCore thermal recovery time.",
                List.of("warmedAfterExposure", "warmthRecoveredSeconds")));
        rows.add(row(bridge, "mission.build_atmospheric_scrubber", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.BUILD_ATMOSPHERIC_SCRUBBER_MISSION_ID,
                "Build atmospheric scrubber mission places the AdapterCore toxic ash mitigation block.",
                List.of("atmosphericScrubberBuilt")));
        rows.add(row(bridge, "mission.build_radiation_cleanser", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.BUILD_RADIATION_CLEANSER_MISSION_ID,
                "Build radiation cleanser mission places the AdapterCore exposure cleanse block.",
                List.of("radiationCleanserBuilt", "radiationCleanserCycles")));
        rows.add(row(bridge, "mission.build_field_med_bay", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                EchoAdapterCoreStandaloneContentBridge.BUILD_FIELD_MED_BAY_MISSION_ID,
                "Build field med bay mission places the AdapterCore field treatment block.",
                List.of("fieldMedBayBuilt")));
        rows.add(row(bridge, "mission.use_field_med_bay", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                EchoAdapterCoreStandaloneContentBridge.USE_FIELD_MED_BAY_MISSION_ID,
                "Use field med bay mission records AdapterCore treatment count and survival recovery.",
                List.of("fieldMedBayUsed", "fieldMedBayTreatments")));
        rows.add(row(bridge, "mission.build_filter_workbench", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.BUILD_FILTER_WORKBENCH_MISSION_ID,
                "Build filter workbench mission places the AdapterCore advanced expedition crafting station.",
                List.of("filterWorkbenchBuilt")));
        rows.add(row(bridge, "mission.build_ore_grinder", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.BUILD_ORE_GRINDER_MISSION_ID,
                "Build ore grinder mission places the AdapterCore powered substrate processor.",
                List.of("oreGrinderBuilt")));
        rows.add(row(bridge, "mission.find_dense_alloy", EchoAshfallGameplayFeatureKind.LOOT_SCAVENGING,
                EchoAdapterCoreStandaloneContentBridge.FIND_DENSE_ALLOY_MISSION_ID,
                "Find dense alloy mission records AdapterCore ore-grinder material output.",
                List.of("denseAlloyFound", "denseAlloyChunks")));
        rows.add(row(bridge, "mission.build_isotope_refiner", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.BUILD_ISOTOPE_REFINER_MISSION_ID,
                "Build isotope refiner mission places the AdapterCore powered catalyst processor.",
                List.of("isotopeRefinerBuilt")));
        rows.add(row(bridge, "mission.forge_alloy_weapon", EchoAshfallGameplayFeatureKind.TOOLS,
                EchoAdapterCoreStandaloneContentBridge.FORGE_ALLOY_WEAPON_MISSION_ID,
                "Forge alloy weapon mission converts dense alloy into AdapterCore tool progression.",
                List.of("alloyWeaponForged", "alloyWeaponDurability", "denseAlloyChunks")));
        rows.add(row(bridge, "mission.equip_alloy_kit", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.EQUIP_ALLOY_KIT_MISSION_ID,
                "Equip alloy kit mission records AdapterCore advanced expedition armor readiness.",
                List.of("alloyKitEquipped")));
        rows.add(row(bridge, "mission.activate_relay_station", EchoAshfallGameplayFeatureKind.MISSIONS,
                EchoAdapterCoreStandaloneContentBridge.ACTIVATE_RELAY_STATION_MISSION_ID,
                "Activate relay station mission records AdapterCore relay signal strength.",
                List.of("relayStationActivated", "relaySignalStrength")));
        rows.add(row(bridge, "mission.build_scout_drone", EchoAshfallGameplayFeatureKind.MISSIONS,
                EchoAdapterCoreStandaloneContentBridge.BUILD_SCOUT_DRONE_MISSION_ID,
                "Build scout drone mission records AdapterCore scout drone entity readiness.",
                List.of("scoutDroneBuilt", "scoutDroneRangeMeters")));
        rows.add(row(bridge, "mission.build_filter_workbench", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.BUILD_FILTER_WORKBENCH_MISSION_ID,
                "Build filter workbench mission starts the AdapterCore advanced expedition chain.",
                List.of("filterWorkbenchBuilt", "basicFilterCharges")));
        rows.add(row(bridge, "mission.build_ore_grinder", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.BUILD_ORE_GRINDER_MISSION_ID,
                "Build ore grinder mission maps powered dense alloy recovery into AdapterCore state.",
                List.of("oreGrinderBuilt", "denseAlloyChunks")));
        rows.add(row(bridge, "mission.find_dense_alloy", EchoAshfallGameplayFeatureKind.ITEMS,
                EchoAdapterCoreStandaloneContentBridge.FIND_DENSE_ALLOY_MISSION_ID,
                "Find dense alloy mission records AdapterCore alloy material discovery.",
                List.of("denseAlloyFound", "denseAlloyChunks")));
        rows.add(row(bridge, "mission.build_isotope_refiner", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.BUILD_ISOTOPE_REFINER_MISSION_ID,
                "Build isotope refiner mission links advanced crafting to radiation-cleanser readiness.",
                List.of("isotopeRefinerBuilt", "radiationCleanserCycles")));
        rows.add(row(bridge, "mission.forge_alloy_weapon", EchoAshfallGameplayFeatureKind.TOOLS,
                EchoAdapterCoreStandaloneContentBridge.FORGE_ALLOY_WEAPON_MISSION_ID,
                "Forge alloy weapon mission consumes dense alloy into AdapterCore weapon durability state.",
                List.of("alloyWeaponForged", "alloyWeaponDurability", "denseAlloyChunks")));
        rows.add(row(bridge, "mission.equip_alloy_kit", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                EchoAdapterCoreStandaloneContentBridge.EQUIP_ALLOY_KIT_MISSION_ID,
                "Equip alloy kit mission records advanced armor readiness before relay activation.",
                List.of("alloyKitEquipped")));
        rows.add(row(bridge, "mission.activate_relay_station", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreStandaloneContentBridge.ACTIVATE_RELAY_STATION_MISSION_ID,
                "Activate relay station mission records scanner-lens relay signal strength.",
                List.of("relayStationActivated", "relaySignalStrength")));
        rows.add(row(bridge, "mission.build_scout_drone", EchoAshfallGameplayFeatureKind.TOOLS,
                EchoAdapterCoreStandaloneContentBridge.BUILD_SCOUT_DRONE_MISSION_ID,
                "Build scout drone mission records route survey range before extraction.",
                List.of("scoutDroneBuilt", "scoutDroneRangeMeters")));
        rows.add(row(bridge, "mission.use_rad_away", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                EchoAdapterCoreStandaloneContentBridge.USE_RAD_AWAY_MISSION_ID,
                "Use rad away mission records anti-radiation recovery through AdapterCore field recovery.",
                List.of("radAwayUsed", "radAwayDoses", "ashExposure", "toxicAshExposureSeconds")));
        rows.add(row(bridge, "mission.use_stim_pack", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                EchoAdapterCoreStandaloneContentBridge.USE_STIM_PACK_MISSION_ID,
                "Use stim pack mission records emergency healing through AdapterCore field recovery.",
                List.of("stimPackUsed", "stimPackDoses", "playerHealth")));
        rows.add(row(bridge, "mission.use_hand_warmer", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.USE_HAND_WARMER_MISSION_ID,
                "Use hand warmer mission records portable warmth recovery through AdapterCore state.",
                List.of("handWarmerUsed", "handWarmerCharges", "warmthRecoveredSeconds")));
        rows.add(row(bridge, "mission.install_thermal_liner", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                EchoAdapterCoreStandaloneContentBridge.INSTALL_THERMAL_LINER_MISSION_ID,
                "Install thermal liner mission records persistent warmth buffer state.",
                List.of("thermalLinerInstalled", "thermalLinerWarmthSeconds", "warmthRecoveredSeconds")));
        rows.add(row(bridge, "mission.place_return_beacon", EchoAshfallGameplayFeatureKind.EXTRACTION_LOGIC,
                EchoAdapterCoreStandaloneContentBridge.PLACE_RETURN_BEACON_MISSION_ID,
                "Place return beacon mission records fallback extraction signal state.",
                List.of("returnBeaconPlaced", "returnBeaconSignalStrength")));
        rows.add(row(bridge, "mission.bind_return_keystone", EchoAshfallGameplayFeatureKind.EXTRACTION_LOGIC,
                EchoAdapterCoreStandaloneContentBridge.BIND_RETURN_KEYSTONE_MISSION_ID,
                "Bind return keystone mission records fallback extraction charge state.",
                List.of("returnKeystoneBound", "returnKeystoneCharges")));
        rows.add(row(bridge, "save.live_mission_state", EchoAshfallGameplayFeatureKind.SAVES_PROGRESSION,
                "echoashfallprotocol:save/live_mission_state",
                "Versioned save/load contract for Ashfall mission, survival, shelter, power, and extraction state.",
                REQUIRED_SAVE_FIELDS));
        rows.add(row(bridge, "component.stored_energy", EchoAshfallGameplayFeatureKind.SAVES_PROGRESSION,
                EchoAdapterCoreStandaloneContentBridge.STORED_ENERGY_COMPONENT_ID,
                "Persistent item energy component maps NeoForge item storage into AdapterCore save and machine-power state.",
                List.of("storedEnergyCells")));
        rows.add(row(bridge, "component.ashfall_tooltip", EchoAshfallGameplayFeatureKind.SAVES_PROGRESSION,
                EchoAdapterCoreStandaloneContentBridge.ASHFALL_TOOLTIP_COMPONENT_ID,
                "Persistent tooltip component has a portable AdapterCore component id for Native and Standalone item metadata."));
        rows.add(row(bridge, "status_effect.alliance", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                EchoAdapterCoreStandaloneContentBridge.ALLIANCE_EFFECT_ID,
                "Nexus RESTORE alliance effect is represented as an AdapterCore player status target for Native and Standalone healing/status behavior."));
        rows.add(row(bridge, "network.live_state_sync", EchoAshfallGameplayFeatureKind.SAVES_PROGRESSION,
                "echoashfallprotocol:network/live_state_sync",
                "Local/remote sync hook for live mission and inventory state."));
        rows.add(row(bridge, "command.ashfall_status", EchoAshfallGameplayFeatureKind.MISSIONS,
                "echoashfallprotocol:command/ashfall_status",
                "Status/debug command target for AdapterCore readiness."));
        rows.add(row(bridge, "world.region.crash_site", EchoAshfallGameplayFeatureKind.WORLD_REGIONS,
                "echoashfallprotocol:world_region/crash_zone_wasteland",
                "Crash-site region contract."));
        rows.add(row(bridge, "world.hazard.toxic_ash", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.TOXIC_ASH_HAZARD_ID,
                "Toxic ash exposure hazard contract."));
        rows.add(row(bridge, "world.hazard.hot_ash", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.HOT_ASH_HAZARD_ID,
                "Hot ash contact hazard contract for scorched terrain."));
        rows.add(row(bridge, "world.hazard.unstable_ground", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.UNSTABLE_GROUND_HAZARD_ID,
                "Unstable ground hazard contract for collapsed debris traversal."));
        rows.add(row(bridge, "world.hazard.electrical_discharge", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.ELECTRICAL_DISCHARGE_HAZARD_ID,
                "PowerGrid electrical discharge hazard contract around damaged nodes."));
        rows.add(row(bridge, "world.hazard.extraction_storm", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreStandaloneContentBridge.EXTRACTION_STORM_HAZARD_ID,
                "Extraction storm event hazard contract."));
        rows.add(data("world.regions.datapack", EchoAshfallGameplayFeatureKind.WORLD_REGIONS,
                EchoAdapterCoreDomain.WORLDGEN,
                "data/echoashfallprotocol/echoworldcore/world_regions/",
                "Ashfall WorldCore region definitions are data-driven and loaded from every namespace."));
        rows.add(data("world.hazards.datapack", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreDomain.WORLDGEN,
                "data/echoashfallprotocol/echoworldcore/world_hazards/",
                "Ashfall-owned hazard definitions live under echoashfallprotocol."));
        rows.add(data("missioncore.missions.datapack", EchoAshfallGameplayFeatureKind.MISSIONS,
                EchoAdapterCoreDomain.MISSIONS,
                "data/echoashfallprotocol/missioncore/missions/",
                "MissionCore JSON supplies the NeoForge mission catalog used by terminal flows."));
        rows.add(data("loot.tables.datapack", EchoAshfallGameplayFeatureKind.LOOT_SCAVENGING,
                EchoAdapterCoreDomain.LOOT,
                "data/echoashfallprotocol/loot_table/",
                "NeoForge loot tables and standalone scavenge contracts share stable content IDs."));
        rows.add(data("block.break.place.world_edits", EchoAshfallGameplayFeatureKind.BLOCK_INTERACTION,
                EchoAdapterCoreDomain.BLOCKS,
                "playable/world-edits.tsv",
                "Broken and placed block IDs are persisted as AdapterCore live voxel IDs."));
        rows.add(data("tool.scrap_knife.live_state", EchoAshfallGameplayFeatureKind.TOOLS,
                EchoAdapterCoreDomain.SAVES,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Scrap metal recovery, scrap knife crafting, tool mining count, and durability are serialized contract fields.",
                List.of("scrapKnifeCrafted", "toolAssistedMining",
                        "scrapMetal", "scrapKnifeDurability", "toolMiningBlocksBroken")));
        rows.add(data("workshop.field_recycler.live_state", EchoAshfallGameplayFeatureKind.ITEMS,
                EchoAdapterCoreDomain.SAVES,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Hand recycler, machine casing, field kit, scrap wire, and scrap circuit progression are serialized contract fields.",
                List.of("handRecyclerBuilt", "machineCasingMade", "wastelandFieldKitAssembled",
                        "scrapWire", "scrapCircuit", "machineCasings")));
        rows.add(data("power.field_microgrid.live_state", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreDomain.SAVES,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Micro generator, power cable, energy meter, generated power, segment count, and readings are serialized contract fields.",
                List.of("microGeneratorBuilt", "powerCableRouted", "energyMeterInstalled",
                        "fieldPowerGenerated", "powerCableSegments", "energyMeterReadings")));
        rows.add(data("power.machine_route.live_state", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreDomain.SAVES,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Scrap dynamo, energy cell charge, battery bank, thermal burner, stored cells, and heat are serialized contract fields.",
                List.of("scrapDynamoBuilt", "basicBatteryCharged", "batteryBankBuilt", "thermalBurnerBuilt",
                        "machinePowerGenerated", "storedEnergyCells", "thermalBurnerHeat")));
        rows.add(data("progression.midgame_factory.live_state", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreDomain.SAVES,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Gas mask, schematic, factory machines, reinforced cable, priority, overclock, and segment counters are serialized contract fields.",
                List.of("gasMaskEquipped", "schematicFragmentFound", "firstSchematicDecoded",
                        "scrapPressBuilt", "itemPipeInstalled", "factoryControllerBuilt", "researchLabBuilt",
                        "powerCableUpgraded", "powerPrioritySet", "machineOverclocked",
                        "schematicFragments", "itemPipeSegments", "upgradedPowerCableSegments", "overclockHeat")));
        rows.add(data("safety.expedition_hazards.live_state", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreDomain.SAVES,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Filter repair, advanced filtration, warmth recovery, scrubber, cleanser, and med bay treatment state are serialized contract fields.",
                List.of("basicFilterFixed", "advancedFilterCrafted", "thermalArrayBuilt", "warmedAfterExposure",
                        "atmosphericScrubberBuilt", "radiationCleanserBuilt", "fieldMedBayBuilt", "fieldMedBayUsed",
                        "basicFilterCharges", "advancedFilterCharges", "warmthRecoveredSeconds",
                        "radiationCleanserCycles", "fieldMedBayTreatments")));
        rows.add(data("progression.advanced_expedition.live_state", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreDomain.SAVES,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Filter workbench, ore grinder, dense alloy, isotope refiner, alloy kit, relay, and scout drone state are serialized contract fields.",
                List.of("filterWorkbenchBuilt", "oreGrinderBuilt", "denseAlloyFound", "isotopeRefinerBuilt",
                        "alloyWeaponForged", "alloyKitEquipped", "relayStationActivated", "scoutDroneBuilt",
                        "denseAlloyChunks", "alloyWeaponDurability", "relaySignalStrength", "scoutDroneRangeMeters")));
        rows.add(data("recovery.field_recovery.live_state", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                EchoAdapterCoreDomain.SAVES,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Field recovery consumables, warmth gear, return beacon, and keystone counters are serialized contract fields.",
                List.of("radAwayUsed", "stimPackUsed", "handWarmerUsed", "thermalLinerInstalled",
                        "returnBeaconPlaced", "returnKeystoneBound", "radAwayDoses", "stimPackDoses",
                        "handWarmerCharges", "thermalLinerWarmthSeconds", "returnBeaconSignalStrength",
                        "returnKeystoneCharges")));
        rows.add(data("survival.stats.live_state", EchoAshfallGameplayFeatureKind.SURVIVAL_STATS,
                EchoAdapterCoreDomain.SAVES,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Hydration, hunger, health, ash exposure, recovery counters, deprivation pulses, hazard counters, and survival time are serialized contract fields.",
                List.of("playerHealth", "hydration", "hunger", "ashExposure",
                        "hydrationRecovered", "hungerRecovered",
                        "dehydrationDamagePulses", "starvationDamagePulses",
                        "toxicAshExposureSeconds", "hotAshExposureSeconds",
                        "unstableGroundStrikes", "electricalDischargeHits",
                        "extractionStormExposureSeconds", "survivalSeconds", "scavengedLootKeys")));
        rows.add(data("survival.water_loop.live_state", EchoAshfallGameplayFeatureKind.FLUIDS_CONSUMABLES,
                EchoAdapterCoreDomain.SAVES,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Rain collector, dirty water, purifier cycles, clean-water reserve, forage, and ration reserve state are serialized contract fields.",
                List.of("rainCollectorBuilt", "waterPurifierBuilt", "emergencyWaterLoopSecured",
                        "foragedFood", "cleanWaterStockpiled", "rationsStockpiled",
                        "dirtyWaterBottles", "filteredWaterBottles", "cleanWaterStockpile",
                        "foodRationStockpile", "rainCollectorCollections",
                        "waterPurifierCycles", "foragedFoodBundles")));
        rows.add(data("shelter.profile.live_state", EchoAshfallGameplayFeatureKind.SHELTER_LOGIC,
                EchoAdapterCoreDomain.SAVES,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Shelter radius, rest recovery, and storm integrity pressure come from the AdapterCore shelter profile.",
                List.of("shelterBuilt", "shelterIntegrity", "shelterRestSeconds",
                        "shelterStormDamage", "shelterX", "shelterY", "shelterZ")));
        rows.add(data("hazards.adaptercore_rule_table", EchoAshfallGameplayFeatureKind.HAZARDS,
                EchoAdapterCoreDomain.WORLDGEN,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Standalone hazard application resolves contact and event pressure through AdapterCore hazard rules.",
                List.of("toxicAshExposureSeconds", "hotAshExposureSeconds",
                        "unstableGroundStrikes", "electricalDischargeHits",
                        "extractionStormExposureSeconds")));
        rows.add(data("power.terminal.repair_flow", EchoAshfallGameplayFeatureKind.POWER_REPAIR,
                EchoAdapterCoreDomain.MISSIONS,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Damaged node discovery, required repair kit, repair state, restored power flag, and terminal response share mission/save fields.",
                List.of("terminalOnline", "cacheRecovered", "hazardCleared", "repairKits",
                        "microGeneratorBuilt", "powerCableRouted", "energyMeterInstalled",
                        "scrapDynamoBuilt", "basicBatteryCharged", "batteryBankBuilt", "thermalBurnerBuilt",
                        "gasMaskEquipped", "schematicFragmentFound", "firstSchematicDecoded",
                        "scrapPressBuilt", "itemPipeInstalled", "factoryControllerBuilt", "researchLabBuilt",
                        "powerCableUpgraded", "powerPrioritySet", "machineOverclocked",
                        "basicFilterFixed", "advancedFilterCrafted", "thermalArrayBuilt", "warmedAfterExposure",
                        "atmosphericScrubberBuilt", "radiationCleanserBuilt", "fieldMedBayBuilt", "fieldMedBayUsed",
                        "powerNodeDiscovered", "powerRepairStarted", "powerRebootSeconds",
                        "powerTerminalConfirmed", "powerRepaired")));
        rows.add(data("extraction.trigger.live_state", EchoAshfallGameplayFeatureKind.EXTRACTION_LOGIC,
                EchoAdapterCoreDomain.MISSIONS,
                LIVE_MISSION_STATE_CONTRACT_ID,
                "Extraction is mission completion state, not a standalone-only end flag.",
                List.of("powerRepaired", "extractionArmed", "extractionCountdownSeconds", "extracted")));
        return List.copyOf(rows);
    }

    public static List<EchoAshfallGameplayFeatureContract> blockingContracts(
            List<EchoAshfallGameplayFeatureContract> contracts
    ) {
        Objects.requireNonNull(contracts, "contracts");
        return contracts.stream()
                .filter(contract -> contract.blocksParity() || !contract.contractEvidenceComplete())
                .toList();
    }

    public static boolean parityReady(List<EchoAshfallGameplayFeatureContract> contracts) {
        return blockingContracts(contracts).isEmpty()
                && missingRequiredAdapterCoreDomains(contracts).isEmpty()
                && contracts.stream().allMatch(EchoAshfallGameplayFeatureContract::standaloneAliasRegisteredThroughAdapterCore);
    }

    public static List<EchoAdapterCoreDomain> coveredRequiredAdapterCoreDomains(
            List<EchoAshfallGameplayFeatureContract> contracts
    ) {
        Objects.requireNonNull(contracts, "contracts");
        Set<EchoAdapterCoreDomain> required = EnumSet.copyOf(EchoAdapterCoreContractLock.requiredBetaDomains());
        Set<EchoAdapterCoreDomain> covered = EnumSet.noneOf(EchoAdapterCoreDomain.class);
        for (EchoAshfallGameplayFeatureContract contract : contracts) {
            if (contract.adapterDomain() != null
                    && required.contains(contract.adapterDomain())
                    && contract.contractEvidenceComplete()) {
                covered.add(contract.adapterDomain());
            }
        }
        return covered.stream().sorted().toList();
    }

    public static List<EchoAdapterCoreDomain> missingRequiredAdapterCoreDomains(
            List<EchoAshfallGameplayFeatureContract> contracts
    ) {
        Objects.requireNonNull(contracts, "contracts");
        Set<EchoAdapterCoreDomain> covered = EnumSet.noneOf(EchoAdapterCoreDomain.class);
        covered.addAll(coveredRequiredAdapterCoreDomains(contracts));
        return EchoAdapterCoreContractLock.requiredBetaDomains().stream()
                .filter(domain -> !covered.contains(domain))
                .toList();
    }

    private static EchoAshfallGameplayFeatureContract row(
            EchoAdapterCoreStandaloneContentBridge bridge,
            String featureId,
            EchoAshfallGameplayFeatureKind kind,
            String contentId,
            String note
    ) {
        return row(bridge, featureId, kind, contentId, note, List.of());
    }

    private static EchoAshfallGameplayFeatureContract row(
            EchoAdapterCoreStandaloneContentBridge bridge,
            String featureId,
            EchoAshfallGameplayFeatureKind kind,
            String contentId,
            String note,
            List<String> saveFields
    ) {
        EchoAdapterCoreRegistryEntry entry = bridge.registry().requireContentId(contentId);
        EchoAdapterCoreContentBinding binding = entry.binding();
        return new EchoAshfallGameplayFeatureContract(
                featureId,
                kind,
                EchoAshfallGameplayFeatureStatus.ADAPTERCORE_BACKED,
                entry.domain(),
                binding.adapterKey(),
                binding.neoForgeId(),
                binding.nativeLoaderId(),
                binding.standaloneRuntimeId(),
                binding.contentId(),
                "",
                saveFields,
                note
        );
    }

    private static EchoAshfallGameplayFeatureContract data(
            String featureId,
            EchoAshfallGameplayFeatureKind kind,
            EchoAdapterCoreDomain domain,
            String dataSource,
            String note
    ) {
        return data(featureId, kind, domain, dataSource, note, List.of());
    }

    private static EchoAshfallGameplayFeatureContract data(
            String featureId,
            EchoAshfallGameplayFeatureKind kind,
            EchoAdapterCoreDomain domain,
            String dataSource,
            String note,
            List<String> saveFields
    ) {
        return new EchoAshfallGameplayFeatureContract(
                featureId,
                kind,
                EchoAshfallGameplayFeatureStatus.DATA_DRIVEN_SHARED,
                domain,
                "",
                "",
                "",
                "",
                "",
                dataSource,
                saveFields,
                note
        );
    }
}
