package dev.echo.standalone.runtime.gameplay;

import dev.echo.standalone.runtime.entity.EchoEntityId;

import java.util.Objects;

public record EchoGameplayHazardResult(
        EchoEntityId entityId,
        double hazardIntensity,
        double exposureDelta,
        int healthDamage
) {
    public EchoGameplayHazardResult {
        Objects.requireNonNull(entityId, "entityId");
        if (hazardIntensity < 0.0D) {
            throw new IllegalArgumentException("hazardIntensity must not be negative");
        }
        if (exposureDelta < 0.0D) {
            throw new IllegalArgumentException("exposureDelta must not be negative");
        }
        if (healthDamage < 0) {
            throw new IllegalArgumentException("healthDamage must not be negative");
        }
    }
}
