package dev.echo.standalone.runtime.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EchoWorldgenStructureDefinition(
        String id,
        String type,
        List<String> references,
        Map<String, String> runtimeHints,
        String sourceLogicalId
) {
    public EchoWorldgenStructureDefinition {
        id = EchoDataPaths.requireText(id, "id");
        type = EchoDataPaths.requireText(type, "type");
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
