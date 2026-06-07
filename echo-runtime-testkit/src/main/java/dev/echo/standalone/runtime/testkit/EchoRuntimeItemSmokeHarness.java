package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreNativeContentRegistrations;
import dev.echo.standalone.runtime.compat.EchoCompatLootItemBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.item.EchoInventoryContainer;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoInventoryOperationResult;
import dev.echo.standalone.runtime.item.EchoInventoryStore;
import dev.echo.standalone.runtime.item.EchoInventoryTransferResult;
import dev.echo.standalone.runtime.item.EchoItemCraftResult;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemLootResult;
import dev.echo.standalone.runtime.item.EchoItemRegistry;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemSaveResult;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.item.EchoLootTable;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeItemSmokeHarness {
    private EchoRuntimeItemSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite()
        );
        EchoEntityRuntimeResult entities = new EchoEntityRuntime().createDebugEntities(services, world);
        EchoItemRuntimeResult items = new EchoItemRuntime().createDebugInventory(services, entities);
        EchoItemRegistry registry = items.registry();
        EchoInventoryStore store = items.inventoryStore();
        EchoInventoryContainer playerPack = store.require(new EchoInventoryId("inventory:player-001"));
        EchoInventoryContainer crashCache = store.require(new EchoInventoryId("container:crash-cache"));

        EchoItemId metalId = new EchoItemId(EchoItemRuntime.SCRAP_METAL_ITEM_ID);
        EchoItemId waterId = new EchoItemId(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID);
        EchoItemId canisterId = new EchoItemId("ashfall:filter_canister");
        EchoItemId patchedFilterId = new EchoItemId("ashfall:patched_filter");
        EchoItemId bladeId = new EchoItemId("ashfall:scavenger_blade");
        EchoItemDefinition water = registry.require(waterId);

        require(services.require(EchoItemRuntimeResult.class) == items,
                "item runtime result should be service-bound");
        require(services.require(EchoItemRegistry.class) == registry,
                "item registry should be service-bound");
        require(services.require(EchoInventoryStore.class) == store,
                "inventory store should be service-bound");
        require(registry.count() == 5, "debug item registry should contain five definitions");
        require(registry.tagged("crafting").size() == 2, "crafting tag should resolve deterministic items");
        require(store.count() == 2, "debug inventory store should contain player pack and crash cache");
        require(playerPack.occupiedSlots() == 3, "player pack should start with three occupied slots");
        require(items.operations().count(playerPack, metalId) == 3, "player pack should start with metal");
        require(items.operations().count(playerPack, waterId) == 2, "player pack should start with water");
        require(items.operations().count(playerPack, canisterId) == 1, "player pack should start with canister");

        EchoInventoryOperationResult addedWater = items.operations().add(playerPack, new EchoItemStack(water, 1));
        require(addedWater.success(), "water should merge into player pack");
        require(items.operations().count(playerPack, waterId) == 3, "merged water count should increase");

        EchoInventoryTransferResult transferredBlade = items.operations().transfer(crashCache, 1, playerPack, 1);
        require(transferredBlade.success(), "blade should transfer from cache to player pack");
        require(items.operations().count(playerPack, bladeId) == 1, "player pack should contain transferred blade");
        require(items.operations().count(crashCache, bladeId) == 0, "crash cache should no longer contain blade");

        EchoInventoryTransferResult movedWater = items.operations().moveOrMergeSlot(playerPack, 1, 5);
        require(movedWater.success() && movedWater.reason().equals("moved_stack"),
                "same-container inventory move should move a stack into an empty slot");
        require(playerPack.slot(1).empty(), "moved source slot should be empty");
        require(playerPack.slot(5).stack().orElseThrow().itemId().equals(waterId),
                "moved target slot should contain water");
        EchoInventoryTransferResult splitWater = items.operations().splitSlotTo(playerPack, 5, 6);
        require(splitWater.success() && splitWater.reason().equals("split_stack"),
                "same-container inventory split should split a stack into an empty slot");
        require(playerPack.slot(5).stack().orElseThrow().quantity() == 2,
                "split source slot should retain the rounded-up remainder");
        require(playerPack.slot(6).stack().orElseThrow().quantity() == 1,
                "split target slot should receive half the stack");
        EchoInventoryTransferResult mergedWater = items.operations().moveOrMergeSlot(playerPack, 6, 5);
        require(mergedWater.success() && mergedWater.reason().equals("merged_stack"),
                "same-container inventory move should merge matching stacks");
        require(playerPack.slot(6).empty(), "merged source slot should be empty");
        EchoInventoryTransferResult swappedBlade = items.operations().swapSlots(playerPack, 3, 4);
        require(swappedBlade.success() && swappedBlade.reason().equals("swapped_slots"),
                "same-container inventory swap should exchange occupied and empty slots");
        require(playerPack.slot(3).empty(), "swapped source slot should become empty");
        require(playerPack.slot(4).stack().orElseThrow().itemId().equals(bladeId),
                "swapped target slot should contain blade");
        EchoInventoryTransferResult crossContainerSwap = items.operations().swapSlots(playerPack, 4, crashCache, 2);
        require(crossContainerSwap.success() && crossContainerSwap.reason().equals("swapped_slots"),
                "cross-container inventory swap should exchange player and container slots");
        require(playerPack.slot(4).empty(), "cross-container swap should clear the player source slot");
        require(crashCache.slot(2).stack().orElseThrow().itemId().equals(bladeId),
                "cross-container swap should move blade into the crash cache target slot");
        require(items.operations().swapSlots(crashCache, 2, playerPack, 4).success(),
                "cross-container inventory swap should move the stack back for later crafting checks");
        EchoInventoryTransferResult quickMovedWater = items.operations().quickMoveSlot(playerPack, 5, 1, 4);
        require(quickMovedWater.success() && quickMovedWater.reason().equals("quick_moved_stack"),
                "quick move should move a stack into the requested target range");
        require(playerPack.slot(5).empty(), "quick-moved source slot should become empty");
        require(playerPack.slot(1).stack().orElseThrow().itemId().equals(waterId),
                "quick move should use the first empty slot in the target range");

        EchoInventoryOperationResult consumedWater = items.operations().consume(playerPack, waterId, 1);
        require(consumedWater.success(), "water should be consumable");
        require(items.operations().count(playerPack, waterId) == 2, "consumed water should reduce count");

        EchoItemCraftResult crafted = items.craftingSystem().craft(playerPack, items.debugRecipe());
        require(crafted.crafted(), "patched filter recipe should craft");
        require(items.operations().count(playerPack, patchedFilterId) == 1, "player pack should contain crafted filter");
        require(items.operations().count(playerPack, metalId) == 1, "crafting should consume metal");
        require(items.operations().count(playerPack, canisterId) == 0, "crafting should consume canister");

        EchoItemLootResult loot = items.lootRuntime().grant(items.debugLootTable(), crashCache);
        require(loot.granted(), "crash cache salvage loot should grant");
        require(loot.quantityGranted() == 3, "loot table should grant three total items");
        require(items.operations().count(crashCache, waterId) == 1, "crash cache should receive water loot");
        require(items.operations().count(crashCache, metalId) == 6, "crash cache should receive metal loot");

        String runtimeLootItemId = "echoruntimehost:runtime_salvage_token";
        Map<String, Object> runtimeItemRow = Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:item/runtime_salvage_token",
                "contentKind", "ITEM",
                "domain", "items",
                "displayName", "Runtime Salvage Token",
                "adapterKey", "registry.items.runtime_salvage_token",
                "neoForgeId", runtimeLootItemId,
                "nativeLoaderId", "echoruntimehost:item/runtime_salvage_token",
                "standaloneRuntimeId", runtimeLootItemId,
                "metadata", Map.of(
                        "category", "MATERIAL",
                        "maxStackSize", 16,
                        "weight", "0.1",
                        "tags", List.of("adaptercore", "native-content", "loot_token"),
                        "tooltipLines", List.of("Granted by native loot row")
                )
        );
        Map<String, Object> runtimeLootRow = Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:loot/runtime_cache",
                "contentKind", "LOOT_TABLE",
                "domain", "loot",
                "displayName", "Runtime Cache Loot",
                "adapterKey", "registry.loot.runtime_cache",
                "neoForgeId", "echoruntimehost:runtime_cache",
                "nativeLoaderId", "echoruntimehost:loot/runtime_cache",
                "standaloneRuntimeId", "echoruntimehost:runtime_cache",
                "metadata", Map.of(
                        "lootTableId", "echoruntimehost:runtime_cache",
                        "entryCounts", Map.of(runtimeLootItemId, 2, "#hydration", 1),
                        "sourceLogicalId", "runtime/native/content/runtime_cache_loot.json"
                )
        );
        EchoItemDefinition runtimeLootItem = EchoAdapterCoreNativeContentRegistrations
                .itemDefinitionsFromRows(List.of(runtimeItemRow))
                .get(0);
        registry.register(runtimeLootItem);
        EchoLootDefinition runtimeLootDefinition = EchoAdapterCoreNativeContentRegistrations
                .lootDefinitionsFromRows(List.of(runtimeLootRow))
                .get(0);
        EchoLootTable runtimeLootTable = new EchoCompatLootItemBridge()
                .toItemLootTable(runtimeLootDefinition, registry)
                .orElseThrow(() -> new AssertionError("native runtime loot row should bridge to item loot table"));
        EchoItemLootResult runtimeLoot = items.lootRuntime().grant(runtimeLootTable, crashCache);
        require(runtimeLoot.granted(), "native runtime loot row should grant through item loot runtime");
        require(runtimeLoot.quantityGranted() == 3, "native runtime loot row should grant three total items");
        require(items.operations().count(crashCache, new EchoItemId(runtimeLootItemId)) == 2,
                "crash cache should receive native runtime loot item");
        require(items.operations().count(crashCache, waterId) == 2,
                "tagged native loot entry should resolve to hydration item");

        List<String> tooltip = items.tooltipRenderer().render(new EchoItemStack(registry.require(patchedFilterId), 1));
        require(tooltip.contains("Patched Filter"), "tooltip should include item display name");
        require(tooltip.stream().anyMatch(line -> line.contains("Item: ashfall:patched_filter")),
                "tooltip should include stable item id");
        require(tooltip.stream().anyMatch(line -> line.startsWith("Use: ")),
                "tooltip should include player-facing item usage");
        require(tooltip.stream().anyMatch(line -> line.contains("crafted")), "tooltip should include item tags");
        require(tooltip.stream().anyMatch(line -> line.equals("State: Ready")),
                "tooltip should include ready state");
        List<String> disabledTooltip = items.tooltipRenderer().render(
                new EchoItemStack(registry.require(waterId), 1),
                false,
                "low_hydration_only"
        );
        require(disabledTooltip.stream().anyMatch(line -> line.equals("State: Disabled - low hydration only")),
                "tooltip should include disabled-state reason");
        List<String> useFeedback = items.tooltipRenderer().renderUseFeedback(
                new EchoItemStack(registry.require(waterId), 1),
                consumedWater
        );
        require(useFeedback.stream().anyMatch(line -> line.equals("Feedback: used 1 Clean Water Bottle")),
                "tooltip should include consume/use feedback");

        Path fixtureRoot = Files.createTempDirectory("echo-runtime-item-smoke");
        EchoSaveProfile saveProfile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "ashfall-items",
                "Ashfall Items",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/ashfall-items"),
                Map.of("phase", "14.11")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(services, saveProfile);
        EchoItemSaveResult saved = items.saveHook().save(saves, "slot-items", "tx-item-001");
        require(saved.commit().filesWritten() == 3, "item save hook should write summary and two inventories");
        require(saved.writtenPaths().contains("items/summary.json"), "item summary should be written");
        require(saved.writtenPaths().contains("items/inventories/inventory_player-001.json"),
                "player inventory should be written");
        require(saved.writtenPaths().contains("items/inventories/container_crash-cache.json"),
                "crash cache inventory should be written");

        EchoSaveManifest manifest = saves.readManifest("slot-items");
        require(manifest.file("items/summary.json").isPresent(), "manifest should track item summary");
        require(manifest.file("items/inventories/inventory_player-001.json").isPresent(),
                "manifest should track player inventory");
        require(manifest.file("items/inventories/container_crash-cache.json").isPresent(),
                "manifest should track crash cache inventory");
        EchoSaveCorruptionReport saveCheck = saves.check("slot-items");
        require(saveCheck.healthy(), "item save should pass corruption check");

        System.out.println("phase14.11 item runtime smoke PASS definitions="
                + registry.count()
                + " inventories="
                + store.count()
                + " transfer="
                + transferredBlade.quantity()
                + " consumed="
                + consumedWater.quantity()
                + " crafted="
                + crafted.outputQuantity()
                + " loot="
                + loot.quantityGranted()
                + " nativeLoot="
                + runtimeLoot.quantityGranted()
                + " savedFiles="
                + saved.writtenPaths().size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
