package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.render.EchoRenderCamera;

import java.util.Objects;
import java.util.Optional;

public record EchoPlayerControllerState(
        EchoEntityState player,
        EchoPlayerFacing facing,
        EchoRenderCamera camera,
        Optional<EchoPlayerInteractionTarget> target,
        Optional<EchoPlayerHazardFeedback> hazard
) {
    public EchoPlayerControllerState {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(facing, "facing");
        Objects.requireNonNull(camera, "camera");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(hazard, "hazard");
    }
}
