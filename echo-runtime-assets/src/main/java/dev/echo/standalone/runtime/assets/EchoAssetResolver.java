package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public final class EchoAssetResolver {
    private final EchoAssetIndex index;
    private final EchoAssetLoader loader;

    public EchoAssetResolver(EchoAssetIndex index, EchoAssetLoader loader) {
        this.index = Objects.requireNonNull(index, "index");
        this.loader = Objects.requireNonNull(loader, "loader");
    }

    public Optional<EchoAssetEntry> resolve(String logicalId) {
        return index.resolve(logicalId);
    }

    public java.util.List<EchoAssetEntry> resolveAll(String logicalId) {
        java.util.List<EchoAssetEntry> matches = index.entriesByLogicalId().get(logicalId);
        if (matches == null || matches.isEmpty()) {
            return java.util.List.of();
        }
        return matches.stream()
                .sorted(java.util.Comparator.comparingInt(entry -> entry.mount().order()))
                .toList();
    }

    public Optional<String> loadText(String logicalId) throws IOException {
        Optional<EchoAssetEntry> entry = resolve(logicalId);
        if (entry.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(loader.loadText(entry.get()));
    }

    public java.util.List<String> loadAllText(String logicalId) throws IOException {
        java.util.ArrayList<String> result = new java.util.ArrayList<>();
        for (EchoAssetEntry entry : resolveAll(logicalId)) {
            result.add(loader.loadText(entry));
        }
        return java.util.List.copyOf(result);
    }
}
