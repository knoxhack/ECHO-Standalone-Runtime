package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoCompatLootItemBridge;
import dev.echo.standalone.runtime.compat.EchoCompatRecipeItemBridge;
import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeRegistry;
import dev.echo.standalone.runtime.data.EchoUnifiedRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.entity.EchoEntitySpawnDefinition;
import dev.echo.standalone.runtime.item.EchoInventoryContainer;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoItemCraftResult;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemCraftingSystem;
import dev.echo.standalone.runtime.item.EchoItemLootResult;
import dev.echo.standalone.runtime.item.EchoItemLootRuntime;
import dev.echo.standalone.runtime.item.EchoItemRecipe;
import dev.echo.standalone.runtime.item.EchoItemRegistry;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.world.EchoVoxelBiomeSources;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockBreakResult;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.block.behavior.EchoBlockBehaviorRegistry;
import dev.echo.standalone.runtime.world.block.state.EchoBlockRegistry;
import dev.echo.standalone.runtime.world.openlands.EchoFoundationContentLoadResult;
import dev.echo.standalone.runtime.world.openlands.EchoFoundationContentLoader;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBiomeDefinition;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBlockDefinition;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBlocks;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsContentLoader;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsCreatureDefinition;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsItemDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Smoke harness that verifies loaded Openlands/Foundation content is wired into gameplay systems:
 * block behavior registry for mining, crafting recipes, loot tables, and creature spawning.
 */
public final class EchoOpenlandsGameplayWiringSmokeHarness {

    private EchoOpenlandsGameplayWiringSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Expected ECHO-Modules repository root as first argument");
        }
        Path modulesRoot = Path.of(args[0]).toAbsolutePath().normalize();
        Path reportRoot = Path.of(".").toAbsolutePath().normalize().resolve("reports/echo/standalone");
        Files.createDirectories(reportRoot);

        List<Path> moduleRoots = List.of(
                modulesRoot.resolve("addons/echoopenlandsprotocol"),
                modulesRoot.resolve("addons/echomaterialcore"),
                modulesRoot.resolve("addons/echotoolcore"),
                modulesRoot.resolve("addons/echostationcore"),
                modulesRoot.resolve("addons/echoworldstarter"),
                modulesRoot.resolve("addons/echocommonloot"),
                modulesRoot.resolve("addons/echocreatureroles")
        );

        EchoOpenlandsContentLoader loader = EchoOpenlandsContentLoader.withBridge(moduleRoots);
        EchoFoundationContentLoader foundationLoader = EchoFoundationContentLoader.withBridge(moduleRoots);
        EchoFoundationContentLoadResult foundation = foundationLoader.load(moduleRoots);

        List<EchoOpenlandsBlockDefinition> blocks = loader.loadBlocksInstance(moduleRoots);
        List<EchoOpenlandsItemDefinition> items = loader.loadItemsInstance(moduleRoots);
        List<EchoOpenlandsCreatureDefinition> creatures = loader.loadCreaturesInstance(moduleRoots);
        List<EchoRecipeDefinition> recipes = loader.loadRecipesInstance(moduleRoots);
        List<EchoLootDefinition> loot = loader.loadLootInstance(moduleRoots);
        List<EchoOpenlandsBiomeDefinition> biomes = loader.loadBiomesInstance(moduleRoots);

        EchoBlockRegistry blockRegistry = new EchoBlockRegistry();
        EchoOpenlandsBlocks.registerAll(blockRegistry, blocks);
        for (EchoOpenlandsBlockDefinition block : foundation.blocks()) {
            blockRegistry.register(block.id(), block.displayName());
        }
        blockRegistry.freeze();

        EchoBlockBehaviorRegistry behaviorRegistry = new EchoBlockBehaviorRegistry();
        List<Path> behaviorRoots = new ArrayList<>(moduleRoots);
        behaviorRoots.add(Path.of(".").toAbsolutePath().normalize().resolve("echo-runtime-world"));
        for (Path root : behaviorRoots) {
            for (String candidate : List.of("src/main/resources/data", "build/resources/main/data")) {
                Path behaviors = root.resolve(candidate).resolve("echoopenlandsprotocol/block_behaviors");
                if (Files.isDirectory(behaviors)) {
                    behaviorRegistry.loadDirectory(behaviors);
                }
            }
        }
        behaviorRegistry.freeze();

        EchoItemRegistry itemRegistry = new EchoItemRegistry();
        EchoOpenlandsItems.registerAll(itemRegistry, items);
        for (EchoOpenlandsItemDefinition item : foundation.items()) {
            EchoOpenlandsItems.register(itemRegistry, item);
        }

        EchoUnifiedRegistry unified = new EchoUnifiedRegistry();
        for (EchoRecipeDefinition recipe : recipes) {
            unified.registerRecipe(recipe);
        }
        for (EchoRecipeDefinition recipe : foundation.recipes()) {
            unified.registerRecipe(recipe);
        }
        for (EchoLootDefinition lootTable : loot) {
            unified.registerLoot(lootTable);
        }
        for (EchoLootDefinition lootTable : foundation.loot()) {
            unified.registerLoot(lootTable);
        }

        EchoRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        services.register(EchoItemRegistry.class, itemRegistry);

        dev.echo.standalone.runtime.world.EchoWorldState stubWorldState = new dev.echo.standalone.runtime.world.EchoWorldState(
                "openlands:gameplay_wiring", 0L, 0L, List.of(), List.of(), List.of());
        dev.echo.standalone.runtime.world.EchoWorldQuery stubQuery = new dev.echo.standalone.runtime.world.EchoWorldQuery(stubWorldState);
        dev.echo.standalone.runtime.world.EchoWorldSaveHook stubSaveHook = new dev.echo.standalone.runtime.world.EchoWorldSaveHook(stubWorldState);
        dev.echo.standalone.runtime.world.EchoWorldRuntimeResult stubWorld = new dev.echo.standalone.runtime.world.EchoWorldRuntimeResult(
                stubWorldState, stubQuery, stubSaveHook);

        EchoEntityRuntimeResult entities = new EchoEntityRuntime().createDebugEntities(
                services,
                stubWorld,
                creatures.stream()
                        .map(c -> new EchoEntitySpawnDefinition(c.id(), c.displayName(), c.category(), c.biomes(), c.health(), c.damage(), c.notes()))
                        .toList()
        );

        EchoItemRuntimeResult itemsResult = new EchoItemRuntime().createDebugInventory(services, entities);
        EchoItemRegistry runtimeItemRegistry = itemsResult.registry();
        for (EchoItemDefinition loadedItem : itemRegistry.all()) {
            runtimeItemRegistry.register(loadedItem);
        }
        EchoItemCraftingSystem crafting = itemsResult.craftingSystem();
        EchoItemLootRuntime lootRuntime = itemsResult.lootRuntime();

        EchoCompatRecipeItemBridge recipeBridge = new EchoCompatRecipeItemBridge();
        List<EchoItemRecipe> itemRecipes = recipeBridge.toItemRecipes(unified.recipes(), runtimeItemRegistry);
        for (EchoItemRecipe itemRecipe : itemRecipes) {
            crafting.register(itemRecipe);
        }

        EchoCompatLootItemBridge lootBridge = new EchoCompatLootItemBridge();
        for (dev.echo.standalone.runtime.data.EchoLootDefinition lootDef : unified.loot().lootTables()) {
            lootBridge.toItemLootTable(lootDef, runtimeItemRegistry).ifPresent(lootRuntime::register);
        }

        int checked = 0;
        int failures = 0;
        StringBuilder details = new StringBuilder();

        checked++;
        if (behaviorRegistry.size() == 0) {
            failures++;
            details.append("no block behaviors loaded; ");
        }

        EchoVoxelBlock testBlock = new EchoVoxelBlock(
                "echoopenlandsprotocol:pine_log",
                "Pine Log",
                0x8B5A2B,
                true,
                true,
                5.0D
        );
        EchoVoxelChunk chunk = new EchoVoxelChunk(new EchoVoxelChunkId(0, 0, 0), 16);
        chunk.setBlockLocal(0, 0, 0, testBlock);
        EchoVoxelWorld world = new EchoVoxelWorld(
                "openlands:gameplay_wiring",
                1L,
                16,
                List.of(chunk),
                0.5D,
                1.0D,
                0.5D,
                0.0D,
                EchoVoxelBiomeSources.byWorldId("openlands:gameplay_wiring")
        ).withBehaviorRegistry(behaviorRegistry);

        checked++;
        EchoVoxelBlockBreakResult breakResult = world.attemptBreakBlock(0, 0, 0, 10.0D, 1.0D);
        if (!breakResult.broken()) {
            failures++;
            details.append("block did not break with behavior registry; ");
        }

        checked++;
        EchoInventoryContainer inventory = new EchoInventoryContainer(
                new EchoInventoryId("inventory:craft-test"),
                Optional.empty(),
                "Craft Test",
                9
        );
        boolean crafted = false;
        for (EchoItemRecipe itemRecipe : itemRecipes) {
            EchoItemCraftResult craftResult = crafting.craftById(inventory, itemRecipe.recipeId());
            if (craftResult.crafted() || craftResult.reason().equals("missing_ingredient")) {
                crafted = true;
                break;
            }
        }
        if (!crafted) {
            failures++;
            details.append("no recipe lookup succeeded; ");
        }

        checked++;
        boolean granted = false;
        for (String tableId : lootRuntime.lootTables().keySet()) {
            EchoItemLootResult lootResult = lootRuntime.grantById(tableId, inventory);
            if (lootResult.granted() || lootResult.entriesGranted() > 0) {
                granted = true;
                break;
            }
        }
        if (!granted) {
            failures++;
            details.append("no loot granted; ");
        }

        checked++;
        String meadowsId = biomes.stream()
                .filter(b -> b.id().endsWith(":meadows"))
                .findFirst()
                .map(EchoOpenlandsBiomeDefinition::id)
                .orElse("echoopenlandsprotocol:meadows");
        var spawned = entities.spawner().spawnForBiome(meadowsId, new dev.echo.standalone.runtime.world.EchoWorldPosition(10, 0, 10));
        if (spawned.isEmpty()) {
            failures++;
            details.append("creature did not spawn for meadows biome; ");
        }

        checked++;
        if (entities.store().count() < 3) {
            failures++;
            details.append("expected at least player+scavenger+spawned creature; ");
        }

        boolean pass = failures == 0;
        System.out.println("openlands gameplay wiring smoke " + (pass ? "PASS" : "FAIL")
                + " behaviors=" + behaviorRegistry.size()
                + " recipes=" + itemRecipes.size()
                + " lootTables=" + lootRuntime.lootTables().size()
                + " creatures=" + creatures.size()
                + " checked=" + checked
                + " failures=" + failures);
        if (!pass) {
            throw new AssertionError("Gameplay wiring smoke failed: " + details);
        }
    }
}
