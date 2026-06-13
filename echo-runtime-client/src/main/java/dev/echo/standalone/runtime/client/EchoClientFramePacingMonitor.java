package dev.echo.standalone.runtime.client;

final class EchoClientFramePacingMonitor {
    static final double TARGET_FRAME_SECONDS = 1.0D / 60.0D;
    private static final int SAMPLE_WINDOW = 120;
    private static final double SLOW_FRAME_MULTIPLIER = 1.25D;

    private final double[] frameSamples = new double[SAMPLE_WINDOW];
    private int sampleIndex;
    private int sampleCount;
    private double sampleTotalSeconds;
    private double lastFrameSeconds;
    private double lastInputDeltaSeconds;
    private double lastSleepSeconds;
    private double lastAccumulatorSeconds;
    private int lastFixedUpdates;
    private int slowFrameCount;
    private int consecutiveSlowFrames;

    EchoClientFramePacingSnapshot snapshot() {
        return new EchoClientFramePacingSnapshot(
                sampleCount,
                TARGET_FRAME_SECONDS,
                lastFrameSeconds,
                sampleCount == 0 ? 0.0D : sampleTotalSeconds / sampleCount,
                maxFrameSeconds(),
                lastInputDeltaSeconds,
                lastFixedUpdates,
                lastSleepSeconds,
                lastAccumulatorSeconds,
                slowFrameCount,
                consecutiveSlowFrames
        );
    }

    void record(
            double inputDeltaSeconds,
            double frameSeconds,
            int fixedUpdates,
            double sleepSeconds,
            double accumulatorSeconds
    ) {
        double safeFrameSeconds = safeSeconds(frameSeconds);
        if (sampleCount == SAMPLE_WINDOW) {
            sampleTotalSeconds -= frameSamples[sampleIndex];
        } else {
            sampleCount++;
        }
        frameSamples[sampleIndex] = safeFrameSeconds;
        sampleTotalSeconds += safeFrameSeconds;
        sampleIndex = (sampleIndex + 1) % SAMPLE_WINDOW;

        lastInputDeltaSeconds = safeSeconds(inputDeltaSeconds);
        lastFrameSeconds = safeFrameSeconds;
        lastFixedUpdates = Math.max(0, fixedUpdates);
        lastSleepSeconds = safeSeconds(sleepSeconds);
        lastAccumulatorSeconds = safeSeconds(accumulatorSeconds);
        if (safeFrameSeconds > TARGET_FRAME_SECONDS * SLOW_FRAME_MULTIPLIER) {
            slowFrameCount++;
            consecutiveSlowFrames++;
        } else {
            consecutiveSlowFrames = 0;
        }
    }

    private double maxFrameSeconds() {
        double max = 0.0D;
        for (int i = 0; i < sampleCount; i++) {
            max = Math.max(max, frameSamples[i]);
        }
        return max;
    }

    private static double safeSeconds(double value) {
        return Double.isFinite(value) && value > 0.0D ? value : 0.0D;
    }
}
