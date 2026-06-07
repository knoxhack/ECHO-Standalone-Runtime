package dev.echo.standalone.runtime.data;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoWorldCoreHazardRegistry {
    private final LinkedHashMap<String, EchoWorldCoreHazardDefinition> hazards = new LinkedHashMap<>();
    private boolean frozen;

    public void register(EchoWorldCoreHazardDefinition hazard) {
        ensureMutable();
        Objects.requireNonNull(hazard, "hazard");
        hazards.put(hazard.id(), hazard);
    }

    public Optional<EchoWorldCoreHazardDefinition> find(String id) {
        return Optional.ofNullable(hazards.get(EchoDataPaths.requireText(id, "id")));
    }

    public List<EchoWorldCoreHazardDefinition> hazards() {
        return hazards.values().stream()
                .sorted(Comparator.comparing(EchoWorldCoreHazardDefinition::id))
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
            throw new IllegalStateException("WorldCore hazard registry is frozen");
        }
    }
}
