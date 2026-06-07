package dev.echo.standalone.runtime.player;

import java.util.Objects;

public record EchoVoxelPlayerStep(
        EchoVoxelPlayerState previous,
        EchoVoxelPlayerState current,
        boolean moved,
        boolean jumped,
        boolean collidedHorizontal,
        boolean collidedVertical,
        String reason
) {
    public EchoVoxelPlayerStep {
        Objects.requireNonNull(previous, "previous");
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(reason, "reason");
    }
}
