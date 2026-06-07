package dev.echo.standalone.runtime.app;

public record EchoAshfallPlayableMissionObjective(
        String objectiveId,
        String label,
        boolean completed,
        String proof
) {
    public EchoAshfallPlayableMissionObjective {
        objectiveId = EchoAppText.requireText(objectiveId, "objectiveId");
        label = EchoAppText.requireText(label, "label");
        proof = EchoAppText.requireText(proof, "proof");
    }
}
