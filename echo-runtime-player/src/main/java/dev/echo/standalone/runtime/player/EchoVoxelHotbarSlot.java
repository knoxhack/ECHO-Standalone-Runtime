package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.Objects;

public record EchoVoxelHotbarSlot(
        int index,
        EchoVoxelBlock block,
        int count
) {
    public EchoVoxelHotbarSlot {
        if (index < 0) {
            throw new IllegalArgumentException("index must not be negative");
        }
        Objects.requireNonNull(block, "block");
        if (block.air() && count > 0) {
            throw new IllegalArgumentException("air slots cannot have count");
        }
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
    }

    public boolean empty() {
        return block.air() || count == 0;
    }

    public String label() {
        return empty() ? "Empty" : block.displayName();
    }
}
