package dev.echo.standalone.runtime.world;

import java.util.Objects;

public record EchoVoxelBlockInstance(
        int x,
        int y,
        int z,
        EchoVoxelBlock block,
        EchoVoxelBlockState state
) {
    public EchoVoxelBlockInstance(int x, int y, int z, EchoVoxelBlock block) {
        this(x, y, z, block, EchoVoxelBlockState.of(block));
    }

    public EchoVoxelBlockInstance {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(state, "state");
        if (!state.block().id().equals(block.id())) {
            throw new IllegalArgumentException("block instance state must match block id");
        }
    }
}
