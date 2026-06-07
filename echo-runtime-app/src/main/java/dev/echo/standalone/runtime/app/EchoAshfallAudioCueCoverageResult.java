package dev.echo.standalone.runtime.app;

public record EchoAshfallAudioCueCoverageResult(
        boolean adapterCoreSoundBacked,
        boolean windAmbienceReady,
        boolean miningHitReady,
        boolean blockBreakReady,
        boolean itemPickupReady,
        boolean waterFoodUseReady,
        boolean terminalBeepReady,
        boolean powerRepairReady,
        boolean extractionBeaconReady,
        boolean dangerAlertReady,
        int diagnosticsCount,
        int eventCount,
        String summary
) {
    public EchoAshfallAudioCueCoverageResult {
        summary = EchoAppText.requireText(summary, "summary");
        if (diagnosticsCount < 0) {
            throw new IllegalArgumentException("diagnosticsCount must not be negative");
        }
        if (eventCount < 0) {
            throw new IllegalArgumentException("eventCount must not be negative");
        }
    }

    public boolean ready() {
        return adapterCoreSoundBacked
                && windAmbienceReady
                && miningHitReady
                && blockBreakReady
                && itemPickupReady
                && waterFoodUseReady
                && terminalBeepReady
                && powerRepairReady
                && extractionBeaconReady
                && dangerAlertReady
                && diagnosticsCount >= 10
                && eventCount >= 13;
    }
}
