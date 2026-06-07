package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoStandalonePlayableLoopResult;
import dev.echo.standalone.runtime.app.EchoStandalonePlayableLoopRuntime;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EchoRuntimePlayableLoopSmokeHarness {
    private EchoRuntimePlayableLoopSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path workspaceRoot = Path.of(".").toAbsolutePath().normalize();
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoStandalonePlayableLoopResult result = new EchoStandalonePlayableLoopRuntime().run(
                services,
                workspaceRoot,
                Files.createTempDirectory("echo-standalone-playable-loop-smoke")
        );

        require(services.require(EchoStandalonePlayableLoopResult.class) == result,
                "playable loop result should be service-bound");
        require(result.ready(), "standalone playable loop should pass the full Agent 3 checklist: " + result.summary());
        require(result.newGame(), "new game should route from title into the playable runtime");
        require(result.spawn(), "new game should spawn the player into the generated world");
        require(result.move(), "player movement should update position, sprint state, and streamed chunks");
        require(result.openTerminal(), "terminal should open from gameplay and reflect live mission progress");
        require(result.completeObjective(), "mission objective chain should complete through extraction");
        require(result.interactWithHazard(), "hazard interaction should apply real survival state and clearing");
        require(result.useItem(), "item use should drive scanner, water, food, and right-click behavior");
        require(result.save(), "playable loop should write contract-backed save files");
        require(result.load(), "playable loop should restore world, player, hotbar, and mission state");
        require(result.continueGame(), "continue should reload a mid-route checkpoint into playable state");
        require(result.exitCleanly(), "standalone runtime should stop with a clean shutdown hook");

        System.out.println("phase15.playable loop smoke PASS " + result.summary());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
