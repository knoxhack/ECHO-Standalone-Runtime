package dev.echo.standalone.runtime.world;

import java.util.Objects;
import java.util.function.Function;

public final class EchoVoxelWorldProfiles {
    private EchoVoxelWorldProfiles() {
    }

    public static EchoVoxelWorldRuntimeProfile ashfallCrashSite(
            Function<String, EchoVoxelBlock> blockLookup,
            int streamRadius
    ) {
        EchoVoxelAshfallWorldGeneration.Blocks blocks =
                EchoVoxelAshfallWorldGeneration.Blocks.fromRegistry(Objects.requireNonNull(blockLookup, "blockLookup"));
        return new EchoVoxelWorldRuntimeProfile(
                EchoVoxelAshfallWorldGeneration.crashSiteProfile(blocks),
                streamRadius
        );
    }

    public static EchoVoxelWorldRuntimeProfile openlandsFirstHour(
            Function<String, EchoVoxelBlock> blockLookup,
            int streamRadius
    ) {
        EchoVoxelOpenlandsWorldGeneration.Blocks blocks =
                EchoVoxelOpenlandsWorldGeneration.Blocks.fromRegistry(Objects.requireNonNull(blockLookup, "blockLookup"));
        return new EchoVoxelWorldRuntimeProfile(
                EchoVoxelOpenlandsWorldGeneration.firstHourProfile(blocks),
                streamRadius
        );
    }
}
