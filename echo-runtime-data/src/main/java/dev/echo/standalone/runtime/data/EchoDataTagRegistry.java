package dev.echo.standalone.runtime.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoDataTagRegistry {
    private final LinkedHashMap<String, EchoDataTag> tags = new LinkedHashMap<>();
    private boolean frozen;

    public void register(EchoDataTag tag) {
        ensureMutable();
        Objects.requireNonNull(tag, "tag");
        tags.put(tag.id(), tag);
    }

    public Optional<EchoDataTag> find(String id) {
        return Optional.ofNullable(tags.get(id));
    }

    public List<EchoDataTag> tags() {
        return tags.values().stream()
                .sorted(java.util.Comparator.comparing(EchoDataTag::id))
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
            throw new IllegalStateException("Data tag registry is frozen");
        }
    }
}
