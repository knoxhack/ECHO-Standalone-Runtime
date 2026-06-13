package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime.EchoVoxelFluidType;
import dev.echo.standalone.runtime.world.EchoVoxelHit;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoClientFluidBucketSmokeHarness {
    private static final Path REPORT_PATH = Path.of("reports", "echo", "standalone", "world-fluid-buckets.json");

    private EchoClientFluidBucketSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        BucketSmokeResult result = requireGameplayAndSessionBucketUse();
        writeReport(result);
        System.out.println("client fluid bucket smoke PASS collected="
                + result.collectedItemId()
                + " placed=" + result.placedWaterId()
                + " lava=" + result.placedLavaId()
                + " gameplay=" + result.gameplayEventAction());
    }

    private static BucketSmokeResult requireGameplayAndSessionBucketUse() {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        EchoVoxelBlock support = bridge.registry().requireLiveVoxelBlock(
                EchoAdapterCoreStandaloneContentBridge.SCORCHED_BASALT_BLOCK_ID
        );
        EchoVoxelWorld world = bucketWorld(support);
        EchoVoxelFluidRuntime fluids = new EchoVoxelFluidRuntime();
        require(fluids.placeSource(world, EchoVoxelFluidType.WATER, 2, 2, 4).placed(),
                "Bucket smoke should place a source water target in front of the player");

        EchoClientWorldSession worldSession = EchoClientWorldSessionFactory.defaultFactory().restoreGameplaySnapshot(
                "client-fluid-bucket-smoke",
                "Client Fluid Bucket Smoke",
                new EchoClientGameplay.GameplaySnapshot(
                        world,
                        playerState(),
                        new EchoVoxelPlayerHotbar(List.of(), 0)
                ),
                List.of()
        );
        EchoClientGameSession session = worldSession.gameSession();
        session.hotbar().select(0);
        session.player().selectSlot(0);
        session.playerInventory().slot(0).clear();
        session.playerInventory().slot(0).setStack(new EchoItemStack(EchoClientGameSession.emptyBucketDefinition(), 1));

        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        gameplay.tick(EchoVoxelPlayerInput.idle(), new FakeGameplayInput(), 1.0D / 60.0D, session);

        EchoClientSelectedItemUse use = gameplay.consumeSelectedItemUse();
        require(use.active() && use.action().equals("bucket"),
                "Right-clicking source water with an empty bucket should emit a bucket use event");
        require(session.world().blockAt(2, 2, 4).air(),
                "Right-clicking source water with an empty bucket should remove the source cell");
        String collectedItemId = session.playerInventory().slot(0).stack().orElseThrow().itemId().value();
        require(collectedItemId.equals("minecraft:water_bucket"),
                "Collecting source water should replace the selected bucket with a water bucket");

        EchoClientFluidBucketUse placedWater = session.useSelectedFluidBucket(
                new EchoVoxelHit(4, 1, 4, 0, 1, 0, support, 3.0D)
        );
        require(placedWater.used() && placedWater.action().equals("place"),
                "Using a water bucket on a support block should place a water source");
        require(session.world().blockStateAt(4, 2, 4).block().id().equals(EchoVoxelFluidRuntime.WATER.id()),
                "Water bucket placement should create a water source cell");
        require(session.world().blockStateAt(4, 2, 4).property("fluidSource").orElse("").equals("true"),
                "Water bucket placement should mark the placed cell as a source");
        require(session.playerInventory().slot(0).stack().orElseThrow().itemId().value().equals("minecraft:bucket"),
                "Placing water should replace the selected water bucket with an empty bucket");

        session.playerInventory().slot(0).setStack(new EchoItemStack(EchoClientGameSession.lavaBucketDefinition(), 1));
        EchoClientFluidBucketUse placedLava = session.useSelectedFluidBucket(
                new EchoVoxelHit(5, 1, 4, 0, 1, 0, support, 3.0D)
        );
        require(placedLava.used() && placedLava.action().equals("place"),
                "Using a lava bucket on a support block should place a lava source");
        require(session.world().blockStateAt(5, 2, 4).block().id().equals(EchoVoxelFluidRuntime.LAVA.id()),
                "Lava bucket placement should create a lava source cell");
        require(session.playerInventory().slot(0).stack().orElseThrow().itemId().value().equals("minecraft:bucket"),
                "Placing lava should replace the selected lava bucket with an empty bucket");

        return new BucketSmokeResult(
                use.action(),
                use.toastText(),
                collectedItemId,
                session.world().blockStateAt(4, 2, 4).block().id(),
                session.world().blockStateAt(5, 2, 4).block().id(),
                session.playerInventory().slot(0).stack().orElseThrow().itemId().value()
        );
    }

    private static EchoVoxelWorld bucketWorld(EchoVoxelBlock support) {
        EchoVoxelChunk origin = new EchoVoxelChunk(new EchoVoxelChunkId(0, 0, 0), 16);
        origin.setBlockLocal(4, 1, 4, support);
        origin.setBlockLocal(5, 1, 4, support);
        return new EchoVoxelWorld(
                "test:client_fluid_bucket",
                20260610L,
                16,
                List.of(origin),
                2.5D,
                1.0D,
                1.2D,
                0.0D
        );
    }

    private static EchoVoxelPlayerState playerState() {
        return new EchoVoxelPlayerState(
                2.5D,
                1.0D,
                1.2D,
                0.0D,
                0.0D,
                0.0D,
                true,
                false,
                false,
                0,
                EchoVoxelPlayerState.SURVIVAL_REACH
        );
    }

    private static void writeReport(BucketSmokeResult result) throws IOException {
        Files.createDirectories(REPORT_PATH.getParent());
        String json = """
                {
                  "schema": "echo.standalone.client_fluid_bucket_smoke.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoClientFluidBucketSmokeHarness",
                  "status": "PASS",
                  "summary": "Fluid buckets collect source water through gameplay right-click and place water/lava source cells through the client session item path.",
                  "gameplay": {
                    "rightClickAction": "%s",
                    "toast": "%s",
                    "collectedItem": "%s"
                  },
                  "placements": {
                    "waterBlock": "%s",
                    "lavaBlock": "%s",
                    "finalSelectedItem": "%s"
                  },
                  "evidence": {
                    "nativeModLoaderCommandUsed": false,
                    "realGameplayRightClick": true,
                    "survivalInventoryTransform": true
                  }
                }
                """.formatted(
                escape(result.gameplayEventAction()),
                escape(result.gameplayToast()),
                escape(result.collectedItemId()),
                escape(result.placedWaterId()),
                escape(result.placedLavaId()),
                escape(result.finalSelectedItemId())
        );
        Files.writeString(REPORT_PATH, json);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record BucketSmokeResult(
            String gameplayEventAction,
            String gameplayToast,
            String collectedItemId,
            String placedWaterId,
            String placedLavaId,
            String finalSelectedItemId
    ) {
    }

    private static final class FakeGameplayInput implements EchoClientGameplayInput {
        private boolean place = true;

        @Override
        public int selectedHotbarSlot(int current) {
            return 0;
        }

        @Override
        public boolean consumeBreak() {
            return false;
        }

        @Override
        public boolean isCursorLocked() {
            return true;
        }

        @Override
        public boolean consumePlace() {
            boolean value = place;
            place = false;
            return value;
        }
    }
}
