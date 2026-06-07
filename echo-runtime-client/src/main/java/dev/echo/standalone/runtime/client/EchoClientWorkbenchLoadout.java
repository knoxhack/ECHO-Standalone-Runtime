package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemRecipe;
import dev.echo.standalone.runtime.item.EchoItemRegistry;
import dev.echo.standalone.runtime.item.EchoLootTable;

import java.util.List;

record EchoClientWorkbenchLoadout(
        EchoItemRegistry registry,
        List<EchoItemRecipe> recipes,
        List<EchoLootTable> lootTables
) {
    EchoClientWorkbenchLoadout {
        if (registry == null) {
            throw new IllegalArgumentException("registry must not be null");
        }
        recipes = recipes == null ? List.of() : List.copyOf(recipes);
        lootTables = lootTables == null ? List.of() : List.copyOf(lootTables);
    }
}
