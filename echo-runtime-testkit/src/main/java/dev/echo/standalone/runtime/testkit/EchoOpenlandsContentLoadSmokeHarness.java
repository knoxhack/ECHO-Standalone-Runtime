package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.data.EchoDataTag;
import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;
import dev.echo.standalone.runtime.data.EchoSoundDefinition;
import dev.echo.standalone.runtime.data.EchoUnifiedRegistry;
import dev.echo.standalone.runtime.data.EchoWorldgenBiomeDefinition;
import dev.echo.standalone.runtime.data.EchoWorldgenStructureDefinition;
import dev.echo.standalone.runtime.item.EchoItemRegistry;
import dev.echo.standalone.runtime.world.block.state.EchoBlockRegistry;
import dev.echo.standalone.runtime.world.openlands.EchoFoundationAliasBridge;
import dev.echo.standalone.runtime.world.openlands.EchoFoundationContentLoadResult;
import dev.echo.standalone.runtime.world.openlands.EchoFoundationContentLoader;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBiomeDefinition;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBlockDefinition;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBlocks;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsContentLoader;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsCreatureDefinition;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsItemDefinition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Smoke harness for Phase B.1: load Openlands content from ECHO Native module data roots.
 *
 * <p>Expects a single command-line argument: the path to the ECHO-Modules repository root.
 * Loads Openlands blocks, items, biomes, creatures, recipes, structures, loot, tags, and sounds,
 * registers them into runtime registries, and asserts that the expected MVP content is present.
 * Writes a deterministic report under {@code reports/echo/standalone}.
 */
public final class EchoOpenlandsContentLoadSmokeHarness {

    private EchoOpenlandsContentLoadSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Expected ECHO-Modules repository root as first argument");
        }
        Path modulesRoot = Path.of(args[0]).toAbsolutePath().normalize();
        Path reportRoot = Path.of(".").toAbsolutePath().normalize().resolve("reports/echo/standalone");
        Files.createDirectories(reportRoot);

        List<Path> moduleRoots = List.of(
                modulesRoot.resolve("addons/echoopenlandsprotocol"),
                modulesRoot.resolve("addons/echomaterialcore"),
                modulesRoot.resolve("addons/echotoolcore"),
                modulesRoot.resolve("addons/echostationcore"),
                modulesRoot.resolve("addons/echoworldstarter"),
                modulesRoot.resolve("addons/echocommonloot"),
                modulesRoot.resolve("addons/echocreatureroles")
        );

        EchoOpenlandsContentLoader loader = EchoOpenlandsContentLoader.withBridge(moduleRoots);
        EchoFoundationContentLoader foundationLoader = EchoFoundationContentLoader.withBridge(moduleRoots);
        EchoFoundationContentLoadResult foundation = foundationLoader.load(moduleRoots);
        List<EchoOpenlandsBlockDefinition> blocks = loader.loadBlocksInstance(moduleRoots);
        List<EchoOpenlandsBiomeDefinition> biomes = loader.loadBiomesInstance(moduleRoots);
        List<EchoOpenlandsItemDefinition> items = loader.loadItemsInstance(moduleRoots);
        List<EchoOpenlandsCreatureDefinition> creatures = loader.loadCreaturesInstance(moduleRoots);
        List<EchoRecipeDefinition> recipes = loader.loadRecipesInstance(moduleRoots);
        List<EchoWorldgenStructureDefinition> structures = loader.loadStructuresInstance(moduleRoots);
        List<EchoLootDefinition> loot = loader.loadLootInstance(moduleRoots);
        List<EchoDataTag> tags = loader.loadTagsInstance(moduleRoots);
        List<EchoSoundDefinition> sounds = loader.loadSoundsInstance(moduleRoots);
        EchoFoundationAliasBridge aliasBridge = loader.bridge();

        EchoBlockRegistry registry = new EchoBlockRegistry();
        EchoOpenlandsBlocks.registerAll(registry, blocks);

        EchoItemRegistry itemRegistry = new EchoItemRegistry();
        EchoOpenlandsItems.registerAll(itemRegistry, items);

        EchoUnifiedRegistry unified = new EchoUnifiedRegistry();
        for (EchoOpenlandsBiomeDefinition biome : biomes) {
            unified.registerBiome(toRuntimeBiome(biome));
        }
        for (EchoRecipeDefinition recipe : recipes) {
            unified.registerRecipe(recipe);
        }
        for (EchoWorldgenStructureDefinition structure : structures) {
            unified.registerStructure(structure);
        }
        for (EchoLootDefinition lootTable : loot) {
            unified.registerLoot(lootTable);
        }
        for (EchoDataTag tag : tags) {
            unified.registerTag(tag);
        }
        for (EchoSoundDefinition sound : sounds) {
            unified.registerSound(sound);
        }
        for (EchoOpenlandsBlockDefinition block : foundation.blocks()) {
            registry.register(block.id(), block.displayName());
        }
        registry.freeze();
        for (EchoOpenlandsItemDefinition item : foundation.items()) {
            EchoOpenlandsItems.register(itemRegistry, item);
        }
        for (EchoRecipeDefinition recipe : foundation.recipes()) {
            unified.registerRecipe(recipe);
        }
        for (EchoLootDefinition lootTable : foundation.loot()) {
            unified.registerLoot(lootTable);
        }

        int checked = 0;
        int failures = 0;
        StringBuilder failureDetails = new StringBuilder();

        checked++;
        if (!hasBlock(blocks, "echoopenlandsprotocol:meadow_grass_block")) {
            failures++;
            failureDetails.append("missing meadow_grass_block; ");
        }
        checked++;
        if (!hasBlock(blocks, "echoopenlandsprotocol:forest_soil")) {
            failures++;
            failureDetails.append("missing forest_soil; ");
        }
        checked++;
        if (!hasBlock(blocks, "echoopenlandsprotocol:pine_log")) {
            failures++;
            failureDetails.append("missing pine_log; ");
        }
        checked++;
        if (!hasBlock(blocks, "echoopenlandsprotocol:limestone")) {
            failures++;
            failureDetails.append("missing limestone; ");
        }
        checked++;
        if (!registry.find("echoopenlandsprotocol:meadow_grass_block").isPresent()) {
            failures++;
            failureDetails.append("registry missing meadow_grass_block; ");
        }
        checked++;
        if (biomes.stream().noneMatch(b -> b.id().endsWith(":meadows"))) {
            failures++;
            failureDetails.append("missing meadows biome; ");
        }
        checked++;
        if (biomes.stream().noneMatch(b -> b.id().endsWith(":woodlands"))) {
            failures++;
            failureDetails.append("missing woodlands biome; ");
        }
        checked++;
        if (biomes.stream().noneMatch(b -> b.id().endsWith(":stonehills"))) {
            failures++;
            failureDetails.append("missing stonehills biome; ");
        }
        checked++;
        if (biomes.stream().noneMatch(b -> b.id().endsWith(":marshlands"))) {
            failures++;
            failureDetails.append("missing marshlands biome; ");
        }
        checked++;
        if (items.size() < 20) {
            failures++;
            failureDetails.append("items=").append(items.size()).append(" expected>=20; ");
        }
        checked++;
        if (itemRegistry.find(new dev.echo.standalone.runtime.item.EchoItemId("echoopenlandsprotocol:berries")).isEmpty()) {
            failures++;
            failureDetails.append("registry missing berries item; ");
        }
        checked++;
        if (aliasBridge.aliases().isEmpty()) {
            failures++;
            failureDetails.append("alias bridge empty; ");
        }
        checked++;
        if (!"echoworldstarter:pitchlight".equals(aliasBridge.resolve("echoopenlandsprotocol:torch"))) {
            failures++;
            failureDetails.append("torch alias missing; ");
        }
        checked++;
        if (creatures.size() < 10) {
            failures++;
            failureDetails.append("creatures=").append(creatures.size()).append(" expected>=10; ");
        }
        checked++;
        if (recipes.isEmpty()) {
            failures++;
            failureDetails.append("no recipes loaded; ");
        }
        checked++;
        if (structures.isEmpty()) {
            failures++;
            failureDetails.append("no structures loaded; ");
        }
        checked++;
        if (tags.isEmpty()) {
            failures++;
            failureDetails.append("no tags loaded; ");
        }
        checked++;
        if (sounds.isEmpty()) {
            failures++;
            failureDetails.append("no sounds loaded; ");
        }
        checked++;
        if (foundation.blocks().isEmpty()) {
            failures++;
            failureDetails.append("no foundation blocks loaded; ");
        }
        checked++;
        if (foundation.items().isEmpty()) {
            failures++;
            failureDetails.append("no foundation items loaded; ");
        }
        checked++;
        if (foundation.recipes().isEmpty()) {
            failures++;
            failureDetails.append("no foundation recipes loaded; ");
        }
        checked++;
        if (foundation.stations().isEmpty()) {
            failures++;
            failureDetails.append("no foundation stations loaded; ");
        }
        checked++;
        if (!hasBlock(foundation.blocks(), "echomaterialcore:fieldstone")) {
            failures++;
            failureDetails.append("missing foundation fieldstone block; ");
        }
        checked++;
        if (!hasBlock(foundation.blocks(), "echoworldstarter:pitchlight")) {
            failures++;
            failureDetails.append("missing foundation pitchlight block; ");
        }
        checked++;
        if (!hasItem(foundation.items(), "echomaterialcore:cupral_bar")) {
            failures++;
            failureDetails.append("missing foundation cupral_bar item; ");
        }
        checked++;
        if (foundation.loot().isEmpty()) {
            failures++;
            failureDetails.append("no foundation loot loaded; ");
        }
        checked++;
        if (foundation.creatureRoleMappings().isEmpty()) {
            failures++;
            failureDetails.append("no foundation creature role mappings loaded; ");
        }

        boolean pass = failures == 0;
        writeReport(reportRoot, pass, blocks.size(), biomes.size(), items.size(), aliasBridge.aliases().size(),
                creatures.size(), recipes.size(), structures.size(), loot.size(), tags.size(), sounds.size(),
                foundation.blocks().size(), foundation.items().size(), foundation.recipes().size(),
                foundation.stations().size(), foundation.loot().size(), foundation.creatureRoleMappings().size(),
                checked, failures, failureDetails.length() > 0 ? failureDetails.toString() : "");

        if (!pass) {
            throw new AssertionError("Openlands content load smoke failed: " + failureDetails);
        }

        System.out.println("openlands content load smoke PASS blocks=" + blocks.size()
                + " biomes=" + biomes.size() + " items=" + items.size()
                + " aliases=" + aliasBridge.aliases().size()
                + " creatures=" + creatures.size() + " recipes=" + recipes.size()
                + " structures=" + structures.size() + " loot=" + loot.size()
                + " tags=" + tags.size() + " sounds=" + sounds.size()
                + " foundationBlocks=" + foundation.blocks().size()
                + " foundationItems=" + foundation.items().size()
                + " foundationRecipes=" + foundation.recipes().size()
                + " foundationStations=" + foundation.stations().size()
                + " foundationLoot=" + foundation.loot().size()
                + " foundationRoles=" + foundation.creatureRoleMappings().size()
                + " checked=" + checked);
    }

    private static EchoWorldgenBiomeDefinition toRuntimeBiome(EchoOpenlandsBiomeDefinition biome) {
        Map<String, String> hints = new LinkedHashMap<>();
        hints.put("temperatureCategory", biome.temperature());
        hints.put("humidityCategory", biome.humidity());
        hints.put("terrainProfile", biome.terrainProfile());
        return new EchoWorldgenBiomeDefinition(
                biome.id(),
                biome.displayName(),
                temperatureValue(biome.temperature()),
                humidityValue(biome.humidity()),
                0xC6D6E8,
                0x88BB66,
                "",
                List.of(),
                List.copyOf(biome.resourceSet()),
                hints,
                "openlands:biomes/mvp_biomes.json"
        );
    }

    private static double temperatureValue(String temperature) {
        return switch (temperature.toLowerCase()) {
            case "frozen" -> -0.8D;
            case "cool" -> -0.3D;
            case "mild" -> 0.0D;
            case "warm" -> 0.5D;
            case "hot" -> 0.9D;
            default -> 0.0D;
        };
    }

    private static double humidityValue(String humidity) {
        return switch (humidity.toLowerCase()) {
            case "dry" -> 0.0D;
            case "normal" -> 0.4D;
            case "damp" -> 0.7D;
            case "wet" -> 1.0D;
            default -> 0.4D;
        };
    }

    private static boolean hasBlock(List<EchoOpenlandsBlockDefinition> blocks, String id) {
        return blocks.stream().anyMatch(b -> b.id().equals(id));
    }

    private static boolean hasItem(List<EchoOpenlandsItemDefinition> items, String id) {
        return items.stream().anyMatch(i -> i.id().equals(id));
    }

    private static void writeReport(Path root, boolean pass, int blocks, int biomes, int items,
                                    int aliases, int creatures, int recipes, int structures, int loot,
                                    int tags, int sounds, int foundationBlocks, int foundationItems,
                                    int foundationRecipes, int foundationStations, int foundationLoot,
                                    int foundationRoles, int checked, int failures,
                                    String failureDetails) throws IOException {
        Path path = root.resolve("openlands-content-load.json");
        String status = pass ? "PASS" : "FAIL";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"schema\": \"echo.standalone.openlands_content_load.v1\",\n");
        sb.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        sb.append("  \"status\": \"").append(status).append("\",\n");
        sb.append("  \"blocksLoaded\": ").append(blocks).append(",\n");
        sb.append("  \"biomesLoaded\": ").append(biomes).append(",\n");
        sb.append("  \"itemsLoaded\": ").append(items).append(",\n");
        sb.append("  \"aliasesLoaded\": ").append(aliases).append(",\n");
        sb.append("  \"creaturesLoaded\": ").append(creatures).append(",\n");
        sb.append("  \"recipesLoaded\": ").append(recipes).append(",\n");
        sb.append("  \"structuresLoaded\": ").append(structures).append(",\n");
        sb.append("  \"lootTablesLoaded\": ").append(loot).append(",\n");
        sb.append("  \"tagsLoaded\": ").append(tags).append(",\n");
        sb.append("  \"soundsLoaded\": ").append(sounds).append(",\n");
        sb.append("  \"foundationBlocksLoaded\": ").append(foundationBlocks).append(",\n");
        sb.append("  \"foundationItemsLoaded\": ").append(foundationItems).append(",\n");
        sb.append("  \"foundationRecipesLoaded\": ").append(foundationRecipes).append(",\n");
        sb.append("  \"foundationStationsLoaded\": ").append(foundationStations).append(",\n");
        sb.append("  \"foundationLootLoaded\": ").append(foundationLoot).append(",\n");
        sb.append("  \"foundationCreatureRolesLoaded\": ").append(foundationRoles).append(",\n");
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
