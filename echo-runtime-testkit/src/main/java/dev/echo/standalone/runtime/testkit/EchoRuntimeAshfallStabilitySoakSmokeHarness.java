package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoAshfallStabilitySoakResult;
import dev.echo.standalone.runtime.app.EchoAshfallStabilitySoakRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

import java.io.IOException;
import java.nio.file.Files;

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

        System.out.println("phase15.stability soak smoke PASS " + result.summary());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
