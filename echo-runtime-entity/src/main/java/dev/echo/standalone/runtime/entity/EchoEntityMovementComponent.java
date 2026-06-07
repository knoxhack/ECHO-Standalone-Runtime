package dev.echo.standalone.runtime.entity;

public record EchoEntityMovementComponent(int movementSpeed, boolean blockedByWorld) {
    public EchoEntityMovementComponent {
        if (movementSpeed < 0) {
            throw new IllegalArgumentException("movementSpeed must not be negative");
        }
    }
}
