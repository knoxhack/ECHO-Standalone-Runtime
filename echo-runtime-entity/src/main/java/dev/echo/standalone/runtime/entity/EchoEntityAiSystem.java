package dev.echo.standalone.runtime.entity;

import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public final class EchoEntityAiSystem {
    private static final String HOSTILE_SCAVENGER_PROFILE = "hostile_scavenger";
    private static final int HOSTILE_SCAVENGER_DAMAGE = 5;

    private final EchoEntityMovementSystem movementSystem;

    public EchoEntityAiSystem(EchoEntityMovementSystem movementSystem) {
        this.movementSystem = Objects.requireNonNull(movementSystem, "movementSystem");
    }

    public EchoEntityAiTickResult tick(EchoEntityStore store) {
        Objects.requireNonNull(store, "store");
        Optional<EchoEntityState> player = store.living().stream()
                .filter(entity -> entity.definition().kind() == EchoEntityKind.PLAYER)
                .findFirst();
        ArrayList<EchoEntityAiIntent> intents = new ArrayList<>();
        int movements = 0;
        int attacks = 0;

        for (EchoEntityState actor : store.hostile()) {
            if (!actor.alive() || !HOSTILE_SCAVENGER_PROFILE.equals(actor.ai().profile())) {
                continue;
            }
            if (player.isEmpty()) {
                store.update(actor.withAi(actor.ai().withState(EchoEntityAiState.IDLE)));
                intents.add(new EchoEntityAiIntent(actor.id(), Optional.empty(), Optional.empty(), EchoEntityAiState.IDLE));
                continue;
            }

            EchoEntityState target = player.orElseThrow();
            EchoWorldPosition from = actor.worldPosition();
            EchoWorldPosition to = target.worldPosition();
            int deltaX = Integer.compare(to.x(), from.x());
            int deltaZ = Integer.compare(to.z(), from.z());
            int distance = Math.max(Math.abs(to.x() - from.x()), Math.abs(to.z() - from.z()));

            if (distance <= 1) {
                EchoEntityState damagedTarget = target.withHealth(target.health().damage(HOSTILE_SCAVENGER_DAMAGE));
                store.update(damagedTarget);
                store.update(actor.withAi(actor.ai().withState(EchoEntityAiState.ATTACKING)));
                attacks++;
                intents.add(new EchoEntityAiIntent(
                        actor.id(),
                        Optional.of(target.id()),
                        Optional.empty(),
                        EchoEntityAiState.ATTACKING
                ));
                continue;
            }

            EchoEntityMovementIntent movement = new EchoEntityMovementIntent(actor.id(), deltaX, deltaZ);
            EchoEntityMovementResult moved = movementSystem.move(store, movement);
            EchoEntityState movedActor = store.require(actor.id());
            store.update(movedActor.withAi(movedActor.ai().withState(EchoEntityAiState.PURSUING)));
            if (moved.moved()) {
                movements++;
            }
            intents.add(new EchoEntityAiIntent(
                    actor.id(),
                    Optional.of(target.id()),
                    Optional.of(movement),
                    EchoEntityAiState.PURSUING
            ));
        }

        return new EchoEntityAiTickResult(intents, movements, attacks);
    }
}
