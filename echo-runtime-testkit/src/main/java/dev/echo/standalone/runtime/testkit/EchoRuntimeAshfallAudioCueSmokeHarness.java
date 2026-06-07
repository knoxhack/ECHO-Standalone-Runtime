package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoAshfallAudioCueCoverageResult;
import dev.echo.standalone.runtime.app.EchoAshfallAudioCueCoverageRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

public final class EchoRuntimeAshfallAudioCueSmokeHarness {
    private EchoRuntimeAshfallAudioCueSmokeHarness() {
    }

    public static void main(String[] args) {
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

        System.out.println("phase15.audio cue smoke PASS " + result.summary());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
