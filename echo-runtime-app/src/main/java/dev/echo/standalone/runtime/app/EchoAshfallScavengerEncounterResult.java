package dev.echo.standalone.runtime.app;

public record EchoAshfallScavengerEncounterResult(
        int attacks,
        int playerHealthBefore,
        int playerHealthAfter,
        boolean survived,
        boolean repelled,
        String resolution
) {
    public EchoAshfallScavengerEncounterResult {
        if (attacks < 0) {
            throw new IllegalArgumentException("attacks must not be negative");
        }
        if (playerHealthBefore < 0 || playerHealthAfter < 0) {
            throw new IllegalArgumentException("health values must not be negative");
        }
        resolution = EchoAppText.requireText(resolution, "resolution");
    }
}
