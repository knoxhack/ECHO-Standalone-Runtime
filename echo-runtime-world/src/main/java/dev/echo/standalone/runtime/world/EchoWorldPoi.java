package dev.echo.standalone.runtime.world;

import java.util.Objects;

public record EchoWorldPoi(
        String id,
        String type,
        String label,
        EchoWorldPosition position
) {
    public EchoWorldPoi {
        id = EchoWorldText.requireText(id, "id");
        type = EchoWorldText.requireText(type, "type");
        label = EchoWorldText.requireText(label, "label");
        Objects.requireNonNull(position, "position");
    }
}
