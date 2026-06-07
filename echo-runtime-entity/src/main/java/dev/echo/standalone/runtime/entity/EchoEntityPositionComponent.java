package dev.echo.standalone.runtime.entity;

import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.Objects;

public record EchoEntityPositionComponent(EchoWorldPosition position) {
    public EchoEntityPositionComponent {
        Objects.requireNonNull(position, "position");
    }

    public EchoEntityPositionComponent moveTo(EchoWorldPosition target) {
        return new EchoEntityPositionComponent(target);
    }
}
