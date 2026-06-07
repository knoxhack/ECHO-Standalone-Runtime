package dev.echo.standalone.runtime.gameplay;

import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.Objects;

public final class EchoHazardGameplaySystem {
    private final EchoWorldRuntimeResult world;
    private final EchoEntityRuntimeResult entities;
    private final EchoSurvivalState survival;
    private final EchoNotificationLog notifications;

    public EchoHazardGameplaySystem(
            EchoWorldRuntimeResult world,
            EchoEntityRuntimeResult entities,
            EchoSurvivalState survival,
            EchoNotificationLog notifications
    ) {
        this.world = Objects.requireNonNull(world, "world");
        this.entities = Objects.requireNonNull(entities, "entities");
        this.survival = Objects.requireNonNull(survival, "survival");
        this.notifications = Objects.requireNonNull(notifications, "notifications");
    }

    public EchoGameplayHazardResult apply(EchoEntityId entityId) {
        Objects.requireNonNull(entityId, "entityId");
        EchoEntityState entity = entities.store().require(entityId);
        double intensity = world.query().hazardIntensityAt(entity.worldPosition());
        double exposureDelta = EchoSurvivalState.round(intensity * 10.0D);
        int damage = intensity >= 0.70D ? 4 : intensity > 0.0D ? 2 : 0;
        if (exposureDelta > 0.0D) {
            survival.addAshExposure(exposureDelta);
        }
        if (damage > 0) {
            entities.store().update(entity.withHealth(entity.health().damage(damage)));
            notifications.add(
                    EchoGameplayNotificationSeverity.WARNING,
                    "Toxic ash exposure is damaging suit seals.",
                    world.world().tick()
            );
        }
        return new EchoGameplayHazardResult(entityId, intensity, exposureDelta, damage);
    }
}
