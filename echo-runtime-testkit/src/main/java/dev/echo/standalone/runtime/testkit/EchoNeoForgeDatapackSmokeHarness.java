package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoNeoForgeDatapackLoader;
import dev.echo.standalone.runtime.compat.EchoNeoForgeDatapackScanResult;
import dev.echo.standalone.runtime.compat.EchoVanillaToOpenlandsAliasBridge;
import dev.echo.standalone.runtime.data.EchoDataTag;
import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Smoke harness for the NeoForge datapack loader + vanilla→Openlands alias bridge.
 *
 * <p>Creates a synthetic datapack, loads it, and asserts that vanilla IDs are translated to
 * Openlands/Foundation canonical IDs.
 */
public final class EchoNeoForgeDatapackSmokeHarness {

    private EchoNeoForgeDatapackSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("echo-neoforge-datapack-smoke");
        try {
            Path dataDir = tempDir.resolve("data");
            Files.createDirectories(dataDir.resolve("minecraft/recipes"));
            Files.createDirectories(dataDir.resolve("minecraft/loot_tables/blocks"));
            Files.createDirectories(dataDir.resolve("minecraft/tags/blocks"));

            writeRecipe(dataDir.resolve("minecraft/recipes/stone_pickaxe.json"));
            writeLootTable(dataDir.resolve("minecraft/loot_tables/blocks/stone.json"));
            writeTag(dataDir.resolve("minecraft/tags/blocks/logs.json"));

            EchoVanillaToOpenlandsAliasBridge bridge = new EchoVanillaToOpenlandsAliasBridge();
            EchoNeoForgeDatapackLoader loader = new EchoNeoForgeDatapackLoader(bridge);
            EchoNeoForgeDatapackScanResult result = loader.load(List.of(tempDir));

            int checked = 0;
            int failures = 0;
            StringBuilder details = new StringBuilder();

            checked++;
            if (!"echomaterialcore:fieldstone".equals(bridge.resolve("minecraft:stone"))) {
                failures++;
                details.append("stone alias failed; ");
            }
            checked++;
            if (!"echoopenlandsprotocol:meadow_grass_block".equals(bridge.resolve("minecraft:grass_block"))) {
                failures++;
                details.append("grass_block alias failed; ");
            }
            checked++;
            if (!"echoworldstarter:pitchlight".equals(bridge.resolve("minecraft:torch"))) {
                failures++;
                details.append("torch alias failed; ");
            }
            checked++;
            if (result.recipes().isEmpty()) {
                failures++;
                details.append("no recipes translated; ");
            }
            checked++;
            EchoRecipeDefinition recipe = result.recipes().stream()
                    .filter(r -> r.id().endsWith(":stone_pickaxe"))
                    .findFirst()
                    .orElse(null);
            if (recipe == null) {
                failures++;
                details.append("torch recipe missing; ");
            } else {
                if (!recipe.result().equals("echoworldstarter:pitchlight")) {
                    failures++;
                    details.append("recipe result not remapped: ").append(recipe.result()).append("; ");
                }
                if (recipe.ingredients().stream().noneMatch(i -> i.equals("echomaterialcore:branchwood_stick"))) {
                    failures++;
                    details.append("recipe ingredient stick not remapped; ");
                }
                if (recipe.ingredients().stream().noneMatch(i -> i.equals("echomaterialcore:fieldstone"))) {
                    failures++;
                    details.append("recipe ingredient stone not remapped; ");
                }
            }
            checked++;
            if (result.lootTables().isEmpty()) {
                failures++;
                details.append("no loot tables translated; ");
            }
            checked++;
            EchoLootDefinition loot = result.lootTables().stream()
                    .filter(l -> l.id().endsWith(":blocks/stone"))
                    .findFirst()
                    .orElse(null);
            if (loot == null) {
                failures++;
                details.append("stone loot table missing; ");
            } else if (loot.entries().stream().noneMatch(e -> e.equals("echomaterialcore:fieldstone"))) {
                failures++;
                details.append("loot entry stone not remapped; ");
            }
            checked++;
            if (result.tags().isEmpty()) {
                failures++;
                details.append("no tags translated; ");
            }
            checked++;
            EchoDataTag tag = result.tags().stream()
                    .filter(t -> t.id().endsWith(":logs"))
                    .findFirst()
                    .orElse(null);
            if (tag == null) {
                failures++;
                details.append("logs tag missing; ");
            } else if (tag.values().stream().noneMatch(v -> v.equals("echoopenlandsprotocol:pine_log"))) {
                failures++;
                details.append("logs tag value not remapped; ");
            }

            boolean pass = failures == 0;
            System.out.println("neoforge datapack smoke " + (pass ? "PASS" : "FAIL")
                    + " recipes=" + result.recipes().size()
                    + " loot=" + result.lootTables().size()
                    + " tags=" + result.tags().size()
                    + " diagnostics=" + result.diagnostics().size()
                    + " checked=" + checked
                    + " failures=" + failures);
            if (!pass) {
                throw new AssertionError("NeoForge datapack smoke failed: " + details);
            }
        } finally {
            deleteTree(tempDir);
        }
    }

    private static void writeRecipe(Path path) throws IOException {
        String json = "{\n"
                + "  \"type\": \"minecraft:crafting_shaped\",\n"
                + "  \"pattern\": [\"S S\", \" / \", \" / \"],\n"
                + "  \"key\": {\n"
                + "    \"S\": { \"item\": \"minecraft:stone\" },\n"
                + "    \"/\": { \"item\": \"minecraft:stick\" }\n"
                + "  },\n"
                + "  \"result\": { \"item\": \"minecraft:torch\", \"count\": 4 }\n"
                + "}\n";
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    private static void writeLootTable(Path path) throws IOException {
        String json = "{\n"
                + "  \"type\": \"minecraft:block\",\n"
                + "  \"pools\": [\n"
                + "    {\n"
                + "      \"rolls\": 1,\n"
                + "      \"entries\": [\n"
                + "        { \"type\": \"minecraft:item\", \"name\": \"minecraft:stone\" }\n"
                + "      ]\n"
                + "    }\n"
                + "  ]\n"
                + "}\n";
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    private static void writeTag(Path path) throws IOException {
        String json = "{\n"
                + "  \"values\": [\n"
                + "    \"minecraft:oak_log\"\n"
                + "  ]\n"
                + "}\n";
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        List<Path> paths = new ArrayList<>();
        try (var stream = Files.walk(root)) {
            stream.forEach(paths::add);
        }
        paths.sort((a, b) -> -a.compareTo(b));
        for (Path path : paths) {
            Files.deleteIfExists(path);
        }
    }
}
