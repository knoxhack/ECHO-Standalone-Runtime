package dev.echo.standalone.runtime.world;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.util.Objects;

public final class EchoWorldRuntime {
    private final EchoWorldDebugGenerator generator;

    public EchoWorldRuntime() {
        this(new EchoWorldDebugGenerator());
    }

    public EchoWorldRuntime(EchoWorldDebugGenerator generator) {
        this.generator = Objects.requireNonNull(generator, "generator");
    }

    public EchoWorldRuntimeResult createDebugWorld(
            EchoRuntimeServiceRegistry services,
            EchoWorldGenerationSettings settings
    ) {
        Objects.requireNonNull(services, "services");
        EchoWorldState world = generator.generate(settings);
        EchoWorldQuery query = new EchoWorldQuery(world);
        EchoWorldSaveHook saveHook = new EchoWorldSaveHook(world);
        EchoWorldRuntimeResult result = new EchoWorldRuntimeResult(world, query, saveHook);
        services.register(EchoWorldRuntimeResult.class, result);
        services.register(EchoWorldState.class, world);
        services.register(EchoWorldQuery.class, query);
        services.register(EchoWorldSaveHook.class, saveHook);
        return result;
    }
}
