package dev.echo.standalone.runtime.compat;

import java.util.List;
import java.util.Objects;

public record EchoCompatMigrationPlan(
        String planId,
        String sourceProfileId,
        String targetProfileId,
        EchoCompatMigrationPolicy policy,
        boolean blocked,
        List<EchoCompatMigrationStep> steps,
        EchoCompatValidationResult validation
) {
    public EchoCompatMigrationPlan {
        planId = EchoCompatText.requireText(planId, "planId");
        sourceProfileId = EchoCompatText.requireText(sourceProfileId, "sourceProfileId");
        targetProfileId = EchoCompatText.requireText(targetProfileId, "targetProfileId");
        Objects.requireNonNull(policy, "policy");
        Objects.requireNonNull(steps, "steps");
        steps = List.copyOf(steps);
        Objects.requireNonNull(validation, "validation");
        blocked = blocked || !validation.valid();
    }

    public int manualReviewStepCount() {
        return (int) steps.stream()
                .filter(step -> step.actionKind() == EchoCompatMigrationActionKind.MANUAL_REVIEW)
                .count();
    }

    public int mutationStepCount() {
        return (int) steps.stream()
                .filter(EchoCompatMigrationStep::mutatesSource)
                .count();
    }
}
