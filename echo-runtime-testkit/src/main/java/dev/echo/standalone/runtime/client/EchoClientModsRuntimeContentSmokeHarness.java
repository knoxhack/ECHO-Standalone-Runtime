package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class EchoClientModsRuntimeContentSmokeHarness {
    private EchoClientModsRuntimeContentSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path saveRoot = Path.of("build", "tmp", "client-mods-runtime-content-smoke").toAbsolutePath();
        deleteRecursively(saveRoot);

        EchoClientRuntimeServices services = new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        int imported = services.importAdapterCoreContentRegistrations(runtimeRows());
        require(imported == 5,
                "Client runtime services should import five representative native content rows");

        EchoClientRuntimeContentSummary summary = services.runtimeContentSummary();
        require(summary.rowCount() == 5,
                "Runtime content summary should count every imported native row");
        require(summary.domainCount() == 5,
                "Runtime content summary should preserve per-domain import buckets");
        require(summary.domainCounts().getOrDefault("blocks", 0) == 1,
                "Runtime content summary should include the block import domain");
        require(summary.domainCounts().getOrDefault("items", 0) == 1,
                "Runtime content summary should include the item import domain");
        require(summary.domainCounts().getOrDefault("recipes", 0) == 1,
                "Runtime content summary should include the recipe import domain");
        require(summary.domainCounts().getOrDefault("loot", 0) == 1,
                "Runtime content summary should include the loot import domain");
        require(summary.domainCounts().getOrDefault("ui_screens", 0) == 1,
                "Runtime content summary should include the UI screen import domain");
        require(summary.recentRows(1).get(0).contentId().equals("echoruntimehost:ui/native_runtime_mods"),
                "Runtime content summary should expose most-recent imported rows first");

        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientSettingsController settings = new EchoClientSettingsController(
                screens,
                new EchoClientSettingsStore(saveRoot.resolve("client-options.properties")),
                new RecordingSettingsHost()
        );
        EchoClientScreenRuntimeController screenRuntime =
                new EchoClientScreenRuntimeController(services, screens, settings);
        screenRuntime.showInitialMainMenu();
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_MODS, services.hasContinuableSession()),
                "Mods route should open after runtime surface refresh");

        EchoClientScreenSnapshot mods = screens.snapshot(services.hasContinuableSession());
        require(mods.kind() == EchoClientScreenKind.MODS,
                "Mods runtime content smoke should inspect the Mods route");
        require(optionLabelPrefix(mods, "Runtime Content: 5 runtime row(s), 5 domain(s)"),
                "Mods route should expose a live native runtime content summary");
        require(optionLabel(mods, "Runtime Blocks: 1"),
                "Mods route should expose imported block domain counts");
        require(optionLabel(mods, "Runtime Items: 1"),
                "Mods route should expose imported item domain counts");
        require(optionLabel(mods, "Runtime Recipes: 1"),
                "Mods route should expose imported recipe domain counts");
        require(optionLabel(mods, "Runtime Loot: 1"),
                "Mods route should expose imported loot domain counts");
        require(optionLabel(mods, "Runtime UI Screens: 1"),
                "Mods route should expose imported UI screen domain counts");

        EchoClientScreenOption block = optionLabelPrefixMatch(mods, "Runtime Block: Client Runtime Glass");
        require(block.tooltip().contains("echoruntimehost:block/client_runtime_glass"),
                "Runtime block row tooltip should preserve the native loader content id");
        require(block.tooltip().contains("standalone=echoruntimehost:client_runtime_glass"),
                "Runtime block row tooltip should preserve the standalone runtime id");
        require(optionLabelPrefix(mods, "Runtime Item: Native Runtime Gel"),
                "Mods route should expose imported native item rows");
        require(optionLabelPrefix(mods, "Runtime Recipe: Native Runtime Gel"),
                "Mods route should expose imported native recipe rows");
        require(optionLabelPrefix(mods, "Runtime Loot Table: Runtime Marker Block Loot"),
                "Mods route should expose imported native loot table rows");
        require(optionLabelPrefix(mods, "Runtime UI Screen: Native Runtime Mods"),
                "Mods route should expose imported native UI screen rows");

        writeSmokeReport(summary);
        System.out.println("client mods runtime content smoke PASS rows="
                + summary.rowCount()
                + " domains=" + summary.domainCount()
                + " block=echoruntimehost:client_runtime_glass");
    }

    private static void writeSmokeReport(EchoClientRuntimeContentSummary summary) throws IOException {
        Path report = Path.of("reports", "echo", "standalone", "client-mods-runtime-content-smoke.json").toAbsolutePath();
        Files.createDirectories(report.getParent());
        String json = """
                {
                  "schema": "echo.standalone.client_smoke.client-mods-runtime-content-smoke.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "runtime": "standalone",
                  "moduleIds": ["echoruntimehost", "echoscreencore"],
                  "featureBuckets": ["gui", "screen", "blocks", "items", "recipes", "loot"],
                  "trustedMutations": [
                    "importAdapterCoreContentRegistrations:block",
                    "importAdapterCoreContentRegistrations:item",
                    "importAdapterCoreContentRegistrations:recipe",
                    "importAdapterCoreContentRegistrations:loot",
                    "importAdapterCoreContentRegistrations:ui_screen"
                  ],
                  "visibleRoutes": ["echoscreencore:mods", "echoruntimehost:standalone/native_runtime_mods"],
                  "saveEvidence": [],
                  "networkEvidence": [],
                  "rowCount": %d,
                  "domainCount": %d,
                  "blockers": []
                }
                """.formatted(summary.rowCount(), summary.domainCount());
        Files.writeString(report, json, StandardCharsets.UTF_8);
    }

    private static List<Map<String, Object>> runtimeRows() {
        return List.of(
                blockRow(),
                itemRow(),
                recipeRow(),
                lootRow(),
                screenRow()
        );
    }

    private static Map<String, Object> blockRow() {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:block/client_runtime_glass",
                "contentKind", "BLOCK",
                "domain", "blocks",
                "displayName", "Client Runtime Glass",
                "adapterKey", "registry.blocks.client_runtime_glass",
                "neoForgeId", "echoruntimehost:client_runtime_glass",
                "nativeLoaderId", "echoruntimehost:block/client_runtime_glass",
                "standaloneRuntimeId", "echoruntimehost:client_runtime_glass",
                "metadata", Map.of(
                        "liveVoxelId", "echoruntimehost:client_runtime_glass",
                        "argb", "#6FAFE3",
                        "detailArgb", "0xFFBFE8FF",
                        "atlasKey", "echoruntimehost/block/client_runtime_glass",
                        "materialPattern", "TERMINAL_GRID",
                        "solid", false,
                        "opaque", false,
                        "hardness", "0.35"
                )
        );
    }

    private static Map<String, Object> itemRow() {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:item/native_runtime_gel",
                "contentKind", "ITEM",
                "domain", "items",
                "displayName", "Native Runtime Gel",
                "adapterKey", "registry.items.native_runtime_gel",
                "neoForgeId", "echoruntimehost:native_runtime_gel",
                "nativeLoaderId", "echoruntimehost:item/native_runtime_gel",
                "standaloneRuntimeId", "echoruntimehost:native_runtime_gel",
                "metadata", Map.of(
                        "category", "MATERIAL",
                        "maxStackSize", 16,
                        "weight", "0.2",
                        "tags", List.of("adaptercore", "native-content", "gel")
                )
        );
    }

    private static Map<String, Object> recipeRow() {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:recipe/craft_native_runtime_gel",
                "contentKind", "RECIPE",
                "domain", "recipes",
                "displayName", "Native Runtime Gel",
                "adapterKey", "registry.recipes.craft_native_runtime_gel",
                "neoForgeId", "echoruntimehost:craft_native_runtime_gel",
                "nativeLoaderId", "echoruntimehost:recipe/craft_native_runtime_gel",
                "standaloneRuntimeId", "echoruntimehost:craft_native_runtime_gel",
                "metadata", Map.of(
                        "recipeId", "echoruntimehost:craft_native_runtime_gel",
                        "type", "minecraft:crafting_shapeless",
                        "ingredients", List.of("echoashfallprotocol:scrap_metal"),
                        "ingredientCounts", Map.of("echoashfallprotocol:scrap_metal", 1),
                        "result", "echoruntimehost:native_runtime_gel",
                        "resultCount", 1
                )
        );
    }

    private static Map<String, Object> lootRow() {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:loot/blocks/client_runtime_glass",
                "contentKind", "LOOT_TABLE",
                "domain", "loot",
                "displayName", "Runtime Marker Block Loot",
                "adapterKey", "registry.loot.client_runtime_glass",
                "neoForgeId", "echoruntimehost:blocks/client_runtime_glass",
                "nativeLoaderId", "echoruntimehost:loot/blocks/client_runtime_glass",
                "standaloneRuntimeId", "echoruntimehost:blocks/client_runtime_glass",
                "metadata", Map.of(
                        "lootTableId", "echoruntimehost:blocks/client_runtime_glass",
                        "entries", List.of("echoruntimehost:native_runtime_gel"),
                        "entryCounts", Map.of("echoruntimehost:native_runtime_gel", 2)
                )
        );
    }

    private static Map<String, Object> screenRow() {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:ui/native_runtime_mods",
                "contentKind", "UI_SCREEN",
                "domain", "ui_screens",
                "displayName", "Native Runtime Mods",
                "adapterKey", "screencore.native.runtime_mods",
                "neoForgeId", "echoruntimehost:native_runtime_mods",
                "nativeLoaderId", "echoruntimehost:screen/native_runtime_mods",
                "standaloneRuntimeId", "echoruntimehost:standalone/native_runtime_mods",
                "metadata", Map.of("route", "screencore.native.runtime_mods")
        );
    }

    private static boolean optionLabel(EchoClientScreenSnapshot snapshot, String label) {
        return snapshot.options().stream()
                .anyMatch(option -> option.label().equals(label));
    }

    private static boolean optionLabelPrefix(EchoClientScreenSnapshot snapshot, String labelPrefix) {
        return snapshot.options().stream()
                .anyMatch(option -> option.label().startsWith(labelPrefix));
    }

    private static EchoClientScreenOption optionLabelPrefixMatch(
            EchoClientScreenSnapshot snapshot,
            String labelPrefix
    ) {
        return snapshot.options().stream()
                .filter(option -> option.label().startsWith(labelPrefix))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing option label prefix: " + labelPrefix));
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.delete(path);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class RecordingSettingsHost implements EchoClientSettingsController.Host {
        @Override
        public void applyInputSettings(EchoClientSettings settings) {
        }

        @Override
        public void applyAudioSettings(EchoClientSettings settings) {
        }

        @Override
        public void applyRenderSettings(int chunkViewDistance, boolean chunkViewChanged) {
        }

        @Override
        public void applyWindowSettings(boolean fullscreen, boolean vSync) {
        }

        @Override
        public void settingsSaveFailed(Path path, String error) {
        }
    }
}
