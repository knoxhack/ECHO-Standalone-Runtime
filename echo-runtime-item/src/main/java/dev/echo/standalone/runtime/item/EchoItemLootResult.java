package dev.echo.standalone.runtime.item;

public record EchoItemLootResult(String tableId, boolean granted, int entriesGranted, int quantityGranted, String reason) {
    public EchoItemLootResult {
        tableId = EchoItemText.requireText(tableId, "tableId");
        if (entriesGranted < 0) {
            throw new IllegalArgumentException("entriesGranted must not be negative");
        }
        if (quantityGranted < 0) {
            throw new IllegalArgumentException("quantityGranted must not be negative");
        }
        reason = EchoItemText.requireText(reason, "reason");
    }
}
