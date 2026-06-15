package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.nbt.EchoNbtCompound;
import dev.echo.standalone.runtime.nbt.EchoNbtIo;
import dev.echo.standalone.runtime.nbt.EchoNbtList;
import dev.echo.standalone.runtime.nbt.EchoNbtString;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;
import dev.echo.standalone.runtime.save.anvil.EchoAnvilChunkData;
import dev.echo.standalone.runtime.save.anvil.EchoAnvilRegionLoader;
import dev.echo.standalone.runtime.save.anvil.EchoAnvilRegionWriter;
import dev.echo.standalone.runtime.save.anvil.EchoAnvilSectionData;
import dev.echo.standalone.runtime.save.anvil.EchoLevelDatCodec;
import dev.echo.standalone.runtime.world.block.state.EchoBlockRegistry;
import dev.echo.standalone.runtime.world.chunk.EchoAnvilSectionAdapter;
import dev.echo.standalone.runtime.world.chunk.EchoChunkColumn;
import dev.echo.standalone.runtime.world.chunk.EchoChunkSection;
import dev.echo.standalone.runtime.world.gen.EchoOpenlandsChunkGenerator;
import dev.echo.standalone.runtime.world.gen.biome.EchoBiomeSource;
import dev.echo.standalone.runtime.world.gen.biome.EchoSurfaceRule;
import dev.echo.standalone.runtime.world.gen.biome.EchoSurfaceRules;
import dev.echo.standalone.runtime.world.openlands.EchoFoundationBlocks;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBiomeDefinition;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBlockDefinition;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBlocks;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsContentLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Phase B deliverable smoke: create an Openlands world, generate terrain, save it through the
 * transactional save runtime, load it back, and verify block/biome round-trip.
 *
 * <p>Expects a single command-line argument: the path to the ECHO-Modules repository root.
 * Writes {@code reports/echo/standalone/openlands-world-save.json}.
 */
public final class EchoOpenlandsWorldSaveSmokeHarness {

    private static final int REGION_SIZE_CHUNKS = 2;
    private static final int SAMPLES_PER_CHUNK = 8;
    private static final int BIOME_SEARCH_RADIUS_BLOCKS = 8192;
    private static final int BIOME_SEARCH_STEP_BLOCKS = 64;

    private EchoOpenlandsWorldSaveSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Expected ECHO-Modules repository root as first argument");
        }
        Path modulesRoot = Path.of(args[0]).toAbsolutePath().normalize();
        Path reportRoot = Path.of(".").toAbsolutePath().normalize().resolve("reports/echo/standalone");
        Files.createDirectories(reportRoot);

        long seed = 12345L;
        int dataVersion = 1;
        String levelName = "openlands-smoke";
        List<Path> moduleRoots = List.of(
                modulesRoot.resolve("addons/echoopenlandsprotocol"),
                modulesRoot.resolve("addons/echomaterialcore"),
                modulesRoot.resolve("addons/echoworldstarter")
        );

        List<EchoOpenlandsBlockDefinition> blocks = EchoOpenlandsContentLoader.loadBlocks(moduleRoots);
        List<EchoOpenlandsBiomeDefinition> biomes = EchoOpenlandsContentLoader.loadBiomes(moduleRoots);

        EchoBlockRegistry registry = new EchoBlockRegistry();
        EchoOpenlandsBlocks.registerAll(registry, blocks);
        EchoFoundationBlocks.registerAll(registry);
        registry.freeze();

        EchoBiomeSource biomeSource = new EchoBiomeSource(seed, biomes);
        EchoSurfaceRule surfaceRule = new EchoSurfaceRules(registry, biomes).defaultRule();
        EchoOpenlandsChunkGenerator generator = new EchoOpenlandsChunkGenerator(seed, registry, biomeSource, surfaceRule);

        // Discover all four MVP biomes across a coarse scan so the small saved region does not
        // have to contain every climate corner.
        Set<String> biomesSeen = new HashSet<>();
        for (int z = -BIOME_SEARCH_RADIUS_BLOCKS; z <= BIOME_SEARCH_RADIUS_BLOCKS && biomesSeen.size() < 4; z += BIOME_SEARCH_STEP_BLOCKS) {
            for (int x = -BIOME_SEARCH_RADIUS_BLOCKS; x <= BIOME_SEARCH_RADIUS_BLOCKS && biomesSeen.size() < 4; x += BIOME_SEARCH_STEP_BLOCKS) {
                biomesSeen.add(biomeSource.biomeAt(x, z));
            }
        }

        // Generate columns and keep them in memory for comparison.
        EchoChunkColumn[][] originalColumns = new EchoChunkColumn[REGION_SIZE_CHUNKS][REGION_SIZE_CHUNKS];
        for (int cz = 0; cz < REGION_SIZE_CHUNKS; cz++) {
            for (int cx = 0; cx < REGION_SIZE_CHUNKS; cx++) {
                originalColumns[cz][cx] = generator.generateColumn(cx, cz);
            }
        }

        // Set up a transactional save profile.
        Path fixtureRoot = Files.createTempDirectory("echo-openlands-world-save-smoke");
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "openlands-smoke",
                "Openlands Smoke",
                "echoopenlandsprotocol",
                1,
                fixtureRoot.resolve("profiles/openlands-smoke"),
                Map.of("worldId", "openlands-test", "seed", Long.toString(seed), "generator", "EchoOpenlandsChunkGenerator")
        );

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoSaveRuntimeResult save = new EchoSaveRuntime().open(services, profile);
        EchoSaveTransaction tx = save.beginTransaction("slot-openlands", "tx-001");

        // Write level.dat as compressed NBT bytes.
        EchoNbtCompound levelData = EchoLevelDatCodec.createMinimal(
                dataVersion, levelName, seed, 0, EchoOpenlandsChunkGenerator.SEA_LEVEL, 0);
        ByteArrayOutputStream levelBytes = new ByteArrayOutputStream();
        EchoNbtCompound levelRoot = new EchoNbtCompound().put("Data", levelData);
        EchoNbtIo.writeCompressed(levelBytes, levelRoot);
        tx.writeBytes("level.dat", levelBytes.toByteArray());

        // Write region file to a temp file, then stage its bytes.
        EchoAnvilSectionAdapter adapter = new EchoAnvilSectionAdapter(registry);
        Path tempRegion = Files.createTempFile("echo-openlands-region", ".mca");
        try (EchoAnvilRegionWriter writer = new EchoAnvilRegionWriter(tempRegion)) {
            for (int cz = 0; cz < REGION_SIZE_CHUNKS; cz++) {
                for (int cx = 0; cx < REGION_SIZE_CHUNKS; cx++) {
                    EchoNbtCompound chunkRoot = encodeChunk(originalColumns[cz][cx], cx, cz, adapter);
                    writer.writeChunk(cx, cz, chunkRoot, 0);
                }
            }
        }
        byte[] regionBytes = Files.readAllBytes(tempRegion);
        Files.deleteIfExists(tempRegion);
        tx.writeBytes("region/r.0.0.mca", regionBytes);

        EchoSaveCommitResult commit = tx.commit(Map.of("generator", "EchoOpenlandsChunkGenerator"));

        // Load back from the committed save slot.
        Path savedRegion = profile.slot("slot-openlands").dataRoot().resolve("region/r.0.0.mca");
        Path savedLevelDat = profile.slot("slot-openlands").dataRoot().resolve("level.dat");

        EchoNbtCompound loadedLevelRoot = EchoLevelDatCodec.read(savedLevelDat);
        EchoNbtCompound loadedData = loadedLevelRoot.getCompound("Data");
        long loadedSeed = loadedData.getLong("RandomSeed");
        String loadedName = loadedData.getString("LevelName");

        EchoAnvilRegionLoader loader = new EchoAnvilRegionLoader(savedRegion);
        EchoChunkColumn[][] loadedColumns = new EchoChunkColumn[REGION_SIZE_CHUNKS][REGION_SIZE_CHUNKS];
        int loadedSections = 0;
        for (int cz = 0; cz < REGION_SIZE_CHUNKS; cz++) {
            for (int cx = 0; cx < REGION_SIZE_CHUNKS; cx++) {
                final int chunkX = cx;
                final int chunkZ = cz;
                EchoAnvilChunkData chunkData = loader.loadChunk(chunkX, chunkZ)
                        .orElseThrow(() -> new AssertionError("missing chunk [" + chunkX + ", " + chunkZ + "]"));
                EchoBlockStateContract air = registry.air();
                loadedColumns[cz][cx] = new EchoChunkColumn(chunkX, chunkZ, air, "echoopenlandsprotocol:meadows");
                EchoChunkColumn loadedColumn = loadedColumns[cz][cx];
                for (EchoAnvilSectionData sectionData : chunkData.sections().values()) {
                    EchoChunkSection section = adapter.adapt(sectionData);
                    for (int y = 0; y < EchoChunkSection.SECTION_SIZE; y++) {
                        for (int z = 0; z < EchoChunkSection.SECTION_SIZE; z++) {
                            for (int x = 0; x < EchoChunkSection.SECTION_SIZE; x++) {
                                int worldX = chunkX * EchoChunkColumn.SECTION_SIZE + x;
                                int worldY = section.sectionY() * EchoChunkSection.SECTION_SIZE + y;
                                int worldZ = chunkZ * EchoChunkColumn.SECTION_SIZE + z;
                                loadedColumn.setState(worldX, worldY, worldZ, section.stateAt(x, y, z));
                                loadedColumn.setBiome(worldX, worldY, worldZ, section.biomeAt(x, y, z));
                            }
                        }
                    }
                    loadedSections++;
                }
            }
        }

        // Compare sampled blocks.
        int compared = 0;
        int mismatches = 0;
        int nonAir = 0;
        int sectionYMin = EchoOpenlandsChunkGenerator.MIN_Y / EchoChunkSection.SECTION_SIZE;
        int sectionYMax = (EchoOpenlandsChunkGenerator.MAX_Y - 1) / EchoChunkSection.SECTION_SIZE;
        List<String> mismatchDetails = new ArrayList<>();
        for (int cz = 0; cz < REGION_SIZE_CHUNKS; cz++) {
            for (int cx = 0; cx < REGION_SIZE_CHUNKS; cx++) {
                EchoChunkColumn original = originalColumns[cz][cx];
                EchoChunkColumn loaded = loadedColumns[cz][cx];
                for (int i = 0; i < SAMPLES_PER_CHUNK; i++) {
                    int x = (cx * EchoChunkColumn.SECTION_SIZE) + ((i * 3) % EchoChunkColumn.SECTION_SIZE);
                    int z = (cz * EchoChunkColumn.SECTION_SIZE) + ((i * 5 + 1) % EchoChunkColumn.SECTION_SIZE);
                    for (int sy = sectionYMin; sy <= sectionYMax; sy++) {
                        int baseY = sy * EchoChunkSection.SECTION_SIZE;
                        for (int y = baseY; y < baseY + EchoChunkSection.SECTION_SIZE; y += 4) {
                            EchoBlockStateContract originalState = original.stateAt(x, y, z);
                            EchoBlockStateContract loadedState = loaded.stateAt(x, y, z);
                            compared++;
                            if (!originalState.equals(loadedState)) {
                                mismatches++;
                                if (mismatchDetails.size() < 10) {
                                    mismatchDetails.add("[" + x + "," + y + "," + z + "] original=" + originalState
                                            + " loaded=" + loadedState);
                                }
                            }
                            if (!originalState.air()) {
                                nonAir++;
                            }
                        }
                    }
                }
            }
        }

        boolean pass = mismatches == 0
                && seed == loadedSeed
                && levelName.equals(loadedName)
                && biomesSeen.size() == 4;
        StringBuilder failureDetails = new StringBuilder();
        if (mismatches > 0) {
            failureDetails.append("round-trip mismatches=").append(mismatches).append("; ");
        }
        if (seed != loadedSeed) {
            failureDetails.append("seed mismatch; ");
        }
        if (!levelName.equals(loadedName)) {
            failureDetails.append("level name mismatch; ");
        }
        if (biomesSeen.size() != 4) {
            failureDetails.append("biomesSeen=").append(biomesSeen.size()).append("; ");
        }

        writeReport(reportRoot, pass, REGION_SIZE_CHUNKS * REGION_SIZE_CHUNKS, loadedSections,
                compared, nonAir, mismatches, biomesSeen.size(), commit.manifest().files().size(),
                failureDetails.length() > 0 ? failureDetails.toString() : "");

        // Cleanup.
        deleteRecursive(fixtureRoot);

        if (!pass) {
            throw new AssertionError("Openlands world save smoke failed: " + failureDetails);
        }

        System.out.println("openlands world save smoke PASS columns=" + (REGION_SIZE_CHUNKS * REGION_SIZE_CHUNKS)
                + " sections=" + loadedSections + " compared=" + compared + " nonAir=" + nonAir
                + " biomes=" + biomesSeen.size() + " files=" + commit.manifest().files().size());
    }

    private static EchoNbtCompound encodeChunk(EchoChunkColumn column, int chunkX, int chunkZ,
                                               EchoAnvilSectionAdapter adapter) {
        List<EchoNbtCompound> sectionCompounds = new ArrayList<>();
        for (var entry : column.sections().entrySet()) {
            EchoAnvilSectionData data = adapter.encode(entry.getValue());
            sectionCompounds.add(encodeSectionData(data));
        }

        return new EchoNbtCompound()
                .put("xPos", chunkX)
                .put("zPos", chunkZ)
                .put("sections", new EchoNbtList(dev.echo.standalone.runtime.nbt.EchoNbtTagType.COMPOUND, sectionCompounds.stream()
                        .map(c -> (dev.echo.standalone.runtime.nbt.EchoNbtTag) c)
                        .toList()));
    }

    private static EchoNbtCompound encodeSectionData(EchoAnvilSectionData data) {
        EchoNbtCompound blockStates = new EchoNbtCompound()
                .put("palette", new EchoNbtList(dev.echo.standalone.runtime.nbt.EchoNbtTagType.STRING,
                        data.blockStatePalette().stream()
                                .map(s -> (dev.echo.standalone.runtime.nbt.EchoNbtTag) new EchoNbtString(s))
                                .toList()))
                .put("data", new dev.echo.standalone.runtime.nbt.EchoNbtLongArray(data.blockStateData()));

        EchoNbtCompound biomes = new EchoNbtCompound()
                .put("palette", new EchoNbtList(dev.echo.standalone.runtime.nbt.EchoNbtTagType.STRING,
                        data.biomePalette().stream()
                                .map(s -> (dev.echo.standalone.runtime.nbt.EchoNbtTag) new EchoNbtString(s))
                                .toList()))
                .put("data", new dev.echo.standalone.runtime.nbt.EchoNbtLongArray(data.biomeData()));

        return new EchoNbtCompound()
                .put("Y", (byte) data.sectionY())
                .put("block_states", blockStates)
                .put("biomes", biomes);
    }

    private static void writeReport(Path root, boolean pass, int columns, int sections, int compared,
                                    int nonAir, int mismatches, int biomesSeen, int filesWritten,
                                    String failureDetails) throws IOException {
        Path path = root.resolve("openlands-world-save.json");
        String status = pass ? "PASS" : "FAIL";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"schema\": \"echo.standalone.openlands_world_save.v1\",\n");
        sb.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        sb.append("  \"status\": \"").append(status).append("\",\n");
        sb.append("  \"columns\": ").append(columns).append(",\n");
        sb.append("  \"sections\": ").append(sections).append(",\n");
        sb.append("  \"comparedBlocks\": ").append(compared).append(",\n");
        sb.append("  \"nonAirBlocks\": ").append(nonAir).append(",\n");
        sb.append("  \"mismatches\": ").append(mismatches).append(",\n");
        sb.append("  \"biomesSeen\": ").append(biomesSeen).append(",\n");
        sb.append("  \"filesWritten\": ").append(filesWritten).append(",\n");
        sb.append("  \"failureDetails\": \"").append(escape(failureDetails)).append("\"\n");
        sb.append("}\n");
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static void deleteRecursive(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        if (Files.isDirectory(path)) {
            try (var stream = Files.list(path)) {
                for (Path child : stream.toList()) {
                    deleteRecursive(child);
                }
            }
        }
        Files.deleteIfExists(path);
    }
}
