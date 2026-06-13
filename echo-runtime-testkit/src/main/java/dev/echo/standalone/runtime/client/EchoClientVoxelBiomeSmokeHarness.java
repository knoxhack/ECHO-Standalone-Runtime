package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.world.EchoVoxelBiome;
import dev.echo.standalone.runtime.world.EchoVoxelAshfallBiomes;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackAction;
import dev.echo.standalone.runtime.audio.EchoRecordingAudioBackend;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.render.EchoVoxelChunkMesher;
import dev.echo.standalone.runtime.render.EchoVoxelMeshFace;
import dev.echo.standalone.runtime.render.EchoVoxelMeshMaterial;
import dev.echo.standalone.runtime.render.EchoVoxelRenderPacket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class EchoClientVoxelBiomeSmokeHarness {
    private static final Path REPORT_PATH = Path.of("reports/echo/standalone/client-voxel-biome-rendering.json");

    private EchoClientVoxelBiomeSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("42").gameSession();
        EchoVoxelWorld world = session.world();
        EchoVoxelBiome spawnBiome = world.biomeAt(world.spawnX(), world.spawnZ());
        require(spawnBiome.id().equals(EchoVoxelAshfallBiomes.CRASH_ZONE_WASTELAND.id()),
                "Spawn should resolve to the crash-zone wasteland biome");

        Set<String> sampledBiomeIds = new HashSet<>();
        for (int z = -160; z <= 160; z += 16) {
            for (int x = -160; x <= 160; x += 16) {
                sampledBiomeIds.add(world.biomeAt(x, z).id());
            }
        }
        require(sampledBiomeIds.size() >= 4,
                "Ashfall biome resolver should expose multiple deterministic biome regions");
        require(sampledBiomeIds.contains(EchoVoxelAshfallBiomes.TOXIC_SWAMP.id())
                        || sampledBiomeIds.contains(EchoVoxelAshfallBiomes.RADIATION_ZONE.id()),
                "Ashfall biome resolver should include hazardous biome regions");

        BlockSample spawnSample = firstNonAirState(
                world,
                (int) Math.floor(world.spawnX()),
                (int) Math.floor(world.spawnZ())
        );
        EchoVoxelBlockState spawnState = spawnSample.state();
        require(spawnState.property("biome").orElse("").equals(spawnBiome.id()),
                "Generated voxel block state should carry biome metadata");
        require(world.biomeIdAtBlock(
                        (int) Math.floor(world.spawnX()),
                        spawnSample.y(),
                        (int) Math.floor(world.spawnZ())
                ).orElse("").equals(spawnBiome.id()),
                "Generated voxel chunk should expose a typed biome id for block cells");

        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(world, session.player(), session.hotbar());
        String debug = EchoClientDebugOverlay.text(
                60,
                EchoClientGameState.IN_GAME,
                EchoClientScreenKind.MAIN_MENU,
                session,
                gameplay
        );
        require(debug.contains("BIOME " + spawnBiome.id()),
                "Client debug overlay should surface the current voxel biome");
        require(debug.contains("ENV echo:ambience_ash_wasteland FOG"),
                "Client debug overlay should surface biome ambience and fog profile");
        BiomeEnvironmentEvidence environment = requireBiomeEnvironmentAndAmbienceSwitch(spawnBiome);
        BiomeEnvironmentCacheEvidence environmentCache = requireBiomeEnvironmentCache(spawnBiome);
        BiomeRenderEvidence render = requireRenderPacketBiomeTint(world, spawnBiome);
        AtlasSignatureEvidence atlasSignature = requireAtlasSourceSignatureTracksMaterialInputs(world, spawnSample);
        AtlasCacheEvidence atlasCache = requireAtlasSourceCacheSkipsUnchangedChunks(world, spawnSample);
        AtlasMetadataEvidence atlasMetadata = requireAtlasSourceIgnoresRuntimeOnlyMachineMetadata(world, spawnSample);

        writeReport(
                spawnBiome,
                sampledBiomeIds,
                spawnSample,
                debug,
                environment,
                environmentCache,
                render,
                atlasSignature,
                atlasCache,
                atlasMetadata
        );

        System.out.println("client voxel biome smoke PASS spawn="
                + spawnBiome.id()
                + " sampled=" + sampledBiomeIds.size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static BiomeEnvironmentEvidence requireBiomeEnvironmentAndAmbienceSwitch(EchoVoxelBiome spawnBiome) {
        EchoClientBiomeEnvironment spawn = EchoClientBiomeEnvironment.fromBiome(spawnBiome);
        EchoClientBiomeEnvironment toxic = EchoClientBiomeEnvironment.fromBiome(EchoVoxelAshfallBiomes.TOXIC_SWAMP);
        require(spawn.ambienceClipId().equals("echo:ambience_ash_wasteland"),
                "Crash-zone biome should use ash wasteland ambience");
        require(toxic.ambienceClipId().equals("echo:ambience_toxic_swamp"),
                "Toxic swamp biome should use toxic ambience");
        require(toxic.fogDensity() > spawn.fogDensity(),
                "Toxic swamp fog should be denser than crash-zone fog");
        require(!toxic.fogDebugText().equals(spawn.fogDebugText()),
                "Different biomes should produce different fog debug profiles");

        EchoRecordingAudioBackend backend = new EchoRecordingAudioBackend();
        EchoClientAudio audio = new EchoClientAudio();
        audio.init(backend);
        require(audio.applyBiomeEnvironment(spawn, 1L),
                "First biome environment should start an ambience loop");
        require(!audio.applyBiomeEnvironment(spawn, 2L),
                "Applying the same biome environment should not replay ambience");
        require(audio.applyBiomeEnvironment(toxic, 3L),
                "Changing biome environment should switch ambience loops");
        require(audio.currentAmbienceClipId().equals(toxic.ambienceClipId()),
                "Client audio should remember the active biome ambience clip");
        require(backend.events().size() == 3,
                "Biome ambience switch should record loop stop loop events");
        require(backend.events().get(0).action() == EchoAudioPlaybackAction.LOOP
                        && backend.events().get(0).clip().clipId().equals(spawn.ambienceClipId()),
                "First ambience event should loop the spawn biome clip");
        require(backend.events().get(1).action() == EchoAudioPlaybackAction.STOP,
                "Biome ambience switch should stop the previous clip");
        require(backend.events().get(2).action() == EchoAudioPlaybackAction.LOOP
                        && backend.events().get(2).clip().clipId().equals(toxic.ambienceClipId()),
                "Biome ambience switch should loop the new biome clip");
        return new BiomeEnvironmentEvidence(
                spawn.ambienceClipId(),
                toxic.ambienceClipId(),
                spawn.fogDensity(),
                toxic.fogDensity(),
                backend.events().size()
        );
    }

    private static BiomeEnvironmentCacheEvidence requireBiomeEnvironmentCache(EchoVoxelBiome spawnBiome) {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientWorldSessionController worldSessions = new EchoClientWorldSessionController(services, screens);
        EchoClientRenderRuntimeController renderRuntime = new EchoClientRenderRuntimeController(
                new EchoGlfwWindow("biome-cache-smoke", 320, 240),
                services,
                screens,
                new EchoClientGameplayRuntimeController(services, screens, worldSessions),
                new EchoClientSlotGridController(services, screens)
        );

        EchoClientBiomeEnvironment first = renderRuntime.resolveBiomeEnvironment(spawnBiome);
        EchoClientBiomeEnvironment second = renderRuntime.resolveBiomeEnvironment(spawnBiome);
        require(first == second, "Render runtime should reuse the active biome environment instance");
        require(renderRuntime.biomeEnvironmentBuildCount() == 1,
                "Render runtime should build one biome environment for repeated biome frames");
        require(renderRuntime.biomeEnvironmentCacheHitCount() == 1,
                "Render runtime should count repeated biome frames as cache hits");

        EchoVoxelBiome equalBiome = new EchoVoxelBiome(
                spawnBiome.id(),
                spawnBiome.displayName(),
                spawnBiome.temperature(),
                spawnBiome.downfall(),
                spawnBiome.fogColor(),
                spawnBiome.grassColor(),
                spawnBiome.ambientParticle(),
                spawnBiome.tags()
        );
        require(renderRuntime.resolveBiomeEnvironment(equalBiome) == first,
                "Render runtime should reuse equal immutable biome definitions");
        require(renderRuntime.biomeEnvironmentBuildCount() == 1,
                "Equal biome definitions should not rebuild the render environment");

        EchoVoxelBiome changedFog = new EchoVoxelBiome(
                spawnBiome.id(),
                spawnBiome.displayName(),
                spawnBiome.temperature(),
                spawnBiome.downfall(),
                spawnBiome.fogColor() ^ 0x000101,
                spawnBiome.grassColor(),
                spawnBiome.ambientParticle(),
                spawnBiome.tags()
        );
        EchoClientBiomeEnvironment changed = renderRuntime.resolveBiomeEnvironment(changedFog);
        require(changed != first, "Changed biome definition should rebuild the render environment");
        require(renderRuntime.biomeEnvironmentBuildCount() == 2,
                "Changed biome definition should increment the render environment build count");
        return new BiomeEnvironmentCacheEvidence(
                renderRuntime.biomeEnvironmentBuildCount(),
                renderRuntime.biomeEnvironmentCacheHitCount()
        );
    }

    private static BiomeRenderEvidence requireRenderPacketBiomeTint(EchoVoxelWorld world, EchoVoxelBiome spawnBiome) {
        EchoVoxelRenderPacket packet = new EchoVoxelChunkMesher().buildPacket(
                world,
                new EchoVoxelCamera(world.spawnX(), world.spawnY() + 18.0D, world.spawnZ(), 35.0D, -18.0D, 70.0D),
                512.0D
        );
        EchoVoxelMeshFace tintedFace = firstBiomeTintedFace(packet);
        require(tintedFace != null,
                "Generated chunk render packet should include at least one biome-tinted face");

        EchoVoxelBiome faceBiome = world.biomeAt(tintedFace.x(), tintedFace.z());
        EchoVoxelBlockState faceState = world.blockStateAt(tintedFace.x(), tintedFace.y(), tintedFace.z());
        require(faceState.property("biome").orElse("").equals(faceBiome.id()),
                "Biome-tinted face should come from a block state with generated biome metadata");
        require(tintedFace.material().stateProperties().equals(renderRelevantProperties(faceState.properties())),
                "Generated chunk render packet should preserve render-relevant block-state properties on mesh materials");
        require(world.biomeIdAtBlock(tintedFace.x(), tintedFace.y(), tintedFace.z()).orElse("")
                        .equals(tintedFace.material().biomeId()),
                "Biome-tinted face material should match the chunk biome id");
        require(tintedFace.material().biomeId().equals(faceBiome.id()),
                "Biome-tinted face material should carry the resolved biome id");
        require(tintedFace.material().biomeTintArgb() == opaqueArgb(faceBiome.grassColor()),
                "Biome-tinted face material should carry the biome grass tint color");
        require(tintedFace.material().argb() != faceState.block().argb(),
                "Biome-tinted face material color should differ from the untinted block color");

        EchoVoxelMeshMaterial spawnMaterial = EchoVoxelMeshMaterial.fromBlock(faceState.block(), spawnBiome);
        EchoVoxelMeshMaterial toxicMaterial =
                EchoVoxelMeshMaterial.fromBlock(faceState.block(), EchoVoxelAshfallBiomes.TOXIC_SWAMP);
        require(spawnMaterial.biomeTinted() && toxicMaterial.biomeTinted(),
                "Organic render materials should opt into biome tinting");
        require(spawnMaterial.argb() != toxicMaterial.argb(),
                "Different biomes should produce different render tint colors for the same organic block");
        return new BiomeRenderEvidence(
                packet.chunkMeshes().size(),
                faceBiome.id(),
                tintedFace.material().biomeId(),
                tintedFace.material().biomeTintArgb(),
                tintedFace.material().argb(),
                faceState.block().argb()
        );
    }

    private static AtlasSignatureEvidence requireAtlasSourceSignatureTracksMaterialInputs(EchoVoxelWorld world, BlockSample sample) {
        int before = EchoClientRenderer.atlasSourceSignature(world);
        EchoVoxelBlock signatureBlock = new EchoVoxelBlock(
                "echotest:atlas_signature_marker",
                "Atlas Signature Marker",
                0xFF55CCEE,
                true,
                true,
                0.25D
        );
        boolean changed = world.setBlockStateAt(
                (int) Math.floor(world.spawnX()),
                sample.y(),
                (int) Math.floor(world.spawnZ()),
                EchoVoxelBlockState.of(signatureBlock)
        );
        require(changed, "Atlas signature smoke should replace one loaded block state");
        int after = EchoClientRenderer.atlasSourceSignature(world);
        require(before != after,
                "Atlas source signature should change when streamed block material inputs change");
        return new AtlasSignatureEvidence(before, after);
    }

    private static AtlasCacheEvidence requireAtlasSourceCacheSkipsUnchangedChunks(EchoVoxelWorld world, BlockSample sample) {
        EchoClientRenderer.AtlasSourceCache cache = new EchoClientRenderer.AtlasSourceCache();
        int first = cache.sourceSignature(world);
        int expectedCellsPerChunk = world.chunkSize() * world.chunkSize() * world.chunkSize();
        require(cache.lastChunkScanCount() == world.loadedChunkCount(),
                "Initial atlas source cache pass should scan every loaded chunk");
        require(cache.lastCellScanCount() == world.loadedChunkCount() * expectedCellsPerChunk,
                "Initial atlas source cache pass should scan loaded chunk cells once");

        int repeat = cache.sourceSignature(world);
        require(repeat == first,
                "Atlas source cache should keep the same signature for unchanged loaded chunks");
        require(cache.lastChunkScanCount() == 0 && cache.lastCellScanCount() == 0,
                "Atlas source cache should skip chunk and cell scans for unchanged loaded chunks");

        EchoVoxelBlock cacheBlock = new EchoVoxelBlock(
                "echotest:atlas_cache_marker",
                "Atlas Cache Marker",
                0xFFDD8855,
                true,
                true,
                0.25D
        );
        boolean changed = world.setBlockStateAt(
                (int) Math.floor(world.spawnX()),
                sample.y(),
                (int) Math.floor(world.spawnZ()),
                EchoVoxelBlockState.of(cacheBlock)
        );
        require(changed, "Atlas source cache smoke should mutate one loaded block");
        int changedSignature = cache.sourceSignature(world);
        require(changedSignature != repeat,
                "Atlas source cache signature should change when one chunk's material inputs change");
        require(cache.lastChunkScanCount() == 1,
                "Atlas source cache should rescan only the mutated chunk");
        require(cache.lastCellScanCount() == expectedCellsPerChunk,
                "Atlas source cache should rescan one chunk's cells after one block mutation");
        int mutatedChunkScans = cache.lastChunkScanCount();
        int mutatedCellScans = cache.lastCellScanCount();

        int changedRepeat = cache.sourceSignature(world);
        require(changedRepeat == changedSignature,
                "Atlas source cache should stabilize after the mutated chunk is cached");
        require(cache.lastChunkScanCount() == 0 && cache.lastCellScanCount() == 0,
                "Atlas source cache should skip scans again after caching the mutation");
        return new AtlasCacheEvidence(
                world.loadedChunkCount(),
                expectedCellsPerChunk,
                mutatedChunkScans,
                mutatedCellScans
        );
    }

    private static AtlasMetadataEvidence requireAtlasSourceIgnoresRuntimeOnlyMachineMetadata(EchoVoxelWorld world, BlockSample sample) {
        EchoVoxelBlock machineBlock = new EchoVoxelBlock(
                "echotest:render_metadata_machine",
                "Render Metadata Machine",
                0xFF77AA99,
                true,
                true,
                1.0D
        );
        EchoVoxelBlockState first = EchoVoxelBlockState.of(machineBlock)
                .withProperty("facing", "north")
                .withProperty("source", "machine_block_entity")
                .withProperty("blockEntityId", "scrap_press_a")
                .withProperty("canonicalId", "scrap_press")
                .withProperty("machineKind", "CRAFTING")
                .withProperty("recipeProgressTicks", "10");
        boolean firstChanged = world.setBlockStateAt(
                (int) Math.floor(world.spawnX()),
                sample.y(),
                (int) Math.floor(world.spawnZ()),
                first
        );
        require(firstChanged, "Atlas metadata smoke should place the first machine block state");
        EchoVoxelMeshMaterial firstMaterial =
                EchoVoxelMeshMaterial.fromBlockState(first, world.biomeAt(world.spawnX(), world.spawnZ()));
        require(firstMaterial.stateProperties().equals(java.util.Map.of("facing", "north")),
                "Render materials should keep model-relevant state and strip runtime-only machine metadata");
        int firstSignature = EchoClientRenderer.atlasSourceSignature(world);

        EchoVoxelBlockState second = EchoVoxelBlockState.of(machineBlock)
                .withProperty("facing", "north")
                .withProperty("source", "machine_block_entity")
                .withProperty("blockEntityId", "scrap_press_b")
                .withProperty("canonicalId", "scrap_press")
                .withProperty("machineKind", "PROCESSING")
                .withProperty("recipeProgressTicks", "240");
        boolean secondChanged = world.setBlockStateAt(
                (int) Math.floor(world.spawnX()),
                sample.y(),
                (int) Math.floor(world.spawnZ()),
                second
        );
        require(secondChanged, "Atlas metadata smoke should place the second machine block state");
        int secondSignature = EchoClientRenderer.atlasSourceSignature(world);
        require(secondSignature == firstSignature,
                "Atlas source signature should ignore runtime-only machine metadata changes");

        EchoVoxelBlockState third = second.withProperty("facing", "east");
        boolean thirdChanged = world.setBlockStateAt(
                (int) Math.floor(world.spawnX()),
                sample.y(),
                (int) Math.floor(world.spawnZ()),
                third
        );
        require(thirdChanged, "Atlas metadata smoke should place the model-state variant");
        int thirdSignature = EchoClientRenderer.atlasSourceSignature(world);
        require(thirdSignature != secondSignature,
                "Atlas source signature should still track model-relevant state changes");
        return new AtlasMetadataEvidence(firstSignature, secondSignature, thirdSignature);
    }

    private static EchoVoxelMeshFace firstBiomeTintedFace(EchoVoxelRenderPacket packet) {
        for (var mesh : packet.chunkMeshes()) {
            for (EchoVoxelMeshFace face : mesh.faces()) {
                if (face.material().biomeTinted()) {
                    return face;
                }
            }
        }
        return null;
    }

    private static int opaqueArgb(int rgbOrArgb) {
        int alpha = (rgbOrArgb >>> 24) & 0xFF;
        return (alpha == 0 ? 0xFF000000 : (alpha << 24)) | (rgbOrArgb & 0x00FFFFFF);
    }

    private static java.util.Map<String, String> renderRelevantProperties(java.util.Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return java.util.Map.of();
        }
        java.util.TreeMap<String, String> result = new java.util.TreeMap<>();
        for (var entry : properties.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()
                    || entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            String key = entry.getKey().trim();
            if ("blockEntityId".equals(key)
                    || "canonicalId".equals(key)
                    || "machineKind".equals(key)
                    || "recipeProgressTicks".equals(key)
                    || "source".equals(key)) {
                continue;
            }
            result.put(key, entry.getValue().trim());
        }
        return result.isEmpty() ? java.util.Map.of() : java.util.Map.copyOf(result);
    }

    private static BlockSample firstNonAirState(EchoVoxelWorld world, int x, int z) {
        for (int y = world.chunkSize() - 1; y >= 0; y--) {
            EchoVoxelBlockState state = world.blockStateAt(x, y, z);
            if (!state.air()) {
                return new BlockSample(y, state);
            }
        }
        throw new AssertionError("Spawn column should contain generated terrain");
    }

    private record BlockSample(int y, EchoVoxelBlockState state) {
    }

    private static void writeReport(
            EchoVoxelBiome spawnBiome,
            Set<String> sampledBiomeIds,
            BlockSample spawnSample,
            String debugOverlay,
            BiomeEnvironmentEvidence environment,
            BiomeEnvironmentCacheEvidence environmentCache,
            BiomeRenderEvidence render,
            AtlasSignatureEvidence atlasSignature,
            AtlasCacheEvidence atlasCache,
            AtlasMetadataEvidence atlasMetadata
    ) throws IOException {
        String json = """
                {
                  "schema": "echo.standalone.client_voxel_biome_rendering.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoClientVoxelBiomeSmokeHarness",
                  "status": "PASS",
                  "summary": "Generated voxel chunks expose biome ids through block-state metadata and render materials; biome tint, fog/ambience switching, debug overlay text, and atlas source caching are covered by deterministic client smoke evidence.",
                  "coverage": {
                    "generatedBiomeRegions": true,
                    "blockStateBiomeMetadata": true,
                    "typedChunkBiomeCells": true,
                    "debugOverlayBiomeAndFog": true,
                    "audioAmbienceSwitch": true,
                    "renderEnvironmentCache": true,
                    "openglChunkMaterialBiomeId": true,
                    "openglChunkMaterialBiomeTint": true,
                    "atlasSourceSignature": true,
                    "runtimeOnlyMachineMetadataIgnored": true
                  },
                  "spawn": {
                    "biomeId": "%s",
                    "displayName": "%s",
                    "sampleY": %d,
                    "sampleBlockId": "%s"
                  },
                  "sampledBiomeIds": %s,
                  "debugOverlay": {
                    "containsBiome": %s,
                    "containsFog": %s
                  },
                  "environment": {
                    "spawnAmbienceClipId": "%s",
                    "toxicAmbienceClipId": "%s",
                    "spawnFogDensity": %s,
                    "toxicFogDensity": %s,
                    "audioEvents": %d,
                    "cacheBuilds": %d,
                    "cacheHits": %d
                  },
                  "renderPacket": {
                    "chunkMeshes": %d,
                    "faceBiomeId": "%s",
                    "materialBiomeId": "%s",
                    "biomeTintArgb": "%s",
                    "materialArgb": "%s",
                    "untintedBlockArgb": "%s"
                  },
                  "atlasSource": {
                    "signatureBefore": %d,
                    "signatureAfterMaterialMutation": %d,
                    "loadedChunks": %d,
                    "cellsPerChunk": %d,
                    "mutatedChunkScans": %d,
                    "mutatedCellScans": %d,
                    "runtimeMetadataSignature": %d,
                    "runtimeMetadataRepeatSignature": %d,
                    "modelRelevantSignature": %d
                  },
                  "nativeModLoaderCommandUsed": false
                }
                """.formatted(
                escape(spawnBiome.id()),
                escape(spawnBiome.displayName()),
                spawnSample.y(),
                escape(spawnSample.state().block().id()),
                stringArray(new java.util.TreeSet<>(sampledBiomeIds)),
                debugOverlay.contains("BIOME " + spawnBiome.id()),
                debugOverlay.contains("ENV echo:ambience_ash_wasteland FOG"),
                escape(environment.spawnAmbienceClipId()),
                escape(environment.toxicAmbienceClipId()),
                Double.toString(environment.spawnFogDensity()),
                Double.toString(environment.toxicFogDensity()),
                environment.audioEvents(),
                environmentCache.buildCount(),
                environmentCache.cacheHitCount(),
                render.chunkMeshes(),
                escape(render.faceBiomeId()),
                escape(render.materialBiomeId()),
                hex(render.biomeTintArgb()),
                hex(render.materialArgb()),
                hex(render.untintedBlockArgb()),
                atlasSignature.beforeSignature(),
                atlasSignature.afterSignature(),
                atlasCache.loadedChunks(),
                atlasCache.cellsPerChunk(),
                atlasCache.mutatedChunkScans(),
                atlasCache.mutatedCellScans(),
                atlasMetadata.modelStateSignature(),
                atlasMetadata.runtimeMetadataSignature(),
                atlasMetadata.modelRelevantSignature()
        );
        Files.createDirectories(REPORT_PATH.getParent());
        Files.writeString(REPORT_PATH, json);
    }

    private static String stringArray(Set<String> values) {
        StringBuilder json = new StringBuilder("[");
        int index = 0;
        for (String value : values) {
            if (index++ > 0) {
                json.append(", ");
            }
            json.append("\"").append(escape(value)).append("\"");
        }
        json.append("]");
        return json.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String hex(int value) {
        return "0x" + String.format("%08X", value);
    }

    private record BiomeEnvironmentEvidence(
            String spawnAmbienceClipId,
            String toxicAmbienceClipId,
            double spawnFogDensity,
            double toxicFogDensity,
            int audioEvents
    ) {
    }

    private record BiomeEnvironmentCacheEvidence(int buildCount, int cacheHitCount) {
    }

    private record BiomeRenderEvidence(
            int chunkMeshes,
            String faceBiomeId,
            String materialBiomeId,
            int biomeTintArgb,
            int materialArgb,
            int untintedBlockArgb
    ) {
    }

    private record AtlasSignatureEvidence(int beforeSignature, int afterSignature) {
    }

    private record AtlasCacheEvidence(
            int loadedChunks,
            int cellsPerChunk,
            int mutatedChunkScans,
            int mutatedCellScans
    ) {
    }

    private record AtlasMetadataEvidence(
            int modelStateSignature,
            int runtimeMetadataSignature,
            int modelRelevantSignature
    ) {
    }
}
