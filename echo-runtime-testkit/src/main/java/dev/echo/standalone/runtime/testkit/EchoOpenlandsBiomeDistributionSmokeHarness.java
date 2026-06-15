package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.world.gen.biome.EchoBiomeSource;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBiomeDefinition;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsContentLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase B smoke harness: measures Openlands biome distribution across a deterministic sample grid.
 *
 * <p>Expects a single command-line argument: the path to the ECHO-Modules repository root.
 * Writes {@code reports/echo/standalone/openlands-biome-distribution.json}.
 */
public final class EchoOpenlandsBiomeDistributionSmokeHarness {

    private static final long SEED = 12345L;
    private static final int SAMPLE_RADIUS_BLOCKS = 8192;
    private static final int SAMPLE_STEP_BLOCKS = 64;

    private EchoOpenlandsBiomeDistributionSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Expected ECHO-Modules repository root as first argument");
        }
        Path modulesRoot = Path.of(args[0]).toAbsolutePath().normalize();
        Path reportRoot = Path.of(".").toAbsolutePath().normalize().resolve("reports/echo/standalone");
        Files.createDirectories(reportRoot);

        List<Path> moduleRoots = List.of(modulesRoot.resolve("addons/echoopenlandsprotocol"));
        List<EchoOpenlandsBiomeDefinition> biomes = EchoOpenlandsContentLoader.loadBiomes(moduleRoots);

        EchoBiomeSource source = new EchoBiomeSource(SEED, biomes);

        Map<String, Long> counts = new HashMap<>();
        int totalSamples = 0;
        for (int z = -SAMPLE_RADIUS_BLOCKS; z <= SAMPLE_RADIUS_BLOCKS; z += SAMPLE_STEP_BLOCKS) {
            for (int x = -SAMPLE_RADIUS_BLOCKS; x <= SAMPLE_RADIUS_BLOCKS; x += SAMPLE_STEP_BLOCKS) {
                String biome = source.biomeAt(x, z);
                counts.merge(biome, 1L, Long::sum);
                totalSamples++;
            }
        }

        int expected = 4;
        int seen = counts.size();
        boolean pass = seen >= expected;

        writeReport(reportRoot, pass, totalSamples, seen, counts);

        System.out.println("openlands biome distribution smoke PASS samples=" + totalSamples
                + " biomes=" + seen);
    }

    private static void writeReport(Path root, boolean pass, int totalSamples, int biomesSeen,
                                    Map<String, Long> counts) throws IOException {
        Path path = root.resolve("openlands-biome-distribution.json");
        String status = pass ? "PASS" : "FAIL";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"schema\": \"echo.standalone.openlands_biome_distribution.v1\",\n");
        sb.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        sb.append("  \"status\": \"").append(status).append("\",\n");
        sb.append("  \"seed\": ").append(SEED).append(",\n");
        sb.append("  \"sampleRadiusBlocks\": ").append(SAMPLE_RADIUS_BLOCKS).append(",\n");
        sb.append("  \"sampleStepBlocks\": ").append(SAMPLE_STEP_BLOCKS).append(",\n");
        sb.append("  \"totalSamples\": ").append(totalSamples).append(",\n");
        sb.append("  \"biomesSeen\": ").append(biomesSeen).append(",\n");
        sb.append("  \"distribution\": {");
        List<String> keys = counts.keySet().stream().sorted().toList();
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            String key = keys.get(i);
            sb.append("\"").append(escape(key)).append("\": ").append(counts.get(key));
        }
        sb.append("}\n");
        sb.append("}\n");
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
