package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.item.EchoInventoryOperationResult;
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class EchoClientDroppedItemSmokeHarness {
    private EchoClientDroppedItemSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        requireSelectedItemDropPickup();
        requireRuntimeDropPickup();
        requireDroppedItemsMergeAndDespawn();
        requireDroppedItemPressureBudget();
        requireDiskRestore();
        System.out.println("client dropped item smoke PASS pickup=nearby save=restored");
    }

    private static void requireSelectedItemDropPickup() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("selected-drop-runtime").gameSession();
        EchoVoxelBlock block = session.bridge().runtimeMarkerBlock();
        session.hotbar().select(0);
        session.player().selectSlot(0);
        int beforeCount = countItem(session, block.id());
        require(beforeCount > 0,
                "Selected item drop smoke should start with a hotbar-backed block item");

        EchoClientDroppedItem drop = session.dropSelectedItem();
        require(drop != null && drop.itemId().value().equals(block.id()) && drop.quantity() == 1,
                "Dropping the selected item should create a one-item dropped entity");
        require(countItem(session, block.id()) == beforeCount - 1,
                "Dropping the selected item should remove one item from the inventory model");
        require(session.inventoryScreenModel().slot(0).count() == beforeCount - 1,
                "Dropping the selected hotbar item should sync the hotbar-backed inventory slot");
        require(session.droppedItemCount() == 1 && session.droppedItemQuantity() == 1,
                "Selected item drop should be visible in the dropped-item runtime");

        EchoClientDroppedItemRuntime.PickupResult pickup = session.pickupNearbyDroppedItems();
        require(pickup.pickedQuantity() == 1 && pickup.remainingDrops() == 0,
                "Nearby pickup should collect the selected item drop");
        require(countItem(session, block.id()) == beforeCount,
                "Picking the selected item drop back up should restore the inventory item count");
    }

    private static void requireRuntimeDropPickup() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("dropped-item-runtime").gameSession();
        EchoVoxelBlock block = session.bridge().runtimeMarkerBlock();
        int beforeCount = countItem(session, block.id());
        EchoClientDroppedItem drop = session.dropBlockItem(block);
        require(drop != null && drop.itemId().value().equals(block.id()),
                "Dropping a block should create an item-backed dropped entity");
        require(session.droppedItemCount() == 1 && session.droppedItemQuantity() == 1,
                "Dropped item runtime should expose live drop counts");
        require(countItem(session, block.id()) == beforeCount,
                "Creating a dropped item should not collect it until pickup runs");
        session.tickEntities(0.5D);
        require(session.droppedItemSnapshots().getFirst().ageSeconds() >= 0.5D,
                "Dropped items should age during the live entity tick");

        EchoClientDroppedItemRuntime.PickupResult result = session.pickupNearbyDroppedItems();
        require(result.pickedQuantity() == 1 && result.remainingDrops() == 0,
                "Nearby pickup should move the dropped item into player inventory");
        require(countItem(session, block.id()) == beforeCount + 1,
                "Picked up dropped item should appear in the item-runtime inventory");
    }

    private static void requireDroppedItemsMergeAndDespawn() {
        EchoClientDroppedItemRuntime runtime = new EchoClientDroppedItemRuntime();
        EchoItemDefinition definition = new EchoItemDefinition(
                new EchoItemId("echoashfallprotocol:merge_smoke_scrap"),
                "Merge Smoke Scrap",
                EchoItemCategory.MATERIAL,
                64,
                1.0D,
                List.of("dropped_item_smoke"),
                List.of("Dropped item merge smoke")
        );
        require(runtime.drop(new EchoItemStack(definition, 1), 6.0D, 0.0D, 0.0D) != null,
                "Dropped item merge smoke should create the first far dropped item");
        require(runtime.drop(new EchoItemStack(definition, 1), 6.2D, 0.0D, 0.2D) != null,
                "Dropped item merge smoke should create or merge the nearby second dropped item");
        require(runtime.count() == 1 && runtime.totalQuantity() == 2,
                "Nearby dropped items with the same id should merge into one runtime entity");
        runtime.tick(299.0D);
        require(runtime.count() == 1,
                "Dropped item should remain before the despawn boundary");
        runtime.tick(1.0D);
        require(runtime.count() == 0 && runtime.totalQuantity() == 0,
                "Dropped item should despawn at the five-minute boundary");
    }

    private static void requireDroppedItemPressureBudget() {
        EchoClientDroppedItemRuntime runtime = new EchoClientDroppedItemRuntime();
        EchoItemDefinition definition = new EchoItemDefinition(
                new EchoItemId("echoashfallprotocol:budget_smoke_scrap"),
                "Budget Smoke Scrap",
                EchoItemCategory.MATERIAL,
                64,
                1.0D,
                List.of("dropped_item_smoke"),
                List.of("Dropped item pressure budget smoke")
        );
        for (int i = 0; i < EchoClientDroppedItemRuntime.MAX_ACTIVE_DROPS + 24; i++) {
            runtime.drop(new EchoItemStack(definition, 1), 20.0D + i * 2.0D, 0.0D, 20.0D);
        }
        require(runtime.count() == EchoClientDroppedItemRuntime.MAX_ACTIVE_DROPS,
                "Dropped item runtime should cap active entities under item pressure");
        require(runtime.totalQuantity() == EchoClientDroppedItemRuntime.MAX_ACTIVE_DROPS,
                "Dropped item budget trimming should keep total quantity cache in sync");
        require(runtime.physicsDropOrderSize() == runtime.count(),
                "Dropped item budget trimming should keep physics iteration order in sync");
        require(runtime.spatialBucketCount() > 1,
                "Dropped item pressure budget should spread active drops across packed spatial buckets");

        runtime.drop(new EchoItemStack(definition, 1), 0.0D, 0.0D, 0.0D);
        EchoClientDroppedItemRuntime.PickupResult pickup = runtime.pickupNearby(
                stack -> new EchoInventoryOperationResult("add", true, stack.quantity(), "added"),
                0.0D,
                0.0D,
                0.0D,
                1.75D
        );
        require(pickup.pickedQuantity() == 1,
                "Dropped item spatial lookup should collect a nearby drop even when far drops are at the budget");
        require(runtime.lastNearbyQueryBucketCount() > 0 && runtime.lastNearbyQueryDropIdCount() == 1,
                "Dropped item spatial lookup should scan packed buckets without duplicate nearby ids");
        require(runtime.count() == EchoClientDroppedItemRuntime.MAX_ACTIVE_DROPS - 1,
                "Picking up the pressure-budget near drop should remove only that entity");
        require(runtime.physicsDropOrderSize() == runtime.count(),
                "Dropped item pickup should remove collected drops from physics iteration order");

        EchoClientDroppedItemRuntime restored = new EchoClientDroppedItemRuntime();
        restored.applySnapshots(runtime.snapshots());
        require(restored.physicsDropOrderSize() == restored.count(),
                "Dropped item restore should rebuild physics iteration order from snapshots");
        restored.tick(300.0D);
        require(restored.count() == 0
                        && restored.totalQuantity() == 0
                        && restored.physicsDropOrderSize() == 0,
                "Dropped item despawn should clear physics iteration order with active drops");
    }

    private static void requireDiskRestore() throws IOException {
        Path fixtureRoot = Path.of("build", "tmp", "client-dropped-item-save-smoke").toAbsolutePath();
        deleteRecursively(fixtureRoot);
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.client_dropped_item_profile.v1",
                "client-dropped-item-smoke",
                "Client Dropped Item Smoke",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/client-dropped-item"),
                Map.of("surface", "echoscreencore:hud")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(new EchoDefaultRuntimeServiceRegistry(), profile);
        EchoClientWorldSession worldSession =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("dropped-item-save");
        EchoClientGameSession session = worldSession.gameSession();
        EchoVoxelBlock block = session.bridge().runtimeMarkerBlock();
        EchoClientDroppedItem drop = session.dropBlockItem(
                block,
                session.player().state().x() + 6.0D,
                session.player().state().y(),
                session.player().state().z()
        );
        require(drop != null, "Disk restore smoke should create a dropped item before saving");
        require(session.pickupNearbyDroppedItems().pickedQuantity() == 0,
                "Far dropped item should remain in-world until the player is nearby");

        EchoClientGameplaySaveCodec.writeSession(saves, worldSession, "tx-dropped-item-save", "dropped-item-save");
        EchoSaveManifest manifest = saves.readManifest(worldSession.slotId());
        require(manifest.file(EchoClientGameplaySaveCodec.DROPPED_ITEMS_PATH).isPresent(),
                "Client save manifest should include dropped item state");
        require(manifest.metadata().getOrDefault("clientDroppedItemsCodec", "").equals("echo.client.dropped_items.v1"),
                "Client save manifest should advertise the dropped item codec");

        Path droppedItemsPath = fixtureRoot.resolve("profiles/client-dropped-item/slots")
                .resolve(worldSession.slotId())
                .resolve("data")
                .resolve(EchoClientGameplaySaveCodec.DROPPED_ITEMS_PATH);
        String droppedItemsText = Files.readString(droppedItemsPath);
        require(droppedItemsText.contains(block.id()) && droppedItemsText.contains(drop.dropId()),
                "Dropped item save sidecar should include drop id and item id");

        EchoClientSavedSessionSnapshot restoredSnapshot = EchoClientGameplaySaveCodec.restoreSessionSnapshot(
                EchoAdapterCoreStandaloneContentBridge.ashfallLive(),
                saves,
                manifest
        );
        EchoClientWorldSession restored = EchoClientWorldSession.fromSavedSession(
                manifest.slotId(),
                manifest.metadata().getOrDefault("displayName", manifest.slotId()),
                restoredSnapshot
        );
        EchoClientGameSession restoredSession = restored.gameSession();
        require(restoredSession.droppedItemCount() == 1 && restoredSession.droppedItemQuantity() == 1,
                "Disk restore should preserve dropped item runtime state");
        require(restoredSession.droppedItemSnapshots().getFirst().itemId().equals(block.id()),
                "Disk restore should preserve dropped item definition id");
    }

    private static int countItem(EchoClientGameSession session, String itemId) {
        return session.inventoryScreenModel().slots().stream()
                .filter(slot -> slot.runtimeId().equals(itemId))
                .mapToInt(EchoClientSlotStack::count)
                .sum();
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.delete(path);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
