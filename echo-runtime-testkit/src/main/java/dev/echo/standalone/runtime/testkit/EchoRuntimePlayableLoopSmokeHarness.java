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

        writeReport(workspaceRoot, result);

        System.out.println("phase15.playable loop smoke PASS " + result.summary());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void writeReport(Path workspaceRoot, EchoStandalonePlayableLoopResult result) throws IOException {
        Path reportDir = standaloneRoot(workspaceRoot).resolve("reports/echo/standalone");
        Files.createDirectories(reportDir);
        Files.writeString(reportDir.resolve("standalone-playable-loop.json"), """
                {
                  "schema": "echo.standalone.playable_loop.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimePlayableLoopSmokeHarness",
                  "status": "PASS",
                  "ready": %s,
                  "newGame": %s,
                  "spawn": %s,
                  "move": %s,
                  "openTerminal": %s,
                  "completeObjective": %s,
                  "interactWithHazard": %s,
                  "useItem": %s,
                  "save": %s,
                  "load": %s,
                  "continueGame": %s,
                  "exitCleanly": %s,
                  "shellSummary": "%s",
                  "playableSummary": "%s",
                  "saveSummary": "%s",
                  "exitSummary": "%s"
                }
                """.formatted(
                result.ready(),
                result.newGame(),
                result.spawn(),
                result.move(),
                result.openTerminal(),
                result.completeObjective(),
                result.interactWithHazard(),
                result.useItem(),
                result.save(),
                result.load(),
                result.continueGame(),
                result.exitCleanly(),
                escape(result.shellSummary()),
                escape(result.playableSummary()),
                escape(result.saveSummary()),
                escape(result.exitSummary())
        ));
    }

    private static Path standaloneRoot(Path workspaceRoot) {
        if (workspaceRoot.getFileName() != null
                && workspaceRoot.getFileName().toString().equalsIgnoreCase("echo-standalone-runtime")) {
            return workspaceRoot;
        }
        if (Files.isDirectory(workspaceRoot.resolve("echo-runtime-app"))
                && Files.isRegularFile(workspaceRoot.resolve("settings.gradle"))) {
            return workspaceRoot;
        }
        Path nested = workspaceRoot.resolve("echo-standalone-runtime");
        if (Files.isDirectory(nested)) {
            return nested;
        }
        return workspaceRoot;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
