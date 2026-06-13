package dev.echo.standalone.runtime.client;

record EchoClientProjectileResult(
        boolean fired,
        boolean consumedProjectile,
        String projectileItemId,
        int projectileCountBefore,
        int projectileCountAfter,
        String damageSourceId,
        EchoClientEntityAttackResult attack,
        String reason
) {
    EchoClientProjectileResult {
        projectileItemId = projectileItemId == null ? "" : projectileItemId.trim();
        projectileCountBefore = Math.max(0, projectileCountBefore);
        projectileCountAfter = Math.max(0, projectileCountAfter);
        damageSourceId = damageSourceId == null ? "" : damageSourceId.trim();
        attack = attack == null ? EchoClientEntityAttackResult.miss(reason) : attack;
        reason = reason == null || reason.isBlank() ? attack.reason() : reason.trim();
    }

    static EchoClientProjectileResult miss(String reason) {
        return new EchoClientProjectileResult(
                false,
                false,
                "",
                0,
                0,
                "",
                EchoClientEntityAttackResult.miss(reason),
                reason
        );
    }

    boolean hit() {
        return attack.hit();
    }

    boolean killed() {
        return attack.killed();
    }
}
