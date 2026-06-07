package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.data.EchoDataRuntime;
import dev.echo.standalone.runtime.data.EchoDataRuntimeResult;
import dev.echo.standalone.runtime.data.EchoWorldgenFeatureDefinition;
import dev.echo.standalone.runtime.data.EchoWorldgenFeatureRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EchoClientDataWorldgenFeatureService {
    private static final List<String> BLOCK_HINT_KEYS = List.of(
            "featureBlockId",
            "generationBlockId",
            "placementBlockId",
            "blockId",
            "liveVoxelId"
    );
    private static final List<String> PLACEMENT_HINT_KEYS = List.of(
            "x",
            "centerX",
            "y",
            "fixedY",
            "z",
            "centerZ",
            "width",
            "height",
            "depth",
            "radius",
            "surfaceYOffset",
            "count",
            "chance",
            "density",
            "shape"
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
            rows = rowsFrom(data.worldgenFeatures());
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

    static List<Map<String, Object>> rowsFrom(EchoWorldgenFeatureRegistry registry) {
        if (registry == null || registry.placedFeatures().isEmpty()) {
            return List.of();
        }
        ArrayList<Map<String, Object>> converted = new ArrayList<>();
        for (EchoWorldgenFeatureDefinition placed : registry.placedFeatures()) {
            EchoWorldgenFeatureDefinition configured = placed.configuredFeature().isBlank()
                    ? null
                    : registry.findConfiguredFeature(placed.configuredFeature()).orElse(null);
            Map<String, Object> row = rowFrom(placed, configured);
            if (!row.isEmpty()) {
                converted.add(row);
            }
        }
        return List.copyOf(converted);
    }

    private static Map<String, Object> rowFrom(
            EchoWorldgenFeatureDefinition placed,
            EchoWorldgenFeatureDefinition configured
    ) {
        String blockId = featureBlockId(placed, configured);
        if (blockId.isBlank()) {
            return Map.of();
        }
        LinkedHashMap<String, String> hints = runtimeHints(placed, configured);
        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("worldgenType", "FEATURE");
        metadata.put("featureBlockId", blockId);
        metadata.put("featureId", placed.id());
        metadata.put("standaloneRuntimeId", placed.id());
        metadata.put("contentId", placed.id());
        metadata.put("sourceLogicalId", placed.sourceLogicalId());
        if (configured != null) {
            metadata.put("configuredFeature", configured.id());
            metadata.put("configuredFeatureType", configured.type());
        }
        for (String key : PLACEMENT_HINT_KEYS) {
            String value = hints.get(key);
            if (value != null && !value.isBlank()) {
                metadata.put(key, value);
            }
        }

        LinkedHashMap<String, Object> row = new LinkedHashMap<>();
        row.put("moduleId", namespace(placed.id()));
        row.put("contentId", placed.id());
        row.put("contentKind", "FEATURE");
        row.put("domain", "features");
        row.put("standaloneRuntimeId", placed.id());
        row.put("featureBlockId", blockId);
        row.put("metadata", Map.copyOf(metadata));
        return Map.copyOf(row);
    }

    private static String featureBlockId(
            EchoWorldgenFeatureDefinition placed,
            EchoWorldgenFeatureDefinition configured
    ) {
        LinkedHashMap<String, String> hints = runtimeHints(placed, configured);
        for (String key : BLOCK_HINT_KEYS) {
            String value = hints.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        String referenced = firstBlockReference(placed, configured);
        return referenced == null ? "" : referenced;
    }

    private static LinkedHashMap<String, String> runtimeHints(
            EchoWorldgenFeatureDefinition placed,
            EchoWorldgenFeatureDefinition configured
    ) {
        LinkedHashMap<String, String> hints = new LinkedHashMap<>();
        if (configured != null) {
            hints.putAll(configured.runtimeHints());
        }
        if (placed != null) {
            hints.putAll(placed.runtimeHints());
        }
        return hints;
    }

    private static String firstBlockReference(
            EchoWorldgenFeatureDefinition placed,
            EchoWorldgenFeatureDefinition configured
    ) {
        ArrayList<String> references = new ArrayList<>();
        if (configured != null) {
            references.addAll(configured.references());
        }
        if (placed != null) {
            references.addAll(placed.references());
        }
        for (String reference : references) {
            if (looksLikeRuntimeBlockReference(reference, placed, configured)) {
                return reference;
            }
        }
        return "";
    }

    private static boolean looksLikeRuntimeBlockReference(
            String reference,
            EchoWorldgenFeatureDefinition placed,
            EchoWorldgenFeatureDefinition configured
    ) {
        if (reference == null || reference.isBlank() || reference.startsWith("#") || !reference.contains(":")) {
            return false;
        }
        String normalized = reference.trim();
        if (normalized.startsWith("minecraft:")) {
            return false;
        }
        if (placed != null && (normalized.equals(placed.id()) || normalized.equals(placed.configuredFeature()))) {
            return false;
        }
        return configured == null || !normalized.equals(configured.id());
    }

    private static String namespace(String id) {
        if (id == null || !id.contains(":")) {
            return "data";
        }
        return id.substring(0, id.indexOf(':'));
    }
}
