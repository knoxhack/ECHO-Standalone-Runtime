package dev.echo.standalone.runtime.contracts.story;

public record EchoRelicEffect(
        String id,
        String gameplayStat,
        int delta,
        String archiveEntryId
) {
    public EchoRelicEffect {
        id = EchoStoryText.requireText(id, "id");
        gameplayStat = EchoStoryText.requireText(gameplayStat, "gameplayStat");
        archiveEntryId = EchoStoryText.requireText(archiveEntryId, "archiveEntryId");
    }
}
