package dev.echo.standalone.runtime.client;

record EchoClientMachineInputResult(
        boolean success,
        String machineId,
        String itemId,
        int insertedQuantity,
        int remainingItemCount,
        int machineInputCount,
        String reason
) {
    EchoClientMachineInputResult {
        machineId = machineId == null ? "" : machineId.trim();
        itemId = itemId == null ? "" : itemId.trim();
        if (insertedQuantity < 0) {
            insertedQuantity = 0;
        }
        if (remainingItemCount < 0) {
            remainingItemCount = 0;
        }
        if (machineInputCount < 0) {
            machineInputCount = 0;
        }
        reason = reason == null || reason.isBlank() ? "unknown" : reason.trim();
    }
}
