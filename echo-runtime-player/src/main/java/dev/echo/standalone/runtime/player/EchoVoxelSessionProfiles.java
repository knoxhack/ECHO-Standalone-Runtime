package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelWorldProfiles;

import java.util.Objects;
import java.util.function.Function;

public final class EchoVoxelSessionProfiles {
    private EchoVoxelSessionProfiles() {
    }

    public static EchoVoxelSessionRuntimeProfile ashfallCrashSite(
            Function<String, EchoVoxelBlock> blockLookup,
            EchoVoxelBlock buildBlock,
            int streamRadius
    ) {
        return new EchoVoxelSessionRuntimeProfile(
                EchoVoxelWorldProfiles.ashfallCrashSite(blockLookup, streamRadius),
                EchoVoxelHotbarProfiles.ashfallStarter(Objects.requireNonNull(buildBlock, "buildBlock"))
        );
    }
}
