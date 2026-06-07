package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.contracts.EchoRuntimeLifecycle;
import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class EchoStandalonePlayableLoopRuntime {
    public EchoStandalonePlayableLoopResult run(
            EchoRuntimeServiceRegistry services,
            Path workspaceRoot,
            Path saveRoot
    ) throws IOException {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(workspaceRoot, "workspaceRoot");
        Objects.requireNonNull(saveRoot, "saveRoot");

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        EchoStandaloneGameShellResult shell = new EchoStandaloneGameShellRuntime().run(
                services,
                saveRoot.resolve("shell"),
                bridge
        );
        EchoStandalonePlayableVoxelResult playable = new EchoStandalonePlayableVoxelRuntime().run(bridge);
        EchoStandalonePlayableVoxelSaveResult save = new EchoStandalonePlayableVoxelSaveRuntime().run(
                bridge,
                saveRoot.resolve("playable-save")
        );
        EchoRuntimeBootResult exit = new EchoRuntimeLauncher().launch(EchoRuntimeBootContext.headless(workspaceRoot));

        boolean newGame = shell.titleVisible() && shell.newGameStartsPlayableRuntime();
        boolean spawn = playable.playerSpawned()
                && playable.streamedChunkCount() >= playable.initialChunkCount()
                && playable.initialFacesDrawn() > 0;
        boolean move = playable.playerMoved()
                && playable.playerSprinted()
                && playable.chunksStreamedAfterMove();
        boolean openTerminal = shell.terminalVisible()
                && shell.terminalBlocksGameplay()
                && playable.terminalOnline();
        boolean completeObjective = playable.canonicalBetaRouteReady()
                && playable.canonicalRouteStepsCompleted() == playable.canonicalRouteStepsTotal()
                && playable.missionCompleted()
                && playable.completedObjectives() == playable.totalObjectives();
        boolean interactWithHazard = playable.hazardCleared()
                && playable.failureStatesReady()
                && playable.survivalSecondsSimulated() > 0.0D;
        boolean useItem = playable.rightClickUseOrPlace()
                && playable.scannerUsed()
                && playable.waterUsed()
                && playable.foodUsed();
        boolean saveReady = save.ready()
                && save.filesWritten() >= 5
                && save.contractBacked()
                && save.contractVersioned();
        boolean loadReady = save.restoredWorldEdits()
                && save.restoredPlayer()
                && save.restoredHotbar()
                && save.restoredMission()
                && save.restoredContractState();
        boolean continueReady = shell.continueStartsPlayableRuntime()
                && save.midRouteSaveLoadReady()
                && shell.saveProfileBound();
        boolean exitCleanly = exit.success()
                && exit.finalLifecycle() == EchoRuntimeLifecycle.STOPPED
                && exit.ticksRun() == 3
                && exit.shutdownHook().reason().equals("headless_tick_loop_complete");

        EchoStandalonePlayableLoopResult result = new EchoStandalonePlayableLoopResult(
                newGame,
                spawn,
                move,
                openTerminal,
                completeObjective,
                interactWithHazard,
                useItem,
                saveReady,
                loadReady,
                continueReady,
                exitCleanly,
                shell.summary(),
                playable.summary(),
                save.summary(),
                "exitCode=" + exit.exitCode().name()
                        + " lifecycle=" + exit.finalLifecycle().name()
                        + " ticks=" + exit.ticksRun()
                        + " hook=" + exit.shutdownHook().reason()
        );
        services.register(EchoStandalonePlayableLoopResult.class, result);
        return result;
    }
}
