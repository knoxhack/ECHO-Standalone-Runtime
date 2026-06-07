package dev.echo.standalone.runtime.network;

import dev.echo.standalone.runtime.entity.EchoEntityAiState;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.Objects;

public record EchoEntitySyncSnapshot(
        String entityId,
        String definitionId,
        EchoEntityKind kind,
        int x,
        int y,
        int z,
        int currentHealth,
        int maxHealth,
        EchoEntityAiState aiState
) {
    public EchoEntitySyncSnapshot {
        entityId = EchoNetworkText.requireText(entityId, "entityId");
        definitionId = EchoNetworkText.requireText(definitionId, "definitionId");
        Objects.requireNonNull(kind, "kind");
        if (currentHealth < 0) {
            throw new IllegalArgumentException("currentHealth must not be negative");
        }
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("maxHealth must be positive");
        }
        Objects.requireNonNull(aiState, "aiState");
    }

    public static EchoEntitySyncSnapshot from(EchoEntityState state) {
        Objects.requireNonNull(state, "state");
        EchoWorldPosition position = state.worldPosition();
        return new EchoEntitySyncSnapshot(
                state.id().value(),
                state.definition().definitionId(),
                state.definition().kind(),
                position.x(),
                position.y(),
                position.z(),
                state.health().currentHealth(),
                state.health().maxHealth(),
                state.ai().state()
        );
    }

    public String payload() {
        return entityId
                + "|definition=" + definitionId
                + "|kind=" + kind.name()
                + "|pos=" + x + "," + y + "," + z
                + "|health=" + currentHealth + "/" + maxHealth
                + "|ai=" + aiState.name();
    }
}
