package dev.echo.standalone.runtime.item;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoItemRuntime {
    public static final String SCRAP_METAL_ITEM_ID = "echoashfallprotocol:scrap_metal";
    public static final String CLEAN_WATER_BOTTLE_ITEM_ID = "echoashfallprotocol:clean_water_bottle";

    public EchoItemRuntimeResult createDebugInventory(
            EchoRuntimeServiceRegistry services,
            EchoEntityRuntimeResult entities
    ) {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(entities, "entities");

        EchoItemRegistry registry = new EchoItemRegistry();
        EchoItemDefinition salvagedMetal = definition(
                SCRAP_METAL_ITEM_ID,
                "Scrap Metal",
                EchoItemCategory.MATERIAL,
                16,
                0.30D,
                List.of("crafting", "salvage"),
                List.of("Scrap from the crash site.")
        );
        EchoItemDefinition waterRation = definition(
                CLEAN_WATER_BOTTLE_ITEM_ID,
                "Clean Water Bottle",
                EchoItemCategory.CONSUMABLE,
                4,
                0.50D,
                List.of("consumable", "hydration"),
                List.of("Sealed emergency water.")
        );
        EchoItemDefinition filterCanister = definition(
                "ashfall:filter_canister",
                "Filter Canister",
                EchoItemCategory.TOOL,
                2,
                0.80D,
                List.of("breathing", "crafting"),
                List.of("A worn canister for ash filtration.")
        );
        EchoItemDefinition patchedFilter = definition(
                "ashfall:patched_filter",
                "Patched Filter",
                EchoItemCategory.TOOL,
                2,
                0.95D,
                List.of("breathing", "crafted"),
                List.of("A field-patched filter with a little life left.")
        );
        EchoItemDefinition scavengerBlade = definition(
                "ashfall:scavenger_blade",
                "Scavenger Blade",
                EchoItemCategory.EQUIPMENT,
                1,
                2.10D,
                List.of("melee", "weapon"),
                List.of("Improvised edge, ugly but useful.")
        );
        for (EchoItemDefinition definition : List.of(
                salvagedMetal,
                waterRation,
                filterCanister,
                patchedFilter,
                scavengerBlade
        )) {
            registry.register(definition);
        }

        EchoEntityId playerId = entities.store().all().stream()
                .filter(entity -> entity.definition().kind() == EchoEntityKind.PLAYER)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Debug inventory requires a player entity"))
                .id();
        EchoInventoryStore inventoryStore = new EchoInventoryStore();
        EchoInventoryContainer playerPack = new EchoInventoryContainer(
                new EchoInventoryId("inventory:player-001"),
                Optional.of(playerId),
                "Debug Survivor Pack",
                8
        );
        EchoInventoryContainer crashCache = new EchoInventoryContainer(
                new EchoInventoryId("container:crash-cache"),
                Optional.empty(),
                "Crash Cache",
                4
        );
        inventoryStore.register(playerPack);
        inventoryStore.register(crashCache);

        playerPack.slot(0).setStack(new EchoItemStack(salvagedMetal, 3));
        playerPack.slot(1).setStack(new EchoItemStack(waterRation, 2));
        playerPack.slot(2).setStack(new EchoItemStack(filterCanister, 1));
        crashCache.slot(0).setStack(new EchoItemStack(salvagedMetal, 4));
        crashCache.slot(1).setStack(new EchoItemStack(scavengerBlade, 1));

        EchoInventoryOperations operations = new EchoInventoryOperations();
        EchoItemCraftingSystem craftingSystem = new EchoItemCraftingSystem(registry, operations);
        EchoItemLootRuntime lootRuntime = new EchoItemLootRuntime(registry, operations);
        EchoItemTooltipRenderer tooltipRenderer = new EchoItemTooltipRenderer();
        LinkedHashMap<EchoItemId, Integer> patchFilterIngredients = new LinkedHashMap<>();
        patchFilterIngredients.put(salvagedMetal.id(), 2);
        patchFilterIngredients.put(filterCanister.id(), 1);
        EchoItemRecipe debugRecipe = new EchoItemRecipe(
                "ashfall:patch_filter",
                patchFilterIngredients,
                patchedFilter.id(),
                1
        );
        craftingSystem.register(debugRecipe);
        EchoLootTable debugLootTable = new EchoLootTable(
                "ashfall:crash_cache_salvage",
                List.of(
                        new EchoLootEntry(waterRation.id(), 1),
                        new EchoLootEntry(salvagedMetal.id(), 2)
                )
        );
        lootRuntime.register(debugLootTable);
        EchoItemSaveHook saveHook = new EchoItemSaveHook(registry, inventoryStore);
        EchoItemRuntimeResult result = new EchoItemRuntimeResult(
                registry,
                inventoryStore,
                operations,
                craftingSystem,
                lootRuntime,
                tooltipRenderer,
                debugRecipe,
                debugLootTable,
                saveHook
        );
        services.register(EchoItemRuntimeResult.class, result);
        services.register(EchoItemRegistry.class, registry);
        services.register(EchoInventoryStore.class, inventoryStore);
        services.register(EchoInventoryOperations.class, operations);
        services.register(EchoItemCraftingSystem.class, craftingSystem);
        services.register(EchoItemLootRuntime.class, lootRuntime);
        services.register(EchoItemTooltipRenderer.class, tooltipRenderer);
        services.register(EchoItemSaveHook.class, saveHook);
        return result;
    }

    private static EchoItemDefinition definition(
            String id,
            String displayName,
            EchoItemCategory category,
            int maxStackSize,
            double weight,
            List<String> tags,
            List<String> tooltipLines
    ) {
        return new EchoItemDefinition(
                new EchoItemId(id),
                displayName,
                category,
                maxStackSize,
                weight,
                tags,
                tooltipLines
        );
    }
}
