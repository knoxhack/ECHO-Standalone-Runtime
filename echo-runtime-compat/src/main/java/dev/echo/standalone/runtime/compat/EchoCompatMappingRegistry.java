package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoCompatMappingRegistry {
    private final LinkedHashMap<String, EchoCompatContentMapping> mappingsBySource = new LinkedHashMap<>();

    public synchronized void register(EchoCompatContentMapping mapping) {
        Objects.requireNonNull(mapping, "mapping");
        if (mappingsBySource.containsKey(mapping.sourceId())) {
            throw new IllegalArgumentException("Duplicate compatibility source id: " + mapping.sourceId());
        }
        mappingsBySource.put(mapping.sourceId(), mapping);
    }

    public synchronized Optional<EchoCompatContentMapping> findSource(String sourceId) {
        String normalized = EchoCompatText.requireText(sourceId, "sourceId");
        return Optional.ofNullable(mappingsBySource.get(normalized));
    }

    public synchronized EchoCompatContentMapping requireSource(String sourceId) {
        String normalized = EchoCompatText.requireText(sourceId, "sourceId");
        EchoCompatContentMapping mapping = mappingsBySource.get(normalized);
        if (mapping == null) {
            throw new IllegalArgumentException("Unknown compatibility source id: " + normalized);
        }
        return mapping;
    }

    public synchronized List<EchoCompatContentMapping> all() {
        return List.copyOf(mappingsBySource.values());
    }

    public synchronized int count() {
        return mappingsBySource.size();
    }

    public synchronized int supportedCount() {
        return countByStatus(EchoCompatMappingStatus.SUPPORTED);
    }

    public synchronized int manualReviewCount() {
        return countByStatus(EchoCompatMappingStatus.MANUAL_REVIEW);
    }

    public synchronized int blockedCount() {
        return countByStatus(EchoCompatMappingStatus.BLOCKED);
    }

    private int countByStatus(EchoCompatMappingStatus status) {
        return (int) mappingsBySource.values().stream()
                .filter(mapping -> mapping.status() == status)
                .count();
    }
}
