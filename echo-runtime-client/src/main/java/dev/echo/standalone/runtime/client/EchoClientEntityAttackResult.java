package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

record EchoClientEntityAttackResult(
        boolean hit,
        boolean killed,
        String entityId,
        String definitionId,
        String displayName,
        String entityKind,
        int damage,
        int healthBefore,
        int healthAfter,
        double distance,
        EchoWorldPosition position,
        String reason
) {
    EchoClientEntityAttackResult {
        entityId = entityId == null ? "" : entityId.trim();
        definitionId = definitionId == null ? "" : definitionId.trim();
        displayName = displayName == null || displayName.isBlank() ? definitionId : displayName.trim();
        entityKind = entityKind == null || entityKind.isBlank()
                ? "UNKNOWN"
                : entityKind.trim().toUpperCase(java.util.Locale.ROOT);
        damage = Math.max(0, damage);
        healthBefore = Math.max(0, healthBefore);
        healthAfter = Math.max(0, healthAfter);
        distance = Double.isFinite(distance) ? Math.max(0.0D, distance) : 0.0D;
        position = position == null ? new EchoWorldPosition(0, 0, 0) : position;
        reason = reason == null || reason.isBlank() ? (hit ? "hit" : "miss") : reason.trim();
    }

    static EchoClientEntityAttackResult miss(String reason) {
        return new EchoClientEntityAttackResult(
                false,
                false,
                "",
                "",
                "",
                "UNKNOWN",
                0,
                0,
                0,
                0.0D,
                new EchoWorldPosition(0, 0, 0),
                reason
        );
    }

    static EchoClientEntityAttackResult hit(
            EchoEntityState before,
            EchoEntityState after,
            int damage,
            double distance
    ) {
        return new EchoClientEntityAttackResult(
                true,
                after != null && !after.alive(),
                before.id().value(),
                before.definition().definitionId(),
                before.definition().displayName(),
                before.definition().kind().name(),
                damage,
                before.health().currentHealth(),
                after == null ? 0 : after.health().currentHealth(),
                distance,
                before.worldPosition(),
                after != null && !after.alive() ? "killed" : "damaged"
        );
    }
}
