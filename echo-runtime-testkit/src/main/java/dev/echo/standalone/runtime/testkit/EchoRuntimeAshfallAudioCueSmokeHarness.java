package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoAshfallAudioCueCoverageResult;
import dev.echo.standalone.runtime.app.EchoAshfallAudioCueCoverageRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EchoRuntimeAshfallAudioCueSmokeHarness {
    private EchoRuntimeAshfallAudioCueSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoAshfallAudioCueCoverageResult result = new EchoAshfallAudioCueCoverageRuntime().run(
                EchoAdapterCoreStandaloneContentBridge.ashfallLive()
        );

        require(result.ready(), "Ashfall audio cue smoke should pass: " + result.summary());
        require(result.adapterCoreSoundBacked(), "terminal cue should resolve through AdapterCore sound binding");
        require(result.windAmbienceReady(), "wind and ash ambience should be part of the initial audio plan");
        require(result.miningHitReady(), "mining hit cue should be planned from block interaction state");
        require(result.blockBreakReady(), "block break cue should be planned from block interaction state");
        require(result.itemPickupReady(), "item pickup cue should be planned from AdapterCore scavenge rewards");
        require(result.waterFoodUseReady(), "water and food consumption cues should be planned from mission consumable state");
        require(result.terminalBeepReady(), "terminal beep should use the AdapterCore terminal sound event");
        require(result.powerRepairReady(), "power repair cue should be planned from mission power state");
        require(result.extractionBeaconReady(), "extraction beacon cue should loop while extraction is armed");
        require(result.dangerAlertReady(), "danger alert cue should be planned from hazard pressure state");

        writeReport(result);

        System.out.println("phase15.audio cue smoke PASS " + result.summary());
    }

    private static void writeReport(EchoAshfallAudioCueCoverageResult result) throws IOException {
        Path report = Path.of("reports", "echo", "standalone", "ashfall-audio-cue-coverage.json");
        Files.createDirectories(report.getParent());
        Files.writeString(report, """
                {
                  "schema": "echo.standalone.ashfall_audio_cue_coverage.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAshfallAudioCueSmokeHarness",
                  "summary": "%s",
                  "adapterCoreSoundBacked": %s,
                  "windAmbienceReady": %s,
                  "miningHitReady": %s,
                  "blockBreakReady": %s,
                  "itemPickupReady": %s,
                  "waterFoodUseReady": %s,
                  "terminalBeepReady": %s,
                  "powerRepairReady": %s,
                  "extractionBeaconReady": %s,
                  "dangerAlertReady": %s,
                  "diagnosticsCount": %d,
                  "eventCount": %d
                }
                """.formatted(
                escape(result.summary()),
                result.adapterCoreSoundBacked(),
                result.windAmbienceReady(),
                result.miningHitReady(),
                result.blockBreakReady(),
                result.itemPickupReady(),
                result.waterFoodUseReady(),
                result.terminalBeepReady(),
                result.powerRepairReady(),
                result.extractionBeaconReady(),
                result.dangerAlertReady(),
                result.diagnosticsCount(),
                result.eventCount()
        ));
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
