package dev.echo.standalone.runtime.app;

public record EchoSaveProfileFlowSummary(
        String status,
        int visibleSlots,
        int healthySlots,
        int warningCount,
        int blockedSlotCount,
        int backupCount,
        int migrationStepCount,
        boolean newGameReady,
        boolean continueReady,
        boolean autosaveReady,
        boolean manualSaveReady,
        boolean restoreReady,
        boolean migrationPromptReady,
        boolean incompatibleModSetPromptReady
) {
    public EchoSaveProfileFlowSummary {
        status = EchoAppText.requireText(status, "status");
        if (visibleSlots < 0
                || healthySlots < 0
                || warningCount < 0
                || blockedSlotCount < 0
                || backupCount < 0
                || migrationStepCount < 0) {
            throw new IllegalArgumentException("save profile flow counters must not be negative");
        }
    }
}
