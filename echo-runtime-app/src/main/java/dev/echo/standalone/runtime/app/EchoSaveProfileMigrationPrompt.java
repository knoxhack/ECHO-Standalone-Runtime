package dev.echo.standalone.runtime.app;

public record EchoSaveProfileMigrationPrompt(
        String slotId,
        int fromVersion,
        int toVersion,
        int stepCount,
        boolean visible,
        boolean backupRequired,
        boolean manualApprovalRequired,
        boolean executesAutomatically,
        String message
) {
    public EchoSaveProfileMigrationPrompt {
        slotId = EchoAppText.requireText(slotId, "slotId");
        if (fromVersion < 1 || toVersion < 1 || stepCount < 0) {
            throw new IllegalArgumentException("migration prompt versions and counts must be positive");
        }
        message = EchoAppText.requireText(message, "message");
    }
}
