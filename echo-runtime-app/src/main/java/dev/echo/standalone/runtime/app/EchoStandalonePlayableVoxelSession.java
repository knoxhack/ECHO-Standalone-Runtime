package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.util.List;
import java.util.Objects;

record EchoStandalonePlayableVoxelSession(
        EchoStandalonePlayableVoxelResult result,
        EchoVoxelWorld world,
        EchoVoxelPlayerState player,
        EchoVoxelPlayerHotbar hotbar,
        EchoAshfallLiveMissionState mission,
        List<EchoStandalonePlayableVoxelEdit> edits
) {
    EchoStandalonePlayableVoxelSession {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(hotbar, "hotbar");
        Objects.requireNonNull(mission, "mission");
        Objects.requireNonNull(edits, "edits");
        edits = List.copyOf(edits);
    }
}
