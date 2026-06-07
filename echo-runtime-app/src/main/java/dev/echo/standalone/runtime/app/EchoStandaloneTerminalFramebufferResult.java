package dev.echo.standalone.runtime.app;

public record EchoStandaloneTerminalFramebufferResult(
        String target,
        int width,
        int height,
        boolean adapterCoreMultiRuntimeReady,
        boolean framebufferShapePreserved,
        boolean terminalOverlayChangedFrame,
        int centralChangedPixels,
        boolean terminalOnline,
        boolean terminalStateCoverageReady,
        boolean diagnosticsReady,
        boolean extractionAuthorizedReady,
        long baseChecksum,
        long terminalChecksum
) {
    public EchoStandaloneTerminalFramebufferResult {
        target = EchoAppText.requireText(target, "target");
        if (width <= 0 || height <= 0 || centralChangedPixels < 0) {
            throw new IllegalArgumentException("terminal framebuffer counts must be positive");
        }
    }

    public boolean ready() {
        return adapterCoreMultiRuntimeReady
                && framebufferShapePreserved
                && terminalOverlayChangedFrame
                && centralChangedPixels > 14_000
                && terminalOnline
                && terminalStateCoverageReady
                && diagnosticsReady
                && extractionAuthorizedReady
                && baseChecksum != 0L
                && terminalChecksum != 0L
                && baseChecksum != terminalChecksum;
    }

    public String summary() {
        return "target=" + target
                + " size=" + width + "x" + height
                + " centralPixels=" + centralChangedPixels
                + " terminalOnline=" + terminalOnline
                + " states=" + terminalStateCoverageReady
                + " diagnostics=" + diagnosticsReady
                + " extractionAuthorized=" + extractionAuthorizedReady
                + " checksum=" + Long.toUnsignedString(baseChecksum, 16)
                + "->" + Long.toUnsignedString(terminalChecksum, 16);
    }
}
