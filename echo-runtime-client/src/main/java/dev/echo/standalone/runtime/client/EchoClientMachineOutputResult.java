package dev.echo.standalone.runtime.client;

record EchoClientMachineOutputResult(
        boolean success,
        String machineId,
        String itemId,
        int extractedQuantity,
        int inventoryItemCount,
        int machineOutputCount,
        String reason
) {
    EchoClientMachineOutputResult {
        machineId = machineId == null ? "" : machineId.trim();
        itemId = itemId == null ? "" : itemId.trim();
        if (extractedQuantity < 0) {
            extractedQuantity = 0;
        }
        if (inventoryItemCount < 0) {
            inventoryItemCount = 0;
        }
        if (machineOutputCount < 0) {
            machineOutputCount = 0;
        }
        reason = reason == null || reason.isBlank() ? "unknown" : reason.trim();
    }
}
