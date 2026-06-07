package dev.echo.standalone.runtime.player;

import java.util.Objects;

public record EchoVoxelHotbarTransferResult(
        boolean changed,
        String action,
        String reason,
        int sourceSlot,
        int targetSlot,
        int moved,
        EchoVoxelHotbarSlot source,
        EchoVoxelHotbarSlot target
) {
    public EchoVoxelHotbarTransferResult {
        action = Objects.requireNonNull(action, "action");
        reason = Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        if (sourceSlot < 0 || targetSlot < 0) {
            throw new IllegalArgumentException("slot indexes must not be negative");
        }
        if (moved < 0) {
            throw new IllegalArgumentException("moved must not be negative");
        }
    }
}
