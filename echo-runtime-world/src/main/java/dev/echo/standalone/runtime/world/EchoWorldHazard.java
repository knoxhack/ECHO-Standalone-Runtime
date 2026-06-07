package dev.echo.standalone.runtime.world;

import java.util.Objects;

public record EchoWorldHazard(
        String id,
        String type,
        double intensity,
        EchoWorldPosition origin,
        int radiusCells
) {
    public EchoWorldHazard {
        id = EchoWorldText.requireText(id, "id");
        type = EchoWorldText.requireText(type, "type");
        if (intensity < 0.0D) {
            throw new IllegalArgumentException("intensity must not be negative");
        }
        Objects.requireNonNull(origin, "origin");
        if (radiusCells < 0) {
            throw new IllegalArgumentException("radiusCells must not be negative");
        }
    }
}
