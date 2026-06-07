package dev.echo.standalone.runtime.data;

import java.util.List;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record EchoRecipeDefinition(
        String id,
        String type,
        List<String> ingredients,
        Map<String, Integer> ingredientCounts,
        String result,
        int resultCount,
        List<String> pattern,
        String group,
        String category,
        String sourceLogicalId
) {
    public EchoRecipeDefinition(String id, String type, List<String> ingredients, String result, String sourceLogicalId) {
        this(
                id,
                type,
                ingredients,
                countIngredients(ingredients),
                result,
                1,
                List.of(),
                "",
                "",
                sourceLogicalId
        );
    }

    public EchoRecipeDefinition {
        id = EchoDataPaths.requireText(id, "id");
        type = EchoDataPaths.requireText(type, "type");
        Objects.requireNonNull(ingredients, "ingredients");
        Objects.requireNonNull(ingredientCounts, "ingredientCounts");
        result = EchoDataPaths.requireText(result, "result");
        if (resultCount <= 0) {
            throw new IllegalArgumentException("resultCount must be positive");
        }
        Objects.requireNonNull(pattern, "pattern");
        group = group == null ? "" : group;
        category = category == null ? "" : category;
        sourceLogicalId = EchoDataPaths.requireText(sourceLogicalId, "sourceLogicalId");
        ingredients = ingredients.stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted()
                .toList();
        ingredientCounts = sortedCounts(ingredientCounts);
        pattern = pattern.stream()
                .map(String::valueOf)
                .toList();
    }

    private static Map<String, Integer> countIngredients(List<String> ingredients) {
        Objects.requireNonNull(ingredients, "ingredients");
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        for (String ingredient : ingredients) {
            if (ingredient != null && !ingredient.isBlank()) {
                counts.merge(ingredient, 1, Integer::sum);
            }
        }
        return counts;
    }

    private static Map<String, Integer> sortedCounts(Map<String, Integer> counts) {
        LinkedHashMap<String, Integer> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : new TreeMap<>(counts).entrySet()) {
            String ingredient = EchoDataPaths.requireText(entry.getKey(), "ingredient");
            Integer count = entry.getValue();
            if (count == null || count <= 0) {
                throw new IllegalArgumentException("ingredient count must be positive");
            }
            copy.put(ingredient, count);
        }
        return Collections.unmodifiableMap(copy);
    }
}
