package dev.echo.standalone.runtime.contracts.story;

public record EchoSpell(
        String id,
        String gameplayStat,
        int delta
) {
    public EchoSpell {
        id = EchoStoryText.requireText(id, "id");
        gameplayStat = EchoStoryText.requireText(gameplayStat, "gameplayStat");
    }
}
