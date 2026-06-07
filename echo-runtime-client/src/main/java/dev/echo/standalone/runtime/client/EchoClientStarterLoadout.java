package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoInventoryContainer;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRecipe;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

record EchoClientStarterLoadout(
        String playerInventoryId,
        String playerInventoryLabel,
        String openContainerId,
        String openContainerLabel,
        List<Item> items,
        List<Stack> openContainerStacks,
        List<Recipe> workbenchRecipes
) {
    EchoClientStarterLoadout {
        playerInventoryId = requireText(playerInventoryId, "playerInventoryId");
        playerInventoryLabel = requireText(playerInventoryLabel, "playerInventoryLabel");
        openContainerId = requireText(openContainerId, "openContainerId");
        openContainerLabel = requireText(openContainerLabel, "openContainerLabel");
        items = List.copyOf(items == null ? List.of() : items);
        openContainerStacks = List.copyOf(openContainerStacks == null ? List.of() : openContainerStacks);
        workbenchRecipes = List.copyOf(workbenchRecipes == null ? List.of() : workbenchRecipes);
    }

    static EchoClientStarterLoadout empty() {
        return new EchoClientStarterLoadout(
                "inventory:client-player",
                "Player Inventory",
                "container:client-open",
                "Open Container",
                List.of(),
                List.of(),
                List.of()
        );
    }

    EchoInventoryContainer newPlayerInventory() {
        return new EchoInventoryContainer(
                new EchoInventoryId(playerInventoryId),
                Optional.empty(),
                playerInventoryLabel,
                EchoVoxelPlayerHotbar.SLOT_COUNT
        );
    }

    EchoInventoryContainer newOpenContainer() {
        EchoInventoryContainer container = new EchoInventoryContainer(
                new EchoInventoryId(openContainerId),
                Optional.empty(),
                openContainerLabel,
                EchoVoxelPlayerHotbar.SLOT_COUNT
        );
        Map<String, EchoItemDefinition> definitions = definitionsById();
        for (Stack stack : openContainerStacks) {
            if (stack.slotIndex() < 0 || stack.slotIndex() >= container.capacity()) {
                continue;
            }
            EchoItemDefinition definition = definitions.get(stack.itemId());
            if (definition == null) {
                continue;
            }
            container.slot(stack.slotIndex()).setStack(new EchoItemStack(definition, stack.quantity()));
        }
        return container;
    }

    List<EchoItemDefinition> itemDefinitions() {
        return items.stream()
                .map(Item::definition)
                .toList();
    }

    List<EchoItemRecipe> itemRecipes() {
        return workbenchRecipes.stream()
                .map(Recipe::itemRecipe)
                .toList();
    }

    private Map<String, EchoItemDefinition> definitionsById() {
        LinkedHashMap<String, EchoItemDefinition> definitions = new LinkedHashMap<>();
        for (EchoItemDefinition definition : itemDefinitions()) {
            definitions.put(definition.id().value(), definition);
        }
        return definitions;
    }

    static Item item(
            String id,
            String displayName,
            EchoItemCategory category,
            int maxStackSize,
            String... tags
    ) {
        return new Item(
                id,
                displayName,
                category,
                maxStackSize,
                List.of(tags),
                List.of("Loaded from starter loadout")
        );
    }

    static Stack stack(int slotIndex, String itemId, int quantity) {
        return new Stack(slotIndex, itemId, quantity);
    }

    static Recipe recipe(
            String recipeId,
            Map<String, Integer> ingredients,
            String outputItemId,
            int outputQuantity
    ) {
        return new Recipe(recipeId, ingredients, outputItemId, outputQuantity);
    }

    record Item(
            String id,
            String displayName,
            EchoItemCategory category,
            int maxStackSize,
            List<String> tags,
            List<String> tooltipLines
    ) {
        Item {
            id = requireText(id, "id");
            displayName = requireText(displayName, "displayName");
            if (category == null) {
                throw new IllegalArgumentException("category must not be null");
            }
            if (maxStackSize <= 0) {
                throw new IllegalArgumentException("maxStackSize must be positive");
            }
            tags = List.copyOf(tags == null ? List.of() : tags);
            tooltipLines = List.copyOf(tooltipLines == null ? List.of() : tooltipLines);
        }

        EchoItemDefinition definition() {
            return new EchoItemDefinition(
                    new EchoItemId(id),
                    displayName,
                    category,
                    maxStackSize,
                    1.0D,
                    tags,
                    tooltipLines
            );
        }
    }

    record Stack(int slotIndex, String itemId, int quantity) {
        Stack {
            itemId = requireText(itemId, "itemId");
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
        }
    }

    record Recipe(
            String recipeId,
            Map<String, Integer> ingredients,
            String outputItemId,
            int outputQuantity
    ) {
        Recipe {
            recipeId = requireText(recipeId, "recipeId");
            outputItemId = requireText(outputItemId, "outputItemId");
            LinkedHashMap<String, Integer> copy = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : (ingredients == null ? Map.<String, Integer>of() : ingredients).entrySet()) {
                String itemId = requireText(entry.getKey(), "ingredient itemId");
                Integer quantity = entry.getValue();
                if (quantity == null || quantity <= 0) {
                    throw new IllegalArgumentException("ingredient quantity must be positive");
                }
                copy.put(itemId, quantity);
            }
            ingredients = Map.copyOf(copy);
            if (ingredients.isEmpty()) {
                throw new IllegalArgumentException("ingredients must not be empty");
            }
            if (outputQuantity <= 0) {
                throw new IllegalArgumentException("outputQuantity must be positive");
            }
        }

        EchoItemRecipe itemRecipe() {
            LinkedHashMap<EchoItemId, Integer> itemIngredients = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
                itemIngredients.put(new EchoItemId(entry.getKey()), entry.getValue());
            }
            return new EchoItemRecipe(
                    recipeId,
                    itemIngredients,
                    new EchoItemId(outputItemId),
                    outputQuantity
            );
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
