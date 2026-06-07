package dev.echo.standalone.runtime.world;

public record EchoVoxelBlockBreakResult(
        int x,
        int y,
        int z,
        EchoVoxelBlock block,
        double accumulatedSeconds,
        double requiredSeconds,
        double progress,
        boolean broken,
        String reason
) {
    public EchoVoxelBlockBreakResult {
        block = block == null ? EchoVoxelBlock.AIR : block;
        if (!Double.isFinite(accumulatedSeconds)
                || !Double.isFinite(requiredSeconds)
                || !Double.isFinite(progress)
                || accumulatedSeconds < 0.0D
                || requiredSeconds < 0.0D
                || progress < 0.0D
                || progress > 1.0D) {
            throw new IllegalArgumentException("break progress values must be finite and normalized");
        }
        if (reason == null || reason.isBlank()) {
            reason = broken ? "broken" : "in_progress";
        } else {
            reason = reason.trim();
        }
    }

    public String summary() {
        return block.id()
                + " progress=" + String.format("%.2f", progress)
                + " time=" + String.format("%.2f", accumulatedSeconds)
                + "/" + String.format("%.2f", requiredSeconds)
                + " broken=" + broken;
    }
}
