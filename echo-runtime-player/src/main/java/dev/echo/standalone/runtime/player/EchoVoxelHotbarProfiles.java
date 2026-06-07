package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.List;
import java.util.Objects;

public final class EchoVoxelHotbarProfiles {
    private EchoVoxelHotbarProfiles() {
    }

    public static EchoVoxelPlayerHotbar ashfallStarter(EchoVoxelBlock buildBlock) {
        Objects.requireNonNull(buildBlock, "buildBlock");
        return new EchoVoxelPlayerHotbar(
                List.of(new EchoVoxelHotbarSlot(0, buildBlock, 12)),
                0
        );
    }
}
