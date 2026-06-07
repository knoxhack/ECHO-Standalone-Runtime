package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class EchoClientBlockLootDataSmokeHarness {
    private static final String PACK_ID = "client-block-loot-data-smoke";
    private static final String DIRECT_DROP_ID = "smokeloot:salvage_chip";
    private static final String TAGGED_DROP_ID = "smokeloot:tagged_bolt";
    private static final String BLOCK_TABLE_ID = "echoadaptercore:blocks/runtime_marker_block";

    private EchoClientBlockLootDataSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path packRoot = Path.of("resourcepacks", PACK_ID).toAbsolutePath();
        deleteRecursively(packRoot);
        writeFixturePack(packRoot);
        try {
            EchoClientRuntimeServices services = new EchoClientRuntimeServices();
            require(services.resourcePackSummaries().stream().anyMatch(pack -> pack.id().equals(PACK_ID)),
                    "Client resource pack service should mount the block loot fixture pack");
            services.startNewWorld("block-loot-data-smoke");
            EchoClientGameSession session = services.session();
            require(session != null, "Block loot smoke should start an active client game session");
            EchoVoxelBlock block = session.bridge().runtimeMarkerBlock();
            int beforeBlockSelf = countItem(session, block.id());
            int beforeDirectDrop = countItem(session, DIRECT_DROP_ID);
            int beforeTaggedDrop = countItem(session, TAGGED_DROP_ID);

            List<EchoClientDroppedItem> drops = session.dropBlockItems(block);
            require(drops.size() == 2,
                    "Runtime marker block should use two entries from the mounted loot table");
            require(drops.stream().anyMatch(drop -> drop.itemId().value().equals(DIRECT_DROP_ID)),
                    "Mounted block loot should create the direct datapack item drop");
            require(drops.stream().anyMatch(drop -> drop.itemId().value().equals(TAGGED_DROP_ID)),
                    "Mounted block loot should resolve the datapack item tag into a concrete dropped item");
            require(countItem(session, block.id()) == beforeBlockSelf,
                    "Data-driven block loot should not fall back to dropping the block itself");

            EchoClientDroppedItemRuntime.PickupResult pickup = session.pickupNearbyDroppedItems();
            require(pickup.pickedQuantity() == 2 && pickup.remainingDrops() == 0,
                    "Nearby pickup should collect both data-driven block loot drops");
            require(countItem(session, DIRECT_DROP_ID) == beforeDirectDrop + 1,
                    "Direct loot entry should land in the player inventory");
            require(countItem(session, TAGGED_DROP_ID) == beforeTaggedDrop + 1,
                    "Tagged loot entry should land in the player inventory");

            System.out.println("client block loot data smoke PASS table=" + BLOCK_TABLE_ID
                    + " direct=" + DIRECT_DROP_ID
                    + " tagged=" + TAGGED_DROP_ID);
        } finally {
            deleteRecursively(packRoot);
        }
    }

    private static void writeFixturePack(Path packRoot) throws IOException {
        write(packRoot.resolve("pack.mcmeta"), """
                {
                  "pack": {
                    "pack_format": 15,
                    "description": "Client block loot data smoke"
                  }
                }
                """);
        write(packRoot.resolve("assets/smokeloot/lang/en_us.json"), """
                {
                  "item.smokeloot.salvage_chip": "Salvage Chip",
                  "item.smokeloot.tagged_bolt": "Tagged Bolt"
                }
                """);
        write(packRoot.resolve("data/smokeloot/tags/items/bonus_drops.json"), """
                {
                  "values": ["smokeloot:tagged_bolt"]
                }
                """);
        write(packRoot.resolve("data/echoadaptercore/loot_table/blocks/runtime_marker_block.json"), """
                {
                  "entries": [
                    "smokeloot:salvage_chip",
                    "#smokeloot:bonus_drops"
                  ]
                }
                """);
    }

    private static int countItem(EchoClientGameSession session, String itemId) {
        return session.inventoryScreenModel().slots().stream()
                .filter(slot -> slot.runtimeId().equals(itemId))
                .mapToInt(EchoClientSlotStack::count)
                .sum();
    }

    private static void write(Path path, String text) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, text);
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
