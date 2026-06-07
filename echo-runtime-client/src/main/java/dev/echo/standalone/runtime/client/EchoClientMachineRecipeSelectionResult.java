package dev.echo.standalone.runtime.client;

record EchoClientMachineRecipeSelectionResult(
        boolean success,
        String machineId,
        String recipeId,
        String selectedRecipeId,
        boolean changed,
        String reason
) {
    EchoClientMachineRecipeSelectionResult {
        machineId = machineId == null ? "" : machineId.trim();
        recipeId = recipeId == null ? "" : recipeId.trim();
        selectedRecipeId = selectedRecipeId == null ? "" : selectedRecipeId.trim();
        reason = reason == null || reason.isBlank() ? "unknown" : reason.trim();
    }
}
