package dev.echo.standalone.runtime.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EchoWorldgenFeatureDefinition(
        String id,
        String featureKind,
        String type,
        String configuredFeature,
        List<String> placementModifiers,
        List<String> references,
        Map<String, String> runtimeHints,
        String sourceLogicalId
) {
    public EchoWorldgenFeatureDefinition {
        id = EchoDataPaths.requireText(id, "id");
        featureKind = EchoDataPaths.requireText(featureKind, "featureKind");
        type = EchoDataPaths.requireText(type, "type");
        configuredFeature = configuredFeature == null ? "" : configuredFeature.trim();
        Objects.requireNonNull(placementModifiers, "placementModifiers");
        Objects.requireNonNull(references, "references");
        Objects.requireNonNull(runtimeHints, "runtimeHints");
        sourceLogicalId = EchoDataPaths.requireText(sourceLogicalId, "sourceLogicalId");
        placementModifiers = placementModifiers.stream()
                .filter(value -> value != null && !value.isBlank())
                .sorted()
                .toList();
        references = references.stream()
                .filter(value -> value != null && !value.isBlank())
                .sorted()
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
