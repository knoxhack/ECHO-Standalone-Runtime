package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime.EchoVoxelFluidPlacement;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime.EchoVoxelFluidTickResult;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime.EchoVoxelFluidType;
import dev.echo.standalone.runtime.world.EchoVoxelHit;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class EchoClientFluidWaterloggingSmokeHarness {
    private static final String SLOT_ID = "client-fluid-waterlogging-smoke";
    private static final String DISPLAY_NAME = "Client Fluid Waterlogging Smoke";
    private static final Path REPORT_PATH = Path.of("reports", "echo", "standalone", "world-fluid-waterlogging.json");

    private EchoClientFluidWaterloggingSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        WaterloggingResult result = requireWaterloggingRuntimeBucketAndSave();
        writeReport(result);
        System.out.println("client fluid waterlogging smoke PASS source="
                + result.sourceWaterloggedBlockId()
                + " flow=" + result.flowWaterloggedBlockId()
                + " bucketCollect=" + result.bucketCollectedItemId()
                + " restored=" + result.restoredWaterloggedBlockId());
    }

    private static WaterloggingResult requireWaterloggingRuntimeBucketAndSave() throws IOException {
        Path fixtureRoot = Path.of("build", "tmp", "client-fluid-waterlogging-smoke").toAbsolutePath();
        deleteRecursively(fixtureRoot);

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        EchoVoxelBlock support = bridge.registry().requireLiveVoxelBlock(
                EchoAdapterCoreStandaloneContentBridge.SCORCHED_BASALT_BLOCK_ID
        );
        EchoVoxelWorld world = waterloggingWorld(support);
        EchoVoxelFluidRuntime fluids = new EchoVoxelFluidRuntime();

        EchoVoxelFluidPlacement sourceWaterlogged = fluids.placeSource(world, EchoVoxelFluidType.WATER, 5, 1, 4);
        require(sourceWaterlogged.placed() && sourceWaterlogged.reason().equals("source_waterlogged"),
                "Placing a source into a waterloggable host should waterlog the host block");
        EchoVoxelBlockState sourceState = world.blockStateAt(5, 1, 4);
        require(sourceState.block().id().equals(support.id()),
                "Source waterlogging should preserve the host block id");
        require(EchoVoxelFluidRuntime.isWaterlogged(sourceState)
                        && sourceState.property("fluidSource").orElse("").equals("true"),
                "Source waterlogging should mark source fluid metadata on the host block");

        require(fluids.placeSource(world, EchoVoxelFluidType.WATER, 2, 1, 4).placed(),
                "Water source should place next to a dry waterloggable host");
        EchoVoxelFluidTickResult flowTick = fluids.tick(world, 1L);
        EchoVoxelBlockState flowedState = world.blockStateAt(3, 1, 4);
        require(flowedState.block().id().equals(support.id()),
                "Flowing water should preserve the waterlogged host block id");
        require(EchoVoxelFluidRuntime.isWaterlogged(flowedState)
                        && flowedState.property("fluidLevel").orElse("").equals("1"),
                "Flowing water should attach waterlogged fluid level metadata");
        require(world.blockStateAt(4, 1, 4).block().id().equals(support.id())
                        && !EchoVoxelFluidRuntime.isFluid(world.blockStateAt(4, 1, 4)),
                "Non-waterloggable blocks should remain blocked and dry");

        EchoClientWorldSession worldSession = EchoClientWorldSessionFactory.defaultFactory().restoreGameplaySnapshot(
                SLOT_ID,
                DISPLAY_NAME,
                new EchoClientGameplay.GameplaySnapshot(world, playerState(), new EchoVoxelPlayerHotbar(List.of(), 0)),
                List.of()
        );
        EchoClientGameSession session = worldSession.gameSession();
        session.hotbar().select(0);
        session.player().selectSlot(0);
        session.playerInventory().slot(0).clear();
        session.playerInventory().slot(0).setStack(new EchoItemStack(EchoClientGameSession.waterBucketDefinition(), 1));

        EchoClientFluidBucketUse bucketPlace = session.useSelectedFluidBucket(
                new EchoVoxelHit(8, 1, 4, 0, 1, 0, support, 3.0D)
        );
        require(bucketPlace.used() && bucketPlace.x() == 8 && bucketPlace.y() == 1 && bucketPlace.z() == 4,
                "A water bucket used on a dry waterloggable host should waterlog that host cell");
        EchoVoxelBlockState bucketWaterlogged = session.world().blockStateAt(8, 1, 4);
        require(bucketWaterlogged.block().id().equals(support.id())
                        && EchoVoxelFluidRuntime.isWaterlogged(bucketWaterlogged),
                "Bucket placement should preserve host block identity while adding fluid metadata");
        require(session.playerInventory().slot(0).stack().orElseThrow().itemId().value().equals("minecraft:bucket"),
                "Placing water into a waterloggable host should return an empty bucket");

        EchoClientFluidBucketUse bucketCollect = session.useSelectedFluidBucket(
                new EchoVoxelHit(8, 1, 4, 0, 1, 0, support, 3.0D)
        );
        EchoVoxelBlockState drainedHost = session.world().blockStateAt(8, 1, 4);
        require(bucketCollect.used() && bucketCollect.action().equals("collect"),
                "An empty bucket should collect source water from a waterlogged host");
        require(drainedHost.block().id().equals(support.id())
                        && EchoVoxelFluidRuntime.isWaterloggable(drainedHost)
                        && !EchoVoxelFluidRuntime.isFluid(drainedHost),
                "Collecting from a waterlogged host should drain fluid metadata without deleting the host block");
        String bucketCollectedItemId = session.playerInventory().slot(0).stack().orElseThrow().itemId().value();
        require(bucketCollectedItemId.equals("minecraft:water_bucket"),
                "Collecting waterlogged source fluid should return a water bucket");

        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.client_fluid_waterlogging_profile.v1",
                "client-fluid-waterlogging-smoke",
                "Client Fluid Waterlogging Smoke",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/client-fluid-waterlogging"),
                Map.of("surface", "echoscreencore:hud")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(new EchoDefaultRuntimeServiceRegistry(), profile);
        EchoClientGameplaySaveCodec.writeSession(saves, worldSession, "tx-fluid-waterlogging", "fluid-waterlogging-smoke");
        EchoSaveManifest manifest = saves.readManifest(SLOT_ID);
        Path chunksPath = profile.slot(SLOT_ID).dataRoot().resolve(EchoClientGameplaySaveCodec.CHUNKS_PATH);
        String chunksText = Files.readString(chunksPath);
        require(chunksText.contains("waterlogged=true") && chunksText.contains("fluid=water"),
                "Chunk TSV should include waterlogged host metadata");

        EchoClientSavedSessionSnapshot restoredSnapshot = EchoClientGameplaySaveCodec.restoreSessionSnapshot(
                bridge,
                saves,
                manifest
        );
        EchoVoxelBlockState restoredWaterlogged = restoredSnapshot.gameplay().world().blockStateAt(5, 1, 4);
        EchoVoxelBlockState restoredDrainedHost = restoredSnapshot.gameplay().world().blockStateAt(8, 1, 4);
        require(restoredWaterlogged.block().id().equals(support.id())
                        && EchoVoxelFluidRuntime.isWaterlogged(restoredWaterlogged)
                        && restoredWaterlogged.property("fluidSource").orElse("").equals("true"),
                "Disk restore should preserve waterlogged source host block metadata");
        require(restoredDrainedHost.block().id().equals(support.id())
                        && EchoVoxelFluidRuntime.isWaterloggable(restoredDrainedHost)
                        && !EchoVoxelFluidRuntime.isFluid(restoredDrainedHost),
                "Disk restore should preserve a drained waterloggable host without stale fluid metadata");

        return new WaterloggingResult(
                sourceWaterlogged.reason(),
                flowTick.horizontalWrites(),
                sourceState.block().id(),
                flowedState.block().id(),
                bucketCollectedItemId,
                restoredWaterlogged.block().id(),
                restoredWaterlogged.property("fluidLevel").orElse(""),
                restoredDrainedHost.block().id(),
                chunksText.length()
        );
    }

    private static EchoVoxelWorld waterloggingWorld(EchoVoxelBlock support) {
        EchoVoxelChunk origin = new EchoVoxelChunk(new EchoVoxelChunkId(0, 0, 0), 16);
        for (int x = 1; x <= 8; x++) {
            origin.setBlockLocal(x, 0, 4, support);
        }
        origin.setStateLocal(3, 1, 4, waterloggableHost(support));
        origin.setBlockLocal(4, 1, 4, support);
        origin.setStateLocal(5, 1, 4, waterloggableHost(support));
        origin.setStateLocal(8, 1, 4, waterloggableHost(support));
        return new EchoVoxelWorld(
                "test:client_fluid_waterlogging",
                20260610L,
                16,
                List.of(origin),
                5.5D,
                2.0D,
                4.5D,
                90.0D
        );
    }

    private static EchoVoxelBlockState waterloggableHost(EchoVoxelBlock support) {
        return EchoVoxelBlockState.of(support)
                .withProperty("waterloggable", "true")
                .withProperty("shape", "grate");
    }

    private static EchoVoxelPlayerState playerState() {
        return new EchoVoxelPlayerState(
                5.5D,
                2.0D,
                4.5D,
                0.0D,
                90.0D,
                0.0D,
                true,
                false,
                false,
                0,
                EchoVoxelPlayerState.SURVIVAL_REACH
        );
    }

    private static void writeReport(WaterloggingResult result) throws IOException {
        Files.createDirectories(REPORT_PATH.getParent());
        String json = """
                {
                  "schema": "echo.standalone.client_fluid_waterlogging_smoke.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoClientFluidWaterloggingSmokeHarness",
                  "status": "PASS",
                  "summary": "Fluid waterlogging preserves waterloggable host block identity during source placement, flow, bucket placement/collection, and real client save/load.",
                  "runtime": {
                    "sourceReason": "%s",
                    "horizontalWrites": %d,
                    "sourceWaterloggedBlock": "%s",
                    "flowWaterloggedBlock": "%s"
                  },
                  "bucket": {
                    "collectedItem": "%s",
                    "drainedHostPreserved": "%s"
                  },
                  "restored": {
                    "waterloggedBlock": "%s",
                    "waterloggedLevel": "%s",
                    "savedChunkTextBytes": %d
                  },
                  "evidence": {
                    "nativeModLoaderCommandUsed": false,
                    "waterloggableStateProperty": true,
                    "hostBlockIdentityPreserved": true,
                    "bucketDrainPreservesHost": true,
                    "realClientSaveCodec": true
                  }
                }
                """.formatted(
                escape(result.sourceReason()),
                result.horizontalWrites(),
                escape(result.sourceWaterloggedBlockId()),
                escape(result.flowWaterloggedBlockId()),
                escape(result.bucketCollectedItemId()),
                escape(result.drainedHostBlockId()),
                escape(result.restoredWaterloggedBlockId()),
                escape(result.restoredWaterloggedLevel()),
                result.savedChunkTextBytes()
        );
        Files.writeString(REPORT_PATH, json);
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

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record WaterloggingResult(
            String sourceReason,
            int horizontalWrites,
            String sourceWaterloggedBlockId,
            String flowWaterloggedBlockId,
            String bucketCollectedItemId,
            String restoredWaterloggedBlockId,
            String restoredWaterloggedLevel,
            String drainedHostBlockId,
            int savedChunkTextBytes
    ) {
    }
}
