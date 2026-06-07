package dev.echo.standalone.runtime.app;

public record EchoAshfallPlayableMissionSummary(
        String missionId,
        String status,
        int completedObjectives,
        int totalObjectives,
        int stepCount,
        int playerHealth,
        double hydration,
        double ashExposure,
        int experience,
        int level,
        boolean rewardGranted,
        boolean failBranchCovered,
        boolean retryRecovered,
        boolean cleanExit
) {
    public EchoAshfallPlayableMissionSummary {
        missionId = EchoAppText.requireText(missionId, "missionId");
        status = EchoAppText.requireText(status, "status");
        if (completedObjectives < 0 || totalObjectives <= 0 || completedObjectives > totalObjectives) {
            throw new IllegalArgumentException("objective counts are invalid");
        }
        if (stepCount < 0 || playerHealth < 0 || experience < 0 || level <= 0) {
            throw new IllegalArgumentException("summary counters are invalid");
        }
        if (hydration < 0.0D || ashExposure < 0.0D) {
            throw new IllegalArgumentException("survival values must not be negative");
        }
    }
}
