package dev.echo.standalone.runtime.app;

public record EchoStandalonePlayableVoxelResult(
        String worldId,
        int adapterCoreBindingCount,
        boolean adapterCoreMultiRuntimeReady,
        int initialChunkCount,
        int streamedChunkCount,
        boolean playerSpawned,
        boolean fieldManualRead,
        boolean raycastPickedBlock,
        String pickedBlockId,
        boolean blockBroken,
        double blockBreakRequiredSeconds,
        double blockBreakAppliedSeconds,
        boolean hotbarPickup,
        boolean blockPlaced,
        String placedBlockId,
        boolean mouseLookApplied,
        boolean allHotbarSlotsSelectable,
        boolean rightClickUseOrPlace,
        boolean inventoryToggleReleasesMouse,
        boolean escapePauseReleasesMouse,
        double baselineWalkingSurvivalMinutes,
        double survivalSecondsSimulated,
        boolean canonicalBetaRouteReady,
        double firstShelterMinutes,
        double firstWaterUseMinutes,
        double firstHazardWarningMinutes,
        double terminalOnlineMinutes,
        double powerRestoredMinutes,
        double extractionCompleteMinutes,
        boolean failureStatesReady,
        boolean recoveryPathsReady,
        boolean hudObjectiveStateReady,
        int canonicalRouteStepsCompleted,
        int canonicalRouteStepsTotal,
        boolean playerMoved,
        boolean playerSprinted,
        boolean chunksStreamedAfterMove,
        long initialFrameChecksum,
        long finalFrameChecksum,
        int initialFacesDrawn,
        int finalFacesDrawn,
        boolean shelterBuilt,
        boolean scannerUsed,
        boolean waterUsed,
        boolean foodUsed,
        boolean hazardCleared,
        boolean scavengedSupplies,
        String adapterCoreScavengeLootTableId,
        boolean cacheLootRecovered,
        boolean terminalOnline,
        boolean cacheRecovered,
        boolean fieldPowerReady,
        boolean machinePowerReady,
        boolean midgameProgressionReady,
        boolean expeditionSafetyReady,
        boolean advancedExpeditionReady,
        boolean fieldRecoveryReady,
        boolean powerRepaired,
        boolean missionCompleted,
        int completedObjectives,
        int totalObjectives
) {
    public EchoStandalonePlayableVoxelResult {
        worldId = EchoAppText.requireText(worldId, "worldId");
        pickedBlockId = pickedBlockId == null ? "none" : pickedBlockId.trim();
        placedBlockId = placedBlockId == null ? "none" : placedBlockId.trim();
        adapterCoreScavengeLootTableId = adapterCoreScavengeLootTableId == null
                ? ""
                : adapterCoreScavengeLootTableId.trim();
        if (adapterCoreBindingCount < 0 || initialChunkCount < 0 || streamedChunkCount < 0
                || initialFacesDrawn < 0 || finalFacesDrawn < 0
                || completedObjectives < 0 || totalObjectives < 0
                || blockBreakRequiredSeconds < 0.0D
                || blockBreakAppliedSeconds < 0.0D
                || baselineWalkingSurvivalMinutes < 0.0D
                || survivalSecondsSimulated < 0.0D
                || firstShelterMinutes < 0.0D
                || firstWaterUseMinutes < 0.0D
                || firstHazardWarningMinutes < 0.0D
                || terminalOnlineMinutes < 0.0D
                || powerRestoredMinutes < 0.0D
                || extractionCompleteMinutes < 0.0D
                || canonicalRouteStepsCompleted < 0
                || canonicalRouteStepsTotal < 0) {
            throw new IllegalArgumentException("playable voxel result counts must not be negative");
        }
    }

    public boolean betaPlayableCoreReady() {
        return adapterCoreBindingCount >= 10
                && adapterCoreMultiRuntimeReady
                && streamedChunkCount >= initialChunkCount
                && playerSpawned
                && fieldManualRead
                && raycastPickedBlock
                && blockBroken
                && blockBreakRequiredSeconds > 0.0D
                && blockBreakAppliedSeconds >= blockBreakRequiredSeconds
                && hotbarPickup
                && blockPlaced
                && mouseLookApplied
                && allHotbarSlotsSelectable
                && rightClickUseOrPlace
                && inventoryToggleReleasesMouse
                && escapePauseReleasesMouse
                && baselineWalkingSurvivalMinutes >= 30.0D
                && baselineWalkingSurvivalMinutes <= 60.0D
                && canonicalBetaRouteReady
                && failureStatesReady
                && recoveryPathsReady
                && hudObjectiveStateReady
                && canonicalRouteStepsCompleted == canonicalRouteStepsTotal
                && playerMoved
                && playerSprinted
                && chunksStreamedAfterMove
                && initialFrameChecksum != 0L
                && finalFrameChecksum != 0L
                && initialFrameChecksum != finalFrameChecksum
                && initialFacesDrawn > 0
                && finalFacesDrawn > 0
                && shelterBuilt
                && scannerUsed
                && waterUsed
                && foodUsed
                && hazardCleared
                && scavengedSupplies
                && !adapterCoreScavengeLootTableId.isBlank()
                && cacheLootRecovered
                && terminalOnline
                && cacheRecovered
                && fieldPowerReady
                && machinePowerReady
                && midgameProgressionReady
                && expeditionSafetyReady
                && advancedExpeditionReady
                && fieldRecoveryReady
                && powerRepaired
                && missionCompleted
                && completedObjectives == totalObjectives;
    }

    public String summary() {
        return "adapterCoreBindings=" + adapterCoreBindingCount
                + " chunks=" + initialChunkCount + "->" + streamedChunkCount
                + " spawn=" + playerSpawned
                + " manual=" + fieldManualRead
                + " picked=" + pickedBlockId
                + " break=" + blockBroken
                + "@" + String.format("%.2f", blockBreakAppliedSeconds)
                + "/" + String.format("%.2f", blockBreakRequiredSeconds)
                + " place=" + blockPlaced + ":" + placedBlockId
                + " controls=look:" + mouseLookApplied
                + ",hotbar:" + allHotbarSlotsSelectable
                + ",use:" + rightClickUseOrPlace
                + ",inventory:" + inventoryToggleReleasesMouse
                + ",pause:" + escapePauseReleasesMouse
                + " pacing=" + String.format("%.1f", baselineWalkingSurvivalMinutes)
                + "min"
                + " route=" + canonicalRouteStepsCompleted + "/" + canonicalRouteStepsTotal
                + "@shelter:" + String.format("%.1f", firstShelterMinutes)
                + ",water:" + String.format("%.1f", firstWaterUseMinutes)
                + ",hazard:" + String.format("%.1f", firstHazardWarningMinutes)
                + ",terminal:" + String.format("%.1f", terminalOnlineMinutes)
                + ",power:" + String.format("%.1f", powerRestoredMinutes)
                + ",extract:" + String.format("%.1f", extractionCompleteMinutes)
                + " failures=" + failureStatesReady
                + " recovery=" + recoveryPathsReady
                + " hud=" + hudObjectiveStateReady
                + " moved=" + playerMoved
                + " sprint=" + playerSprinted
                + " frameChanged=" + (initialFrameChecksum != finalFrameChecksum)
                + " survival=shelter:" + shelterBuilt + ",scan:" + scannerUsed
                + ",water:" + waterUsed + ",food:" + foodUsed + ",hazard:" + hazardCleared
                + ",scavenge:" + scavengedSupplies
                + "@" + adapterCoreScavengeLootTableId
                + ",loot:" + cacheLootRecovered
                + ",fieldPower:" + fieldPowerReady
                + ",machinePower:" + machinePowerReady
                + ",midgame:" + midgameProgressionReady
                + ",expeditionSafety:" + expeditionSafetyReady
                + ",advancedExpedition:" + advancedExpeditionReady
                + ",fieldRecovery:" + fieldRecoveryReady
                + " mission=" + completedObjectives + "/" + totalObjectives;
    }
}
