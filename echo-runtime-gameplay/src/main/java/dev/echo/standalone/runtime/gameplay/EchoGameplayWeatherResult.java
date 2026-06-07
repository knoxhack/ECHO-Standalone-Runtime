package dev.echo.standalone.runtime.gameplay;

public record EchoGameplayWeatherResult(
        String profileId,
        double ashDensity,
        double heatStressDelta,
        double visibility
) {
    public EchoGameplayWeatherResult {
        profileId = EchoGameplayText.requireText(profileId, "profileId");
        if (ashDensity < 0.0D) {
            throw new IllegalArgumentException("ashDensity must not be negative");
        }
        if (heatStressDelta < 0.0D) {
            throw new IllegalArgumentException("heatStressDelta must not be negative");
        }
        if (visibility < 0.0D) {
            throw new IllegalArgumentException("visibility must not be negative");
        }
    }
}
