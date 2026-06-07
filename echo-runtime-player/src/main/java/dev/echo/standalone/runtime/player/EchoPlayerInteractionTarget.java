package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.Objects;

public record EchoPlayerInteractionTarget(
        String id,
        String type,
        String label,
        EchoWorldPosition position,
        int distance,
        boolean exact,
        boolean facing
) {
    public EchoPlayerInteractionTarget {
        id = EchoPlayerText.requireText(id, "id");
        type = EchoPlayerText.requireText(type, "type");
        label = EchoPlayerText.requireText(label, "label");
        Objects.requireNonNull(position, "position");
        if (distance < 0) {
            throw new IllegalArgumentException("distance must not be negative");
        }
    }
}
