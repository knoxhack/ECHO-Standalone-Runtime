package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.render.EchoRenderCamera;

import java.util.Objects;

public final class EchoPlayerCameraRig {
    public EchoRenderCamera follow(EchoEntityState player) {
        Objects.requireNonNull(player, "player");
        return new EchoRenderCamera(
                "ashfall-player-follow-camera",
                player.worldPosition().x() + 0.5D,
                3.0D,
                player.worldPosition().z() + 0.5D,
                1.15D,
                55.0D
        );
    }
}
