package dev.echo.standalone.runtime.world.openlands;

import dev.echo.standalone.runtime.data.EchoDataJson;
import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Loads Foundation module payloads that contain content moved from Openlands.
 *
 * <p>Scans {@code data/<namespace>/foundation/.../*.json} in each module root, filters files by the
 * {@code echo.foundation.moved_openlands_(name).v1} schema, sorts them by {@code movedOrder}, and
 * parses canonical block/item/recipe/station/loot/creature-role definitions. Short IDs inside
 * payloads are namespaced using the file's {@code source} field, then aliases from the Openlands
 * bridge are applied so that legacy references resolve to canonical Foundation IDs.
 */
public final class EchoFoundationContentLoader {

    private static final List<String> DATA_ROOT_CANDIDATES = List.of(
            "src/main/resources/data",
            "build/resources/main/data"
    );

    private final EchoFoundationAliasBridge bridge;

    public EchoFoundationContentLoader() {
        this(EchoFoundationAliasBridge.empty());
    }

    public EchoFoundationContentLoader(EchoFoundationAliasBridge bridge) {
        this.bridge = bridge == null ? EchoFoundationAliasBridge.empty() : bridge;
    }

    /**
     * Creates a loader using the alias bridge loaded from the provided module roots.
     */
    public static EchoFoundationContentLoader withBridge(List<Path> moduleRoots) throws IOException {
        return new EchoFoundationContentLoader(EchoFoundationAliasBridge.load(moduleRoots));
    }

    /**
     * Loads all Foundation payloads found in the module roots.
     */
    public EchoFoundationContentLoadResult load(List<Path> moduleRoots) throws IOException {
        List<FoundFile> found = scan(moduleRoots);
        List<EchoOpenlandsBlockDefinition> blocks = new ArrayList<>();
        List<EchoOpenlandsItemDefinition> items = new ArrayList<>();
        List<EchoRecipeDefinition> recipes = new ArrayList<>();
        List<EchoFoundationStationDefinition> stations = new ArrayList<>();
        List<EchoLootDefinition> loot = new ArrayList<>();
        List<EchoFoundationCreatureRoleMapping> creatureRoles = new ArrayList<>();
        List<EchoFoundationContentSource> sources = new ArrayList<>();

        for (FoundFile file : found) {
            String json = Files.readString(file.path, StandardCharsets.UTF_8);
            Map<String, Object> root = parseObject(json);
            String schema = string(root, "schema");
            if (!isFoundationPayloadSchema(schema)) {
                continue;
            }
            int movedOrder = intValue(root.get("movedOrder"));
            String sourceNs = string(root, "source");
            String canonicalOwner = string(root, "canonicalOwner");
            sources.add(new EchoFoundationContentSource(file.path, schema, movedOrder, sourceNs, canonicalOwner));

            blocks.addAll(parseBlocks(root, sourceNs, file.path.toString()));
            items.addAll(parseItems(root, sourceNs, file.path.toString()));
            recipes.addAll(parseRecipes(root, sourceNs, file.path.toString()));
            stations.addAll(parseStations(root, sourceNs, file.path.toString()));
            loot.addAll(parseLoot(root, sourceNs, file.path.toString()));
            creatureRoles.addAll(parseCreatureRoles(root, sourceNs));
        }

        return new EchoFoundationContentLoadResult(blocks, items, recipes, stations, loot, creatureRoles, sources);
    }

    private List<FoundFile> scan(List<Path> moduleRoots) throws IOException {
        List<FoundFile> found = new ArrayList<>();
        for (Path moduleRoot : moduleRoots) {
            for (Path dataRoot : dataRoots(moduleRoot)) {
                Path foundationRoot = dataRoot;
                if (!Files.isDirectory(foundationRoot)) {
                    continue;
                }
                try (var stream = Files.walk(foundationRoot)) {
                    for (Path path : stream.filter(p -> p.toString().endsWith(".json")).toList()) {
                        if (!path.toString().replace('\\', '/').contains("/foundation/")) {
                            continue;
                        }
                        FoundFile candidate = readHeader(path);
                        if (candidate != null) {
                            found.add(candidate);
                        }
                    }
                }
            }
        }
        found.sort(Comparator.comparingInt((FoundFile f) -> f.movedOrder).thenComparing(f -> f.path.toString()));
        return found;
    }

    @SuppressWarnings("unchecked")
    private FoundFile readHeader(Path path) throws IOException {
        String json = Files.readString(path, StandardCharsets.UTF_8);
        try {
            Map<String, Object> root = parseObject(json);
            String schema = string(root, "schema");
            if (!isFoundationPayloadSchema(schema)) {
                return null;
            }
            return new FoundFile(path, schema, intValue(root.get("movedOrder")));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isFoundationPayloadSchema(String schema) {
        return schema != null && schema.startsWith("echo.foundation.moved_openlands_") && schema.endsWith(".v1");
    }

    @SuppressWarnings("unchecked")
    private List<EchoOpenlandsBlockDefinition> parseBlocks(Map<String, Object> root, String sourceNs, String sourceLogicalId) {
        List<Map<String, Object>> blocks = (List<Map<String, Object>>) root.get("blocks");
        if (blocks == null) {
            return List.of();
        }
        List<EchoOpenlandsBlockDefinition> result = new ArrayList<>();
        for (Map<String, Object> object : blocks) {
            result.add(new EchoOpenlandsBlockDefinition(
                    resolve(string(object, "id"), sourceNs),
                    string(object, "displayName"),
                    string(object, "category"),
                    doubleValue(object.get("hardness")),
                    string(object, "tool"),
                    resolveDrops(object.get("drops"), sourceNs),
                    resolveAll(stringList(object, "tags"), sourceNs),
                    string(object, "model"),
                    string(object, "texture"),
                    resolveAll(stringList(object, "biomePlacement"), sourceNs),
                    object
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoOpenlandsItemDefinition> parseItems(Map<String, Object> root, String sourceNs, String sourceLogicalId) {
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
                    resolve(string(object, "id"), sourceNs),
                    string(object, "displayName"),
                    string(object, "useType"),
                    intValue(object.get("stackSize")),
                    resolveAll(stringList(object, "tags"), sourceNs),
                    string(object, "model"),
                    string(object, "texture"),
                    resolveAll(stringList(object, "recipeRefs"), sourceNs),
                    List.copyOf(tooltipLines),
                    object
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoRecipeDefinition> parseRecipes(Map<String, Object> root, String sourceNs, String sourceLogicalId) {
        List<Map<String, Object>> recipes = (List<Map<String, Object>>) root.get("recipes");
        if (recipes == null) {
            return List.of();
        }
        List<EchoRecipeDefinition> result = new ArrayList<>();
        for (Map<String, Object> object : recipes) {
            String id = resolve(string(object, "id"), sourceNs);
            List<String> ingredients = new ArrayList<>();
            for (Map<String, Object> input : (List<Map<String, Object>>) object.getOrDefault("inputs", List.of())) {
                String key = input.get("block") != null ? "block"
                        : (input.get("item") != null ? "item" : (input.get("tag") != null ? "tag" : null));
                if (key == null) {
                    continue;
                }
                String value = string(input, key);
                if (!value.isEmpty()) {
                    int count = intValue(input.get("count"));
                    String resolved = resolve(value, sourceNs);
                    for (int i = 0; i < count; i++) {
                        ingredients.add(resolved);
                    }
                }
            }
            String resultId = "";
            int resultCount = 1;
            for (Map<String, Object> output : (List<Map<String, Object>>) object.getOrDefault("outputs", List.of())) {
                if (output.get("tagOutput") != null) {
                    continue;
                }
                String key = output.get("block") != null ? "block" : "item";
                String value = string(output, key);
                if (!value.isEmpty()) {
                    resultId = resolve(value, sourceNs);
                    resultCount = intValue(output.get("count"));
                    break;
                }
            }
            if (!resultId.isEmpty()) {
                result.add(new EchoRecipeDefinition(id, "foundation", ingredients, resultId, sourceLogicalId));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoFoundationStationDefinition> parseStations(Map<String, Object> root, String sourceNs, String sourceLogicalId) {
        List<Map<String, Object>> stations = (List<Map<String, Object>>) root.get("stations");
        if (stations == null) {
            return List.of();
        }
        List<EchoFoundationStationDefinition> result = new ArrayList<>();
        for (Map<String, Object> object : stations) {
            String requiresBlock = string(object, "requiresBlock");
            if (requiresBlock.isEmpty()) {
                requiresBlock = null;
            }
            result.add(new EchoFoundationStationDefinition(
                    resolve(string(object, "id"), sourceNs),
                    string(object, "displayName"),
                    requiresBlock,
                    string(object, "grid"),
                    string(object, "process"),
                    string(object, "notes"),
                    object
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoLootDefinition> parseLoot(Map<String, Object> root, String sourceNs, String sourceLogicalId) {
        List<EchoLootDefinition> result = new ArrayList<>();

        List<Map<String, Object>> blockDrops = (List<Map<String, Object>>) root.get("blockDrops");
        if (blockDrops != null) {
            for (Map<String, Object> object : blockDrops) {
                String id = resolve(string(object, "block"), sourceNs) + "_drops";
                List<String> entries = new ArrayList<>();
                for (Map<String, Object> drop : (List<Map<String, Object>>) object.getOrDefault("drops", List.of())) {
                    String key = drop.get("block") != null ? "block" : "item";
                    entries.add(resolve(string(drop, key), sourceNs));
                }
                result.add(new EchoLootDefinition(id, entries, sourceLogicalId));
            }
        }

        List<Map<String, Object>> chestTables = (List<Map<String, Object>>) root.get("chestTables");
        if (chestTables != null) {
            for (Map<String, Object> object : chestTables) {
                String id = resolve(string(object, "id"), sourceNs);
                List<String> entries = new ArrayList<>();
                for (Map<String, Object> entry : (List<Map<String, Object>>) object.getOrDefault("entries", List.of())) {
                    entries.add(resolve(string(entry, "item"), sourceNs));
                }
                result.add(new EchoLootDefinition(id, entries, sourceLogicalId));
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoFoundationCreatureRoleMapping> parseCreatureRoles(Map<String, Object> root, String sourceNs) {
        List<EchoFoundationCreatureRoleMapping> result = new ArrayList<>();
        List<Map<String, Object>> mappings = (List<Map<String, Object>>) root.get("openlandsCreatureMappings");
        if (mappings == null) {
            return result;
        }
        for (Map<String, Object> object : mappings) {
            result.add(new EchoFoundationCreatureRoleMapping(
                    resolve(string(object, "creature"), sourceNs),
                    string(object, "legacyCategory"),
                    resolve(string(object, "foundationRole"), sourceNs)
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EchoOpenlandsBlockDefinition.EchoOpenlandsDrop> resolveDrops(Object value, String sourceNs) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<EchoOpenlandsBlockDefinition.EchoOpenlandsDrop> drops = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Map<String, Object> object = (Map<String, Object>) map;
                drops.add(new EchoOpenlandsBlockDefinition.EchoOpenlandsDrop(
                        resolve(string(object, "item"), sourceNs),
                        intValue(object.get("count")),
                        resolve(string(object, "fallback"), sourceNs)
                ));
            }
        }
        return drops;
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseObject(String json) {
        Object value = EchoDataJson.parse(json);
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

    private record FoundFile(Path path, String schema, int movedOrder) {
    }
}
