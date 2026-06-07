package dev.echo.standalone.runtime.app;

public record EchoStandaloneVoxelMaterialAtlasResult(
        String target,
        boolean adapterCoreMultiRuntimeReady,
        int atlasKeyCount,
        int materialPatternCount,
        int patternedFaceCount,
        int uniqueFramebufferColors,
        long framebufferChecksum,
        String atlasSummary
) {
    public EchoStandaloneVoxelMaterialAtlasResult {
        target = EchoAppText.requireText(target, "target");
        atlasSummary = EchoAppText.requireText(atlasSummary, "atlasSummary");
        if (atlasKeyCount < 0
                || materialPatternCount < 0
                || patternedFaceCount < 0
                || uniqueFramebufferColors < 0) {
            throw new IllegalArgumentException("material atlas counts must not be negative");
        }
    }

    public boolean ready() {
        return adapterCoreMultiRuntimeReady
                && atlasKeyCount >= 7
                && materialPatternCount >= 7
                && patternedFaceCount > 400
                && uniqueFramebufferColors > 48
                && framebufferChecksum != 0L;
    }

    public String summary() {
        return "target=" + target
                + " atlasKeys=" + atlasKeyCount
                + " patterns=" + materialPatternCount
                + " patternedFaces=" + patternedFaceCount
                + " colors=" + uniqueFramebufferColors
                + " checksum=" + Long.toUnsignedString(framebufferChecksum, 16)
                + " summary=" + atlasSummary;
    }
}
