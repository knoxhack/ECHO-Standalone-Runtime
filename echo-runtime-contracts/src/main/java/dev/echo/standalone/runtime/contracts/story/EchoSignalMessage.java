package dev.echo.standalone.runtime.contracts.story;

public record EchoSignalMessage(
        String id,
        String source,
        String body,
        String missionId
) {
    public EchoSignalMessage {
        id = EchoStoryText.requireText(id, "id");
        source = EchoStoryText.requireText(source, "source");
        body = EchoStoryText.requireText(body, "body");
        missionId = EchoStoryText.requireText(missionId, "missionId");
    }
}
