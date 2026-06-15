package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.data.EchoDataJson;
import dev.echo.standalone.runtime.data.EchoDataTag;
import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Loads standard NeoForge datapack entries from directories or jar/zip archives and translates them
 * into ECHO runtime content definitions.
 *
 * <p>Supported subsets: vanilla-style shapeless/shaped recipes, simple loot tables with item
 * entries, and block/item tag files. All vanilla IDs are remapped through
 * {@link EchoVanillaToOpenlandsAliasBridge} before translation. Unsupported entries generate
 * {@link EchoCompatDiagnostic}s instead of failing the whole scan.
 */
public final class EchoNeoForgeDatapackLoader {

    private final EchoVanillaToOpenlandsAliasBridge vanillaBridge;

    public EchoNeoForgeDatapackLoader() {
        this(new EchoVanillaToOpenlandsAliasBridge());
    }

    public EchoNeoForgeDatapackLoader(EchoVanillaToOpenlandsAliasBridge vanillaBridge) {
        this.vanillaBridge = vanillaBridge == null ? new EchoVanillaToOpenlandsAliasBridge() : vanillaBridge;
    }

    /**
     * Scans all provided datapack roots and translates supported entries.
     */
    public EchoNeoForgeDatapackScanResult load(List<Path> datapackRoots) throws IOException {
        List<EchoRecipeDefinition> recipes = new ArrayList<>();
        List<EchoLootDefinition> lootTables = new ArrayList<>();
        List<EchoDataTag> tags = new ArrayList<>();
        List<EchoCompatDiagnostic> diagnostics = new ArrayList<>();

        for (Path root : datapackRoots) {
            if (!Files.exists(root)) {
                diagnostics.add(diagnostic("missing_datapack_root", "Datapack root does not exist: " + root, EchoCompatDiagnosticSeverity.WARNING));
                continue;
            }
            if (Files.isDirectory(root)) {
                scanDirectory(root, recipes, lootTables, tags, diagnostics);
            } else {
                scanArchive(root, recipes, lootTables, tags, diagnostics);
            }
        }

        return new EchoNeoForgeDatapackScanResult(recipes, lootTables, tags, diagnostics);
    }

    private void scanDirectory(
            Path root,
            List<EchoRecipeDefinition> recipes,
            List<EchoLootDefinition> lootTables,
            List<EchoDataTag> tags,
            List<EchoCompatDiagnostic> diagnostics
    ) throws IOException {
        Path dataDir = root.resolve("data");
        if (!Files.isDirectory(dataDir)) {
            return;
        }
        try (var stream = Files.walk(dataDir)) {
            List<Path> paths = stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .sorted()
                    .toList();
            for (Path path : paths) {
                processDatapackFile(dataDir, path, recipes, lootTables, tags, diagnostics);
            }
        }
    }

    private void scanArchive(
            Path archive,
            List<EchoRecipeDefinition> recipes,
            List<EchoLootDefinition> lootTables,
            List<EchoDataTag> tags,
            List<EchoCompatDiagnostic> diagnostics
    ) throws IOException {
        URI uri = URI.create("jar:" + archive.toUri());
        try (FileSystem fs = FileSystems.newFileSystem(uri, Map.of())) {
            Path dataDir = fs.getPath("/data");
            if (!Files.isDirectory(dataDir)) {
                return;
            }
            try (var stream = Files.walk(dataDir)) {
                List<Path> paths = stream.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".json"))
                        .sorted()
                        .toList();
                for (Path path : paths) {
                    processDatapackFile(dataDir, path, recipes, lootTables, tags, diagnostics);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processDatapackFile(
            Path dataDir,
            Path path,
            List<EchoRecipeDefinition> recipes,
            List<EchoLootDefinition> lootTables,
            List<EchoDataTag> tags,
            List<EchoCompatDiagnostic> diagnostics
    ) throws IOException {
        String relative = dataDir.relativize(path).toString().replace('\\', '/');
        String[] parts = relative.split("/", 4);
        if (parts.length < 3) {
            return;
        }
        String namespace = parts[0];
        String category = parts[1];
        String rest = parts[2];
        if (parts.length == 4) {
            rest = rest + "/" + parts[3];
        }
        String fileName = path.getFileName().toString();
        String idWithoutExtension = fileName.endsWith(".json")
                ? fileName.substring(0, fileName.length() - ".json".length())
                : fileName;

        String json = readString(path);
        Object parsed;
        try {
            parsed = EchoDataJson.parse(json);
        } catch (IllegalArgumentException e) {
            diagnostics.add(diagnostic("parse_error", relative + ": " + e.getMessage(), EchoCompatDiagnosticSeverity.ERROR));
            return;
        }
        if (!(parsed instanceof Map<?, ?> rootMap)) {
            diagnostics.add(diagnostic("invalid_root", relative + ": root is not an object", EchoCompatDiagnosticSeverity.ERROR));
            return;
        }
        Map<String, Object> root = (Map<String, Object>) rootMap;

        switch (category) {
            case "recipes" -> {
                EchoRecipeDefinition recipe = translateRecipe(namespace, idWithoutExtension, root, relative, diagnostics);
                if (recipe != null) {
                    recipes.add(recipe);
                }
            }
            case "loot_tables" -> {
                EchoLootDefinition loot = translateLoot(namespace, rest, idWithoutExtension, root, relative, diagnostics);
                if (loot != null) {
                    lootTables.add(loot);
                }
            }
            case "tags" -> {
                EchoDataTag tag = translateTag(namespace, rest, idWithoutExtension, root, relative, diagnostics);
                if (tag != null) {
                    tags.add(tag);
                }
            }
            default -> diagnostics.add(diagnostic("unsupported_category", relative, EchoCompatDiagnosticSeverity.INFO));
        }
    }

    @SuppressWarnings("unchecked")
    private EchoRecipeDefinition translateRecipe(
            String namespace,
            String id,
            Map<String, Object> root,
            String sourceLogicalId,
            List<EchoCompatDiagnostic> diagnostics
    ) {
        String type = string(root, "type");
        String recipeId = namespace + ":" + id;
        List<String> ingredients = new ArrayList<>();
        String resultItem = null;
        int resultCount = 1;

        Object resultObj = root.get("result");
        if (resultObj instanceof Map<?, ?> resultMap) {
            Map<String, Object> result = (Map<String, Object>) resultMap;
            resultItem = resolveItemOrTag(result);
            resultCount = intValue(result.get("count"));
        } else if (resultObj instanceof String s) {
            resultItem = s;
        }
        if (resultItem == null || resultItem.isBlank()) {
            diagnostics.add(diagnostic("missing_recipe_result", sourceLogicalId, EchoCompatDiagnosticSeverity.WARNING));
            return null;
        }
        resultItem = vanillaBridge.resolve(resultItem);

        if ("minecraft:crafting_shapeless".equals(type)) {
            List<Map<String, Object>> rawIngredients = (List<Map<String, Object>>) root.get("ingredients");
            if (rawIngredients != null) {
                for (Map<String, Object> ingredient : rawIngredients) {
                    String resolved = resolveItemOrTag(ingredient);
                    if (!resolved.isBlank()) {
                        ingredients.add(vanillaBridge.resolve(resolved));
                    }
                }
            }
        } else if ("minecraft:crafting_shaped".equals(type)) {
            Map<String, Object> key = (Map<String, Object>) root.get("key");
            List<String> pattern = stringList(root, "pattern");
            if (key != null && !pattern.isEmpty()) {
                for (String row : pattern) {
                    for (char c : row.toCharArray()) {
                        if (c == ' ') {
                            continue;
                        }
                        Object entry = key.get(String.valueOf(c));
                        if (entry instanceof Map<?, ?> entryMap) {
                            String resolved = resolveItemOrTag((Map<String, Object>) entryMap);
                            if (!resolved.isBlank()) {
                                ingredients.add(vanillaBridge.resolve(resolved));
                            }
                        } else if (entry instanceof String s) {
                            ingredients.add(vanillaBridge.resolve(s));
                        }
                    }
                }
            }
        } else {
            diagnostics.add(diagnostic("unsupported_recipe_type", sourceLogicalId + " type=" + type, EchoCompatDiagnosticSeverity.INFO));
            return null;
        }

        if (ingredients.isEmpty()) {
            diagnostics.add(diagnostic("missing_recipe_ingredients", sourceLogicalId, EchoCompatDiagnosticSeverity.WARNING));
            return null;
        }
        return new EchoRecipeDefinition(recipeId, "neoforge", ingredients, resultItem, sourceLogicalId);
    }

    @SuppressWarnings("unchecked")
    private EchoLootDefinition translateLoot(
            String namespace,
            String rest,
            String id,
            Map<String, Object> root,
            String sourceLogicalId,
            List<EchoCompatDiagnostic> diagnostics
    ) {
        String lootId = namespace + ":" + rest.replace(".json", "").replace('\\', '/');
        List<String> entries = new ArrayList<>();
        List<Map<String, Object>> pools = (List<Map<String, Object>>) root.get("pools");
        if (pools != null) {
            for (Map<String, Object> pool : pools) {
                List<Map<String, Object>> poolEntries = (List<Map<String, Object>>) pool.get("entries");
                if (poolEntries == null) {
                    continue;
                }
                for (Map<String, Object> entry : poolEntries) {
                    String entryType = string(entry, "type");
                    if (!"minecraft:item".equals(entryType)) {
                        continue;
                    }
                    String name = string(entry, "name");
                    if (!name.isBlank()) {
                        entries.add(vanillaBridge.resolve(name));
                    }
                }
            }
        }
        if (entries.isEmpty()) {
            diagnostics.add(diagnostic("empty_loot_table", sourceLogicalId, EchoCompatDiagnosticSeverity.INFO));
            return null;
        }
        return new EchoLootDefinition(lootId, entries, sourceLogicalId);
    }

    @SuppressWarnings("unchecked")
    private EchoDataTag translateTag(
            String namespace,
            String rest,
            String id,
            Map<String, Object> root,
            String sourceLogicalId,
            List<EchoCompatDiagnostic> diagnostics
    ) {
        String registryId;
        if (rest.startsWith("blocks/")) {
            registryId = "block";
        } else if (rest.startsWith("items/")) {
            registryId = "item";
        } else {
            diagnostics.add(diagnostic("unsupported_tag_registry", sourceLogicalId, EchoCompatDiagnosticSeverity.INFO));
            return null;
        }
        String tagId = namespace + ":" + id;
        List<String> values = new ArrayList<>();
        List<Object> rawValues = (List<Object>) root.get("values");
        if (rawValues != null) {
            for (Object value : rawValues) {
                if (value instanceof String s) {
                    values.add(vanillaBridge.resolve(s));
                } else if (value instanceof Map<?, ?> valueMap) {
                    String resolved = string((Map<String, Object>) valueMap, "id");
                    if (!resolved.isBlank()) {
                        values.add(vanillaBridge.resolve(resolved));
                    }
                }
            }
        }
        if (values.isEmpty()) {
            diagnostics.add(diagnostic("empty_tag", sourceLogicalId, EchoCompatDiagnosticSeverity.INFO));
            return null;
        }
        return new EchoDataTag(tagId, registryId, values, sourceLogicalId);
    }

    private String resolveItemOrTag(Map<String, Object> object) {
        if (object.get("item") != null) {
            return string(object, "item");
        }
        if (object.get("tag") != null) {
            return "#" + string(object, "tag");
        }
        return "";
    }

    private String readString(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private EchoCompatDiagnostic diagnostic(String subject, String message, EchoCompatDiagnosticSeverity severity) {
        return new EchoCompatDiagnostic(severity, "neoforge_datapack:" + subject, message);
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
}
