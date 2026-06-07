package dev.echo.standalone.runtime.app;

public record EchoStandaloneInventoryFramebufferResult(
        String target,
        int width,
        int height,
        boolean adapterCoreMultiRuntimeReady,
        boolean framebufferShapePreserved,
        boolean inventoryOverlayChangedFrame,
        boolean indexOverlayVisible,
        String indexOverlaySurfaceId,
        String indexOverlayFocusedControl,
        int indexOverlayActionCount,
        int indexOverlayRecipeRows,
        int centralChangedPixels,
        int filledHotbarSlots,
        long baseChecksum,
        long inventoryChecksum
) {
    public EchoStandaloneInventoryFramebufferResult {
        target = EchoAppText.requireText(target, "target");
        indexOverlaySurfaceId = EchoAppText.requireText(indexOverlaySurfaceId, "indexOverlaySurfaceId");
        indexOverlayFocusedControl = EchoAppText.requireText(indexOverlayFocusedControl, "indexOverlayFocusedControl");
        if (width <= 0 || height <= 0 || centralChangedPixels < 0 || filledHotbarSlots < 0
                || indexOverlayActionCount < 0 || indexOverlayRecipeRows < 0) {
            throw new IllegalArgumentException("inventory framebuffer counts must be positive");
        }
    }

    public boolean ready() {
        return adapterCoreMultiRuntimeReady
                && framebufferShapePreserved
                && inventoryOverlayChangedFrame
                && indexOverlayVisible
                && "echoindex:inventory_overlay".equals(indexOverlaySurfaceId)
                && "index:overlay_search".equals(indexOverlayFocusedControl)
                && indexOverlayActionCount >= 5
                && indexOverlayRecipeRows >= 1
                && centralChangedPixels > 12_000
                && filledHotbarSlots >= 2
                && baseChecksum != 0L
                && inventoryChecksum != 0L
                && baseChecksum != inventoryChecksum;
    }

    public String summary() {
        return "target=" + target
                + " size=" + width + "x" + height
                + " indexOverlay=" + indexOverlaySurfaceId
                + " actions=" + indexOverlayActionCount
                + " rows=" + indexOverlayRecipeRows
                + " centralPixels=" + centralChangedPixels
                + " slots=" + filledHotbarSlots
                + " checksum=" + Long.toUnsignedString(baseChecksum, 16)
                + "->" + Long.toUnsignedString(inventoryChecksum, 16);
    }
}
