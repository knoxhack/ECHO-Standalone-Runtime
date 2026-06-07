package dev.echo.standalone.runtime.world;

public record EchoWorldWeatherField(
        String profileId,
        double temperatureCelsius,
        double windSpeed,
        double ashDensity,
        double visibility
) {
    public EchoWorldWeatherField {
        profileId = EchoWorldText.requireText(profileId, "profileId");
        if (visibility < 0.0D) {
            throw new IllegalArgumentException("visibility must not be negative");
        }
    }
}
