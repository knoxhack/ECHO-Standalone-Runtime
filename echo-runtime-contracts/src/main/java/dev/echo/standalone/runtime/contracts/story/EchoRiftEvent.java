package dev.echo.standalone.runtime.contracts.story;

public record EchoRiftEvent(
        String id,
        String chapterId,
        String unlockFlagId
) {
    public EchoRiftEvent {
        id = EchoStoryText.requireText(id, "id");
        chapterId = EchoStoryText.requireText(chapterId, "chapterId");
        unlockFlagId = EchoStoryText.requireText(unlockFlagId, "unlockFlagId");
    }
}
