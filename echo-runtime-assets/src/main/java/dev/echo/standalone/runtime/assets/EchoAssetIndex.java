package dev.echo.standalone.runtime.assets;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public record EchoAssetIndex(
        List<EchoAssetEntry> entries,
        Map<String, List<EchoAssetEntry>> entriesByLogicalId,
        Map<String, List<EchoAssetEntry>> entriesByNamespace
) {
    public EchoAssetIndex {
        Objects.requireNonNull(entries, "entries");
        entries = entries.stream()
                .sorted(Comparator.comparing(EchoAssetEntry::logicalId)
                        .thenComparing(entry -> entry.mount().order())
                        .thenComparing(entry -> entry.file().toString()))
                .toList();
        entriesByLogicalId = group(entries, EchoAssetEntry::logicalId);
        entriesByNamespace = group(entries, entry -> entry.namespace().id());
    }

    public Optional<EchoAssetEntry> resolve(String logicalId) {
        List<EchoAssetEntry> matches = entriesByLogicalId.get(logicalId);
        if (matches == null || matches.isEmpty()) {
            return Optional.empty();
        }
        return matches.stream()
                .max(Comparator.comparingInt(entry -> entry.mount().order()));
    }

    public List<String> namespaces() {
        return entriesByNamespace.keySet().stream().sorted().toList();
    }

    private static Map<String, List<EchoAssetEntry>> group(
            List<EchoAssetEntry> entries,
            java.util.function.Function<EchoAssetEntry, String> classifier
    ) {
        TreeMap<String, List<EchoAssetEntry>> grouped = new TreeMap<>();
        for (EchoAssetEntry entry : entries) {
            grouped.computeIfAbsent(classifier.apply(entry), ignored -> new java.util.ArrayList<>()).add(entry);
        }
        TreeMap<String, List<EchoAssetEntry>> immutable = new TreeMap<>();
        grouped.forEach((key, value) -> immutable.put(key, List.copyOf(value)));
        return Map.copyOf(immutable);
    }
}
