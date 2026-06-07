package dev.echo.standalone.runtime.item;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoItemRegistry {
    private final LinkedHashMap<EchoItemId, EchoItemDefinition> definitions = new LinkedHashMap<>();

    public synchronized void register(EchoItemDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (definitions.containsKey(definition.id())) {
            throw new IllegalArgumentException("Duplicate item definition: " + definition.id().value());
        }
        definitions.put(definition.id(), definition);
    }

    public synchronized Optional<EchoItemDefinition> find(EchoItemId id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(definitions.get(id));
    }

    public synchronized EchoItemDefinition require(EchoItemId id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("Unknown item id: " + id.value()));
    }

    public synchronized List<EchoItemDefinition> all() {
        return List.copyOf(definitions.values());
    }

    public synchronized List<EchoItemDefinition> tagged(String tag) {
        String normalized = EchoItemText.requireText(tag, "tag");
        return definitions.values().stream()
                .filter(definition -> definition.tagged(normalized))
                .toList();
    }

    public synchronized int count() {
        return definitions.size();
    }
}
