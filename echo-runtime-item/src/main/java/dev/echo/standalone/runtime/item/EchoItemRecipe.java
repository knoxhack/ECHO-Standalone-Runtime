package dev.echo.standalone.runtime.item;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record EchoItemRecipe(
        String recipeId,
        Map<EchoItemId, Integer> ingredients,
        EchoItemId outputItemId,
        int outputQuantity
) {
    public EchoItemRecipe {
        recipeId = EchoItemText.requireText(recipeId, "recipeId");
        Objects.requireNonNull(ingredients, "ingredients");
        LinkedHashMap<EchoItemId, Integer> copy = new LinkedHashMap<>();
        for (Map.Entry<EchoItemId, Integer> entry : ingredients.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "ingredient item id");
            if (entry.getValue() == null || entry.getValue() <= 0) {
                throw new IllegalArgumentException("ingredient quantity must be positive");
            }
            copy.put(entry.getKey(), entry.getValue());
        }
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("ingredients must not be empty");
        }
        ingredients = Collections.unmodifiableMap(copy);
        Objects.requireNonNull(outputItemId, "outputItemId");
        if (outputQuantity <= 0) {
            throw new IllegalArgumentException("outputQuantity must be positive");
        }
    }
}
