package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.entity.EchoEntityAiComponent;
import dev.echo.standalone.runtime.entity.EchoEntityAiState;
import dev.echo.standalone.runtime.entity.EchoEntityDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityHealthComponent;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityMovementComponent;
import dev.echo.standalone.runtime.entity.EchoEntityPositionComponent;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

record EchoClientEntitySnapshot(
        String entityId,
        String definitionId,
        String displayName,
        EchoEntityKind kind,
        int maxHealth,
        int movementSpeed,
        String aiProfile,
        int x,
        int y,
        int z,
        int currentHealth,
        int healthMax,
        boolean blockedByWorld,
        EchoEntityAiState aiState
) {
    EchoClientEntitySnapshot {
        entityId = requireText(entityId, "entityId");
        definitionId = requireText(definitionId, "definitionId");
        displayName = displayName == null || displayName.isBlank() ? definitionId : displayName.trim();
        kind = kind == null ? EchoEntityKind.HOSTILE : kind;
        maxHealth = Math.max(1, maxHealth);
        movementSpeed = Math.max(0, movementSpeed);
        aiProfile = requireText(aiProfile, "aiProfile");
        currentHealth = Math.max(0, Math.min(currentHealth, Math.max(1, healthMax)));
        healthMax = Math.max(1, healthMax);
        aiState = aiState == null ? EchoEntityAiState.IDLE : aiState;
    }

    static EchoClientEntitySnapshot fromEntity(EchoEntityState entity) {
        EchoEntityDefinition definition = entity.definition();
        EchoWorldPosition position = entity.worldPosition();
        return new EchoClientEntitySnapshot(
                entity.id().value(),
                definition.definitionId(),
                definition.displayName(),
                definition.kind(),
                definition.maxHealth(),
                definition.movementSpeed(),
                definition.aiProfile(),
                position.x(),
                position.y(),
                position.z(),
                entity.health().currentHealth(),
                entity.health().maxHealth(),
                entity.movement().blockedByWorld(),
                entity.ai().state()
        );
    }

    EchoEntityState entity() {
        EchoEntityDefinition definition = new EchoEntityDefinition(
                definitionId,
                displayName,
                kind,
                maxHealth,
                movementSpeed,
                aiProfile
        );
        return new EchoEntityState(
                new EchoEntityId(entityId),
                definition,
                new EchoEntityPositionComponent(new EchoWorldPosition(x, y, z)),
                new EchoEntityHealthComponent(currentHealth, healthMax),
                new EchoEntityMovementComponent(movementSpeed, blockedByWorld),
                new EchoEntityAiComponent(aiProfile, aiState)
        );
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
