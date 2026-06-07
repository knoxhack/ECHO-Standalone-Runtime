package dev.echo.standalone.runtime.entity;

import dev.echo.standalone.runtime.world.EchoWorldCell;
import dev.echo.standalone.runtime.world.EchoWorldPosition;
import dev.echo.standalone.runtime.world.EchoWorldQuery;

import java.util.Objects;
import java.util.Optional;

public final class EchoEntityMovementSystem {
    private final EchoWorldQuery world;

    public EchoEntityMovementSystem(EchoWorldQuery world) {
        this.world = Objects.requireNonNull(world, "world");
    }

    public EchoEntityMovementResult move(EchoEntityStore store, EchoEntityMovementIntent intent) {
        Objects.requireNonNull(store, "store");
        Objects.requireNonNull(intent, "intent");
        EchoEntityState state = store.require(intent.entityId());
        EchoWorldPosition from = state.worldPosition();
        EchoWorldPosition to = new EchoWorldPosition(from.x() + intent.deltaX(), from.y(), from.z() + intent.deltaZ());

        if (!state.alive()) {
            return new EchoEntityMovementResult(state.id(), from, from, false, "entity_dead");
        }
        int requiredSpeed = Math.max(Math.abs(intent.deltaX()), Math.abs(intent.deltaZ()));
        if (requiredSpeed > state.movement().movementSpeed()) {
            return new EchoEntityMovementResult(state.id(), from, from, false, "exceeds_speed");
        }
        Optional<EchoWorldCell> cell = world.cellAt(to);
        if (cell.isEmpty()) {
            return new EchoEntityMovementResult(state.id(), from, from, false, "outside_world");
        }
        if (state.movement().blockedByWorld() && cell.orElseThrow().blocked()) {
            return new EchoEntityMovementResult(state.id(), from, from, false, "blocked_cell");
        }
        store.update(state.withPosition(to));
        return new EchoEntityMovementResult(state.id(), from, to, true, "moved");
    }
}
