package dev.echo.standalone.runtime.entity;

import java.util.List;

/**
 * Data needed to spawn a creature from a content definition.
 */
public record EchoEntitySpawnDefinition(
        String creatureId,
        String displayName,
        String category,
        List<String> biomes,
        int health,
        int damage,
        String notes
) {
    public EchoEntitySpawnDefinition {
        creatureId = creatureId == null || creatureId.isBlank() ? "echo:unknown" : creatureId.trim();
        displayName = displayName == null || displayName.isBlank() ? creatureId : displayName.trim();
        category = category == null ? "" : category.trim();
        biomes = biomes == null ? List.of() : List.copyOf(biomes);
        health = Math.max(1, health);
        damage = Math.max(0, damage);
        notes = notes == null ? "" : notes.trim();
    }
}
