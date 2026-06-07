package dev.echo.standalone.runtime.item;

import java.util.Map;
import java.util.Objects;

public final class EchoItemCraftingSystem {
    private final EchoItemRegistry registry;
    private final EchoInventoryOperations operations;

    public EchoItemCraftingSystem(EchoItemRegistry registry, EchoInventoryOperations operations) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.operations = Objects.requireNonNull(operations, "operations");
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
}
