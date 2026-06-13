package dev.echo.standalone.runtime.client;

import java.util.Locale;

record EchoClientFramePacingSnapshot(
        int sampleCount,
        double targetFrameSeconds,
        double lastFrameSeconds,
        double averageFrameSeconds,
        double maxFrameSeconds,
        double inputDeltaSeconds,
        int fixedUpdates,
        double sleepSeconds,
        double accumulatorSeconds,
        int slowFrameCount,
        int consecutiveSlowFrames
) {
    static final EchoClientFramePacingSnapshot EMPTY = new EchoClientFramePacingSnapshot(
            0,
            1.0D / 60.0D,
            0.0D,
            0.0D,
            0.0D,
            0.0D,
            0,
            0.0D,
            0.0D,
            0,
            0
    );

    EchoClientFramePacingSnapshot {
        sampleCount = Math.max(0, sampleCount);
        targetFrameSeconds = safeSeconds(targetFrameSeconds, 1.0D / 60.0D);
        lastFrameSeconds = safeSeconds(lastFrameSeconds, 0.0D);
        averageFrameSeconds = safeSeconds(averageFrameSeconds, 0.0D);
        maxFrameSeconds = safeSeconds(maxFrameSeconds, 0.0D);
        inputDeltaSeconds = safeSeconds(inputDeltaSeconds, 0.0D);
        fixedUpdates = Math.max(0, fixedUpdates);
        sleepSeconds = safeSeconds(sleepSeconds, 0.0D);
        accumulatorSeconds = safeSeconds(accumulatorSeconds, 0.0D);
        slowFrameCount = Math.max(0, slowFrameCount);
        consecutiveSlowFrames = Math.max(0, consecutiveSlowFrames);
    }

    String overlayLine() {
        return "FRAME MS " + ms(lastFrameSeconds)
                + " AVG " + ms(averageFrameSeconds)
                + " MAX " + ms(maxFrameSeconds)
                + " UPD " + fixedUpdates
                + " SLEEP " + ms(sleepSeconds)
                + " SLOW " + slowFrameCount
                + " STREAK " + consecutiveSlowFrames;
    }

    String diagnosticsLine() {
        return "Frame: Last " + ms(lastFrameSeconds)
                + "ms Avg " + ms(averageFrameSeconds)
                + "ms Max " + ms(maxFrameSeconds)
                + "ms Target " + ms(targetFrameSeconds)
                + "ms Updates " + fixedUpdates
                + " Sleep " + ms(sleepSeconds)
                + "ms Slow " + slowFrameCount
                + " Streak " + consecutiveSlowFrames;
    }

    private static String ms(double seconds) {
        return String.format(Locale.ROOT, "%.1f", seconds * 1000.0D);
    }

    private static double safeSeconds(double value, double fallback) {
        return Double.isFinite(value) && value >= 0.0D ? value : fallback;
    }
}
