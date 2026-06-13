package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoAshfallStabilitySoakResult;
import dev.echo.standalone.runtime.app.EchoAshfallStabilitySoakRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EchoRuntimeAshfallStabilitySoakSmokeHarness {
    private EchoRuntimeAshfallStabilitySoakSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoAshfallStabilitySoakResult result = new EchoAshfallStabilitySoakRuntime().run(
                EchoAdapterCoreStandaloneContentBridge.ashfallLive(),
                Files.createTempDirectory("echo-ashfall-stability-soak")
        );

        require(result.ready(), "60-minute-equivalent Ashfall stability soak should pass: " + result.summary());
        require(result.simulatedMinutes() >= 60, "soak should cover at least 60 simulated minutes");
        require(result.memoryStable(), "soak should not show excessive memory growth");
        require(result.frameStable(), "soak should reject blank, white, or dimension-unstable frames");
        require(result.chunkStreamingStable(), "chunk streaming should remain stable under movement");
        require(result.repeatedSaveLoadStable(), "repeated save/load should remain healthy");
        require(result.inventorySpamStable(), "inventory open/close spam should not trap gameplay");
        require(result.terminalSpamStable(), "terminal open/close spam should not trap gameplay");
        require(result.pauseResumeStable(), "pause/resume spam should return to gameplay");
        require(result.focusLossRestoreStable(), "focus loss/restore should release UI focus and return to gameplay");
        require(result.missionAlive(), "survival simulation should not softlock or kill the player");

        writeReport(Path.of(".").toAbsolutePath().normalize(), result);

        System.out.println("phase15.stability soak smoke PASS " + result.summary());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void writeReport(Path workspaceRoot, EchoAshfallStabilitySoakResult result) throws IOException {
        Path reportDir = standaloneRoot(workspaceRoot).resolve("reports/echo/standalone");
        Files.createDirectories(reportDir);
        Files.writeString(reportDir.resolve("performance-stability-soak.json"), """
                {
                  "schema": "echo.standalone.performance_stability_soak.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAshfallStabilitySoakSmokeHarness",
                  "status": "PASS",
                  "ready": %s,
                  "simulatedMinutes": %d,
                  "steps": %d,
                  "framesRendered": %d,
                  "savesWritten": %d,
                  "restoresVerified": %d,
                  "memoryStable": %s,
                  "memoryGrowthBytes": %d,
                  "frameStable": %s,
                  "minUniqueColors": %d,
                  "minFacesDrawn": %d,
                  "maxWhitePixels": %d,
                  "checksumSamples": %d,
                  "chunkCountStart": %d,
                  "chunkCountEnd": %d,
                  "saveBytes": %d,
                  "maxInputLatencyNanos": %d,
                  "chunkStreamingStable": %s,
                  "repeatedSaveLoadStable": %s,
                  "inventorySpamStable": %s,
                  "terminalSpamStable": %s,
                  "pauseResumeStable": %s,
                  "focusLossRestoreStable": %s,
                  "missionAlive": %s,
                  "summary": "%s"
                }
                """.formatted(
                result.ready(),
                result.simulatedMinutes(),
                result.steps(),
                result.framesRendered(),
                result.savesWritten(),
                result.restoresVerified(),
                result.memoryStable(),
                result.memoryGrowthBytes(),
                result.frameStable(),
                result.minUniqueColors(),
                result.minFacesDrawn(),
                result.maxWhitePixels(),
                result.checksumSamples(),
                result.chunkCountStart(),
                result.chunkCountEnd(),
                result.saveBytes(),
                result.maxInputLatencyNanos(),
                result.chunkStreamingStable(),
                result.repeatedSaveLoadStable(),
                result.inventorySpamStable(),
                result.terminalSpamStable(),
                result.pauseResumeStable(),
                result.focusLossRestoreStable(),
                result.missionAlive(),
                escape(result.summary())
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
