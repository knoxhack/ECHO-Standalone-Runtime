package dev.echo.standalone.runtime.world;

import java.util.Objects;

public record EchoWorldRuntimeResult(
        EchoWorldState world,
        EchoWorldQuery query,
        EchoWorldSaveHook saveHook
) {
    public EchoWorldRuntimeResult {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(saveHook, "saveHook");
    }
}
