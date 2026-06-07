package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.input.EchoInputRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.Objects;

public final class EchoPlayerControllerRuntime {
    public EchoPlayerControllerRuntimeResult boot(
            EchoRuntimeServiceRegistry services,
            EchoWorldRuntimeResult world,
            EchoEntityRuntimeResult entities,
            EchoGameplayRuntimeResult gameplay,
            EchoItemRuntimeResult items,
            EchoInputRuntimeResult input,
            EchoEntityId playerId
    ) {
        Objects.requireNonNull(services, "services");
        EchoPlayerCameraRig cameraRig = new EchoPlayerCameraRig();
        EchoPlayerInteractionTargeter targeter = new EchoPlayerInteractionTargeter(world, 1);
        EchoPlayerInventoryShortcuts shortcuts = new EchoPlayerInventoryShortcuts(gameplay, items, playerId);
        EchoPlayerController controller = new EchoPlayerController(
                entities,
                gameplay,
                input,
                playerId,
                cameraRig,
                targeter,
                shortcuts
        );
        EchoPlayerControllerRuntimeResult result = new EchoPlayerControllerRuntimeResult(
                controller,
                cameraRig,
                targeter,
                shortcuts
        );
        services.register(EchoPlayerCameraRig.class, cameraRig);
        services.register(EchoPlayerInteractionTargeter.class, targeter);
        services.register(EchoPlayerInventoryShortcuts.class, shortcuts);
        services.register(EchoPlayerController.class, controller);
        services.register(EchoPlayerControllerRuntimeResult.class, result);
        return result;
    }
}
