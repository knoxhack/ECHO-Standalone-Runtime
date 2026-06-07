package dev.echo.standalone.runtime.item;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoInventoryStore {
    private final LinkedHashMap<EchoInventoryId, EchoInventoryContainer> containers = new LinkedHashMap<>();

    public synchronized void register(EchoInventoryContainer container) {
        Objects.requireNonNull(container, "container");
        if (containers.containsKey(container.id())) {
            throw new IllegalArgumentException("Duplicate inventory id: " + container.id().value());
        }
        containers.put(container.id(), container);
    }

    public synchronized Optional<EchoInventoryContainer> find(EchoInventoryId id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(containers.get(id));
    }

    public synchronized EchoInventoryContainer require(EchoInventoryId id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("Unknown inventory id: " + id.value()));
    }

    public synchronized List<EchoInventoryContainer> all() {
        return List.copyOf(containers.values());
    }

    public synchronized int count() {
        return containers.size();
    }

    public synchronized int occupiedSlots() {
        return containers.values().stream()
                .mapToInt(EchoInventoryContainer::occupiedSlots)
                .sum();
    }
}
