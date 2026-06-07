package dev.echo.standalone.runtime.data;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoWorldgenStructureRegistry {
    private final LinkedHashMap<String, EchoWorldgenStructureDefinition> structures = new LinkedHashMap<>();
    private boolean frozen;

    public void register(EchoWorldgenStructureDefinition structure) {
        ensureMutable();
        Objects.requireNonNull(structure, "structure");
        structures.put(structure.id(), structure);
    }

    public Optional<EchoWorldgenStructureDefinition> find(String id) {
        return Optional.ofNullable(structures.get(id));
    }

    public List<EchoWorldgenStructureDefinition> structures() {
        return structures.values().stream()
                .sorted(Comparator.comparing(EchoWorldgenStructureDefinition::id))
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
            throw new IllegalStateException("Worldgen structure registry is frozen");
        }
    }
}
