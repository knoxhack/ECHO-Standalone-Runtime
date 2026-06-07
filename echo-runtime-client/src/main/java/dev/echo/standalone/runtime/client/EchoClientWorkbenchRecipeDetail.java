package dev.echo.standalone.runtime.client;

import java.util.List;

record EchoClientWorkbenchRecipeDetail(
        String recipeId,
        String label,
        String tooltip,
        boolean craftable,
        List<EchoClientSlotStack> ingredients,
        EchoClientSlotStack output,
        String status
) {
    EchoClientWorkbenchRecipeDetail {
        recipeId = recipeId == null ? "" : recipeId.trim();
        label = label == null || label.isBlank() ? "No Recipe" : label.trim();
        tooltip = tooltip == null ? "" : tooltip.trim();
        ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
        output = output == null ? EchoClientSlotStack.empty(99) : output;
        status = status == null || status.isBlank() ? "Select a recipe" : status.trim();
    }

    static EchoClientWorkbenchRecipeDetail empty() {
        return new EchoClientWorkbenchRecipeDetail(
                "",
                "No Recipe",
                "",
                false,
                List.of(),
                EchoClientSlotStack.empty(99),
                "Select a recipe"
        );
    }
}
