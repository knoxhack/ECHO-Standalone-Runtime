package dev.echo.standalone.runtime.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EchoWorldCoreRegionDefinition(
        String id,
        String type,
        String displayName,
        String summary,
        List<String> biomeIds,
        List<String> biomeTags,
        List<String> structureIds,
        List<String> hazardIds,
        String discoveryId,
        int radius,
        String renderProfileId,
        String audioProfileId,
        int sortOrder,
        List<String> references,
        Map<String, String> runtimeHints,
        String sourceLogicalId
) {
    public EchoWorldCoreRegionDefinition {
        id = EchoDataPaths.requireText(id, "id");
        type = type == null ? "" : type.trim();
        displayName = displayName == null || displayName.isBlank() ? id : displayName.trim();
        summary = summary == null ? "" : summary.trim();
        Objects.requireNonNull(biomeIds, "biomeIds");
        Objects.requireNonNull(biomeTags, "biomeTags");
        Objects.requireNonNull(structureIds, "structureIds");
        Objects.requireNonNull(hazardIds, "hazardIds");
        discoveryId = discoveryId == null ? "" : discoveryId.trim();
        radius = Math.max(0, radius);
        renderProfileId = renderProfileId == null ? "" : renderProfileId.trim();
        audioProfileId = audioProfileId == null ? "" : audioProfileId.trim();
        Objects.requireNonNull(references, "references");
        Objects.requireNonNull(runtimeHints, "runtimeHints");
        sourceLogicalId = EchoDataPaths.requireText(sourceLogicalId, "sourceLogicalId");
        biomeIds = normalizeList(biomeIds);
        biomeTags = normalizeList(biomeTags);
        structureIds = normalizeList(structureIds);
        hazardIds = normalizeList(hazardIds);
        references = normalizeList(references);
        runtimeHints = normalizeHints(runtimeHints);
    }

    private static List<String> normalizeList(List<String> values) {
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .sorted()
                .distinct()
                .toList();
    }

    private static Map<String, String> normalizeHints(Map<String, String> values) {
        return values.entrySet().stream()
                .filter(entry -> entry.getKey() != null
                        && !entry.getKey().isBlank()
                        && entry.getValue() != null
                        && !entry.getValue().isBlank())
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        entry -> entry.getKey().trim(),
                        entry -> entry.getValue().trim()
                ));
    }
}
