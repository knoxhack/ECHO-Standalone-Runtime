package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.data.EchoDataRuntime;
import dev.echo.standalone.runtime.data.EchoDataRuntimeResult;
import dev.echo.standalone.runtime.data.EchoWorldgenStructureDefinition;
import dev.echo.standalone.runtime.data.EchoWorldgenStructureRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EchoClientDataWorldgenStructureService {
    private static final List<String> BLOCK_HINT_KEYS = List.of(
            "structureBlockId",
            "placementBlockId",
            "blockId",
            "liveVoxelId"
    );
    private static final List<String> PLACEMENT_HINT_KEYS = List.of(
            "x",
            "originX",
            "y",
            "startY",
            "startHeight",
            "z",
            "originZ",
            "width",
            "height",
            "depth",
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
            rows = rowsFrom(data.worldgenStructures());
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

    static List<Map<String, Object>> rowsFrom(EchoWorldgenStructureRegistry registry) {
        if (registry == null || registry.structures().isEmpty()) {
            return List.of();
        }
        ArrayList<Map<String, Object>> converted = new ArrayList<>();
        for (EchoWorldgenStructureDefinition structure : registry.structures()) {
            Map<String, Object> row = rowFrom(structure);
            if (!row.isEmpty()) {
                converted.add(row);
            }
        }
        return List.copyOf(converted);
    }

    private static Map<String, Object> rowFrom(EchoWorldgenStructureDefinition structure) {
        if (!hasPlacementHints(structure)) {
            return Map.of();
        }
        String blockId = structureBlockId(structure);
        if (blockId.isBlank()) {
            return Map.of();
        }

        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("worldgenType", "STRUCTURE");
        metadata.put("structureId", structure.id());
        metadata.put("structureType", structure.type());
        metadata.put("structureBlockId", blockId);
        metadata.put("standaloneRuntimeId", structure.id());
        metadata.put("contentId", structure.id());
        metadata.put("sourceLogicalId", structure.sourceLogicalId());
        for (String key : PLACEMENT_HINT_KEYS) {
            String value = structure.runtimeHints().get(key);
            if (value != null && !value.isBlank()) {
                metadata.put(key, value);
            }
        }

        LinkedHashMap<String, Object> row = new LinkedHashMap<>();
        row.put("moduleId", namespace(structure.id()));
        row.put("contentId", structure.id());
        row.put("contentKind", "WORLDGEN_DEFINITION");
        row.put("domain", "worldgen");
        row.put("standaloneRuntimeId", structure.id());
        row.put("structureId", structure.id());
        row.put("structureBlockId", blockId);
        row.put("metadata", Map.copyOf(metadata));
        return Map.copyOf(row);
    }

    private static boolean hasPlacementHints(EchoWorldgenStructureDefinition structure) {
        if (structure == null) {
            return false;
        }
        for (String key : PLACEMENT_HINT_KEYS) {
            String value = structure.runtimeHints().get(key);
            if (value != null && !value.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private static String structureBlockId(EchoWorldgenStructureDefinition structure) {
        if (structure == null) {
            return "";
        }
        for (String key : BLOCK_HINT_KEYS) {
            String value = structure.runtimeHints().get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static String namespace(String id) {
        if (id == null || !id.contains(":")) {
            return "data";
        }
        return id.substring(0, id.indexOf(':'));
    }
}
