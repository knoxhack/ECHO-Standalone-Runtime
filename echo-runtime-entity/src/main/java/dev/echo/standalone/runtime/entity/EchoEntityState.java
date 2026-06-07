package dev.echo.standalone.runtime.entity;

import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.Objects;

public record EchoEntityState(
        EchoEntityId id,
        EchoEntityDefinition definition,
        EchoEntityPositionComponent position,
        EchoEntityHealthComponent health,
        EchoEntityMovementComponent movement,
        EchoEntityAiComponent ai
) {
    public EchoEntityState {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(health, "health");
        Objects.requireNonNull(movement, "movement");
        Objects.requireNonNull(ai, "ai");
    }

    public boolean alive() {
        return health.alive();
    }

    public boolean hostile() {
        return definition.kind() == EchoEntityKind.HOSTILE;
    }

    public EchoWorldPosition worldPosition() {
        return position.position();
    }

    public EchoEntityState withPosition(EchoWorldPosition target) {
        return new EchoEntityState(id, definition, position.moveTo(target), health, movement, ai);
    }

    public EchoEntityState withHealth(EchoEntityHealthComponent nextHealth) {
        return new EchoEntityState(id, definition, position, nextHealth, movement, ai);
    }

    public EchoEntityState withAi(EchoEntityAiComponent nextAi) {
        return new EchoEntityState(id, definition, position, health, movement, nextAi);
    }
}
