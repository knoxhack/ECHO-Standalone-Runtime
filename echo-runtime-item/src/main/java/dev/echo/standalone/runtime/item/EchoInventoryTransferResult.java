package dev.echo.standalone.runtime.item;

import java.util.Objects;

public record EchoInventoryTransferResult(
        EchoInventoryId sourceInventoryId,
        int sourceSlot,
        EchoInventoryId targetInventoryId,
        boolean success,
        int quantity,
        String reason
) {
    public EchoInventoryTransferResult {
        Objects.requireNonNull(sourceInventoryId, "sourceInventoryId");
        if (sourceSlot < 0) {
            throw new IllegalArgumentException("sourceSlot must not be negative");
        }
        Objects.requireNonNull(targetInventoryId, "targetInventoryId");
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }
        reason = EchoItemText.requireText(reason, "reason");
    }
}
