package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoStandalonePlayableVoxelResult;
import dev.echo.standalone.runtime.app.EchoStandalonePlayableVoxelRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

public final class EchoRuntimePlayableVoxelSmokeHarness {
    private EchoRuntimePlayableVoxelSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        EchoStandalonePlayableVoxelResult result = new EchoStandalonePlayableVoxelRuntime().run(bridge);

        require(result.betaPlayableCoreReady(), "playable voxel runtime should pass the beta core loop: " + result.summary());
        require(result.adapterCoreBindingCount() == bridge.bindingCount(),
                "playable voxel runtime should report AdapterCore binding count");
        require(result.adapterCoreMultiRuntimeReady(),
                "playable voxel runtime should require NeoForge, ECHO Native Loader, and standalone adapters");
        require(result.playerSpawned(), "player should spawn into the generated Ashfall crash-site world");
        require(result.fieldManualRead(), "starter field manual should be read through a real item-use mission path");
        require(result.raycastPickedBlock(), "player raycast should pick a block");
        require(result.pickedBlockId().startsWith("echoashfallprotocol:"),
                "picked block should come from AdapterCore Ashfall content");
        require(result.blockBroken(), "player should break the targeted block");
        require(result.blockBreakRequiredSeconds() > 0.0D
                        && result.blockBreakAppliedSeconds() >= result.blockBreakRequiredSeconds(),
                "left-click mining should respect AdapterCore block hardness timing");
        require(result.hotbarPickup(), "broken block should enter hotbar inventory");
        require(result.blockPlaced(), "player should place a selected hotbar block");
        require(result.placedBlockId().equals("echoadaptercore:runtime_marker_block"),
                "placed block should be the AdapterCore runtime marker");
        require(result.mouseLookApplied(),
                "mouse look should update the voxel camera before gameplay actions");
        require(result.allHotbarSlotsSelectable(),
                "number keys 1-9 should select every hotbar slot");
        require(result.rightClickUseOrPlace(),
                "right-click use/place should drive AdapterCore-backed place and mission interactions");
        require(result.inventoryToggleReleasesMouse() && result.escapePauseReleasesMouse(),
                "E inventory and Esc pause should block gameplay and release mouse capture");
        require(result.baselineWalkingSurvivalMinutes() >= 30.0D
                        && result.baselineWalkingSurvivalMinutes() <= 60.0D,
                "baseline Ashfall resource pacing should support a 30-60 minute survival loop");
        require(result.canonicalBetaRouteReady(),
                "canonical beta route should complete wake, shelter, ash pressure, terminal, cache, supplies, power, hold, and extraction");
        require(result.firstShelterMinutes() >= 2.0D && result.firstShelterMinutes() <= 5.0D,
                "first shelter pacing should land within 2-5 minutes");
        require(result.firstWaterUseMinutes() >= 3.0D && result.firstWaterUseMinutes() <= 8.0D,
                "first water use pacing should land within 3-8 minutes");
        require(result.firstHazardWarningMinutes() < 3.0D,
                "first hazard warning should arrive in under three minutes");
        require(result.terminalOnlineMinutes() >= 8.0D && result.terminalOnlineMinutes() <= 15.0D,
                "terminal online pacing should land within 8-15 minutes");
        require(result.powerRestoredMinutes() >= 20.0D && result.powerRestoredMinutes() <= 40.0D,
                "power restoration pacing should land within 20-40 minutes");
        require(result.extractionCompleteMinutes() >= 30.0D && result.extractionCompleteMinutes() <= 60.0D,
                "extraction pacing should land within 30-60 minutes");
        require(result.failureStatesReady() && result.recoveryPathsReady(),
                "dehydration, starvation, ash, storm shelter, missing repair, terminal, early extraction, and recovery paths should be real");
        require(result.hudObjectiveStateReady(),
                "Agent 3 should receive useful objective, terminal, extraction, shelter, and hint state");
        require(result.canonicalRouteStepsCompleted() == result.canonicalRouteStepsTotal(),
                "route smoke should complete every canonical beta step");
        require(result.survivalSecondsSimulated() >= 7.0D,
                "deterministic playable loop should simulate elapsed survival time");
        require(result.playerMoved() && result.playerSprinted(),
                "player controller should prove Minecraft-like movement and sprint");
        require(result.chunksStreamedAfterMove(), "world streamer should keep chunks loaded after movement");
        require(result.initialFacesDrawn() > 0 && result.finalFacesDrawn() > 0,
                "renderer should draw 3D voxel faces before and after gameplay");
        require(result.initialFrameChecksum() != result.finalFrameChecksum(),
                "rendered frame should change after mine/place/move actions");
        require(result.shelterBuilt() && result.scannerUsed(),
                "shared Ashfall live mission state should require shelter setup and scanner objective");
        require(result.waterUsed() && result.foodUsed() && result.hazardCleared(),
                "shared Ashfall live mission state should process water, food, and hazard survival loops");
        require(result.scavengedSupplies(),
                "left-click mining should scavenge AdapterCore-backed survival supplies from Ashfall debris");
        require(result.adapterCoreScavengeLootTableId().startsWith("echoashfallprotocol:loot/")
                        || result.adapterCoreScavengeLootTableId().startsWith("echopowergrid:loot/"),
                "scavenging should resolve rewards through an AdapterCore loot table");
        require(result.cacheLootRecovered(),
                "crash cache should grant AdapterCore repair loot before power repair");
        require(result.terminalOnline() && result.cacheRecovered() && result.powerRepaired(),
                "shared Ashfall live mission state should process terminal, cache, and repair objectives");
        require(result.fieldPowerReady(),
                "shared Ashfall live mission state should build micro generator, route cable, and install meter before repair");
        require(result.machinePowerReady(),
                "shared Ashfall live mission state should build scrap dynamo, charge cell, buffer battery, and stabilize burner before repair");
        require(result.midgameProgressionReady(),
                "shared Ashfall live mission state should equip gas mask, decode schematic, build factory, research, and overclock route before repair");
        require(result.missionCompleted(), "shared Ashfall live mission state should complete extraction");

        System.out.println("phase15.playable voxel smoke PASS " + result.summary());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
