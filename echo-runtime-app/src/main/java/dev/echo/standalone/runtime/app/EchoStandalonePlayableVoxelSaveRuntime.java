package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAshfallGameplayContracts;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.player.EchoVoxelHotbarSlot;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
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

public final class EchoStandalonePlayableVoxelSaveRuntime {
    private static final String SLOT_ID = "slot-playable-voxel";
    private static final long ASHFALL_SEED = 42L;

    public EchoStandalonePlayableVoxelSaveResult run(
            EchoAdapterCoreStandaloneContentBridge bridge,
            Path saveRoot
    ) throws IOException {
        Objects.requireNonNull(bridge, "bridge");
        Objects.requireNonNull(saveRoot, "saveRoot");
        EchoStandalonePlayableVoxelSession session = new EchoStandalonePlayableVoxelRuntime().play(bridge);
        EchoSaveRuntimeResult save = openSave(saveRoot);
        EchoSaveCommitResult commit = writeSnapshot(save, session);
        String savedMissionText = Files.readString(
                save.profile().slot(SLOT_ID).dataRoot().resolve(EchoStandalonePlayableVoxelSaveCodec.MISSION_PATH));
        Map<String, String> savedMissionValues = properties(savedMissionText);
        EchoStandalonePlayableVoxelSaveSnapshot restored = restoreSnapshot(bridge, save, commit.manifest());
        EchoVoxelFramebuffer restoredFrame = new EchoVoxelSoftwareRenderer()
                .render(restored.world(), restored.player().camera(), 960, 540);

        boolean restoredWorldEdits = verifyWorldEdits(restored.world(), session.edits());
        boolean restoredPlayer = samePlayer(session.player(), restored.player());
        boolean restoredHotbar = sameHotbar(session.hotbar(), restored.hotbar());
        boolean restoredMission = restored.mission().completedObjectives() == session.mission().completedObjectives()
                && restored.mission().totalObjectives() == session.mission().totalObjectives()
                && restored.mission().extracted() == session.mission().extracted()
                && restored.mission().powerRepaired() == session.mission().powerRepaired()
                && restored.mission().powerNodeDiscovered() == session.mission().powerNodeDiscovered()
                && restored.mission().powerRepairStarted() == session.mission().powerRepairStarted()
                && restored.mission().powerTerminalConfirmed() == session.mission().powerTerminalConfirmed()
                && close(restored.mission().powerRebootSeconds(), session.mission().powerRebootSeconds())
                && restored.mission().extractionArmed() == session.mission().extractionArmed()
                && close(restored.mission().extractionCountdownSeconds(), session.mission().extractionCountdownSeconds())
                && restored.mission().cacheRecovered() == session.mission().cacheRecovered()
                && restored.mission().hazardCleared() == session.mission().hazardCleared()
                && restored.mission().scavengedSupplies() == session.mission().scavengedSupplies()
                && restored.mission().foodUsed() == session.mission().foodUsed()
                && restored.mission().scannerUsed() == session.mission().scannerUsed()
                && restored.mission().fieldManualRead() == session.mission().fieldManualRead()
                && restored.mission().shelterBuilt() == session.mission().shelterBuilt()
                && restored.mission().shelterX() == session.mission().shelterX()
                && restored.mission().shelterY() == session.mission().shelterY()
                && restored.mission().shelterZ() == session.mission().shelterZ()
                && close(restored.mission().survivalSeconds(), session.mission().survivalSeconds())
                && close(restored.mission().toxicAshExposureSeconds(), session.mission().toxicAshExposureSeconds())
                && close(restored.mission().hotAshExposureSeconds(), session.mission().hotAshExposureSeconds())
                && restored.mission().unstableGroundStrikes() == session.mission().unstableGroundStrikes()
                && restored.mission().electricalDischargeHits() == session.mission().electricalDischargeHits()
                && close(restored.mission().extractionStormExposureSeconds(),
                session.mission().extractionStormExposureSeconds())
                && close(restored.mission().shelterIntegrity(), session.mission().shelterIntegrity())
                && close(restored.mission().shelterRestSeconds(), session.mission().shelterRestSeconds())
                && close(restored.mission().shelterStormDamage(), session.mission().shelterStormDamage())
                && close(restored.mission().hydrationRecovered(), session.mission().hydrationRecovered())
                && close(restored.mission().hungerRecovered(), session.mission().hungerRecovered())
                && restored.mission().dehydrationDamagePulses() == session.mission().dehydrationDamagePulses()
                && restored.mission().starvationDamagePulses() == session.mission().starvationDamagePulses()
                && restored.mission().rainCollectorBuilt() == session.mission().rainCollectorBuilt()
                && restored.mission().waterPurifierBuilt() == session.mission().waterPurifierBuilt()
                && restored.mission().emergencyWaterLoopSecured() == session.mission().emergencyWaterLoopSecured()
                && restored.mission().foragedFood() == session.mission().foragedFood()
                && restored.mission().cleanWaterStockpiled() == session.mission().cleanWaterStockpiled()
                && restored.mission().rationsStockpiled() == session.mission().rationsStockpiled()
                && restored.mission().dirtyWaterBottles() == session.mission().dirtyWaterBottles()
                && restored.mission().filteredWaterBottles() == session.mission().filteredWaterBottles()
                && restored.mission().cleanWaterStockpile() == session.mission().cleanWaterStockpile()
                && restored.mission().foodRationStockpile() == session.mission().foodRationStockpile()
                && restored.mission().rainCollectorCollections() == session.mission().rainCollectorCollections()
                && restored.mission().waterPurifierCycles() == session.mission().waterPurifierCycles()
                && restored.mission().foragedFoodBundles() == session.mission().foragedFoodBundles()
                && restored.mission().scrapKnifeCrafted() == session.mission().scrapKnifeCrafted()
                && restored.mission().toolAssistedMining() == session.mission().toolAssistedMining()
                && restored.mission().handRecyclerBuilt() == session.mission().handRecyclerBuilt()
                && restored.mission().machineCasingMade() == session.mission().machineCasingMade()
                && restored.mission().wastelandFieldKitAssembled() == session.mission().wastelandFieldKitAssembled()
                && restored.mission().microGeneratorBuilt() == session.mission().microGeneratorBuilt()
                && restored.mission().powerCableRouted() == session.mission().powerCableRouted()
                && restored.mission().energyMeterInstalled() == session.mission().energyMeterInstalled()
                && restored.mission().scrapDynamoBuilt() == session.mission().scrapDynamoBuilt()
                && restored.mission().basicBatteryCharged() == session.mission().basicBatteryCharged()
                && restored.mission().batteryBankBuilt() == session.mission().batteryBankBuilt()
                && restored.mission().thermalBurnerBuilt() == session.mission().thermalBurnerBuilt()
                && restored.mission().gasMaskEquipped() == session.mission().gasMaskEquipped()
                && restored.mission().schematicFragmentFound() == session.mission().schematicFragmentFound()
                && restored.mission().firstSchematicDecoded() == session.mission().firstSchematicDecoded()
                && restored.mission().scrapPressBuilt() == session.mission().scrapPressBuilt()
                && restored.mission().itemPipeInstalled() == session.mission().itemPipeInstalled()
                && restored.mission().factoryControllerBuilt() == session.mission().factoryControllerBuilt()
                && restored.mission().researchLabBuilt() == session.mission().researchLabBuilt()
                && restored.mission().powerCableUpgraded() == session.mission().powerCableUpgraded()
                && restored.mission().powerPrioritySet() == session.mission().powerPrioritySet()
                && restored.mission().machineOverclocked() == session.mission().machineOverclocked()
                && restored.mission().basicFilterFixed() == session.mission().basicFilterFixed()
                && restored.mission().advancedFilterCrafted() == session.mission().advancedFilterCrafted()
                && restored.mission().thermalArrayBuilt() == session.mission().thermalArrayBuilt()
                && restored.mission().warmedAfterExposure() == session.mission().warmedAfterExposure()
                && restored.mission().atmosphericScrubberBuilt() == session.mission().atmosphericScrubberBuilt()
                && restored.mission().radiationCleanserBuilt() == session.mission().radiationCleanserBuilt()
                && restored.mission().fieldMedBayBuilt() == session.mission().fieldMedBayBuilt()
                && restored.mission().fieldMedBayUsed() == session.mission().fieldMedBayUsed()
                && restored.mission().filterWorkbenchBuilt() == session.mission().filterWorkbenchBuilt()
                && restored.mission().oreGrinderBuilt() == session.mission().oreGrinderBuilt()
                && restored.mission().denseAlloyFound() == session.mission().denseAlloyFound()
                && restored.mission().isotopeRefinerBuilt() == session.mission().isotopeRefinerBuilt()
                && restored.mission().alloyWeaponForged() == session.mission().alloyWeaponForged()
                && restored.mission().alloyKitEquipped() == session.mission().alloyKitEquipped()
                && restored.mission().relayStationActivated() == session.mission().relayStationActivated()
                && restored.mission().scoutDroneBuilt() == session.mission().scoutDroneBuilt()
                && restored.mission().radAwayUsed() == session.mission().radAwayUsed()
                && restored.mission().stimPackUsed() == session.mission().stimPackUsed()
                && restored.mission().handWarmerUsed() == session.mission().handWarmerUsed()
                && restored.mission().thermalLinerInstalled() == session.mission().thermalLinerInstalled()
                && restored.mission().returnBeaconPlaced() == session.mission().returnBeaconPlaced()
                && restored.mission().returnKeystoneBound() == session.mission().returnKeystoneBound()
                && restored.mission().scrapMetal() == session.mission().scrapMetal()
                && restored.mission().scrapWire() == session.mission().scrapWire()
                && restored.mission().scrapCircuit() == session.mission().scrapCircuit()
                && restored.mission().machineCasings() == session.mission().machineCasings()
                && restored.mission().fieldPowerGenerated() == session.mission().fieldPowerGenerated()
                && restored.mission().powerCableSegments() == session.mission().powerCableSegments()
                && restored.mission().energyMeterReadings() == session.mission().energyMeterReadings()
                && restored.mission().machinePowerGenerated() == session.mission().machinePowerGenerated()
                && restored.mission().storedEnergyCells() == session.mission().storedEnergyCells()
                && restored.mission().thermalBurnerHeat() == session.mission().thermalBurnerHeat()
                && restored.mission().schematicFragments() == session.mission().schematicFragments()
                && restored.mission().itemPipeSegments() == session.mission().itemPipeSegments()
                && restored.mission().upgradedPowerCableSegments() == session.mission().upgradedPowerCableSegments()
                && restored.mission().overclockHeat() == session.mission().overclockHeat()
                && restored.mission().basicFilterCharges() == session.mission().basicFilterCharges()
                && restored.mission().advancedFilterCharges() == session.mission().advancedFilterCharges()
                && restored.mission().warmthRecoveredSeconds() == session.mission().warmthRecoveredSeconds()
                && restored.mission().radiationCleanserCycles() == session.mission().radiationCleanserCycles()
                && restored.mission().fieldMedBayTreatments() == session.mission().fieldMedBayTreatments()
                && restored.mission().denseAlloyChunks() == session.mission().denseAlloyChunks()
                && restored.mission().alloyWeaponDurability() == session.mission().alloyWeaponDurability()
                && restored.mission().relaySignalStrength() == session.mission().relaySignalStrength()
                && restored.mission().scoutDroneRangeMeters() == session.mission().scoutDroneRangeMeters()
                && restored.mission().radAwayDoses() == session.mission().radAwayDoses()
                && restored.mission().stimPackDoses() == session.mission().stimPackDoses()
                && restored.mission().handWarmerCharges() == session.mission().handWarmerCharges()
                && restored.mission().thermalLinerWarmthSeconds() == session.mission().thermalLinerWarmthSeconds()
                && restored.mission().returnBeaconSignalStrength() == session.mission().returnBeaconSignalStrength()
                && restored.mission().returnKeystoneCharges() == session.mission().returnKeystoneCharges()
                && restored.mission().scrapKnifeDurability() == session.mission().scrapKnifeDurability()
                && restored.mission().toolMiningBlocksBroken() == session.mission().toolMiningBlocksBroken()
                && restored.mission().scavengedSupplyCaches() == session.mission().scavengedSupplyCaches()
                && restored.mission().scavengedLootKeys().equals(session.mission().scavengedLootKeys())
                && restored.mission().repairKits() == session.mission().repairKits();
        boolean contractBacked = EchoAshfallGameplayContracts.LIVE_MISSION_STATE_CONTRACT_ID.equals(
                commit.manifest().metadata().get(EchoStandalonePlayableVoxelSaveCodec.CONTRACT_ID_KEY))
                && EchoAshfallGameplayContracts.LIVE_MISSION_STATE_CONTRACT_ID.equals(
                savedMissionValues.get("contractId"));
        boolean contractVersioned = Integer.toString(EchoAshfallGameplayContracts.CURRENT_SAVE_VERSION).equals(
                commit.manifest().metadata().get(EchoStandalonePlayableVoxelSaveCodec.CONTRACT_VERSION_KEY))
                && Integer.toString(EchoAshfallGameplayContracts.CURRENT_SAVE_VERSION).equals(
                savedMissionValues.get("contractVersion"));
        boolean restoredContractState = EchoAshfallGameplayContracts.LIVE_MISSION_STATE_CONTRACT_ID.equals(
                restored.mission().adapterCoreContractId());
        boolean uniqueMissionKeys = EchoStandalonePlayableVoxelSaveCodec.hasUniquePropertyKeys(savedMissionText);
        boolean midRouteSaveLoadReady = verifyMidRouteSaveLoad(bridge, saveRoot.resolve("mid-route-checkpoint"));
        boolean restoredRenderChecksum = restoredFrame.checksum() == session.result().finalFrameChecksum();

        List<String> manifestFiles = commit.manifest().files().stream()
                .map(file -> file.relativePath())
                .toList();
        return new EchoStandalonePlayableVoxelSaveResult(
                SLOT_ID,
                commit.filesWritten(),
                manifestFiles,
                commit.manifest().file("playable/world-edits.tsv").isPresent(),
                commit.manifest().file("playable/player.properties").isPresent(),
                commit.manifest().file("playable/hotbar.tsv").isPresent(),
                commit.manifest().file("playable/mission.properties").isPresent(),
                commit.manifest().file("playable/render.properties").isPresent(),
                restoredWorldEdits,
                restoredPlayer,
                restoredHotbar,
                restoredMission,
                contractBacked,
                contractVersioned,
                restoredContractState,
                uniqueMissionKeys,
                midRouteSaveLoadReady,
                restoredRenderChecksum,
                session.result().finalFrameChecksum(),
                restoredFrame.checksum()
        );
    }

    private static boolean verifyMidRouteSaveLoad(
            EchoAdapterCoreStandaloneContentBridge bridge,
            Path saveRoot
    ) throws IOException {
        dev.echo.standalone.runtime.player.EchoVoxelSessionRuntimeProfile sessionProfile =
                dev.echo.standalone.runtime.player.EchoVoxelSessionProfiles.ashfallCrashSite(
                        bridge.registry()::requireLiveVoxelBlock,
                        bridge.runtimeMarkerBlock(),
                        1
                );
        EchoVoxelWorld world = sessionProfile.generate(ASHFALL_SEED, 0);
        world = sessionProfile.streamer().streamAround(world, world.spawnX(), world.spawnZ());
        EchoVoxelPlayerState player = new EchoVoxelPlayerState(
                world.spawnX(),
                world.spawnY(),
                world.spawnZ(),
                0.0D,
                world.spawnYawDegrees(),
                -20.0D,
                true,
                false,
                false,
                2,
                6.0D
        );
        EchoVoxelPlayerHotbar hotbar = sessionProfile.newStarterHotbar();
        hotbar.add(bridge.waterRationItem(), 1);
        hotbar.add(bridge.fieldRationItem(), 1);
        hotbar.add(bridge.powerRepairKitItem(), 1);
        hotbar.select(2);

        EchoAshfallLiveMissionState mission = EchoAshfallLiveMissionState.restored(
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                1,
                1,
                1,
                1,
                88,
                64.0D,
                58.0D,
                14.0D,
                2,
                5,
                2,
                0.0D,
                0.0D,
                780.0D,
                "mid-route checkpoint: cache recovered, power repair next",
                List.of("block:2,4,3")
        );
        EchoVoxelFramebuffer frame = new EchoVoxelSoftwareRenderer().render(world, player.camera(), 960, 540);
        String expectedObjective = mission.currentObjective();
        String expectedHint = mission.currentHint();
        EchoSaveRuntimeResult save = openSave(saveRoot);
        EchoSaveCommitResult commit = EchoStandalonePlayableVoxelSaveCodec.writeLiveSnapshot(
                save,
                SLOT_ID,
                "tx-mid-route-001",
                player,
                hotbar,
                mission,
                List.of(),
                frame,
                Map.of(
                        "route", "mid",
                        EchoStandalonePlayableVoxelSaveCodec.CONTRACT_ID_KEY,
                        EchoAshfallGameplayContracts.LIVE_MISSION_STATE_CONTRACT_ID,
                        EchoStandalonePlayableVoxelSaveCodec.CONTRACT_SCHEMA_KEY,
                        EchoAshfallGameplayContracts.ASHFALL_SAVE_SCHEMA_ID,
                        EchoStandalonePlayableVoxelSaveCodec.CONTRACT_VERSION_KEY,
                        Integer.toString(EchoAshfallGameplayContracts.CURRENT_SAVE_VERSION)
                )
        );
        EchoStandalonePlayableVoxelSaveSnapshot restored = restoreSnapshot(bridge, save, commit.manifest());
        return restored.mission().terminalOnline()
                && restored.mission().cacheRecovered()
                && !restored.mission().powerRepaired()
                && restored.mission().repairKits() == 1
                && restored.mission().scavengedLootKeys().contains("block:2,4,3")
                && restored.hotbar().selectedSlot() == 2
                && samePlayer(player, restored.player())
                && restored.mission().currentObjective().equals(expectedObjective)
                && restored.mission().currentHint().equals(expectedHint);
    }

    private static EchoSaveRuntimeResult openSave(Path saveRoot) throws IOException {
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "profile-playable-voxel",
                "Playable Voxel Beta Smoke",
                "echoashfallprotocol:standalone_beta",
                1,
                saveRoot,
                Map.of("runtime", "standalone", "mode", "playable_voxel")
        );
        return new EchoSaveRuntime().open(new EchoDefaultRuntimeServiceRegistry(), profile);
    }

    private static EchoSaveCommitResult writeSnapshot(
            EchoSaveRuntimeResult save,
            EchoStandalonePlayableVoxelSession session
    ) throws IOException {
        return EchoStandalonePlayableVoxelSaveCodec.writeSession(save, SLOT_ID, "tx-playable-voxel-001", session, Map.of(
                "saveKind", "manual",
                "runtime", "standalone",
                "scenario", "playable_voxel_loop"
        ));
    }

    private static EchoStandalonePlayableVoxelSaveSnapshot restoreSnapshot(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoSaveRuntimeResult save,
            EchoSaveManifest manifest
    ) throws IOException {
        return EchoStandalonePlayableVoxelSaveCodec.restoreSnapshot(bridge, save, manifest);
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





    private static boolean verifyWorldEdits(EchoVoxelWorld world, List<EchoStandalonePlayableVoxelEdit> edits) {
        for (EchoStandalonePlayableVoxelEdit edit : edits) {
            if (!world.blockAt(edit.x(), edit.y(), edit.z()).id().equals(edit.afterBlockId())) {
                return false;
            }
        }
        return !edits.isEmpty();
    }

    private static boolean samePlayer(EchoVoxelPlayerState expected, EchoVoxelPlayerState actual) {
        return close(expected.x(), actual.x())
                && close(expected.y(), actual.y())
                && close(expected.z(), actual.z())
                && close(expected.velocityY(), actual.velocityY())
                && close(expected.yawDegrees(), actual.yawDegrees())
                && close(expected.pitchDegrees(), actual.pitchDegrees())
                && expected.grounded() == actual.grounded()
                && expected.crouching() == actual.crouching()
                && expected.sprinting() == actual.sprinting()
                && expected.selectedSlot() == actual.selectedSlot()
                && close(expected.reach(), actual.reach());
    }

    private static boolean sameHotbar(EchoVoxelPlayerHotbar expected, EchoVoxelPlayerHotbar actual) {
        if (expected.selectedSlot() != actual.selectedSlot()) {
            return false;
        }
        for (int index = 0; index < EchoVoxelPlayerHotbar.SLOT_COUNT; index++) {
            EchoVoxelHotbarSlot left = expected.slot(index);
            EchoVoxelHotbarSlot right = actual.slot(index);
            if (!left.block().id().equals(right.block().id()) || left.count() != right.count()) {
                return false;
            }
        }
        return true;
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

    private static boolean close(double left, double right) {
        return Math.abs(left - right) < 0.000001D;
    }

}
