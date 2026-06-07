package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;
import dev.echo.standalone.runtime.world.EchoVoxelWorldStreamer;

import java.util.Objects;

public record EchoVoxelSessionRuntimeProfile(
        EchoVoxelWorldRuntimeProfile worldProfile,
        EchoVoxelPlayerHotbar starterHotbar
) {
    public EchoVoxelSessionRuntimeProfile {
        Objects.requireNonNull(worldProfile, "worldProfile");
        Objects.requireNonNull(starterHotbar, "starterHotbar");
    }

    public EchoVoxelWorld generate(long seed, int tick) {
        return worldProfile.generate(seed, tick);
    }

    public EchoVoxelWorld generateAndStream(long seed) {
        return worldProfile.generateAndStream(seed);
    }

    public EchoVoxelWorldStreamer streamer() {
        return worldProfile.streamer();
    }

    public EchoVoxelPlayerHotbar newStarterHotbar() {
        return starterHotbar.copy();
    }
}
