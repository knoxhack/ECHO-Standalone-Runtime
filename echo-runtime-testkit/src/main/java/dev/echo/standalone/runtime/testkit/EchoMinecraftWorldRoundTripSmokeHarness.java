package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;
import dev.echo.standalone.runtime.nbt.EchoNbtCompound;
import dev.echo.standalone.runtime.nbt.EchoNbtList;
import dev.echo.standalone.runtime.nbt.EchoNbtString;
import dev.echo.standalone.runtime.save.anvil.EchoAnvilChunkData;
import dev.echo.standalone.runtime.save.anvil.EchoAnvilRegionLoader;
import dev.echo.standalone.runtime.save.anvil.EchoAnvilRegionWriter;
import dev.echo.standalone.runtime.save.anvil.EchoAnvilSectionData;
import dev.echo.standalone.runtime.world.EchoMinecraftWorld;
import dev.echo.standalone.runtime.world.block.state.EchoBlockRegistry;
import dev.echo.standalone.runtime.world.block.state.EchoVanillaBlocks;
import dev.echo.standalone.runtime.world.chunk.EchoAnvilSectionAdapter;
import dev.echo.standalone.runtime.world.chunk.EchoChunkColumn;
import dev.echo.standalone.runtime.world.chunk.EchoChunkSection;
import dev.echo.standalone.runtime.world.gen.EchoMinecraftChunkGenerator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * End-to-end smoke harness for the Phase A Minecraft world foundation.
 *
 * <p>Generates a small Minecraft-style world, writes it to an Anvil region file, loads it back,
 * decodes the sections, and asserts that blockstates round-trip exactly. Produces a deterministic
 * JSON report under {@code reports/echo/standalone}.
 */
public final class EchoMinecraftWorldRoundTripSmokeHarness {

    private static final int REGION_SIZE_CHUNKS = 2;
    private static final int SAMPLES_PER_CHUNK = 8;

    private EchoMinecraftWorldRoundTripSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        long seed = 12345L;
        Path reportRoot = Path.of(".").toAbsolutePath().normalize().resolve("reports/echo/standalone");
        Files.createDirectories(reportRoot);

        EchoBlockRegistry registry = new EchoBlockRegistry();
        EchoVanillaBlocks.registerAll(registry);
        registry.freeze();

        EchoMinecraftWorld world = new EchoMinecraftWorld("roundtrip-test", seed, registry);
        EchoMinecraftChunkGenerator generator = new EchoMinecraftChunkGenerator(seed, registry);

        // Generate and copy a small region of columns into the world.
        int totalColumns = 0;
        for (int cz = 0; cz < REGION_SIZE_CHUNKS; cz++) {
            for (int cx = 0; cx < REGION_SIZE_CHUNKS; cx++) {
                EchoChunkColumn generated = generator.generateColumn(cx, cz);
                EchoChunkColumn target = world.columnAt(cx, cz);
                copyColumn(generated, target, cx, cz);
                totalColumns++;
            }
        }

        // Save to a temporary Anvil region file.
        Path tempDir = Files.createTempDirectory("echo-anvil-roundtrip");
        Path regionFile = tempDir.resolve("r.0.0.mca");
        EchoAnvilSectionAdapter adapter = new EchoAnvilSectionAdapter(registry);
        try (EchoAnvilRegionWriter writer = new EchoAnvilRegionWriter(regionFile)) {
            for (int cz = 0; cz < REGION_SIZE_CHUNKS; cz++) {
                for (int cx = 0; cx < REGION_SIZE_CHUNKS; cx++) {
                    EchoChunkColumn column = world.columnAt(cx, cz);
                    EchoNbtCompound chunkRoot = encodeChunk(column, cx, cz, adapter);
                    writer.writeChunk(cx, cz, chunkRoot, 0);
                }
            }
        }

        // Load back and rebuild columns.
        EchoMinecraftWorld loadedWorld = new EchoMinecraftWorld("roundtrip-test-loaded", seed, registry);
        EchoAnvilRegionLoader loader = new EchoAnvilRegionLoader(regionFile);
        int loadedSections = 0;
        for (int cz = 0; cz < REGION_SIZE_CHUNKS; cz++) {
            for (int cx = 0; cx < REGION_SIZE_CHUNKS; cx++) {
                final int chunkX = cx;
                final int chunkZ = cz;
                EchoAnvilChunkData chunkData = loader.loadChunk(chunkX, chunkZ)
                        .orElseThrow(() -> new AssertionError("missing chunk [" + chunkX + ", " + chunkZ + "]"));
                EchoChunkColumn loadedColumn = loadedWorld.columnAt(chunkX, chunkZ);
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
        int sectionYMin = EchoMinecraftChunkGenerator.MIN_Y / EchoChunkSection.SECTION_SIZE;
        int sectionYMax = (EchoMinecraftChunkGenerator.MAX_Y - 1) / EchoChunkSection.SECTION_SIZE;
        List<String> mismatchDetails = new ArrayList<>();
        for (int cz = 0; cz < REGION_SIZE_CHUNKS; cz++) {
            for (int cx = 0; cx < REGION_SIZE_CHUNKS; cx++) {
                for (int i = 0; i < SAMPLES_PER_CHUNK; i++) {
                    int x = (cx * EchoChunkColumn.SECTION_SIZE) + ((i * 3) % EchoChunkColumn.SECTION_SIZE);
                    int z = (cz * EchoChunkColumn.SECTION_SIZE) + ((i * 5 + 1) % EchoChunkColumn.SECTION_SIZE);
                    for (int sy = sectionYMin; sy <= sectionYMax; sy++) {
                        int baseY = sy * EchoChunkSection.SECTION_SIZE;
                        for (int y = baseY; y < baseY + EchoChunkSection.SECTION_SIZE; y += 4) {
                            EchoBlockStateContract original = world.stateAt(x, y, z);
                            EchoBlockStateContract loaded = loadedWorld.stateAt(x, y, z);
                            compared++;
                            if (!original.equals(loaded)) {
                                mismatches++;
                                if (mismatchDetails.size() < 10) {
                                    mismatchDetails.add("[" + x + "," + y + "," + z + "] original=" + original
                                            + " loaded=" + loaded);
                                }
                            }
                            if (!original.air()) {
                                nonAir++;
                            }
                        }
                    }
                }
            }
        }

        boolean pass = mismatches == 0;
        writeReport(reportRoot, pass, totalColumns, loadedSections, compared, nonAir, mismatches, mismatchDetails);

        // Cleanup.
        Files.deleteIfExists(regionFile);
        Files.deleteIfExists(tempDir);

        if (!pass) {
            throw new AssertionError("Round-trip mismatches: " + mismatches + ". First few: " + mismatchDetails);
        }

        System.out.println("minecraft world anvil round-trip smoke PASS columns=" + totalColumns
                + " sections=" + loadedSections + " compared=" + compared + " nonAir=" + nonAir);
    }

    private static void copyColumn(EchoChunkColumn source, EchoChunkColumn target, int cx, int cz) {
        for (int y = EchoMinecraftChunkGenerator.MIN_Y; y <= EchoMinecraftChunkGenerator.MAX_Y; y++) {
            for (int z = 0; z < EchoChunkColumn.SECTION_SIZE; z++) {
                for (int x = 0; x < EchoChunkColumn.SECTION_SIZE; x++) {
                    int wx = cx * EchoChunkColumn.SECTION_SIZE + x;
                    int wz = cz * EchoChunkColumn.SECTION_SIZE + z;
                    target.setState(wx, y, wz, source.stateAt(wx, y, wz));
                    target.setBiome(wx, y, wz, source.biomeAt(wx, y, wz));
                }
            }
        }
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
                                    int nonAir, int mismatches, List<String> mismatchDetails) throws IOException {
        Path path = root.resolve("minecraft-world-anvil-round-trip.json");
        String status = pass ? "PASS" : "FAIL";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"schema\": \"echo.standalone.minecraft_world_anvil_round_trip.v1\",\n");
        sb.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        sb.append("  \"status\": \"").append(status).append("\",\n");
        sb.append("  \"columns\": ").append(columns).append(",\n");
        sb.append("  \"sections\": ").append(sections).append(",\n");
        sb.append("  \"comparedBlocks\": ").append(compared).append(",\n");
        sb.append("  \"nonAirBlocks\": ").append(nonAir).append(",\n");
        sb.append("  \"mismatches\": ").append(mismatches).append(",\n");
        sb.append("  \"mismatchDetails\": [");
        for (int i = 0; i < mismatchDetails.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("\"").append(escape(mismatchDetails.get(i))).append("\"");
        }
        sb.append("]\n");
        sb.append("}\n");
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
