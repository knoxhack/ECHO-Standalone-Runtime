package dev.echo.standalone.runtime.app;

public record EchoSaveProfileSlotSummary(
        String slotId,
        String displayName,
        String saveKind,
        String status,
        boolean canContinue,
        boolean selectedForContinue,
        boolean backupAvailable,
        int trackedFiles,
        int backupCount,
        String warningCode
) {
    public EchoSaveProfileSlotSummary {
        slotId = EchoAppText.requireText(slotId, "slotId");
        displayName = EchoAppText.requireText(displayName, "displayName");
        saveKind = EchoAppText.requireText(saveKind, "saveKind");
        status = EchoAppText.requireText(status, "status");
        if (trackedFiles < 0 || backupCount < 0) {
            throw new IllegalArgumentException("save slot counters must not be negative");
        }
        warningCode = warningCode == null ? "" : warningCode;
    }

    public boolean warningShown() {
        return !warningCode.isBlank();
    }
}
