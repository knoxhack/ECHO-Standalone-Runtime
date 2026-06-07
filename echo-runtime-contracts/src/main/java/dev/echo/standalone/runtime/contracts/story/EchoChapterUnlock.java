package dev.echo.standalone.runtime.contracts.story;

public record EchoChapterUnlock(
        String id,
        String title,
        String requiredFlagId
) {
    public EchoChapterUnlock {
        id = EchoStoryText.requireText(id, "id");
        title = EchoStoryText.requireText(title, "title");
        requiredFlagId = EchoStoryText.requireText(requiredFlagId, "requiredFlagId");
    }
}
