package dev.echo.standalone.runtime.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoDataRegistry {
    private final String registryId;
    private final LinkedHashMap<String, EchoDataDefinition> entries = new LinkedHashMap<>();
    private boolean frozen;

    public EchoDataRegistry(String registryId) {
        this.registryId = EchoDataPaths.requireText(registryId, "registryId");
    }

    public void register(EchoDataDefinition definition) {
        ensureMutable();
        Objects.requireNonNull(definition, "definition");
        if (!registryId.equals(definition.registryId())) {
            throw new IllegalArgumentException("Definition registry mismatch: " + definition.registryId());
        }
        entries.put(definition.id(), definition);
    }

    public Optional<EchoDataDefinition> find(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    public List<EchoDataDefinition> entries() {
        return entries.values().stream()
                .sorted(java.util.Comparator.comparing(EchoDataDefinition::id))
                .toList();
    }

    public List<String> ids() {
        return entries.keySet().stream().sorted().toList();
    }

    public String registryId() {
        return registryId;
    }

    public int size() {
        return entries.size();
    }

    public void freeze() {
        frozen = true;
    }

    public boolean frozen() {
        return frozen;
    }

    private void ensureMutable() {
        if (frozen) {
            throw new IllegalStateException("Data registry is frozen: " + registryId);
        }
    }
}
