package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.gameplay.EchoGameplayHazardResult;

import java.util.Objects;

public record EchoPlayerHazardFeedback(
        double intensity,
        double exposureDelta,
        int healthDamage,
        String message
) {
    public EchoPlayerHazardFeedback {
        if (intensity < 0.0D) {
            throw new IllegalArgumentException("intensity must not be negative");
        }
        if (exposureDelta < 0.0D) {
            throw new IllegalArgumentException("exposureDelta must not be negative");
        }
        if (healthDamage < 0) {
            throw new IllegalArgumentException("healthDamage must not be negative");
        }
        message = message == null ? "" : message;
    }

    public static EchoPlayerHazardFeedback from(EchoGameplayHazardResult result) {
        Objects.requireNonNull(result, "result");
        String message = result.healthDamage() > 0
                ? "Toxic ash is damaging suit seals."
                : result.hazardIntensity() > 0.0D
                        ? "Toxic ash exposure rising."
                        : "Air is clear.";
        return new EchoPlayerHazardFeedback(
                result.hazardIntensity(),
                result.exposureDelta(),
                result.healthDamage(),
                message
        );
    }
}
