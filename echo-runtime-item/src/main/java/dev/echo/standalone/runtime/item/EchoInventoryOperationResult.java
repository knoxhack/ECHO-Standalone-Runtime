package dev.echo.standalone.runtime.item;

public record EchoInventoryOperationResult(
        String action,
        boolean success,
        int quantity,
        String reason
) {
    public EchoInventoryOperationResult {
        action = EchoItemText.requireText(action, "action");
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }
        reason = EchoItemText.requireText(reason, "reason");
    }
}
