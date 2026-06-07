package dev.echo.standalone.runtime.app;

public record EchoAshfallPlayableMissionReward(
        String rewardId,
        String label,
        int experienceAwarded,
        String itemId,
        boolean granted
) {
    public EchoAshfallPlayableMissionReward {
        rewardId = EchoAppText.requireText(rewardId, "rewardId");
        label = EchoAppText.requireText(label, "label");
        if (experienceAwarded < 0) {
            throw new IllegalArgumentException("experienceAwarded must not be negative");
        }
        itemId = itemId == null ? "" : itemId;
    }
}
