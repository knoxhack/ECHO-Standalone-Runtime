package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;
import dev.echo.standalone.runtime.world.block.state.EchoBlockRegistry;
import dev.echo.standalone.runtime.world.chunk.EchoChunkColumn;
import dev.echo.standalone.runtime.world.gen.EchoOpenlandsChunkGenerator;
import dev.echo.standalone.runtime.world.gen.biome.EchoBiomeSource;
import dev.echo.standalone.runtime.world.gen.biome.EchoSurfaceRule;
import dev.echo.standalone.runtime.world.gen.biome.EchoSurfaceRules;
import dev.echo.standalone.runtime.world.openlands.EchoFoundationBlocks;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBiomeDefinition;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBlockDefinition;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBlocks;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsContentLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Smoke harness for Phase B.3/B.4: Openlands terrain generation with biome-aware surface rules.
 *
 * <p>Expects a single command-line argument: the path to the ECHO-Modules repository root.
 * Generates a small region of Openlands terrain and verifies that all four MVP biomes appear
 * and that surface blocks are Openlands blocks. Writes a deterministic report.
 */
public final class EchoOpenlandsTerrainGenerationSmokeHarness {

    private static final int REGION_SIZE_CHUNKS = 16;
    private static final int BIOME_SEARCH_RADIUS_BLOCKS = 8192;
    private static final int BIOME_SEARCH_STEP_BLOCKS = 64;

    private EchoOpenlandsTerrainGenerationSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Expected ECHO-Modules repository root as first argument");
        }
        Path modulesRoot = Path.of(args[0]).toAbsolutePath().normalize();
        Path reportRoot = Path.of(".").toAbsolutePath().normalize().resolve("reports/echo/standalone");
        Files.createDirectories(reportRoot);

        long seed = 12345L;
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

        Set<String> biomesSeen = new HashSet<>();
        java.util.Map<String, int[]> biomeSamplePositions = new java.util.HashMap<>();
        // Scan a large area cheaply to discover representative positions for each biome.
        for (int z = -BIOME_SEARCH_RADIUS_BLOCKS; z <= BIOME_SEARCH_RADIUS_BLOCKS && biomesSeen.size() < 4; z += BIOME_SEARCH_STEP_BLOCKS) {
            for (int x = -BIOME_SEARCH_RADIUS_BLOCKS; x <= BIOME_SEARCH_RADIUS_BLOCKS && biomesSeen.size() < 4; x += BIOME_SEARCH_STEP_BLOCKS) {
                String biome = biomeSource.biomeAt(x, z);
                if (!biomesSeen.contains(biome)) {
                    biomesSeen.add(biome);
                    biomeSamplePositions.put(biome, new int[]{x, z});
                }
            }
        }

        Set<String> surfaceBlocks = new HashSet<>();
        int totalColumns = 0;
        int nonAirBlocks = 0;

        for (int cz = 0; cz < REGION_SIZE_CHUNKS; cz++) {
            for (int cx = 0; cx < REGION_SIZE_CHUNKS; cx++) {
                EchoChunkColumn column = generator.generateColumn(cx, cz);
                totalColumns++;
                for (int z = 0; z < EchoChunkColumn.SECTION_SIZE; z++) {
                    for (int x = 0; x < EchoChunkColumn.SECTION_SIZE; x++) {
                        int worldX = cx * EchoChunkColumn.SECTION_SIZE + x;
                        int worldZ = cz * EchoChunkColumn.SECTION_SIZE + z;
                        biomesSeen.add(biomeSource.biomeAt(worldX, worldZ));
                        for (int y = EchoOpenlandsChunkGenerator.MIN_Y; y <= EchoOpenlandsChunkGenerator.MAX_Y; y++) {
                            EchoBlockStateContract state = column.stateAt(worldX, y, worldZ);
                            if (!state.air()) {
                                nonAirBlocks++;
                            }
                            if (y == surfaceHeight(worldX, worldZ, seed)) {
                                surfaceBlocks.add(state.toString());
                            }
                        }
                    }
                }
            }
        }

        // Also generate at least one chunk centered on each discovered biome sample position.
        for (int[] pos : biomeSamplePositions.values()) {
            int centerChunkX = pos[0] >> 4;
            int centerChunkZ = pos[1] >> 4;
            EchoChunkColumn column = generator.generateColumn(centerChunkX, centerChunkZ);
            totalColumns++;
            for (int z = 0; z < EchoChunkColumn.SECTION_SIZE; z++) {
                for (int x = 0; x < EchoChunkColumn.SECTION_SIZE; x++) {
                    int worldX = centerChunkX * EchoChunkColumn.SECTION_SIZE + x;
                    int worldZ = centerChunkZ * EchoChunkColumn.SECTION_SIZE + z;
                    for (int y = EchoOpenlandsChunkGenerator.MIN_Y; y <= EchoOpenlandsChunkGenerator.MAX_Y; y++) {
                        EchoBlockStateContract state = column.stateAt(worldX, y, worldZ);
                        if (!state.air()) {
                            nonAirBlocks++;
                        }
                        if (y == surfaceHeight(worldX, worldZ, seed)) {
                            surfaceBlocks.add(state.toString());
                        }
                    }
                }
            }
        }

        int checked = 4;
        int failures = 0;
        StringBuilder failureDetails = new StringBuilder();

        if (!biomesSeen.contains("echoopenlandsprotocol:meadows")) {
            failures++;
            failureDetails.append("missing meadows biome; ");
        }
        if (!biomesSeen.contains("echoopenlandsprotocol:woodlands")) {
            failures++;
            failureDetails.append("missing woodlands biome; ");
        }
        if (!biomesSeen.contains("echoopenlandsprotocol:stonehills")) {
            failures++;
            failureDetails.append("missing stonehills biome; ");
        }
        if (!biomesSeen.contains("echoopenlandsprotocol:marshlands")) {
            failures++;
            failureDetails.append("missing marshlands biome; ");
        }

        checked++;
        boolean allOpenlandsSurfaces = surfaceBlocks.stream().allMatch(id ->
                id.startsWith("echoopenlandsprotocol:") || id.startsWith("echomaterialcore:")
        );
        if (!allOpenlandsSurfaces) {
            failures++;
            failureDetails.append("non-Openlands surface blocks: ").append(surfaceBlocks).append("; ");
        }

        boolean pass = failures == 0;
        writeReport(reportRoot, pass, totalColumns, biomesSeen.size(), nonAirBlocks,
                checked, failures, failureDetails.length() > 0 ? failureDetails.toString() : "");

        if (!pass) {
            throw new AssertionError("Openlands terrain generation smoke failed: " + failureDetails);
        }

        System.out.println("openlands terrain generation smoke PASS columns=" + totalColumns
                + " biomes=" + biomesSeen.size() + " nonAir=" + nonAirBlocks);
    }

    private static int surfaceHeight(int worldX, int worldZ, long seed) {
        dev.echo.standalone.runtime.world.EchoNoiseSampler noise = new dev.echo.standalone.runtime.world.EchoNoiseSampler(seed);
        double continentNoise = noise.sampleOctave(worldX * 0.0015D, 0.0D, worldZ * 0.0015D, 3, 0.5D, 2.0D);
        double detailNoise = noise.sampleOctave(worldX * 0.01D, 0.0D, worldZ * 0.01D, 2, 0.5D, 2.0D);
        double height = EchoOpenlandsChunkGenerator.SEA_LEVEL + continentNoise * 32.0D + detailNoise * 4.0D;
        return (int) Math.round(height);
    }

    private static void writeReport(Path root, boolean pass, int columns, int biomesSeen,
                                    int nonAirBlocks, int checked, int failures,
                                    String failureDetails) throws IOException {
        Path path = root.resolve("openlands-terrain-generation.json");
        String status = pass ? "PASS" : "FAIL";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"schema\": \"echo.standalone.openlands_terrain_generation.v1\",\n");
        sb.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        sb.append("  \"status\": \"").append(status).append("\",\n");
        sb.append("  \"columns\": ").append(columns).append(",\n");
        sb.append("  \"biomesSeen\": ").append(biomesSeen).append(",\n");
        sb.append("  \"nonAirBlocks\": ").append(nonAirBlocks).append(",\n");
        sb.append("  \"checks\": ").append(checked).append(",\n");
        sb.append("  \"failures\": ").append(failures).append(",\n");
        sb.append("  \"failureDetails\": \"").append(escape(failureDetails)).append("\"\n");
        sb.append("}\n");
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
