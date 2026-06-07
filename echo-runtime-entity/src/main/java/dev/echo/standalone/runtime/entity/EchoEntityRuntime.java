package dev.echo.standalone.runtime.entity;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.world.EchoWorldPosition;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.Objects;

public final class EchoEntityRuntime {
    public EchoEntityRuntimeResult createDebugEntities(
            EchoRuntimeServiceRegistry services,
            EchoWorldRuntimeResult world
    ) {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(world, "world");

        EchoEntityStore store = new EchoEntityStore();
        EchoEntityDefinition player = new EchoEntityDefinition(
                "echo:debug_player",
                "Debug Survivor",
                EchoEntityKind.PLAYER,
                100,
                3,
                "manual"
        );
        EchoEntityDefinition scavenger = new EchoEntityDefinition(
                "ashfall:hostile_scavenger",
                "Hostile Scavenger",
                EchoEntityKind.HOSTILE,
                35,
                1,
                "hostile_scavenger"
        );

        store.register(new EchoEntityState(
                new EchoEntityId("player-001"),
                player,
                new EchoEntityPositionComponent(new EchoWorldPosition(0, 0, 0)),
                new EchoEntityHealthComponent(player.maxHealth(), player.maxHealth()),
                new EchoEntityMovementComponent(player.movementSpeed(), true),
                new EchoEntityAiComponent(player.aiProfile(), EchoEntityAiState.IDLE)
        ));
        store.register(new EchoEntityState(
                new EchoEntityId("scavenger-001"),
                scavenger,
                new EchoEntityPositionComponent(new EchoWorldPosition(3, 0, 1)),
                new EchoEntityHealthComponent(scavenger.maxHealth(), scavenger.maxHealth()),
                new EchoEntityMovementComponent(scavenger.movementSpeed(), true),
                new EchoEntityAiComponent(scavenger.aiProfile(), EchoEntityAiState.IDLE)
        ));

        EchoEntityMovementSystem movementSystem = new EchoEntityMovementSystem(world.query());
        EchoEntityAiSystem aiSystem = new EchoEntityAiSystem(movementSystem);
        EchoEntitySaveHook saveHook = new EchoEntitySaveHook(store);
        EchoEntityRuntimeResult result = new EchoEntityRuntimeResult(store, movementSystem, aiSystem, saveHook);
        services.register(EchoEntityRuntimeResult.class, result);
        services.register(EchoEntityStore.class, store);
        services.register(EchoEntityMovementSystem.class, movementSystem);
        services.register(EchoEntityAiSystem.class, aiSystem);
        services.register(EchoEntitySaveHook.class, saveHook);
        return result;
    }
}
