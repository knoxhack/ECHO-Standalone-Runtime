package dev.echo.standalone.runtime.world.openlands;

import dev.echo.standalone.runtime.data.EchoDataTag;
import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;
import dev.echo.standalone.runtime.data.EchoSoundDefinition;
import dev.echo.standalone.runtime.data.EchoWorldgenStructureDefinition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Loads Openlands content definitions from ECHO module data roots.
 *
 * <p>Supports both source trees ({@code src/main/resources/data/<namespace>/openlands/}) and
 * built modules ({@code build/resources/main/data/<namespace>/openlands/}).
 *
 * <p>The loader optionally holds an {@link EchoFoundationAliasBridge} that rewrites legacy
 * Openlands IDs to canonical Foundation IDs at parse time. Static convenience methods use an
 * empty bridge for backward compatibility.
 */
public final class EchoOpenlandsContentLoader {

    private static final List<String> DATA_ROOT_CANDIDATES = List.of(
            "src/main/resources/data",
            "build/resources/main/data"
    );

    private final EchoFoundationAliasBridge bridge;

    public EchoOpenlandsContentLoader() {
        this(EchoFoundationAliasBridge.empty());
    }

    public EchoOpenlandsContentLoader(EchoFoundationAliasBridge bridge) {
        this.bridge = bridge == null ? EchoFoundationAliasBridge.empty() : bridge;
    }

    /**
     * Returns the alias bridge used by this loader.
     */
    public EchoFoundationAliasBridge bridge() {
        return bridge;
    }

    /**
     * Creates a loader using the alias bridge loaded from the provided module roots.
     */
    public static EchoOpenlandsContentLoader withBridge(List<Path> moduleRoots) throws IOException {
        return new EchoOpenlandsContentLoader(EchoFoundationAliasBridge.load(moduleRoots));
    }

    /**
     * Loads Openlands blocks from all provided module roots.
     */
    public static List<EchoOpenlandsBlockDefinition> loadBlocks(List<Path> moduleRoots) throws IOException {
        return new EchoOpenlandsContentLoader().loadBlocksInstance(moduleRoots);
    }

    /**
     * Loads Openlands biomes from all provided module roots.
     */
    public static List<EchoOpenlandsBiomeDefinition> loadBiomes(List<Path> moduleRoots) throws IOException {
        return new EchoOpenlandsContentLoader().loadBiomesInstance(moduleRoots);
    }

    /**
     * Loads Openlands items from all provided module roots.
     */
    public static List<EchoOpenlandsItemDefinition> loadItems(List<Path> moduleRoots) throws IOException {
        return new EchoOpenlandsContentLoader().loadItemsInstance(moduleRoots);
    }

    /**
     * Loads Openlands creatures from all provided module roots.
     */
    public static List<EchoOpenlandsCreatureDefinition> loadCreatures(List<Path> moduleRoots) throws IOException {
        return new EchoOpenlandsContentLoader().loadCreaturesInstance(moduleRoots);
    }

    /**
     * Loads Openlands recipes as runtime recipe definitions.
     */
    public static List<EchoRecipeDefinition> loadRecipes(List<Path> moduleRoots) throws IOException {
        return new EchoOpenlandsContentLoader().loadRecipesInstance(moduleRoots);
    }

    /**
     * Loads Openlands landmarks as runtime structure definitions.
     */
    public static List<EchoWorldgenStructureDefinition> loadStructures(List<Path> moduleRoots) throws IOException {
        return new EchoOpenlandsContentLoader().loadStructuresInstance(moduleRoots);
    }

    /**
     * Loads Openlands loot tables as runtime loot definitions.
     */
    public static List<EchoLootDefinition> loadLoot(List<Path> moduleRoots) throws IOException {
        return new EchoOpenlandsContentLoader().loadLootInstance(moduleRoots);
    }

    /**
     * Loads Openlands tags as runtime tag definitions.
     */
    public static List<EchoDataTag> loadTags(List<Path> moduleRoots) throws IOException {
        return new EchoOpenlandsContentLoader().loadTagsInstance(moduleRoots);
    }

    /**
     * Loads Openlands sounds as runtime sound definitions.
     */
    public static List<EchoSoundDefinition> loadSounds(List<Path> moduleRoots) throws IOException {
        return new EchoOpenlandsContentLoader().loadSoundsInstance(moduleRoots);
    }

    public List<EchoOpenlandsBlockDefinition> loadBlocksInstance(List<Path> moduleRoots) throws IOException {
        List<EchoOpenlandsBlockDefinition> blocks = new ArrayList<>();
        for (Path root : moduleRoots) {
            for (Path dataRoot : dataRoots(root)) {
                Path blocksDir = dataRoot.resolve("echoopenlandsprotocol/openlands/blocks");
                if (!Files.isDirectory(blocksDir)) {
                    continue;
                }
                try (var stream = Files.walk(blocksDir)) {
                    for (Path path : stream.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                        blocks.addAll(parseBlocks(readJson(path)));
                    }
                }
            }
        }
        return List.copyOf(blocks);
    }

    public List<EchoOpenlandsBiomeDefinition> loadBiomesInstance(List<Path> moduleRoots) throws IOException {
        List<EchoOpenlandsBiomeDefinition> biomes = new ArrayList<>();
        for (Path root : moduleRoots) {
            for (Path dataRoot : dataRoots(root)) {
                Path biomesDir = dataRoot.resolve("echoopenlandsprotocol/openlands/biomes");
                if (!Files.isDirectory(biomesDir)) {
                    continue;
                }
                try (var stream = Files.walk(biomesDir)) {
                    for (Path path : stream.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                        biomes.addAll(parseBiomes(readJson(path)));
                    }
                }
            }
        }
        return List.copyOf(biomes);
    }

    public List<EchoOpenlandsItemDefinition> loadItemsInstance(List<Path> moduleRoots) throws IOException {
        List<EchoOpenlandsItemDefinition> items = new ArrayList<>();
        for (Path root : moduleRoots) {
            for (Path dataRoot : dataRoots(root)) {
                Path itemsDir = dataRoot.resolve("echoopenlandsprotocol/openlands/items");
                if (!Files.isDirectory(itemsDir)) {
                    continue;
                }
                try (var stream = Files.walk(itemsDir)) {
                    for (Path path : stream.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                        items.addAll(parseItems(readJson(path)));
                    }
                }
            }
        }
        return List.copyOf(items);
    }

    public List<EchoOpenlandsCreatureDefinition> loadCreaturesInstance(List<Path> moduleRoots) throws IOException {
        List<EchoOpenlandsCreatureDefinition> creatures = new ArrayList<>();
        for (Path root : moduleRoots) {
            for (Path dataRoot : dataRoots(root)) {
                Path creaturesDir = dataRoot.resolve("echoopenlandsprotocol/openlands/creatures");
                if (!Files.isDirectory(creaturesDir)) {
                    continue;
                }
                try (var stream = Files.walk(creaturesDir)) {
                    for (Path path : stream.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                        creatures.addAll(parseCreatures(readJson(path)));
                    }
                }
            }
        }
        return List.copyOf(creatures);
    }

    public List<EchoRecipeDefinition> loadRecipesInstance(List<Path> moduleRoots) throws IOException {
        List<EchoRecipeDefinition> recipes = new ArrayList<>();
        for (Path root : moduleRoots) {
            for (Path dataRoot : dataRoots(root)) {
                Path recipesDir = dataRoot.resolve("echoopenlandsprotocol/openlands/recipes");
                if (!Files.isDirectory(recipesDir)) {
                    continue;
                }
                try (var stream = Files.walk(recipesDir)) {
                    for (Path path : stream.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                        recipes.addAll(parseRecipes(readJson(path), path.toString()));
                    }
                }
            }
        }
        return List.copyOf(recipes);
    }

    public List<EchoWorldgenStructureDefinition> loadStructuresInstance(List<Path> moduleRoots) throws IOException {
        List<EchoWorldgenStructureDefinition> structures = new ArrayList<>();
        for (Path root : moduleRoots) {
            for (Path dataRoot : dataRoots(root)) {
                Path structuresDir = dataRoot.resolve("echoopenlandsprotocol/openlands/structures");
                if (!Files.isDirectory(structuresDir)) {
                    continue;
                }
                try (var stream = Files.walk(structuresDir)) {
                    for (Path path : stream.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                        structures.addAll(parseStructures(readJson(path), path.toString()));
                    }
                }
            }
        }
        return List.copyOf(structures);
    }

    public List<EchoLootDefinition> loadLootInstance(List<Path> moduleRoots) throws IOException {
        List<EchoLootDefinition> loot = new ArrayList<>();
        for (Path root : moduleRoots) {
            for (Path dataRoot : dataRoots(root)) {
                Path lootDir = dataRoot.resolve("echoopenlandsprotocol/openlands/loot");
                if (!Files.isDirectory(lootDir)) {
                    continue;
                }
                try (var stream = Files.walk(lootDir)) {
                    for (Path path : stream.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                        loot.addAll(parseLoot(readJson(path), path.toString()));
                    }
                }
            }
        }
        return List.copyOf(loot);
    }

    public List<EchoDataTag> loadTagsInstance(List<Path> moduleRoots) throws IOException {
        List<EchoDataTag> tags = new ArrayList<>();
        for (Path root : moduleRoots) {
            for (Path dataRoot : dataRoots(root)) {
                Path tagsDir = dataRoot.resolve("echoopenlandsprotocol/openlands/tags");
                if (!Files.isDirectory(tagsDir)) {
                    continue;
                }
                try (var stream = Files.walk(tagsDir)) {
                    for (Path path : stream.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                        tags.addAll(parseTags(readJson(path), path.toString()));
                    }
                }
            }
        }
        return List.copyOf(tags);
    }

    public List<EchoSoundDefinition> loadSoundsInstance(List<Path> moduleRoots) throws IOException {
        List<EchoSoundDefinition> sounds = new ArrayList<>();
        for (Path root : moduleRoots) {
            for (Path dataRoot : dataRoots(root)) {
                Path soundsDir = dataRoot.resolve("echoopenlandsprotocol/openlands/sounds");
                if (!Files.isDirectory(soundsDir)) {
                    continue;
                }
                try (var stream = Files.walk(soundsDir)) {
                    for (Path path : stream.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                        sounds.addAll(parseSounds(readJson(path), path.toString()));
                    }
                }
            }
        }
        return List.copyOf(sounds);
    }

    private List<Path> dataRoots(Path moduleRoot) {
        for (String candidate : DATA_ROOT_CANDIDATES) {
            Path path = moduleRoot.resolve(candidate).normalize();
            if (Files.isDirectory(path)) {
                return List.of(path);
            }
        }
        return List.of();
    }

    private String readJson(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private List<EchoOpenlandsBlockDefinition> parseBlocks(String json) {
        Map<String, Object> root = parseObject(json);
        String namespace = string(root, "namespace");
        List<Map<String, Object>> blocks = (List<Map<String, Object>>) root.get("blocks");
        if (blocks == null) {
            return List.of();
        }
        List<EchoOpenlandsBlockDefinition> result = new ArrayList<>();
        for (Map<String, Object> object : blocks) {
            result.add(new EchoOpenlandsBlockDefinition(
                    resolve(string(object, "id"), namespace),
                    string(object, "displayName"),
                    string(object, "category"),
                    doubleValue(object.get("hardness")),
                    string(object, "tool"),
                    resolveDrops(object.get("drops")),
                    resolveAll(stringList(object, "tags"), namespace),
                    string(object, "model"),
                    string(object, "texture"),
                    resolveAll(stringList(object, "biomePlacement"), namespace),
                    object
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoOpenlandsBiomeDefinition> parseBiomes(String json) {
        Map<String, Object> root = parseObject(json);
        String namespace = string(root, "namespace");
        List<Map<String, Object>> biomes = (List<Map<String, Object>>) root.get("biomes");
        if (biomes == null) {
            return List.of();
        }
        List<EchoOpenlandsBiomeDefinition> result = new ArrayList<>();
        for (Map<String, Object> object : biomes) {
            Map<String, Object> palette = object.get("blockPalette") instanceof Map<?, ?> map
                    ? (Map<String, Object>) map : Map.of();
            result.add(new EchoOpenlandsBiomeDefinition(
                    resolve(string(object, "id"), namespace),
                    string(object, "displayName"),
                    string(object, "temperature"),
                    string(object, "humidity"),
                    string(object, "terrainProfile"),
                    new EchoOpenlandsBiomeDefinition.EchoOpenlandsBlockPalette(
                            resolvePaletteValue(palette.get("surface"), namespace),
                            resolvePaletteValue(palette.get("subsurface"), namespace),
                            resolvePaletteValue(palette.get("stone"), namespace),
                            resolveAll(stringList(palette, "treeFamilies"), namespace)
                    ),
                    resolveAll(stringList(object, "resourceSet"), namespace),
                    object
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoOpenlandsItemDefinition> parseItems(String json) {
        Map<String, Object> root = parseObject(json);
        String namespace = string(root, "namespace");
        List<Map<String, Object>> items = (List<Map<String, Object>>) root.get("items");
        if (items == null) {
            return List.of();
        }
        List<EchoOpenlandsItemDefinition> result = new ArrayList<>();
        for (Map<String, Object> object : items) {
            List<String> tooltipLines = new ArrayList<>();
            String notes = string(object, "notes");
            if (!notes.isEmpty()) {
                tooltipLines.add(notes);
            }
            result.add(new EchoOpenlandsItemDefinition(
                    resolve(string(object, "id"), namespace),
                    string(object, "displayName"),
                    string(object, "useType"),
                    intValue(object.get("stackSize")),
                    resolveAll(stringList(object, "tags"), namespace),
                    string(object, "model"),
                    string(object, "texture"),
                    resolveAll(stringList(object, "recipeRefs"), namespace),
                    List.copyOf(tooltipLines),
                    object
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoOpenlandsCreatureDefinition> parseCreatures(String json) {
        Map<String, Object> root = parseObject(json);
        String namespace = string(root, "namespace");
        List<Map<String, Object>> creatures = (List<Map<String, Object>>) root.get("creatures");
        if (creatures == null) {
            return List.of();
        }
        List<EchoOpenlandsCreatureDefinition> result = new ArrayList<>();
        for (Map<String, Object> object : creatures) {
            result.add(new EchoOpenlandsCreatureDefinition(
                    resolve(string(object, "id"), namespace),
                    string(object, "displayName"),
                    string(object, "category"),
                    resolveAll(stringList(object, "biomes"), namespace),
                    intValue(object.get("health")),
                    intValue(object.get("damage")),
                    string(object, "notes"),
                    object
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoRecipeDefinition> parseRecipes(String json, String sourceLogicalId) {
        Map<String, Object> root = parseObject(json);
        String namespace = string(root, "namespace");
        List<Map<String, Object>> recipes = (List<Map<String, Object>>) root.get("recipes");
        if (recipes == null) {
            return List.of();
        }
        List<EchoRecipeDefinition> result = new ArrayList<>();
        for (Map<String, Object> object : recipes) {
            String id = resolve(string(object, "id"), namespace);
            List<String> ingredients = new ArrayList<>();
            for (Map<String, Object> input : (List<Map<String, Object>>) object.getOrDefault("inputs", List.of())) {
                String key = input.get("block") != null ? "block" : (input.get("item") != null ? "item" : "tag");
                String value = string(input, key);
                if (!value.isEmpty()) {
                    int count = intValue(input.get("count"));
                    String resolved = resolve(value, namespace);
                    for (int i = 0; i < count; i++) {
                        ingredients.add(resolved);
                    }
                }
            }
            String resultId = "";
            int resultCount = 1;
            for (Map<String, Object> output : (List<Map<String, Object>>) object.getOrDefault("outputs", List.of())) {
                String key = output.get("block") != null ? "block" : "item";
                String value = string(output, key);
                if (!value.isEmpty()) {
                    resultId = resolve(value, namespace);
                    resultCount = intValue(output.get("count"));
                    break;
                }
            }
            if (!resultId.isEmpty()) {
                result.add(new EchoRecipeDefinition(id, "openlands", ingredients, resultId, sourceLogicalId));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoWorldgenStructureDefinition> parseStructures(String json, String sourceLogicalId) {
        Map<String, Object> root = parseObject(json);
        String namespace = string(root, "namespace");
        List<Map<String, Object>> landmarks = (List<Map<String, Object>>) root.get("landmarks");
        if (landmarks == null) {
            return List.of();
        }
        List<EchoWorldgenStructureDefinition> result = new ArrayList<>();
        for (Map<String, Object> object : landmarks) {
            String id = resolve(string(object, "id"), namespace);
            Map<String, String> hints = new LinkedHashMap<>();
            hints.put("displayName", string(object, "displayName"));
            hints.put("holoMapHint", string(object, "holoMapHint"));
            hints.put("tutorialHook", string(object, "tutorialHook"));
            Map<String, Object> footprint = object.get("footprint") instanceof Map<?, ?> map
                    ? (Map<String, Object>) map : Map.of();
            hints.put("footprintWidth", String.valueOf(intValue(footprint.get("width"))));
            hints.put("footprintDepth", String.valueOf(intValue(footprint.get("depth"))));
            hints.put("footprintHeight", String.valueOf(intValue(footprint.get("height"))));
            result.add(new EchoWorldgenStructureDefinition(
                    id,
                    "openlands:landmark",
                    resolveAll(stringList(object, "preferredBiomes"), namespace),
                    hints,
                    sourceLogicalId
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoLootDefinition> parseLoot(String json, String sourceLogicalId) {
        Map<String, Object> root = parseObject(json);
        String namespace = string(root, "namespace");
        List<EchoLootDefinition> result = new ArrayList<>();

        List<Map<String, Object>> blockDrops = (List<Map<String, Object>>) root.get("blockDrops");
        if (blockDrops != null) {
            for (Map<String, Object> object : blockDrops) {
                String id = resolve(string(object, "block"), namespace) + "_drops";
                List<String> entries = new ArrayList<>();
                for (Map<String, Object> drop : (List<Map<String, Object>>) object.getOrDefault("drops", List.of())) {
                    String key = drop.get("block") != null ? "block" : "item";
                    entries.add(resolve(string(drop, key), namespace));
                }
                result.add(new EchoLootDefinition(id, entries, sourceLogicalId));
            }
        }

        List<Map<String, Object>> creatureDrops = (List<Map<String, Object>>) root.get("creatureDrops");
        if (creatureDrops != null) {
            for (Map<String, Object> object : creatureDrops) {
                String id = resolve(string(object, "creature"), namespace) + "_drops";
                List<String> entries = new ArrayList<>();
                for (Map<String, Object> drop : (List<Map<String, Object>>) object.getOrDefault("drops", List.of())) {
                    entries.add(resolve(string(drop, "item"), namespace));
                }
                result.add(new EchoLootDefinition(id, entries, sourceLogicalId));
            }
        }

        List<Map<String, Object>> chestTables = (List<Map<String, Object>>) root.get("chestTables");
        if (chestTables != null) {
            for (Map<String, Object> object : chestTables) {
                String id = resolve(string(object, "id"), namespace);
                List<String> entries = new ArrayList<>();
                for (Map<String, Object> entry : (List<Map<String, Object>>) object.getOrDefault("entries", List.of())) {
                    entries.add(resolve(string(entry, "item"), namespace));
                }
                result.add(new EchoLootDefinition(id, entries, sourceLogicalId));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoDataTag> parseTags(String json, String sourceLogicalId) {
        Map<String, Object> root = parseObject(json);
        String namespace = string(root, "namespace");
        List<EchoDataTag> result = new ArrayList<>();

        Map<String, List<String>> blockTags = (Map<String, List<String>>) root.get("blockTags");
        if (blockTags != null) {
            for (Map.Entry<String, List<String>> entry : blockTags.entrySet()) {
                String tagId = resolve(entry.getKey(), namespace);
                List<String> values = entry.getValue().stream()
                        .map(v -> resolve(v, namespace))
                        .toList();
                result.add(new EchoDataTag(tagId, "block", values, sourceLogicalId));
            }
        }

        Map<String, List<String>> itemTags = (Map<String, List<String>>) root.get("itemTags");
        if (itemTags != null) {
            for (Map.Entry<String, List<String>> entry : itemTags.entrySet()) {
                String tagId = resolve(entry.getKey(), namespace);
                List<String> values = entry.getValue().stream()
                        .map(v -> resolve(v, namespace))
                        .toList();
                result.add(new EchoDataTag(tagId, "item", values, sourceLogicalId));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoSoundDefinition> parseSounds(String json, String sourceLogicalId) {
        Map<String, Object> root = parseObject(json);
        String namespace = string(root, "namespace");
        List<EchoSoundDefinition> result = new ArrayList<>();

        List<Map<String, Object>> families = (List<Map<String, Object>>) root.get("soundFamilies");
        if (families == null) {
            return result;
        }
        for (Map<String, Object> family : families) {
            String id = resolve(string(family, "id"), namespace);
            List<String> assets = new ArrayList<>();
            String assetKey = string(family, "assetKey");
            if (!assetKey.isEmpty()) {
                assets.add(assetKey);
            }
            result.add(new EchoSoundDefinition(id, "", assets, sourceLogicalId));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoOpenlandsBlockDefinition.EchoOpenlandsDrop> resolveDrops(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<EchoOpenlandsBlockDefinition.EchoOpenlandsDrop> drops = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Map<String, Object> object = (Map<String, Object>) map;
                String namespace = string(object, "namespace");
                drops.add(new EchoOpenlandsBlockDefinition.EchoOpenlandsDrop(
                        resolve(string(object, "item"), namespace),
                        intValue(object.get("count")),
                        resolve(string(object, "fallback"), namespace)
                ));
            }
        }
        return drops;
    }

    private Object resolvePaletteValue(Object value, String namespace) {
        if (value instanceof String s) {
            return resolve(s, namespace);
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(item -> item instanceof String s ? resolve(s, namespace) : item)
                    .toList();
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseObject(String json) {
        Object value = dev.echo.standalone.runtime.data.EchoDataJson.parse(json);
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("JSON root must be an object");
        }
        return (Map<String, Object>) map;
    }

    private String resolve(String id, String namespace) {
        if (id == null || id.isBlank()) {
            return "";
        }
        String namespaced = id.contains(":") ? id : (namespace == null || namespace.isBlank() ? "echoopenlandsprotocol" : namespace) + ":" + id;
        return bridge.resolve(namespaced);
    }

    private List<String> resolveAll(List<String> ids, String namespace) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().map(id -> resolve(id, namespace)).toList();
    }

    private String string(Map<String, Object> object, String key) {
        Object value = object.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 1;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private double doubleValue(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0.0D;
        }
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (NumberFormatException ignored) {
            return 0.0D;
        }
    }
}
