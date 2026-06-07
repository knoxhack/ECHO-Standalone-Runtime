package dev.echo.standalone.runtime.data;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoWorldCoreRegionRegistry {
    private final LinkedHashMap<String, EchoWorldCoreRegionDefinition> regions = new LinkedHashMap<>();
    private boolean frozen;

    public void register(EchoWorldCoreRegionDefinition region) {
        ensureMutable();
        Objects.requireNonNull(region, "region");
        regions.put(region.id(), region);
    }

    public Optional<EchoWorldCoreRegionDefinition> find(String id) {
        return Optional.ofNullable(regions.get(EchoDataPaths.requireText(id, "id")));
    }

    public List<EchoWorldCoreRegionDefinition> regions() {
        return regions.values().stream()
                .sorted(Comparator.comparing(EchoWorldCoreRegionDefinition::id))
                .toList();
    }

    public void freeze() {
        frozen = true;
    }

    public boolean frozen() {
        return frozen;
    }

    private void ensureMutable() {
        if (frozen) {
            throw new IllegalStateException("WorldCore region registry is frozen");
        }
    }
}
