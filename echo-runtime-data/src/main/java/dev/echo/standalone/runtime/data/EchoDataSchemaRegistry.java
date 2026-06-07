package dev.echo.standalone.runtime.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoDataSchemaRegistry {
    private final LinkedHashMap<String, EchoDataSchema> schemasByRegistry = new LinkedHashMap<>();
    private boolean frozen;

    public void register(EchoDataSchema schema) {
        ensureMutable();
        Objects.requireNonNull(schema, "schema");
        schemasByRegistry.put(schema.registryId(), schema);
    }

    public Optional<EchoDataSchema> schemaFor(String registryId) {
        return Optional.ofNullable(schemasByRegistry.get(registryId));
    }

    public List<EchoDataSchema> schemas() {
        return schemasByRegistry.values().stream()
                .sorted(java.util.Comparator.comparing(EchoDataSchema::registryId))
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
            throw new IllegalStateException("Data schema registry is frozen");
        }
    }
}
