package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.data.EchoDataRuntime;
import dev.echo.standalone.runtime.data.EchoDataRuntimeResult;
import dev.echo.standalone.runtime.data.EchoWorldCoreRegionDefinition;
import dev.echo.standalone.runtime.data.EchoWorldCoreRegionRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EchoClientDataWorldCoreRegionService {
    private static final List<String> BLOCK_HINT_KEYS = List.of(
            "surfaceBlockId",
            "surfaceBlock",
            "regionBlockId",
            "generationBlockId",
            "blockId",
            "liveVoxelId"
    );
    private static final List<String> PLACEMENT_HINT_KEYS = List.of(
            "centerX",
            "centerZ",
            "x",
            "z",
            "radius",
            "fixedY",
            "y",
            "surfaceYOffset"
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
            rows = rowsFrom(data.worldCoreRegions());
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

    static List<Map<String, Object>> rowsFrom(EchoWorldCoreRegionRegistry registry) {
        if (registry == null || registry.regions().isEmpty()) {
            return List.of();
        }
        ArrayList<Map<String, Object>> converted = new ArrayList<>();
        for (EchoWorldCoreRegionDefinition region : registry.regions()) {
            Map<String, Object> row = rowFrom(region);
            if (!row.isEmpty()) {
                converted.add(row);
            }
        }
        return List.copyOf(converted);
    }

    private static Map<String, Object> rowFrom(EchoWorldCoreRegionDefinition region) {
        if (!hasPlacementHints(region)) {
            return Map.of();
        }
        String blockId = regionBlockId(region);
        if (blockId.isBlank()) {
            return Map.of();
        }

        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("regionId", region.id());
        metadata.put("regionType", region.type());
        metadata.put("displayName", region.displayName());
        metadata.put("summary", region.summary());
        metadata.put("surfaceBlockId", blockId);
        metadata.put("standaloneRuntimeId", region.id());
        metadata.put("contentId", region.id());
        metadata.put("sourceLogicalId", region.sourceLogicalId());
        metadata.put("biomeIds", region.biomeIds());
        metadata.put("biomeTags", region.biomeTags());
        metadata.put("structureIds", region.structureIds());
        metadata.put("hazardIds", region.hazardIds());
        if (!region.discoveryId().isBlank()) {
            metadata.put("discoveryId", region.discoveryId());
        }
        if (!region.renderProfileId().isBlank()) {
            metadata.put("renderProfileId", region.renderProfileId());
        }
        if (!region.audioProfileId().isBlank()) {
            metadata.put("audioProfileId", region.audioProfileId());
        }
        for (String key : PLACEMENT_HINT_KEYS) {
            String value = region.runtimeHints().get(key);
            if (value != null && !value.isBlank()) {
                metadata.put(key, value);
            }
        }

        LinkedHashMap<String, Object> row = new LinkedHashMap<>();
        row.put("moduleId", namespace(region.id()));
        row.put("contentId", region.id());
        row.put("contentKind", "WORLD_REGION");
        row.put("domain", "world_regions");
        row.put("standaloneRuntimeId", region.id());
        row.put("regionId", region.id());
        row.put("surfaceBlockId", blockId);
        row.put("metadata", Map.copyOf(metadata));
        return Map.copyOf(row);
    }

    private static boolean hasPlacementHints(EchoWorldCoreRegionDefinition region) {
        if (region == null) {
            return false;
        }
        for (String key : PLACEMENT_HINT_KEYS) {
            String value = region.runtimeHints().get(key);
            if (value != null && !value.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private static String regionBlockId(EchoWorldCoreRegionDefinition region) {
        if (region == null) {
            return "";
        }
        for (String key : BLOCK_HINT_KEYS) {
            String value = region.runtimeHints().get(key);
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
