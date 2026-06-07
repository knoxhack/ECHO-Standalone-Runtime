package dev.echo.standalone.runtime.gameplay;

import java.util.Objects;

public record EchoGameplayMissionObjective(
        String objectiveId,
        String label,
        EchoGameplayObjectiveStatus status,
        int progress,
        int targetProgress
) {
    public EchoGameplayMissionObjective {
        objectiveId = EchoGameplayText.requireText(objectiveId, "objectiveId");
        label = EchoGameplayText.requireText(label, "label");
        Objects.requireNonNull(status, "status");
        if (targetProgress <= 0) {
            throw new IllegalArgumentException("targetProgress must be positive");
        }
        if (progress < 0 || progress > targetProgress) {
            throw new IllegalArgumentException("progress must be between zero and targetProgress");
        }
        if (progress == targetProgress) {
            status = EchoGameplayObjectiveStatus.COMPLETED;
        }
    }

    public boolean completed() {
        return status == EchoGameplayObjectiveStatus.COMPLETED;
    }

    public EchoGameplayMissionObjective complete() {
        return new EchoGameplayMissionObjective(
                objectiveId,
                label,
                EchoGameplayObjectiveStatus.COMPLETED,
                targetProgress,
                targetProgress
        );
    }
}
