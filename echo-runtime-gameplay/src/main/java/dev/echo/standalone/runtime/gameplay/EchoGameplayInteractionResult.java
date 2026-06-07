package dev.echo.standalone.runtime.gameplay;

public record EchoGameplayInteractionResult(
        String interactionId,
        boolean success,
        boolean objectiveCompleted,
        int experienceAwarded,
        String reason
) {
    public EchoGameplayInteractionResult {
        interactionId = EchoGameplayText.requireText(interactionId, "interactionId");
        if (experienceAwarded < 0) {
            throw new IllegalArgumentException("experienceAwarded must not be negative");
        }
        reason = EchoGameplayText.requireText(reason, "reason");
    }
}
