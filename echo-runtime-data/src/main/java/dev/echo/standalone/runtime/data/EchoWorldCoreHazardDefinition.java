package dev.echo.standalone.runtime.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EchoWorldCoreHazardDefinition(
        String id,
        String type,
        String displayName,
        String summary,
        int defaultSeverity,
        boolean ticking,
        List<String> references,
        Map<String, String> runtimeHints,
        String sourceLogicalId
) {
    public EchoWorldCoreHazardDefinition {
        id = EchoDataPaths.requireText(id, "id");
        type = type == null ? "" : type.trim();
        displayName = displayName == null || displayName.isBlank() ? id : displayName.trim();
        summary = summary == null ? "" : summary.trim();
        defaultSeverity = Math.max(0, defaultSeverity);
        Objects.requireNonNull(references, "references");
        Objects.requireNonNull(runtimeHints, "runtimeHints");
        sourceLogicalId = EchoDataPaths.requireText(sourceLogicalId, "sourceLogicalId");
        references = references.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .sorted()
                .distinct()
                .toList();
        runtimeHints = runtimeHints.entrySet().stream()
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
