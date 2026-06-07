package dev.echo.standalone.runtime.app;

public record EchoAshfallStabilitySoakResult(
        int simulatedMinutes,
        int steps,
        int framesRendered,
        int savesWritten,
        int restoresVerified,
        boolean memoryStable,
        long memoryGrowthBytes,
        boolean frameStable,
        int minUniqueColors,
        int minFacesDrawn,
        int maxWhitePixels,
        int checksumSamples,
        int chunkCountStart,
        int chunkCountEnd,
        long saveBytes,
        long maxInputLatencyNanos,
        boolean chunkStreamingStable,
        boolean repeatedSaveLoadStable,
        boolean inventorySpamStable,
        boolean terminalSpamStable,
        boolean pauseResumeStable,
        boolean focusLossRestoreStable,
        boolean missionAlive,
        String summary
) {
    public EchoAshfallStabilitySoakResult {
        summary = EchoAppText.requireText(summary, "summary");
        if (simulatedMinutes <= 0 || steps <= 0 || framesRendered < 0 || savesWritten < 0
                || restoresVerified < 0 || memoryGrowthBytes < 0L || minUniqueColors < 0
                || minFacesDrawn < 0 || maxWhitePixels < 0 || checksumSamples < 0
                || chunkCountStart < 0 || chunkCountEnd < 0 || saveBytes < 0L || maxInputLatencyNanos < 0L) {
            throw new IllegalArgumentException("soak metrics must not be negative");
        }
    }

    public boolean ready() {
        return simulatedMinutes >= 60
                && steps >= 720
                && framesRendered >= 60
                && savesWritten >= 12
                && restoresVerified >= 12
                && memoryStable
                && frameStable
                && minUniqueColors >= 40
                && minFacesDrawn >= 400
                && maxWhitePixels < 6_000
                && checksumSamples >= 30
                && chunkStreamingStable
                && repeatedSaveLoadStable
                && inventorySpamStable
                && terminalSpamStable
                && pauseResumeStable
                && focusLossRestoreStable
                && missionAlive
                && saveBytes > 0L
                && saveBytes < 2_000_000L
                && maxInputLatencyNanos < 50_000_000L;
    }
}
