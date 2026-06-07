package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoCompatMigrationStep(
        String stepId,
        EchoCompatMigrationActionKind actionKind,
        String sourceId,
        String targetId,
        String description,
        boolean requiresBackup,
        boolean mutatesSource
) {
    public EchoCompatMigrationStep {
        stepId = EchoCompatText.requireText(stepId, "stepId");
        Objects.requireNonNull(actionKind, "actionKind");
        sourceId = EchoCompatText.optionalText(sourceId);
        targetId = EchoCompatText.optionalText(targetId);
        description = EchoCompatText.requireText(description, "description");
    }
}
