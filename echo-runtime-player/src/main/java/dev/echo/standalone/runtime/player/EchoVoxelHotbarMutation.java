package dev.echo.standalone.runtime.player;

import java.util.Objects;

public record EchoVoxelHotbarMutation(
        boolean changed,
        String reason,
        EchoVoxelHotbarSlot slot
) {
    public EchoVoxelHotbarMutation {
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(slot, "slot");
    }
}
