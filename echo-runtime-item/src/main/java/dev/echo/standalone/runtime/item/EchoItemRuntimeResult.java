package dev.echo.standalone.runtime.item;

import java.util.Objects;

public record EchoItemRuntimeResult(
        EchoItemRegistry registry,
        EchoInventoryStore inventoryStore,
        EchoInventoryOperations operations,
        EchoItemCraftingSystem craftingSystem,
        EchoItemLootRuntime lootRuntime,
        EchoItemTooltipRenderer tooltipRenderer,
        EchoItemRecipe debugRecipe,
        EchoLootTable debugLootTable,
        EchoItemSaveHook saveHook
) {
    public EchoItemRuntimeResult {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(inventoryStore, "inventoryStore");
        Objects.requireNonNull(operations, "operations");
        Objects.requireNonNull(craftingSystem, "craftingSystem");
        Objects.requireNonNull(lootRuntime, "lootRuntime");
        Objects.requireNonNull(tooltipRenderer, "tooltipRenderer");
        Objects.requireNonNull(debugRecipe, "debugRecipe");
        Objects.requireNonNull(debugLootTable, "debugLootTable");
        Objects.requireNonNull(saveHook, "saveHook");
    }
}
