package dev.echo.standalone.runtime.player;

public record EchoPlayerInventoryShortcutResult(
        int slotIndex,
        String itemId,
        boolean used,
        String reason
) {
    public EchoPlayerInventoryShortcutResult {
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex must not be negative");
        }
        itemId = itemId == null ? "" : itemId;
        reason = EchoPlayerText.requireText(reason, "reason");
    }
}
