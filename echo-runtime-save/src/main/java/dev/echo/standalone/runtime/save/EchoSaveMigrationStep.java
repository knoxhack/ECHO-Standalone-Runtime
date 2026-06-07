package dev.echo.standalone.runtime.save;

public record EchoSaveMigrationStep(
        int targetVersion,
        String action,
        boolean requiresBackup
) {
    public EchoSaveMigrationStep {
        if (targetVersion < 1) {
            throw new IllegalArgumentException("targetVersion must be positive");
        }
        action = EchoSavePaths.requireText(action, "action");
    }
}
