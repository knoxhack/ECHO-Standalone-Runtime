package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.contracts.voxel.EchoBlockBehaviorContract;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockBreakResult;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.block.behavior.EchoBlockBehavior;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 4 Slice 1 smoke harness: verifies that every Ashfall block exported by
 * {@link EchoAdapterCoreStandaloneContentBridge#ashfallLive()} carries a resolved
 * {@link EchoBlockBehaviorContract} and that the runtime derives gameplay properties
 * (hardness, solidity, opacity, harvest tool, light) from it.
 *
 * <p>Writes {@code reports/echo/standalone/ashfall-block-behavior-wired.json}.
 */
public final class EchoAshfallBlockBehaviorWiredSmokeHarness {
    private static final int TOOL_BREAK_CHECKS = 4;

    private EchoAshfallBlockBehaviorWiredSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path reportRoot = Path.of(".").toAbsolutePath().normalize().resolve("reports/echo/standalone");
        Files.createDirectories(reportRoot);

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        List<EchoAdapterCoreRegistryEntry> blocks = bridge.registry().blocks();

        int checked = 0;
        int failures = 0;
        int behaviorCount = 0;
        List<String> failureDetails = new ArrayList<>();

        for (EchoAdapterCoreRegistryEntry entry : blocks) {
            EchoVoxelBlock block = entry.voxelBlock().orElse(null);
            if (block == null) {
                continue;
            }
            checked++;
            if (block.behavior().isEmpty()) {
                failures++;
                failureDetails.add("missing behavior for " + block.id());
                continue;
            }
            behaviorCount++;
            EchoBlockBehaviorContract behavior = block.behavior().orElseThrow();
            if (!block.id().equals(behavior.blockId())) {
                failures++;
                failureDetails.add("behavior id mismatch for " + block.id());
            }
            if (Double.isNaN(block.hardness()) || block.hardness() < 0.0D) {
                failures++;
                failureDetails.add("invalid hardness for " + block.id() + ": " + block.hardness());
            }
            if (block.solid() != behavior.solid()) {
                failures++;
                failureDetails.add("solid mismatch for " + block.id());
            }
            if (block.opaque() != behavior.opaque()) {
                failures++;
                failureDetails.add("opaque mismatch for " + block.id());
            }
            if (block.lightOpacity() < 0 || block.lightOpacity() > 15) {
                failures++;
                failureDetails.add("invalid lightOpacity for " + block.id());
            }
            if (block.lightEmission() < 0 || block.lightEmission() > 15) {
                failures++;
                failureDetails.add("invalid lightEmission for " + block.id());
            }
        }

        List<String> breakCompatibilityFailures = verifyBreakToolCompatibility(blocks);
        if (!breakCompatibilityFailures.isEmpty()) {
            failures += breakCompatibilityFailures.size();
            failureDetails.addAll(breakCompatibilityFailures);
        }

        boolean pass = failures == 0 && checked > 0;
        writeReport(reportRoot, pass, checked, behaviorCount, failures, failureDetails, TOOL_BREAK_CHECKS);

        if (!pass) {
            throw new AssertionError("Ashfall block behavior wiring smoke failed: " + failures + " failures. "
                    + String.join("; ", failureDetails));
        }

        System.out.println("ashfall block behavior wired smoke PASS blocks=" + checked
                + " behaviors=" + behaviorCount + " checked=" + (checked * 6 + TOOL_BREAK_CHECKS));
    }

    private static List<String> verifyBreakToolCompatibility(List<EchoAdapterCoreRegistryEntry> entries) {
        List<String> failures = new ArrayList<>();
        EchoVoxelBlock toolRequiredBlock = null;
        for (EchoAdapterCoreRegistryEntry entry : entries) {
            EchoVoxelBlock block = entry.voxelBlock().orElse(null);
            if (block != null && block.requiresTool() && !block.harvestTool().isBlank()) {
                toolRequiredBlock = block;
                break;
            }
        }
        toolRequiredBlock = toolRequiredBlock == null ? syntheticToolRequiredBlock() : toolRequiredBlock;

        EchoVoxelWorld legacyWorld = oneBlockWorld(toolRequiredBlock);
        EchoVoxelBlockBreakResult legacyProbe = legacyWorld.attemptBreakBlock(0, 0, 0, 0.0D, 1.0D);
        if ("wrong_tool".equals(legacyProbe.reason()) || !Double.isFinite(legacyProbe.requiredSeconds())) {
            failures.add("legacy speed-only break overload enforced harvest tool for " + toolRequiredBlock.id());
        }
        EchoVoxelBlockBreakResult legacyBreak = legacyWorld.attemptBreakBlock(
                0,
                0,
                0,
                legacyProbe.requiredSeconds(),
                1.0D
        );
        if (!legacyBreak.broken()) {
            failures.add("legacy speed-only break overload did not break " + toolRequiredBlock.id());
        }

        EchoVoxelWorld wrongToolWorld = oneBlockWorld(toolRequiredBlock);
        EchoVoxelBlockBreakResult wrongTool = wrongToolWorld.attemptBreakBlock(
                0,
                0,
                0,
                999.0D,
                1.0D,
                "hand"
        );
        if (!"wrong_tool".equals(wrongTool.reason()) || wrongTool.broken() || wrongTool.progress() != 0.0D) {
            failures.add("explicit wrong-tool break did not return wrong_tool for " + toolRequiredBlock.id());
        }

        EchoVoxelWorld correctToolWorld = oneBlockWorld(toolRequiredBlock);
        EchoVoxelBlockBreakResult correctToolProbe = correctToolWorld.attemptBreakBlock(
                0,
                0,
                0,
                0.0D,
                1.0D,
                toolRequiredBlock.harvestTool()
        );
        EchoVoxelBlockBreakResult correctToolBreak = correctToolWorld.attemptBreakBlock(
                0,
                0,
                0,
                correctToolProbe.requiredSeconds(),
                1.0D,
                toolRequiredBlock.harvestTool()
        );
        if (!correctToolBreak.broken()) {
            failures.add("explicit correct-tool break did not break " + toolRequiredBlock.id());
        }

        return failures;
    }

    private static EchoVoxelWorld oneBlockWorld(EchoVoxelBlock block) {
        EchoVoxelChunk chunk = new EchoVoxelChunk(new EchoVoxelChunkId(0, 0, 0), 16);
        chunk.setBlockLocal(0, 0, 0, block);
        return new EchoVoxelWorld(
                "ashfall-block-behavior-break-compat",
                17L,
                16,
                List.of(chunk),
                0.5D,
                1.0D,
                0.5D,
                0.0D
        );
    }

    private static EchoVoxelBlock syntheticToolRequiredBlock() {
        return new EchoVoxelBlock(
                "echotest:tool_required_block",
                "Tool Required Block",
                0xFF5B646B,
                true,
                true,
                2.0D
        ).withBehavior(new EchoBlockBehavior(
                "echotest:tool_required_block",
                2.0D,
                5.0D,
                "pickaxe",
                1,
                0,
                15,
                0.6D,
                1.0D,
                1.0D,
                false,
                true,
                true,
                true,
                true,
                false,
                0
        ));
    }

    private static void writeReport(Path root, boolean pass, int blocks, int behaviors, int failures,
                                    List<String> failureDetails, int toolBreakChecks) throws IOException {
        Path path = root.resolve("ashfall-block-behavior-wired.json");
        String status = pass ? "PASS" : "FAIL";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"schema\": \"echo.standalone.ashfall_block_behavior_wired.v1\",\n");
        sb.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        sb.append("  \"status\": \"").append(status).append("\",\n");
        sb.append("  \"blocksChecked\": ").append(blocks).append(",\n");
        sb.append("  \"behaviorsResolved\": ").append(behaviors).append(",\n");
        sb.append("  \"toolBreakChecks\": ").append(toolBreakChecks).append(",\n");
        sb.append("  \"failures\": ").append(failures).append(",\n");
        sb.append("  \"failureDetails\": \"").append(escape(String.join("; ", failureDetails))).append("\"\n");
        sb.append("}\n");
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
