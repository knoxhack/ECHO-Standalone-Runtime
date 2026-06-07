package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class EchoStandaloneGameShellRuntime {
    public EchoStandaloneGameShellResult run(
            EchoRuntimeServiceRegistry services,
            Path saveRoot,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) throws IOException {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(saveRoot, "saveRoot");
        Objects.requireNonNull(bridge, "bridge");

        EchoSaveProfileFlowResult saveFlow = new EchoSaveProfileFlowRuntime().run(services, saveRoot);
        EchoStandaloneGameShellState title = EchoStandaloneGameShellState.title(saveFlow.continueFlow());
        EchoStandalonePlayableVoxelResult playable = new EchoStandalonePlayableVoxelRuntime().run(bridge);
        EchoStandaloneGameShellState newGame = title.startNewGame();
        EchoStandaloneGameShellState inventory = newGame.openInventory();
        EchoStandaloneGameShellState terminal = newGame.openTerminal();
        EchoStandaloneGameShellState missionLog = newGame.openMissionLog();
        EchoStandaloneGameShellState paused = newGame.pause();
        EchoStandaloneGameShellState options = paused.openOptions();
        EchoStandaloneGameShellState resumed = options.closeOptions().resume();
        EchoStandaloneGameShellState continued = title.continueGame();

        EchoStandaloneGameShellResult result = new EchoStandaloneGameShellResult(
                title.overlayVisible() && title.mode() == EchoStandaloneGameShellMode.TITLE,
                newGame.gameplayActive() && playable.betaPlayableCoreReady(),
                continued.gameplayActive() && saveFlow.continueFlow().continueAvailable() && playable.betaPlayableCoreReady(),
                paused.overlayVisible() && !paused.gameplayActive(),
                options.overlayVisible() && options.mode() == EchoStandaloneGameShellMode.OPTIONS,
                inventory.overlayVisible() && inventory.mode() == EchoStandaloneGameShellMode.INVENTORY,
                !inventory.gameplayActive() && inventory.closeInventory().gameplayActive(),
                terminal.overlayVisible() && terminal.mode() == EchoStandaloneGameShellMode.TERMINAL,
                !terminal.gameplayActive() && terminal.closeTerminal().gameplayActive(),
                missionLog.overlayVisible() && missionLog.mode() == EchoStandaloneGameShellMode.MISSION_LOG,
                !missionLog.gameplayActive() && missionLog.closeMissionLog().gameplayActive(),
                resumed.gameplayActive(),
                saveFlow.summary().newGameReady()
                        && saveFlow.summary().continueReady()
                        && saveFlow.summary().restoreReady(),
                bridge.supportsAllAdapterCoreRuntimes(),
                title.summary(),
                resumed.summary(),
                playable.summary()
        );
        services.register(EchoStandaloneGameShellResult.class, result);
        return result;
    }
}
