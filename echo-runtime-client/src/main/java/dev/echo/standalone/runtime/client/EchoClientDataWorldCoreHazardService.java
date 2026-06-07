package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.data.EchoDataRuntime;
import dev.echo.standalone.runtime.data.EchoDataRuntimeResult;
import dev.echo.standalone.runtime.data.EchoWorldCoreHazardDefinition;
import dev.echo.standalone.runtime.data.EchoWorldCoreHazardRegistry;
import dev.echo.standalone.runtime.data.EchoWorldCoreRegionDefinition;
import dev.echo.standalone.runtime.data.EchoWorldCoreRegionRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

final class EchoClientDataWorldCoreHazardService {
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
            rows = rowsFrom(data.worldCoreHazards(), data.worldCoreRegions());
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

    static List<Map<String, Object>> rowsFrom(
            EchoWorldCoreHazardRegistry hazards,
            EchoWorldCoreRegionRegistry regions
    ) {
        if (hazards == null || hazards.hazards().isEmpty()) {
            return List.of();
        }
        ArrayList<Map<String, Object>> converted = new ArrayList<>();
        for (EchoWorldCoreHazardDefinition hazard : hazards.hazards()) {
            Map<String, Object> row = rowFrom(hazard, regions);
            if (!row.isEmpty()) {
                converted.add(row);
            }
        }
        return List.copyOf(converted);
    }

    private static Map<String, Object> rowFrom(
            EchoWorldCoreHazardDefinition hazard,
            EchoWorldCoreRegionRegistry regions
    ) {
        LinkedHashSet<String> biomeTags = biomeMatchTags(hazard, regions);
        if (biomeTags.isEmpty()) {
            return Map.of();
        }
        double exposurePerSecond = exposurePerSecond(hazard);
        int damage = damage(hazard);
        if (exposurePerSecond <= 0.0D || damage <= 0) {
            return Map.of();
        }

        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("hazardId", hazard.id());
        metadata.put("hazardType", hazard.type());
        metadata.put("displayName", hazard.displayName());
        metadata.put("summary", hazard.summary());
        metadata.put("defaultSeverity", Integer.toString(hazard.defaultSeverity()));
        metadata.put("ticking", Boolean.toString(hazard.ticking()));
        metadata.put("biomeTags", List.copyOf(biomeTags));
        metadata.put("exposurePerSecond", Double.toString(exposurePerSecond));
        metadata.put("damage", Integer.toString(damage));
        metadata.put("sourceLogicalId", hazard.sourceLogicalId());
        String statusEffectId = hazard.runtimeHints().get("statusEffectId");
        if (statusEffectId != null && !statusEffectId.isBlank()) {
            metadata.put("statusEffectId", statusEffectId);
        }

        LinkedHashMap<String, Object> row = new LinkedHashMap<>();
        row.put("moduleId", namespace(hazard.id()));
        row.put("contentId", hazard.id());
        row.put("contentKind", "WORLD_HAZARD");
        row.put("domain", "hazards");
        row.put("standaloneRuntimeId", hazard.id());
        row.put("hazardId", hazard.id());
        row.put("displayName", hazard.displayName());
        row.put("metadata", Map.copyOf(metadata));
        return Map.copyOf(row);
    }

    private static LinkedHashSet<String> biomeMatchTags(
            EchoWorldCoreHazardDefinition hazard,
            EchoWorldCoreRegionRegistry regions
    ) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        addDelimited(tags, hazard.runtimeHints().get("biomeTags"));
        addDelimited(tags, hazard.runtimeHints().get("hazardBiomeTags"));
        addDelimited(tags, hazard.runtimeHints().get("matchTags"));
        if (regions == null) {
            return tags;
        }
        for (EchoWorldCoreRegionDefinition region : regions.regions()) {
            if (!region.hazardIds().contains(hazard.id())) {
                continue;
            }
            tags.addAll(region.biomeTags());
            tags.addAll(region.biomeIds());
        }
        return tags;
    }

    private static double exposurePerSecond(EchoWorldCoreHazardDefinition hazard) {
        double explicit = positiveDouble(firstText(
                hazard.runtimeHints().get("exposurePerSecond"),
                hazard.runtimeHints().get("exposure")
        ), -1.0D);
        if (explicit >= 0.0D) {
            return explicit;
        }
        if (!hazard.ticking() || hazard.defaultSeverity() <= 0) {
            return 0.0D;
        }
        return Math.max(1.0D, hazard.defaultSeverity() / 8.0D);
    }

    private static int damage(EchoWorldCoreHazardDefinition hazard) {
        int explicit = intValue(hazard.runtimeHints().get("damage"), -1);
        if (explicit >= 0) {
            return explicit;
        }
        if (!hazard.ticking() || hazard.defaultSeverity() <= 0) {
            return 0;
        }
        return hazard.defaultSeverity() >= 80 ? 2 : 1;
    }

    private static void addDelimited(LinkedHashSet<String> values, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        for (String value : text.split("[,;]")) {
            String normalized = value.trim();
            if (!normalized.isBlank()) {
                values.add(normalized);
            }
        }
    }

    private static String firstText(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private static int intValue(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static double positiveDouble(String value, double fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            double parsed = Double.parseDouble(value.trim());
            return parsed >= 0.0D && Double.isFinite(parsed) ? parsed : fallback;
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static String namespace(String id) {
        if (id == null || !id.contains(":")) {
            return "data";
        }
        return id.substring(0, id.indexOf(':'));
    }
}
