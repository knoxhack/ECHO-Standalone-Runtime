package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerController;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBiome;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelChunkSource;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldStreamer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class EchoClientWorldStreamingSmokeHarness {
    private EchoClientWorldStreamingSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("42").gameSession();
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());

        int defaultView = EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE;
        EchoClientWorldStreamResult initial = session.streamAroundPlayer(defaultView);
        require(initial.playerChunkChanged(), "Initial stream should establish the player chunk");
        require(session.cachedChunkCount() >= session.world().loadedChunkCount(),
                "Initial stream should cache at least the active loaded region");

        EchoVoxelPlayerState spawn = session.player().state();
        int widerView = defaultView + 1;
        EchoClientWorldStreamResult prefetch = session.streamAroundPlayer(widerView);
        require(prefetch.viewDistanceChanged(), "Expanding the view should prefetch neighbor traversal cache");
        require(prefetch.loadedChunksChanged(), "Expanding the view should load the prefetch ring");
        EchoClientWorldStreamResult resetView = session.streamAroundPlayer(defaultView);
        require(resetView.viewDistanceChanged(), "Returning to the default view should reset the active radius");
        require(resetView.loadedChunksChanged(), "Returning to the default view should prune active chunks");

        int loadedBeforeNeighbor = session.world().loadedChunkCount();
        int cachedBeforeNeighbor = session.cachedChunkCount();
        double neighborZ = adjacentChunkCenterZ(spawn, session.world().chunkSize());
        movePlayer(session, gameplay, spawn.x(), spawn.y(), neighborZ);
        EchoClientWorldStreamResult neighbor = session.streamAroundPlayer(defaultView);
        require(neighbor.playerChunkChanged(), "Moving to an already loaded neighboring chunk should be reported");
        require(neighbor.renderRegionChanged(), "Moving to a loaded neighboring chunk should refresh render meshes");
        require(!neighbor.cachedChunksChanged(),
                "Moving within cached chunks should not report cache growth");
        require(session.cachedChunkCount() == cachedBeforeNeighbor,
                "Cached neighbor traversal should not generate extra chunks");
        require(session.world().loadedChunkCount() == loadedBeforeNeighbor,
                "Loaded neighbor traversal should keep the active view radius stable");
        requireClose(session.player().state().x(), spawn.x(), "loaded neighbor x");
        requireClose(session.player().state().z(), neighborZ, "loaded neighbor z");

        EchoClientWorldStreamResult idle = session.streamAroundPlayer(defaultView);
        require(!idle.renderRegionChanged(), "Streaming twice in the same chunk should be idle");

        int loadedBeforeWider = session.world().loadedChunkCount();
        EchoClientWorldStreamResult wider = session.streamAroundPlayer(widerView);
        require(wider.viewDistanceChanged(), "Changing chunk view in the same chunk should be reported");
        require(wider.renderRegionChanged(), "Changing chunk view should refresh render meshes");
        require(wider.loadedChunksChanged(), "Increasing chunk view should stream additional chunks");
        require(session.world().loadedChunkCount() > loadedBeforeWider,
                "Increasing chunk view should expand the loaded world");
        int loadedBeforeShrink = session.world().loadedChunkCount();
        int cachedBeforeShrink = session.cachedChunkCount();
        EchoClientWorldStreamResult shrink = session.streamAroundPlayer(defaultView);
        require(shrink.viewDistanceChanged(), "Shrinking chunk view should be reported");
        require(shrink.renderRegionChanged(), "Shrinking chunk view should refresh render meshes");
        require(shrink.loadedChunksChanged(), "Shrinking chunk view should unload active chunks");
        require(session.world().loadedChunkCount() < loadedBeforeShrink,
                "Shrinking chunk view should prune active chunks");
        require(session.cachedChunkCount() == cachedBeforeShrink,
                "Shrinking chunk view should keep pruned chunks in the reload cache");
        EchoClientWorldStreamResult rewide = session.streamAroundPlayer(widerView);
        require(rewide.loadedChunksChanged()
                        && session.world().loadedChunkCount() == loadedBeforeShrink
                        && session.cachedChunkCount() == cachedBeforeShrink,
                "Re-expanding chunk view should reload cached chunks without regenerating them");
        require(session.streamAroundPlayer(defaultView).loadedChunksChanged(),
                "Returning to the default chunk view should prune active chunks again");

        int loadedBeforeFar = session.world().loadedChunkCount();
        int cachedBeforeFar = session.cachedChunkCount();
        session.materializeMachineBlockEntities();
        EchoVoxelChunkId originMachineChunk = EchoVoxelChunkId.fromBlock(9, 5, 9, session.world().chunkSize());
        EchoVoxelBlockState itemPipeBeforeUnload = session.world().blockStateAt(9, 5, 9);
        require(itemPipeBeforeUnload.property("blockEntityId").orElse("").equals("item_pipe"),
                "Machine block entity metadata should be active before chunk unload");
        double farZ = (defaultView + 2) * session.world().chunkSize() + 0.5D;
        movePlayer(session, gameplay, spawn.x(), spawn.y(), farZ);
        EchoClientWorldStreamResult far = session.streamAroundPlayer(defaultView);
        require(far.playerChunkChanged(), "Moving to a far chunk should report chunk traversal");
        require(far.loadedChunksChanged(), "Moving beyond the loaded radius should swap active chunks");
        require(far.cachedChunksChanged(), "Moving beyond the cached radius should report cache growth");
        require(session.world().loadedChunkCount() == loadedBeforeFar,
                "Far traversal should keep only the active view radius resident");
        require(session.cachedChunkCount() > cachedBeforeFar,
                "Far traversal should generate and cache new chunks");
        require(!session.world().hasChunk(originMachineChunk),
                "Far traversal should unload the origin machine chunk from the active world");
        require(session.world().blockAt(9, 5, 9).air(),
                "Blocks in an unloaded machine chunk should not be visible in the active world");
        require(session.reconcileMachineBlockEntitiesFromWorld() == 7,
                "Machine reconciliation should still find cached block entities after chunk unload");
        requireClose(session.player().state().x(), spawn.x(), "far stream x should not rubber-band");
        requireClose(session.player().state().z(), farZ, "far stream z should not rubber-band");
        movePlayer(session, gameplay, spawn.x(), spawn.y(), spawn.z());
        EchoClientWorldStreamResult back = session.streamAroundPlayer(defaultView);
        EchoVoxelBlockState itemPipeAfterReload = session.world().blockStateAt(9, 5, 9);
        require(back.loadedChunksChanged()
                        && session.world().hasChunk(originMachineChunk)
                        && itemPipeAfterReload.block().id().equals(itemPipeBeforeUnload.block().id())
                        && itemPipeAfterReload.property("blockEntityId").orElse("").equals("item_pipe"),
                "Returning to origin should reload cached machine block entity state");
        require(!back.cachedChunksChanged(),
                "Returning to already cached origin chunks should not report cache growth");

        requireCachedChunkSaveIncludesUnloadedMachineBlocks();
        requireCleanGeneratedChunkCacheIsBoundedAndDirtyChunksSurvive();
        requireChunkInspectionCacheSkipsUnchangedChunks();
        requireStreamerUpdateInvalidatesOldCleanGeneratedChunks();
        requireRuntimeWorldgenHotImportStreamsNewChunks();
        requireDataWorldgenFeatureStreamsNewChunks();

        System.out.println("client world streaming smoke PASS loadedChunks="
                + session.world().loadedChunkCount()
                + " cachedChunks=" + session.cachedChunkCount()
                + " neighborRenderRefresh=" + neighbor.renderRegionChanged()
                + " farLoaded=" + far.loadedChunksChanged());
    }

    private static void requireCachedChunkSaveIncludesUnloadedMachineBlocks() throws IOException {
        Path saveRoot = Path.of("build", "tmp", "client-world-stream-cache-save-smoke").toAbsolutePath();
        EchoClientRuntimeServices services = new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        services.startNewWorld("chunk-cache-save");
        EchoClientGameSession session = services.session();
        require(session != null, "Chunk cache save smoke requires a live session");
        session.materializeMachineBlockEntities();
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        EchoVoxelPlayerState spawn = session.player().state();
        int defaultView = EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE;
        session.streamAroundPlayer(defaultView);
        int activeBeforeFar = session.world().loadedChunkCount();
        double farZ = (defaultView + 2) * session.world().chunkSize() + 0.5D;
        movePlayer(session, gameplay, spawn.x(), spawn.y(), farZ);
        session.streamAroundPlayer(defaultView);
        require(session.world().loadedChunkCount() == activeBeforeFar
                        && session.cachedChunkCount() > session.world().loadedChunkCount(),
                "Chunk cache save smoke should have unloaded cached chunks before saving");
        services.captureMemorySave();
        String slotId = services.worldSession().slotId();
        Path slotRoot = saveRoot.resolve("slots").resolve(slotId).resolve("data");
        String chunks = Files.readString(slotRoot.resolve(EchoClientGameplaySaveCodec.CHUNKS_PATH));
        String sessionText = Files.readString(slotRoot.resolve(EchoClientGameplaySaveCodec.SESSION_PATH));
        require(chunks.contains("item_pipe") && chunks.contains("blockEntityId=item_pipe"),
                "Disk save should include cached unloaded machine block entity chunks");
        require(sessionText.contains("world.cachedChunks=")
                        && sessionText.contains("world.savedChunks="),
                "Disk save session metadata should expose cached and saved chunk diagnostics");
    }

    private static void requireCleanGeneratedChunkCacheIsBoundedAndDirtyChunksSurvive() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("chunk-cache-eviction").gameSession();
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        int defaultView = EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE;
        session.streamAroundPlayer(defaultView);
        EchoVoxelPlayerState spawn = session.player().state();
        int dirtyX = (int) Math.floor(spawn.x()) + 2;
        int dirtyY = 4;
        int dirtyZ = (int) Math.floor(spawn.z()) + 2;
        EchoVoxelBlockState dirtyState = EchoVoxelBlockState
                .of(session.bridge().runtimeMarkerBlock())
                .withProperty("cacheSmoke", "dirty");
        require(session.world().setBlockStateAt(dirtyX, dirtyY, dirtyZ, dirtyState),
                "Chunk cache eviction smoke should place a dirty block in the origin chunk");

        int strideBlocks = (defaultView + 3) * session.world().chunkSize();
        for (int step = 1; step <= 9; step++) {
            movePlayer(session, gameplay, spawn.x(), spawn.y(), spawn.z() + strideBlocks * step);
            session.streamAroundPlayer(defaultView);
        }

        int cleanCacheAllowance = session.world().loadedChunkCount()
                + session.cachedProtectedChunkCount()
                + EchoClientWorldRuntime.CLEAN_CACHE_EXTRA_CHUNKS;
        require(session.cachedChunkCount() <= cleanCacheAllowance,
                "Clean generated chunk cache should stay bounded while preserving protected chunks; cached="
                        + session.cachedChunkCount()
                        + " active=" + session.world().loadedChunkCount()
                        + " protected=" + session.cachedProtectedChunkCount());

        movePlayer(session, gameplay, spawn.x(), spawn.y(), spawn.z());
        session.streamAroundPlayer(defaultView);
        require(session.world().blockStateAt(dirtyX, dirtyY, dirtyZ).property("cacheSmoke").orElse("").equals("dirty"),
                "Dirty cached chunks should survive clean generated chunk eviction and reload intact");
    }

    private static void requireChunkInspectionCacheSkipsUnchangedChunks() {
        int chunkSize = 16;
        int defaultView = EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE;
        EchoVoxelBlock block = new EchoVoxelBlock(
                "echotest:chunk_inspection_cache",
                "Chunk Inspection Cache",
                0xFF7892C7,
                true,
                true,
                0.8D
        );
        EchoVoxelChunkSource source = streamerSwapSource(block, "inspection", chunkSize);
        EchoVoxelWorld world = new EchoVoxelWorld(
                "chunk-inspection-cache",
                23L,
                chunkSize,
                List.of(source.generateChunk(23L, 0, 0, 0)),
                0.5D,
                2.0D,
                0.5D,
                0.0D
        );
        EchoClientWorldRuntime runtime = new EchoClientWorldRuntime(
                world,
                new EchoVoxelWorldStreamer(source, defaultView),
                EchoClientHazardCatalog.empty()
        );

        runtime.cachedChunkCount();
        require(runtime.lastChunkInspectionScanCount() == 0
                        && runtime.lastChunkInspectionCellScanCount() == 0,
                "Repeated chunk cache bookkeeping should reuse unchanged chunk inspections");

        EchoVoxelBlockState changedState = EchoVoxelBlockState.of(block)
                .withProperty("inspectionSmoke", "changed");
        require(runtime.setBlockStateAt(1, 1, 1, changedState),
                "Chunk inspection cache smoke should mutate the active chunk");
        runtime.cachedChunkCount();
        require(runtime.lastChunkInspectionScanCount() == 1,
                "Only the changed chunk should rescan after its version changes; scans="
                        + runtime.lastChunkInspectionScanCount());
        require(runtime.lastChunkInspectionCellScanCount() == chunkSize * chunkSize * chunkSize,
                "Changed chunk inspection should scan exactly one chunk worth of cells; cells="
                        + runtime.lastChunkInspectionCellScanCount());

        runtime.cachedChunkCount();
        require(runtime.lastChunkInspectionScanCount() == 0
                        && runtime.lastChunkInspectionCellScanCount() == 0,
                "Changed chunk inspection should be cached again after one rescan");
    }

    private static void requireStreamerUpdateInvalidatesOldCleanGeneratedChunks() {
        int chunkSize = 16;
        int defaultView = EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE;
        EchoVoxelBlock oldBlock = new EchoVoxelBlock(
                "echotest:streamer_old_clean",
                "Streamer Old Clean",
                0xFF665847,
                true,
                true,
                0.8D
        );
        EchoVoxelBlock newBlock = new EchoVoxelBlock(
                "echotest:streamer_new_clean",
                "Streamer New Clean",
                0xFF64B9A0,
                true,
                true,
                0.8D
        );
        EchoVoxelChunkSource oldSource = streamerSwapSource(oldBlock, "old", chunkSize);
        EchoVoxelChunkSource newSource = streamerSwapSource(newBlock, "new", chunkSize);
        EchoVoxelWorldStreamer oldStreamer = new EchoVoxelWorldStreamer(oldSource, defaultView);
        EchoVoxelWorld world = new EchoVoxelWorld(
                "streamer-update-cache",
                19L,
                chunkSize,
                List.of(oldSource.generateChunk(19L, 0, 0, 0)),
                0.5D,
                2.0D,
                0.5D,
                0.0D
        );
        EchoClientWorldRuntime runtime = new EchoClientWorldRuntime(
                world,
                oldStreamer,
                EchoClientHazardCatalog.empty()
        );
        EchoVoxelPlayerState origin = playerAt(0.5D, 2.0D, 0.5D);
        runtime.streamAroundPlayer(origin, defaultView);
        EchoVoxelBlockState protectedState = EchoVoxelBlockState.of(oldBlock)
                .withProperty("streamerSwap", "protected")
                .withProperty("blockEntityId", "streamer_swap_smoke");
        require(runtime.setBlockStateAt(1, 1, 1, protectedState),
                "Streamer update smoke should place a protected origin block");

        EchoVoxelPlayerState far = playerAt(96.5D, 2.0D, 96.5D);
        runtime.streamAroundPlayer(far, defaultView);
        require(runtime.cachedChunkCount() > runtime.world().loadedChunkCount(),
                "Streamer update smoke should have cached unloaded chunks before swapping generators");

        runtime.updateStreamer(new EchoVoxelWorldStreamer(newSource, defaultView));
        EchoClientWorldStreamResult refresh = runtime.streamAroundPlayer(far, defaultView);
        EchoVoxelBlockState farState = runtime.world().blockStateAt(96, 0, 96);
        require(refresh.renderRegionChanged(),
                "Streamer update should invalidate the stream cursor for a same-position render refresh");
        require(farState.block().id().equals(newBlock.id())
                        && farState.property("streamerSwap").orElse("").equals("new"),
                "Active clean chunks should regenerate from the replacement streamer");
        require(runtime.cachedProtectedChunkCount() == 1,
                "Streamer update should keep only the protected cached chunk; protected="
                        + runtime.cachedProtectedChunkCount());
        require(runtime.cachedChunkCount() == runtime.world().loadedChunkCount() + 1,
                "Streamer update should drop stale clean cached chunks while keeping protected chunks; cached="
                        + runtime.cachedChunkCount()
                        + " active=" + runtime.world().loadedChunkCount());

        runtime.streamAroundPlayer(origin, defaultView);
        EchoVoxelBlockState preserved = runtime.world().blockStateAt(1, 1, 1);
        EchoVoxelBlockState regeneratedNeighbor = runtime.world().blockStateAt(16, 0, 0);
        require(preserved.property("blockEntityId").orElse("").equals("streamer_swap_smoke"),
                "Protected cached chunk should survive streamer cache invalidation");
        require(regeneratedNeighbor.block().id().equals(newBlock.id())
                        && regeneratedNeighbor.property("streamerSwap").orElse("").equals("new"),
                "Dropped old clean cached chunks should be regenerated by the replacement streamer on reload");
    }

    private static EchoVoxelChunkSource streamerSwapSource(
            EchoVoxelBlock block,
            String label,
            int chunkSize
    ) {
        return (seed, chunkX, chunkY, chunkZ) -> {
            EchoVoxelChunk chunk = new EchoVoxelChunk(new EchoVoxelChunkId(chunkX, chunkY, chunkZ), chunkSize);
            EchoVoxelBlockState state = EchoVoxelBlockState.of(block)
                    .withProperty("streamerSwap", label);
            for (int z = 0; z < chunkSize; z++) {
                for (int x = 0; x < chunkSize; x++) {
                    chunk.setStateLocal(x, 0, z, state);
                }
            }
            return chunk;
        };
    }

    private static EchoVoxelPlayerState playerAt(double x, double y, double z) {
        return new EchoVoxelPlayerState(
                x,
                y,
                z,
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

    private static void requireRuntimeWorldgenHotImportStreamsNewChunks() {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices(
                EchoClientSaveSlotService.open(Path.of("build", "tmp", "client-worldgen-streaming-smoke").toAbsolutePath())
        );
        services.startNewWorld("runtime-worldgen-stream");
        EchoClientGameSession session = services.session();
        require(session != null, "Runtime worldgen smoke requires a live session");
        int cachedBefore = session.cachedChunkCount();
        RuntimeWorldgenIds ids = importRuntimeWorldgenRows(services);
        require(services.session() == session,
                "Runtime worldgen hot-import should refresh the active session in place");

        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        movePlayer(session, gameplay, 96.5D, session.player().state().y(), 96.5D);
        EchoClientWorldStreamResult stream = services.streamAroundPlayer(EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE);
        require(stream.loadedChunksChanged(),
                "Runtime worldgen smoke should stream a previously unloaded chunk after hot-import");
        require(session.cachedChunkCount() > cachedBefore,
                "Runtime worldgen smoke should cache newly streamed chunks");

        EchoVoxelBlockState structureBase = session.world().blockStateAt(96, 7, 96);
        EchoVoxelBlockState structureTop = session.world().blockStateAt(96, 8, 96);
        require(structureBase.block().id().equals(ids.blockId())
                        && structureTop.block().id().equals(ids.blockId()),
                "Hot-imported native structure row should place its configured block in streamed chunks");
        require(structureBase.property("source").orElse("").equals("runtime_structure"),
                "Hot-imported native structure block should be tagged as runtime_structure");
        require(structureBase.property("structure").orElse("").equals(ids.structureRuntimeId()),
                "Hot-imported native structure block should preserve the structure runtime id");

        EchoVoxelBlockState featureBase = session.world().blockStateAt(104, 9, 96);
        EchoVoxelBlockState featureEdge = session.world().blockStateAt(105, 9, 97);
        require(featureBase.block().id().equals(ids.blockId())
                        && featureEdge.block().id().equals(ids.blockId()),
                "Hot-imported native feature row should place its configured feature blocks in streamed chunks");
        require(featureBase.property("source").orElse("").equals("runtime_feature"),
                "Hot-imported native feature block should be tagged as runtime_feature");
        require(featureBase.property("feature").orElse("").equals(ids.featureRuntimeId()),
                "Hot-imported native feature block should preserve the feature runtime id");

        EchoVoxelBlockState regionMarker = session.world().blockStateAt(102, 6, 96);
        require(regionMarker.block().id().equals(ids.blockId()),
                "Hot-imported native world-region row should alter generated chunk block state");
        require(regionMarker.property("source").orElse("").equals("runtime_region"),
                "Hot-imported native world-region block should be tagged as runtime_region");
        require(regionMarker.property("region").orElse("").equals(ids.regionRuntimeId()),
                "Hot-imported native world-region block should preserve the region runtime id");

        EchoVoxelBiome runtimeBiome = session.world().biomeAt(102, 96);
        require(runtimeBiome.id().equals(ids.biomeRuntimeId()),
                "Hot-imported native biome row should update the active world biome source");
        require(runtimeBiome.hasTag("runtime_worldgen") && runtimeBiome.hasTag("toxic"),
                "Hot-imported native biome should preserve runtime and datapack tags");
        require(regionMarker.property("biome").orElse("").equals(ids.biomeRuntimeId()),
                "Hot-imported native biome should tag generated region blocks in streamed chunks");
        require(session.world().biomeIdAtBlock(102, 6, 96).orElse("").equals(ids.biomeRuntimeId()),
                "Hot-imported native biome should be visible through block biome metadata");
        require(EchoClientBiomeEnvironment.fromBiome(runtimeBiome).ambienceClipId().equals("echo:ambience_toxic_swamp"),
                "Hot-imported native biome tags should drive client ambience selection");
    }

    private static void requireDataWorldgenFeatureStreamsNewChunks() throws IOException {
        Path root = Path.of("build", "tmp", "client-data-worldgen-streaming-smoke").toAbsolutePath().normalize();
        deleteRecursively(root);
        Path workspaceRoot = root.resolve("Echo");
        Path standaloneRoot = workspaceRoot.resolve("echo-standalone-runtime");
        Path clientRoot = standaloneRoot.resolve("echo-runtime-client");
        Path packRoot = standaloneRoot.resolve("resourcepacks/data-worldgen-smoke");
        String blockId = EchoAdapterCoreStandaloneContentBridge.ashfallLive().runtimeMarkerBlock().id();
        String featureId = "smokeworldgen:data_runtime_marker";
        String structureId = "smokeworldgen:data_runtime_wall";
        String regionId = "smokeworldgen:data_toxic_surface";
        write(standaloneRoot.resolve("settings.gradle"), "rootProject.name = 'data-worldgen-smoke'\n");
        Files.createDirectories(clientRoot);
        Files.createDirectories(workspaceRoot.resolve("core"));
        write(packRoot.resolve("pack.mcmeta"), """
                {
                  "pack": {
                    "pack_format": 34,
                    "description": "Data worldgen streaming smoke"
                  }
                }
                """);
        write(packRoot.resolve("data/smokeworldgen/worldgen/configured_feature/data_runtime_marker.json"), """
                {
                  "type": "minecraft:simple_block",
                  "config": {
                    "to_place": {
                      "type": "minecraft:simple_state_provider",
                      "state": {
                        "Name": "%s"
                      }
                    }
                  }
                }
                """.formatted(blockId));
        write(packRoot.resolve("data/smokeworldgen/worldgen/biome/toxic_data_basin.json"), """
                {
                  "temperature": 1.1,
                  "downfall": 0.75,
                  "centerX": 112,
                  "centerZ": 112,
                  "radius": 7,
                  "tags": ["toxic", "runtime_smoke"],
                  "effects": {
                    "fog_color": 2174020,
                    "grass_color": 4259960,
                    "ambient_particle": {
                      "options": {
                        "type": "minecraft:spore_blossom_air"
                      }
                    }
                  }
                }
                """);
        write(packRoot.resolve("data/smokeworldgen/worldgen/placed_feature/data_runtime_marker.json"), """
                {
                  "feature": "%s",
                  "featureBlockId": "%s",
                  "x": 112,
                  "y": 9,
                  "z": 112,
                  "width": 2,
                  "depth": 2,
                  "shape": "PLATFORM",
                  "placement": [
                    {
                      "type": "minecraft:count",
                      "count": 1
                    }
                  ]
                }
                """.formatted(featureId, blockId));
        write(packRoot.resolve("data/smokeworldgen/worldgen/structure/data_runtime_wall.json"), """
                {
                  "type": "minecraft:jigsaw",
                  "start_pool": "smokeworldgen:data_runtime_wall",
                  "step": "surface_structures",
                  "structureBlockId": "%s",
                  "x": 116,
                  "y": 9,
                  "z": 112,
                  "width": 2,
                  "height": 2,
                  "depth": 1,
                  "shape": "WALL"
                }
                """.formatted(blockId));
        write(packRoot.resolve("data/smokeworldgen/echoworldcore/world_regions/data_toxic_surface.json"), """
                {
                  "id": "%s",
                  "type": "toxic_surface",
                  "displayName": "Data Toxic Surface",
                  "summary": "Streaming smoke WorldCore region with explicit standalone surface hints.",
                  "biomeIds": ["smokeworldgen:toxic_data_basin"],
                  "hazardIds": ["smokeworldgen:hazard/toxic_spores"],
                  "radius": 2,
                  "surfaceBlockId": "%s",
                  "centerX": 119,
                  "centerZ": 112,
                  "fixedY": 8
                }
                """.formatted(regionId, blockId));
        write(packRoot.resolve("data/smokeworldgen/echoworldcore/world_hazards/hazard/toxic_spores.json"), """
                {
                  "id": "smokeworldgen:hazard/toxic_spores",
                  "type": "toxic_air",
                  "displayName": "Toxic Spores",
                  "summary": "Smoke-test spores linked from a WorldCore region.",
                  "defaultSeverity": 58,
                  "ticking": true
                }
                """);

        EchoClientResourcePackService resourcePacks = new EchoClientResourcePackService(List.of(clientRoot));
        EchoClientRuntimeServices services = new EchoClientRuntimeServices(
                EchoClientSaveSlotService.open(root.resolve("saves")),
                resourcePacks
        );
        require(resourcePacks.resourcePacks().stream().anyMatch(pack -> pack.id().equals("data-worldgen-smoke")),
                "Data worldgen smoke pack should be mounted by the resource pack service");
        require(services.loadedDataWorldgenStructureRowCount() >= 1,
                "Mounted data structure should bridge into live worldgen structure rows; rows="
                        + services.loadedDataWorldgenStructureRowCount()
                        + " error=" + services.dataWorldgenStructureError());
        require(services.loadedDataWorldgenFeatureRowCount() >= 1,
                "Mounted data placed_feature should bridge into live worldgen feature rows; rows="
                        + services.loadedDataWorldgenFeatureRowCount()
                        + " error=" + services.dataWorldgenFeatureError()
                        + " packs=" + resourcePacks.resourcePacks().size()
                        + " assetEntries=" + (resourcePacks.assets() == null
                        ? -1
                        : resourcePacks.assets().index().entries().size()));
        require(services.loadedDataWorldgenBiomeRowCount() >= 1,
                "Mounted data biome should bridge into live worldgen biome rows; rows="
                        + services.loadedDataWorldgenBiomeRowCount()
                        + " error=" + services.dataWorldgenBiomeError());
        require(services.loadedDataWorldCoreRegionRowCount() >= 1,
                "Mounted WorldCore region should bridge into live world-region rows; rows="
                        + services.loadedDataWorldCoreRegionRowCount()
                        + " error=" + services.dataWorldCoreRegionError());
        require(services.dataWorldgenStructureError().isBlank(),
                "Mounted data worldgen structure bridge should not report an error");
        require(services.dataWorldgenFeatureError().isBlank(),
                "Mounted data worldgen feature bridge should not report an error");
        require(services.dataWorldgenBiomeError().isBlank(),
                "Mounted data worldgen biome bridge should not report an error");
        require(services.dataWorldCoreRegionError().isBlank(),
                "Mounted WorldCore region bridge should not report an error");
        services.startNewWorld("data-worldgen-stream");
        EchoClientGameSession session = services.session();
        require(session != null, "Data worldgen feature smoke requires a live session");

        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        movePlayer(session, gameplay, 112.5D, session.player().state().y(), 112.5D);
        EchoClientWorldStreamResult stream = services.streamAroundPlayer(EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE);
        require(stream.loadedChunksChanged(),
                "Data worldgen feature smoke should stream the feature chunk after moving to it");

        EchoVoxelBlockState featureBase = session.world().blockStateAt(112, 9, 112);
        EchoVoxelBlockState featureEdge = session.world().blockStateAt(113, 9, 113);
        require(featureBase.block().id().equals(blockId) && featureEdge.block().id().equals(blockId),
                "Data placed_feature should place its configured block in streamed chunks");
        require(featureBase.property("source").orElse("").equals("runtime_feature"),
                "Data placed_feature block should be tagged as runtime_feature");
        require(featureBase.property("feature").orElse("").equals(featureId),
                "Data placed_feature block should preserve the placed feature id");
        EchoVoxelBlockState structureBase = session.world().blockStateAt(116, 9, 112);
        EchoVoxelBlockState structureTop = session.world().blockStateAt(117, 10, 112);
        require(structureBase.block().id().equals(blockId) && structureTop.block().id().equals(blockId),
                "Data worldgen structure should place its hinted block in streamed chunks");
        require(structureBase.property("source").orElse("").equals("runtime_structure"),
                "Data worldgen structure block should be tagged as runtime_structure");
        require(structureBase.property("structure").orElse("").equals(structureId),
                "Data worldgen structure block should preserve the structure id");
        EchoVoxelBlockState regionMarker = session.world().blockStateAt(119, 8, 112);
        require(regionMarker.block().id().equals(blockId),
                "Data WorldCore region should place its hinted surface block in streamed chunks");
        require(regionMarker.property("source").orElse("").equals("runtime_region"),
                "Data WorldCore region block should be tagged as runtime_region");
        require(regionMarker.property("region").orElse("").equals(regionId),
                "Data WorldCore region block should preserve the region id");
        EchoVoxelBiome dataBiome = session.world().biomeAt(112, 112);
        require(dataBiome.id().equals("smokeworldgen:toxic_data_basin"),
                "Data worldgen biome should update the active world biome source");
        require(dataBiome.hasTag("runtime_worldgen") && dataBiome.hasTag("toxic"),
                "Data worldgen biome should preserve runtime and datapack tags");
        require(featureBase.property("biome").orElse("").equals("smokeworldgen:toxic_data_basin"),
                "Data worldgen biome should tag generated feature blocks in streamed chunks");
        require(structureBase.property("biome").orElse("").equals("smokeworldgen:toxic_data_basin"),
                "Data worldgen biome should tag generated structure blocks in streamed chunks");
        require(regionMarker.property("biome").orElse("").equals("smokeworldgen:toxic_data_basin"),
                "Data worldgen biome should tag generated WorldCore region blocks in streamed chunks");
        require(EchoClientBiomeEnvironment.fromBiome(dataBiome).ambienceClipId().equals("echo:ambience_toxic_swamp"),
                "Data worldgen biome tags should drive client ambience selection");
    }

    private static RuntimeWorldgenIds importRuntimeWorldgenRows(EchoClientRuntimeServices services) {
        RuntimeWorldgenIds ids = new RuntimeWorldgenIds(
                "echoruntimehost:streaming_worldgen_glass",
                "echoruntimehost:streaming_runtime_tower",
                "echoruntimehost:streaming_runtime_feature",
                "echoruntimehost:streaming_runtime_region",
                "echoruntimehost:streaming_runtime_biome"
        );
        int imported = services.importAdapterCoreContentRegistrations(List.of(
                Map.of(
                        "moduleId", "echoruntimehost",
                        "contentId", "echoruntimehost:block/streaming_worldgen_glass",
                        "contentKind", "BLOCK",
                        "domain", "blocks",
                        "displayName", "Streaming Worldgen Glass",
                        "adapterKey", "registry.blocks.streaming_worldgen_glass",
                        "neoForgeId", ids.blockId(),
                        "nativeLoaderId", "echoruntimehost:block/streaming_worldgen_glass",
                        "standaloneRuntimeId", ids.blockId(),
                        "metadata", Map.of(
                                "liveVoxelId", ids.blockId(),
                                "argb", "#98E8D1",
                                "detailArgb", "0xFFD9FFF5",
                                "atlasKey", "echoruntimehost/block/streaming_worldgen_glass",
                                "materialPattern", "TERMINAL_GRID",
                                "solid", false,
                                "opaque", false,
                                "hardness", "0.25"
                        )
                ),
                Map.of(
                        "moduleId", "echoruntimehost",
                        "contentId", "echoruntimehost:structure/streaming_runtime_tower",
                        "contentKind", "STRUCTURE",
                        "domain", "structures",
                        "displayName", "Streaming Runtime Tower",
                        "adapterKey", "registry.structures.streaming_runtime_tower",
                        "neoForgeId", ids.structureRuntimeId(),
                        "nativeLoaderId", "echoruntimehost:structure/streaming_runtime_tower",
                        "standaloneRuntimeId", ids.structureRuntimeId(),
                        "metadata", Map.of(
                                "placementBlockId", ids.blockId(),
                                "shape", "PILLAR",
                                "x", 96,
                                "y", 7,
                                "z", 96,
                                "height", 2
                        )
                ),
                Map.of(
                        "moduleId", "echoruntimehost",
                        "contentId", "echoruntimehost:feature/streaming_runtime_feature",
                        "contentKind", "FEATURE",
                        "domain", "features",
                        "displayName", "Streaming Runtime Feature",
                        "adapterKey", "registry.features.streaming_runtime_feature",
                        "neoForgeId", ids.featureRuntimeId(),
                        "nativeLoaderId", "echoruntimehost:feature/streaming_runtime_feature",
                        "standaloneRuntimeId", ids.featureRuntimeId(),
                        "metadata", Map.of(
                                "featureBlockId", ids.blockId(),
                                "shape", "PLATFORM",
                                "x", 104,
                                "y", 9,
                                "z", 96,
                                "width", 2,
                                "depth", 2
                        )
                ),
                Map.of(
                        "moduleId", "echoruntimehost",
                        "contentId", "echoruntimehost:world_region/streaming_runtime_region",
                        "contentKind", "WORLD_REGION",
                        "domain", "world_regions",
                        "displayName", "Streaming Runtime Region",
                        "adapterKey", "registry.world_regions.streaming_runtime_region",
                        "neoForgeId", ids.regionRuntimeId(),
                        "nativeLoaderId", "echoruntimehost:world_region/streaming_runtime_region",
                        "standaloneRuntimeId", ids.regionRuntimeId(),
                        "metadata", Map.of(
                                "surfaceBlockId", ids.blockId(),
                                "centerX", 102,
                                "centerZ", 96,
                                "radius", 4,
                                "fixedY", 6
                        )
                ),
                Map.of(
                        "moduleId", "echoruntimehost",
                        "contentId", "echoruntimehost:worldgen/streaming_runtime_biome",
                        "contentKind", "WORLDGEN_DEFINITION",
                        "domain", "worldgen",
                        "displayName", "Streaming Runtime Biome",
                        "adapterKey", "registry.worldgen.streaming_runtime_biome",
                        "neoForgeId", ids.biomeRuntimeId(),
                        "nativeLoaderId", "echoruntimehost:worldgen/streaming_runtime_biome",
                        "standaloneRuntimeId", ids.biomeRuntimeId(),
                        "metadata", Map.ofEntries(
                                Map.entry("worldgenType", "BIOME"),
                                Map.entry("biomeId", ids.biomeRuntimeId()),
                                Map.entry("centerX", 102),
                                Map.entry("centerZ", 96),
                                Map.entry("radius", 6),
                                Map.entry("temperature", "1.15"),
                                Map.entry("downfall", "0.80"),
                                Map.entry("fogColor", "#214C44"),
                                Map.entry("grassColor", "#40E078"),
                                Map.entry("ambientParticle", "minecraft:spore_blossom_air"),
                                Map.entry("tags", List.of("toxic", "runtime_smoke"))
                        )
                )
        ));
        require(imported == 5,
                "Runtime worldgen smoke should import block, structure, feature, world-region, and biome rows");
        return ids;
    }

    private record RuntimeWorldgenIds(
            String blockId,
            String structureRuntimeId,
            String featureRuntimeId,
            String regionRuntimeId,
            String biomeRuntimeId
    ) {
    }

    private static double adjacentChunkCenterZ(EchoVoxelPlayerState origin, int chunkSize) {
        int safeChunkSize = Math.max(1, chunkSize);
        int originChunkZ = Math.floorDiv((int) Math.floor(origin.z()), safeChunkSize);
        return (originChunkZ + 1) * (double) safeChunkSize + 0.5D;
    }

    private static void movePlayer(
            EchoClientGameSession session,
            EchoClientGameplay gameplay,
            double x,
            double y,
            double z
    ) {
        EchoVoxelPlayerState current = session.player().state();
        EchoVoxelPlayerController moved = new EchoVoxelPlayerController(new EchoVoxelPlayerState(
                x,
                y,
                z,
                current.velocityY(),
                current.yawDegrees(),
                current.pitchDegrees(),
                current.grounded(),
                current.crouching(),
                current.sprinting(),
                current.selectedSlot(),
                current.reach()
        ));
        gameplay.init(session.world(), moved, session.hotbar());
        session.updateFromGameplay(gameplay);
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
            for (Path path : stream.sorted(java.util.Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void requireClose(double actual, double expected, String label) {
        if (Math.abs(actual - expected) > 0.0001D) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }
}
