package dev.echo.standalone.runtime.item;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoItemCraftingSystem {
    private final EchoItemRegistry registry;
    private final EchoInventoryOperations operations;
    private final LinkedHashMap<String, EchoItemRecipe> recipes = new LinkedHashMap<>();

    public EchoItemCraftingSystem(EchoItemRegistry registry, EchoInventoryOperations operations) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    public void register(EchoItemRecipe recipe) {
        Objects.requireNonNull(recipe, "recipe");
        recipes.put(recipe.recipeId(), recipe);
    }

    public Optional<EchoItemRecipe> find(String recipeId) {
        return Optional.ofNullable(recipes.get(recipeId));
    }

    public Map<String, EchoItemRecipe> recipes() {
        return Map.copyOf(recipes);
    }

    public EchoItemCraftResult craft(EchoInventoryContainer inventory, EchoItemRecipe recipe) {
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(recipe, "recipe");
        for (Map.Entry<EchoItemId, Integer> ingredient : recipe.ingredients().entrySet()) {
            if (operations.count(inventory, ingredient.getKey()) < ingredient.getValue()) {
                return new EchoItemCraftResult(recipe.recipeId(), false, 0, "missing_ingredient");
            }
        }

        EchoItemDefinition output = registry.require(recipe.outputItemId());
        if (operations.availableSpace(inventory, output) < recipe.outputQuantity()) {
            return new EchoItemCraftResult(recipe.recipeId(), false, 0, "inventory_full");
        }

        for (Map.Entry<EchoItemId, Integer> ingredient : recipe.ingredients().entrySet()) {
            EchoInventoryOperationResult consumed = operations.consume(inventory, ingredient.getKey(), ingredient.getValue());
            if (!consumed.success()) {
                return new EchoItemCraftResult(recipe.recipeId(), false, 0, consumed.reason());
            }
        }
        EchoInventoryOperationResult added = operations.add(inventory, new EchoItemStack(output, recipe.outputQuantity()));
        return new EchoItemCraftResult(recipe.recipeId(), added.success(), added.quantity(), added.reason());
    }

    public EchoItemCraftResult craftById(EchoInventoryContainer inventory, String recipeId) {
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(recipeId, "recipeId");
        EchoItemRecipe recipe = find(recipeId).orElse(null);
        if (recipe == null) {
            return new EchoItemCraftResult(recipeId, false, 0, "unknown_recipe");
        }
        return craft(inventory, recipe);
    }
}
