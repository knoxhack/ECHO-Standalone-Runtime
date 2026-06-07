package dev.echo.standalone.runtime.data;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoWorldgenFeatureRegistry {
    private final LinkedHashMap<String, EchoWorldgenFeatureDefinition> features = new LinkedHashMap<>();
    private boolean frozen;

    public void register(EchoWorldgenFeatureDefinition feature) {
        ensureMutable();
        Objects.requireNonNull(feature, "feature");
        features.put(key(feature.featureKind(), feature.id()), feature);
    }

    public Optional<EchoWorldgenFeatureDefinition> find(String id) {
        String normalized = EchoDataPaths.requireText(id, "id");
        return features.values().stream()
                .filter(feature -> feature.id().equals(normalized))
                .findFirst();
    }

    public Optional<EchoWorldgenFeatureDefinition> findConfiguredFeature(String id) {
        return find("configured_feature", id);
    }

    public Optional<EchoWorldgenFeatureDefinition> findPlacedFeature(String id) {
        return find("placed_feature", id);
    }

    public List<EchoWorldgenFeatureDefinition> features() {
        return features.values().stream()
                .sorted(Comparator.comparing(EchoWorldgenFeatureDefinition::id))
                .toList();
    }

    public List<EchoWorldgenFeatureDefinition> configuredFeatures() {
        return byKind("configured_feature");
    }

    public List<EchoWorldgenFeatureDefinition> placedFeatures() {
        return byKind("placed_feature");
    }

    public void freeze() {
        frozen = true;
    }

    public boolean frozen() {
        return frozen;
    }

    private List<EchoWorldgenFeatureDefinition> byKind(String kind) {
        return features().stream()
                .filter(feature -> feature.featureKind().equals(kind))
                .toList();
    }

    private Optional<EchoWorldgenFeatureDefinition> find(String kind, String id) {
        return Optional.ofNullable(features.get(key(kind, EchoDataPaths.requireText(id, "id"))));
    }

    private static String key(String kind, String id) {
        return EchoDataPaths.requireText(kind, "kind") + "|" + EchoDataPaths.requireText(id, "id");
    }

    private void ensureMutable() {
        if (frozen) {
            throw new IllegalStateException("Worldgen feature registry is frozen");
        }
    }
}
