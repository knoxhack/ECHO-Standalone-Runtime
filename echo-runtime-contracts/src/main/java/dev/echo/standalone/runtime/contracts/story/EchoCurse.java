package dev.echo.standalone.runtime.contracts.story;

public record EchoCurse(
        String id,
        String gameplayStat,
        int delta
) {
    public EchoCurse {
        id = EchoStoryText.requireText(id, "id");
        gameplayStat = EchoStoryText.requireText(gameplayStat, "gameplayStat");
    }
}
