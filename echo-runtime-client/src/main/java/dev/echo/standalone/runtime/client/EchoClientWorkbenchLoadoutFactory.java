package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoCompatLootItemBridge;
import dev.echo.standalone.runtime.compat.EchoCompatRecipeItemBridge;
import dev.echo.standalone.runtime.data.EchoDataTag;
import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemDefinitionInference;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRecipe;
import dev.echo.standalone.runtime.item.EchoItemRegistry;
import dev.echo.standalone.runtime.item.EchoLootTable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class EchoClientWorkbenchLoadoutFactory {
    private EchoClientWorkbenchLoadoutFactory() {
    }

    static EchoClientWorkbenchLoadout fromDataRecipes(List<EchoRecipeDefinition> dataRecipes) {
        return fromStarterLoadout(EchoClientStarterLoadout.empty(), dataRecipes);
    }

    static EchoClientWorkbenchLoadout fromDataRecipes(
            List<EchoRecipeDefinition> dataRecipes,
            List<EchoDataTag> dataTags
    ) {
        return fromStarterLoadout(EchoClientStarterLoadout.empty(), dataRecipes, List.of(), dataTags);
    }

    static EchoClientWorkbenchLoadout fromStarterLoadout(
            EchoClientStarterLoadout starterLoadout,
            List<EchoRecipeDefinition> dataRecipes
    ) {
        return fromStarterLoadout(starterLoadout, dataRecipes, List.of());
    }

    static EchoClientWorkbenchLoadout fromStarterLoadout(
            EchoClientStarterLoadout starterLoadout,
            List<EchoRecipeDefinition> dataRecipes,
            List<EchoItemDefinition> runtimeItemDefinitions
    ) {
        return fromStarterLoadout(starterLoadout, dataRecipes, runtimeItemDefinitions, List.of());
    }

    static EchoClientWorkbenchLoadout fromStarterLoadout(
            EchoClientStarterLoadout starterLoadout,
            List<EchoRecipeDefinition> dataRecipes,
            List<EchoItemDefinition> runtimeItemDefinitions,
            List<EchoDataTag> dataTags
    ) {
        return fromStarterLoadout(starterLoadout, dataRecipes, runtimeItemDefinitions, dataTags, List.of());
    }

    static EchoClientWorkbenchLoadout fromStarterLoadout(
            EchoClientStarterLoadout starterLoadout,
            List<EchoRecipeDefinition> dataRecipes,
            List<EchoItemDefinition> runtimeItemDefinitions,
            List<EchoDataTag> dataTags,
            List<EchoLootDefinition> lootDefinitions
    ) {
        EchoClientStarterLoadout safeStarterLoadout =
                starterLoadout == null ? EchoClientStarterLoadout.empty() : starterLoadout;
        LinkedHashMap<EchoItemId, EchoItemDefinition> definitions = new LinkedHashMap<>();
        for (EchoItemDefinition definition : safeStarterLoadout.itemDefinitions()) {
            registerDefinition(definitions, definition);
        }
        List<EchoItemDefinition> runtimeDefinitions =
                runtimeItemDefinitions == null ? List.of() : List.copyOf(runtimeItemDefinitions);
        for (EchoItemDefinition definition : runtimeDefinitions) {
            registerDefinition(definitions, definition);
        }

        List<EchoRecipeDefinition> recipes = dataRecipes == null ? List.of() : List.copyOf(dataRecipes);
        for (EchoRecipeDefinition recipe : recipes) {
            registerRecipeItems(definitions, recipe);
        }
        List<EchoLootDefinition> loot = lootDefinitions == null ? List.of() : List.copyOf(lootDefinitions);
        for (EchoLootDefinition lootDefinition : loot) {
            registerLootItems(definitions, lootDefinition);
        }
        applyDataTags(definitions, dataTags);

        EchoItemRegistry registry = new EchoItemRegistry();
        for (EchoItemDefinition definition : definitions.values()) {
            registry.register(definition);
        }

        LinkedHashMap<String, EchoItemRecipe> bridged = new LinkedHashMap<>();
        EchoCompatRecipeItemBridge bridge = new EchoCompatRecipeItemBridge();
        for (EchoRecipeDefinition recipe : recipes.stream()
                .sorted(Comparator.comparing(EchoRecipeDefinition::id))
                .toList()) {
            bridge.toItemRecipe(recipe, registry).ifPresent(itemRecipe -> bridged.put(itemRecipe.recipeId(), itemRecipe));
        }
        for (EchoItemRecipe recipe : safeStarterLoadout.itemRecipes()) {
            bridged.putIfAbsent(recipe.recipeId(), recipe);
        }

        LinkedHashMap<String, EchoLootTable> bridgedLoot = new LinkedHashMap<>();
        EchoCompatLootItemBridge lootBridge = new EchoCompatLootItemBridge();
        for (EchoLootDefinition lootDefinition : loot.stream()
                .sorted(Comparator.comparing(EchoLootDefinition::id))
                .toList()) {
            lootBridge.toItemLootTable(lootDefinition, registry)
                    .ifPresent(table -> bridgedLoot.put(table.tableId(), table));
        }
        return new EchoClientWorkbenchLoadout(
                registry,
                new ArrayList<>(bridged.values()),
                new ArrayList<>(bridgedLoot.values())
        );
    }

    private static void registerRecipeItems(
            LinkedHashMap<EchoItemId, EchoItemDefinition> definitions,
            EchoRecipeDefinition recipe
    ) {
        if (!recipe.result().startsWith("#")) {
            registerDefinition(definitions, inferredDefinition(recipe.result(), recipe.resultCount()));
        }
        for (Map.Entry<String, Integer> entry : recipe.ingredientCounts().entrySet()) {
            if (!entry.getKey().startsWith("#")) {
                registerDefinition(definitions, inferredDefinition(entry.getKey(), entry.getValue()));
            }
        }
    }

    private static void registerLootItems(
            LinkedHashMap<EchoItemId, EchoItemDefinition> definitions,
            EchoLootDefinition loot
    ) {
        if (loot == null) {
            return;
        }
        for (String entry : loot.entries()) {
            if (entry == null || entry.isBlank() || entry.startsWith("#")) {
                continue;
            }
            registerDefinition(definitions, inferredDefinition(entry, 1));
        }
    }

    private static void registerDefinition(
            LinkedHashMap<EchoItemId, EchoItemDefinition> definitions,
            EchoItemDefinition definition
    ) {
        if (!definitions.containsKey(definition.id())) {
            definitions.put(definition.id(), definition);
        }
    }

    private static void applyDataTags(
            LinkedHashMap<EchoItemId, EchoItemDefinition> definitions,
            List<EchoDataTag> dataTags
    ) {
        if (dataTags == null || dataTags.isEmpty()) {
            return;
        }
        for (EchoDataTag dataTag : dataTags) {
            if (dataTag == null || !itemRegistryTag(dataTag)) {
                continue;
            }
            List<String> tagNames = tagAliases(dataTag.id());
            for (String value : dataTag.values()) {
                if (value == null || value.isBlank() || value.startsWith("#")) {
                    continue;
                }
                EchoItemId itemId = new EchoItemId(value);
                EchoItemDefinition definition = definitions.get(itemId);
                if (definition == null) {
                    definition = inferredDefinition(value, 1);
                }
                definitions.put(itemId, withAdditionalTags(definition, tagNames));
            }
        }
    }

    private static boolean itemRegistryTag(EchoDataTag dataTag) {
        String registry = dataTag.registryId().trim().toLowerCase(Locale.ROOT);
        return registry.equals("item")
                || registry.equals("items")
                || registry.endsWith(":item")
                || registry.endsWith(":items");
    }

    private static List<String> tagAliases(String tagId) {
        String normalized = tagId == null ? "" : tagId.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isBlank()) {
            return List.of();
        }
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.add(normalized);
        int namespaceSeparator = normalized.indexOf(':');
        if (namespaceSeparator >= 0 && namespaceSeparator < normalized.length() - 1) {
            tags.add(normalized.substring(namespaceSeparator + 1));
        }
        return List.copyOf(tags);
    }

    private static EchoItemDefinition withAdditionalTags(
            EchoItemDefinition definition,
            List<String> additionalTags
    ) {
        if (additionalTags.isEmpty()) {
            return definition;
        }
        LinkedHashSet<String> tags = new LinkedHashSet<>(definition.tags());
        tags.addAll(additionalTags);
        return new EchoItemDefinition(
                definition.id(),
                definition.displayName(),
                definition.category(),
                definition.maxStackSize(),
                definition.weight(),
                new ArrayList<>(tags),
                definition.tooltipLines()
        );
    }

    private static EchoItemDefinition inferredDefinition(String itemId, int requiredStack) {
        return EchoItemDefinitionInference.inferDefinition(
                itemId,
                requiredStack,
                "data-recipe",
                "Loaded from standalone ScreenCore container"
        );
    }
}
