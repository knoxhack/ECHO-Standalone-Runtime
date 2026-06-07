package dev.echo.standalone.runtime.player;

import java.util.Objects;

public record EchoPlayerControllerRuntimeResult(
        EchoPlayerController controller,
        EchoPlayerCameraRig cameraRig,
        EchoPlayerInteractionTargeter targeter,
        EchoPlayerInventoryShortcuts shortcuts
) {
    public EchoPlayerControllerRuntimeResult {
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(cameraRig, "cameraRig");
        Objects.requireNonNull(targeter, "targeter");
        Objects.requireNonNull(shortcuts, "shortcuts");
    }
}
