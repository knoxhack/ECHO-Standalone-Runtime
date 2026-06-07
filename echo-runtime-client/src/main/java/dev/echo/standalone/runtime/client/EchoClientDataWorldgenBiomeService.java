package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.data.EchoDataRuntime;
import dev.echo.standalone.runtime.data.EchoDataRuntimeResult;
import dev.echo.standalone.runtime.data.EchoWorldgenBiomeDefinition;
import dev.echo.standalone.runtime.data.EchoWorldgenBiomeRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EchoClientDataWorldgenBiomeService {
    private static final List<String> PLACEMENT_HINT_KEYS = List.of(
            "centerX",
            "centerZ",
            "x",
            "z",
            "radius"
    );

    private List<Map<String, Object>> rows = List.of();
    private String lastError = "";

    void refresh(EchoAssetRuntimeResult assets) {
        lastError = "";
        rows = List.of();
        if (assets == null) {
            lastError = "No mounted data assets";
            return;
        }
        try {
            EchoDataRuntimeResult data = new EchoDataRuntime().load(new EchoDefaultRuntimeServiceRegistry(), assets);
            rows = rowsFrom(data.worldgenBiomes());
        } catch (IOException | IllegalArgumentException exception) {
            lastError = exception.getMessage();
        }
    }

    List<Map<String, Object>> rows() {
        return rows;
    }

    String lastError() {
        return lastError;
    }

    static List<Map<String, Object>> rowsFrom(EchoWorldgenBiomeRegistry registry) {
        if (registry == null || registry.biomes().isEmpty()) {
            return List.of();
        }
        ArrayList<Map<String, Object>> converted = new ArrayList<>();
        for (EchoWorldgenBiomeDefinition biome : registry.biomes()) {
            Map<String, Object> row = rowFrom(biome);
            if (!row.isEmpty()) {
                converted.add(row);
            }
        }
        return List.copyOf(converted);
    }

    private static Map<String, Object> rowFrom(EchoWorldgenBiomeDefinition biome) {
        if (!hasOverlayHints(biome)) {
            return Map.of();
        }
        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("worldgenType", "BIOME");
        metadata.put("biomeId", biome.id());
        metadata.put("displayName", biome.displayName());
        metadata.put("temperature", Double.toString(biome.temperature()));
        metadata.put("downfall", Double.toString(biome.downfall()));
        metadata.put("fogColor", colorHex(biome.fogColor()));
        metadata.put("grassColor", colorHex(biome.grassColor()));
        metadata.put("ambientParticle", biome.ambientParticle());
        metadata.put("tags", biome.tags());
        metadata.put("sourceLogicalId", biome.sourceLogicalId());
        for (String key : PLACEMENT_HINT_KEYS) {
            String value = biome.runtimeHints().get(key);
            if (value != null && !value.isBlank()) {
                metadata.put(key, value);
            }
        }

        LinkedHashMap<String, Object> row = new LinkedHashMap<>();
        row.put("moduleId", namespace(biome.id()));
        row.put("contentId", biome.id());
        row.put("contentKind", "WORLDGEN_DEFINITION");
        row.put("domain", "worldgen");
        row.put("standaloneRuntimeId", biome.id());
        row.put("biomeId", biome.id());
        row.put("metadata", Map.copyOf(metadata));
        return Map.copyOf(row);
    }

    private static boolean hasOverlayHints(EchoWorldgenBiomeDefinition biome) {
        if (biome == null) {
            return false;
        }
        for (String key : PLACEMENT_HINT_KEYS) {
            String value = biome.runtimeHints().get(key);
            if (value != null && !value.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private static String colorHex(int color) {
        return String.format(java.util.Locale.ROOT, "#%06X", color & 0xFFFFFF);
    }

    private static String namespace(String id) {
        if (id == null || !id.contains(":")) {
            return "data";
        }
        return id.substring(0, id.indexOf(':'));
    }
}
