package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneRegistry;
import dev.echo.standalone.runtime.compat.EchoAshfallGameplayContracts;
import dev.echo.standalone.runtime.player.EchoVoxelHotbarSlot;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

final class EchoStandalonePlayableVoxelSaveCodec {
    static final String WORLD_EDITS_PATH = "playable/world-edits.tsv";
    static final String PLAYER_PATH = "playable/player.properties";
    static final String HOTBAR_PATH = "playable/hotbar.tsv";
    static final String MISSION_PATH = "playable/mission.properties";
    static final String RENDER_PATH = "playable/render.properties";
    static final String CONTRACT_ID_KEY = "adapterCoreContractId";
    static final String CONTRACT_SCHEMA_KEY = "adapterCoreSaveSchema";
    static final String CONTRACT_VERSION_KEY = "adapterCoreSaveVersion";

    private static final long ASHFALL_SEED = 42L;

    private EchoStandalonePlayableVoxelSaveCodec() {
    }

    static EchoSaveCommitResult writeSession(
            EchoSaveRuntimeResult save,
            String slotId,
            String transactionId,
            EchoStandalonePlayableVoxelSession session,
            Map<String, String> metadata
    ) throws IOException {
        Objects.requireNonNull(session, "session");
        return writeSnapshot(
                save,
                slotId,
                transactionId,
                session.player(),
                session.hotbar(),
                session.mission(),
                session.edits(),
                renderText(session.result()),
                metadata
        );
    }

    static EchoSaveCommitResult writeLiveSnapshot(
            EchoSaveRuntimeResult save,
            String slotId,
            String transactionId,
            EchoVoxelPlayerState player,
            EchoVoxelPlayerHotbar hotbar,
            EchoAshfallLiveMissionState mission,
            List<EchoStandalonePlayableVoxelEdit> edits,
            EchoVoxelFramebuffer frame,
            Map<String, String> metadata
    ) throws IOException {
        Objects.requireNonNull(frame, "frame");
        return writeSnapshot(
                save,
                slotId,
                transactionId,
                player,
                hotbar,
                mission,
                edits,
                liveRenderText(frame),
                metadata
        );
    }

    static EchoStandalonePlayableVoxelSaveSnapshot restoreSnapshot(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoSaveRuntimeResult save,
            EchoSaveManifest manifest
    ) throws IOException {
        Objects.requireNonNull(bridge, "bridge");
        Objects.requireNonNull(save, "save");
        Objects.requireNonNull(manifest, "manifest");
        Path dataRoot = save.profile().slot(manifest.slotId()).dataRoot();
        EchoAdapterCoreStandaloneRegistry registry = bridge.registry();
        dev.echo.standalone.runtime.player.EchoVoxelSessionRuntimeProfile sessionProfile =
                dev.echo.standalone.runtime.player.EchoVoxelSessionProfiles.ashfallCrashSite(
                        registry::requireLiveVoxelBlock,
                        registry.requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.RUNTIME_MARKER_BLOCK_ID),
                        1
                );
        EchoVoxelWorld world = sessionProfile.generate(ASHFALL_SEED, 0);
        Map<String, String> playerValues = properties(Files.readString(dataRoot.resolve(PLAYER_PATH)));
        EchoVoxelPlayerState player = player(playerValues);
        world = sessionProfile.streamer().streamAround(world, player.x(), player.z());
        List<EchoStandalonePlayableVoxelEdit> edits = worldEdits(Files.readAllLines(dataRoot.resolve(WORLD_EDITS_PATH)));
        applyWorldEdits(registry, world, edits);
        EchoVoxelPlayerHotbar hotbar = hotbar(
                registry,
                player.selectedSlot(),
                Files.readAllLines(dataRoot.resolve(HOTBAR_PATH))
        );
        EchoAshfallLiveMissionState mission = mission(properties(Files.readString(dataRoot.resolve(MISSION_PATH))));
        return new EchoStandalonePlayableVoxelSaveSnapshot(world, player, hotbar, mission, edits);
    }

    static boolean manifestTracksGameplayFiles(EchoSaveManifest manifest) {
        return manifest.file(WORLD_EDITS_PATH).isPresent()
                && manifest.file(PLAYER_PATH).isPresent()
                && manifest.file(HOTBAR_PATH).isPresent()
                && manifest.file(MISSION_PATH).isPresent()
                && manifest.file(RENDER_PATH).isPresent();
    }

    private static EchoSaveCommitResult writeSnapshot(
            EchoSaveRuntimeResult save,
            String slotId,
            String transactionId,
            EchoVoxelPlayerState player,
            EchoVoxelPlayerHotbar hotbar,
            EchoAshfallLiveMissionState mission,
            List<EchoStandalonePlayableVoxelEdit> edits,
            String renderText,
            Map<String, String> metadata
    ) throws IOException {
        Objects.requireNonNull(save, "save");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(hotbar, "hotbar");
        Objects.requireNonNull(mission, "mission");
        Objects.requireNonNull(edits, "edits");
        TreeMap<String, String> mergedMetadata = new TreeMap<>();
        mergedMetadata.putAll(metadata == null ? Map.of() : metadata);
        mergedMetadata.put(CONTRACT_ID_KEY, EchoAshfallGameplayContracts.LIVE_MISSION_STATE_CONTRACT_ID);
        mergedMetadata.put(CONTRACT_SCHEMA_KEY, EchoAshfallGameplayContracts.ASHFALL_SAVE_SCHEMA_ID);
        mergedMetadata.put(CONTRACT_VERSION_KEY, Integer.toString(EchoAshfallGameplayContracts.CURRENT_SAVE_VERSION));
        EchoSaveTransaction transaction = save.beginTransaction(slotId, transactionId);
        transaction.writeText(WORLD_EDITS_PATH, worldEditsText(edits));
        transaction.writeText(PLAYER_PATH, playerText(player));
        transaction.writeText(HOTBAR_PATH, hotbarText(hotbar));
        transaction.writeText(MISSION_PATH, missionText(mission));
        transaction.writeText(RENDER_PATH, renderText);
        return transaction.commit(mergedMetadata);
    }

    private static String worldEditsText(List<EchoStandalonePlayableVoxelEdit> edits) {
        StringBuilder builder = new StringBuilder("x\ty\tz\tbefore\tafter\n");
        for (EchoStandalonePlayableVoxelEdit edit : edits) {
            builder.append(edit.x()).append('\t')
                    .append(edit.y()).append('\t')
                    .append(edit.z()).append('\t')
                    .append(edit.beforeBlockId()).append('\t')
                    .append(edit.afterBlockId()).append('\n');
        }
        return builder.toString();
    }

    private static String playerText(EchoVoxelPlayerState player) {
        return "x=" + player.x() + "\n"
                + "y=" + player.y() + "\n"
                + "z=" + player.z() + "\n"
                + "velocityY=" + player.velocityY() + "\n"
                + "yawDegrees=" + player.yawDegrees() + "\n"
                + "pitchDegrees=" + player.pitchDegrees() + "\n"
                + "grounded=" + player.grounded() + "\n"
                + "crouching=" + player.crouching() + "\n"
                + "sprinting=" + player.sprinting() + "\n"
                + "selectedSlot=" + player.selectedSlot() + "\n"
                + "reach=" + player.reach() + "\n";
    }

    private static String hotbarText(EchoVoxelPlayerHotbar hotbar) {
        StringBuilder builder = new StringBuilder("slot\tblock\tcount\n");
        for (EchoVoxelHotbarSlot slot : hotbar.slots()) {
            builder.append(slot.index()).append('\t')
                    .append(slot.empty() ? EchoVoxelBlock.AIR.id() : slot.block().id()).append('\t')
                    .append(slot.count()).append('\n');
        }
        return builder.toString();
    }

    private static String missionText(EchoAshfallLiveMissionState mission) {
        return "contractId=" + mission.adapterCoreContractId() + "\n"
                + "contractSchema=" + EchoAshfallGameplayContracts.ASHFALL_SAVE_SCHEMA_ID + "\n"
                + "contractVersion=" + EchoAshfallGameplayContracts.CURRENT_SAVE_VERSION + "\n"
                + "fieldManualRead=" + mission.fieldManualRead() + "\n"
                + "shelterBuilt=" + mission.shelterBuilt() + "\n"
                + "scannerUsed=" + mission.scannerUsed() + "\n"
                + "terminalOnline=" + mission.terminalOnline() + "\n"
                + "waterUsed=" + mission.waterUsed() + "\n"
                + "foodUsed=" + mission.foodUsed() + "\n"
                + "crossedAsh=" + mission.crossedAsh() + "\n"
                + "hazardCleared=" + mission.hazardCleared() + "\n"
                + "scavengedSupplies=" + mission.scavengedSupplies() + "\n"
                + "cacheRecovered=" + mission.cacheRecovered() + "\n"
                + "powerNodeDiscovered=" + mission.powerNodeDiscovered() + "\n"
                + "powerRepairStarted=" + mission.powerRepairStarted() + "\n"
                + "powerTerminalConfirmed=" + mission.powerTerminalConfirmed() + "\n"
                + "powerRepaired=" + mission.powerRepaired() + "\n"
                + "extractionArmed=" + mission.extractionArmed() + "\n"
                + "extracted=" + mission.extracted() + "\n"
                + "rainCollectorBuilt=" + mission.rainCollectorBuilt() + "\n"
                + "waterPurifierBuilt=" + mission.waterPurifierBuilt() + "\n"
                + "emergencyWaterLoopSecured=" + mission.emergencyWaterLoopSecured() + "\n"
                + "foragedFood=" + mission.foragedFood() + "\n"
                + "cleanWaterStockpiled=" + mission.cleanWaterStockpiled() + "\n"
                + "rationsStockpiled=" + mission.rationsStockpiled() + "\n"
                + "scrapKnifeCrafted=" + mission.scrapKnifeCrafted() + "\n"
                + "toolAssistedMining=" + mission.toolAssistedMining() + "\n"
                + "handRecyclerBuilt=" + mission.handRecyclerBuilt() + "\n"
                + "machineCasingMade=" + mission.machineCasingMade() + "\n"
                + "wastelandFieldKitAssembled=" + mission.wastelandFieldKitAssembled() + "\n"
                + "microGeneratorBuilt=" + mission.microGeneratorBuilt() + "\n"
                + "powerCableRouted=" + mission.powerCableRouted() + "\n"
                + "energyMeterInstalled=" + mission.energyMeterInstalled() + "\n"
                + "scrapDynamoBuilt=" + mission.scrapDynamoBuilt() + "\n"
                + "basicBatteryCharged=" + mission.basicBatteryCharged() + "\n"
                + "batteryBankBuilt=" + mission.batteryBankBuilt() + "\n"
                + "thermalBurnerBuilt=" + mission.thermalBurnerBuilt() + "\n"
                + "gasMaskEquipped=" + mission.gasMaskEquipped() + "\n"
                + "schematicFragmentFound=" + mission.schematicFragmentFound() + "\n"
                + "firstSchematicDecoded=" + mission.firstSchematicDecoded() + "\n"
                + "scrapPressBuilt=" + mission.scrapPressBuilt() + "\n"
                + "itemPipeInstalled=" + mission.itemPipeInstalled() + "\n"
                + "factoryControllerBuilt=" + mission.factoryControllerBuilt() + "\n"
                + "researchLabBuilt=" + mission.researchLabBuilt() + "\n"
                + "powerCableUpgraded=" + mission.powerCableUpgraded() + "\n"
                + "powerPrioritySet=" + mission.powerPrioritySet() + "\n"
                + "machineOverclocked=" + mission.machineOverclocked() + "\n"
                + "basicFilterFixed=" + mission.basicFilterFixed() + "\n"
                + "advancedFilterCrafted=" + mission.advancedFilterCrafted() + "\n"
                + "thermalArrayBuilt=" + mission.thermalArrayBuilt() + "\n"
                + "warmedAfterExposure=" + mission.warmedAfterExposure() + "\n"
                + "atmosphericScrubberBuilt=" + mission.atmosphericScrubberBuilt() + "\n"
                + "radiationCleanserBuilt=" + mission.radiationCleanserBuilt() + "\n"
                + "fieldMedBayBuilt=" + mission.fieldMedBayBuilt() + "\n"
                + "fieldMedBayUsed=" + mission.fieldMedBayUsed() + "\n"
                + "filterWorkbenchBuilt=" + mission.filterWorkbenchBuilt() + "\n"
                + "oreGrinderBuilt=" + mission.oreGrinderBuilt() + "\n"
                + "denseAlloyFound=" + mission.denseAlloyFound() + "\n"
                + "isotopeRefinerBuilt=" + mission.isotopeRefinerBuilt() + "\n"
                + "alloyWeaponForged=" + mission.alloyWeaponForged() + "\n"
                + "alloyKitEquipped=" + mission.alloyKitEquipped() + "\n"
                + "relayStationActivated=" + mission.relayStationActivated() + "\n"
                + "scoutDroneBuilt=" + mission.scoutDroneBuilt() + "\n"
                + "radAwayUsed=" + mission.radAwayUsed() + "\n"
                + "stimPackUsed=" + mission.stimPackUsed() + "\n"
                + "handWarmerUsed=" + mission.handWarmerUsed() + "\n"
                + "thermalLinerInstalled=" + mission.thermalLinerInstalled() + "\n"
                + "returnBeaconPlaced=" + mission.returnBeaconPlaced() + "\n"
                + "returnKeystoneBound=" + mission.returnKeystoneBound() + "\n"
                + "waterRations=" + mission.waterRations() + "\n"
                + "foodRations=" + mission.foodRations() + "\n"
                + "repairKits=" + mission.repairKits() + "\n"
                + "scavengedSupplyCaches=" + mission.scavengedSupplyCaches() + "\n"
                + "scrapMetal=" + mission.scrapMetal() + "\n"
                + "scrapWire=" + mission.scrapWire() + "\n"
                + "scrapCircuit=" + mission.scrapCircuit() + "\n"
                + "machineCasings=" + mission.machineCasings() + "\n"
                + "fieldPowerGenerated=" + mission.fieldPowerGenerated() + "\n"
                + "powerCableSegments=" + mission.powerCableSegments() + "\n"
                + "energyMeterReadings=" + mission.energyMeterReadings() + "\n"
                + "machinePowerGenerated=" + mission.machinePowerGenerated() + "\n"
                + "storedEnergyCells=" + mission.storedEnergyCells() + "\n"
                + "thermalBurnerHeat=" + mission.thermalBurnerHeat() + "\n"
                + "schematicFragments=" + mission.schematicFragments() + "\n"
                + "itemPipeSegments=" + mission.itemPipeSegments() + "\n"
                + "upgradedPowerCableSegments=" + mission.upgradedPowerCableSegments() + "\n"
                + "overclockHeat=" + mission.overclockHeat() + "\n"
                + "basicFilterCharges=" + mission.basicFilterCharges() + "\n"
                + "advancedFilterCharges=" + mission.advancedFilterCharges() + "\n"
                + "warmthRecoveredSeconds=" + mission.warmthRecoveredSeconds() + "\n"
                + "radiationCleanserCycles=" + mission.radiationCleanserCycles() + "\n"
                + "fieldMedBayTreatments=" + mission.fieldMedBayTreatments() + "\n"
                + "denseAlloyChunks=" + mission.denseAlloyChunks() + "\n"
                + "alloyWeaponDurability=" + mission.alloyWeaponDurability() + "\n"
                + "relaySignalStrength=" + mission.relaySignalStrength() + "\n"
                + "scoutDroneRangeMeters=" + mission.scoutDroneRangeMeters() + "\n"
                + "radAwayDoses=" + mission.radAwayDoses() + "\n"
                + "stimPackDoses=" + mission.stimPackDoses() + "\n"
                + "handWarmerCharges=" + mission.handWarmerCharges() + "\n"
                + "thermalLinerWarmthSeconds=" + mission.thermalLinerWarmthSeconds() + "\n"
                + "returnBeaconSignalStrength=" + mission.returnBeaconSignalStrength() + "\n"
                + "returnKeystoneCharges=" + mission.returnKeystoneCharges() + "\n"
                + "scrapKnifeDurability=" + mission.scrapKnifeDurability() + "\n"
                + "toolMiningBlocksBroken=" + mission.toolMiningBlocksBroken() + "\n"
                + "dirtyWaterBottles=" + mission.dirtyWaterBottles() + "\n"
                + "filteredWaterBottles=" + mission.filteredWaterBottles() + "\n"
                + "cleanWaterStockpile=" + mission.cleanWaterStockpile() + "\n"
                + "foodRationStockpile=" + mission.foodRationStockpile() + "\n"
                + "rainCollectorCollections=" + mission.rainCollectorCollections() + "\n"
                + "waterPurifierCycles=" + mission.waterPurifierCycles() + "\n"
                + "foragedFoodBundles=" + mission.foragedFoodBundles() + "\n"
                + "scavengedLootKeys=" + joined(mission.scavengedLootKeys()) + "\n"
                + "playerHealth=" + mission.playerHealth() + "\n"
                + "hydration=" + mission.hydration() + "\n"
                + "hunger=" + mission.hunger() + "\n"
                + "ashExposure=" + mission.ashExposure() + "\n"
                + "hydrationRecovered=" + mission.hydrationRecovered() + "\n"
                + "hungerRecovered=" + mission.hungerRecovered() + "\n"
                + "dehydrationDamagePulses=" + mission.dehydrationDamagePulses() + "\n"
                + "starvationDamagePulses=" + mission.starvationDamagePulses() + "\n"
                + "toxicAshExposureSeconds=" + mission.toxicAshExposureSeconds() + "\n"
                + "hotAshExposureSeconds=" + mission.hotAshExposureSeconds() + "\n"
                + "unstableGroundStrikes=" + mission.unstableGroundStrikes() + "\n"
                + "electricalDischargeHits=" + mission.electricalDischargeHits() + "\n"
                + "extractionStormExposureSeconds=" + mission.extractionStormExposureSeconds() + "\n"
                + "shelterIntegrity=" + mission.shelterIntegrity() + "\n"
                + "shelterRestSeconds=" + mission.shelterRestSeconds() + "\n"
                + "shelterStormDamage=" + mission.shelterStormDamage() + "\n"
                + "shelterX=" + mission.shelterX() + "\n"
                + "shelterY=" + mission.shelterY() + "\n"
                + "shelterZ=" + mission.shelterZ() + "\n"
                + "powerRebootSeconds=" + mission.powerRebootSeconds() + "\n"
                + "extractionCountdownSeconds=" + mission.extractionCountdownSeconds() + "\n"
                + "survivalSeconds=" + mission.survivalSeconds() + "\n"
                + "lastMessage=" + mission.lastMessage() + "\n";
    }

    static boolean hasUniquePropertyKeys(String text) {
        LinkedHashMap<String, Boolean> seen = new LinkedHashMap<>();
        for (String line : text.split("\\R")) {
            if (line.isBlank() || !line.contains("=")) {
                continue;
            }
            String key = line.substring(0, line.indexOf('='));
            if (seen.put(key, Boolean.TRUE) != null) {
                return false;
            }
        }
        return true;
    }

    private static String renderText(EchoStandalonePlayableVoxelResult result) {
        return "initialFrameChecksum=" + Long.toUnsignedString(result.initialFrameChecksum()) + "\n"
                + "finalFrameChecksum=" + Long.toUnsignedString(result.finalFrameChecksum()) + "\n"
                + "initialFacesDrawn=" + result.initialFacesDrawn() + "\n"
                + "finalFacesDrawn=" + result.finalFacesDrawn() + "\n";
    }

    private static String liveRenderText(EchoVoxelFramebuffer frame) {
        return "currentFrameChecksum=" + Long.toUnsignedString(frame.checksum()) + "\n"
                + "currentFrameChecksumHex=" + Long.toUnsignedString(frame.checksum(), 16) + "\n"
                + "currentFacesDrawn=" + frame.facesDrawn() + "\n";
    }

    private static Map<String, String> properties(String text) {
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        for (String line : text.split("\\R")) {
            if (line.isBlank() || !line.contains("=")) {
                continue;
            }
            int separator = line.indexOf('=');
            values.put(line.substring(0, separator), line.substring(separator + 1));
        }
        return Map.copyOf(values);
    }

    private static EchoVoxelPlayerState player(Map<String, String> values) {
        return new EchoVoxelPlayerState(
                doubleValue(values, "x"),
                doubleValue(values, "y"),
                doubleValue(values, "z"),
                doubleValue(values, "velocityY"),
                doubleValue(values, "yawDegrees"),
                doubleValue(values, "pitchDegrees"),
                booleanValue(values, "grounded"),
                booleanValue(values, "crouching"),
                booleanValue(values, "sprinting"),
                intValue(values, "selectedSlot"),
                doubleValue(values, "reach")
        );
    }

    private static EchoVoxelPlayerHotbar hotbar(
            EchoAdapterCoreStandaloneRegistry registry,
            int selectedSlot,
            List<String> lines
    ) {
        ArrayList<EchoVoxelHotbarSlot> slots = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("slot\t")) {
                continue;
            }
            String[] parts = line.split("\\t");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid hotbar line: " + line);
            }
            String blockId = parts[1];
            EchoVoxelBlock block = blockId.equals(EchoVoxelBlock.AIR.id())
                    ? EchoVoxelBlock.AIR
                    : registry.requireLiveVoxelBlock(blockId);
            slots.add(new EchoVoxelHotbarSlot(Integer.parseInt(parts[0]), block, Integer.parseInt(parts[2])));
        }
        return new EchoVoxelPlayerHotbar(slots, selectedSlot);
    }

    private static EchoAshfallLiveMissionState mission(Map<String, String> values) {
        validateMissionContract(values);
        return EchoAshfallLiveMissionState.restoredFull(
                booleanValue(values, "shelterBuilt", false),
                booleanValue(values, "scannerUsed", false),
                booleanValue(values, "terminalOnline"),
                booleanValue(values, "waterUsed"),
                booleanValue(values, "foodUsed", false),
                booleanValue(values, "crossedAsh"),
                booleanValue(values, "hazardCleared", false),
                booleanValue(values, "scavengedSupplies", false),
                booleanValue(values, "cacheRecovered"),
                booleanValue(values, "powerNodeDiscovered", booleanValue(values, "powerRepaired")),
                booleanValue(values, "powerRepairStarted", booleanValue(values, "powerRepaired")),
                booleanValue(values, "powerTerminalConfirmed", booleanValue(values, "powerRepaired")),
                booleanValue(values, "powerRepaired"),
                booleanValue(values, "extractionArmed", booleanValue(values, "extracted")),
                booleanValue(values, "extracted"),
                intValue(values, "waterRations"),
                intValue(values, "foodRations", 0),
                intValue(values, "repairKits", 0),
                intValue(values, "scavengedSupplyCaches", 0),
                intValue(values, "playerHealth"),
                doubleValue(values, "hydration"),
                doubleValue(values, "hunger", 64.0D),
                doubleValue(values, "ashExposure"),
                doubleValue(values, "hydrationRecovered", 0.0D),
                doubleValue(values, "hungerRecovered", 0.0D),
                intValue(values, "dehydrationDamagePulses", 0),
                intValue(values, "starvationDamagePulses", 0),
                booleanValue(values, "rainCollectorBuilt", false),
                booleanValue(values, "waterPurifierBuilt", false),
                booleanValue(values, "emergencyWaterLoopSecured", false),
                booleanValue(values, "foragedFood", false),
                booleanValue(values, "cleanWaterStockpiled", false),
                booleanValue(values, "rationsStockpiled", false),
                booleanValue(values, "scrapKnifeCrafted", false),
                booleanValue(values, "toolAssistedMining", false),
                booleanValue(values, "handRecyclerBuilt", false),
                booleanValue(values, "machineCasingMade", false),
                booleanValue(values, "wastelandFieldKitAssembled", false),
                booleanValue(values, "microGeneratorBuilt", false),
                booleanValue(values, "powerCableRouted", false),
                booleanValue(values, "energyMeterInstalled", false),
                booleanValue(values, "scrapDynamoBuilt", false),
                booleanValue(values, "basicBatteryCharged", false),
                booleanValue(values, "batteryBankBuilt", false),
                booleanValue(values, "thermalBurnerBuilt", false),
                booleanValue(values, "gasMaskEquipped", false),
                booleanValue(values, "schematicFragmentFound", false),
                booleanValue(values, "firstSchematicDecoded", false),
                booleanValue(values, "scrapPressBuilt", false),
                booleanValue(values, "itemPipeInstalled", false),
                booleanValue(values, "factoryControllerBuilt", false),
                booleanValue(values, "researchLabBuilt", false),
                booleanValue(values, "powerCableUpgraded", false),
                booleanValue(values, "powerPrioritySet", false),
                booleanValue(values, "machineOverclocked", false),
                booleanValue(values, "basicFilterFixed", false),
                booleanValue(values, "advancedFilterCrafted", false),
                booleanValue(values, "thermalArrayBuilt", false),
                booleanValue(values, "warmedAfterExposure", false),
                booleanValue(values, "atmosphericScrubberBuilt", false),
                booleanValue(values, "radiationCleanserBuilt", false),
                booleanValue(values, "fieldMedBayBuilt", false),
                booleanValue(values, "fieldMedBayUsed", false),
                booleanValue(values, "filterWorkbenchBuilt", false),
                booleanValue(values, "oreGrinderBuilt", false),
                booleanValue(values, "denseAlloyFound", false),
                booleanValue(values, "isotopeRefinerBuilt", false),
                booleanValue(values, "alloyWeaponForged", false),
                booleanValue(values, "alloyKitEquipped", false),
                booleanValue(values, "relayStationActivated", false),
                booleanValue(values, "scoutDroneBuilt", false),
                booleanValue(values, "radAwayUsed", false),
                booleanValue(values, "stimPackUsed", false),
                booleanValue(values, "handWarmerUsed", false),
                booleanValue(values, "thermalLinerInstalled", false),
                booleanValue(values, "returnBeaconPlaced", false),
                booleanValue(values, "returnKeystoneBound", false),
                intValue(values, "scrapMetal", 0),
                intValue(values, "scrapWire", 0),
                intValue(values, "scrapCircuit", 0),
                intValue(values, "machineCasings", 0),
                intValue(values, "fieldPowerGenerated", 0),
                intValue(values, "powerCableSegments", 0),
                intValue(values, "energyMeterReadings", 0),
                intValue(values, "machinePowerGenerated", 0),
                intValue(values, "storedEnergyCells", 0),
                intValue(values, "thermalBurnerHeat", 0),
                intValue(values, "schematicFragments", 0),
                intValue(values, "itemPipeSegments", 0),
                intValue(values, "upgradedPowerCableSegments", 0),
                intValue(values, "overclockHeat", 0),
                intValue(values, "basicFilterCharges", 0),
                intValue(values, "advancedFilterCharges", 0),
                intValue(values, "warmthRecoveredSeconds", 0),
                intValue(values, "radiationCleanserCycles", 0),
                intValue(values, "fieldMedBayTreatments", 0),
                intValue(values, "denseAlloyChunks", 0),
                intValue(values, "alloyWeaponDurability", 0),
                intValue(values, "relaySignalStrength", 0),
                intValue(values, "scoutDroneRangeMeters", 0),
                intValue(values, "radAwayDoses", 0),
                intValue(values, "stimPackDoses", 0),
                intValue(values, "handWarmerCharges", 0),
                intValue(values, "thermalLinerWarmthSeconds", 0),
                intValue(values, "returnBeaconSignalStrength", 0),
                intValue(values, "returnKeystoneCharges", 0),
                intValue(values, "scrapKnifeDurability", 0),
                intValue(values, "toolMiningBlocksBroken", 0),
                intValue(values, "dirtyWaterBottles", 0),
                intValue(values, "filteredWaterBottles", 0),
                intValue(values, "cleanWaterStockpile", 0),
                intValue(values, "foodRationStockpile", 0),
                intValue(values, "rainCollectorCollections", 0),
                intValue(values, "waterPurifierCycles", 0),
                intValue(values, "foragedFoodBundles", 0),
                doubleValue(values, "toxicAshExposureSeconds", 0.0D),
                doubleValue(values, "hotAshExposureSeconds", 0.0D),
                intValue(values, "unstableGroundStrikes", 0),
                intValue(values, "electricalDischargeHits", 0),
                doubleValue(values, "extractionStormExposureSeconds", 0.0D),
                doubleValue(values, "shelterIntegrity", booleanValue(values, "shelterBuilt", false) ? 100.0D : 0.0D),
                doubleValue(values, "shelterRestSeconds", 0.0D),
                doubleValue(values, "shelterStormDamage", 0.0D),
                intValue(values, "shelterX", 0),
                intValue(values, "shelterY", 0),
                intValue(values, "shelterZ", 0),
                doubleValue(values, "powerRebootSeconds", booleanValue(values, "powerRepaired") ? 8.0D : 0.0D),
                doubleValue(values, "extractionCountdownSeconds", booleanValue(values, "extracted") ? 12.0D : 0.0D),
                doubleValue(values, "survivalSeconds", 0.0D),
                values.getOrDefault("lastMessage", "mission restored"),
                splitList(values.getOrDefault("scavengedLootKeys", "")),
                booleanValue(values, "fieldManualRead", false)
        );
    }

    private static void validateMissionContract(Map<String, String> values) {
        String contractId = requireValue(values, "contractId");
        if (!contractId.equals(EchoAshfallGameplayContracts.LIVE_MISSION_STATE_CONTRACT_ID)) {
            throw new IllegalArgumentException("Unsupported Ashfall save contract id: " + contractId);
        }
        String contractSchema = requireValue(values, "contractSchema");
        if (!contractSchema.equals(EchoAshfallGameplayContracts.ASHFALL_SAVE_SCHEMA_ID)) {
            throw new IllegalArgumentException("Unsupported Ashfall save schema: " + contractSchema);
        }
        int contractVersion = intValue(values, "contractVersion");
        if (contractVersion < 1 || contractVersion > EchoAshfallGameplayContracts.CURRENT_SAVE_VERSION) {
            throw new IllegalArgumentException("Unsupported Ashfall save contract version: " + contractVersion);
        }
    }

    private static List<EchoStandalonePlayableVoxelEdit> worldEdits(List<String> lines) {
        ArrayList<EchoStandalonePlayableVoxelEdit> edits = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("x\t")) {
                continue;
            }
            String[] parts = line.split("\\t");
            if (parts.length != 5) {
                throw new IllegalArgumentException("Invalid world edit line: " + line);
            }
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    parts[3],
                    parts[4]
            ));
        }
        return List.copyOf(edits);
    }

    private static void applyWorldEdits(
            EchoAdapterCoreStandaloneRegistry registry,
            EchoVoxelWorld world,
            List<EchoStandalonePlayableVoxelEdit> edits
    ) {
        for (EchoStandalonePlayableVoxelEdit edit : edits) {
            EchoVoxelBlock block = edit.afterBlockId().equals(EchoVoxelBlock.AIR.id())
                    ? EchoVoxelBlock.AIR
                    : registry.requireLiveVoxelBlock(edit.afterBlockId());
            world.setBlockAt(
                    edit.x(),
                    edit.y(),
                    edit.z(),
                    block
            );
        }
    }

    private static int intValue(Map<String, String> values, String key) {
        return Integer.parseInt(requireValue(values, key));
    }

    private static int intValue(Map<String, String> values, String key, int fallback) {
        String value = values.get(key);
        return value == null ? fallback : Integer.parseInt(value);
    }

    private static double doubleValue(Map<String, String> values, String key) {
        return Double.parseDouble(requireValue(values, key));
    }

    private static double doubleValue(Map<String, String> values, String key, double fallback) {
        String value = values.get(key);
        return value == null ? fallback : Double.parseDouble(value);
    }

    private static boolean booleanValue(Map<String, String> values, String key) {
        return Boolean.parseBoolean(requireValue(values, key));
    }

    private static boolean booleanValue(Map<String, String> values, String key, boolean fallback) {
        String value = values.get(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static String requireValue(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing playable save value: " + key);
        }
        return value;
    }

    private static String joined(List<String> values) {
        return String.join(";", values);
    }

    private static List<String> splitList(String value) {
        ArrayList<String> result = new ArrayList<>();
        for (String part : value.split(";")) {
            String trimmed = part.trim();
            if (!trimmed.isBlank()) {
                result.add(trimmed);
            }
        }
        return List.copyOf(result);
    }
}
