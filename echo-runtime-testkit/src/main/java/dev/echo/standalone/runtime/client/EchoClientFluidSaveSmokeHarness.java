package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
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
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime.EchoVoxelFluidTickResult;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime.EchoVoxelFluidType;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class EchoClientFluidSaveSmokeHarness {
    private static final String SLOT_ID = "client-fluid-save-smoke";
    private static final String DISPLAY_NAME = "Client Fluid Save Smoke";
    private static final Path REPORT_PATH = Path.of("reports", "echo", "standalone", "world-fluid-save-load.json");

    private EchoClientFluidSaveSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        FluidSaveResult result = requireFluidCellsRoundTrip();
        writeReport(result);
        System.out.println("client fluid save smoke PASS restoredFluids="
                + result.restoredFluidCells()
                + " savedContainsFluidProps="
                + result.savedContainsFluidProperties()
                + " hardened="
                + result.restoredHardenedId());
    }

    private static FluidSaveResult requireFluidCellsRoundTrip() throws IOException {
        Path fixtureRoot = Path.of("build", "tmp", "client-fluid-save-smoke").toAbsolutePath();
        deleteRecursively(fixtureRoot);

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.client_fluid_profile.v1",
                "client-fluid-smoke",
                "Client Fluid Smoke",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/client-fluid"),
                Map.of("surface", "echoscreencore:hud")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(new EchoDefaultRuntimeServiceRegistry(), profile);

        EchoVoxelWorld world = fluidWorld(bridge);
        EchoVoxelFluidRuntime fluids = new EchoVoxelFluidRuntime();
        require(fluids.placeSource(world, EchoVoxelFluidType.WATER, 15, 1, 3).placed(),
                "Water source should place at a chunk edge");
        require(fluids.placeSource(world, EchoVoxelFluidType.LAVA, 17, 1, 3).placed(),
                "Lava source should place in the neighboring chunk");
        require(fluids.placeSource(world, EchoVoxelFluidType.WATER, 3, 4, 3).placed(),
                "Falling water source should place above air");
        EchoVoxelFluidTickResult firstTick = fluids.tick(world, 1L);
        EchoVoxelFluidTickResult secondTick = fluids.tick(world, 2L);
        require(firstTick.crossChunkWrites() >= 1,
                "Fluid save fixture should include a cross-chunk fluid write before saving");
        require(firstTick.hardenedCells() >= 1,
                "Fluid save fixture should include water/lava hardening before saving");
        require(world.blockStateAt(3, 2, 3).block().id().equals(EchoVoxelFluidRuntime.WATER.id()),
                "Fluid save fixture should include a flowing falling water cell before saving");

        EchoClientWorldSession worldSession = EchoClientWorldSessionFactory.defaultFactory().restoreGameplaySnapshot(
                SLOT_ID,
                DISPLAY_NAME,
                new EchoClientGameplay.GameplaySnapshot(world, playerState(), new EchoVoxelPlayerHotbar(List.of(), 0)),
                List.of()
        );
        EchoClientGameplaySaveCodec.writeSession(saves, worldSession, "tx-fluid-save", "fluid-save-smoke");
        EchoSaveManifest manifest = saves.readManifest(SLOT_ID);
        require(manifest.file(EchoClientGameplaySaveCodec.CHUNKS_PATH).isPresent(),
                "Client save manifest should include chunk state");
        require(manifest.metadata().getOrDefault("clientSaveCodec", "").equals("echo.client.gameplay.v1"),
                "Client save manifest should advertise the gameplay codec");

        Path chunksPath = profile.slot(SLOT_ID).dataRoot().resolve(EchoClientGameplaySaveCodec.CHUNKS_PATH);
        String chunksText = Files.readString(chunksPath);
        boolean savedContainsFluidProperties = chunksText.contains(EchoVoxelFluidRuntime.WATER.id())
                && chunksText.contains(EchoVoxelFluidRuntime.LAVA.id())
                && chunksText.contains("fluidLevel")
                && chunksText.contains("fluidFalling")
                && chunksText.contains("fluid_hardening");
        require(savedContainsFluidProperties,
                "Chunk TSV should include fluid block ids and fluid metadata");

        EchoClientSavedSessionSnapshot restoredSnapshot = EchoClientGameplaySaveCodec.restoreSessionSnapshot(
                bridge,
                saves,
                manifest
        );
        EchoVoxelWorld restoredWorld = restoredSnapshot.gameplay().world();
        EchoVoxelBlockState restoredSource = restoredWorld.blockStateAt(15, 1, 3);
        EchoVoxelBlockState restoredFalling = restoredWorld.blockStateAt(3, 2, 3);
        EchoVoxelBlockState restoredHardened = restoredWorld.blockStateAt(16, 1, 3);

        require(restoredSource.block().id().equals(EchoVoxelFluidRuntime.WATER.id())
                        && restoredSource.property("fluidSource").orElse("").equals("true"),
                "Disk restore should preserve source water cells and source metadata");
        require(restoredFalling.block().id().equals(EchoVoxelFluidRuntime.WATER.id())
                        && restoredFalling.property("fluidFalling").orElse("").equals("true")
                        && restoredFalling.property("fluidLevel").orElse("").equals("1"),
                "Disk restore should preserve flowing falling water state");
        require(restoredHardened.block().id().equals(EchoVoxelFluidRuntime.HARDENED_FLUID_STONE.id())
                        && restoredHardened.property("interaction").orElse("").equals("fluid_hardening"),
                "Disk restore should preserve water/lava hardened fluid stone metadata");

        int restoredFluidCells = (int) restoredWorld.nonAirBlocks().stream()
                .filter(instance -> EchoVoxelFluidRuntime.isFluid(instance.state()))
                .count();
        require(restoredFluidCells >= 8,
                "Disk restore should preserve multiple source and flowing fluid cells");

        return new FluidSaveResult(
                manifest.slotId(),
                firstTick,
                secondTick,
                restoredFluidCells,
                restoredSource.block().id(),
                restoredFalling.block().id(),
                restoredFalling.property("fluidLevel").orElse(""),
                restoredHardened.block().id(),
                restoredHardened.property("interaction").orElse(""),
                chunksText.length(),
                savedContainsFluidProperties
        );
    }

    private static EchoVoxelWorld fluidWorld(EchoAdapterCoreStandaloneContentBridge bridge) {
        EchoVoxelBlock support = bridge.registry().requireLiveVoxelBlock(
                EchoAdapterCoreStandaloneContentBridge.SCORCHED_BASALT_BLOCK_ID
        );
        EchoVoxelChunk origin = new EchoVoxelChunk(new EchoVoxelChunkId(0, 0, 0), 16);
        EchoVoxelChunk east = new EchoVoxelChunk(new EchoVoxelChunkId(1, 0, 0), 16);
        origin.setBlockLocal(15, 0, 3, support);
        east.setBlockLocal(1, 0, 3, support);
        return new EchoVoxelWorld(
                "test:client_fluid_save",
                20260610L,
                16,
                List.of(origin, east),
                15.5D,
                2.0D,
                3.5D,
                90.0D
        );
    }

    private static EchoVoxelPlayerState playerState() {
        return new EchoVoxelPlayerState(
                15.5D,
                2.0D,
                3.5D,
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

    private static void writeReport(FluidSaveResult result) throws IOException {
        Files.createDirectories(REPORT_PATH.getParent());
        String json = """
                {
                  "schema": "echo.standalone.client_fluid_save_load_smoke.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoClientFluidSaveSmokeHarness",
                  "status": "PASS",
                  "summary": "Client save/load preserves standalone fluid source, flowing, and hardened cells through the real chunk TSV codec.",
                  "slotId": "%s",
                  "savedChunkTextBytes": %d,
                  "savedContainsFluidProperties": %s,
                  "preSaveTicks": {
                    "first": {
                      "downwardWrites": %d,
                      "horizontalWrites": %d,
                      "hardenedCells": %d,
                      "crossChunkWrites": %d
                    },
                    "second": {
                      "downwardWrites": %d,
                      "horizontalWrites": %d,
                      "hardenedCells": %d,
                      "crossChunkWrites": %d
                    }
                  },
                  "restored": {
                    "fluidCells": %d,
                    "sourceBlock": "%s",
                    "fallingBlock": "%s",
                    "fallingLevel": "%s",
                    "hardenedBlock": "%s",
                    "hardenedInteraction": "%s"
                  },
                  "evidence": {
                    "nativeModLoaderCommandUsed": false,
                    "realClientSaveCodec": true,
                    "engineOwnedFluidBlockResolver": true
                  }
                }
                """.formatted(
                escape(result.slotId()),
                result.savedChunkTextBytes(),
                Boolean.toString(result.savedContainsFluidProperties()),
                result.firstTick().downwardWrites(),
                result.firstTick().horizontalWrites(),
                result.firstTick().hardenedCells(),
                result.firstTick().crossChunkWrites(),
                result.secondTick().downwardWrites(),
                result.secondTick().horizontalWrites(),
                result.secondTick().hardenedCells(),
                result.secondTick().crossChunkWrites(),
                result.restoredFluidCells(),
                escape(result.restoredSourceId()),
                escape(result.restoredFallingId()),
                escape(result.restoredFallingLevel()),
                escape(result.restoredHardenedId()),
                escape(result.restoredHardenedInteraction())
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

    private record FluidSaveResult(
            String slotId,
            EchoVoxelFluidTickResult firstTick,
            EchoVoxelFluidTickResult secondTick,
            int restoredFluidCells,
            String restoredSourceId,
            String restoredFallingId,
            String restoredFallingLevel,
            String restoredHardenedId,
            String restoredHardenedInteraction,
            int savedChunkTextBytes,
            boolean savedContainsFluidProperties
    ) {
    }
}
