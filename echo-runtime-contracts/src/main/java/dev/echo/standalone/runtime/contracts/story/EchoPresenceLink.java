package dev.echo.standalone.runtime.contracts.story;

public record EchoPresenceLink(
        String id,
        String state,
        String signalMessageId
) {
    public EchoPresenceLink {
        id = EchoStoryText.requireText(id, "id");
        state = EchoStoryText.requireText(state, "state");
        signalMessageId = EchoStoryText.requireText(signalMessageId, "signalMessageId");
    }
}
