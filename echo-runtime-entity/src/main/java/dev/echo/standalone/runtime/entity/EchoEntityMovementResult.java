package dev.echo.standalone.runtime.entity;

import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.Objects;

public record EchoEntityMovementResult(
        EchoEntityId entityId,
        EchoWorldPosition from,
        EchoWorldPosition to,
        boolean moved,
        String reason
) {
    public EchoEntityMovementResult {
        Objects.requireNonNull(entityId, "entityId");
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        reason = EchoEntityText.requireText(reason, "reason");
    }
}
