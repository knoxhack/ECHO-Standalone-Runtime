package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAshfallGameplayContracts;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelHit;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class EchoAshfallExperienceQaRuntime {
    public EchoAshfallExperienceQaResult run(
            EchoAdapterCoreStandaloneContentBridge bridge,
            Path workRoot
    ) throws IOException {
        Objects.requireNonNull(bridge, "bridge");
        Objects.requireNonNull(workRoot, "workRoot");
        Files.createDirectories(workRoot);

        EchoStandalonePlayableVoxelSession playableSession = new EchoStandalonePlayableVoxelRuntime().play(bridge);
        EchoStandalonePlayableVoxelResult playable = playableSession.result();
        EchoStandalonePlayableVoxelSaveResult save = new EchoStandalonePlayableVoxelSaveRuntime().run(
                bridge,
                workRoot.resolve("mid-run-save")
        );
        EchoSaveProfileFlowResult saveProfile = new EchoSaveProfileFlowRuntime().run(
                new EchoDefaultRuntimeServiceRegistry(),
                workRoot.resolve("profile-flow")
        );
        EchoStandaloneVoxelHudFramebufferResult visibleHud = new EchoStandaloneVoxelHudFramebufferRuntime().run(bridge);
        EchoStandaloneInventoryFramebufferResult inventory = new EchoStandaloneInventoryFramebufferRuntime().run(bridge);
        EchoAshfallInventoryUxResult inventoryUx = new EchoAshfallInventoryUxRuntime().run(bridge);
        EchoStandaloneTerminalFramebufferResult terminal = new EchoStandaloneTerminalFramebufferRuntime().run(bridge);
        EchoAshfallAudioCueCoverageResult audioCueCoverage = new EchoAshfallAudioCueCoverageRuntime().run(bridge);

        boolean fullChapterComplete = playable.missionCompleted()
                && playable.betaPlayableCoreReady()
                && playable.adapterCoreMultiRuntimeReady();
        boolean midRunSaveLoadReady = save.ready()
                && save.contractBacked()
                && save.contractVersioned()
                && save.restoredContractState();
        boolean deathRecoveryReady = saveProfile.restoreResult().restored()
                && saveProfile.restoreResult().afterRestore().healthy();
        boolean inventoryManipulationReady = inventory.ready()
                && inventory.centralChangedPixels() > 12_000;
        boolean inventoryUxReady = inventoryUx.ready();
        boolean visibleHudFeedbackReady = visibleHud.ready()
                && visibleHud.heldItemPreviewReady()
                && visibleHud.blockBreakFeedbackReady();
        boolean visibleActionParticlesReady = visibleHud.actionParticlesReady()
                && visibleHud.actionParticlesChangedPixels() > 240;
        boolean terminalBranchingReady = terminal.ready()
                && terminal.terminalOnline()
                && terminal.centralChangedPixels() > 14_000;
        boolean scavengeDepletionReady = scavengeDepletionReady(bridge)
                && save.restoredMission();
        boolean powerRepairFlowReady = powerRepairFlowReady(bridge);
        boolean extractionEventReady = extractionEventReady(bridge);
        boolean hazardVarietyReady = hazardVarietyReady(bridge)
                && save.restoredMission();
        boolean shelterSystemReady = shelterSystemReady(bridge)
                && save.restoredMission();
        boolean survivalNeedsReady = survivalNeedsReady(bridge)
                && save.restoredMission();
        boolean waterLoopReady = waterLoopReady(bridge)
                && save.restoredMission();
        boolean toolProgressionReady = toolProgressionReady(bridge)
                && save.restoredMission();
        boolean fieldWorkshopReady = fieldWorkshopReady(bridge)
                && save.restoredMission();
        boolean fieldPowerReady = fieldPowerReady(bridge)
                && save.restoredMission();
        boolean machinePowerReady = machinePowerReady(bridge)
                && save.restoredMission();
        boolean midgameProgressionReady = midgameProgressionReady(bridge)
                && save.restoredMission();
        boolean expeditionSafetyReady = expeditionSafetyReady(bridge)
                && save.restoredMission();
        boolean advancedExpeditionReady = advancedExpeditionReady(bridge)
                && save.restoredMission();
        boolean fieldRecoveryReady = fieldRecoveryReady(bridge)
                && save.restoredMission();
        boolean canonicalRouteReady = playable.canonicalBetaRouteReady()
                && playable.canonicalRouteStepsCompleted() == playable.canonicalRouteStepsTotal();
        boolean failureRecoveryReady = playable.failureStatesReady()
                && playable.recoveryPathsReady()
                && save.midRouteSaveLoadReady();
        boolean hudObjectiveStateReady = playable.hudObjectiveStateReady()
                && save.restoredMission();
        boolean routeWideGuidanceReady = routeWideGuidanceReady(playableSession);
        boolean audioCueCoverageReady = audioCueCoverage.ready();
        boolean corruptedSaveDetected = !saveProfile.corruptionWarning().healthy();
        boolean adapterCoreParityReady = bridge.supportsAllAdapterCoreRuntimes()
                && EchoAshfallGameplayContracts.parityReady(EchoAshfallGameplayContracts.ashfall(bridge));
        int diagnosticsCount = 0;
        diagnosticsCount += fullChapterComplete ? 1 : 0;
        diagnosticsCount += midRunSaveLoadReady ? 1 : 0;
        diagnosticsCount += deathRecoveryReady ? 1 : 0;
        diagnosticsCount += inventoryManipulationReady ? 1 : 0;
        diagnosticsCount += inventoryUxReady ? 1 : 0;
        diagnosticsCount += visibleHudFeedbackReady ? 1 : 0;
        diagnosticsCount += visibleActionParticlesReady ? 1 : 0;
        diagnosticsCount += terminalBranchingReady ? 1 : 0;
        diagnosticsCount += scavengeDepletionReady ? 1 : 0;
        diagnosticsCount += powerRepairFlowReady ? 1 : 0;
        diagnosticsCount += extractionEventReady ? 1 : 0;
        diagnosticsCount += hazardVarietyReady ? 1 : 0;
        diagnosticsCount += shelterSystemReady ? 1 : 0;
        diagnosticsCount += survivalNeedsReady ? 1 : 0;
        diagnosticsCount += waterLoopReady ? 1 : 0;
        diagnosticsCount += toolProgressionReady ? 1 : 0;
        diagnosticsCount += fieldWorkshopReady ? 1 : 0;
        diagnosticsCount += fieldPowerReady ? 1 : 0;
        diagnosticsCount += machinePowerReady ? 1 : 0;
        diagnosticsCount += midgameProgressionReady ? 1 : 0;
        diagnosticsCount += expeditionSafetyReady ? 1 : 0;
        diagnosticsCount += advancedExpeditionReady ? 1 : 0;
        diagnosticsCount += fieldRecoveryReady ? 1 : 0;
        diagnosticsCount += canonicalRouteReady ? 1 : 0;
        diagnosticsCount += failureRecoveryReady ? 1 : 0;
        diagnosticsCount += hudObjectiveStateReady ? 1 : 0;
        diagnosticsCount += routeWideGuidanceReady ? 1 : 0;
        diagnosticsCount += audioCueCoverageReady ? 1 : 0;
        diagnosticsCount += corruptedSaveDetected ? 1 : 0;
        diagnosticsCount += adapterCoreParityReady ? 1 : 0;

        return new EchoAshfallExperienceQaResult(
                fullChapterComplete,
                midRunSaveLoadReady,
                deathRecoveryReady,
                inventoryManipulationReady,
                inventoryUxReady,
                visibleHudFeedbackReady,
                visibleActionParticlesReady,
                terminalBranchingReady,
                scavengeDepletionReady,
                powerRepairFlowReady,
                extractionEventReady,
                hazardVarietyReady,
                shelterSystemReady,
                survivalNeedsReady,
                waterLoopReady,
                toolProgressionReady,
                fieldWorkshopReady,
                fieldPowerReady,
                machinePowerReady,
                midgameProgressionReady,
                expeditionSafetyReady,
                advancedExpeditionReady,
                fieldRecoveryReady,
                canonicalRouteReady,
                failureRecoveryReady,
                hudObjectiveStateReady,
                routeWideGuidanceReady,
                audioCueCoverageReady,
                corruptedSaveDetected,
                adapterCoreParityReady,
                diagnosticsCount,
                save.restoredRenderChecksumValue(),
                "fullChapter=" + fullChapterComplete
                        + " midRunSave=" + midRunSaveLoadReady
                        + " deathRecovery=" + deathRecoveryReady
                        + " inventory=" + inventoryManipulationReady
                        + " inventoryUx=" + inventoryUxReady
                        + " visibleHud=" + visibleHudFeedbackReady
                        + " heldPixels=" + visibleHud.heldItemChangedPixels()
                        + " breakPixels=" + visibleHud.breakFeedbackChangedPixels()
                        + " actionParticles=" + visibleActionParticlesReady
                        + " particlePixels=" + visibleHud.actionParticlesChangedPixels()
                        + " terminal=" + terminalBranchingReady
                        + " scavengeDepletion=" + scavengeDepletionReady
                        + " powerRepairFlow=" + powerRepairFlowReady
                        + " extractionEvent=" + extractionEventReady
                        + " hazardVariety=" + hazardVarietyReady
                        + " shelterSystem=" + shelterSystemReady
                        + " survivalNeeds=" + survivalNeedsReady
                        + " waterLoop=" + waterLoopReady
                        + " toolProgression=" + toolProgressionReady
                        + " fieldWorkshop=" + fieldWorkshopReady
                        + " fieldPower=" + fieldPowerReady
                        + " machinePower=" + machinePowerReady
                        + " midgameProgression=" + midgameProgressionReady
                        + " expeditionSafety=" + expeditionSafetyReady
                        + " advancedExpedition=" + advancedExpeditionReady
                        + " fieldRecovery=" + fieldRecoveryReady
                        + " canonicalRoute=" + canonicalRouteReady
                        + " failureRecovery=" + failureRecoveryReady
                        + " hudObjective=" + hudObjectiveStateReady
                        + " routeGuidance=" + routeWideGuidanceReady
                        + " audioCueCoverage=" + audioCueCoverageReady
                        + " corruptSave=" + corruptedSaveDetected
                        + " adapterCore=" + adapterCoreParityReady
                        + " saveContract=" + EchoAshfallGameplayContracts.LIVE_MISSION_STATE_CONTRACT_ID
        );
    }

    private static boolean fieldWorkshopReady(EchoAdapterCoreStandaloneContentBridge bridge) {
        EchoAshfallLiveMissionState mission = EchoAshfallLiveMissionState.restored(
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
                1,
                1,
                0,
                0,
                100,
                80.0D,
                80.0D,
                8.0D,
                2,
                5,
                2,
                0.0D,
                "field workshop qa"
        );
        mission.recoverScrapMetal(
                bridge.registry().requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID),
                bridge.toolProfile()
        );
        mission.craftScrapKnife(bridge.toolProfile());
        mission.recordToolMining(
                bridge.registry().requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID),
                bridge.toolProfile()
        );
        boolean workshopScrap = mission.recoverWorkshopScrap(bridge.fieldWorkshopProfile());
        boolean handRecycler = mission.markHandRecyclerBuilt(
                bridge.handRecyclerBlock(),
                bridge.fieldWorkshopProfile()
        );
        boolean casing = mission.makeMachineCasing(bridge.fieldWorkshopProfile());
        boolean fieldKit = mission.assembleWastelandFieldKit(bridge.fieldWorkshopProfile());

        return bridge.fieldWorkshopProfile().handRecyclerLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.HAND_RECYCLER_BLOCK_ID)
                && bridge.fieldWorkshopProfile().machineCasingLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.MACHINE_CASING_ITEM_ID)
                && bridge.fieldWorkshopProfile().scrapWireLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.SCRAP_WIRE_ITEM_ID)
                && bridge.fieldWorkshopProfile().scrapCircuitLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.SCRAP_CIRCUIT_ITEM_ID)
                && workshopScrap
                && handRecycler
                && casing
                && fieldKit
                && mission.handRecyclerBuilt()
                && mission.machineCasingMade()
                && mission.wastelandFieldKitAssembled()
                && mission.scrapWire() == 0
                && mission.scrapCircuit() == 0
                && mission.machineCasings() == 0;
    }

    private static boolean routeWideGuidanceReady(EchoStandalonePlayableVoxelSession session) {
        EchoAshfallLiveMissionState mission = session.mission();
        EchoAshfallPlayerFeedback feedback = EchoAshfallPlayerFeedback.from(
                mission,
                session.hotbar(),
                true,
                mission.lastMessage()
        );
        boolean requiredLabelsReady = mission.requiredObjectiveLabels().size()
                + mission.optionalObjectiveLabels().size() == mission.totalObjectives()
                && mission.requiredObjectiveLabels().stream().allMatch(EchoAshfallExperienceQaRuntime::playerFacingText);
        boolean objectiveSummariesReady = true;
        for (int index = 0; index < mission.totalObjectives(); index++) {
            String summary = mission.objectiveSummary(index);
            objectiveSummariesReady = objectiveSummariesReady
                    && playerFacingText(summary)
                    && (summary.startsWith("done:") || summary.startsWith("open:"));
        }
        return session.result().canonicalBetaRouteReady()
                && session.result().canonicalRouteStepsCompleted() == session.result().canonicalRouteStepsTotal()
                && requiredLabelsReady
                && objectiveSummariesReady
                && mission.optionalObjectiveLabels().size() >= 4
                && mission.optionalObjectiveLabels().stream().allMatch(EchoAshfallExperienceQaRuntime::playerFacingText)
                && mission.completedHistory().size() >= mission.requiredObjectiveLabels().size()
                && mission.completedHistory().stream().allMatch(EchoAshfallExperienceQaRuntime::playerFacingText)
                && mission.completedHistory().contains("Extraction beacon secured")
                && mission.terminalNotes().stream().allMatch(EchoAshfallExperienceQaRuntime::playerFacingText)
                && mission.terminalNotes().stream().anyMatch(note -> note.startsWith("Hint: "))
                && playerFacingText(mission.currentObjective())
                && playerFacingText(mission.currentHint())
                && playerFacingText(mission.shelterStatus())
                && playerFacingText(mission.extractionStatus())
                && playerFacingText(mission.hudObjectiveState())
                && feedback.coversPlayerHud()
                && feedback.currentObjective().equals(mission.currentObjective())
                && feedback.currentHint().equals(mission.currentHint())
                && feedback.actionFeedback().equals(mission.lastMessage())
                && (feedback.warningStates().contains("extraction started")
                || mission.extractionStatus().equals("EXTRACTED"))
                && feedback.warningStates().contains("power restored");
    }

    private static boolean playerFacingText(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.toLowerCase();
        return !normalized.contains("debug")
                && !normalized.contains("todo")
                && !normalized.contains("null")
                && !normalized.contains("log-only");
    }

    private static boolean fieldPowerReady(EchoAdapterCoreStandaloneContentBridge bridge) {
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
                false,
                false,
                1,
                1,
                0,
                0,
                100,
                80.0D,
                80.0D,
                8.0D,
                2,
                5,
                2,
                0.0D,
                "field power qa"
        );
        mission.recoverScrapMetal(
                bridge.registry().requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID),
                bridge.toolProfile()
        );
        mission.craftScrapKnife(bridge.toolProfile());
        mission.recordToolMining(
                bridge.registry().requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID),
                bridge.toolProfile()
        );
        mission.recoverWorkshopScrap(bridge.fieldWorkshopProfile());
        mission.markHandRecyclerBuilt(bridge.handRecyclerBlock(), bridge.fieldWorkshopProfile());
        mission.makeMachineCasing(bridge.fieldWorkshopProfile());
        mission.assembleWastelandFieldKit(bridge.fieldWorkshopProfile());
        boolean generator = mission.markMicroGeneratorBuilt(bridge.microGeneratorBlock(), bridge.fieldPowerProfile());
        boolean cable = true;
        for (int segment = 0; segment < bridge.fieldPowerProfile().cableSegmentsRequired(); segment++) {
            cable = cable && mission.routePowerCable(bridge.powerCableBlock(), bridge.fieldPowerProfile());
        }
        boolean meter = mission.installEnergyMeter(bridge.energyMeterBlock(), bridge.fieldPowerProfile());

        return bridge.fieldPowerProfile().microGeneratorLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.MICRO_GENERATOR_BLOCK_ID)
                && bridge.fieldPowerProfile().powerCableLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.POWER_CABLE_BLOCK_ID)
                && bridge.fieldPowerProfile().energyMeterLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.ENERGY_METER_BLOCK_ID)
                && bridge.fieldPowerProfile().buildMicroGeneratorMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.BUILD_MICRO_GENERATOR_MISSION_ID)
                && bridge.fieldPowerProfile().routePowerCableMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.ROUTE_POWER_CABLE_MISSION_ID)
                && bridge.fieldPowerProfile().installEnergyMeterMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.INSTALL_ENERGY_METER_MISSION_ID)
                && generator
                && cable
                && meter
                && mission.microGeneratorBuilt()
                && mission.powerCableRouted()
                && mission.energyMeterInstalled()
                && mission.fieldPowerGenerated() >= bridge.fieldPowerProfile().wattsPerGenerator()
                && mission.powerCableSegments() == bridge.fieldPowerProfile().cableSegmentsRequired()
                && mission.energyMeterReadings() >= bridge.fieldPowerProfile().meterReadingsRequired();
    }

    private static boolean machinePowerReady(EchoAdapterCoreStandaloneContentBridge bridge) {
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
                false,
                false,
                1,
                1,
                0,
                0,
                100,
                80.0D,
                80.0D,
                8.0D,
                2,
                5,
                2,
                0.0D,
                "machine power qa"
        );
        prepareFieldMicrogrid(mission, bridge);
        boolean dynamo = mission.markScrapDynamoBuilt(bridge.scrapDynamoBlock(), bridge.machinePowerProfile());
        boolean battery = mission.chargeBasicBattery(bridge.energyCellItem(), bridge.machinePowerProfile());
        boolean bank = mission.markBatteryBankBuilt(bridge.batteryBankBlock(), bridge.machinePowerProfile());
        boolean burner = mission.markThermalBurnerBuilt(bridge.thermalBurnerBlock(), bridge.machinePowerProfile());

        return bridge.machinePowerProfile().scrapDynamoLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.SCRAP_DYNAMO_BLOCK_ID)
                && bridge.machinePowerProfile().energyCellLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.ENERGY_CELL_ITEM_ID)
                && bridge.machinePowerProfile().batteryBankLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.BATTERY_BANK_BLOCK_ID)
                && bridge.machinePowerProfile().thermalBurnerLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.THERMAL_BURNER_BLOCK_ID)
                && bridge.machinePowerProfile().buildScrapDynamoMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.BUILD_SCRAP_DYNAMO_MISSION_ID)
                && bridge.machinePowerProfile().chargeBasicBatteryMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.CHARGE_BASIC_BATTERY_MISSION_ID)
                && bridge.machinePowerProfile().buildBatteryBankMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.BUILD_BATTERY_BANK_MISSION_ID)
                && bridge.machinePowerProfile().buildThermalBurnerMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.BUILD_THERMAL_BURNER_MISSION_ID)
                && dynamo
                && battery
                && bank
                && burner
                && mission.scrapDynamoBuilt()
                && mission.basicBatteryCharged()
                && mission.batteryBankBuilt()
                && mission.thermalBurnerBuilt()
                && mission.machinePowerGenerated() >= bridge.machinePowerProfile().wattsPerScrapDynamo()
                && mission.storedEnergyCells() >= bridge.machinePowerProfile().batteryBankCapacityCells()
                && mission.thermalBurnerHeat() >= bridge.machinePowerProfile().thermalBurnerHeatUnits();
    }

    private static boolean midgameProgressionReady(EchoAdapterCoreStandaloneContentBridge bridge) {
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
                false,
                false,
                1,
                1,
                0,
                0,
                100,
                84.0D,
                84.0D,
                10.0D,
                2,
                5,
                2,
                0.0D,
                "midgame progression qa"
        );
        prepareFieldMicrogrid(mission, bridge);
        prepareMachinePower(mission, bridge);
        boolean gasMask = mission.equipGasMask(bridge.gasMaskItem(), bridge.midgameProgressionProfile());
        boolean schematic = mission.findSchematicFragment(
                bridge.schematicFragmentItem(),
                bridge.midgameProgressionProfile()
        );
        boolean decode = mission.decodeFirstSchematic(bridge.midgameProgressionProfile());
        boolean press = mission.markScrapPressBuilt(bridge.scrapPressBlock(), bridge.midgameProgressionProfile());
        boolean pipes = true;
        for (int segment = 0; segment < bridge.midgameProgressionProfile().itemPipeSegmentsRequired(); segment++) {
            pipes = pipes && mission.installItemPipe(bridge.itemPipeBlock(), bridge.midgameProgressionProfile());
        }
        boolean controller = mission.markFactoryControllerBuilt(
                bridge.factoryControllerBlock(),
                bridge.midgameProgressionProfile()
        );
        boolean lab = mission.markResearchLabBuilt(bridge.researchLabBlock(), bridge.midgameProgressionProfile());
        boolean cable = true;
        for (int segment = 0; segment < bridge.midgameProgressionProfile().upgradedCableSegmentsRequired(); segment++) {
            cable = cable && mission.upgradePowerCable(
                    bridge.reinforcedPowerCableBlock(),
                    bridge.midgameProgressionProfile()
            );
        }
        boolean priority = mission.setPowerPriority(bridge.midgameProgressionProfile());
        boolean overclock = mission.overclockMachine(bridge.midgameProgressionProfile());

        return bridge.midgameProgressionProfile().gasMaskLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.GAS_MASK_ITEM_ID)
                && bridge.midgameProgressionProfile().schematicFragmentLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.SCHEMATIC_FRAGMENT_ITEM_ID)
                && bridge.midgameProgressionProfile().scrapPressLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.SCRAP_PRESS_BLOCK_ID)
                && bridge.midgameProgressionProfile().itemPipeLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.ITEM_PIPE_BLOCK_ID)
                && bridge.midgameProgressionProfile().factoryControllerLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.FACTORY_CONTROLLER_BLOCK_ID)
                && bridge.midgameProgressionProfile().researchLabLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.RESEARCH_LAB_BLOCK_ID)
                && bridge.midgameProgressionProfile().reinforcedPowerCableLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.REINFORCED_POWER_CABLE_BLOCK_ID)
                && gasMask
                && schematic
                && decode
                && press
                && pipes
                && controller
                && lab
                && cable
                && priority
                && overclock
                && mission.gasMaskEquipped()
                && mission.schematicFragmentFound()
                && mission.firstSchematicDecoded()
                && mission.scrapPressBuilt()
                && mission.itemPipeInstalled()
                && mission.factoryControllerBuilt()
                && mission.researchLabBuilt()
                && mission.powerCableUpgraded()
                && mission.powerPrioritySet()
                && mission.machineOverclocked()
                && mission.schematicFragments() >= 1
                && mission.itemPipeSegments() >= bridge.midgameProgressionProfile().itemPipeSegmentsRequired()
                && mission.upgradedPowerCableSegments()
                >= bridge.midgameProgressionProfile().upgradedCableSegmentsRequired()
                && mission.overclockHeat() >= bridge.midgameProgressionProfile().overclockHeatUnits();
    }

    private static boolean expeditionSafetyReady(EchoAdapterCoreStandaloneContentBridge bridge) {
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
                false,
                false,
                1,
                1,
                0,
                0,
                100,
                84.0D,
                84.0D,
                12.0D,
                2,
                5,
                2,
                20.0D,
                "expedition safety qa"
        );
        prepareFieldMicrogrid(mission, bridge);
        prepareMachinePower(mission, bridge);
        prepareMidgameProgression(mission, bridge);

        boolean basicFilter = mission.fixMaskFilter(bridge.basicFilterItem(), bridge.expeditionSafetyProfile());
        boolean advancedFilter = mission.craftAdvancedFilter(
                bridge.advancedFilterItem(),
                bridge.expeditionSafetyProfile()
        );
        boolean thermalArray = mission.markThermalArrayBuilt(
                bridge.thermalArrayBlock(),
                bridge.expeditionSafetyProfile()
        );
        boolean warmth = mission.warmUpAfterExposure(
                bridge.thermalArrayBlock(),
                bridge.expeditionSafetyProfile()
        );
        boolean scrubber = mission.markAtmosphericScrubberBuilt(
                bridge.atmosphericScrubberBlock(),
                bridge.expeditionSafetyProfile()
        );
        boolean cleanser = mission.markRadiationCleanserBuilt(
                bridge.radiationCleanserBlock(),
                bridge.expeditionSafetyProfile()
        );
        boolean medBay = mission.markFieldMedBayBuilt(
                bridge.fieldMedBayBlock(),
                bridge.expeditionSafetyProfile()
        );
        boolean treatment = mission.useFieldMedBay(bridge.fieldMedBayBlock(), bridge.expeditionSafetyProfile());

        return bridge.expeditionSafetyProfile().basicFilterLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.BASIC_FILTER_ITEM_ID)
                && bridge.expeditionSafetyProfile().advancedFilterLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.ADVANCED_FILTER_ITEM_ID)
                && bridge.expeditionSafetyProfile().thermalArrayLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.THERMAL_ARRAY_BLOCK_ID)
                && bridge.expeditionSafetyProfile().atmosphericScrubberLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.ATMOSPHERIC_SCRUBBER_BLOCK_ID)
                && bridge.expeditionSafetyProfile().radiationCleanserLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.RADIATION_CLEANSER_BLOCK_ID)
                && bridge.expeditionSafetyProfile().fieldMedBayLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.FIELD_MED_BAY_BLOCK_ID)
                && bridge.expeditionSafetyProfile().fixMaskFilterMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.FIX_MASK_FILTER_MISSION_ID)
                && bridge.expeditionSafetyProfile().craftAdvancedFilterMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.CRAFT_ADVANCED_FILTER_MISSION_ID)
                && bridge.expeditionSafetyProfile().buildThermalArrayMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.BUILD_THERMAL_ARRAY_MISSION_ID)
                && bridge.expeditionSafetyProfile().warmUpAfterExposureMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.WARM_UP_AFTER_EXPOSURE_MISSION_ID)
                && bridge.expeditionSafetyProfile().buildAtmosphericScrubberMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.BUILD_ATMOSPHERIC_SCRUBBER_MISSION_ID)
                && bridge.expeditionSafetyProfile().buildRadiationCleanserMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.BUILD_RADIATION_CLEANSER_MISSION_ID)
                && bridge.expeditionSafetyProfile().buildFieldMedBayMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.BUILD_FIELD_MED_BAY_MISSION_ID)
                && bridge.expeditionSafetyProfile().useFieldMedBayMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.USE_FIELD_MED_BAY_MISSION_ID)
                && basicFilter
                && advancedFilter
                && thermalArray
                && warmth
                && scrubber
                && cleanser
                && medBay
                && treatment
                && mission.basicFilterFixed()
                && mission.advancedFilterCrafted()
                && mission.thermalArrayBuilt()
                && mission.warmedAfterExposure()
                && mission.atmosphericScrubberBuilt()
                && mission.radiationCleanserBuilt()
                && mission.fieldMedBayBuilt()
                && mission.fieldMedBayUsed()
                && mission.basicFilterCharges() >= bridge.expeditionSafetyProfile().basicFilterCharges()
                && mission.advancedFilterCharges() >= bridge.expeditionSafetyProfile().advancedFilterCharges()
                && mission.warmthRecoveredSeconds() >= bridge.expeditionSafetyProfile().warmthRecoverySeconds()
                && mission.radiationCleanserCycles() >= bridge.expeditionSafetyProfile().radiationCleanserCycles()
                && mission.fieldMedBayTreatments() >= bridge.expeditionSafetyProfile().fieldMedBayTreatments();
    }

    private static boolean advancedExpeditionReady(EchoAdapterCoreStandaloneContentBridge bridge) {
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
                false,
                false,
                1,
                1,
                0,
                0,
                100,
                86.0D,
                86.0D,
                12.0D,
                2,
                5,
                2,
                20.0D,
                "advanced expedition qa"
        );
        prepareFieldMicrogrid(mission, bridge);
        prepareMachinePower(mission, bridge);
        prepareMidgameProgression(mission, bridge);
        prepareExpeditionSafety(mission, bridge);

        boolean filterWorkbench = mission.markFilterWorkbenchBuilt(
                bridge.filterWorkbenchBlock(),
                bridge.advancedExpeditionProfile()
        );
        boolean oreGrinder = mission.markOreGrinderBuilt(
                bridge.oreGrinderBlock(),
                bridge.advancedExpeditionProfile()
        );
        boolean denseAlloy = mission.findDenseAlloy(
                bridge.denseAlloyItem(),
                bridge.advancedExpeditionProfile()
        );
        boolean isotopeRefiner = mission.markIsotopeRefinerBuilt(
                bridge.isotopeRefinerBlock(),
                bridge.advancedExpeditionProfile()
        );
        boolean alloyWeapon = mission.forgeAlloyWeapon(
                bridge.alloyBladeItem(),
                bridge.advancedExpeditionProfile()
        );
        boolean alloyKit = mission.equipAlloyKit(
                bridge.alloyHelmetItem(),
                bridge.alloyChestplateItem(),
                bridge.advancedExpeditionProfile()
        );
        boolean relay = mission.activateRelayStation(
                bridge.relayStationBlock(),
                bridge.relayScannerLensItem(),
                bridge.advancedExpeditionProfile()
        );
        boolean scoutDrone = mission.buildScoutDrone(
                bridge.scoutDroneItem(),
                bridge.advancedExpeditionProfile()
        );

        return bridge.advancedExpeditionProfile().filterWorkbenchLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.FILTER_WORKBENCH_BLOCK_ID)
                && bridge.advancedExpeditionProfile().oreGrinderLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.ORE_GRINDER_BLOCK_ID)
                && bridge.advancedExpeditionProfile().isotopeRefinerLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.ISOTOPE_REFINER_BLOCK_ID)
                && bridge.advancedExpeditionProfile().relayStationLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.RELAY_STATION_BLOCK_ID)
                && bridge.advancedExpeditionProfile().denseAlloyLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.DENSE_ALLOY_ITEM_ID)
                && bridge.advancedExpeditionProfile().alloyBladeLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.ALLOY_BLADE_ITEM_ID)
                && bridge.advancedExpeditionProfile().alloyHelmetLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.ALLOY_HELMET_ITEM_ID)
                && bridge.advancedExpeditionProfile().alloyChestplateLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.ALLOY_CHESTPLATE_ITEM_ID)
                && bridge.advancedExpeditionProfile().relayScannerLensLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.RELAY_SCANNER_LENS_ITEM_ID)
                && bridge.advancedExpeditionProfile().scoutDroneItemLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.SCOUT_DRONE_ITEM_ID)
                && bridge.advancedExpeditionProfile().scoutDroneEntityContentId().equals(
                EchoAdapterCoreStandaloneContentBridge.SCOUT_DRONE_ENTITY_ID)
                && bridge.advancedExpeditionProfile().buildFilterWorkbenchMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.BUILD_FILTER_WORKBENCH_MISSION_ID)
                && bridge.advancedExpeditionProfile().buildOreGrinderMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.BUILD_ORE_GRINDER_MISSION_ID)
                && bridge.advancedExpeditionProfile().findDenseAlloyMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.FIND_DENSE_ALLOY_MISSION_ID)
                && bridge.advancedExpeditionProfile().buildIsotopeRefinerMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.BUILD_ISOTOPE_REFINER_MISSION_ID)
                && bridge.advancedExpeditionProfile().forgeAlloyWeaponMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.FORGE_ALLOY_WEAPON_MISSION_ID)
                && bridge.advancedExpeditionProfile().equipAlloyKitMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.EQUIP_ALLOY_KIT_MISSION_ID)
                && bridge.advancedExpeditionProfile().activateRelayStationMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.ACTIVATE_RELAY_STATION_MISSION_ID)
                && bridge.advancedExpeditionProfile().buildScoutDroneMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.BUILD_SCOUT_DRONE_MISSION_ID)
                && filterWorkbench
                && oreGrinder
                && denseAlloy
                && isotopeRefiner
                && alloyWeapon
                && alloyKit
                && relay
                && scoutDrone
                && mission.filterWorkbenchBuilt()
                && mission.oreGrinderBuilt()
                && mission.denseAlloyFound()
                && mission.isotopeRefinerBuilt()
                && mission.alloyWeaponForged()
                && mission.alloyKitEquipped()
                && mission.relayStationActivated()
                && mission.scoutDroneBuilt()
                && mission.alloyWeaponDurability() >= bridge.advancedExpeditionProfile().alloyWeaponDurability()
                && mission.relaySignalStrength() >= bridge.advancedExpeditionProfile().relaySignalStrength()
                && mission.scoutDroneRangeMeters() >= bridge.advancedExpeditionProfile().scoutDroneRangeMeters();
    }

    private static boolean fieldRecoveryReady(EchoAdapterCoreStandaloneContentBridge bridge) {
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
                false,
                false,
                1,
                1,
                0,
                0,
                70,
                82.0D,
                82.0D,
                55.0D,
                2,
                5,
                2,
                50.0D,
                "field recovery qa"
        );
        prepareFieldMicrogrid(mission, bridge);
        prepareMachinePower(mission, bridge);
        prepareMidgameProgression(mission, bridge);
        prepareExpeditionSafety(mission, bridge);
        prepareAdvancedExpedition(mission, bridge);

        boolean radAway = mission.useRadAway(bridge.radAwayItem(), bridge.fieldRecoveryProfile());
        boolean stim = mission.useStimPack(bridge.stimPackItem(), bridge.fieldRecoveryProfile());
        boolean warmer = mission.useHandWarmer(bridge.handWarmerItem(), bridge.fieldRecoveryProfile());
        boolean liner = mission.installThermalLiner(bridge.thermalLinerItem(), bridge.fieldRecoveryProfile());
        boolean beacon = mission.placeReturnBeacon(bridge.returnBeaconItem(), bridge.fieldRecoveryProfile());
        boolean keystone = mission.bindReturnKeystone(bridge.returnKeystoneItem(), bridge.fieldRecoveryProfile());

        return bridge.fieldRecoveryProfile().radAwayLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.RAD_AWAY_ITEM_ID)
                && bridge.fieldRecoveryProfile().stimPackLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.STIM_PACK_ITEM_ID)
                && bridge.fieldRecoveryProfile().handWarmerLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.HAND_WARMER_ITEM_ID)
                && bridge.fieldRecoveryProfile().thermalLinerLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.THERMAL_LINER_ITEM_ID)
                && bridge.fieldRecoveryProfile().returnBeaconLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.RETURN_BEACON_ITEM_ID)
                && bridge.fieldRecoveryProfile().returnKeystoneLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.RETURN_KEYSTONE_ITEM_ID)
                && bridge.fieldRecoveryProfile().useRadAwayMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.USE_RAD_AWAY_MISSION_ID)
                && bridge.fieldRecoveryProfile().useStimPackMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.USE_STIM_PACK_MISSION_ID)
                && bridge.fieldRecoveryProfile().useHandWarmerMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.USE_HAND_WARMER_MISSION_ID)
                && bridge.fieldRecoveryProfile().installThermalLinerMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.INSTALL_THERMAL_LINER_MISSION_ID)
                && bridge.fieldRecoveryProfile().placeReturnBeaconMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.PLACE_RETURN_BEACON_MISSION_ID)
                && bridge.fieldRecoveryProfile().bindReturnKeystoneMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.BIND_RETURN_KEYSTONE_MISSION_ID)
                && radAway
                && stim
                && warmer
                && liner
                && beacon
                && keystone
                && mission.radAwayUsed()
                && mission.stimPackUsed()
                && mission.handWarmerUsed()
                && mission.thermalLinerInstalled()
                && mission.returnBeaconPlaced()
                && mission.returnKeystoneBound()
                && mission.radAwayDoses() >= 1
                && mission.stimPackDoses() >= 1
                && mission.handWarmerCharges() >= 1
                && mission.thermalLinerWarmthSeconds()
                >= bridge.fieldRecoveryProfile().thermalLinerWarmthSeconds()
                && mission.returnBeaconSignalStrength()
                >= bridge.fieldRecoveryProfile().returnBeaconSignalStrength()
                && mission.returnKeystoneCharges() >= bridge.fieldRecoveryProfile().returnKeystoneCharges();
    }

    private static boolean toolProgressionReady(EchoAdapterCoreStandaloneContentBridge bridge) {
        EchoAshfallLiveMissionState mission = new EchoAshfallLiveMissionState();
        EchoVoxelBlock rustedDebris = bridge.registry().requireLiveVoxelBlock(
                EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID);
        boolean recoveredScrap = mission.recoverScrapMetal(rustedDebris, bridge.toolProfile());
        boolean craftedKnife = mission.craftScrapKnife(bridge.toolProfile());
        EchoVoxelWorld world = ashfallWorld(bridge);
        world.setBlockAt(2, 4, 4, rustedDebris);
        double handRequiredSeconds = world.attemptBreakBlock(
                2,
                4,
                4,
                0.0D,
                bridge.toolProfile().speedFor(rustedDebris, false)
        ).requiredSeconds();
        double toolRequiredSeconds = world.attemptBreakBlock(
                2,
                4,
                4,
                0.0D,
                bridge.toolProfile().speedFor(rustedDebris, true)
        ).requiredSeconds();
        boolean minedWithTool = world.attemptBreakBlock(
                2,
                4,
                4,
                toolRequiredSeconds,
                bridge.toolProfile().speedFor(rustedDebris, true)
        ).broken() && mission.recordToolMining(rustedDebris, bridge.toolProfile());

        return bridge.toolProfile().scrapKnifeLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.SCRAP_KNIFE_ITEM_ID)
                && bridge.toolProfile().scrapMetalLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.SCRAP_METAL_ITEM_ID)
                && bridge.toolProfile().craftScrapKnifeMissionId().equals(
                EchoAdapterCoreStandaloneContentBridge.CRAFT_SCRAP_KNIFE_MISSION_ID)
                && bridge.toolProfile().scrapKnifeRecipeId().equals(
                EchoAdapterCoreStandaloneContentBridge.SCRAP_KNIFE_RECIPE_ID)
                && recoveredScrap
                && craftedKnife
                && minedWithTool
                && mission.scrapKnifeCrafted()
                && mission.toolAssistedMining()
                && mission.toolMiningBlocksBroken() == 1
                && mission.scrapKnifeDurability() == bridge.toolProfile().scrapKnifeMaxDurability()
                - bridge.toolProfile().durabilityCostFor(rustedDebris)
                && toolRequiredSeconds < handRequiredSeconds;
    }

    private static boolean waterLoopReady(EchoAdapterCoreStandaloneContentBridge bridge) {
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
                true,
                true,
                true,
                false,
                false,
                1,
                1,
                0,
                0,
                100,
                80.0D,
                80.0D,
                8.0D,
                2,
                5,
                2,
                8.0D,
                0.0D,
                0.0D,
                "water loop qa",
                List.of()
        );
        boolean rainBuilt = mission.markRainCollectorBuilt(
                bridge.rainCollectorBlock(),
                bridge.waterLoopProfile()
        );
        boolean dirtyCollected = mission.collectRainWater(bridge.waterLoopProfile());
        mission.forageWastelandFood(bridge.waterLoopProfile());
        mission.forageWastelandFood(bridge.waterLoopProfile());
        boolean purifierBuilt = mission.markWaterPurifierBuilt(
                bridge.waterPurifierBlock(),
                bridge.waterLoopProfile()
        );
        boolean firstPurify = mission.purifyWater(bridge.waterLoopProfile());
        boolean secondPurify = mission.purifyWater(bridge.waterLoopProfile());

        return bridge.waterLoopProfile().rainCollectorLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.RAIN_COLLECTOR_BLOCK_ID)
                && bridge.waterLoopProfile().waterPurifierLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.WATER_PURIFIER_BLOCK_ID)
                && bridge.waterLoopProfile().dirtyWaterLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.DIRTY_WATER_ITEM_ID)
                && rainBuilt
                && dirtyCollected
                && purifierBuilt
                && firstPurify
                && secondPurify
                && mission.rainCollectorBuilt()
                && mission.emergencyWaterLoopSecured()
                && mission.rainCollectorCollections() == 1
                && mission.foragedFood()
                && mission.foragedFoodBundles() == 2
                && mission.rationsStockpiled()
                && mission.waterPurifierBuilt()
                && mission.waterPurifierCycles() == 2
                && mission.filteredWaterBottles() == 2
                && mission.cleanWaterStockpiled()
                && mission.cleanWaterStockpile() >= bridge.waterLoopProfile().cleanWaterStockpileTarget();
    }

    private static boolean survivalNeedsReady(EchoAdapterCoreStandaloneContentBridge bridge) {
        EchoAshfallLiveMissionState rationMission = EchoAshfallLiveMissionState.restored(
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
                1,
                1,
                0,
                0,
                50,
                5.0D,
                4.0D,
                12.0D,
                0,
                0,
                0,
                0.0D,
                0.0D,
                0.0D,
                "survival needs qa",
                List.of()
        );
        boolean waterUsed = rationMission.useWaterRation(bridge.survivalProfile());
        boolean foodUsed = rationMission.useFoodRation(bridge.survivalProfile());

        EchoAshfallLiveMissionState deprivationMission = EchoAshfallLiveMissionState.restored(
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
                50,
                0.0D,
                0.0D,
                0.0D,
                0,
                0,
                0,
                0.0D,
                0.0D,
                0.0D,
                "deprivation qa",
                List.of()
        );
        EchoVoxelWorld world = ashfallWorld(bridge);
        deprivationMission.tick(world, hazardPlayer(world, 2.5D, 2.5D), true, 60.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());

        return bridge.survivalProfile().waterLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.WATER_RATION_ITEM_ID)
                && bridge.survivalProfile().foodLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.FIELD_RATION_ITEM_ID)
                && waterUsed
                && foodUsed
                && rationMission.hydrationRecovered() >= bridge.survivalProfile().waterHydrationRecovery()
                && rationMission.hungerRecovered() >= bridge.survivalProfile().foodHungerRecovery()
                && rationMission.playerHealth() > 50
                && deprivationMission.dehydrationDamagePulses() > 0
                && deprivationMission.starvationDamagePulses() > 0
                && deprivationMission.playerHealth() < 50;
    }

    private static boolean shelterSystemReady(EchoAdapterCoreStandaloneContentBridge bridge) {
        EchoVoxelWorld world = ashfallWorld(bridge);
        EchoAshfallLiveMissionState rest = EchoAshfallLiveMissionState.restored(
                true,
                true,
                true,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                1,
                1,
                0,
                0,
                90,
                50.0D,
                45.0D,
                30.0D,
                2,
                5,
                2,
                0.0D,
                "shelter rest qa"
        );
        world.setBlockAt(2, 4, 2, EchoVoxelBlock.AIR);
        double exposureBefore = rest.ashExposure();
        int healthBefore = rest.playerHealth();
        rest.tick(world, hazardPlayer(world, 2.5D, 2.5D), true, 60.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());

        EchoAshfallLiveMissionState storm = EchoAshfallLiveMissionState.restored(
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
                true,
                true,
                true,
                false,
                false,
                1,
                1,
                0,
                0,
                100,
                90.0D,
                90.0D,
                8.0D,
                7,
                5,
                1,
                8.0D,
                0.0D,
                0.0D,
                "shelter storm qa",
                List.of()
        );
        world.setBlockAt(7, 4, 1, EchoVoxelBlock.AIR);
        double integrityBefore = storm.shelterIntegrity();
        storm.tick(world, hazardPlayer(world, 7.5D, 1.5D), true, 12.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());

        return bridge.shelterProfile().anchorLiveVoxelId().equals(
                EchoAdapterCoreStandaloneContentBridge.SHELTER_ANCHOR_BLOCK_ID)
                && rest.shelterIntegrity() >= 99.0D
                && rest.shelterRestSeconds() >= 60.0D
                && rest.ashExposure() < exposureBefore
                && rest.playerHealth() > healthBefore
                && rest.shelterStatus().contains("SHELTER")
                && storm.shelterStormDamage() > 0.0D
                && storm.shelterIntegrity() < integrityBefore
                && storm.extractionArmed();
    }

    private static boolean hazardVarietyReady(EchoAdapterCoreStandaloneContentBridge bridge) {
        EchoVoxelWorld world = ashfallWorld(bridge);
        EchoAshfallLiveMissionState mission = new EchoAshfallLiveMissionState();
        world.setBlockAt(10, 4, 2, bridge.registry().requireLiveVoxelBlock(
                EchoAdapterCoreStandaloneContentBridge.TOXIC_ASH_BLOCK_ID));
        EchoVoxelPlayerState toxicPlayer = hazardPlayer(world, 10.5D, 2.5D);
        mission.tick(world, toxicPlayer, true, 6.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
        double toxicSeconds = mission.toxicAshExposureSeconds();
        double exposureAfterToxic = mission.ashExposure();

        world.setBlockAt(8, 4, 2, bridge.registry().requireLiveVoxelBlock(
                EchoAdapterCoreStandaloneContentBridge.SCORCHED_BASALT_BLOCK_ID));
        mission.tick(world, hazardPlayer(world, 8.5D, 2.5D), true, 6.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
        double hotSeconds = mission.hotAshExposureSeconds();
        double exposureAfterHot = mission.ashExposure();

        world.setBlockAt(9, 4, 2, bridge.registry().requireLiveVoxelBlock(
                EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID));
        int healthBeforeUnstable = mission.playerHealth();
        mission.tick(world, hazardPlayer(world, 9.5D, 2.5D), true, 2.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());

        world.setBlockAt(11, 4, 4, bridge.registry().requireLiveVoxelBlock(
                EchoAdapterCoreStandaloneContentBridge.DAMAGED_POWER_NODE_BLOCK_ID));
        int healthBeforeElectrical = mission.playerHealth();
        mission.tick(world, hazardPlayer(world, 11.5D, 4.5D), true, 2.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());

        EchoAshfallLiveMissionState extractionMission = EchoAshfallLiveMissionState.restored(
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
                true,
                true,
                true,
                false,
                false,
                1,
                1,
                0,
                0,
                100,
                90.0D,
                90.0D,
                8.0D,
                0,
                0,
                0,
                8.0D,
                0.0D,
                0.0D,
                "hazard variety qa",
                List.of()
        );
        extractionMission.tick(world, hazardPlayer(world, 7.5D, 1.5D), true, 4.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());

        return bridge.hazardTable().contactRuleCount() >= 4
                && toxicSeconds >= 6.0D
                && hotSeconds >= 6.0D
                && exposureAfterHot > exposureAfterToxic
                && mission.unstableGroundStrikes() >= 1
                && mission.playerHealth() < healthBeforeUnstable
                && mission.electricalDischargeHits() >= 1
                && mission.playerHealth() < healthBeforeElectrical
                && extractionMission.extractionStormExposureSeconds() >= 4.0D
                && extractionMission.extractionArmed();
    }

    private static boolean scavengeDepletionReady(EchoAdapterCoreStandaloneContentBridge bridge) {
        EchoAshfallLiveMissionState mission = new EchoAshfallLiveMissionState();
        int initialFood = mission.foodRations();
        String sourceKey = "block:2,4,3";
        EchoAshfallLiveMissionState.ScavengeReward first = mission.scavenge(
                bridge.registry().requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID),
                bridge.scavengeTable(),
                sourceKey
        );
        EchoAshfallLiveMissionState.ScavengeReward second = mission.scavenge(
                bridge.registry().requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID),
                bridge.scavengeTable(),
                sourceKey
        );
        return first.rewarded()
                && first.adapterCoreBacked()
                && !second.rewarded()
                && mission.foodRations() == initialFood + 1
                && mission.scavengedSupplyCaches() == 1
                && mission.scavengedLootKeys().contains(sourceKey);
    }

    private static boolean powerRepairFlowReady(EchoAdapterCoreStandaloneContentBridge bridge) {
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
                false,
                false,
                1,
                1,
                1,
                100,
                90.0D,
                90.0D,
                4.0D,
                "power qa"
        );
        EchoVoxelWorld world = ashfallWorld(bridge);
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
                0,
                6.0D
        );
        prepareFieldMicrogrid(mission, bridge);
        prepareMachinePower(mission, bridge);
        prepareMidgameProgression(mission, bridge);
        prepareExpeditionSafety(mission, bridge);
        mission.interact(new EchoVoxelHit(
                11,
                4,
                4,
                0,
                1,
                0,
                bridge.registry().requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.DAMAGED_POWER_NODE_BLOCK_ID),
                0.0D
        ), player);
        boolean started = mission.powerNodeDiscovered()
                && mission.powerRepairStarted()
                && !mission.powerRepaired()
                && mission.repairKits() == 0
                && mission.terminalState().equals("REBOOTING");
        mission.tick(world, player, false, 8.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
        boolean stabilized = mission.powerRebootStabilized()
                && mission.terminalState().equals("AWAITING CONFIRMATION");
        mission.interact(new EchoVoxelHit(
                3,
                4,
                3,
                0,
                1,
                0,
                bridge.registry().requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.FIELD_TERMINAL_BLOCK_ID),
                0.0D
        ), player);
        return started
                && stabilized
                && mission.powerTerminalConfirmed()
                && mission.powerRepaired()
                && mission.terminalState().equals("ONLINE");
    }

    private static void prepareFieldMicrogrid(
            EchoAshfallLiveMissionState mission,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        mission.recoverScrapMetal(
                bridge.registry().requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID),
                bridge.toolProfile()
        );
        mission.craftScrapKnife(bridge.toolProfile());
        mission.recordToolMining(
                bridge.registry().requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID),
                bridge.toolProfile()
        );
        mission.recoverWorkshopScrap(bridge.fieldWorkshopProfile());
        mission.markHandRecyclerBuilt(bridge.handRecyclerBlock(), bridge.fieldWorkshopProfile());
        mission.makeMachineCasing(bridge.fieldWorkshopProfile());
        mission.assembleWastelandFieldKit(bridge.fieldWorkshopProfile());
        mission.markMicroGeneratorBuilt(bridge.microGeneratorBlock(), bridge.fieldPowerProfile());
        for (int segment = 0; segment < bridge.fieldPowerProfile().cableSegmentsRequired(); segment++) {
            mission.routePowerCable(bridge.powerCableBlock(), bridge.fieldPowerProfile());
        }
        mission.installEnergyMeter(bridge.energyMeterBlock(), bridge.fieldPowerProfile());
    }

    private static void prepareMachinePower(
            EchoAshfallLiveMissionState mission,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        mission.markScrapDynamoBuilt(bridge.scrapDynamoBlock(), bridge.machinePowerProfile());
        mission.chargeBasicBattery(bridge.energyCellItem(), bridge.machinePowerProfile());
        mission.markBatteryBankBuilt(bridge.batteryBankBlock(), bridge.machinePowerProfile());
        mission.markThermalBurnerBuilt(bridge.thermalBurnerBlock(), bridge.machinePowerProfile());
    }

    private static void prepareMidgameProgression(
            EchoAshfallLiveMissionState mission,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        mission.equipGasMask(bridge.gasMaskItem(), bridge.midgameProgressionProfile());
        mission.findSchematicFragment(bridge.schematicFragmentItem(), bridge.midgameProgressionProfile());
        mission.decodeFirstSchematic(bridge.midgameProgressionProfile());
        mission.markScrapPressBuilt(bridge.scrapPressBlock(), bridge.midgameProgressionProfile());
        for (int segment = 0; segment < bridge.midgameProgressionProfile().itemPipeSegmentsRequired(); segment++) {
            mission.installItemPipe(bridge.itemPipeBlock(), bridge.midgameProgressionProfile());
        }
        mission.markFactoryControllerBuilt(bridge.factoryControllerBlock(), bridge.midgameProgressionProfile());
        mission.markResearchLabBuilt(bridge.researchLabBlock(), bridge.midgameProgressionProfile());
        for (int segment = 0; segment < bridge.midgameProgressionProfile().upgradedCableSegmentsRequired(); segment++) {
            mission.upgradePowerCable(bridge.reinforcedPowerCableBlock(), bridge.midgameProgressionProfile());
        }
        mission.setPowerPriority(bridge.midgameProgressionProfile());
        mission.overclockMachine(bridge.midgameProgressionProfile());
    }

    private static void prepareExpeditionSafety(
            EchoAshfallLiveMissionState mission,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        mission.fixMaskFilter(bridge.basicFilterItem(), bridge.expeditionSafetyProfile());
        mission.craftAdvancedFilter(bridge.advancedFilterItem(), bridge.expeditionSafetyProfile());
        mission.markThermalArrayBuilt(bridge.thermalArrayBlock(), bridge.expeditionSafetyProfile());
        mission.warmUpAfterExposure(bridge.thermalArrayBlock(), bridge.expeditionSafetyProfile());
        mission.markAtmosphericScrubberBuilt(bridge.atmosphericScrubberBlock(), bridge.expeditionSafetyProfile());
        mission.markRadiationCleanserBuilt(bridge.radiationCleanserBlock(), bridge.expeditionSafetyProfile());
        mission.markFieldMedBayBuilt(bridge.fieldMedBayBlock(), bridge.expeditionSafetyProfile());
        mission.useFieldMedBay(bridge.fieldMedBayBlock(), bridge.expeditionSafetyProfile());
    }

    private static void prepareAdvancedExpedition(
            EchoAshfallLiveMissionState mission,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        mission.markFilterWorkbenchBuilt(bridge.filterWorkbenchBlock(), bridge.advancedExpeditionProfile());
        mission.markOreGrinderBuilt(bridge.oreGrinderBlock(), bridge.advancedExpeditionProfile());
        mission.findDenseAlloy(bridge.denseAlloyItem(), bridge.advancedExpeditionProfile());
        mission.markIsotopeRefinerBuilt(bridge.isotopeRefinerBlock(), bridge.advancedExpeditionProfile());
        mission.forgeAlloyWeapon(bridge.alloyBladeItem(), bridge.advancedExpeditionProfile());
        mission.equipAlloyKit(
                bridge.alloyHelmetItem(),
                bridge.alloyChestplateItem(),
                bridge.advancedExpeditionProfile()
        );
        mission.activateRelayStation(
                bridge.relayStationBlock(),
                bridge.relayScannerLensItem(),
                bridge.advancedExpeditionProfile()
        );
        mission.buildScoutDrone(bridge.scoutDroneItem(), bridge.advancedExpeditionProfile());
    }

    private static boolean extractionEventReady(EchoAdapterCoreStandaloneContentBridge bridge) {
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
                true,
                true,
                true,
                false,
                false,
                1,
                1,
                0,
                0,
                100,
                90.0D,
                90.0D,
                8.0D,
                0,
                0,
                0,
                8.0D,
                0.0D,
                0.0D,
                "extraction qa",
                List.of()
        );
        EchoVoxelWorld world = ashfallWorld(bridge);
        EchoVoxelPlayerState extractionPlayer = new EchoVoxelPlayerState(
                7.5D,
                world.spawnY(),
                1.5D,
                0.0D,
                world.spawnYawDegrees(),
                -20.0D,
                true,
                false,
                false,
                0,
                6.0D
        );
        double exposureBefore = mission.ashExposure();
        mission.tick(world, extractionPlayer, true, 6.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
        boolean armed = mission.extractionArmed()
                && !mission.extracted()
                && mission.extractionCountdownSeconds() >= 6.0D
                && mission.extractionStatus().startsWith("EXTRACTION COUNTDOWN")
                && mission.ashExposure() > exposureBefore;
        mission.tick(world, extractionPlayer, true, 6.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
        return armed
                && mission.extractionCountdownComplete()
                && mission.extracted()
                && mission.status().equals("COMPLETED")
                && mission.extractionStatus().equals("EXTRACTED");
    }

    private static EchoVoxelWorld ashfallWorld(EchoAdapterCoreStandaloneContentBridge bridge) {
        return dev.echo.standalone.runtime.player.EchoVoxelSessionProfiles
                .ashfallCrashSite(
                        bridge.registry()::requireLiveVoxelBlock,
                        bridge.runtimeMarkerBlock(),
                        0
                )
                .generate(42L, 0);
    }

    private static EchoVoxelPlayerState hazardPlayer(EchoVoxelWorld world, double x, double z) {
        return new EchoVoxelPlayerState(
                x,
                5.0D,
                z,
                0.0D,
                world.spawnYawDegrees(),
                -20.0D,
                true,
                false,
                false,
                0,
                6.0D
        );
    }
}
