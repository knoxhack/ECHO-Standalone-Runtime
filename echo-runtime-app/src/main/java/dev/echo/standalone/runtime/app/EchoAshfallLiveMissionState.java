package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreHazardRule;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreHazardTable;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreAdvancedExpeditionProfile;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreExpeditionSafetyProfile;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreFieldRecoveryProfile;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreFieldPowerProfile;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreFieldWorkshopProfile;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreMachinePowerProfile;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreMidgameProgressionProfile;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreScavengeReward;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreScavengeTable;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreShelterProfile;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreSurvivalProfile;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreToolProfile;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreWaterLoopProfile;
import dev.echo.standalone.runtime.compat.EchoAshfallGameplayContracts;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelHit;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public final class EchoAshfallLiveMissionState {
    public static final String ADAPTERCORE_CONTRACT_ID =
            EchoAshfallGameplayContracts.LIVE_MISSION_STATE_CONTRACT_ID;

    private static final double WALK_HYDRATION_PER_MINUTE = 1.25D;
    private static final double SPRINT_HYDRATION_PER_MINUTE = 2.10D;
    private static final double WALK_HUNGER_PER_MINUTE = 0.85D;
    private static final double SPRINT_HUNGER_PER_MINUTE = 1.45D;
    private static final double POWER_REBOOT_REQUIRED_SECONDS = 8.0D;
    private static final double EXTRACTION_COUNTDOWN_REQUIRED_SECONDS = 12.0D;
    private final ArrayList<String> feed = new ArrayList<>();
    private boolean beaconTracked = true;
    private boolean fieldManualRead;
    private boolean shelterBuilt;
    private boolean scannerUsed;
    private boolean terminalOnline;
    private boolean waterUsed;
    private boolean foodUsed;
    private boolean crossedAsh;
    private boolean hazardCleared;
    private boolean scavengedSupplies;
    private boolean cacheRecovered;
    private boolean powerNodeDiscovered;
    private boolean powerRepairStarted;
    private boolean powerTerminalConfirmed;
    private boolean powerRepaired;
    private boolean extractionArmed;
    private boolean extracted;
    private boolean rainCollectorBuilt;
    private boolean waterPurifierBuilt;
    private boolean emergencyWaterLoopSecured;
    private boolean foragedFood;
    private boolean cleanWaterStockpiled;
    private boolean rationsStockpiled;
    private boolean scrapKnifeCrafted;
    private boolean toolAssistedMining;
    private boolean handRecyclerBuilt;
    private boolean machineCasingMade;
    private boolean wastelandFieldKitAssembled;
    private boolean microGeneratorBuilt;
    private boolean powerCableRouted;
    private boolean energyMeterInstalled;
    private boolean scrapDynamoBuilt;
    private boolean basicBatteryCharged;
    private boolean batteryBankBuilt;
    private boolean thermalBurnerBuilt;
    private boolean gasMaskEquipped;
    private boolean schematicFragmentFound;
    private boolean firstSchematicDecoded;
    private boolean scrapPressBuilt;
    private boolean itemPipeInstalled;
    private boolean factoryControllerBuilt;
    private boolean researchLabBuilt;
    private boolean powerCableUpgraded;
    private boolean powerPrioritySet;
    private boolean machineOverclocked;
    private boolean basicFilterFixed;
    private boolean advancedFilterCrafted;
    private boolean thermalArrayBuilt;
    private boolean warmedAfterExposure;
    private boolean atmosphericScrubberBuilt;
    private boolean radiationCleanserBuilt;
    private boolean fieldMedBayBuilt;
    private boolean fieldMedBayUsed;
    private boolean filterWorkbenchBuilt;
    private boolean oreGrinderBuilt;
    private boolean denseAlloyFound;
    private boolean isotopeRefinerBuilt;
    private boolean alloyWeaponForged;
    private boolean alloyKitEquipped;
    private boolean relayStationActivated;
    private boolean scoutDroneBuilt;
    private boolean radAwayUsed;
    private boolean stimPackUsed;
    private boolean handWarmerUsed;
    private boolean thermalLinerInstalled;
    private boolean returnBeaconPlaced;
    private boolean returnKeystoneBound;
    private int waterRations = 2;
    private int foodRations = 2;
    private int repairKits;
    private int scavengedSupplyCaches;
    private int scrapMetal;
    private int scrapWire;
    private int scrapCircuit;
    private int machineCasings;
    private int fieldPowerGenerated;
    private int powerCableSegments;
    private int energyMeterReadings;
    private int machinePowerGenerated;
    private int storedEnergyCells;
    private int thermalBurnerHeat;
    private int schematicFragments;
    private int itemPipeSegments;
    private int upgradedPowerCableSegments;
    private int overclockHeat;
    private int basicFilterCharges;
    private int advancedFilterCharges;
    private int warmthRecoveredSeconds;
    private int radiationCleanserCycles;
    private int fieldMedBayTreatments;
    private int denseAlloyChunks;
    private int alloyWeaponDurability;
    private int relaySignalStrength;
    private int scoutDroneRangeMeters;
    private int radAwayDoses;
    private int stimPackDoses;
    private int handWarmerCharges;
    private int thermalLinerWarmthSeconds;
    private int returnBeaconSignalStrength;
    private int returnKeystoneCharges;
    private int scrapKnifeDurability;
    private int toolMiningBlocksBroken;
    private int dirtyWaterBottles;
    private int filteredWaterBottles;
    private int cleanWaterStockpile;
    private int foodRationStockpile;
    private int rainCollectorCollections;
    private int waterPurifierCycles;
    private int foragedFoodBundles;
    private final LinkedHashSet<String> scavengedLootKeys = new LinkedHashSet<>();
    private int playerHealth = 100;
    private double hydration = 72.0D;
    private double hunger = 64.0D;
    private double ashExposure;
    private double hydrationRecovered;
    private double hungerRecovered;
    private int dehydrationDamagePulses;
    private int starvationDamagePulses;
    private double toxicAshExposureSeconds;
    private double hotAshExposureSeconds;
    private int unstableGroundStrikes;
    private int electricalDischargeHits;
    private double extractionStormExposureSeconds;
    private double shelterIntegrity;
    private double shelterRestSeconds;
    private double shelterStormDamage;
    private int shelterX;
    private int shelterY;
    private int shelterZ;
    private double powerRebootSeconds;
    private double extractionCountdownSeconds;
    private double survivalSeconds;
    private String lastMessage = "mission started: place shelter and scan the crash site";

    public EchoAshfallLiveMissionState() {
        addFeed("Crash pod beacon acquired");
    }

    public String adapterCoreContractId() {
        return ADAPTERCORE_CONTRACT_ID;
    }

    public static EchoAshfallLiveMissionState restored(
            boolean shelterBuilt,
            boolean scannerUsed,
            boolean terminalOnline,
            boolean waterUsed,
            boolean foodUsed,
            boolean crossedAsh,
            boolean hazardCleared,
            boolean cacheRecovered,
            boolean powerRepaired,
            boolean extracted,
            int waterRations,
            int foodRations,
            int repairKits,
            int playerHealth,
            double hydration,
            double hunger,
            double ashExposure,
            String lastMessage
    ) {
        return restored(
                shelterBuilt,
                scannerUsed,
                terminalOnline,
                waterUsed,
                foodUsed,
                crossedAsh,
                hazardCleared,
                false,
                cacheRecovered,
                powerRepaired,
                extracted,
                waterRations,
                foodRations,
                repairKits,
                playerHealth,
                hydration,
                hunger,
                ashExposure,
                lastMessage
        );
    }

    static EchoAshfallLiveMissionState restored(
            boolean shelterBuilt,
            boolean scannerUsed,
            boolean terminalOnline,
            boolean waterUsed,
            boolean foodUsed,
            boolean crossedAsh,
            boolean hazardCleared,
            boolean cacheRecovered,
            boolean powerRepaired,
            boolean extracted,
            int waterRations,
            int foodRations,
            int repairKits,
            int playerHealth,
            double hydration,
            double hunger,
            double ashExposure,
            int shelterX,
            int shelterY,
            int shelterZ,
            double survivalSeconds,
            String lastMessage
    ) {
        return restored(
                shelterBuilt,
                scannerUsed,
                terminalOnline,
                waterUsed,
                foodUsed,
                crossedAsh,
                hazardCleared,
                false,
                cacheRecovered,
                powerRepaired,
                extracted,
                waterRations,
                foodRations,
                repairKits,
                0,
                playerHealth,
                hydration,
                hunger,
                ashExposure,
                shelterX,
                shelterY,
                shelterZ,
                survivalSeconds,
                lastMessage
        );
    }

    static EchoAshfallLiveMissionState restored(
            boolean shelterBuilt,
            boolean scannerUsed,
            boolean terminalOnline,
            boolean waterUsed,
            boolean foodUsed,
            boolean crossedAsh,
            boolean hazardCleared,
            boolean scavengedSupplies,
            boolean cacheRecovered,
            boolean powerRepaired,
            boolean extracted,
            int waterRations,
            int foodRations,
            int repairKits,
            int playerHealth,
            double hydration,
            double hunger,
            double ashExposure,
            String lastMessage
    ) {
        return restored(
                shelterBuilt,
                scannerUsed,
                terminalOnline,
                waterUsed,
                foodUsed,
                crossedAsh,
                hazardCleared,
                scavengedSupplies,
                cacheRecovered,
                powerRepaired,
                extracted,
                waterRations,
                foodRations,
                repairKits,
                0,
                playerHealth,
                hydration,
                hunger,
                ashExposure,
                0,
                0,
                0,
                0.0D,
                lastMessage
        );
    }

    static EchoAshfallLiveMissionState restored(
            boolean shelterBuilt,
            boolean scannerUsed,
            boolean terminalOnline,
            boolean waterUsed,
            boolean foodUsed,
            boolean crossedAsh,
            boolean hazardCleared,
            boolean scavengedSupplies,
            boolean cacheRecovered,
            boolean powerRepaired,
            boolean extracted,
            int waterRations,
            int foodRations,
            int repairKits,
            int scavengedSupplyCaches,
            int playerHealth,
            double hydration,
            double hunger,
            double ashExposure,
            int shelterX,
            int shelterY,
            int shelterZ,
            double survivalSeconds,
            String lastMessage
    ) {
        return restored(
                shelterBuilt,
                scannerUsed,
                terminalOnline,
                waterUsed,
                foodUsed,
                crossedAsh,
                hazardCleared,
                scavengedSupplies,
                cacheRecovered,
                powerRepaired,
                extracted,
                waterRations,
                foodRations,
                repairKits,
                scavengedSupplyCaches,
                playerHealth,
                hydration,
                hunger,
                ashExposure,
                shelterX,
                shelterY,
                shelterZ,
                survivalSeconds,
                lastMessage,
                List.of()
        );
    }

    static EchoAshfallLiveMissionState restored(
            boolean shelterBuilt,
            boolean scannerUsed,
            boolean terminalOnline,
            boolean waterUsed,
            boolean foodUsed,
            boolean crossedAsh,
            boolean hazardCleared,
            boolean scavengedSupplies,
            boolean cacheRecovered,
            boolean powerRepaired,
            boolean extracted,
            int waterRations,
            int foodRations,
            int repairKits,
            int scavengedSupplyCaches,
            int playerHealth,
            double hydration,
            double hunger,
            double ashExposure,
            int shelterX,
            int shelterY,
            int shelterZ,
            double survivalSeconds,
            String lastMessage,
            List<String> scavengedLootKeys
    ) {
        return restored(
                shelterBuilt,
                scannerUsed,
                terminalOnline,
                waterUsed,
                foodUsed,
                crossedAsh,
                hazardCleared,
                scavengedSupplies,
                cacheRecovered,
                powerRepaired,
                powerRepaired,
                powerRepaired,
                powerRepaired,
                extracted,
                extracted,
                waterRations,
                foodRations,
                repairKits,
                scavengedSupplyCaches,
                playerHealth,
                hydration,
                hunger,
                ashExposure,
                shelterX,
                shelterY,
                shelterZ,
                powerRepaired ? POWER_REBOOT_REQUIRED_SECONDS : 0.0D,
                extracted ? EXTRACTION_COUNTDOWN_REQUIRED_SECONDS : 0.0D,
                survivalSeconds,
                lastMessage,
                scavengedLootKeys
        );
    }

    static EchoAshfallLiveMissionState restored(
            boolean shelterBuilt,
            boolean scannerUsed,
            boolean terminalOnline,
            boolean waterUsed,
            boolean foodUsed,
            boolean crossedAsh,
            boolean hazardCleared,
            boolean scavengedSupplies,
            boolean cacheRecovered,
            boolean powerNodeDiscovered,
            boolean powerRepairStarted,
            boolean powerTerminalConfirmed,
            boolean powerRepaired,
            boolean extractionArmed,
            boolean extracted,
            int waterRations,
            int foodRations,
            int repairKits,
            int scavengedSupplyCaches,
            int playerHealth,
            double hydration,
            double hunger,
            double ashExposure,
            int shelterX,
            int shelterY,
            int shelterZ,
            double powerRebootSeconds,
            double extractionCountdownSeconds,
            double survivalSeconds,
            String lastMessage,
            List<String> scavengedLootKeys
    ) {
        return restored(
                shelterBuilt,
                scannerUsed,
                terminalOnline,
                waterUsed,
                foodUsed,
                crossedAsh,
                hazardCleared,
                scavengedSupplies,
                cacheRecovered,
                powerNodeDiscovered,
                powerRepairStarted,
                powerTerminalConfirmed,
                powerRepaired,
                extractionArmed,
                extracted,
                waterRations,
                foodRations,
                repairKits,
                scavengedSupplyCaches,
                playerHealth,
                hydration,
                hunger,
                ashExposure,
                0.0D,
                0.0D,
                0,
                0,
                0.0D,
                shelterBuilt ? 100.0D : 0.0D,
                0.0D,
                0.0D,
                shelterX,
                shelterY,
                shelterZ,
                powerRebootSeconds,
                extractionCountdownSeconds,
                survivalSeconds,
                lastMessage,
                scavengedLootKeys
        );
    }

    static EchoAshfallLiveMissionState restored(
            boolean shelterBuilt,
            boolean scannerUsed,
            boolean terminalOnline,
            boolean waterUsed,
            boolean foodUsed,
            boolean crossedAsh,
            boolean hazardCleared,
            boolean scavengedSupplies,
            boolean cacheRecovered,
            boolean powerNodeDiscovered,
            boolean powerRepairStarted,
            boolean powerTerminalConfirmed,
            boolean powerRepaired,
            boolean extractionArmed,
            boolean extracted,
            int waterRations,
            int foodRations,
            int repairKits,
            int scavengedSupplyCaches,
            int playerHealth,
            double hydration,
            double hunger,
            double ashExposure,
            double toxicAshExposureSeconds,
            double hotAshExposureSeconds,
            int unstableGroundStrikes,
            int electricalDischargeHits,
            double extractionStormExposureSeconds,
            double shelterIntegrity,
            double shelterRestSeconds,
            double shelterStormDamage,
            int shelterX,
            int shelterY,
            int shelterZ,
            double powerRebootSeconds,
            double extractionCountdownSeconds,
            double survivalSeconds,
            String lastMessage,
            List<String> scavengedLootKeys
    ) {
        return restoredFull(
                shelterBuilt,
                scannerUsed,
                terminalOnline,
                waterUsed,
                foodUsed,
                crossedAsh,
                hazardCleared,
                scavengedSupplies,
                cacheRecovered,
                powerNodeDiscovered,
                powerRepairStarted,
                powerTerminalConfirmed,
                powerRepaired,
                extractionArmed,
                extracted,
                waterRations,
                foodRations,
                repairKits,
                scavengedSupplyCaches,
                playerHealth,
                hydration,
                hunger,
                ashExposure,
                0.0D,
                0.0D,
                0,
                0,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                toxicAshExposureSeconds,
                hotAshExposureSeconds,
                unstableGroundStrikes,
                electricalDischargeHits,
                extractionStormExposureSeconds,
                shelterIntegrity,
                shelterRestSeconds,
                shelterStormDamage,
                shelterX,
                shelterY,
                shelterZ,
                powerRebootSeconds,
                extractionCountdownSeconds,
                survivalSeconds,
                lastMessage,
                scavengedLootKeys,
                false
        );
    }

    public static EchoAshfallLiveMissionState restoredFull(
            boolean shelterBuilt,
            boolean scannerUsed,
            boolean terminalOnline,
            boolean waterUsed,
            boolean foodUsed,
            boolean crossedAsh,
            boolean hazardCleared,
            boolean scavengedSupplies,
            boolean cacheRecovered,
            boolean powerNodeDiscovered,
            boolean powerRepairStarted,
            boolean powerTerminalConfirmed,
            boolean powerRepaired,
            boolean extractionArmed,
            boolean extracted,
            int waterRations,
            int foodRations,
            int repairKits,
            int scavengedSupplyCaches,
            int playerHealth,
            double hydration,
            double hunger,
            double ashExposure,
            double hydrationRecovered,
            double hungerRecovered,
            int dehydrationDamagePulses,
            int starvationDamagePulses,
            boolean rainCollectorBuilt,
            boolean waterPurifierBuilt,
            boolean emergencyWaterLoopSecured,
            boolean foragedFood,
            boolean cleanWaterStockpiled,
            boolean rationsStockpiled,
            boolean scrapKnifeCrafted,
            boolean toolAssistedMining,
            boolean handRecyclerBuilt,
            boolean machineCasingMade,
            boolean wastelandFieldKitAssembled,
            boolean microGeneratorBuilt,
            boolean powerCableRouted,
            boolean energyMeterInstalled,
            boolean scrapDynamoBuilt,
            boolean basicBatteryCharged,
            boolean batteryBankBuilt,
            boolean thermalBurnerBuilt,
            boolean gasMaskEquipped,
            boolean schematicFragmentFound,
            boolean firstSchematicDecoded,
            boolean scrapPressBuilt,
            boolean itemPipeInstalled,
            boolean factoryControllerBuilt,
            boolean researchLabBuilt,
            boolean powerCableUpgraded,
            boolean powerPrioritySet,
            boolean machineOverclocked,
            boolean basicFilterFixed,
            boolean advancedFilterCrafted,
            boolean thermalArrayBuilt,
            boolean warmedAfterExposure,
            boolean atmosphericScrubberBuilt,
            boolean radiationCleanserBuilt,
            boolean fieldMedBayBuilt,
            boolean fieldMedBayUsed,
            boolean filterWorkbenchBuilt,
            boolean oreGrinderBuilt,
            boolean denseAlloyFound,
            boolean isotopeRefinerBuilt,
            boolean alloyWeaponForged,
            boolean alloyKitEquipped,
            boolean relayStationActivated,
            boolean scoutDroneBuilt,
            boolean radAwayUsed,
            boolean stimPackUsed,
            boolean handWarmerUsed,
            boolean thermalLinerInstalled,
            boolean returnBeaconPlaced,
            boolean returnKeystoneBound,
            int scrapMetal,
            int scrapWire,
            int scrapCircuit,
            int machineCasings,
            int fieldPowerGenerated,
            int powerCableSegments,
            int energyMeterReadings,
            int machinePowerGenerated,
            int storedEnergyCells,
            int thermalBurnerHeat,
            int schematicFragments,
            int itemPipeSegments,
            int upgradedPowerCableSegments,
            int overclockHeat,
            int basicFilterCharges,
            int advancedFilterCharges,
            int warmthRecoveredSeconds,
            int radiationCleanserCycles,
            int fieldMedBayTreatments,
            int denseAlloyChunks,
            int alloyWeaponDurability,
            int relaySignalStrength,
            int scoutDroneRangeMeters,
            int radAwayDoses,
            int stimPackDoses,
            int handWarmerCharges,
            int thermalLinerWarmthSeconds,
            int returnBeaconSignalStrength,
            int returnKeystoneCharges,
            int scrapKnifeDurability,
            int toolMiningBlocksBroken,
            int dirtyWaterBottles,
            int filteredWaterBottles,
            int cleanWaterStockpile,
            int foodRationStockpile,
            int rainCollectorCollections,
            int waterPurifierCycles,
            int foragedFoodBundles,
            double toxicAshExposureSeconds,
            double hotAshExposureSeconds,
            int unstableGroundStrikes,
            int electricalDischargeHits,
            double extractionStormExposureSeconds,
            double shelterIntegrity,
            double shelterRestSeconds,
            double shelterStormDamage,
            int shelterX,
            int shelterY,
            int shelterZ,
            double powerRebootSeconds,
            double extractionCountdownSeconds,
            double survivalSeconds,
            String lastMessage,
            List<String> scavengedLootKeys,
            boolean fieldManualRead
    ) {
        EchoAshfallLiveMissionState state = new EchoAshfallLiveMissionState();
        state.fieldManualRead = fieldManualRead || shelterBuilt || scannerUsed || terminalOnline;
        state.shelterBuilt = shelterBuilt;
        state.scannerUsed = scannerUsed;
        state.terminalOnline = terminalOnline;
        state.waterUsed = waterUsed;
        state.foodUsed = foodUsed;
        state.crossedAsh = crossedAsh;
        state.hazardCleared = hazardCleared;
        state.scavengedSupplies = scavengedSupplies;
        state.cacheRecovered = cacheRecovered;
        state.powerNodeDiscovered = powerNodeDiscovered;
        state.powerRepairStarted = powerRepairStarted;
        state.powerTerminalConfirmed = powerTerminalConfirmed;
        state.powerRepaired = powerRepaired;
        state.extractionArmed = extractionArmed;
        state.extracted = extracted;
        state.waterRations = Math.max(0, waterRations);
        state.foodRations = Math.max(0, foodRations);
        state.repairKits = Math.max(0, repairKits);
        state.scavengedSupplyCaches = Math.max(0, scavengedSupplyCaches);
        for (String key : scavengedLootKeys == null ? List.<String>of() : scavengedLootKeys) {
            String normalized = normalizeScavengeKey(key);
            if (!normalized.isBlank()) {
                state.scavengedLootKeys.add(normalized);
            }
        }
        state.playerHealth = Math.max(0, playerHealth);
        state.hydration = clamp(hydration, 0.0D, 100.0D);
        state.hunger = clamp(hunger, 0.0D, 100.0D);
        state.ashExposure = clamp(ashExposure, 0.0D, 100.0D);
        state.hydrationRecovered = Math.max(0.0D, hydrationRecovered);
        state.hungerRecovered = Math.max(0.0D, hungerRecovered);
        state.dehydrationDamagePulses = Math.max(0, dehydrationDamagePulses);
        state.starvationDamagePulses = Math.max(0, starvationDamagePulses);
        state.rainCollectorBuilt = rainCollectorBuilt;
        state.waterPurifierBuilt = waterPurifierBuilt;
        state.emergencyWaterLoopSecured = emergencyWaterLoopSecured;
        state.foragedFood = foragedFood;
        state.cleanWaterStockpiled = cleanWaterStockpiled;
        state.rationsStockpiled = rationsStockpiled;
        state.scrapKnifeCrafted = scrapKnifeCrafted;
        state.toolAssistedMining = toolAssistedMining;
        state.handRecyclerBuilt = handRecyclerBuilt;
        state.machineCasingMade = machineCasingMade;
        state.wastelandFieldKitAssembled = wastelandFieldKitAssembled;
        state.microGeneratorBuilt = microGeneratorBuilt;
        state.powerCableRouted = powerCableRouted;
        state.energyMeterInstalled = energyMeterInstalled;
        state.scrapDynamoBuilt = scrapDynamoBuilt;
        state.basicBatteryCharged = basicBatteryCharged;
        state.batteryBankBuilt = batteryBankBuilt;
        state.thermalBurnerBuilt = thermalBurnerBuilt;
        state.gasMaskEquipped = gasMaskEquipped;
        state.schematicFragmentFound = schematicFragmentFound;
        state.firstSchematicDecoded = firstSchematicDecoded;
        state.scrapPressBuilt = scrapPressBuilt;
        state.itemPipeInstalled = itemPipeInstalled;
        state.factoryControllerBuilt = factoryControllerBuilt;
        state.researchLabBuilt = researchLabBuilt;
        state.powerCableUpgraded = powerCableUpgraded;
        state.powerPrioritySet = powerPrioritySet;
        state.machineOverclocked = machineOverclocked;
        state.basicFilterFixed = basicFilterFixed;
        state.advancedFilterCrafted = advancedFilterCrafted;
        state.thermalArrayBuilt = thermalArrayBuilt;
        state.warmedAfterExposure = warmedAfterExposure;
        state.atmosphericScrubberBuilt = atmosphericScrubberBuilt;
        state.radiationCleanserBuilt = radiationCleanserBuilt;
        state.fieldMedBayBuilt = fieldMedBayBuilt;
        state.fieldMedBayUsed = fieldMedBayUsed;
        state.filterWorkbenchBuilt = filterWorkbenchBuilt;
        state.oreGrinderBuilt = oreGrinderBuilt;
        state.denseAlloyFound = denseAlloyFound;
        state.isotopeRefinerBuilt = isotopeRefinerBuilt;
        state.alloyWeaponForged = alloyWeaponForged;
        state.alloyKitEquipped = alloyKitEquipped;
        state.relayStationActivated = relayStationActivated;
        state.scoutDroneBuilt = scoutDroneBuilt;
        state.radAwayUsed = radAwayUsed;
        state.stimPackUsed = stimPackUsed;
        state.handWarmerUsed = handWarmerUsed;
        state.thermalLinerInstalled = thermalLinerInstalled;
        state.returnBeaconPlaced = returnBeaconPlaced;
        state.returnKeystoneBound = returnKeystoneBound;
        state.scrapMetal = Math.max(0, scrapMetal);
        state.scrapWire = Math.max(0, scrapWire);
        state.scrapCircuit = Math.max(0, scrapCircuit);
        state.machineCasings = Math.max(0, machineCasings);
        state.fieldPowerGenerated = Math.max(0, fieldPowerGenerated);
        state.powerCableSegments = Math.max(0, powerCableSegments);
        state.energyMeterReadings = Math.max(0, energyMeterReadings);
        state.machinePowerGenerated = Math.max(0, machinePowerGenerated);
        state.storedEnergyCells = Math.max(0, storedEnergyCells);
        state.thermalBurnerHeat = Math.max(0, thermalBurnerHeat);
        state.schematicFragments = Math.max(0, schematicFragments);
        state.itemPipeSegments = Math.max(0, itemPipeSegments);
        state.upgradedPowerCableSegments = Math.max(0, upgradedPowerCableSegments);
        state.overclockHeat = Math.max(0, overclockHeat);
        state.basicFilterCharges = Math.max(0, basicFilterCharges);
        state.advancedFilterCharges = Math.max(0, advancedFilterCharges);
        state.warmthRecoveredSeconds = Math.max(0, warmthRecoveredSeconds);
        state.radiationCleanserCycles = Math.max(0, radiationCleanserCycles);
        state.fieldMedBayTreatments = Math.max(0, fieldMedBayTreatments);
        state.denseAlloyChunks = Math.max(0, denseAlloyChunks);
        state.alloyWeaponDurability = Math.max(0, alloyWeaponDurability);
        state.relaySignalStrength = Math.max(0, relaySignalStrength);
        state.scoutDroneRangeMeters = Math.max(0, scoutDroneRangeMeters);
        state.radAwayDoses = Math.max(0, radAwayDoses);
        state.stimPackDoses = Math.max(0, stimPackDoses);
        state.handWarmerCharges = Math.max(0, handWarmerCharges);
        state.thermalLinerWarmthSeconds = Math.max(0, thermalLinerWarmthSeconds);
        state.returnBeaconSignalStrength = Math.max(0, returnBeaconSignalStrength);
        state.returnKeystoneCharges = Math.max(0, returnKeystoneCharges);
        state.scrapKnifeDurability = Math.max(0, scrapKnifeDurability);
        state.toolMiningBlocksBroken = Math.max(0, toolMiningBlocksBroken);
        state.dirtyWaterBottles = Math.max(0, dirtyWaterBottles);
        state.filteredWaterBottles = Math.max(0, filteredWaterBottles);
        state.cleanWaterStockpile = Math.max(0, cleanWaterStockpile);
        state.foodRationStockpile = Math.max(0, foodRationStockpile);
        state.rainCollectorCollections = Math.max(0, rainCollectorCollections);
        state.waterPurifierCycles = Math.max(0, waterPurifierCycles);
        state.foragedFoodBundles = Math.max(0, foragedFoodBundles);
        state.toxicAshExposureSeconds = Math.max(0.0D, toxicAshExposureSeconds);
        state.hotAshExposureSeconds = Math.max(0.0D, hotAshExposureSeconds);
        state.unstableGroundStrikes = Math.max(0, unstableGroundStrikes);
        state.electricalDischargeHits = Math.max(0, electricalDischargeHits);
        state.extractionStormExposureSeconds = Math.max(0.0D, extractionStormExposureSeconds);
        state.shelterIntegrity = clamp(
                shelterIntegrity,
                0.0D,
                shelterBuilt ? 100.0D : 0.0D
        );
        state.shelterRestSeconds = Math.max(0.0D, shelterRestSeconds);
        state.shelterStormDamage = Math.max(0.0D, shelterStormDamage);
        state.shelterX = shelterX;
        state.shelterY = shelterY;
        state.shelterZ = shelterZ;
        state.powerRebootSeconds = clamp(powerRebootSeconds, 0.0D, POWER_REBOOT_REQUIRED_SECONDS);
        state.extractionCountdownSeconds = clamp(
                extractionCountdownSeconds,
                0.0D,
                EXTRACTION_COUNTDOWN_REQUIRED_SECONDS
        );
        state.survivalSeconds = Math.max(0.0D, survivalSeconds);
        state.lastMessage = lastMessage == null || lastMessage.isBlank()
                ? "mission restored"
                : lastMessage.trim();
        if (state.extracted) {
            state.addFeed("Extraction beacon secured");
        } else if (state.powerRepaired) {
            state.addFeed("Power node repaired");
        } else if (state.cacheRecovered) {
            state.addFeed("Crash cache recovered");
        } else if (state.scavengedSupplies) {
            state.addFeed("Scavenged survival supplies");
        } else if (state.hazardCleared) {
            state.addFeed("Ash hazard cleared");
        } else if (state.terminalOnline) {
            state.addFeed("Emergency terminal online");
        } else if (state.scannerUsed) {
            state.addFeed("Scanner route pinged");
        } else if (state.shelterBuilt) {
            state.addFeed("Shelter anchor placed");
        } else if (state.fieldManualRead) {
            state.addFeed("Field manual read");
        }
        return state;
    }

    public void tick(EchoVoxelWorld world, EchoVoxelPlayerState player, boolean moved) {
        tick(world, player, moved, moved ? 1.0D : 0.0D);
    }

    public void tick(EchoVoxelWorld world, EchoVoxelPlayerState player, boolean moved, double seconds) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        tick(world, player, moved, seconds, bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
    }

    public void tick(
            EchoVoxelWorld world,
            EchoVoxelPlayerState player,
            boolean moved,
            double seconds,
            EchoAdapterCoreHazardTable hazardTable
    ) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        tick(world, player, moved, seconds, hazardTable, bridge.shelterProfile(), bridge.survivalProfile());
    }

    public void tick(
            EchoVoxelWorld world,
            EchoVoxelPlayerState player,
            boolean moved,
            double seconds,
            EchoAdapterCoreHazardTable hazardTable,
            EchoAdapterCoreShelterProfile shelterProfile
    ) {
        tick(world, player, moved, seconds, hazardTable, shelterProfile,
                EchoAdapterCoreStandaloneContentBridge.ashfallLive().survivalProfile());
    }

    public void tick(
            EchoVoxelWorld world,
            EchoVoxelPlayerState player,
            boolean moved,
            double seconds,
            EchoAdapterCoreHazardTable hazardTable,
            EchoAdapterCoreShelterProfile shelterProfile,
            EchoAdapterCoreSurvivalProfile survivalProfile
    ) {
        Objects.requireNonNull(hazardTable, "hazardTable");
        Objects.requireNonNull(shelterProfile, "shelterProfile");
        Objects.requireNonNull(survivalProfile, "survivalProfile");
        if (playerHealth <= 0 || extracted) {
            return;
        }
        if (!Double.isFinite(seconds) || seconds < 0.0D) {
            throw new IllegalArgumentException("seconds must be finite and non-negative");
        }
        survivalSeconds += seconds;
        boolean sheltered = sheltered(player, shelterProfile);
        if (powerRepairStarted && !powerRepaired && seconds > 0.0D) {
            powerRebootSeconds = clamp(
                    powerRebootSeconds + seconds,
                    0.0D,
                    POWER_REBOOT_REQUIRED_SECONDS
            );
            if (powerRebootSeconds >= POWER_REBOOT_REQUIRED_SECONDS) {
                lastMessage = "power node reboot stabilized: confirm at terminal";
            }
        }
        if (moved) {
            double minutes = seconds / 60.0D;
            double shelterMultiplier = sheltered ? shelterProfile.resourceMultiplier() : 1.0D;
            hydration = clamp(hydration - (player.sprinting()
                    ? survivalProfile.sprintHydrationDrainPerMinute()
                    : survivalProfile.walkHydrationDrainPerMinute()) * minutes * shelterMultiplier, 0.0D, 100.0D);
            hunger = clamp(hunger - (player.sprinting()
                    ? survivalProfile.sprintHungerDrainPerMinute()
                    : survivalProfile.walkHungerDrainPerMinute()) * minutes * shelterMultiplier, 0.0D, 100.0D);
            EchoAdapterCoreHazardRule contactHazard = contactHazard(world, player, hazardTable);
            if (contactHazard != null) {
                applyContactHazard(contactHazard, seconds, minutes);
            } else if (sheltered) {
                applyShelterRest(shelterProfile, seconds, minutes);
            }
            applySurvivalDamage(survivalProfile);
            if (powerRepaired && nearExtractionBeacon(player)) {
                if (!extractionArmed) {
                    extractionArmed = true;
                    extractionCountdownSeconds = 0.0D;
                    addFeed("Extraction beacon armed");
                }
                extractionCountdownSeconds = clamp(
                        extractionCountdownSeconds + seconds,
                        0.0D,
                        EXTRACTION_COUNTDOWN_REQUIRED_SECONDS
                );
                applyExtractionStorm(hazardTable.extractionStorm(), shelterProfile, sheltered, seconds, minutes);
                if (ashExposure >= 85.0D) {
                    playerHealth = Math.max(0, playerHealth - 1);
                }
                if (extractionCountdownSeconds >= EXTRACTION_COUNTDOWN_REQUIRED_SECONDS) {
                    extracted = true;
                    addFeed("Extraction beacon secured");
                    lastMessage = "mission complete: extraction beacon secured";
                } else {
                    lastMessage = "extraction beacon holding: "
                            + String.format("%.1f", extractionCountdownSeconds)
                            + "/"
                            + String.format("%.1f", EXTRACTION_COUNTDOWN_REQUIRED_SECONDS)
                            + "s";
                }
            }
        }
    }

    public boolean interact(EchoVoxelHit target, EchoVoxelPlayerState player) {
        if (target == null) {
            lastMessage = "interact: aim at terminal, cache, or power node";
            return true;
        }
        String blockId = target.block().id();
        if (isTerminal(blockId)) {
            if (!scannerUsed) {
                lastMessage = "terminal offline: scan the crash site first";
                return true;
            }
            if (powerRepairStarted && !powerRepaired && powerRebootSeconds >= POWER_REBOOT_REQUIRED_SECONDS) {
                powerTerminalConfirmed = true;
                powerRepaired = true;
                lastMessage = "terminal confirmed: power restored, extraction beacon ready";
                addFeed("Power restoration confirmed");
                return true;
            }
            terminalOnline = true;
            lastMessage = "terminal online: cache route unlocked";
            addFeed("Emergency terminal online");
            return true;
        }
        if (isCache(blockId)) {
            if (!terminalOnline) {
                lastMessage = "cache locked: bring terminal online first";
                return true;
            }
            cacheRecovered = true;
            repairKits = Math.max(1, repairKits);
            lastMessage = "cache recovered: power node repair parts found";
            addFeed("Crash cache recovered");
            return true;
        }
        if (isPowerNode(blockId)) {
            powerNodeDiscovered = true;
            if (!cacheRecovered) {
                lastMessage = "power node needs cache repair parts";
                return true;
            }
            if (!hazardCleared) {
                lastMessage = "power node unsafe: clear ash hazard first";
                return true;
            }
            if (repairKits <= 0) {
                lastMessage = "power node needs a cache repair kit";
                return true;
            }
            if (!microGeneratorBuilt || !powerCableRouted || !energyMeterInstalled
                    || !scrapDynamoBuilt || !basicBatteryCharged || !batteryBankBuilt || !thermalBurnerBuilt) {
                lastMessage = "power node: finish field and machine power route before repair";
                return true;
            }
            if (!gasMaskEquipped || !firstSchematicDecoded || !factoryControllerBuilt
                    || !researchLabBuilt || !powerCableUpgraded || !powerPrioritySet || !machineOverclocked) {
                lastMessage = "power node: finish protected factory upgrade route before repair";
                return true;
            }
            if (!basicFilterFixed || !advancedFilterCrafted || !thermalArrayBuilt || !warmedAfterExposure
                    || !atmosphericScrubberBuilt || !radiationCleanserBuilt || !fieldMedBayBuilt || !fieldMedBayUsed) {
                lastMessage = "power node: finish expedition safety route before repair";
                return true;
            }
            if (powerRepairStarted) {
                lastMessage = powerRebootSeconds >= POWER_REBOOT_REQUIRED_SECONDS
                        ? "power node reboot stable: confirm restoration at terminal"
                        : "power node rebooting: " + String.format("%.1f", powerRebootSeconds)
                        + "/" + String.format("%.1f", POWER_REBOOT_REQUIRED_SECONDS) + "s";
                return true;
            }
            repairKits--;
            powerRepairStarted = true;
            powerRebootSeconds = 0.0D;
            lastMessage = "power node repair started: reboot stabilizing";
            addFeed("Power node reboot started");
            return true;
        }
        lastMessage = "interact: " + target.block().displayName() + " has no action";
        return true;
    }

    public boolean readFieldManual(EchoVoxelBlock item) {
        if (item == null || !item.id().equals(EchoAdapterCoreStandaloneContentBridge.FIELD_MANUAL_ITEM_ID)) {
            lastMessage = "field manual: select the starter manual";
            return false;
        }
        if (fieldManualRead) {
            lastMessage = "field manual already read";
            return true;
        }
        fieldManualRead = true;
        lastMessage = "field manual read: first-loop checklist loaded";
        addFeed("Field manual read");
        return true;
    }

    public boolean attemptExtraction(EchoVoxelPlayerState player) {
        Objects.requireNonNull(player, "player");
        if (!nearExtractionBeacon(player)) {
            lastMessage = "extraction denied: return to crash beacon";
            return true;
        }
        if (!terminalOnline) {
            lastMessage = "extraction denied: terminal offline";
            addFeed("Extraction denied: terminal offline");
            return true;
        }
        if (!cacheRecovered) {
            lastMessage = "extraction denied: crash cache authorization missing";
            addFeed("Extraction denied: cache authorization missing");
            return true;
        }
        if (!powerRepaired) {
            lastMessage = "extraction denied: restore power first";
            addFeed("Extraction denied: power offline");
            return true;
        }
        extractionArmed = true;
        extractionCountdownSeconds = 0.0D;
        lastMessage = "extraction beacon armed: hold position";
        addFeed("Extraction beacon armed");
        return true;
    }

    public boolean markShelterBuilt(EchoVoxelBlock block, int x, int y, int z, EchoVoxelPlayerState player) {
        if (block == null || !isShelterAnchor(block.id())) {
            return false;
        }
        if (distance(player.x(), player.z(), 7.5D, 1.5D) > 10.0D) {
            lastMessage = "shelter: place anchor near the crash beacon";
            return true;
        }
        shelterBuilt = true;
        shelterIntegrity = 100.0D;
        shelterRestSeconds = 0.0D;
        shelterStormDamage = 0.0D;
        shelterX = x;
        shelterY = y;
        shelterZ = z;
        lastMessage = "shelter anchor placed @ " + x + "," + y + "," + z;
        addFeed("Shelter anchor placed");
        return true;
    }

    public boolean markRainCollectorBuilt(EchoVoxelBlock block, EchoAdapterCoreWaterLoopProfile waterLoopProfile) {
        Objects.requireNonNull(waterLoopProfile, "waterLoopProfile");
        if (block == null || !block.id().equals(waterLoopProfile.rainCollectorLiveVoxelId())) {
            return false;
        }
        if (!shelterBuilt) {
            lastMessage = "rain collector: place shelter before water infrastructure";
            return true;
        }
        rainCollectorBuilt = true;
        lastMessage = "rain collector built: dirty water collection ready";
        addFeed("Rain collector built");
        return true;
    }

    public boolean collectRainWater(EchoAdapterCoreWaterLoopProfile waterLoopProfile) {
        Objects.requireNonNull(waterLoopProfile, "waterLoopProfile");
        if (!rainCollectorBuilt) {
            lastMessage = "dirty water: build rain collector first";
            return true;
        }
        dirtyWaterBottles += waterLoopProfile.dirtyWaterPerCollection();
        rainCollectorCollections++;
        emergencyWaterLoopSecured = true;
        lastMessage = "rain collector filled dirty water x" + waterLoopProfile.dirtyWaterPerCollection();
        addFeed("Dirty water collected");
        return true;
    }

    public boolean markWaterPurifierBuilt(EchoVoxelBlock block, EchoAdapterCoreWaterLoopProfile waterLoopProfile) {
        Objects.requireNonNull(waterLoopProfile, "waterLoopProfile");
        if (block == null || !block.id().equals(waterLoopProfile.waterPurifierLiveVoxelId())) {
            return false;
        }
        if (!shelterBuilt) {
            lastMessage = "water purifier: place shelter before water infrastructure";
            return true;
        }
        waterPurifierBuilt = true;
        lastMessage = "water purifier built: insert dirty water to start production";
        addFeed("Water purifier built");
        return true;
    }

    public boolean insertDirtyWater(EchoVoxelBlock item, EchoAdapterCoreWaterLoopProfile waterLoopProfile) {
        Objects.requireNonNull(waterLoopProfile, "waterLoopProfile");
        if (item == null || !item.id().equals(EchoAdapterCoreStandaloneContentBridge.DIRTY_WATER_ITEM_ID)) {
            lastMessage = "dirty water: select a dirty water bottle";
            return false;
        }
        if (!waterPurifierBuilt) {
            lastMessage = "dirty water: place water purifier first";
            return true;
        }
        dirtyWaterBottles++;
        emergencyWaterLoopSecured = true;
        lastMessage = "dirty water inserted: purifier input=" + dirtyWaterBottles;
        addFeed("Dirty water inserted");
        return true;
    }

    public boolean purifyWater(EchoAdapterCoreWaterLoopProfile waterLoopProfile) {
        Objects.requireNonNull(waterLoopProfile, "waterLoopProfile");
        if (!waterPurifierBuilt) {
            lastMessage = "purifier: build water purifier first";
            return true;
        }
        if (dirtyWaterBottles <= 0) {
            lastMessage = "purifier: no dirty water loaded";
            return true;
        }
        dirtyWaterBottles--;
        waterRations += waterLoopProfile.cleanWaterPerPurify();
        cleanWaterStockpile += waterLoopProfile.cleanWaterPerPurify();
        filteredWaterBottles++;
        waterPurifierCycles++;
        updateWaterLoopStockpiles(waterLoopProfile);
        lastMessage = "purifier cycle complete: clean water x" + waterLoopProfile.cleanWaterPerPurify();
        addFeed("Clean water purified");
        return true;
    }

    public boolean forageWastelandFood(EchoAdapterCoreWaterLoopProfile waterLoopProfile) {
        Objects.requireNonNull(waterLoopProfile, "waterLoopProfile");
        foodRations += waterLoopProfile.forageFoodRations();
        foodRationStockpile += waterLoopProfile.forageFoodRations();
        foragedFoodBundles++;
        foragedFood = true;
        updateWaterLoopStockpiles(waterLoopProfile);
        lastMessage = "foraged wasteland food: rations x" + waterLoopProfile.forageFoodRations();
        addFeed("Wasteland food foraged");
        return true;
    }

    public boolean useEmergencyScanner(EchoVoxelWorld world, EchoVoxelPlayerState player) {
        if (!shelterBuilt) {
            lastMessage = "scanner: place shelter anchor first";
            return true;
        }
        ScanTarget target = nearestMissionTarget(world, player, 10);
        if (target == null) {
            lastMessage = "scanner: no Ashfall signal in range";
            return true;
        }
        scannerUsed = true;
        lastMessage = "scanner ping: " + target.label() + " @ " + target.x() + "," + target.y() + "," + target.z();
        addFeed("Scanner route pinged");
        return true;
    }

    public boolean useWaterRation() {
        return useWaterRation(EchoAdapterCoreStandaloneContentBridge.ashfallLive().survivalProfile());
    }

    public boolean useWaterRation(EchoAdapterCoreSurvivalProfile survivalProfile) {
        Objects.requireNonNull(survivalProfile, "survivalProfile");
        if (waterRations <= 0) {
            lastMessage = "water: no rations left";
            return true;
        }
        waterRations--;
        waterUsed = true;
        double hydrationBefore = hydration;
        double hungerBefore = hunger;
        hydration = clamp(hydration + survivalProfile.waterHydrationRecovery(), 0.0D, 100.0D);
        ashExposure = clamp(ashExposure - survivalProfile.waterAshRecovery(), 0.0D, 100.0D);
        hydrationRecovered += Math.max(0.0D, hydration - hydrationBefore);
        hungerRecovered += Math.max(0.0D, hunger - hungerBefore);
        lastMessage = "water ration used: hydration restored";
        addFeed("Water ration used");
        return true;
    }

    public boolean useFoodRation() {
        return useFoodRation(EchoAdapterCoreStandaloneContentBridge.ashfallLive().survivalProfile());
    }

    public boolean useFoodRation(EchoAdapterCoreSurvivalProfile survivalProfile) {
        Objects.requireNonNull(survivalProfile, "survivalProfile");
        if (foodRations <= 0) {
            lastMessage = "food: no rations left";
            return true;
        }
        foodRations--;
        foodUsed = true;
        double hungerBefore = hunger;
        hunger = clamp(hunger + survivalProfile.foodHungerRecovery(), 0.0D, 100.0D);
        hungerRecovered += Math.max(0.0D, hunger - hungerBefore);
        playerHealth = Math.min(100, playerHealth + survivalProfile.foodHealthRecovery());
        lastMessage = "field ration used: hunger restored";
        addFeed("Field ration used");
        return true;
    }

    public boolean markHazardCleared(EchoVoxelBlock block) {
        if (block == null || !isHazardTarget(block.id())) {
            return false;
        }
        hazardCleared = true;
        ashExposure = clamp(ashExposure - 8.0D, 0.0D, 100.0D);
        lastMessage = "ash hazard cleared: power route safe";
        addFeed("Ash hazard cleared");
        return true;
    }

    public ScavengeReward scavenge(EchoVoxelBlock block, EchoAdapterCoreScavengeTable scavengeTable) {
        return scavenge(block, scavengeTable, "");
    }

    public ScavengeReward scavenge(
            EchoVoxelBlock block,
            EchoAdapterCoreScavengeTable scavengeTable,
            String sourceKey
    ) {
        Objects.requireNonNull(scavengeTable, "scavengeTable");
        if (block == null || block.air()) {
            lastMessage = "scavenge: no usable material";
            return ScavengeReward.none(lastMessage);
        }
        EchoAdapterCoreScavengeReward reward = scavengeTable.rewardFor(block).orElse(null);
        if (reward != null) {
            String normalizedSourceKey = scavengeKey(sourceKey, reward);
            if (scavengedLootKeys.contains(normalizedSourceKey)) {
                lastMessage = "scavenge depleted: " + block.displayName() + " already searched";
                return ScavengeReward.none(lastMessage);
            }
            scavengedSupplies = true;
            scavengedSupplyCaches++;
            scavengedLootKeys.add(normalizedSourceKey);
            waterRations += reward.waterRations();
            foodRations += reward.foodRations();
            repairKits += reward.repairKits();
            lastMessage = reward.message();
            addFeed("Scavenged AdapterCore survival supplies");
            return new ScavengeReward(
                    true,
                    reward.waterRations() > 0,
                    reward.foodRations() > 0,
                    reward.repairKits(),
                    lastMessage,
                    reward.lootTableContentId()
            );
        }
        lastMessage = "scavenge: " + block.displayName() + " has no useful supplies";
        return ScavengeReward.none(lastMessage);
    }

    public boolean recoverScrapMetal(EchoVoxelBlock block, EchoAdapterCoreToolProfile toolProfile) {
        Objects.requireNonNull(toolProfile, "toolProfile");
        if (block == null || !block.id().equals(EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID)) {
            lastMessage = "scrap recovery: no usable metal";
            return false;
        }
        scrapMetal += toolProfile.scrapMetalRequired();
        lastMessage = "scrap recovered: metal x" + toolProfile.scrapMetalRequired();
        addFeed("Scrap metal recovered");
        return true;
    }

    public boolean craftScrapKnife(EchoAdapterCoreToolProfile toolProfile) {
        Objects.requireNonNull(toolProfile, "toolProfile");
        if (scrapKnifeCrafted) {
            lastMessage = "scrap knife already crafted";
            return true;
        }
        if (scrapMetal < toolProfile.scrapMetalRequired()) {
            lastMessage = "scrap knife: need metal x" + toolProfile.scrapMetalRequired();
            return true;
        }
        scrapMetal -= toolProfile.scrapMetalRequired();
        scrapKnifeCrafted = true;
        scrapKnifeDurability = toolProfile.scrapKnifeMaxDurability();
        lastMessage = "scrap knife crafted from scavenged metal";
        addFeed("Scrap knife crafted");
        return true;
    }

    public boolean recordToolMining(EchoVoxelBlock block, EchoAdapterCoreToolProfile toolProfile) {
        Objects.requireNonNull(toolProfile, "toolProfile");
        if (!scrapKnifeCrafted || scrapKnifeDurability <= 0 || block == null || block.air()) {
            lastMessage = "tool mining: no usable scrap knife";
            return false;
        }
        scrapKnifeDurability = Math.max(0, scrapKnifeDurability - toolProfile.durabilityCostFor(block));
        toolAssistedMining = true;
        toolMiningBlocksBroken++;
        lastMessage = "scrap knife mining: " + block.displayName();
        addFeed("Tool-assisted mining complete");
        return true;
    }

    public boolean recoverWorkshopScrap(EchoAdapterCoreFieldWorkshopProfile workshopProfile) {
        Objects.requireNonNull(workshopProfile, "workshopProfile");
        if (!toolAssistedMining) {
            lastMessage = "workshop scrap: mine debris with the scrap knife first";
            return true;
        }
        scrapMetal += workshopProfile.scrapMetalForCasing();
        scrapWire += workshopProfile.scrapWireForFieldKit();
        scrapCircuit += workshopProfile.scrapCircuitForFieldKit();
        lastMessage = "workshop scrap recovered: casing parts and field kit components";
        addFeed("Workshop scrap recovered");
        return true;
    }

    public boolean markHandRecyclerBuilt(EchoVoxelBlock block, EchoAdapterCoreFieldWorkshopProfile workshopProfile) {
        Objects.requireNonNull(workshopProfile, "workshopProfile");
        if (block == null || !block.id().equals(workshopProfile.handRecyclerLiveVoxelId())) {
            return false;
        }
        if (!shelterBuilt) {
            lastMessage = "hand recycler: place shelter before field workshop";
            return true;
        }
        handRecyclerBuilt = true;
        lastMessage = "hand recycler built: scrap conversion online";
        addFeed("Hand recycler built");
        return true;
    }

    public boolean makeMachineCasing(EchoAdapterCoreFieldWorkshopProfile workshopProfile) {
        Objects.requireNonNull(workshopProfile, "workshopProfile");
        if (!handRecyclerBuilt) {
            lastMessage = "machine casing: build hand recycler first";
            return true;
        }
        if (scrapMetal < workshopProfile.scrapMetalForCasing()) {
            lastMessage = "machine casing: need scrap metal x" + workshopProfile.scrapMetalForCasing();
            return true;
        }
        scrapMetal -= workshopProfile.scrapMetalForCasing();
        machineCasings++;
        machineCasingMade = true;
        lastMessage = "machine casing made from field scrap";
        addFeed("Machine casing made");
        return true;
    }

    public boolean assembleWastelandFieldKit(EchoAdapterCoreFieldWorkshopProfile workshopProfile) {
        Objects.requireNonNull(workshopProfile, "workshopProfile");
        if (machineCasings <= 0
                || scrapWire < workshopProfile.scrapWireForFieldKit()
                || scrapCircuit < workshopProfile.scrapCircuitForFieldKit()) {
            lastMessage = "field kit: missing casing, wire, or circuit";
            return true;
        }
        machineCasings--;
        scrapWire -= workshopProfile.scrapWireForFieldKit();
        scrapCircuit -= workshopProfile.scrapCircuitForFieldKit();
        wastelandFieldKitAssembled = true;
        lastMessage = "wasteland field kit assembled";
        addFeed("Wasteland field kit assembled");
        return true;
    }

    public boolean markMicroGeneratorBuilt(EchoVoxelBlock block, EchoAdapterCoreFieldPowerProfile powerProfile) {
        Objects.requireNonNull(powerProfile, "powerProfile");
        if (block == null || !block.id().equals(powerProfile.microGeneratorLiveVoxelId())) {
            return false;
        }
        if (!wastelandFieldKitAssembled) {
            lastMessage = "micro generator: assemble field kit first";
            return true;
        }
        microGeneratorBuilt = true;
        fieldPowerGenerated = Math.max(fieldPowerGenerated, powerProfile.wattsPerGenerator());
        lastMessage = "micro generator built: field power online";
        addFeed("Micro generator built");
        return true;
    }

    public boolean routePowerCable(EchoVoxelBlock block, EchoAdapterCoreFieldPowerProfile powerProfile) {
        Objects.requireNonNull(powerProfile, "powerProfile");
        if (block == null || !block.id().equals(powerProfile.powerCableLiveVoxelId())) {
            return false;
        }
        if (!microGeneratorBuilt) {
            lastMessage = "power cable: build micro generator first";
            return true;
        }
        powerCableSegments++;
        powerCableRouted = powerCableSegments >= powerProfile.cableSegmentsRequired();
        lastMessage = "power cable routed: "
                + powerCableSegments
                + "/"
                + powerProfile.cableSegmentsRequired()
                + " segments";
        if (powerCableRouted) {
            addFeed("Power cable route complete");
        }
        return true;
    }

    public boolean installEnergyMeter(EchoVoxelBlock block, EchoAdapterCoreFieldPowerProfile powerProfile) {
        Objects.requireNonNull(powerProfile, "powerProfile");
        if (block == null || !block.id().equals(powerProfile.energyMeterLiveVoxelId())) {
            return false;
        }
        if (!powerCableRouted) {
            lastMessage = "energy meter: route power cable first";
            return true;
        }
        energyMeterInstalled = true;
        energyMeterReadings = Math.max(energyMeterReadings, powerProfile.meterReadingsRequired());
        fieldPowerGenerated = Math.max(fieldPowerGenerated, powerProfile.wattsPerGenerator());
        lastMessage = "energy meter installed: " + fieldPowerGenerated + "W field output verified";
        addFeed("Energy meter installed");
        return true;
    }

    public boolean markScrapDynamoBuilt(EchoVoxelBlock block, EchoAdapterCoreMachinePowerProfile powerProfile) {
        Objects.requireNonNull(powerProfile, "powerProfile");
        if (block == null || !block.id().equals(powerProfile.scrapDynamoLiveVoxelId())) {
            return false;
        }
        if (!energyMeterInstalled) {
            lastMessage = "scrap dynamo: install energy meter first";
            return true;
        }
        scrapDynamoBuilt = true;
        machinePowerGenerated = Math.max(machinePowerGenerated, powerProfile.wattsPerScrapDynamo());
        lastMessage = "scrap dynamo built: " + machinePowerGenerated + "W machine route available";
        addFeed("Scrap dynamo built");
        return true;
    }

    public boolean chargeBasicBattery(EchoVoxelBlock item, EchoAdapterCoreMachinePowerProfile powerProfile) {
        Objects.requireNonNull(powerProfile, "powerProfile");
        if (item == null || !item.id().equals(powerProfile.energyCellLiveVoxelId())) {
            return false;
        }
        if (!scrapDynamoBuilt) {
            lastMessage = "energy cell: build scrap dynamo first";
            return true;
        }
        storedEnergyCells += powerProfile.energyCellsPerCharge();
        basicBatteryCharged = true;
        machinePowerGenerated = Math.max(machinePowerGenerated, powerProfile.wattsPerScrapDynamo());
        lastMessage = "energy cell charged: " + storedEnergyCells + " stored";
        addFeed("Energy cell charged");
        return true;
    }

    public boolean markBatteryBankBuilt(EchoVoxelBlock block, EchoAdapterCoreMachinePowerProfile powerProfile) {
        Objects.requireNonNull(powerProfile, "powerProfile");
        if (block == null || !block.id().equals(powerProfile.batteryBankLiveVoxelId())) {
            return false;
        }
        if (!basicBatteryCharged) {
            lastMessage = "battery bank: charge an energy cell first";
            return true;
        }
        batteryBankBuilt = true;
        storedEnergyCells = Math.max(storedEnergyCells, powerProfile.batteryBankCapacityCells());
        lastMessage = "battery bank built: " + storedEnergyCells + " cells buffered";
        addFeed("Battery bank built");
        return true;
    }

    public boolean markThermalBurnerBuilt(EchoVoxelBlock block, EchoAdapterCoreMachinePowerProfile powerProfile) {
        Objects.requireNonNull(powerProfile, "powerProfile");
        if (block == null || !block.id().equals(powerProfile.thermalBurnerLiveVoxelId())) {
            return false;
        }
        if (!batteryBankBuilt) {
            lastMessage = "thermal burner: build battery bank first";
            return true;
        }
        thermalBurnerBuilt = true;
        thermalBurnerHeat = Math.max(thermalBurnerHeat, powerProfile.thermalBurnerHeatUnits());
        lastMessage = "thermal burner built: extraction load stabilized";
        addFeed("Thermal burner built");
        return true;
    }

    public boolean equipGasMask(EchoVoxelBlock item, EchoAdapterCoreMidgameProgressionProfile progressionProfile) {
        Objects.requireNonNull(progressionProfile, "progressionProfile");
        if (item == null || !item.id().equals(progressionProfile.gasMaskLiveVoxelId())) {
            return false;
        }
        if (!thermalBurnerBuilt) {
            lastMessage = "gas mask: stabilize machine power first";
            return true;
        }
        gasMaskEquipped = true;
        lastMessage = "gas mask equipped: deeper ash route survivable";
        addFeed("Gas mask equipped");
        return true;
    }

    public boolean findSchematicFragment(
            EchoVoxelBlock item,
            EchoAdapterCoreMidgameProgressionProfile progressionProfile
    ) {
        Objects.requireNonNull(progressionProfile, "progressionProfile");
        if (item == null || !item.id().equals(progressionProfile.schematicFragmentLiveVoxelId())) {
            return false;
        }
        if (!gasMaskEquipped) {
            lastMessage = "schematic fragment: equip gas mask first";
            return true;
        }
        schematicFragmentFound = true;
        schematicFragments = Math.max(1, schematicFragments + 1);
        lastMessage = "schematic fragment recovered";
        addFeed("Schematic fragment recovered");
        return true;
    }

    public boolean decodeFirstSchematic(EchoAdapterCoreMidgameProgressionProfile progressionProfile) {
        Objects.requireNonNull(progressionProfile, "progressionProfile");
        if (!schematicFragmentFound || schematicFragments <= 0) {
            lastMessage = "schematic decode: recover a fragment first";
            return true;
        }
        firstSchematicDecoded = true;
        lastMessage = "first schematic decoded: factory route unlocked";
        addFeed("First schematic decoded");
        return true;
    }

    public boolean markScrapPressBuilt(
            EchoVoxelBlock block,
            EchoAdapterCoreMidgameProgressionProfile progressionProfile
    ) {
        Objects.requireNonNull(progressionProfile, "progressionProfile");
        if (block == null || !block.id().equals(progressionProfile.scrapPressLiveVoxelId())) {
            return false;
        }
        if (!firstSchematicDecoded) {
            lastMessage = "scrap press: decode first schematic first";
            return true;
        }
        scrapPressBuilt = true;
        lastMessage = "scrap press built: compact parts available";
        addFeed("Scrap press built");
        return true;
    }

    public boolean installItemPipe(
            EchoVoxelBlock block,
            EchoAdapterCoreMidgameProgressionProfile progressionProfile
    ) {
        Objects.requireNonNull(progressionProfile, "progressionProfile");
        if (block == null || !block.id().equals(progressionProfile.itemPipeLiveVoxelId())) {
            return false;
        }
        if (!scrapPressBuilt) {
            lastMessage = "item pipe: build scrap press first";
            return true;
        }
        itemPipeSegments++;
        itemPipeInstalled = itemPipeSegments >= progressionProfile.itemPipeSegmentsRequired();
        lastMessage = "item pipe installed: "
                + itemPipeSegments
                + "/"
                + progressionProfile.itemPipeSegmentsRequired()
                + " segments";
        if (itemPipeInstalled) {
            addFeed("Item pipe route complete");
        }
        return true;
    }

    public boolean markFactoryControllerBuilt(
            EchoVoxelBlock block,
            EchoAdapterCoreMidgameProgressionProfile progressionProfile
    ) {
        Objects.requireNonNull(progressionProfile, "progressionProfile");
        if (block == null || !block.id().equals(progressionProfile.factoryControllerLiveVoxelId())) {
            return false;
        }
        if (!itemPipeInstalled) {
            lastMessage = "factory controller: install item pipe first";
            return true;
        }
        factoryControllerBuilt = true;
        lastMessage = "factory controller built: automation bus online";
        addFeed("Factory controller built");
        return true;
    }

    public boolean markResearchLabBuilt(
            EchoVoxelBlock block,
            EchoAdapterCoreMidgameProgressionProfile progressionProfile
    ) {
        Objects.requireNonNull(progressionProfile, "progressionProfile");
        if (block == null || !block.id().equals(progressionProfile.researchLabLiveVoxelId())) {
            return false;
        }
        if (!factoryControllerBuilt) {
            lastMessage = "research lab: build factory controller first";
            return true;
        }
        researchLabBuilt = true;
        lastMessage = "research lab built: upgrades available";
        addFeed("Research lab built");
        return true;
    }

    public boolean upgradePowerCable(
            EchoVoxelBlock block,
            EchoAdapterCoreMidgameProgressionProfile progressionProfile
    ) {
        Objects.requireNonNull(progressionProfile, "progressionProfile");
        if (block == null || !block.id().equals(progressionProfile.reinforcedPowerCableLiveVoxelId())) {
            return false;
        }
        if (!researchLabBuilt) {
            lastMessage = "power cable upgrade: build research lab first";
            return true;
        }
        upgradedPowerCableSegments++;
        powerCableUpgraded = upgradedPowerCableSegments >= progressionProfile.upgradedCableSegmentsRequired();
        lastMessage = "reinforced cable installed: "
                + upgradedPowerCableSegments
                + "/"
                + progressionProfile.upgradedCableSegmentsRequired()
                + " segments";
        if (powerCableUpgraded) {
            addFeed("Power cable upgraded");
        }
        return true;
    }

    public boolean setPowerPriority(EchoAdapterCoreMidgameProgressionProfile progressionProfile) {
        Objects.requireNonNull(progressionProfile, "progressionProfile");
        if (!factoryControllerBuilt || !powerCableUpgraded) {
            lastMessage = "power priority: finish factory controller and reinforced cable first";
            return true;
        }
        powerPrioritySet = true;
        lastMessage = "power priority set: extraction systems preferred";
        addFeed("Power priority set");
        return true;
    }

    public boolean overclockMachine(EchoAdapterCoreMidgameProgressionProfile progressionProfile) {
        Objects.requireNonNull(progressionProfile, "progressionProfile");
        if (!researchLabBuilt || !powerPrioritySet) {
            lastMessage = "overclock: build research lab and set priority first";
            return true;
        }
        machineOverclocked = true;
        overclockHeat = Math.max(overclockHeat, progressionProfile.overclockHeatUnits());
        lastMessage = "machine overclocked: extraction runtime shortened";
        addFeed("Machine overclocked");
        return true;
    }

    public boolean fixMaskFilter(EchoVoxelBlock item, EchoAdapterCoreExpeditionSafetyProfile safetyProfile) {
        Objects.requireNonNull(safetyProfile, "safetyProfile");
        if (item == null || !item.id().equals(safetyProfile.basicFilterLiveVoxelId())) {
            return false;
        }
        if (!gasMaskEquipped) {
            lastMessage = "mask filter: equip gas mask first";
            return true;
        }
        basicFilterFixed = true;
        basicFilterCharges = Math.max(basicFilterCharges, safetyProfile.basicFilterCharges());
        toxicAshExposureSeconds = Math.max(0.0D, toxicAshExposureSeconds - 3.0D);
        lastMessage = "mask filter fixed: " + basicFilterCharges + " basic charges";
        addFeed("Mask filter fixed");
        return true;
    }

    public boolean craftAdvancedFilter(EchoVoxelBlock item, EchoAdapterCoreExpeditionSafetyProfile safetyProfile) {
        Objects.requireNonNull(safetyProfile, "safetyProfile");
        if (item == null || !item.id().equals(safetyProfile.advancedFilterLiveVoxelId())) {
            return false;
        }
        if (!basicFilterFixed || !researchLabBuilt) {
            lastMessage = "advanced filter: fix mask filter and build research lab first";
            return true;
        }
        advancedFilterCrafted = true;
        advancedFilterCharges = Math.max(advancedFilterCharges, safetyProfile.advancedFilterCharges());
        lastMessage = "advanced filter crafted: " + advancedFilterCharges + " charges";
        addFeed("Advanced filter crafted");
        return true;
    }

    public boolean markThermalArrayBuilt(EchoVoxelBlock block, EchoAdapterCoreExpeditionSafetyProfile safetyProfile) {
        Objects.requireNonNull(safetyProfile, "safetyProfile");
        if (block == null || !block.id().equals(safetyProfile.thermalArrayLiveVoxelId())) {
            return false;
        }
        if (!machineOverclocked) {
            lastMessage = "thermal array: overclock machine route first";
            return true;
        }
        thermalArrayBuilt = true;
        lastMessage = "thermal array built: exposure recovery heat available";
        addFeed("Thermal array built");
        return true;
    }

    public boolean warmUpAfterExposure(EchoVoxelBlock block, EchoAdapterCoreExpeditionSafetyProfile safetyProfile) {
        Objects.requireNonNull(safetyProfile, "safetyProfile");
        if (block == null || !block.id().equals(safetyProfile.thermalArrayLiveVoxelId())) {
            return false;
        }
        if (!thermalArrayBuilt) {
            lastMessage = "warm up: build thermal array first";
            return true;
        }
        warmedAfterExposure = true;
        warmthRecoveredSeconds = Math.max(warmthRecoveredSeconds, safetyProfile.warmthRecoverySeconds());
        hotAshExposureSeconds = Math.max(0.0D, hotAshExposureSeconds - safetyProfile.warmthRecoverySeconds());
        lastMessage = "warmed after exposure: " + warmthRecoveredSeconds + "s recovered";
        addFeed("Exposure warmth recovered");
        return true;
    }

    public boolean markAtmosphericScrubberBuilt(
            EchoVoxelBlock block,
            EchoAdapterCoreExpeditionSafetyProfile safetyProfile
    ) {
        Objects.requireNonNull(safetyProfile, "safetyProfile");
        if (block == null || !block.id().equals(safetyProfile.atmosphericScrubberLiveVoxelId())) {
            return false;
        }
        if (!advancedFilterCrafted || !thermalArrayBuilt) {
            lastMessage = "atmospheric scrubber: craft advanced filter and build thermal array first";
            return true;
        }
        atmosphericScrubberBuilt = true;
        toxicAshExposureSeconds = 0.0D;
        ashExposure = Math.max(0.0D, ashExposure - 20.0D);
        lastMessage = "atmospheric scrubber built: toxic air pocket suppressed";
        addFeed("Atmospheric scrubber built");
        return true;
    }

    public boolean markRadiationCleanserBuilt(
            EchoVoxelBlock block,
            EchoAdapterCoreExpeditionSafetyProfile safetyProfile
    ) {
        Objects.requireNonNull(safetyProfile, "safetyProfile");
        if (block == null || !block.id().equals(safetyProfile.radiationCleanserLiveVoxelId())) {
            return false;
        }
        if (!atmosphericScrubberBuilt) {
            lastMessage = "radiation cleanser: build atmospheric scrubber first";
            return true;
        }
        radiationCleanserBuilt = true;
        radiationCleanserCycles = Math.max(radiationCleanserCycles, safetyProfile.radiationCleanserCycles());
        ashExposure = Math.max(0.0D, ashExposure - 15.0D);
        lastMessage = "radiation cleanser built: " + radiationCleanserCycles + " salvage cycles ready";
        addFeed("Radiation cleanser built");
        return true;
    }

    public boolean markFieldMedBayBuilt(EchoVoxelBlock block, EchoAdapterCoreExpeditionSafetyProfile safetyProfile) {
        Objects.requireNonNull(safetyProfile, "safetyProfile");
        if (block == null || !block.id().equals(safetyProfile.fieldMedBayLiveVoxelId())) {
            return false;
        }
        if (!radiationCleanserBuilt) {
            lastMessage = "field med bay: build radiation cleanser first";
            return true;
        }
        fieldMedBayBuilt = true;
        lastMessage = "field med bay built: route treatment online";
        addFeed("Field med bay built");
        return true;
    }

    public boolean useFieldMedBay(EchoVoxelBlock block, EchoAdapterCoreExpeditionSafetyProfile safetyProfile) {
        Objects.requireNonNull(safetyProfile, "safetyProfile");
        if (block == null || !block.id().equals(safetyProfile.fieldMedBayLiveVoxelId())) {
            return false;
        }
        if (!fieldMedBayBuilt) {
            lastMessage = "field med bay: build it before treatment";
            return true;
        }
        fieldMedBayUsed = true;
        fieldMedBayTreatments = Math.max(fieldMedBayTreatments, safetyProfile.fieldMedBayTreatments());
        playerHealth = Math.min(100, playerHealth + 25);
        ashExposure = Math.max(0.0D, ashExposure - 25.0D);
        lastMessage = "field med bay used: treatment complete";
        addFeed("Field med bay treatment complete");
        return true;
    }

    public boolean markFilterWorkbenchBuilt(
            EchoVoxelBlock block,
            EchoAdapterCoreAdvancedExpeditionProfile advancedProfile
    ) {
        Objects.requireNonNull(advancedProfile, "advancedProfile");
        if (block == null || !block.id().equals(advancedProfile.filterWorkbenchLiveVoxelId())) {
            return false;
        }
        if (!fieldMedBayUsed) {
            lastMessage = "filter workbench: finish expedition treatment route first";
            return true;
        }
        filterWorkbenchBuilt = true;
        basicFilterCharges = Math.max(basicFilterCharges, 90);
        lastMessage = "filter workbench built: advanced route crafting online";
        addFeed("Filter workbench built");
        return true;
    }

    public boolean markOreGrinderBuilt(
            EchoVoxelBlock block,
            EchoAdapterCoreAdvancedExpeditionProfile advancedProfile
    ) {
        Objects.requireNonNull(advancedProfile, "advancedProfile");
        if (block == null || !block.id().equals(advancedProfile.oreGrinderLiveVoxelId())) {
            return false;
        }
        if (!filterWorkbenchBuilt || !machineOverclocked) {
            lastMessage = "ore grinder: build filter workbench and overclock machine route first";
            return true;
        }
        oreGrinderBuilt = true;
        denseAlloyChunks = Math.max(denseAlloyChunks, advancedProfile.denseAlloyPerOreGrind());
        lastMessage = "ore grinder built: dense alloy recovery started";
        addFeed("Ore grinder built");
        return true;
    }

    public boolean findDenseAlloy(
            EchoVoxelBlock item,
            EchoAdapterCoreAdvancedExpeditionProfile advancedProfile
    ) {
        Objects.requireNonNull(advancedProfile, "advancedProfile");
        if (item == null || !item.id().equals(advancedProfile.denseAlloyLiveVoxelId())) {
            return false;
        }
        if (!oreGrinderBuilt) {
            lastMessage = "dense alloy: build ore grinder first";
            return true;
        }
        denseAlloyFound = true;
        denseAlloyChunks = Math.max(denseAlloyChunks + 1, advancedProfile.denseAlloyPerOreGrind());
        lastMessage = "dense alloy found: chunks=" + denseAlloyChunks;
        addFeed("Dense alloy found");
        return true;
    }

    public boolean markIsotopeRefinerBuilt(
            EchoVoxelBlock block,
            EchoAdapterCoreAdvancedExpeditionProfile advancedProfile
    ) {
        Objects.requireNonNull(advancedProfile, "advancedProfile");
        if (block == null || !block.id().equals(advancedProfile.isotopeRefinerLiveVoxelId())) {
            return false;
        }
        if (!denseAlloyFound || !radiationCleanserBuilt) {
            lastMessage = "isotope refiner: find dense alloy and build radiation cleanser first";
            return true;
        }
        isotopeRefinerBuilt = true;
        radiationCleanserCycles = Math.max(radiationCleanserCycles, 3);
        lastMessage = "isotope refiner built: alloy forging unlocked";
        addFeed("Isotope refiner built");
        return true;
    }

    public boolean forgeAlloyWeapon(
            EchoVoxelBlock item,
            EchoAdapterCoreAdvancedExpeditionProfile advancedProfile
    ) {
        Objects.requireNonNull(advancedProfile, "advancedProfile");
        if (item == null || !item.id().equals(advancedProfile.alloyBladeLiveVoxelId())) {
            return false;
        }
        if (!isotopeRefinerBuilt || denseAlloyChunks <= 0) {
            lastMessage = "alloy weapon: build isotope refiner and recover dense alloy first";
            return true;
        }
        alloyWeaponForged = true;
        denseAlloyChunks = Math.max(0, denseAlloyChunks - 1);
        alloyWeaponDurability = Math.max(alloyWeaponDurability, advancedProfile.alloyWeaponDurability());
        lastMessage = "alloy weapon forged: durability=" + alloyWeaponDurability;
        addFeed("Alloy weapon forged");
        return true;
    }

    public boolean equipAlloyKit(
            EchoVoxelBlock helmet,
            EchoVoxelBlock chestplate,
            EchoAdapterCoreAdvancedExpeditionProfile advancedProfile
    ) {
        Objects.requireNonNull(advancedProfile, "advancedProfile");
        if (helmet == null || chestplate == null
                || !helmet.id().equals(advancedProfile.alloyHelmetLiveVoxelId())
                || !chestplate.id().equals(advancedProfile.alloyChestplateLiveVoxelId())) {
            return false;
        }
        if (!alloyWeaponForged) {
            lastMessage = "alloy kit: forge alloy weapon first";
            return true;
        }
        alloyKitEquipped = true;
        playerHealth = Math.min(100, playerHealth + 10);
        lastMessage = "alloy kit equipped: route armor online";
        addFeed("Alloy kit equipped");
        return true;
    }

    public boolean activateRelayStation(
            EchoVoxelBlock block,
            EchoVoxelBlock lens,
            EchoAdapterCoreAdvancedExpeditionProfile advancedProfile
    ) {
        Objects.requireNonNull(advancedProfile, "advancedProfile");
        if (block == null || lens == null
                || !block.id().equals(advancedProfile.relayStationLiveVoxelId())
                || !lens.id().equals(advancedProfile.relayScannerLensLiveVoxelId())) {
            return false;
        }
        if (!alloyKitEquipped || !terminalOnline) {
            lastMessage = "relay station: equip alloy kit and bring terminal online first";
            return true;
        }
        relayStationActivated = true;
        relaySignalStrength = Math.max(relaySignalStrength, advancedProfile.relaySignalStrength());
        lastMessage = "relay station activated: signal=" + relaySignalStrength;
        addFeed("Relay station activated");
        return true;
    }

    public boolean buildScoutDrone(
            EchoVoxelBlock item,
            EchoAdapterCoreAdvancedExpeditionProfile advancedProfile
    ) {
        Objects.requireNonNull(advancedProfile, "advancedProfile");
        if (item == null || !item.id().equals(advancedProfile.scoutDroneItemLiveVoxelId())) {
            return false;
        }
        if (!relayStationActivated) {
            lastMessage = "scout drone: activate relay station first";
            return true;
        }
        scoutDroneBuilt = true;
        scoutDroneRangeMeters = Math.max(scoutDroneRangeMeters, advancedProfile.scoutDroneRangeMeters());
        lastMessage = "scout drone built: range=" + scoutDroneRangeMeters + "m";
        addFeed("Scout drone built");
        return true;
    }

    public boolean useRadAway(EchoVoxelBlock item, EchoAdapterCoreFieldRecoveryProfile recoveryProfile) {
        Objects.requireNonNull(recoveryProfile, "recoveryProfile");
        if (item == null || !item.id().equals(recoveryProfile.radAwayLiveVoxelId())) {
            return false;
        }
        if (!radiationCleanserBuilt) {
            lastMessage = "rad away: build radiation cleanser first";
            return true;
        }
        radAwayUsed = true;
        radAwayDoses++;
        ashExposure = Math.max(0.0D, ashExposure - recoveryProfile.radAwayExposureReduction());
        toxicAshExposureSeconds = Math.max(0.0D, toxicAshExposureSeconds - recoveryProfile.radAwayExposureReduction());
        lastMessage = "rad away used: ash exposure reduced";
        addFeed("Rad away used");
        return true;
    }

    public boolean useStimPack(EchoVoxelBlock item, EchoAdapterCoreFieldRecoveryProfile recoveryProfile) {
        Objects.requireNonNull(recoveryProfile, "recoveryProfile");
        if (item == null || !item.id().equals(recoveryProfile.stimPackLiveVoxelId())) {
            return false;
        }
        if (!fieldMedBayUsed) {
            lastMessage = "stim pack: complete field med bay treatment first";
            return true;
        }
        stimPackUsed = true;
        stimPackDoses++;
        playerHealth = Math.min(100, playerHealth + recoveryProfile.stimPackHeal());
        lastMessage = "stim pack used: health=" + playerHealth;
        addFeed("Stim pack used");
        return true;
    }

    public boolean useHandWarmer(EchoVoxelBlock item, EchoAdapterCoreFieldRecoveryProfile recoveryProfile) {
        Objects.requireNonNull(recoveryProfile, "recoveryProfile");
        if (item == null || !item.id().equals(recoveryProfile.handWarmerLiveVoxelId())) {
            return false;
        }
        if (!thermalArrayBuilt) {
            lastMessage = "hand warmer: build thermal array first";
            return true;
        }
        handWarmerUsed = true;
        handWarmerCharges++;
        warmthRecoveredSeconds += recoveryProfile.handWarmerWarmthSeconds();
        lastMessage = "hand warmer used: warmth=" + warmthRecoveredSeconds;
        addFeed("Hand warmer used");
        return true;
    }

    public boolean installThermalLiner(EchoVoxelBlock item, EchoAdapterCoreFieldRecoveryProfile recoveryProfile) {
        Objects.requireNonNull(recoveryProfile, "recoveryProfile");
        if (item == null || !item.id().equals(recoveryProfile.thermalLinerLiveVoxelId())) {
            return false;
        }
        if (!alloyKitEquipped || !handWarmerUsed) {
            lastMessage = "thermal liner: equip alloy kit and use hand warmer first";
            return true;
        }
        thermalLinerInstalled = true;
        thermalLinerWarmthSeconds = Math.max(
                thermalLinerWarmthSeconds,
                recoveryProfile.thermalLinerWarmthSeconds()
        );
        warmthRecoveredSeconds += recoveryProfile.thermalLinerWarmthSeconds();
        lastMessage = "thermal liner installed: warmth buffer online";
        addFeed("Thermal liner installed");
        return true;
    }

    public boolean placeReturnBeacon(EchoVoxelBlock item, EchoAdapterCoreFieldRecoveryProfile recoveryProfile) {
        Objects.requireNonNull(recoveryProfile, "recoveryProfile");
        if (item == null || !item.id().equals(recoveryProfile.returnBeaconLiveVoxelId())) {
            return false;
        }
        if (!scoutDroneBuilt || !relayStationActivated) {
            lastMessage = "return beacon: activate relay and scout drone first";
            return true;
        }
        returnBeaconPlaced = true;
        returnBeaconSignalStrength = Math.max(
                returnBeaconSignalStrength,
                recoveryProfile.returnBeaconSignalStrength()
        );
        lastMessage = "return beacon placed: signal=" + returnBeaconSignalStrength;
        addFeed("Return beacon placed");
        return true;
    }

    public boolean bindReturnKeystone(EchoVoxelBlock item, EchoAdapterCoreFieldRecoveryProfile recoveryProfile) {
        Objects.requireNonNull(recoveryProfile, "recoveryProfile");
        if (item == null || !item.id().equals(recoveryProfile.returnKeystoneLiveVoxelId())) {
            return false;
        }
        if (!returnBeaconPlaced) {
            lastMessage = "return keystone: place return beacon first";
            return true;
        }
        returnKeystoneBound = true;
        returnKeystoneCharges = Math.max(returnKeystoneCharges, recoveryProfile.returnKeystoneCharges());
        lastMessage = "return keystone bound: extraction fallback ready";
        addFeed("Return keystone bound");
        return true;
    }

    public String status() {
        if (playerHealth <= 0) {
            return "FAILED";
        }
        return extracted ? "COMPLETED" : "ACTIVE";
    }

    public int completedObjectives() {
        int completed = 0;
        if (beaconTracked) {
            completed++;
        }
        if (fieldManualRead) {
            completed++;
        }
        if (shelterBuilt) {
            completed++;
        }
        if (scannerUsed) {
            completed++;
        }
        if (terminalOnline) {
            completed++;
        }
        if (waterUsed) {
            completed++;
        }
        if (foodUsed) {
            completed++;
        }
        if (crossedAsh) {
            completed++;
        }
        if (hazardCleared) {
            completed++;
        }
        if (scavengedSupplies) {
            completed++;
        }
        if (scrapKnifeCrafted) {
            completed++;
        }
        if (toolAssistedMining) {
            completed++;
        }
        if (handRecyclerBuilt) {
            completed++;
        }
        if (machineCasingMade) {
            completed++;
        }
        if (wastelandFieldKitAssembled) {
            completed++;
        }
        if (microGeneratorBuilt) {
            completed++;
        }
        if (powerCableRouted) {
            completed++;
        }
        if (energyMeterInstalled) {
            completed++;
        }
        if (scrapDynamoBuilt) {
            completed++;
        }
        if (basicBatteryCharged) {
            completed++;
        }
        if (batteryBankBuilt) {
            completed++;
        }
        if (thermalBurnerBuilt) {
            completed++;
        }
        if (gasMaskEquipped) {
            completed++;
        }
        if (schematicFragmentFound) {
            completed++;
        }
        if (firstSchematicDecoded) {
            completed++;
        }
        if (scrapPressBuilt) {
            completed++;
        }
        if (itemPipeInstalled) {
            completed++;
        }
        if (factoryControllerBuilt) {
            completed++;
        }
        if (researchLabBuilt) {
            completed++;
        }
        if (powerCableUpgraded) {
            completed++;
        }
        if (powerPrioritySet) {
            completed++;
        }
        if (machineOverclocked) {
            completed++;
        }
        if (basicFilterFixed) {
            completed++;
        }
        if (advancedFilterCrafted) {
            completed++;
        }
        if (thermalArrayBuilt) {
            completed++;
        }
        if (warmedAfterExposure) {
            completed++;
        }
        if (atmosphericScrubberBuilt) {
            completed++;
        }
        if (radiationCleanserBuilt) {
            completed++;
        }
        if (fieldMedBayBuilt) {
            completed++;
        }
        if (fieldMedBayUsed) {
            completed++;
        }
        if (filterWorkbenchBuilt) {
            completed++;
        }
        if (oreGrinderBuilt) {
            completed++;
        }
        if (denseAlloyFound) {
            completed++;
        }
        if (isotopeRefinerBuilt) {
            completed++;
        }
        if (alloyWeaponForged) {
            completed++;
        }
        if (alloyKitEquipped) {
            completed++;
        }
        if (relayStationActivated) {
            completed++;
        }
        if (scoutDroneBuilt) {
            completed++;
        }
        if (radAwayUsed) {
            completed++;
        }
        if (stimPackUsed) {
            completed++;
        }
        if (handWarmerUsed) {
            completed++;
        }
        if (thermalLinerInstalled) {
            completed++;
        }
        if (returnBeaconPlaced) {
            completed++;
        }
        if (returnKeystoneBound) {
            completed++;
        }
        if (emergencyWaterLoopSecured) {
            completed++;
        }
        if (foragedFood) {
            completed++;
        }
        if (rainCollectorBuilt) {
            completed++;
        }
        if (rationsStockpiled) {
            completed++;
        }
        if (waterPurifierBuilt) {
            completed++;
        }
        if (cleanWaterStockpiled) {
            completed++;
        }
        if (cacheRecovered) {
            completed++;
        }
        if (powerRepaired) {
            completed++;
        }
        if (extracted) {
            completed++;
        }
        return completed;
    }

    public int totalObjectives() {
        return 63;
    }

    public int playerHealth() {
        return playerHealth;
    }

    public double hydration() {
        return hydration;
    }

    public double hunger() {
        return hunger;
    }

    public double ashExposure() {
        return ashExposure;
    }

    public double hydrationRecovered() {
        return hydrationRecovered;
    }

    public double hungerRecovered() {
        return hungerRecovered;
    }

    public int dehydrationDamagePulses() {
        return dehydrationDamagePulses;
    }

    public int starvationDamagePulses() {
        return starvationDamagePulses;
    }

    public double toxicAshExposureSeconds() {
        return toxicAshExposureSeconds;
    }

    public double hotAshExposureSeconds() {
        return hotAshExposureSeconds;
    }

    public int unstableGroundStrikes() {
        return unstableGroundStrikes;
    }

    public int electricalDischargeHits() {
        return electricalDischargeHits;
    }

    public double extractionStormExposureSeconds() {
        return extractionStormExposureSeconds;
    }

    public double shelterIntegrity() {
        return shelterIntegrity;
    }

    public double shelterRestSeconds() {
        return shelterRestSeconds;
    }

    public double shelterStormDamage() {
        return shelterStormDamage;
    }

    public int shelterX() {
        return shelterX;
    }

    public int shelterY() {
        return shelterY;
    }

    public int shelterZ() {
        return shelterZ;
    }

    public double survivalSeconds() {
        return survivalSeconds;
    }

    public double estimatedWalkingSurvivalMinutes() {
        EchoAdapterCoreSurvivalProfile survivalProfile =
                EchoAdapterCoreStandaloneContentBridge.ashfallLive().survivalProfile();
        return Math.min(
                hydration / survivalProfile.walkHydrationDrainPerMinute(),
                hunger / survivalProfile.walkHungerDrainPerMinute()
        );
    }

    public int waterRations() {
        return waterRations;
    }

    public int foodRations() {
        return foodRations;
    }

    public int dirtyWaterBottles() {
        return dirtyWaterBottles;
    }

    public int filteredWaterBottles() {
        return filteredWaterBottles;
    }

    public int cleanWaterStockpile() {
        return cleanWaterStockpile;
    }

    public int foodRationStockpile() {
        return foodRationStockpile;
    }

    public int rainCollectorCollections() {
        return rainCollectorCollections;
    }

    public int waterPurifierCycles() {
        return waterPurifierCycles;
    }

    public int foragedFoodBundles() {
        return foragedFoodBundles;
    }

    public int repairKits() {
        return repairKits;
    }

    public int scavengedSupplyCaches() {
        return scavengedSupplyCaches;
    }

    public int scrapMetal() {
        return scrapMetal;
    }

    public int scrapWire() {
        return scrapWire;
    }

    public int scrapCircuit() {
        return scrapCircuit;
    }

    public int machineCasings() {
        return machineCasings;
    }

    public int fieldPowerGenerated() {
        return fieldPowerGenerated;
    }

    public int powerCableSegments() {
        return powerCableSegments;
    }

    public int energyMeterReadings() {
        return energyMeterReadings;
    }

    public int machinePowerGenerated() {
        return machinePowerGenerated;
    }

    public int storedEnergyCells() {
        return storedEnergyCells;
    }

    public int thermalBurnerHeat() {
        return thermalBurnerHeat;
    }

    public int schematicFragments() {
        return schematicFragments;
    }

    public int itemPipeSegments() {
        return itemPipeSegments;
    }

    public int upgradedPowerCableSegments() {
        return upgradedPowerCableSegments;
    }

    public int overclockHeat() {
        return overclockHeat;
    }

    public int basicFilterCharges() {
        return basicFilterCharges;
    }

    public int advancedFilterCharges() {
        return advancedFilterCharges;
    }

    public int warmthRecoveredSeconds() {
        return warmthRecoveredSeconds;
    }

    public int radiationCleanserCycles() {
        return radiationCleanserCycles;
    }

    public int fieldMedBayTreatments() {
        return fieldMedBayTreatments;
    }

    public int denseAlloyChunks() {
        return denseAlloyChunks;
    }

    public int alloyWeaponDurability() {
        return alloyWeaponDurability;
    }

    public int relaySignalStrength() {
        return relaySignalStrength;
    }

    public int scoutDroneRangeMeters() {
        return scoutDroneRangeMeters;
    }

    public int radAwayDoses() {
        return radAwayDoses;
    }

    public int stimPackDoses() {
        return stimPackDoses;
    }

    public int handWarmerCharges() {
        return handWarmerCharges;
    }

    public int thermalLinerWarmthSeconds() {
        return thermalLinerWarmthSeconds;
    }

    public int returnBeaconSignalStrength() {
        return returnBeaconSignalStrength;
    }

    public int returnKeystoneCharges() {
        return returnKeystoneCharges;
    }

    public int scrapKnifeDurability() {
        return scrapKnifeDurability;
    }

    public int toolMiningBlocksBroken() {
        return toolMiningBlocksBroken;
    }

    public List<String> scavengedLootKeys() {
        return List.copyOf(scavengedLootKeys);
    }

    public boolean shelterBuilt() {
        return shelterBuilt;
    }

    public boolean fieldManualRead() {
        return fieldManualRead;
    }

    public boolean scannerUsed() {
        return scannerUsed;
    }

    public boolean terminalOnline() {
        return terminalOnline;
    }

    public boolean waterUsed() {
        return waterUsed;
    }

    public boolean foodUsed() {
        return foodUsed;
    }

    public boolean crossedAsh() {
        return crossedAsh;
    }

    public boolean hazardCleared() {
        return hazardCleared;
    }

    public boolean scavengedSupplies() {
        return scavengedSupplies;
    }

    public boolean rainCollectorBuilt() {
        return rainCollectorBuilt;
    }

    public boolean waterPurifierBuilt() {
        return waterPurifierBuilt;
    }

    public boolean emergencyWaterLoopSecured() {
        return emergencyWaterLoopSecured;
    }

    public boolean foragedFood() {
        return foragedFood;
    }

    public boolean cleanWaterStockpiled() {
        return cleanWaterStockpiled;
    }

    public boolean rationsStockpiled() {
        return rationsStockpiled;
    }

    public boolean scrapKnifeCrafted() {
        return scrapKnifeCrafted;
    }

    public boolean toolAssistedMining() {
        return toolAssistedMining;
    }

    public boolean handRecyclerBuilt() {
        return handRecyclerBuilt;
    }

    public boolean machineCasingMade() {
        return machineCasingMade;
    }

    public boolean wastelandFieldKitAssembled() {
        return wastelandFieldKitAssembled;
    }

    public boolean microGeneratorBuilt() {
        return microGeneratorBuilt;
    }

    public boolean powerCableRouted() {
        return powerCableRouted;
    }

    public boolean energyMeterInstalled() {
        return energyMeterInstalled;
    }

    public boolean scrapDynamoBuilt() {
        return scrapDynamoBuilt;
    }

    public boolean basicBatteryCharged() {
        return basicBatteryCharged;
    }

    public boolean batteryBankBuilt() {
        return batteryBankBuilt;
    }

    public boolean thermalBurnerBuilt() {
        return thermalBurnerBuilt;
    }

    public boolean gasMaskEquipped() {
        return gasMaskEquipped;
    }

    public boolean schematicFragmentFound() {
        return schematicFragmentFound;
    }

    public boolean firstSchematicDecoded() {
        return firstSchematicDecoded;
    }

    public boolean scrapPressBuilt() {
        return scrapPressBuilt;
    }

    public boolean itemPipeInstalled() {
        return itemPipeInstalled;
    }

    public boolean factoryControllerBuilt() {
        return factoryControllerBuilt;
    }

    public boolean researchLabBuilt() {
        return researchLabBuilt;
    }

    public boolean powerCableUpgraded() {
        return powerCableUpgraded;
    }

    public boolean powerPrioritySet() {
        return powerPrioritySet;
    }

    public boolean machineOverclocked() {
        return machineOverclocked;
    }

    public boolean basicFilterFixed() {
        return basicFilterFixed;
    }

    public boolean advancedFilterCrafted() {
        return advancedFilterCrafted;
    }

    public boolean thermalArrayBuilt() {
        return thermalArrayBuilt;
    }

    public boolean warmedAfterExposure() {
        return warmedAfterExposure;
    }

    public boolean atmosphericScrubberBuilt() {
        return atmosphericScrubberBuilt;
    }

    public boolean radiationCleanserBuilt() {
        return radiationCleanserBuilt;
    }

    public boolean fieldMedBayBuilt() {
        return fieldMedBayBuilt;
    }

    public boolean fieldMedBayUsed() {
        return fieldMedBayUsed;
    }

    public boolean filterWorkbenchBuilt() {
        return filterWorkbenchBuilt;
    }

    public boolean oreGrinderBuilt() {
        return oreGrinderBuilt;
    }

    public boolean denseAlloyFound() {
        return denseAlloyFound;
    }

    public boolean isotopeRefinerBuilt() {
        return isotopeRefinerBuilt;
    }

    public boolean alloyWeaponForged() {
        return alloyWeaponForged;
    }

    public boolean alloyKitEquipped() {
        return alloyKitEquipped;
    }

    public boolean relayStationActivated() {
        return relayStationActivated;
    }

    public boolean scoutDroneBuilt() {
        return scoutDroneBuilt;
    }

    public boolean radAwayUsed() {
        return radAwayUsed;
    }

    public boolean stimPackUsed() {
        return stimPackUsed;
    }

    public boolean handWarmerUsed() {
        return handWarmerUsed;
    }

    public boolean thermalLinerInstalled() {
        return thermalLinerInstalled;
    }

    public boolean returnBeaconPlaced() {
        return returnBeaconPlaced;
    }

    public boolean returnKeystoneBound() {
        return returnKeystoneBound;
    }

    public boolean cacheRecovered() {
        return cacheRecovered;
    }

    public boolean powerRepaired() {
        return powerRepaired;
    }

    public boolean powerNodeDiscovered() {
        return powerNodeDiscovered;
    }

    public boolean powerRepairStarted() {
        return powerRepairStarted;
    }

    public boolean powerTerminalConfirmed() {
        return powerTerminalConfirmed;
    }

    public double powerRebootSeconds() {
        return powerRebootSeconds;
    }

    public boolean powerRebootStabilized() {
        return powerRebootSeconds >= POWER_REBOOT_REQUIRED_SECONDS;
    }

    public boolean extracted() {
        return extracted;
    }

    public boolean extractionArmed() {
        return extractionArmed;
    }

    public double extractionCountdownSeconds() {
        return extractionCountdownSeconds;
    }

    public boolean extractionCountdownComplete() {
        return extractionCountdownSeconds >= EXTRACTION_COUNTDOWN_REQUIRED_SECONDS;
    }

    public String nextObjective() {
        if (!fieldManualRead) {
            return "Read field manual";
        }
        if (!shelterBuilt) {
            return "Place shelter anchor";
        }
        if (!scannerUsed) {
            return "Use emergency scanner";
        }
        if (!terminalOnline) {
            return "Right-click terminal";
        }
        if (!waterUsed) {
            return "Select water, right-click";
        }
        if (!foodUsed) {
            return "Select food, right-click";
        }
        if (!crossedAsh) {
            return "Cross toxic ash";
        }
        if (!hazardCleared) {
            return "Break ash hazard";
        }
        if (!scavengedSupplies) {
            return "Scavenge debris for supplies";
        }
        if (!scrapKnifeCrafted) {
            return "Craft scrap knife";
        }
        if (!toolAssistedMining) {
            return "Mine debris with scrap knife";
        }
        if (!handRecyclerBuilt) {
            return "Build hand recycler";
        }
        if (!machineCasingMade) {
            return "Make machine casing";
        }
        if (!wastelandFieldKitAssembled) {
            return "Assemble field kit";
        }
        if (!microGeneratorBuilt) {
            return "Build micro generator";
        }
        if (!powerCableRouted) {
            return "Route power cable";
        }
        if (!energyMeterInstalled) {
            return "Install energy meter";
        }
        if (!scrapDynamoBuilt) {
            return "Build scrap dynamo";
        }
        if (!basicBatteryCharged) {
            return "Charge energy cell";
        }
        if (!batteryBankBuilt) {
            return "Build battery bank";
        }
        if (!thermalBurnerBuilt) {
            return "Build thermal burner";
        }
        if (!gasMaskEquipped) {
            return "Equip gas mask";
        }
        if (!schematicFragmentFound) {
            return "Find schematic fragment";
        }
        if (!firstSchematicDecoded) {
            return "Decode first schematic";
        }
        if (!scrapPressBuilt) {
            return "Build scrap press";
        }
        if (!itemPipeInstalled) {
            return "Install item pipe";
        }
        if (!factoryControllerBuilt) {
            return "Build factory controller";
        }
        if (!researchLabBuilt) {
            return "Build research lab";
        }
        if (!powerCableUpgraded) {
            return "Upgrade power cable";
        }
        if (!powerPrioritySet) {
            return "Set power priority";
        }
        if (!machineOverclocked) {
            return "Overclock machine";
        }
        if (!basicFilterFixed) {
            return "Fix mask filter";
        }
        if (!advancedFilterCrafted) {
            return "Craft advanced filter";
        }
        if (!thermalArrayBuilt) {
            return "Build thermal array";
        }
        if (!warmedAfterExposure) {
            return "Warm up after exposure";
        }
        if (!atmosphericScrubberBuilt) {
            return "Build atmospheric scrubber";
        }
        if (!radiationCleanserBuilt) {
            return "Build radiation cleanser";
        }
        if (!fieldMedBayBuilt) {
            return "Build field med bay";
        }
        if (!fieldMedBayUsed) {
            return "Use field med bay";
        }
        if (!filterWorkbenchBuilt) {
            return "Build filter workbench";
        }
        if (!oreGrinderBuilt) {
            return "Build ore grinder";
        }
        if (!denseAlloyFound) {
            return "Recover dense alloy";
        }
        if (!isotopeRefinerBuilt) {
            return "Build isotope refiner";
        }
        if (!alloyWeaponForged) {
            return "Forge alloy weapon";
        }
        if (!alloyKitEquipped) {
            return "Equip alloy kit";
        }
        if (!relayStationActivated) {
            return "Activate relay station";
        }
        if (!scoutDroneBuilt) {
            return "Build scout drone";
        }
        if (!radAwayUsed) {
            return "Use rad away";
        }
        if (!stimPackUsed) {
            return "Use stim pack";
        }
        if (!handWarmerUsed) {
            return "Use hand warmer";
        }
        if (!thermalLinerInstalled) {
            return "Install thermal liner";
        }
        if (!returnBeaconPlaced) {
            return "Place return beacon";
        }
        if (!returnKeystoneBound) {
            return "Bind return keystone";
        }
        if (!emergencyWaterLoopSecured) {
            return "Collect dirty water";
        }
        if (!foragedFood) {
            return "Forage wasteland food";
        }
        if (!rainCollectorBuilt) {
            return "Build rain collector";
        }
        if (!rationsStockpiled) {
            return "Stockpile rations";
        }
        if (!waterPurifierBuilt) {
            return "Build water purifier";
        }
        if (!cleanWaterStockpiled) {
            return "Stockpile clean water";
        }
        if (!cacheRecovered) {
            return "Recover crash cache";
        }
        if (!powerRepaired) {
            if (powerRepairStarted && !powerRebootStabilized()) {
                return "Wait for power reboot";
            }
            if (powerRepairStarted) {
                return "Confirm power at terminal";
            }
            return "Repair power node";
        }
        if (!extracted) {
            if (extractionArmed) {
                return "Hold extraction beacon";
            }
            return "Return to crash beacon";
        }
        return "Mission complete";
    }

    public String currentObjective() {
        return nextObjective();
    }

    public String currentHint() {
        if (playerHealth <= 0) {
            return "Retry from the last save or restore a backup profile";
        }
        if (!fieldManualRead) {
            return "Read the field manual from the starter kit to load the first-loop checklist";
        }
        if (!shelterBuilt) {
            return "Place the shelter anchor within ten blocks of the crash beacon";
        }
        if (!scannerUsed) {
            return "Use the emergency scanner from the hotbar to find the terminal route";
        }
        if (!terminalOnline) {
            return "Open the field terminal to unlock crash cache coordinates";
        }
        if (!waterUsed && hydration <= 45.0D) {
            return "Use a water ration before ash exposure starts draining health";
        }
        if (!foodUsed && hunger <= 45.0D) {
            return "Use a field ration to stabilize hunger before the power route";
        }
        if (!crossedAsh) {
            return "Cross toxic ash briefly, then retreat to shelter if exposure climbs";
        }
        if (!hazardCleared) {
            return "Break the ash hazard marker before touching the damaged power node";
        }
        if (!scavengedSupplies) {
            return "Scavenge rusted debris or the cache to recover extra survival supplies";
        }
        if (!scrapKnifeCrafted) {
            return "Craft a scrap knife from recovered metal before the longer power route";
        }
        if (!toolAssistedMining) {
            return "Use the scrap knife to mine debris faster and preserve route time";
        }
        if (!handRecyclerBuilt) {
            return "Place the hand recycler near shelter to unlock field workshop crafting";
        }
        if (!machineCasingMade) {
            return "Use recovered scrap in the hand recycler to make a machine casing";
        }
        if (!wastelandFieldKitAssembled) {
            return "Combine casing, scrap wire, and scrap circuit into a wasteland field kit";
        }
        if (!microGeneratorBuilt) {
            return "Place the micro generator near shelter to start field power";
        }
        if (!powerCableRouted) {
            return "Route power cable segments toward the damaged node";
        }
        if (!energyMeterInstalled) {
            return "Install the energy meter to verify field power output";
        }
        if (!scrapDynamoBuilt) {
            return "Build the scrap dynamo from the verified field microgrid";
        }
        if (!basicBatteryCharged) {
            return "Charge an energy cell before adding buffer storage";
        }
        if (!batteryBankBuilt) {
            return "Place the battery bank to buffer the extraction route";
        }
        if (!thermalBurnerBuilt) {
            return "Build the thermal burner to stabilize the power repair load";
        }
        if (!gasMaskEquipped) {
            return "Equip the gas mask before pushing deeper into ashfall ruins";
        }
        if (!schematicFragmentFound) {
            return "Recover a schematic fragment from the protected route";
        }
        if (!firstSchematicDecoded) {
            return "Decode the schematic fragment to unlock factory infrastructure";
        }
        if (!scrapPressBuilt) {
            return "Place the scrap press to compact machine parts";
        }
        if (!itemPipeInstalled) {
            return "Install item pipe segments from the scrap press toward the controller";
        }
        if (!factoryControllerBuilt) {
            return "Place the factory controller to coordinate powered machines";
        }
        if (!researchLabBuilt) {
            return "Build the research lab after the controller is online";
        }
        if (!powerCableUpgraded) {
            return "Upgrade the power cable route with reinforced segments";
        }
        if (!powerPrioritySet) {
            return "Set extraction as the factory controller power priority";
        }
        if (!machineOverclocked) {
            return "Overclock the machine route once research and priority are ready";
        }
        if (!basicFilterFixed) {
            return "Use a basic filter cartridge to restore gas mask route endurance";
        }
        if (!advancedFilterCrafted) {
            return "Craft an advanced filter before building deeper hazard infrastructure";
        }
        if (!thermalArrayBuilt) {
            return "Build a thermal array to create a recovery point after hot ash exposure";
        }
        if (!warmedAfterExposure) {
            return "Use the thermal array to recover after hot ash exposure";
        }
        if (!atmosphericScrubberBuilt) {
            return "Build an atmospheric scrubber to suppress toxic-air pockets near the route";
        }
        if (!radiationCleanserBuilt) {
            return "Build a radiation cleanser before hauling contaminated salvage home";
        }
        if (!fieldMedBayBuilt) {
            return "Build a field med bay for radiation and mutation route recovery";
        }
        if (!fieldMedBayUsed) {
            return "Use the field med bay before committing to the damaged node repair";
        }
        if (!filterWorkbenchBuilt) {
            return "Build the filter workbench to prepare the relay expedition kit";
        }
        if (!oreGrinderBuilt) {
            return "Place the ore grinder once the overclocked factory route is stable";
        }
        if (!denseAlloyFound) {
            return "Process contaminated ore into dense alloy before isotope refinement";
        }
        if (!isotopeRefinerBuilt) {
            return "Build the isotope refiner after cleanser protection is online";
        }
        if (!alloyWeaponForged) {
            return "Forge the alloy blade from refined dense alloy for the relay push";
        }
        if (!alloyKitEquipped) {
            return "Equip the alloy helmet and chestplate before activating the relay";
        }
        if (!relayStationActivated) {
            return "Install the relay lens and bring the relay station online";
        }
        if (!scoutDroneBuilt) {
            return "Build the scout drone to survey the extraction route";
        }
        if (!radAwayUsed) {
            return "Use rad away to clear exposure before the final water and extraction push";
        }
        if (!stimPackUsed) {
            return "Use a stim pack to prove field healing works outside the med bay";
        }
        if (!handWarmerUsed) {
            return "Use a hand warmer to extend cold-route recovery";
        }
        if (!thermalLinerInstalled) {
            return "Install the thermal liner into the alloy kit";
        }
        if (!returnBeaconPlaced) {
            return "Place the return beacon after the scout drone confirms the route";
        }
        if (!returnKeystoneBound) {
            return "Bind the return keystone to the beacon as an extraction fallback";
        }
        if (!emergencyWaterLoopSecured) {
            return "Build a rain collector and collect dirty water before the long route";
        }
        if (!foragedFood) {
            return "Forage wasteland food to create a ration buffer";
        }
        if (!rainCollectorBuilt) {
            return "Place the rain collector near shelter to seed the emergency water loop";
        }
        if (!rationsStockpiled) {
            return "Stockpile field rations before committing to power repairs";
        }
        if (!waterPurifierBuilt) {
            return "Place the water purifier once dirty water is available";
        }
        if (!cleanWaterStockpiled) {
            return "Run the purifier until the clean water reserve is stable";
        }
        if (!cacheRecovered) {
            return "Return to the crash cache after terminal authorization";
        }
        if (!powerRepaired) {
            if (!powerNodeDiscovered) {
                return "Inspect the damaged power node after recovering repair parts";
            }
            if (powerRepairStarted && !powerRebootStabilized()) {
                return "Hold position while the power node reboot stabilizes";
            }
            if (powerRepairStarted) {
                return "Return to the terminal to confirm restored power";
            }
            return repairKits > 0
                    ? "Use the cache repair kit on the damaged power node"
                    : "Recover a repair kit before attempting the power node";
        }
        if (!extracted) {
            if (extractionArmed) {
                return "Hold the beacon through the extraction storm";
            }
            return "Return to the crash beacon to start extraction";
        }
        return "Extraction complete; export a support bundle if this was a test run";
    }

    public String shelterStatus() {
        if (!shelterBuilt) {
            return "NO SHELTER";
        }
        return "SHELTER "
                + String.format("%.0f", shelterIntegrity)
                + "% @"
                + shelterX
                + ","
                + shelterY
                + ","
                + shelterZ;
    }

    public String terminalState() {
        if (extracted) {
            return "EXTRACTION AUTHORIZED";
        }
        if (!scannerUsed) {
            return "OFFLINE";
        }
        if (!terminalOnline) {
            return "DAMAGED";
        }
        if (powerRepairStarted && !powerRepaired && !powerRebootStabilized()) {
            return "REBOOTING";
        }
        if (powerRepairStarted && !powerRepaired) {
            return "AWAITING CONFIRMATION";
        }
        if (!powerRepaired) {
            return "LOW POWER";
        }
        return "ONLINE";
    }

    public String extractionStatus() {
        if (extracted) {
            return "EXTRACTED";
        }
        if (extractionArmed) {
            return "EXTRACTION COUNTDOWN "
                    + String.format("%.1f", extractionCountdownSeconds)
                    + "/"
                    + String.format("%.1f", EXTRACTION_COUNTDOWN_REQUIRED_SECONDS)
                    + "s";
        }
        if (!terminalOnline) {
            return "LOCKED";
        }
        if (!cacheRecovered) {
            return "CACHE REQUIRED";
        }
        if (!powerRepaired) {
            if (powerRepairStarted && !powerRebootStabilized()) {
                return "REBOOTING";
            }
            if (powerRepairStarted) {
                return "TERMINAL CONFIRMATION REQUIRED";
            }
            return "POWER REQUIRED";
        }
        return "RETURN TO BEACON";
    }

    public List<String> activeFailureStates(
            EchoVoxelPlayerState player,
            EchoAdapterCoreShelterProfile shelterProfile,
            EchoAdapterCoreSurvivalProfile survivalProfile
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(shelterProfile, "shelterProfile");
        Objects.requireNonNull(survivalProfile, "survivalProfile");
        ArrayList<String> states = new ArrayList<>();
        if (playerHealth <= 0) {
            states.add("downed");
        }
        if (hydration <= survivalProfile.dehydrationDamageThreshold()) {
            states.add("dehydration");
        }
        if (hunger <= survivalProfile.starvationDamageThreshold()) {
            states.add("starvation");
        }
        if (ashExposure >= survivalProfile.ashExposureDamageThreshold()) {
            states.add("ash_exposure");
        }
        if (extractionStormExposureSeconds > 0.0D && !sheltered(player, shelterProfile)) {
            states.add("no_shelter_during_storm");
        }
        if (powerNodeDiscovered && !powerRepairStarted && repairKits <= 0) {
            states.add("missing_repair_item");
        }
        if (scannerUsed && !terminalOnline) {
            states.add("terminal_offline");
        }
        if (lastMessage.startsWith("extraction denied:")) {
            states.add("extraction_attempted_too_early");
        }
        return List.copyOf(states);
    }

    public List<String> recoveryOptions() {
        ArrayList<String> options = new ArrayList<>();
        if (playerHealth <= 0) {
            options.add("retry_from_checkpoint");
            options.add("reload_save");
            return List.copyOf(options);
        }
        if (waterRations > 0 || foodRations > 0) {
            options.add("consume_supplies");
        }
        if (shelterBuilt && shelterIntegrity > 0.0D) {
            options.add("retreat_to_shelter");
        }
        if (!powerRepaired && (repairKits <= 0 || !wastelandFieldKitAssembled)) {
            options.add("repair_or_craft_missing_item");
        }
        options.add("reload_save");
        return List.copyOf(options);
    }

    public String hudObjectiveState() {
        return status()
                + "|objective=" + nextObjective()
                + "|hint=" + currentHint()
                + "|terminal=" + terminalState()
                + "|extraction=" + extractionStatus()
                + "|shelter=" + shelterStatus();
    }

    public List<String> requiredObjectiveLabels() {
        return List.of(
                "Track crash beacon",
                "Read field manual",
                "Place shelter anchor",
                "Scan the crash site",
                "Bring field terminal online",
                "Recover crash cache",
                "Clear ash hazard",
                "Craft scrap knife",
                "Mine with scrap knife",
                "Build hand recycler",
                "Make machine casing",
                "Assemble wasteland field kit",
                "Build micro generator",
                "Route power cable",
                "Install energy meter",
                "Build scrap dynamo",
                "Charge energy cell",
                "Build battery bank",
                "Build thermal burner",
                "Equip gas mask",
                "Find schematic fragment",
                "Decode first schematic",
                "Build scrap press",
                "Install item pipe",
                "Build factory controller",
                "Build research lab",
                "Upgrade power cable",
                "Set power priority",
                "Overclock machine",
                "Fix mask filter",
                "Craft advanced filter",
                "Build thermal array",
                "Warm up after exposure",
                "Build atmospheric scrubber",
                "Build radiation cleanser",
                "Build field med bay",
                "Use field med bay",
                "Build filter workbench",
                "Build ore grinder",
                "Recover dense alloy",
                "Build isotope refiner",
                "Forge alloy weapon",
                "Equip alloy kit",
                "Activate relay station",
                "Build scout drone",
                "Use rad away",
                "Use stim pack",
                "Use hand warmer",
                "Install thermal liner",
                "Place return beacon",
                "Bind return keystone",
                "Secure dirty-water loop",
                "Forage food buffer",
                "Build rain collector",
                "Stockpile rations",
                "Build water purifier",
                "Stockpile clean water",
                "Repair power node",
                "Return to extraction beacon"
        );
    }

    public List<String> optionalObjectiveLabels() {
        return List.of(
                "Use water ration",
                "Use field ration",
                "Cross toxic ash safely",
                "Scavenge survival supplies"
        );
    }

    public List<String> completedHistory() {
        ArrayList<String> history = new ArrayList<>();
        if (beaconTracked) {
            history.add("Crash beacon tracked");
        }
        if (fieldManualRead) {
            history.add("Field manual read");
        }
        if (shelterBuilt) {
            history.add("Shelter anchor placed");
        }
        if (scannerUsed) {
            history.add("Emergency scanner used");
        }
        if (terminalOnline) {
            history.add("Field terminal online");
        }
        if (waterUsed) {
            history.add("Water ration consumed");
        }
        if (foodUsed) {
            history.add("Field ration consumed");
        }
        if (crossedAsh) {
            history.add("Toxic ash crossed");
        }
        if (hazardCleared) {
            history.add("Ash hazard cleared");
        }
        if (scavengedSupplies) {
            history.add("Survival supplies scavenged");
        }
        if (scrapKnifeCrafted) {
            history.add("Scrap knife crafted");
        }
        if (toolAssistedMining) {
            history.add("Debris mined with scrap knife");
        }
        if (handRecyclerBuilt) {
            history.add("Hand recycler built");
        }
        if (machineCasingMade) {
            history.add("Machine casing made");
        }
        if (wastelandFieldKitAssembled) {
            history.add("Wasteland field kit assembled");
        }
        if (microGeneratorBuilt) {
            history.add("Micro generator built");
        }
        if (powerCableRouted) {
            history.add("Power cable routed");
        }
        if (energyMeterInstalled) {
            history.add("Energy meter installed");
        }
        if (scrapDynamoBuilt) {
            history.add("Scrap dynamo built");
        }
        if (basicBatteryCharged) {
            history.add("Energy cell charged");
        }
        if (batteryBankBuilt) {
            history.add("Battery bank built");
        }
        if (thermalBurnerBuilt) {
            history.add("Thermal burner built");
        }
        if (gasMaskEquipped) {
            history.add("Gas mask equipped");
        }
        if (schematicFragmentFound) {
            history.add("Schematic fragment recovered");
        }
        if (firstSchematicDecoded) {
            history.add("First schematic decoded");
        }
        if (scrapPressBuilt) {
            history.add("Scrap press built");
        }
        if (itemPipeInstalled) {
            history.add("Item pipe installed");
        }
        if (factoryControllerBuilt) {
            history.add("Factory controller built");
        }
        if (researchLabBuilt) {
            history.add("Research lab built");
        }
        if (powerCableUpgraded) {
            history.add("Power cable upgraded");
        }
        if (powerPrioritySet) {
            history.add("Power priority set");
        }
        if (machineOverclocked) {
            history.add("Machine overclocked");
        }
        if (basicFilterFixed) {
            history.add("Mask filter fixed");
        }
        if (advancedFilterCrafted) {
            history.add("Advanced filter crafted");
        }
        if (thermalArrayBuilt) {
            history.add("Thermal array built");
        }
        if (warmedAfterExposure) {
            history.add("Warmed after exposure");
        }
        if (atmosphericScrubberBuilt) {
            history.add("Atmospheric scrubber built");
        }
        if (radiationCleanserBuilt) {
            history.add("Radiation cleanser built");
        }
        if (fieldMedBayBuilt) {
            history.add("Field med bay built");
        }
        if (fieldMedBayUsed) {
            history.add("Field med bay used");
        }
        if (filterWorkbenchBuilt) {
            history.add("Filter workbench built");
        }
        if (oreGrinderBuilt) {
            history.add("Ore grinder built");
        }
        if (denseAlloyFound) {
            history.add("Dense alloy recovered");
        }
        if (isotopeRefinerBuilt) {
            history.add("Isotope refiner built");
        }
        if (alloyWeaponForged) {
            history.add("Alloy weapon forged");
        }
        if (alloyKitEquipped) {
            history.add("Alloy kit equipped");
        }
        if (relayStationActivated) {
            history.add("Relay station activated");
        }
        if (scoutDroneBuilt) {
            history.add("Scout drone built");
        }
        if (radAwayUsed) {
            history.add("Rad away used");
        }
        if (stimPackUsed) {
            history.add("Stim pack used");
        }
        if (handWarmerUsed) {
            history.add("Hand warmer used");
        }
        if (thermalLinerInstalled) {
            history.add("Thermal liner installed");
        }
        if (returnBeaconPlaced) {
            history.add("Return beacon placed");
        }
        if (returnKeystoneBound) {
            history.add("Return keystone bound");
        }
        if (emergencyWaterLoopSecured) {
            history.add("Emergency water loop secured");
        }
        if (foragedFood) {
            history.add("Wasteland food foraged");
        }
        if (rainCollectorBuilt) {
            history.add("Rain collector built");
        }
        if (rationsStockpiled) {
            history.add("Rations stockpiled");
        }
        if (waterPurifierBuilt) {
            history.add("Water purifier built");
        }
        if (cleanWaterStockpiled) {
            history.add("Clean water stockpiled");
        }
        if (cacheRecovered) {
            history.add("Crash cache recovered");
        }
        if (powerRepaired) {
            history.add("Power node repaired");
        }
        if (extracted) {
            history.add("Extraction beacon secured");
        }
        return List.copyOf(history);
    }

    public List<String> terminalNotes() {
        ArrayList<String> notes = new ArrayList<>();
        notes.add("Terminal state: " + terminalState());
        notes.add("Extraction: " + extractionStatus());
        if (!scannerUsed) {
            notes.add("Signal locked until scanner route ping");
        } else if (!terminalOnline) {
            notes.add("Terminal casing damaged; interact to boot diagnostics");
        } else if (!cacheRecovered) {
            notes.add("Cache coordinates: east ridge, rusted debris field");
        } else if (!powerRepaired) {
            if (!powerNodeDiscovered) {
                notes.add("Power diagnostics: damaged node inspection required");
            } else if (!powerRepairStarted) {
                notes.add("Power diagnostics: repair kit required at damaged node");
            } else if (!powerRebootStabilized()) {
                notes.add("Power diagnostics: reboot "
                        + String.format("%.1f", powerRebootSeconds)
                        + "/"
                        + String.format("%.1f", POWER_REBOOT_REQUIRED_SECONDS)
                        + "s");
            } else {
                notes.add("Power diagnostics: terminal confirmation required");
            }
        } else {
            notes.add("Beacon uplink ready at crash origin");
        }
        notes.add("Hint: " + currentHint());
        return List.copyOf(notes);
    }

    public List<String> feed() {
        return List.copyOf(feed);
    }

    public String objectiveSummary(int index) {
        return switch (index) {
            case 0 -> flag(beaconTracked, "beacon");
            case 1 -> flag(fieldManualRead, "field_manual");
            case 2 -> flag(shelterBuilt, "shelter");
            case 3 -> flag(scannerUsed, "scanner");
            case 4 -> flag(terminalOnline, "terminal");
            case 5 -> flag(waterUsed, "water:" + waterRations);
            case 6 -> flag(foodUsed, "food:" + foodRations);
            case 7 -> flag(crossedAsh, "ash:" + String.format("%.1f", ashExposure));
            case 8 -> flag(hazardCleared, "hazard");
            case 9 -> flag(scavengedSupplies, "scavenge:" + scavengedSupplyCaches);
            case 10 -> flag(scrapKnifeCrafted, "scrap_knife:" + scrapKnifeDurability);
            case 11 -> flag(toolAssistedMining, "tool_mining:" + toolMiningBlocksBroken);
            case 12 -> flag(handRecyclerBuilt, "hand_recycler");
            case 13 -> flag(machineCasingMade, "machine_casing:" + machineCasings);
            case 14 -> flag(wastelandFieldKitAssembled, "field_kit");
            case 15 -> flag(microGeneratorBuilt, "micro_generator:" + fieldPowerGenerated + "W");
            case 16 -> flag(powerCableRouted, "power_cable:" + powerCableSegments);
            case 17 -> flag(energyMeterInstalled, "energy_meter:" + energyMeterReadings);
            case 18 -> flag(scrapDynamoBuilt, "scrap_dynamo:" + machinePowerGenerated + "W");
            case 19 -> flag(basicBatteryCharged, "energy_cell:" + storedEnergyCells);
            case 20 -> flag(batteryBankBuilt, "battery_bank:" + storedEnergyCells);
            case 21 -> flag(thermalBurnerBuilt, "thermal_burner:" + thermalBurnerHeat);
            case 22 -> flag(gasMaskEquipped, "gas_mask");
            case 23 -> flag(schematicFragmentFound, "schematic:" + schematicFragments);
            case 24 -> flag(firstSchematicDecoded, "first_schematic");
            case 25 -> flag(scrapPressBuilt, "scrap_press");
            case 26 -> flag(itemPipeInstalled, "item_pipe:" + itemPipeSegments);
            case 27 -> flag(factoryControllerBuilt, "factory_controller");
            case 28 -> flag(researchLabBuilt, "research_lab");
            case 29 -> flag(powerCableUpgraded, "reinforced_cable:" + upgradedPowerCableSegments);
            case 30 -> flag(powerPrioritySet, "power_priority");
            case 31 -> flag(machineOverclocked, "overclock:" + overclockHeat);
            case 32 -> flag(basicFilterFixed, "basic_filter:" + basicFilterCharges);
            case 33 -> flag(advancedFilterCrafted, "advanced_filter:" + advancedFilterCharges);
            case 34 -> flag(thermalArrayBuilt, "thermal_array");
            case 35 -> flag(warmedAfterExposure, "warmth:" + warmthRecoveredSeconds);
            case 36 -> flag(atmosphericScrubberBuilt, "scrubber");
            case 37 -> flag(radiationCleanserBuilt, "cleanser:" + radiationCleanserCycles);
            case 38 -> flag(fieldMedBayBuilt, "field_med_bay");
            case 39 -> flag(fieldMedBayUsed, "med_treatment:" + fieldMedBayTreatments);
            case 40 -> flag(filterWorkbenchBuilt, "filter_workbench");
            case 41 -> flag(oreGrinderBuilt, "ore_grinder");
            case 42 -> flag(denseAlloyFound, "dense_alloy:" + denseAlloyChunks);
            case 43 -> flag(isotopeRefinerBuilt, "isotope_refiner");
            case 44 -> flag(alloyWeaponForged, "alloy_weapon:" + alloyWeaponDurability);
            case 45 -> flag(alloyKitEquipped, "alloy_kit");
            case 46 -> flag(relayStationActivated, "relay:" + relaySignalStrength);
            case 47 -> flag(scoutDroneBuilt, "scout_drone:" + scoutDroneRangeMeters);
            case 48 -> flag(radAwayUsed, "rad_away:" + radAwayDoses);
            case 49 -> flag(stimPackUsed, "stim_pack:" + stimPackDoses);
            case 50 -> flag(handWarmerUsed, "hand_warmer:" + handWarmerCharges);
            case 51 -> flag(thermalLinerInstalled, "thermal_liner:" + thermalLinerWarmthSeconds);
            case 52 -> flag(returnBeaconPlaced, "return_beacon:" + returnBeaconSignalStrength);
            case 53 -> flag(returnKeystoneBound, "return_keystone:" + returnKeystoneCharges);
            case 54 -> flag(emergencyWaterLoopSecured, "dirty_water:" + dirtyWaterBottles);
            case 55 -> flag(foragedFood, "forage:" + foragedFoodBundles);
            case 56 -> flag(rainCollectorBuilt, "rain_collector");
            case 57 -> flag(rationsStockpiled, "rations:" + foodRationStockpile);
            case 58 -> flag(waterPurifierBuilt, "purifier");
            case 59 -> flag(cleanWaterStockpiled, "clean_water:" + cleanWaterStockpile);
            case 60 -> flag(cacheRecovered, "cache");
            case 61 -> flag(powerRepaired, "power");
            case 62 -> flag(extracted, "extraction");
            default -> flag(completedObjectives() == totalObjectives(), status().toLowerCase());
        };
    }

    public String lastMessage() {
        return lastMessage;
    }

    private void addFeed(String message) {
        feed.add(0, message);
        while (feed.size() > 4) {
            feed.remove(feed.size() - 1);
        }
    }

    private void updateWaterLoopStockpiles(EchoAdapterCoreWaterLoopProfile waterLoopProfile) {
        cleanWaterStockpiled = cleanWaterStockpile >= waterLoopProfile.cleanWaterStockpileTarget();
        rationsStockpiled = foodRationStockpile >= waterLoopProfile.rationStockpileTarget();
    }

    private static String flag(boolean complete, String label) {
        return (complete ? "done:" : "open:") + label;
    }

    private static String scavengeKey(String sourceKey, EchoAdapterCoreScavengeReward reward) {
        String normalized = normalizeScavengeKey(sourceKey);
        if (!normalized.isBlank()) {
            return normalized;
        }
        return reward.sourceLiveVoxelId() + "|" + reward.lootTableContentId();
    }

    private static String normalizeScavengeKey(String sourceKey) {
        return sourceKey == null ? "" : sourceKey.trim();
    }

    private static EchoAdapterCoreHazardRule contactHazard(
            EchoVoxelWorld world,
            EchoVoxelPlayerState player,
            EchoAdapterCoreHazardTable hazardTable
    ) {
        EchoVoxelBlock below = world.blockAt(
                (int) Math.floor(player.x()),
                Math.max(0, (int) Math.floor(player.y() - 0.08D)),
                (int) Math.floor(player.z())
        );
        return hazardTable.contactHazardFor(below).orElse(null);
    }

    private void applyContactHazard(EchoAdapterCoreHazardRule hazard, double seconds, double minutes) {
        ashExposure = clamp(ashExposure + hazard.ashExposurePerMinute() * minutes, 0.0D, 100.0D);
        hydration = clamp(hydration - hazard.hydrationDrainPerMinute() * minutes, 0.0D, 100.0D);
        hunger = clamp(hunger - hazard.hungerDrainPerMinute() * minutes, 0.0D, 100.0D);
        if (hazard.hazardContentId().equals(EchoAdapterCoreStandaloneContentBridge.TOXIC_ASH_HAZARD_ID)) {
            toxicAshExposureSeconds += seconds;
        } else if (hazard.hazardContentId().equals(EchoAdapterCoreStandaloneContentBridge.HOT_ASH_HAZARD_ID)) {
            hotAshExposureSeconds += seconds;
        }
        if (hazard.unstableGround() && seconds >= 1.0D) {
            unstableGroundStrikes++;
            playerHealth = Math.max(0, playerHealth - hazard.healthDamagePerPulse());
            lastMessage = "unstable ground shifted underfoot";
        } else if (hazard.electricalDischarge() && seconds >= 1.0D) {
            electricalDischargeHits++;
            playerHealth = Math.max(0, playerHealth - hazard.healthDamagePerPulse());
            lastMessage = "electrical discharge from damaged power node";
        } else if (hazard.ashExposurePerMinute() > 0.0D) {
            lastMessage = "hazard exposure: " + hazard.label();
        }
        if (!crossedAsh && ashExposure >= 0.2D) {
            crossedAsh = true;
            addFeed("Crossed " + hazard.label());
        }
    }

    private void applyShelterRest(EchoAdapterCoreShelterProfile shelterProfile, double seconds, double minutes) {
        shelterRestSeconds += seconds;
        ashExposure = clamp(
                ashExposure - shelterProfile.ashRecoveryPerMinute() * minutes,
                0.0D,
                100.0D
        );
        if (shelterRestSeconds >= 30.0D && hydration > 20.0D && hunger > 15.0D) {
            playerHealth = Math.min(
                    100,
                    playerHealth + (int) Math.floor(shelterProfile.restHealthRecoveryPerMinute() * minutes)
            );
        }
    }

    private void applySurvivalDamage(EchoAdapterCoreSurvivalProfile survivalProfile) {
        boolean dehydrating = hydration <= survivalProfile.dehydrationDamageThreshold();
        boolean starving = hunger <= survivalProfile.starvationDamageThreshold();
        boolean ashCritical = ashExposure >= survivalProfile.ashExposureDamageThreshold();
        if (!dehydrating && !starving && !ashCritical) {
            return;
        }
        if (dehydrating) {
            dehydrationDamagePulses++;
        }
        if (starving) {
            starvationDamagePulses++;
        }
        playerHealth = Math.max(0, playerHealth - survivalProfile.deprivationHealthDamagePerPulse());
    }

    private void applyExtractionStorm(
            EchoAdapterCoreHazardRule hazard,
            EchoAdapterCoreShelterProfile shelterProfile,
            boolean sheltered,
            double seconds,
            double minutes
    ) {
        extractionStormExposureSeconds += seconds;
        ashExposure = clamp(
                ashExposure + hazard.ashExposurePerMinute() * minutes,
                0.0D,
                100.0D
        );
        if (sheltered) {
            double damage = shelterProfile.stormIntegrityDamagePerMinute() * minutes;
            shelterStormDamage += damage;
            shelterIntegrity = clamp(shelterIntegrity - damage, 0.0D, 100.0D);
            if (shelterIntegrity <= 0.0D) {
                lastMessage = "shelter integrity failed under extraction storm";
            }
        }
        if (seconds >= 1.0D && hazard.healthDamagePerPulse() > 0 && ashExposure >= 85.0D) {
            playerHealth = Math.max(0, playerHealth - hazard.healthDamagePerPulse());
        }
    }

    private boolean sheltered(EchoVoxelPlayerState player, EchoAdapterCoreShelterProfile shelterProfile) {
        return shelterBuilt
                && shelterIntegrity > 0.0D
                && distance(player.x(), player.z(), shelterX + 0.5D, shelterZ + 0.5D) <= shelterProfile.radius();
    }

    private static boolean nearExtractionBeacon(EchoVoxelPlayerState player) {
        return distance(player.x(), player.z(), 7.5D, 1.5D) < 2.0D;
    }

    private static ScanTarget nearestMissionTarget(EchoVoxelWorld world, EchoVoxelPlayerState player, int radius) {
        ScanTarget nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        int centerX = (int) Math.floor(player.x());
        int centerY = (int) Math.floor(player.y());
        int centerZ = (int) Math.floor(player.z());
        for (int y = Math.max(0, centerY - 4); y <= centerY + 5; y++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                for (int x = centerX - radius; x <= centerX + radius; x++) {
                    EchoVoxelBlock block = world.blockAt(x, y, z);
                    String label = missionTargetLabel(block);
                    if (label.isBlank()) {
                        continue;
                    }
                    double distance = Math.hypot(player.x() - (x + 0.5D), player.z() - (z + 0.5D));
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = new ScanTarget(label, x, y, z);
                    }
                }
            }
        }
        return nearest;
    }

    private static String missionTargetLabel(EchoVoxelBlock block) {
        String id = block.id();
        if (isTerminal(id)) {
            return "terminal";
        }
        if (isCache(id)) {
            return "cache";
        }
        if (isHazardTarget(id)) {
            return "hazard";
        }
        if (isPowerNode(id)) {
            return "power";
        }
        return "";
    }

    private static boolean isTerminal(String id) {
        return id.contains("field_terminal") || id.contains("echo_terminal");
    }

    private static boolean isCache(String id) {
        return id.contains("crash_cache") || id.contains("echo_cache") || id.contains("structure_cache");
    }

    private static boolean isPowerNode(String id) {
        return id.contains("damaged_power_node") || id.contains("power_node");
    }

    private static boolean isShelterAnchor(String id) {
        return id.contains("shelter_anchor") || id.contains("ash_campfire");
    }

    private static boolean isHazardTarget(String id) {
        return id.contains("ash_hazard") || id.contains("toxic_waste_barrel") || id.contains("toxic_puddle");
    }

    private static boolean isAshExposureBlock(String id) {
        return id.contains("toxic_ash")
                || id.contains("fallout_dust")
                || id.contains("ash_hazard")
                || id.contains("toxic_waste_barrel")
                || id.contains("toxic_puddle");
    }

    private static double distance(double leftX, double leftZ, double rightX, double rightZ) {
        return Math.hypot(leftX - rightX, leftZ - rightZ);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private record ScanTarget(String label, int x, int y, int z) {
    }

    public record ScavengeReward(
            boolean rewarded,
            boolean waterRation,
            boolean foodRation,
            int repairKits,
            String message,
            String adapterCoreLootTableId
    ) {
        public ScavengeReward {
            message = message == null || message.isBlank() ? "scavenge: no reward" : message.trim();
            adapterCoreLootTableId = adapterCoreLootTableId == null ? "" : adapterCoreLootTableId.trim();
            repairKits = Math.max(0, repairKits);
        }

        static ScavengeReward none(String message) {
            return new ScavengeReward(false, false, false, 0, message, "");
        }

        public boolean adapterCoreBacked() {
            return !adapterCoreLootTableId.isBlank();
        }
    }
}
