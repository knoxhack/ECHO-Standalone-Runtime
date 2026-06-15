package dev.echo.standalone.runtime.entity;

import java.util.Objects;

public record EchoEntityRuntimeResult(
        EchoEntityStore store,
        EchoEntityMovementSystem movementSystem,
        EchoEntityAiSystem aiSystem,
        EchoEntitySaveHook saveHook,
        EchoEntitySpawner spawner
) {
    public EchoEntityRuntimeResult {
        Objects.requireNonNull(store, "store");
        Objects.requireNonNull(movementSystem, "movementSystem");
        Objects.requireNonNull(aiSystem, "aiSystem");
        Objects.requireNonNull(saveHook, "saveHook");
        Objects.requireNonNull(spawner, "spawner");
    }
}
