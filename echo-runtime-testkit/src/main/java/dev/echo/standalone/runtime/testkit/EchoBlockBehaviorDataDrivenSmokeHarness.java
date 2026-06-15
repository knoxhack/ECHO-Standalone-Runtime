package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockBehaviorContract;
import dev.echo.standalone.runtime.world.block.behavior.EchoBlockBehaviorRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Smoke harness for the data-driven block behavior registry (Phase A.6).
 *
 * <p>Loads behavior definitions from JSON data files and verifies that vanilla-like properties
 * round-trip correctly. Writes a deterministic report under {@code reports/echo/standalone}.
 */
public final class EchoBlockBehaviorDataDrivenSmokeHarness {

    private static final List<String> SAMPLE_BLOCKS = List.of(
            "minecraft:stone",
            "minecraft:dirt",
            "minecraft:grass_block",
            "minecraft:oak_log",
            "minecraft:oak_leaves",
            "minecraft:torch",
            "minecraft:water"
    );

    private EchoBlockBehaviorDataDrivenSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path reportRoot = Path.of(".").toAbsolutePath().normalize().resolve("reports/echo/standalone");
        Files.createDirectories(reportRoot);

        EchoBlockBehaviorRegistry registry = new EchoBlockBehaviorRegistry();
        Path tempDir = copyResourcesToTemp();
        registry.loadDirectory(tempDir);
        registry.freeze();

        int checked = 0;
        int failures = 0;
        StringBuilder failureDetails = new StringBuilder();

        // Stone: pickaxe, hard, opaque.
        checked++;
        if (!check(registry.get("minecraft:stone"),
                1.5, 6.0, 0, 15, true, "pickaxe", true)) {
            failures++;
            appendFailure(failureDetails, "minecraft:stone", registry.get("minecraft:stone"));
        }

        // Dirt: shovel, soft, opaque.
        checked++;
        if (!check(registry.get("minecraft:dirt"),
                0.5, 0.5, 0, 15, false, "shovel", true)) {
            failures++;
            appendFailure(failureDetails, "minecraft:dirt", registry.get("minecraft:dirt"));
        }

        // Grass block: shovel, emits no light.
        checked++;
        if (!check(registry.get("minecraft:grass_block"),
                0.6, 0.6, 0, 15, false, "shovel", true)) {
            failures++;
            appendFailure(failureDetails, "minecraft:grass_block", registry.get("minecraft:grass_block"));
        }

        // Oak log: axe, flammable.
        checked++;
        EchoBlockBehaviorContract oakLog = registry.get("minecraft:oak_log");
        if (oakLog.destroyTime() != 2.0 || oakLog.explosionResistance() != 2.0
                || !"axe".equals(oakLog.harvestTool()) || !oakLog.flammable()
                || oakLog.fireSpreadSpeed() != 5) {
            failures++;
            appendFailure(failureDetails, "minecraft:oak_log", oakLog);
        }

        // Oak leaves: hoe, partial light opacity, highly flammable.
        checked++;
        EchoBlockBehaviorContract oakLeaves = registry.get("minecraft:oak_leaves");
        if (oakLeaves.destroyTime() != 0.2 || oakLeaves.lightOpacity() != 1
                || !"hoe".equals(oakLeaves.harvestTool()) || !oakLeaves.flammable()
                || oakLeaves.fireSpreadSpeed() != 30) {
            failures++;
            appendFailure(failureDetails, "minecraft:oak_leaves", oakLeaves);
        }

        // Torch: emits light, not solid.
        checked++;
        EchoBlockBehaviorContract torch = registry.get("minecraft:torch");
        if (torch.lightEmission() != 14 || torch.solid() || torch.blocksMotion()) {
            failures++;
            appendFailure(failureDetails, "minecraft:torch", torch);
        }

        // Water: slows movement, not solid.
        checked++;
        EchoBlockBehaviorContract water = registry.get("minecraft:water");
        if (water.speedFactor() != 0.8 || water.solid() || water.blocksMotion()) {
            failures++;
            appendFailure(failureDetails, "minecraft:water", water);
        }

        // Missing block should default to air-like.
        checked++;
        EchoBlockBehaviorContract missing = registry.get("minecraft:unknown");
        if (missing.destroyTime() != 0.0 || missing.explosionResistance() != 0.0
                || missing.lightEmission() != 0 || missing.solid()) {
            failures++;
            appendFailure(failureDetails, "minecraft:unknown (default)", missing);
        }

        boolean pass = failures == 0;
        writeReport(reportRoot, pass, registry.size(), checked, failures,
                failureDetails.length() > 0 ? failureDetails.toString() : "");

        // Cleanup.
        try (var stream = Files.walk(tempDir)) {
            stream.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        }

        if (!pass) {
            throw new AssertionError("Block behavior smoke failed: " + failures + " failures. " + failureDetails);
        }

        System.out.println("block behavior data-driven smoke PASS behaviors=" + registry.size()
                + " checked=" + checked);
    }

    private static boolean check(EchoBlockBehaviorContract behavior,
                                 double destroyTime, double explosionResistance,
                                 int lightEmission, int lightOpacity,
                                 boolean requiresTool, String harvestTool, boolean solid) {
        return behavior.destroyTime() == destroyTime
                && behavior.explosionResistance() == explosionResistance
                && behavior.lightEmission() == lightEmission
                && behavior.lightOpacity() == lightOpacity
                && behavior.requiresTool() == requiresTool
                && Objects.equals(behavior.harvestTool(), harvestTool)
                && behavior.solid() == solid;
    }

    private static void appendFailure(StringBuilder sb, String id, EchoBlockBehaviorContract behavior) {
        sb.append("[").append(id).append("] ")
                .append("destroyTime=").append(behavior.destroyTime()).append(" ")
                .append("resistance=").append(behavior.explosionResistance()).append(" ")
                .append("light=").append(behavior.lightEmission()).append(" ")
                .append("opacity=").append(behavior.lightOpacity()).append(" ")
                .append("requiresTool=").append(behavior.requiresTool()).append(" ")
                .append("tool=").append(behavior.harvestTool()).append(" ")
                .append("solid=").append(behavior.solid()).append(" ")
                .append("flammable=").append(behavior.flammable()).append(" ")
                .append("fireSpread=").append(behavior.fireSpreadSpeed()).append(";\n");
    }

    private static Path copyResourcesToTemp() throws IOException {
        Path tempDir = Files.createTempDirectory("echo-block-behaviors");
        ClassLoader loader = EchoBlockBehaviorDataDrivenSmokeHarness.class.getClassLoader();
        for (String blockId : SAMPLE_BLOCKS) {
            String path = "data/echo/block_behaviors/" + blockId.replace(':', '_') + ".json";
            try (InputStream in = loader.getResourceAsStream(path)) {
                if (in == null) {
                    throw new IOException("Missing classpath resource: " + path);
                }
                String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                Files.writeString(tempDir.resolve(blockId.replace(':', '_') + ".json"), text, StandardCharsets.UTF_8);
            }
        }
        return tempDir;
    }

    private static void writeReport(Path root, boolean pass, int behaviors, int checked,
                                    int failures, String failureDetails) throws IOException {
        Path path = root.resolve("block-behavior-data-driven.json");
        String status = pass ? "PASS" : "FAIL";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"schema\": \"echo.standalone.block_behavior_data_driven.v1\",\n");
        sb.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        sb.append("  \"status\": \"").append(status).append("\",\n");
        sb.append("  \"behaviorsLoaded\": ").append(behaviors).append(",\n");
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
