package dev.echo.standalone.runtime.app;

public record EchoAshfallPlayableMissionStep(
        int index,
        String stepId,
        String title,
        String outcome
) {
    public EchoAshfallPlayableMissionStep {
        if (index < 0) {
            throw new IllegalArgumentException("index must not be negative");
        }
        stepId = EchoAppText.requireText(stepId, "stepId");
        title = EchoAppText.requireText(title, "title");
        outcome = EchoAppText.requireText(outcome, "outcome");
    }
}
