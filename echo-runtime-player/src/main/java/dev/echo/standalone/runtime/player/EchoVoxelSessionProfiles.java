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

    public static EchoVoxelSessionRuntimeProfile openlandsFirstHour(
            Function<String, EchoVoxelBlock> blockLookup,
            EchoVoxelBlock planks,
            EchoVoxelBlock campfire,
            int streamRadius
    ) {
        return new EchoVoxelSessionRuntimeProfile(
                EchoVoxelWorldProfiles.openlandsFirstHour(Objects.requireNonNull(blockLookup, "blockLookup"), streamRadius),
                EchoVoxelHotbarProfiles.openlandsStarter(
                        Objects.requireNonNull(planks, "planks"),
                        Objects.requireNonNull(campfire, "campfire")
                )
        );
    }
}
