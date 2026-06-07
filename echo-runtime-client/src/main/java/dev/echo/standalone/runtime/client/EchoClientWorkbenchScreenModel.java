package dev.echo.standalone.runtime.client;

import java.util.List;

record EchoClientWorkbenchScreenModel(
        String screenId,
        String title,
        List<EchoClientWorkbenchRecipeSummary> recipes,
        EchoClientWorkbenchRecipeDetail selectedRecipe
) {
    EchoClientWorkbenchScreenModel {
        screenId = screenId == null || screenId.isBlank() ? "echoscreencore:workbench" : screenId.trim();
        title = title == null || title.isBlank() ? "Workbench" : title.trim();
        recipes = recipes == null ? List.of() : List.copyOf(recipes);
        selectedRecipe = selectedRecipe == null ? EchoClientWorkbenchRecipeDetail.empty() : selectedRecipe;
    }
}
