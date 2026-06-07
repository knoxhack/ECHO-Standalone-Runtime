package dev.echo.standalone.runtime.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoDataRegistryStore {
    private final LinkedHashMap<String, EchoDataRegistry> registries = new LinkedHashMap<>();
    private boolean frozen;

    public EchoDataRegistry registryOrCreate(String registryId) {
        ensureMutable();
        return registries.computeIfAbsent(registryId, EchoDataRegistry::new);
    }

    public void register(EchoDataDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        registryOrCreate(definition.registryId()).register(definition);
    }

    public Optional<EchoDataRegistry> registry(String registryId) {
        return Optional.ofNullable(registries.get(registryId));
    }

    public List<EchoDataRegistry> registries() {
        return registries.values().stream()
                .sorted(java.util.Comparator.comparing(EchoDataRegistry::registryId))
                .toList();
    }

    public int totalEntries() {
        return registries.values().stream().mapToInt(EchoDataRegistry::size).sum();
    }

    public void freezeAll() {
        registries.values().forEach(EchoDataRegistry::freeze);
        frozen = true;
    }

    public boolean frozen() {
        return frozen && registries.values().stream().allMatch(EchoDataRegistry::frozen);
    }

    private void ensureMutable() {
        if (frozen) {
            throw new IllegalStateException("Data registry store is frozen");
        }
    }
}
