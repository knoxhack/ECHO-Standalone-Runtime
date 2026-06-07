package dev.echo.standalone.runtime.contracts.story;

public record EchoRitual(
        String id,
        String gameplayStat,
        int delta,
        String unlockFlagId
) {
    public EchoRitual {
        id = EchoStoryText.requireText(id, "id");
        gameplayStat = EchoStoryText.requireText(gameplayStat, "gameplayStat");
        unlockFlagId = EchoStoryText.requireText(unlockFlagId, "unlockFlagId");
    }
}
