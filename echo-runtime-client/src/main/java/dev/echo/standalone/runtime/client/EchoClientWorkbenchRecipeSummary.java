package dev.echo.standalone.runtime.client;

record EchoClientWorkbenchRecipeSummary(
        String recipeId,
        String label,
        String tooltip,
        boolean craftable
) {
    EchoClientWorkbenchRecipeSummary {
        recipeId = requireText(recipeId, "recipeId");
        label = requireText(label, "label");
        tooltip = tooltip == null ? "" : tooltip.trim();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
