package dev.echo.standalone.runtime.app;

public record EchoAshfallVerticalSliceSummary(
        String sliceId,
        int completedObjectives,
        int totalObjectives,
        int playerHealth,
        double hazardIntensity,
        double hydration,
        double ashExposure,
        double heatStress,
        int inventoryContainers,
        int occupiedSlots,
        int renderCommands,
        int audioEvents,
        int networkPackets,
        int ruleMatches,
        int migrationSteps,
        int saveFiles,
        int notifications,
        boolean cleanExit
) {
    public EchoAshfallVerticalSliceSummary {
        sliceId = requireText(sliceId, "sliceId");
        if (completedObjectives < 0 || totalObjectives < 0 || completedObjectives > totalObjectives) {
            throw new IllegalArgumentException("objective counts are invalid");
        }
        if (playerHealth < 0) {
            throw new IllegalArgumentException("playerHealth must not be negative");
        }
        if (hazardIntensity < 0.0D || hydration < 0.0D || ashExposure < 0.0D || heatStress < 0.0D) {
            throw new IllegalArgumentException("meter values must not be negative");
        }
        if (inventoryContainers < 0 || occupiedSlots < 0 || renderCommands < 0 || audioEvents < 0
                || networkPackets < 0 || ruleMatches < 0 || migrationSteps < 0 || saveFiles < 0
                || notifications < 0) {
            throw new IllegalArgumentException("summary counts must not be negative");
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
