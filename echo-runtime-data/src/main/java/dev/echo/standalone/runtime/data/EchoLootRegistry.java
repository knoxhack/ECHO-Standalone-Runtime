package dev.echo.standalone.runtime.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoLootRegistry {
    private final LinkedHashMap<String, EchoLootDefinition> lootTables = new LinkedHashMap<>();
    private boolean frozen;

    public void register(EchoLootDefinition loot) {
        ensureMutable();
        Objects.requireNonNull(loot, "loot");
        lootTables.put(loot.id(), loot);
    }

    public Optional<EchoLootDefinition> find(String id) {
        return Optional.ofNullable(lootTables.get(id));
    }

    public List<EchoLootDefinition> lootTables() {
        return lootTables.values().stream()
                .sorted(java.util.Comparator.comparing(EchoLootDefinition::id))
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
            throw new IllegalStateException("Loot registry is frozen");
        }
    }
}
