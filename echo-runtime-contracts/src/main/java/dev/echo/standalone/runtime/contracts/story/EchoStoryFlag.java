package dev.echo.standalone.runtime.contracts.story;

public record EchoStoryFlag(
        String id,
        boolean value
) {
    public EchoStoryFlag {
        id = EchoStoryText.requireText(id, "id");
    }
}
