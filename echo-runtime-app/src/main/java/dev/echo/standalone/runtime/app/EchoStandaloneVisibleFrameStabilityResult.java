package dev.echo.standalone.runtime.app;

public record EchoStandaloneVisibleFrameStabilityResult(
        String target,
        int frameCount,
        int width,
        int height,
        boolean adapterCoreMultiRuntimeReady,
        boolean dimensionsStable,
        boolean noWhiteFrames,
        boolean noBlankFrames,
        boolean stableChecksums,
        boolean visible3dBlocks,
        int minUniqueColors,
        int minFacesDrawn,
        int minBlockPixels,
        int maxWhitePixels,
        long stableChecksum
) {
    public EchoStandaloneVisibleFrameStabilityResult {
        target = EchoAppText.requireText(target, "target");
        if (frameCount <= 0
                || width <= 0
                || height <= 0
                || minUniqueColors < 0
                || minFacesDrawn < 0
                || minBlockPixels < 0
                || maxWhitePixels < 0) {
            throw new IllegalArgumentException("visible frame stability counts must be positive");
        }
    }

    public boolean ready() {
        return adapterCoreMultiRuntimeReady
                && dimensionsStable
                && noWhiteFrames
                && noBlankFrames
                && stableChecksums
                && visible3dBlocks
                && minUniqueColors >= 40
                && minFacesDrawn >= 400
                && minBlockPixels >= 20_000
                && maxWhitePixels < (width * height) / 100
                && stableChecksum != 0L;
    }

    public String summary() {
        return "target=" + target
                + " frames=" + frameCount
                + " size=" + width + "x" + height
                + " stable=" + stableChecksums
                + " noWhite=" + noWhiteFrames
                + " noBlank=" + noBlankFrames
                + " blockPixels=" + minBlockPixels
                + " colors=" + minUniqueColors
                + " faces=" + minFacesDrawn
                + " checksum=" + Long.toUnsignedString(stableChecksum, 16);
    }
}
