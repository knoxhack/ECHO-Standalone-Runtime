package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.world.block.behavior.EchoBlockBehaviorRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Phase B smoke harness: loads Openlands MVP block behavior JSON and verifies all 26 blocks
 * have registered gameplay properties.
 *
 * <p>Writes {@code reports/echo/standalone/openlands-block-behavior.json}.
 */
public final class EchoOpenlandsBlockBehaviorSmokeHarness {

    private static final List<String> REQUIRED_BLOCKS = List.of(
            "echoopenlandsprotocol:meadow_grass_block",
            "echoopenlandsprotocol:forest_soil",
            "echoopenlandsprotocol:dry_soil",
            "echoopenlandsprotocol:mud",
            "echoopenlandsprotocol:limestone",
            "echoopenlandsprotocol:granite",
            "echoopenlandsprotocol:shale",
            "echoopenlandsprotocol:deepstone",
            "echoopenlandsprotocol:glow_crystal_cluster",
            "echoopenlandsprotocol:pine_log",
            "echoopenlandsprotocol:pine_planks",
            "echoopenlandsprotocol:pine_beam",
            "echoopenlandsprotocol:pine_post",
            "echoopenlandsprotocol:thatch_roof",
            "echoopenlandsprotocol:fieldstone_bricks",
            "echoopenlandsprotocol:brick_block",
            "echoopenlandsprotocol:glass_block",
            "echoopenlandsprotocol:shelf",
            "echoopenlandsprotocol:sign",
            "echoopenlandsprotocol:lantern",
            "echoopenlandsprotocol:map_table",
            "echoopenlandsprotocol:old_road_block",
            "echoopenlandsprotocol:old_road_marker",
            "echoopenlandsprotocol:broken_waystone",
            "echoopenlandsprotocol:restored_waystone",
            "echoopenlandsprotocol:waystone_plinth"
    );

    private EchoOpenlandsBlockBehaviorSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path reportRoot = Path.of(".").toAbsolutePath().normalize().resolve("reports/echo/standalone");
        Files.createDirectories(reportRoot);

        EchoBlockBehaviorRegistry registry = new EchoBlockBehaviorRegistry();
        Path behaviorsDir = findBehaviorsDirectory();
        registry.loadDirectory(behaviorsDir);
        registry.freeze();

        int checked = 0;
        int failures = 0;
        StringBuilder failureDetails = new StringBuilder();

        for (String blockId : REQUIRED_BLOCKS) {
            checked++;
            if (registry.find(blockId).isEmpty()) {
                failures++;
                failureDetails.append("missing behavior for ").append(blockId).append("; ");
            }
        }

        // Spot checks
        checked++;
        if (registry.get("echoopenlandsprotocol:lantern").lightEmission() != 14) {
            failures++;
            failureDetails.append("lantern light level wrong; ");
        }
        checked++;
        if (!registry.get("echoopenlandsprotocol:glass_block").opaque()) {
            failures++;
            failureDetails.append("glass_block should not be opaque; ");
        }
        checked++;
        if (!registry.get("echoopenlandsprotocol:pine_log").flammable()) {
            failures++;
            failureDetails.append("pine_log should be flammable; ");
        }

        boolean pass = failures == 0;
        writeReport(reportRoot, pass, registry.size(), checked, failures,
                failureDetails.length() > 0 ? failureDetails.toString() : "");

        if (!pass) {
            throw new AssertionError("Openlands block behavior smoke failed: " + failureDetails);
        }

        System.out.println("openlands block behavior smoke PASS behaviors=" + registry.size()
                + " checked=" + checked);
    }

    private static Path findBehaviorsDirectory() {
        Path source = Path.of("echo-runtime-world/src/main/resources/data/echoopenlandsprotocol/block_behaviors");
        if (Files.isDirectory(source)) {
            return source.toAbsolutePath().normalize();
        }
        Path built = Path.of("echo-runtime-world/build/resources/main/data/echoopenlandsprotocol/block_behaviors");
        if (Files.isDirectory(built)) {
            return built.toAbsolutePath().normalize();
        }
        throw new IllegalStateException("Openlands block behavior directory not found");
    }

    private static void writeReport(Path root, boolean pass, int behaviors, int checked,
                                    int failures, String failureDetails) throws IOException {
        Path path = root.resolve("openlands-block-behavior.json");
        String status = pass ? "PASS" : "FAIL";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"schema\": \"echo.standalone.openlands_block_behavior.v1\",\n");
        sb.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        sb.append("  \"status\": \"").append(status).append("\",\n");
        sb.append("  \"behaviorsLoaded\": ").append(behaviors).append(",\n");
        sb.append("  \"requiredBlocks\": ").append(REQUIRED_BLOCKS.size()).append(",\n");
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
