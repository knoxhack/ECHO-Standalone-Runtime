package dev.echo.standalone.runtime.render;

import java.util.Objects;

public record EchoRenderWindowEvent(
        String eventId,
        EchoRenderWindowEventType type,
        EchoRenderWindowState state,
        String detail
) {
    public EchoRenderWindowEvent {
        eventId = EchoRenderText.requireText(eventId, "eventId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(state, "state");
        detail = detail == null ? "" : detail;
    }
}
