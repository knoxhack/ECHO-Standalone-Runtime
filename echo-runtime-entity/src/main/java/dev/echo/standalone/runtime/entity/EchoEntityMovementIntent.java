package dev.echo.standalone.runtime.entity;

import java.util.Objects;

public record EchoEntityMovementIntent(EchoEntityId entityId, int deltaX, int deltaZ) {
    public EchoEntityMovementIntent {
        Objects.requireNonNull(entityId, "entityId");
    }
}
