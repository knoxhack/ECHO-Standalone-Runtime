package dev.echo.standalone.runtime.entity;

import java.util.Objects;
import java.util.Optional;

public record EchoEntityAiIntent(
        EchoEntityId actorId,
        Optional<EchoEntityId> targetId,
        Optional<EchoEntityMovementIntent> movement,
        EchoEntityAiState state
) {
    public EchoEntityAiIntent {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(movement, "movement");
        Objects.requireNonNull(state, "state");
    }
}
