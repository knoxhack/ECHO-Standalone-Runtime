package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.audio.EchoAudioCuePlan;
import dev.echo.standalone.runtime.audio.EchoAudioCuePlanner;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackEvent;
import dev.echo.standalone.runtime.audio.EchoAudioRuntime;
import dev.echo.standalone.runtime.audio.EchoAudioRuntimeResult;
import dev.echo.standalone.runtime.audio.EchoAudioVolumeProfiles;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.ui.EchoStaticScreen;
import dev.echo.standalone.runtime.ui.EchoUiRuntime;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiTheme;
import dev.echo.standalone.runtime.world.EchoVoxelHit;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoAshfallAudioCueCoverageRuntime {
    private static final String ADAPTERCORE_TERMINAL_SOUND = "echoashfallprotocol:sound/ui.echo_message";

    public EchoAshfallAudioCueCoverageResult run(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite()
        );
        EchoEntityRuntimeResult entities = new EchoEntityRuntime().createDebugEntities(services, world);
        EchoItemRuntimeResult items = new EchoItemRuntime().createDebugInventory(services, entities);
        EchoGameplayRuntimeResult gameplay = new EchoGameplayRuntime().createDebugGameplay(
                services,
                world,
                entities,
                items
        );
        EchoUiRuntimeResult ui = new EchoUiRuntime().boot(
                services,
                new EchoStaticScreen(
                        "ashfall-terminal",
                        "Ashfall Terminal",
                        List.of("Power diagnostics online", "Extraction beacon armed"),
                        "terminal:input"
                ),
                EchoUiTheme.defaultTerminal()
        );
        EchoAudioRuntimeResult audio = new EchoAudioRuntime().createRecordingDebugAudio(
                services,
                world,
                gameplay,
                ui.frame().screen().id(),
                EchoAudioVolumeProfiles.resolve(EchoAudioVolumeProfiles.ASHFALL_SURVIVAL_MIX_PROFILE_ID)
        );

        EchoAdapterCoreRegistryEntry terminalSound = bridge.registry().requireContentId(ADAPTERCORE_TERMINAL_SOUND);
        boolean adapterCoreSoundBacked = terminalSound.domain() == EchoAdapterCoreDomain.SOUNDS
                && terminalSound.contentKind() == EchoAdapterCoreContentKind.SOUND_EVENT
                && terminalSound.standaloneRuntimeId().equals("ashfall:radio_static")
                && audio.clipRegistry().find(terminalSound.standaloneRuntimeId()).isPresent();

        AshfallCueState cueState = deriveCueState(bridge);
        ArrayList<String> cueKeys = new ArrayList<>();
        if (cueState.miningHit()) {
            cueKeys.add(EchoAudioCuePlanner.CUE_MINING_HIT);
        }
        if (cueState.blockBreak()) {
            cueKeys.add(EchoAudioCuePlanner.CUE_BLOCK_BREAK);
        }
        if (cueState.itemPickup()) {
            cueKeys.add(EchoAudioCuePlanner.CUE_ITEM_PICKUP);
        }
        if (cueState.waterConsumed()) {
            cueKeys.add(EchoAudioCuePlanner.CUE_CONSUME_WATER);
        }
        if (cueState.foodConsumed()) {
            cueKeys.add(EchoAudioCuePlanner.CUE_CONSUME_FOOD);
        }
        if (cueState.terminalOnline() && adapterCoreSoundBacked) {
            cueKeys.add(EchoAudioCuePlanner.CUE_TERMINAL_BEEP);
        }
        if (cueState.powerRepaired()) {
            cueKeys.add(EchoAudioCuePlanner.CUE_POWER_REPAIR);
        }
        if (cueState.extractionArmed()) {
            cueKeys.add(EchoAudioCuePlanner.CUE_EXTRACTION_BEACON);
        }
        if (cueState.dangerAlert()) {
            cueKeys.add(EchoAudioCuePlanner.CUE_DANGER_ALERT);
        }

        EchoAudioCuePlan gameplayCues = audio.cuePlanner().ashfallGameplayCues(cueKeys, world.world().tick() + 64L);
        List<EchoAudioPlaybackEvent> gameplayEvents = gameplayCues.requests().stream()
                .map(audio.mixer()::submit)
                .toList();
        List<EchoAudioPlaybackEvent> allEvents = audio.backend().events();
        boolean windAmbienceReady = audio.initialEvents().stream()
                .anyMatch(event -> event.clip().clipId().equals("ashfall:ambience_ash_storm")
                        && event.reason().contains("weather="));
        boolean miningHitReady = eventPresent(gameplayEvents, "ashfall:block_mining_hit", EchoAudioCuePlanner.CUE_MINING_HIT);
        boolean blockBreakReady = eventPresent(gameplayEvents, "ashfall:block_break", EchoAudioCuePlanner.CUE_BLOCK_BREAK);
        boolean itemPickupReady = eventPresent(gameplayEvents, "ashfall:item_pickup", EchoAudioCuePlanner.CUE_ITEM_PICKUP);
        boolean waterFoodUseReady = eventPresent(gameplayEvents, "ashfall:consume_water", EchoAudioCuePlanner.CUE_CONSUME_WATER)
                && eventPresent(gameplayEvents, "ashfall:consume_food", EchoAudioCuePlanner.CUE_CONSUME_FOOD);
        boolean terminalBeepReady = eventPresent(gameplayEvents, "ashfall:radio_static", EchoAudioCuePlanner.CUE_TERMINAL_BEEP)
                && gameplayEvents.stream().anyMatch(event -> event.reason().contains(ADAPTERCORE_TERMINAL_SOUND));
        boolean powerRepairReady = eventPresent(gameplayEvents, "ashfall:power_repair", EchoAudioCuePlanner.CUE_POWER_REPAIR);
        boolean extractionBeaconReady = eventPresent(gameplayEvents, "ashfall:extraction_beacon", EchoAudioCuePlanner.CUE_EXTRACTION_BEACON);
        boolean dangerAlertReady = eventPresent(gameplayEvents, "ashfall:danger_alert", EchoAudioCuePlanner.CUE_DANGER_ALERT);

        int diagnostics = 0;
        diagnostics += adapterCoreSoundBacked ? 1 : 0;
        diagnostics += windAmbienceReady ? 1 : 0;
        diagnostics += miningHitReady ? 1 : 0;
        diagnostics += blockBreakReady ? 1 : 0;
        diagnostics += itemPickupReady ? 1 : 0;
        diagnostics += waterFoodUseReady ? 1 : 0;
        diagnostics += terminalBeepReady ? 1 : 0;
        diagnostics += powerRepairReady ? 1 : 0;
        diagnostics += extractionBeaconReady ? 1 : 0;
        diagnostics += dangerAlertReady ? 1 : 0;

        audio.backend().close();
        return new EchoAshfallAudioCueCoverageResult(
                adapterCoreSoundBacked,
                windAmbienceReady,
                miningHitReady,
                blockBreakReady,
                itemPickupReady,
                waterFoodUseReady,
                terminalBeepReady,
                powerRepairReady,
                extractionBeaconReady,
                dangerAlertReady,
                diagnostics,
                allEvents.size(),
                "adapterCoreSound=" + adapterCoreSoundBacked
                        + " initial=" + audio.initialEvents().size()
                        + " gameplay=" + gameplayEvents.size()
                        + " events=" + allEvents.size()
                        + " cues=" + cueKeys
                        + " terminalSound=" + terminalSound.standaloneRuntimeId()
        );
    }

    private static AshfallCueState deriveCueState(EchoAdapterCoreStandaloneContentBridge bridge) {
        EchoVoxelWorld world = dev.echo.standalone.runtime.player.EchoVoxelSessionProfiles
                .ashfallCrashSite(
                        bridge.registry()::requireLiveVoxelBlock,
                        bridge.runtimeMarkerBlock(),
                        0
                )
                .generate(42L, 0);
        EchoVoxelPlayerState player = new EchoVoxelPlayerState(
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
        EchoAshfallLiveMissionState mission = new EchoAshfallLiveMissionState();
        boolean miningHit = bridge.registry()
                .requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.SCORCHED_BASALT_BLOCK_ID)
                .hardness() > 0.0D;
        boolean blockBreak = mission.markHazardCleared(
                bridge.registry().requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.ASH_HAZARD_MARKER_BLOCK_ID));
        boolean itemPickup = mission.scavenge(
                bridge.registry().requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID),
                bridge.scavengeTable(),
                "audio-cue:rusted-debris"
        ).rewarded();
        boolean waterConsumed = mission.useWaterRation(bridge.survivalProfile()) && mission.waterUsed();
        boolean foodConsumed = mission.useFoodRation(bridge.survivalProfile()) && mission.foodUsed();

        EchoAshfallLiveMissionState powered = EchoAshfallLiveMissionState.restored(
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
                "audio cue power"
        );
        prepareFieldMicrogrid(powered, bridge);
        prepareMachinePower(powered, bridge);
        prepareMidgameProgression(powered, bridge);
        prepareExpeditionSafety(powered, bridge);
        powered.interact(hit(11, 4, 4, bridge, EchoAdapterCoreStandaloneContentBridge.DAMAGED_POWER_NODE_BLOCK_ID), player);
        powered.tick(world, player, false, 8.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
        powered.interact(hit(3, 4, 3, bridge, EchoAdapterCoreStandaloneContentBridge.FIELD_TERMINAL_BLOCK_ID), player);

        EchoAshfallLiveMissionState extraction = EchoAshfallLiveMissionState.restored(
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
                "audio cue extraction",
                List.of()
        );
        double exposureBefore = extraction.ashExposure();
        extraction.tick(world, player, true, 4.0D,
                bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());

        return new AshfallCueState(
                miningHit,
                blockBreak,
                itemPickup,
                waterConsumed,
                foodConsumed,
                mission.terminalOnline() || powered.terminalOnline(),
                powered.powerRepaired(),
                extraction.extractionArmed(),
                extraction.ashExposure() > exposureBefore
        );
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

    private static EchoVoxelHit hit(
            int x,
            int y,
            int z,
            EchoAdapterCoreStandaloneContentBridge bridge,
            String blockId
    ) {
        return new EchoVoxelHit(
                x,
                y,
                z,
                0,
                1,
                0,
                bridge.registry().requireLiveVoxelBlock(blockId),
                0.0D
        );
    }

    private static boolean eventPresent(
            List<EchoAudioPlaybackEvent> events,
            String clipId,
            String cueKey
    ) {
        return events.stream()
                .anyMatch(event -> event.clip().clipId().equals(clipId)
                        && event.reason().contains("cue=" + cueKey));
    }

    private record AshfallCueState(
            boolean miningHit,
            boolean blockBreak,
            boolean itemPickup,
            boolean waterConsumed,
            boolean foodConsumed,
            boolean terminalOnline,
            boolean powerRepaired,
            boolean extractionArmed,
            boolean dangerAlert
    ) {
    }
}
