package dev.echo.standalone.runtime.data;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoWorldgenBiomeRegistry {
    private final LinkedHashMap<String, EchoWorldgenBiomeDefinition> biomes = new LinkedHashMap<>();
    private boolean frozen;

    public void register(EchoWorldgenBiomeDefinition biome) {
        ensureMutable();
        Objects.requireNonNull(biome, "biome");
        biomes.put(biome.id(), biome);
    }

    public Optional<EchoWorldgenBiomeDefinition> find(String id) {
        return Optional.ofNullable(biomes.get(EchoDataPaths.requireText(id, "id")));
    }

    public List<EchoWorldgenBiomeDefinition> biomes() {
        return biomes.values().stream()
                .sorted(Comparator.comparing(EchoWorldgenBiomeDefinition::id))
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
            throw new IllegalStateException("Worldgen biome registry is frozen");
        }
    }
}
