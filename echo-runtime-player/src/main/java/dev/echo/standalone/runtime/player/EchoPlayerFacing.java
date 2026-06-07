package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.input.EchoInputAction;

import java.util.Optional;

public enum EchoPlayerFacing {
    NORTH(0, -1),
    SOUTH(0, 1),
    WEST(-1, 0),
    EAST(1, 0);

    private final int deltaX;
    private final int deltaZ;

    EchoPlayerFacing(int deltaX, int deltaZ) {
        this.deltaX = deltaX;
        this.deltaZ = deltaZ;
    }

    public int deltaX() {
        return deltaX;
    }

    public int deltaZ() {
        return deltaZ;
    }

    public static Optional<EchoPlayerFacing> fromAction(EchoInputAction action) {
        return switch (action) {
            case MOVE_NORTH -> Optional.of(NORTH);
            case MOVE_SOUTH -> Optional.of(SOUTH);
            case MOVE_WEST -> Optional.of(WEST);
            case MOVE_EAST -> Optional.of(EAST);
            default -> Optional.empty();
        };
    }
}
