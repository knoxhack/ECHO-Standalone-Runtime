package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.data.EchoRecipeDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeRegistry;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRecipe;
import dev.echo.standalone.runtime.item.EchoItemRegistry;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoCompatRecipeItemBridge {
    public Optional<EchoItemRecipe> toItemRecipe(EchoRecipeDefinition recipe, EchoItemRegistry itemRegistry) {
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(itemRegistry, "itemRegistry");
        if (recipe.result().startsWith("#")) {
            return Optional.empty();
        }
        EchoItemId output = new EchoItemId(recipe.result());
        if (itemRegistry.find(output).isEmpty()) {
            return Optional.empty();
        }

        LinkedHashMap<EchoItemId, Integer> ingredients = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : recipe.ingredientCounts().entrySet()) {
            String ingredientId = entry.getKey();
            Optional<EchoItemId> resolved = ingredientId.startsWith("#")
                    ? resolveTaggedIngredient(ingredientId, itemRegistry)
                    : Optional.of(new EchoItemId(ingredientId));
            if (resolved.isEmpty()) {
                return Optional.empty();
            }
            EchoItemId id = resolved.orElseThrow();
            if (itemRegistry.find(id).isEmpty()) {
                return Optional.empty();
            }
            ingredients.merge(id, entry.getValue(), Integer::sum);
        }
        if (ingredients.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new EchoItemRecipe(recipe.id(), ingredients, output, recipe.resultCount()));
    }

    public List<EchoItemRecipe> toItemRecipes(EchoRecipeRegistry recipes, EchoItemRegistry itemRegistry) {
        Objects.requireNonNull(recipes, "recipes");
        Objects.requireNonNull(itemRegistry, "itemRegistry");
        return recipes.recipes().stream()
                .map(recipe -> toItemRecipe(recipe, itemRegistry))
                .flatMap(Optional::stream)
                .toList();
    }

    private static Optional<EchoItemId> resolveTaggedIngredient(String ingredientId, EchoItemRegistry itemRegistry) {
        String tag = ingredientId.substring(1).trim();
        if (tag.isBlank()) {
            return Optional.empty();
        }
        return tagged(itemRegistry, tag).stream()
                .findFirst()
                .or(() -> {
                    int namespaceSeparator = tag.indexOf(':');
                    if (namespaceSeparator < 0 || namespaceSeparator == tag.length() - 1) {
                        return Optional.empty();
                    }
                    return tagged(itemRegistry, tag.substring(namespaceSeparator + 1)).stream().findFirst();
                })
                .map(EchoItemDefinition::id);
    }

    private static List<EchoItemDefinition> tagged(EchoItemRegistry itemRegistry, String tag) {
        return itemRegistry.tagged(tag).stream()
                .sorted(Comparator.comparing(definition -> definition.id().value()))
                .toList();
    }
}
