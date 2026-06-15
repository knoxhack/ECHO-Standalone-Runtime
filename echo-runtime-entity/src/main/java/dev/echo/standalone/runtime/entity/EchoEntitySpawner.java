package dev.echo.standalone.runtime.entity;

import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight data-driven spawner that registers creature spawn definitions and can spawn matching
 * creatures into an {@link EchoEntityStore}.
 */
public final class EchoEntitySpawner {

    private final EchoEntityStore store;
    private final Map<String, EchoEntitySpawnDefinition> definitions = new LinkedHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1);

    public EchoEntitySpawner(EchoEntityStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public void register(EchoEntitySpawnDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        definitions.put(definition.creatureId(), definition);
    }

    public void registerAll(List<EchoEntitySpawnDefinition> definitions) {
        if (definitions == null) {
            return;
        }
        for (EchoEntitySpawnDefinition definition : definitions) {
            register(definition);
        }
    }

    public Optional<EchoEntitySpawnDefinition> find(String creatureId) {
        return Optional.ofNullable(definitions.get(creatureId));
    }

    public List<EchoEntitySpawnDefinition> definitions() {
        return List.copyOf(definitions.values());
    }

    /**
     * Spawns one creature matching the given biome id near the provided position.
     *
     * @return the spawned entity state, or empty if no definition matched
     */
    public Optional<EchoEntityState> spawnForBiome(String biomeId, EchoWorldPosition near) {
        if (biomeId == null || biomeId.isBlank()) {
            return Optional.empty();
        }
        List<EchoEntitySpawnDefinition> candidates = new ArrayList<>();
        for (EchoEntitySpawnDefinition definition : definitions.values()) {
            for (String candidateBiome : definition.biomes()) {
                if (biomeId.equals(candidateBiome) || biomeId.endsWith(":" + candidateBiome)) {
                    candidates.add(definition);
                    break;
                }
            }
        }
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        EchoEntitySpawnDefinition definition = candidates.get(sequence.get() % candidates.size());
        return Optional.of(spawn(definition, near));
    }

    public EchoEntityState spawn(EchoEntitySpawnDefinition definition, EchoWorldPosition position) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(position, "position");
        EchoEntityKind kind = definition.category().toLowerCase().contains("hostile")
                ? EchoEntityKind.HOSTILE
                : EchoEntityKind.NPC;
        EchoEntityDefinition entityDefinition = new EchoEntityDefinition(
                definition.creatureId(),
                definition.displayName(),
                kind,
                definition.health(),
                Math.max(1, definition.damage()),
                definition.category()
        );
        EchoEntityId id = new EchoEntityId(definition.creatureId() + "-" + sequence.getAndIncrement());
        EchoEntityState state = new EchoEntityState(
                id,
                entityDefinition,
                new EchoEntityPositionComponent(position),
                new EchoEntityHealthComponent(entityDefinition.maxHealth(), entityDefinition.maxHealth()),
                new EchoEntityMovementComponent(entityDefinition.movementSpeed(), true),
                new EchoEntityAiComponent(entityDefinition.aiProfile(), EchoEntityAiState.IDLE)
        );
        store.register(state);
        return state;
    }
}
