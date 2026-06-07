package dev.echo.standalone.runtime.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EchoWorldgenBiomeDefinition(
        String id,
        String displayName,
        double temperature,
        double downfall,
        int fogColor,
        int grassColor,
        String ambientParticle,
        List<String> tags,
        List<String> references,
        Map<String, String> runtimeHints,
        String sourceLogicalId
) {
    public EchoWorldgenBiomeDefinition {
        id = EchoDataPaths.requireText(id, "id");
        displayName = displayName == null || displayName.isBlank() ? id : displayName.trim();
        if (!Double.isFinite(temperature)) {
            throw new IllegalArgumentException("temperature must be finite");
        }
        if (!Double.isFinite(downfall) || downfall < 0.0D) {
            throw new IllegalArgumentException("downfall must be finite and non-negative");
        }
        ambientParticle = ambientParticle == null || ambientParticle.isBlank()
                ? "minecraft:ash"
                : ambientParticle.trim();
        Objects.requireNonNull(tags, "tags");
        Objects.requireNonNull(references, "references");
        Objects.requireNonNull(runtimeHints, "runtimeHints");
        sourceLogicalId = EchoDataPaths.requireText(sourceLogicalId, "sourceLogicalId");
        tags = tags.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .sorted()
                .distinct()
                .toList();
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
