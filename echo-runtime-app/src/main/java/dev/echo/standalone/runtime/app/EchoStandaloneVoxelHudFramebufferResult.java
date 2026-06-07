package dev.echo.standalone.runtime.app;

public record EchoStandaloneVoxelHudFramebufferResult(
        String renderTarget,
        int width,
        int height,
        boolean adapterCoreMultiRuntimeReady,
        boolean dimensionsPreserved,
        boolean renderStatsPreserved,
        boolean overlayChangedFrame,
        int topBandChangedPixels,
        int bottomBandChangedPixels,
        int heldItemChangedPixels,
        int breakFeedbackChangedPixels,
        int actionParticlesChangedPixels,
        int hotbarSlotsDrawn,
        boolean heldItemPreviewReady,
        boolean blockBreakFeedbackReady,
        boolean actionParticlesReady,
        boolean playerFeedbackReady,
        boolean survivalWarningsReady,
        boolean missionLogChapterReady,
        boolean terminalStateCoverageReady,
        long baseChecksum,
        long hudChecksum,
        long frameUploadBytes
) {
    public EchoStandaloneVoxelHudFramebufferResult {
        renderTarget = EchoAppText.requireText(renderTarget, "renderTarget");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("framebuffer dimensions must be positive");
        }
        if (topBandChangedPixels < 0
                || bottomBandChangedPixels < 0
                || heldItemChangedPixels < 0
                || breakFeedbackChangedPixels < 0
                || actionParticlesChangedPixels < 0
                || hotbarSlotsDrawn < 0
                || frameUploadBytes < 0L) {
            throw new IllegalArgumentException("hud framebuffer counts must not be negative");
        }
    }

    public boolean ready() {
        return renderTarget.contains("opengl")
                && adapterCoreMultiRuntimeReady
                && dimensionsPreserved
                && renderStatsPreserved
                && overlayChangedFrame
                && topBandChangedPixels > 200
                && bottomBandChangedPixels > 500
                && heldItemChangedPixels > 1_400
                && breakFeedbackChangedPixels > 1_000
                && actionParticlesChangedPixels > 240
                && hotbarSlotsDrawn >= 2
                && heldItemPreviewReady
                && blockBreakFeedbackReady
                && actionParticlesReady
                && playerFeedbackReady
                && survivalWarningsReady
                && missionLogChapterReady
                && terminalStateCoverageReady
                && baseChecksum != 0L
                && hudChecksum != 0L
                && baseChecksum != hudChecksum
                && frameUploadBytes == (long) width * (long) height * Integer.BYTES;
    }

    public String summary() {
        return "target=" + renderTarget
                + " size=" + width + "x" + height
                + " topPixels=" + topBandChangedPixels
                + " bottomPixels=" + bottomBandChangedPixels
                + " heldPixels=" + heldItemChangedPixels
                + " breakPixels=" + breakFeedbackChangedPixels
                + " particlePixels=" + actionParticlesChangedPixels
                + " slots=" + hotbarSlotsDrawn
                + " heldItem=" + heldItemPreviewReady
                + " breakFeedback=" + blockBreakFeedbackReady
                + " actionParticles=" + actionParticlesReady
                + " feedback=" + playerFeedbackReady
                + " warnings=" + survivalWarningsReady
                + " missionLog=" + missionLogChapterReady
                + " terminalStates=" + terminalStateCoverageReady
                + " checksum=" + Long.toUnsignedString(baseChecksum, 16)
                + "->" + Long.toUnsignedString(hudChecksum, 16);
    }
}
