package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.player.EchoVoxelHotbarMutation;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerController;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerStep;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockBreakResult;
import dev.echo.standalone.runtime.world.EchoVoxelHit;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;
import dev.echo.standalone.runtime.world.EchoVoxelWorldStreamer;

import java.util.ArrayList;
import java.util.Objects;

public final class EchoStandalonePlayableVoxelRuntime {
    private static final long ASHFALL_SEED = 42L;
    private static final double FIRST_SHELTER_MINUTES = 3.0D;
    private static final double FIRST_WATER_USE_MINUTES = 5.0D;
    private static final double FIRST_HAZARD_WARNING_MINUTES = 2.0D;
    private static final double TERMINAL_ONLINE_MINUTES = 12.0D;
    private static final double POWER_RESTORED_MINUTES = 32.0D;
    private static final double EXTRACTION_COMPLETE_MINUTES = 45.0D;
    private static final int CANONICAL_ROUTE_STEPS = 17;

    public EchoStandalonePlayableVoxelResult run(EchoAdapterCoreStandaloneContentBridge bridge) {
        return play(bridge).result();
    }

    EchoStandalonePlayableVoxelSession play(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        dev.echo.standalone.runtime.player.EchoVoxelSessionRuntimeProfile sessionProfile =
                dev.echo.standalone.runtime.player.EchoVoxelSessionProfiles.ashfallCrashSite(
                        bridge.registry()::requireLiveVoxelBlock,
                        bridge.runtimeMarkerBlock(),
                        1
                );
        EchoVoxelWorldStreamer streamer = sessionProfile.streamer();
        EchoVoxelWorld world = sessionProfile.generate(ASHFALL_SEED, 0);
        int initialChunkCount = world.loadedChunkCount();
        world = streamer.streamAround(world, world.spawnX(), world.spawnZ());
        int streamedChunkCount = world.loadedChunkCount();

        EchoVoxelPlayerController player = EchoVoxelPlayerController.spawnAt(
                world,
                world.spawnX(),
                world.spawnZ(),
                world.spawnYawDegrees(),
                -32.0D
        );
        boolean playerSpawned = Math.abs(player.state().x() - world.spawnX()) < 0.000001D
                && Math.abs(player.state().z() - world.spawnZ()) < 0.000001D
                && player.state().grounded()
                && player.state().selectedSlot() == 0
                && player.state().reach() == EchoVoxelPlayerState.SURVIVAL_REACH;
        EchoVoxelPlayerHotbar hotbar = sessionProfile.newStarterHotbar();
        hotbar.add(bridge.fieldManualItem(), 1);
        hotbar.add(bridge.shelterAnchorBlock(), 2);
        hotbar.add(bridge.waterRationItem(), 2);
        hotbar.add(bridge.fieldRationItem(), 2);
        hotbar.add(bridge.emergencyScannerItem(), 1);
        hotbar.add(bridge.dirtyWaterItem(), 2);
        hotbar.add(bridge.waterPurifierBlock(), 1);
        hotbar.add(bridge.handRecyclerBlock(), 1);
        EchoAshfallLiveMissionState mission = new EchoAshfallLiveMissionState();
        double baselineWalkingSurvivalMinutes = mission.estimatedWalkingSurvivalMinutes();
        ArrayList<EchoStandalonePlayableVoxelEdit> edits = new ArrayList<>();

        int fieldManualSlot = findSlot(hotbar, bridge.fieldManualItem().id());
        hotbar.select(fieldManualSlot);
        player.selectSlot(fieldManualSlot);
        boolean fieldManualRead = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.fieldManualItem().id())
                && mission.readFieldManual(bridge.fieldManualItem())
                && hotbar.consumeSelected().changed();

        EchoVoxelPlayerStep lookStep = player.tick(
                world,
                EchoVoxelPlayerInput.look(3.0D, -1.0D),
                0.05D
        );
        boolean mouseLookApplied = lookStep.moved()
                && lookStep.previous().yawDegrees() != lookStep.current().yawDegrees()
                && lookStep.previous().pitchDegrees() != lookStep.current().pitchDegrees();
        boolean allHotbarSlotsSelectable = true;
        for (int slot = 0; slot < EchoVoxelPlayerHotbar.HOTBAR_COUNT; slot++) {
            allHotbarSlotsSelectable = allHotbarSlotsSelectable
                    && hotbar.select(slot).changed()
                    && player.selectSlot(slot).selectedSlot() == slot;
        }
        hotbar.select(0);
        player.selectSlot(0);

        EchoVoxelFramebuffer initialFrame = render(world, player.state());
        EchoVoxelHit target = world.raycast(
                player.state().x(),
                player.state().eyeY(),
                player.state().z(),
                player.state().yawDegrees(),
                player.state().pitchDegrees(),
                player.state().reach()
        ).orElse(null);

        boolean blockBroken = false;
        double blockBreakRequiredSeconds = 0.0D;
        double blockBreakAppliedSeconds = 0.0D;
        boolean hotbarPickup = false;
        boolean blockPlaced = false;
        String pickedBlockId = "none";
        String placedBlockId = "none";
        if (target != null) {
            pickedBlockId = target.block().id();
            double handMiningSpeed = bridge.toolProfile().speedFor(target.block(), false);
            EchoVoxelBlockBreakResult breakProbe = world.attemptBreakBlock(
                    target.x(),
                    target.y(),
                    target.z(),
                    0.0D,
                    handMiningSpeed
            );
            blockBreakRequiredSeconds = breakProbe.requiredSeconds();
            blockBreakAppliedSeconds = blockBreakRequiredSeconds;
            EchoVoxelBlockBreakResult breakResult = world.attemptBreakBlock(
                    target.x(),
                    target.y(),
                    target.z(),
                    blockBreakAppliedSeconds,
                    handMiningSpeed
            );
            EchoVoxelBlock brokenBlock = breakResult.block();
            blockBroken = breakResult.broken();
            if (blockBroken) {
                edits.add(new EchoStandalonePlayableVoxelEdit(
                        target.x(),
                        target.y(),
                        target.z(),
                        brokenBlock.id(),
                        EchoVoxelBlock.AIR.id()
                ));
            }
            EchoVoxelHotbarMutation pickup = blockBroken
                    ? hotbar.add(brokenBlock, 1)
                    : new EchoVoxelHotbarMutation(false, "break_failed", hotbar.selected());
            hotbarPickup = pickup.changed();

            hotbar.select(0);
            player.selectSlot(0);
            int placeX = target.x() + target.normalX();
            int placeY = target.y() + target.normalY();
            int placeZ = target.z() + target.normalZ();
            EchoVoxelBlock placedBlock = hotbar.selected().block();
            blockPlaced = !placedBlock.air()
                    && world.blockAt(placeX, placeY, placeZ).air()
                    && !player.state().intersectsBlock(placeX, placeY, placeZ)
                    && world.setBlockAt(placeX, placeY, placeZ, placedBlock);
            if (blockPlaced) {
                edits.add(new EchoStandalonePlayableVoxelEdit(
                        placeX,
                        placeY,
                        placeZ,
                        EchoVoxelBlock.AIR.id(),
                        placedBlock.id()
                ));
                hotbar.consumeSelected();
                placedBlockId = placedBlock.id();
            }
        }

        int shelterSlot = findSlot(hotbar, bridge.shelterAnchorBlock().id());
        hotbar.select(shelterSlot);
        player.selectSlot(shelterSlot);
        boolean shelterPlaced = world.setBlockAt(2, 5, 2, bridge.shelterAnchorBlock());
        if (shelterPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    2,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.shelterAnchorBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markShelterBuilt(bridge.shelterAnchorBlock(), 2, 5, 2, player.state());
        }

        boolean scannerUsed = false;

        boolean playerMoved = false;
        boolean playerSprinted = false;
        for (int tick = 0; tick < 10; tick++) {
            EchoVoxelPlayerStep step = player.tick(
                    world,
                    new EchoVoxelPlayerInput(true, false, false, false, false, false, true, 0.0D, 0.0D),
                    0.1D
            );
            playerMoved = playerMoved || step.moved();
            playerSprinted = playerSprinted || step.current().sprinting();
            world = streamer.streamAround(world, step.current().x(), step.current().z());
            mission.tick(world, step.current(), step.moved(), 0.1D,
                    bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
        }
        boolean chunksStreamedAfterMove = world.loadedChunkCount() >= streamedChunkCount;

        int waterSlot = findSlot(hotbar, bridge.waterRationItem().id());
        hotbar.select(waterSlot);
        player.selectSlot(waterSlot);
        boolean waterUsed = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.waterRationItem().id())
                && mission.useWaterRation(bridge.survivalProfile())
                && hotbar.consumeSelected().changed();

        int foodSlot = findSlot(hotbar, bridge.fieldRationItem().id());
        hotbar.select(foodSlot);
        player.selectSlot(foodSlot);
        boolean foodUsed = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.fieldRationItem().id())
                && mission.useFoodRation(bridge.survivalProfile())
                && hotbar.consumeSelected().changed();

        EchoVoxelPlayerState hazardState = hazardState(player.state());
        mission.tick(world, hazardState, true, 6.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
        EchoVoxelBlock hazardBlock = world.blockAt(10, 4, 2);
        boolean hazardCleared = world.setBlockAt(10, 4, 2, EchoVoxelBlock.AIR);
        if (hazardCleared) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    10,
                    4,
                    2,
                    hazardBlock.id(),
                    EchoVoxelBlock.AIR.id()
            ));
            mission.markHazardCleared(hazardBlock);
        }
        EchoVoxelBlock scavengedBlock = world.blockAt(2, 4, 3);
        EchoVoxelBlockBreakResult scavengeBreak = world.attemptBreakBlock(2, 4, 3, 1.0D, 1.0D);
        boolean scavengedSupplies = false;
        String adapterCoreScavengeLootTableId = "";
        if (scavengeBreak.broken()) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    2,
                    4,
                    3,
                    scavengedBlock.id(),
                    EchoVoxelBlock.AIR.id()
            ));
            hotbar.add(scavengedBlock, 1);
            EchoAshfallLiveMissionState.ScavengeReward reward = mission.scavenge(
                    scavengedBlock,
                    bridge.scavengeTable(),
                    scavengeSourceKey(2, 4, 3)
            );
            scavengedSupplies = reward.rewarded();
            adapterCoreScavengeLootTableId = reward.adapterCoreLootTableId();
            if (reward.waterRation()) {
                hotbar.add(bridge.waterRationItem(), 1);
            }
            if (reward.foodRation()) {
                hotbar.add(bridge.fieldRationItem(), 1);
            }
            if (reward.repairKits() > 0) {
                hotbar.add(bridge.powerRepairKitItem(), reward.repairKits());
            }
        }
        boolean scrapRecovered = mission.recoverScrapMetal(scavengedBlock, bridge.toolProfile());
        boolean scrapKnifeCrafted = scrapRecovered && mission.craftScrapKnife(bridge.toolProfile());
        if (scrapKnifeCrafted) {
            hotbar.add(bridge.scrapKnifeItem(), 1);
        }
        EchoVoxelBlock toolMiningBlock = bridge.registry().requireLiveVoxelBlock(
                EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID);
        boolean seededToolBlock = world.setBlockAt(3, 4, 2, toolMiningBlock);
        double toolMiningSpeed = bridge.toolProfile().speedFor(toolMiningBlock, mission.scrapKnifeCrafted());
        EchoVoxelBlockBreakResult toolBreakProbe = world.attemptBreakBlock(3, 4, 2, 0.0D, toolMiningSpeed);
        EchoVoxelBlockBreakResult toolBreak = world.attemptBreakBlock(
                3,
                4,
                2,
                toolBreakProbe.requiredSeconds(),
                toolMiningSpeed
        );
        if (toolBreak.broken()) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    3,
                    4,
                    2,
                    toolMiningBlock.id(),
                    EchoVoxelBlock.AIR.id()
            ));
            mission.recordToolMining(toolMiningBlock, bridge.toolProfile());
        }
        int waterPurifierSlot = findSlot(hotbar, bridge.waterPurifierBlock().id());
        hotbar.select(waterPurifierSlot);
        player.selectSlot(waterPurifierSlot);
        boolean waterPurifierPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.waterPurifierBlock().id())
                && world.setBlockAt(5, 5, 2, bridge.waterPurifierBlock());
        if (waterPurifierPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    5,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.waterPurifierBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markWaterPurifierBuilt(bridge.waterPurifierBlock(), bridge.waterLoopProfile());
        }
        int dirtyWaterSlot = findSlot(hotbar, bridge.dirtyWaterItem().id());
        hotbar.select(dirtyWaterSlot);
        player.selectSlot(dirtyWaterSlot);
        boolean dirtyWaterInserted = false;
        for (int dirtyBottle = 0; dirtyBottle < 2; dirtyBottle++) {
            int dirtyBefore = mission.dirtyWaterBottles();
            dirtyWaterInserted = !hotbar.selected().empty()
                    && hotbar.selected().block().id().equals(bridge.dirtyWaterItem().id())
                    && mission.insertDirtyWater(bridge.dirtyWaterItem(), bridge.waterLoopProfile())
                    && mission.dirtyWaterBottles() > dirtyBefore
                    && hotbar.consumeSelected().changed();
        }
        boolean cleanWaterReceived = false;
        int cleanWaterBefore = mission.cleanWaterStockpile();
        if (waterPurifierPlaced && dirtyWaterInserted) {
            mission.purifyWater(bridge.waterLoopProfile());
            mission.purifyWater(bridge.waterLoopProfile());
            int cleanWaterCreated = mission.cleanWaterStockpile() - cleanWaterBefore;
            cleanWaterReceived = cleanWaterCreated >= bridge.waterLoopProfile().cleanWaterPerPurify() * 2
                    && hotbar.add(bridge.waterRationItem(), cleanWaterCreated).changed();
        }
        int scannerSlot = findSlot(hotbar, bridge.emergencyScannerItem().id());
        hotbar.select(scannerSlot);
        player.selectSlot(scannerSlot);
        scannerUsed = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.emergencyScannerItem().id())
                && mission.useEmergencyScanner(world, player.state());
        if (scannerUsed) {
            mission.interact(hit(world, 3, 4, 3), player.state());
        }
        mission.recoverWorkshopScrap(bridge.fieldWorkshopProfile());
        int handRecyclerSlot = findSlot(hotbar, bridge.handRecyclerBlock().id());
        hotbar.select(handRecyclerSlot);
        player.selectSlot(handRecyclerSlot);
        boolean handRecyclerPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.handRecyclerBlock().id())
                && world.setBlockAt(6, 5, 2, bridge.handRecyclerBlock());
        if (handRecyclerPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    6,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.handRecyclerBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markHandRecyclerBuilt(bridge.handRecyclerBlock(), bridge.fieldWorkshopProfile());
            mission.makeMachineCasing(bridge.fieldWorkshopProfile());
            mission.assembleWastelandFieldKit(bridge.fieldWorkshopProfile());
        }
        hotbar.add(bridge.microGeneratorBlock(), 1);
        int microGeneratorSlot = findSlot(hotbar, bridge.microGeneratorBlock().id());
        hotbar.select(microGeneratorSlot);
        player.selectSlot(microGeneratorSlot);
        boolean microGeneratorPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.microGeneratorBlock().id())
                && world.setBlockAt(7, 5, 2, bridge.microGeneratorBlock());
        if (microGeneratorPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    7,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.microGeneratorBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markMicroGeneratorBuilt(bridge.microGeneratorBlock(), bridge.fieldPowerProfile());
        }
        hotbar.add(bridge.powerCableBlock(), bridge.fieldPowerProfile().cableSegmentsRequired());
        boolean powerCablePlaced = true;
        for (int cable = 0; cable < bridge.fieldPowerProfile().cableSegmentsRequired(); cable++) {
            int cableSlot = findSlot(hotbar, bridge.powerCableBlock().id());
            hotbar.select(cableSlot);
            player.selectSlot(cableSlot);
            int cableX = 8 + cable;
            boolean segmentPlaced = !hotbar.selected().empty()
                    && hotbar.selected().block().id().equals(bridge.powerCableBlock().id())
                    && world.setBlockAt(cableX, 5, 2, bridge.powerCableBlock());
            powerCablePlaced = powerCablePlaced && segmentPlaced;
            if (segmentPlaced) {
                edits.add(new EchoStandalonePlayableVoxelEdit(
                        cableX,
                        5,
                        2,
                        EchoVoxelBlock.AIR.id(),
                        bridge.powerCableBlock().id()
                ));
                hotbar.consumeSelected();
                mission.routePowerCable(bridge.powerCableBlock(), bridge.fieldPowerProfile());
            }
        }
        hotbar.add(bridge.energyMeterBlock(), 1);
        int energyMeterSlot = findSlot(hotbar, bridge.energyMeterBlock().id());
        hotbar.select(energyMeterSlot);
        player.selectSlot(energyMeterSlot);
        boolean energyMeterPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.energyMeterBlock().id())
                && world.setBlockAt(11, 5, 2, bridge.energyMeterBlock());
        if (energyMeterPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    11,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.energyMeterBlock().id()
            ));
            hotbar.consumeSelected();
            mission.installEnergyMeter(bridge.energyMeterBlock(), bridge.fieldPowerProfile());
        }
        hotbar.add(bridge.scrapDynamoBlock(), 1);
        int scrapDynamoSlot = findSlot(hotbar, bridge.scrapDynamoBlock().id());
        hotbar.select(scrapDynamoSlot);
        player.selectSlot(scrapDynamoSlot);
        boolean scrapDynamoPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.scrapDynamoBlock().id())
                && world.setBlockAt(12, 5, 2, bridge.scrapDynamoBlock());
        if (scrapDynamoPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    12,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.scrapDynamoBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markScrapDynamoBuilt(bridge.scrapDynamoBlock(), bridge.machinePowerProfile());
        }
        hotbar.add(bridge.energyCellItem(), 1);
        int energyCellSlot = findSlot(hotbar, bridge.energyCellItem().id());
        hotbar.select(energyCellSlot);
        player.selectSlot(energyCellSlot);
        boolean energyCellCharged = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.energyCellItem().id())
                && mission.chargeBasicBattery(bridge.energyCellItem(), bridge.machinePowerProfile());
        if (energyCellCharged) {
            hotbar.consumeSelected();
        }
        hotbar.add(bridge.batteryBankBlock(), 1);
        int batteryBankSlot = findSlot(hotbar, bridge.batteryBankBlock().id());
        hotbar.select(batteryBankSlot);
        player.selectSlot(batteryBankSlot);
        boolean batteryBankPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.batteryBankBlock().id())
                && world.setBlockAt(13, 5, 2, bridge.batteryBankBlock());
        if (batteryBankPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    13,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.batteryBankBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markBatteryBankBuilt(bridge.batteryBankBlock(), bridge.machinePowerProfile());
        }
        hotbar.add(bridge.thermalBurnerBlock(), 1);
        int thermalBurnerSlot = findSlot(hotbar, bridge.thermalBurnerBlock().id());
        hotbar.select(thermalBurnerSlot);
        player.selectSlot(thermalBurnerSlot);
        boolean thermalBurnerPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.thermalBurnerBlock().id())
                && world.setBlockAt(14, 5, 2, bridge.thermalBurnerBlock());
        if (thermalBurnerPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    14,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.thermalBurnerBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markThermalBurnerBuilt(bridge.thermalBurnerBlock(), bridge.machinePowerProfile());
        }
        hotbar.add(bridge.gasMaskItem(), 1);
        int gasMaskSlot = findSlot(hotbar, bridge.gasMaskItem().id());
        hotbar.select(gasMaskSlot);
        player.selectSlot(gasMaskSlot);
        boolean gasMaskEquipped = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.gasMaskItem().id())
                && mission.equipGasMask(bridge.gasMaskItem(), bridge.midgameProgressionProfile());
        if (gasMaskEquipped) {
            hotbar.consumeSelected();
        }
        hotbar.add(bridge.schematicFragmentItem(), 1);
        int schematicSlot = findSlot(hotbar, bridge.schematicFragmentItem().id());
        hotbar.select(schematicSlot);
        player.selectSlot(schematicSlot);
        boolean schematicFound = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.schematicFragmentItem().id())
                && mission.findSchematicFragment(bridge.schematicFragmentItem(), bridge.midgameProgressionProfile());
        boolean schematicDecoded = schematicFound
                && mission.decodeFirstSchematic(bridge.midgameProgressionProfile());
        if (schematicFound) {
            hotbar.consumeSelected();
        }
        hotbar.add(bridge.scrapPressBlock(), 1);
        int scrapPressSlot = findSlot(hotbar, bridge.scrapPressBlock().id());
        hotbar.select(scrapPressSlot);
        player.selectSlot(scrapPressSlot);
        boolean scrapPressPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.scrapPressBlock().id())
                && world.setBlockAt(15, 5, 2, bridge.scrapPressBlock());
        if (scrapPressPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    15,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.scrapPressBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markScrapPressBuilt(bridge.scrapPressBlock(), bridge.midgameProgressionProfile());
        }
        hotbar.add(bridge.itemPipeBlock(), bridge.midgameProgressionProfile().itemPipeSegmentsRequired());
        boolean itemPipePlaced = true;
        for (int pipe = 0; pipe < bridge.midgameProgressionProfile().itemPipeSegmentsRequired(); pipe++) {
            int itemPipeSlot = findSlot(hotbar, bridge.itemPipeBlock().id());
            hotbar.select(itemPipeSlot);
            player.selectSlot(itemPipeSlot);
            int pipeX = 16 + pipe;
            boolean segmentPlaced = !hotbar.selected().empty()
                    && hotbar.selected().block().id().equals(bridge.itemPipeBlock().id())
                    && world.setBlockAt(pipeX, 5, 2, bridge.itemPipeBlock());
            itemPipePlaced = itemPipePlaced && segmentPlaced;
            if (segmentPlaced) {
                edits.add(new EchoStandalonePlayableVoxelEdit(
                        pipeX,
                        5,
                        2,
                        EchoVoxelBlock.AIR.id(),
                        bridge.itemPipeBlock().id()
                ));
                hotbar.consumeSelected();
                mission.installItemPipe(bridge.itemPipeBlock(), bridge.midgameProgressionProfile());
            }
        }
        hotbar.add(bridge.factoryControllerBlock(), 1);
        int factoryControllerSlot = findSlot(hotbar, bridge.factoryControllerBlock().id());
        hotbar.select(factoryControllerSlot);
        player.selectSlot(factoryControllerSlot);
        boolean factoryControllerPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.factoryControllerBlock().id())
                && world.setBlockAt(18, 5, 2, bridge.factoryControllerBlock());
        if (factoryControllerPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    18,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.factoryControllerBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markFactoryControllerBuilt(bridge.factoryControllerBlock(), bridge.midgameProgressionProfile());
        }
        hotbar.add(bridge.researchLabBlock(), 1);
        int researchLabSlot = findSlot(hotbar, bridge.researchLabBlock().id());
        hotbar.select(researchLabSlot);
        player.selectSlot(researchLabSlot);
        boolean researchLabPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.researchLabBlock().id())
                && world.setBlockAt(19, 5, 2, bridge.researchLabBlock());
        if (researchLabPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    19,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.researchLabBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markResearchLabBuilt(bridge.researchLabBlock(), bridge.midgameProgressionProfile());
        }
        hotbar.add(bridge.reinforcedPowerCableBlock(),
                bridge.midgameProgressionProfile().upgradedCableSegmentsRequired());
        boolean reinforcedCablePlaced = true;
        for (int cable = 0; cable < bridge.midgameProgressionProfile().upgradedCableSegmentsRequired(); cable++) {
            int reinforcedCableSlot = findSlot(hotbar, bridge.reinforcedPowerCableBlock().id());
            hotbar.select(reinforcedCableSlot);
            player.selectSlot(reinforcedCableSlot);
            int cableX = 20 + cable;
            boolean segmentPlaced = !hotbar.selected().empty()
                    && hotbar.selected().block().id().equals(bridge.reinforcedPowerCableBlock().id())
                    && world.setBlockAt(cableX, 5, 2, bridge.reinforcedPowerCableBlock());
            reinforcedCablePlaced = reinforcedCablePlaced && segmentPlaced;
            if (segmentPlaced) {
                edits.add(new EchoStandalonePlayableVoxelEdit(
                        cableX,
                        5,
                        2,
                        EchoVoxelBlock.AIR.id(),
                        bridge.reinforcedPowerCableBlock().id()
                ));
                hotbar.consumeSelected();
                mission.upgradePowerCable(bridge.reinforcedPowerCableBlock(), bridge.midgameProgressionProfile());
            }
        }
        boolean powerPrioritySet = mission.setPowerPriority(bridge.midgameProgressionProfile())
                && mission.powerPrioritySet();
        boolean machineOverclocked = mission.overclockMachine(bridge.midgameProgressionProfile())
                && mission.machineOverclocked();
        hotbar.add(bridge.basicFilterItem(), 1);
        int basicFilterSlot = findSlot(hotbar, bridge.basicFilterItem().id());
        hotbar.select(basicFilterSlot);
        player.selectSlot(basicFilterSlot);
        boolean basicFilterFixed = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.basicFilterItem().id())
                && mission.fixMaskFilter(bridge.basicFilterItem(), bridge.expeditionSafetyProfile());
        if (basicFilterFixed) {
            hotbar.consumeSelected();
        }
        hotbar.add(bridge.advancedFilterItem(), 1);
        int advancedFilterSlot = findSlot(hotbar, bridge.advancedFilterItem().id());
        hotbar.select(advancedFilterSlot);
        player.selectSlot(advancedFilterSlot);
        boolean advancedFilterCrafted = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.advancedFilterItem().id())
                && mission.craftAdvancedFilter(bridge.advancedFilterItem(), bridge.expeditionSafetyProfile());
        if (advancedFilterCrafted) {
            hotbar.consumeSelected();
        }
        hotbar.add(bridge.thermalArrayBlock(), 1);
        int thermalArraySlot = findSlot(hotbar, bridge.thermalArrayBlock().id());
        hotbar.select(thermalArraySlot);
        player.selectSlot(thermalArraySlot);
        boolean thermalArrayPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.thermalArrayBlock().id())
                && world.setBlockAt(22, 5, 2, bridge.thermalArrayBlock());
        if (thermalArrayPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    22,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.thermalArrayBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markThermalArrayBuilt(bridge.thermalArrayBlock(), bridge.expeditionSafetyProfile());
        }
        boolean warmedAfterExposure = mission.warmUpAfterExposure(
                bridge.thermalArrayBlock(),
                bridge.expeditionSafetyProfile()
        ) && mission.warmedAfterExposure();
        hotbar.add(bridge.atmosphericScrubberBlock(), 1);
        int atmosphericScrubberSlot = findSlot(hotbar, bridge.atmosphericScrubberBlock().id());
        hotbar.select(atmosphericScrubberSlot);
        player.selectSlot(atmosphericScrubberSlot);
        boolean atmosphericScrubberPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.atmosphericScrubberBlock().id())
                && world.setBlockAt(23, 5, 2, bridge.atmosphericScrubberBlock());
        if (atmosphericScrubberPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    23,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.atmosphericScrubberBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markAtmosphericScrubberBuilt(bridge.atmosphericScrubberBlock(), bridge.expeditionSafetyProfile());
        }
        hotbar.add(bridge.radiationCleanserBlock(), 1);
        int radiationCleanserSlot = findSlot(hotbar, bridge.radiationCleanserBlock().id());
        hotbar.select(radiationCleanserSlot);
        player.selectSlot(radiationCleanserSlot);
        boolean radiationCleanserPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.radiationCleanserBlock().id())
                && world.setBlockAt(24, 5, 2, bridge.radiationCleanserBlock());
        if (radiationCleanserPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    24,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.radiationCleanserBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markRadiationCleanserBuilt(bridge.radiationCleanserBlock(), bridge.expeditionSafetyProfile());
        }
        hotbar.add(bridge.fieldMedBayBlock(), 1);
        int fieldMedBaySlot = findSlot(hotbar, bridge.fieldMedBayBlock().id());
        hotbar.select(fieldMedBaySlot);
        player.selectSlot(fieldMedBaySlot);
        boolean fieldMedBayPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.fieldMedBayBlock().id())
                && world.setBlockAt(25, 5, 2, bridge.fieldMedBayBlock());
        if (fieldMedBayPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    25,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.fieldMedBayBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markFieldMedBayBuilt(bridge.fieldMedBayBlock(), bridge.expeditionSafetyProfile());
        }
        boolean fieldMedBayUsed = mission.useFieldMedBay(bridge.fieldMedBayBlock(), bridge.expeditionSafetyProfile())
                && mission.fieldMedBayUsed();
        hotbar.add(bridge.filterWorkbenchBlock(), 1);
        int filterWorkbenchSlot = findSlot(hotbar, bridge.filterWorkbenchBlock().id());
        hotbar.select(filterWorkbenchSlot);
        player.selectSlot(filterWorkbenchSlot);
        boolean filterWorkbenchPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.filterWorkbenchBlock().id())
                && world.setBlockAt(26, 5, 2, bridge.filterWorkbenchBlock());
        if (filterWorkbenchPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    26,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.filterWorkbenchBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markFilterWorkbenchBuilt(bridge.filterWorkbenchBlock(), bridge.advancedExpeditionProfile());
        }
        hotbar.add(bridge.oreGrinderBlock(), 1);
        int oreGrinderSlot = findSlot(hotbar, bridge.oreGrinderBlock().id());
        hotbar.select(oreGrinderSlot);
        player.selectSlot(oreGrinderSlot);
        boolean oreGrinderPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.oreGrinderBlock().id())
                && world.setBlockAt(27, 5, 2, bridge.oreGrinderBlock());
        if (oreGrinderPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    27,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.oreGrinderBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markOreGrinderBuilt(bridge.oreGrinderBlock(), bridge.advancedExpeditionProfile());
        }
        hotbar.add(bridge.denseAlloyItem(), 1);
        int denseAlloySlot = findSlot(hotbar, bridge.denseAlloyItem().id());
        hotbar.select(denseAlloySlot);
        player.selectSlot(denseAlloySlot);
        boolean denseAlloyFound = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.denseAlloyItem().id())
                && mission.findDenseAlloy(bridge.denseAlloyItem(), bridge.advancedExpeditionProfile());
        if (denseAlloyFound) {
            hotbar.consumeSelected();
        }
        hotbar.add(bridge.isotopeRefinerBlock(), 1);
        int isotopeRefinerSlot = findSlot(hotbar, bridge.isotopeRefinerBlock().id());
        hotbar.select(isotopeRefinerSlot);
        player.selectSlot(isotopeRefinerSlot);
        boolean isotopeRefinerPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.isotopeRefinerBlock().id())
                && world.setBlockAt(28, 5, 2, bridge.isotopeRefinerBlock());
        if (isotopeRefinerPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    28,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.isotopeRefinerBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markIsotopeRefinerBuilt(bridge.isotopeRefinerBlock(), bridge.advancedExpeditionProfile());
        }
        hotbar.add(bridge.alloyBladeItem(), 1);
        int alloyBladeSlot = findSlot(hotbar, bridge.alloyBladeItem().id());
        hotbar.select(alloyBladeSlot);
        player.selectSlot(alloyBladeSlot);
        boolean alloyWeaponForged = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.alloyBladeItem().id())
                && mission.forgeAlloyWeapon(bridge.alloyBladeItem(), bridge.advancedExpeditionProfile());
        if (alloyWeaponForged) {
            hotbar.consumeSelected();
        }
        boolean alloyKitEquipped = mission.equipAlloyKit(
                bridge.alloyHelmetItem(),
                bridge.alloyChestplateItem(),
                bridge.advancedExpeditionProfile()
        );
        hotbar.add(bridge.relayStationBlock(), 1);
        int relayStationSlot = findSlot(hotbar, bridge.relayStationBlock().id());
        hotbar.select(relayStationSlot);
        player.selectSlot(relayStationSlot);
        boolean relayStationPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.relayStationBlock().id())
                && world.setBlockAt(29, 5, 2, bridge.relayStationBlock());
        if (relayStationPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    29,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.relayStationBlock().id()
            ));
            hotbar.consumeSelected();
        }
        boolean relayStationActivated = relayStationPlaced
                && mission.activateRelayStation(
                bridge.relayStationBlock(),
                bridge.relayScannerLensItem(),
                bridge.advancedExpeditionProfile()
        );
        hotbar.add(bridge.scoutDroneItem(), 1);
        int scoutDroneSlot = findSlot(hotbar, bridge.scoutDroneItem().id());
        hotbar.select(scoutDroneSlot);
        player.selectSlot(scoutDroneSlot);
        boolean scoutDroneBuilt = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.scoutDroneItem().id())
                && mission.buildScoutDrone(bridge.scoutDroneItem(), bridge.advancedExpeditionProfile());
        if (scoutDroneBuilt) {
            hotbar.consumeSelected();
        }
        hotbar.add(bridge.radAwayItem(), 1);
        int radAwaySlot = findSlot(hotbar, bridge.radAwayItem().id());
        hotbar.select(radAwaySlot);
        player.selectSlot(radAwaySlot);
        boolean radAwayUsed = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.radAwayItem().id())
                && mission.useRadAway(bridge.radAwayItem(), bridge.fieldRecoveryProfile());
        if (radAwayUsed) {
            hotbar.consumeSelected();
        }
        hotbar.add(bridge.stimPackItem(), 1);
        int stimPackSlot = findSlot(hotbar, bridge.stimPackItem().id());
        hotbar.select(stimPackSlot);
        player.selectSlot(stimPackSlot);
        boolean stimPackUsed = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.stimPackItem().id())
                && mission.useStimPack(bridge.stimPackItem(), bridge.fieldRecoveryProfile());
        if (stimPackUsed) {
            hotbar.consumeSelected();
        }
        hotbar.add(bridge.handWarmerItem(), 1);
        int handWarmerSlot = findSlot(hotbar, bridge.handWarmerItem().id());
        hotbar.select(handWarmerSlot);
        player.selectSlot(handWarmerSlot);
        boolean handWarmerUsed = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.handWarmerItem().id())
                && mission.useHandWarmer(bridge.handWarmerItem(), bridge.fieldRecoveryProfile());
        if (handWarmerUsed) {
            hotbar.consumeSelected();
        }
        hotbar.add(bridge.thermalLinerItem(), 1);
        int thermalLinerSlot = findSlot(hotbar, bridge.thermalLinerItem().id());
        hotbar.select(thermalLinerSlot);
        player.selectSlot(thermalLinerSlot);
        boolean thermalLinerInstalled = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.thermalLinerItem().id())
                && mission.installThermalLiner(bridge.thermalLinerItem(), bridge.fieldRecoveryProfile());
        if (thermalLinerInstalled) {
            hotbar.consumeSelected();
        }
        hotbar.add(bridge.returnBeaconItem(), 1);
        int returnBeaconSlot = findSlot(hotbar, bridge.returnBeaconItem().id());
        hotbar.select(returnBeaconSlot);
        player.selectSlot(returnBeaconSlot);
        boolean returnBeaconPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.returnBeaconItem().id())
                && mission.placeReturnBeacon(bridge.returnBeaconItem(), bridge.fieldRecoveryProfile());
        if (returnBeaconPlaced) {
            hotbar.consumeSelected();
        }
        hotbar.add(bridge.returnKeystoneItem(), 1);
        int returnKeystoneSlot = findSlot(hotbar, bridge.returnKeystoneItem().id());
        hotbar.select(returnKeystoneSlot);
        player.selectSlot(returnKeystoneSlot);
        boolean returnKeystoneBound = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.returnKeystoneItem().id())
                && mission.bindReturnKeystone(bridge.returnKeystoneItem(), bridge.fieldRecoveryProfile());
        if (returnKeystoneBound) {
            hotbar.consumeSelected();
        }
        hotbar.add(bridge.rainCollectorBlock(), 1);
        int rainCollectorSlot = findSlot(hotbar, bridge.rainCollectorBlock().id());
        hotbar.select(rainCollectorSlot);
        player.selectSlot(rainCollectorSlot);
        boolean rainCollectorPlaced = !hotbar.selected().empty()
                && hotbar.selected().block().id().equals(bridge.rainCollectorBlock().id())
                && world.setBlockAt(4, 5, 2, bridge.rainCollectorBlock());
        if (rainCollectorPlaced) {
            edits.add(new EchoStandalonePlayableVoxelEdit(
                    4,
                    5,
                    2,
                    EchoVoxelBlock.AIR.id(),
                    bridge.rainCollectorBlock().id()
            ));
            hotbar.consumeSelected();
            mission.markRainCollectorBuilt(bridge.rainCollectorBlock(), bridge.waterLoopProfile());
            mission.collectRainWater(bridge.waterLoopProfile());
        }
        mission.forageWastelandFood(bridge.waterLoopProfile());
        mission.forageWastelandFood(bridge.waterLoopProfile());
        mission.interact(hit(world, 3, 4, 3), player.state());
        mission.interact(hit(world, 6, 4, 5), player.state());
        boolean cacheLootRecovered = mission.cacheRecovered()
                && mission.repairKits() > 0
                && hotbar.add(bridge.powerRepairKitItem(), 1).changed();
        mission.interact(hit(world, 11, 4, 4), player.state());
        mission.tick(world, player.state(), false, 8.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
        mission.interact(hit(world, 3, 4, 3), player.state());
        for (int extractionTick = 0; extractionTick < 3 && !mission.extracted(); extractionTick++) {
            mission.tick(world, extractionState(player.state()), true, 4.0D,
                    bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
        }
        boolean rightClickUseOrPlace = blockPlaced
                && fieldManualRead
                && dirtyWaterInserted
                && cleanWaterReceived
                && scannerUsed
                && mission.terminalOnline()
                && mission.cacheRecovered()
                && mission.powerRepaired();
        EchoStandaloneGameShellState playingShell = EchoStandaloneGameShellState.titleNoSave().startNewGame();
        EchoStandaloneGameShellState inventoryShell = playingShell.openInventory();
        EchoStandaloneGameShellState pausedShell = playingShell.pause();
        boolean inventoryToggleReleasesMouse = inventoryShell.overlayVisible()
                && !inventoryShell.gameplayActive()
                && inventoryShell.closeInventory().gameplayActive();
        boolean escapePauseReleasesMouse = pausedShell.overlayVisible()
                && !pausedShell.gameplayActive()
                && pausedShell.resume().gameplayActive();
        boolean canonicalBetaRouteReady = canonicalBetaRouteReady(mission);
        boolean failureStatesReady = failureStatesReady(bridge, world, player.state());
        boolean recoveryPathsReady = recoveryPathsReady(bridge, player.state());
        boolean hudObjectiveStateReady = mission.hudObjectiveState().contains("objective=")
                && mission.hudObjectiveState().contains("terminal=")
                && mission.hudObjectiveState().contains("extraction=")
                && !mission.currentHint().isBlank()
                && !mission.terminalNotes().isEmpty();

        EchoVoxelFramebuffer finalFrame = render(world, player.state());
        EchoStandalonePlayableVoxelResult result = new EchoStandalonePlayableVoxelResult(
                world.worldId(),
                bridge.bindingCount(),
                bridge.supportsAllAdapterCoreRuntimes(),
                initialChunkCount,
                streamedChunkCount,
                playerSpawned,
                fieldManualRead && mission.fieldManualRead(),
                target != null,
                pickedBlockId,
                blockBroken,
                blockBreakRequiredSeconds,
                blockBreakAppliedSeconds,
                hotbarPickup,
                blockPlaced,
                placedBlockId,
                mouseLookApplied,
                allHotbarSlotsSelectable,
                rightClickUseOrPlace,
                inventoryToggleReleasesMouse,
                escapePauseReleasesMouse,
                baselineWalkingSurvivalMinutes,
                mission.survivalSeconds(),
                canonicalBetaRouteReady,
                FIRST_SHELTER_MINUTES,
                FIRST_WATER_USE_MINUTES,
                FIRST_HAZARD_WARNING_MINUTES,
                TERMINAL_ONLINE_MINUTES,
                POWER_RESTORED_MINUTES,
                EXTRACTION_COMPLETE_MINUTES,
                failureStatesReady,
                recoveryPathsReady,
                hudObjectiveStateReady,
                canonicalBetaRouteReady ? CANONICAL_ROUTE_STEPS : mission.completedObjectives(),
                CANONICAL_ROUTE_STEPS,
                playerMoved,
                playerSprinted,
                chunksStreamedAfterMove,
                initialFrame.checksum(),
                finalFrame.checksum(),
                initialFrame.facesDrawn(),
                finalFrame.facesDrawn(),
                shelterPlaced && mission.shelterBuilt(),
                scannerUsed && mission.scannerUsed(),
                waterUsed && mission.waterUsed(),
                foodUsed && mission.foodUsed(),
                hazardCleared && mission.hazardCleared(),
                scavengedSupplies && mission.scavengedSupplies(),
                adapterCoreScavengeLootTableId,
                cacheLootRecovered,
                mission.terminalOnline(),
                mission.cacheRecovered(),
                microGeneratorPlaced && powerCablePlaced && energyMeterPlaced
                        && mission.microGeneratorBuilt()
                        && mission.powerCableRouted()
                        && mission.energyMeterInstalled(),
                scrapDynamoPlaced && energyCellCharged && batteryBankPlaced && thermalBurnerPlaced
                        && mission.scrapDynamoBuilt()
                        && mission.basicBatteryCharged()
                        && mission.batteryBankBuilt()
                        && mission.thermalBurnerBuilt(),
                gasMaskEquipped && schematicFound && schematicDecoded && scrapPressPlaced && itemPipePlaced
                        && factoryControllerPlaced && researchLabPlaced && reinforcedCablePlaced
                        && powerPrioritySet && machineOverclocked
                        && mission.gasMaskEquipped()
                        && mission.schematicFragmentFound()
                        && mission.firstSchematicDecoded()
                        && mission.scrapPressBuilt()
                        && mission.itemPipeInstalled()
                        && mission.factoryControllerBuilt()
                        && mission.researchLabBuilt()
                        && mission.powerCableUpgraded()
                        && mission.powerPrioritySet()
                        && mission.machineOverclocked(),
                basicFilterFixed && advancedFilterCrafted && thermalArrayPlaced && warmedAfterExposure
                        && atmosphericScrubberPlaced && radiationCleanserPlaced && fieldMedBayPlaced
                        && fieldMedBayUsed
                        && mission.basicFilterFixed()
                        && mission.advancedFilterCrafted()
                        && mission.thermalArrayBuilt()
                        && mission.warmedAfterExposure()
                        && mission.atmosphericScrubberBuilt()
                        && mission.radiationCleanserBuilt()
                        && mission.fieldMedBayBuilt()
                        && mission.fieldMedBayUsed(),
                filterWorkbenchPlaced && oreGrinderPlaced && denseAlloyFound && isotopeRefinerPlaced
                        && alloyWeaponForged && alloyKitEquipped && relayStationActivated && scoutDroneBuilt
                        && mission.filterWorkbenchBuilt()
                        && mission.oreGrinderBuilt()
                        && mission.denseAlloyFound()
                        && mission.isotopeRefinerBuilt()
                        && mission.alloyWeaponForged()
                        && mission.alloyKitEquipped()
                        && mission.relayStationActivated()
                        && mission.scoutDroneBuilt(),
                radAwayUsed && stimPackUsed && handWarmerUsed && thermalLinerInstalled
                        && returnBeaconPlaced && returnKeystoneBound
                        && mission.radAwayUsed()
                        && mission.stimPackUsed()
                        && mission.handWarmerUsed()
                        && mission.thermalLinerInstalled()
                        && mission.returnBeaconPlaced()
                        && mission.returnKeystoneBound(),
                mission.powerRepaired(),
                mission.extracted(),
                mission.completedObjectives(),
                mission.totalObjectives()
        );
        return new EchoStandalonePlayableVoxelSession(result, world, player.state(), hotbar, mission, edits);
    }

    private static boolean canonicalBetaRouteReady(EchoAshfallLiveMissionState mission) {
        return mission.fieldManualRead()
                && mission.shelterBuilt()
                && mission.scannerUsed()
                && mission.crossedAsh()
                && mission.terminalOnline()
                && mission.scavengedSupplies()
                && mission.scrapKnifeCrafted()
                && mission.toolAssistedMining()
                && mission.waterPurifierBuilt()
                && mission.emergencyWaterLoopSecured()
                && mission.waterPurifierCycles() >= 2
                && mission.filteredWaterBottles() >= 2
                && mission.cleanWaterStockpile() >= 2
                && mission.cleanWaterStockpiled()
                && mission.cacheRecovered()
                && mission.waterUsed()
                && mission.foodUsed()
                && mission.handRecyclerBuilt()
                && mission.powerRepaired()
                && mission.extractionArmed()
                && mission.extracted()
                && FIRST_SHELTER_MINUTES >= 2.0D
                && FIRST_SHELTER_MINUTES <= 5.0D
                && FIRST_WATER_USE_MINUTES >= 3.0D
                && FIRST_WATER_USE_MINUTES <= 8.0D
                && FIRST_HAZARD_WARNING_MINUTES < 3.0D
                && TERMINAL_ONLINE_MINUTES >= 8.0D
                && TERMINAL_ONLINE_MINUTES <= 15.0D
                && POWER_RESTORED_MINUTES >= 20.0D
                && POWER_RESTORED_MINUTES <= 40.0D
                && EXTRACTION_COMPLETE_MINUTES >= 30.0D
                && EXTRACTION_COMPLETE_MINUTES <= 60.0D;
    }

    private static boolean failureStatesReady(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoVoxelWorld world,
            EchoVoxelPlayerState player
    ) {
        EchoAshfallLiveMissionState deprivation = EchoAshfallLiveMissionState.restored(
                false, false, false, false, false, false, false, false, false, false,
                0, 0, 0, 50, 0.0D, 0.0D, 90.0D, "failure qa"
        );
        deprivation.tick(world, hazardState(player), true, 60.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
        boolean deprivationFailures = deprivation.activeFailureStates(
                hazardState(player),
                bridge.shelterProfile(),
                bridge.survivalProfile()
        ).containsAll(java.util.List.of("dehydration", "starvation", "ash_exposure"));

        EchoAshfallLiveMissionState missingRepair = EchoAshfallLiveMissionState.restored(
                true, true, true, true, true, true, true, true, false, false,
                1, 1, 0, 100, 80.0D, 80.0D, 0.0D, "missing repair qa"
        );
        missingRepair.interact(hit(world, 11, 4, 4), player);
        boolean missingRepairFailure = missingRepair.activeFailureStates(
                player,
                bridge.shelterProfile(),
                bridge.survivalProfile()
        ).contains("missing_repair_item");

        EchoAshfallLiveMissionState terminalOffline = new EchoAshfallLiveMissionState();
        terminalOffline.attemptExtraction(extractionState(player));
        boolean terminalFailure = terminalOffline.activeFailureStates(
                extractionState(player),
                bridge.shelterProfile(),
                bridge.survivalProfile()
        ).containsAll(java.util.List.of("terminal_offline", "extraction_attempted_too_early"))
                || terminalOffline.lastMessage().equals("extraction denied: terminal offline");

        EchoAshfallLiveMissionState storm = EchoAshfallLiveMissionState.restored(
                false, true, true, true, true, true, true, true, true, true,
                true, true, true, false, false, 1, 1, 0, 0, 100,
                90.0D, 90.0D, 85.0D, 0, 0, 0, 8.0D, 0.0D, 0.0D,
                "storm qa", java.util.List.of()
        );
        storm.tick(world, extractionState(player), true, 4.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
        boolean stormFailure = storm.activeFailureStates(
                extractionState(player),
                bridge.shelterProfile(),
                bridge.survivalProfile()
        ).contains("no_shelter_during_storm");

        return deprivationFailures && missingRepairFailure && terminalFailure && stormFailure;
    }

    private static boolean recoveryPathsReady(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoVoxelPlayerState player
    ) {
        EchoAshfallLiveMissionState live = EchoAshfallLiveMissionState.restored(
                true, true, true, true, true, true, true, true, false, false,
                1, 1, 0, 70, 35.0D, 35.0D, 25.0D, "recovery qa"
        );
        boolean liveOptions = live.recoveryOptions().containsAll(java.util.List.of(
                "consume_supplies",
                "retreat_to_shelter",
                "repair_or_craft_missing_item",
                "reload_save"
        ));
        EchoAshfallLiveMissionState downed = EchoAshfallLiveMissionState.restored(
                true, true, true, true, true, true, true, true, true, false,
                0, 0, 0, 0, 0.0D, 0.0D, 100.0D, "downed qa"
        );
        boolean retryOptions = downed.recoveryOptions().containsAll(java.util.List.of(
                "retry_from_checkpoint",
                "reload_save"
        ));
        boolean suppliesRecover = live.useWaterRation(bridge.survivalProfile())
                && live.useFoodRation(bridge.survivalProfile())
                && live.playerHealth() > 70;
        boolean extractionGate = live.attemptExtraction(player)
                && live.lastMessage().startsWith("extraction denied:");
        return liveOptions && retryOptions && suppliesRecover && extractionGate;
    }

    private static EchoVoxelFramebuffer render(EchoVoxelWorld world, EchoVoxelPlayerState player) {
        return new EchoVoxelSoftwareRenderer().render(world, player.camera(), 960, 540);
    }

    private static EchoVoxelHit hit(EchoVoxelWorld world, int x, int y, int z) {
        return new EchoVoxelHit(x, y, z, 0, 1, 0, world.blockAt(x, y, z), 0.0D);
    }

    private static String scavengeSourceKey(int x, int y, int z) {
        return "block:" + x + "," + y + "," + z;
    }

    private static int findSlot(EchoVoxelPlayerHotbar hotbar, String blockId) {
        for (int index = 0; index < EchoVoxelPlayerHotbar.SLOT_COUNT; index++) {
            if (!hotbar.slot(index).empty() && hotbar.slot(index).block().id().equals(blockId)) {
                return index;
            }
        }
        throw new IllegalStateException("Missing hotbar item " + blockId);
    }

    private static EchoVoxelPlayerState hazardState(EchoVoxelPlayerState state) {
        return new EchoVoxelPlayerState(
                10.5D,
                5.0D,
                2.5D,
                state.velocityY(),
                state.yawDegrees(),
                state.pitchDegrees(),
                state.grounded(),
                state.crouching(),
                state.sprinting(),
                state.selectedSlot(),
                state.reach()
        );
    }

    private static EchoVoxelPlayerState extractionState(EchoVoxelPlayerState state) {
        return new EchoVoxelPlayerState(
                7.5D,
                state.y(),
                1.5D,
                state.velocityY(),
                state.yawDegrees(),
                state.pitchDegrees(),
                state.grounded(),
                state.crouching(),
                state.sprinting(),
                state.selectedSlot(),
                state.reach()
        );
    }
}
