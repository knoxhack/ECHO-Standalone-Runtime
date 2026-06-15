package dev.echo.standalone.runtime.world.openlands;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and applies the Openlands → Foundation alias bridge.
 *
 * <p>The bridge maps legacy Openlands IDs to canonical Foundation (or renamed Openlands) IDs.
 * It is applied at content load time so that recipes, tags, drops, and references resolve to
 * canonical IDs.
 */
public final class EchoFoundationAliasBridge {

    private static final List<String> DATA_ROOT_CANDIDATES = List.of(
            "src/main/resources/data",
            "build/resources/main/data"
    );

    private final Map<String, String> aliases;

    public EchoFoundationAliasBridge(Map<String, String> aliases) {
        this.aliases = aliases == null ? Map.of() : Map.copyOf(aliases);
    }

    /**
     * Loads the alias bridge from all provided module roots.
     */
    public static EchoFoundationAliasBridge load(List<Path> moduleRoots) throws IOException {
        Map<String, String> aliases = new LinkedHashMap<>();
        for (Path root : moduleRoots) {
            for (Path dataRoot : dataRoots(root)) {
                Path bridgeFile = dataRoot.resolve("echoopenlandsprotocol/openlands/foundation/foundation_alias_bridge.json");
                if (!Files.isRegularFile(bridgeFile)) {
                    continue;
                }
                String json = Files.readString(bridgeFile, StandardCharsets.UTF_8);
                Map<String, Object> bridgeRoot = parseObject(json);
                List<Map<String, Object>> entries = listOfMaps(bridgeRoot.get("aliases"));
                for (Map<String, Object> entry : entries) {
                    String legacy = string(entry, "legacyId");
                    String canonical = string(entry, "canonicalId");
                    if (!legacy.isBlank() && !canonical.isBlank()) {
                        aliases.put(legacy, canonical);
                    }
                }
            }
        }
        return new EchoFoundationAliasBridge(aliases);
    }

    /**
     * Loads the alias bridge from a single module root.
     */
    public static EchoFoundationAliasBridge load(Path moduleRoot) throws IOException {
        return load(List.of(moduleRoot));
    }

    /**
     * Returns an empty bridge that never rewrites IDs.
     */
    public static EchoFoundationAliasBridge empty() {
        return new EchoFoundationAliasBridge(Collections.emptyMap());
    }

    /**
     * Returns the canonical ID for a legacy ID, or the input itself if no alias exists.
     */
    public String resolve(String id) {
        if (id == null || id.isBlank()) {
            return id;
        }
        return aliases.getOrDefault(id, id);
    }

    /**
     * Resolves aliases in a collection of IDs.
     */
    public List<String> resolveAll(Collection<String> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().map(this::resolve).toList();
    }

    public Map<String, String> aliases() {
        return aliases;
    }

    private static List<Path> dataRoots(Path moduleRoot) {
        for (String candidate : DATA_ROOT_CANDIDATES) {
            Path path = moduleRoot.resolve(candidate).normalize();
            if (Files.isDirectory(path)) {
                return List.of(path);
            }
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseObject(String json) {
        Object value = dev.echo.standalone.runtime.data.EchoDataJson.parse(json);
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Alias bridge root must be an object");
        }
        return (Map<String, Object>) map;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> listOfMaps(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                result.add((Map<String, Object>) map);
            }
        }
        return result;
    }

    private static String string(Map<String, Object> object, String key) {
        Object value = object.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }
}
