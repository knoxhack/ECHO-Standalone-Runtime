package dev.echo.standalone.runtime.item;

public record EchoItemCraftResult(String recipeId, boolean crafted, int outputQuantity, String reason) {
    public EchoItemCraftResult {
        recipeId = EchoItemText.requireText(recipeId, "recipeId");
        if (outputQuantity < 0) {
            throw new IllegalArgumentException("outputQuantity must not be negative");
        }
        reason = EchoItemText.requireText(reason, "reason");
    }
}
