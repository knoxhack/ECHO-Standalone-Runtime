package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoStandaloneGameShellResult;
import dev.echo.standalone.runtime.app.EchoStandaloneGameShellRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;

import java.io.IOException;
import java.nio.file.Files;

public final class EchoRuntimeGameShellSmokeHarness {
    private EchoRuntimeGameShellSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoStandaloneGameShellResult result = new EchoStandaloneGameShellRuntime().run(
                services,
                Files.createTempDirectory("echo-game-shell-smoke"),
                EchoAdapterCoreStandaloneContentBridge.ashfallLive()
        );

        require(services.require(EchoStandaloneGameShellResult.class) == result,
                "game shell result should be service-bound");
        require(result.ready(), "game shell should be beta-ready");
        require(result.titleVisible(), "title menu should be visible before gameplay");
        require(result.newGameStartsPlayableRuntime(), "new game should route into the playable voxel runtime");
        require(result.continueStartsPlayableRuntime(), "continue should route into the playable voxel runtime");
        require(result.pauseBlocksGameplay(), "pause menu should block gameplay ticks/input");
        require(result.optionsVisible(), "options panel should be reachable from pause");
        require(result.inventoryVisible(), "inventory panel should be reachable from gameplay");
        require(result.inventoryBlocksGameplay(), "inventory panel should block gameplay ticks/input");
        require(result.terminalVisible(), "terminal panel should be reachable from gameplay");
        require(result.terminalBlocksGameplay(), "terminal panel should block gameplay ticks/input");
        require(result.missionLogVisible(), "mission log should be reachable from gameplay");
        require(result.missionLogBlocksGameplay(), "mission log should block gameplay ticks/input");
        require(result.resumeReturnsToGameplay(), "resume should return to gameplay mode");
        require(result.saveProfileBound(), "shell should bind save profile new/continue/restore state");
        require(result.adapterCoreMultiRuntimeReady(),
                "shell should keep AdapterCore multi-runtime support active");

        System.out.println("phase15.game shell smoke PASS " + result.summary());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
