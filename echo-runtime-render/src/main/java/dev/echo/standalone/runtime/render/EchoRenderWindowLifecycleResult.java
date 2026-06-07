package dev.echo.standalone.runtime.render;

import java.util.List;
import java.util.Objects;

public record EchoRenderWindowLifecycleResult(
        EchoRenderWindowState finalState,
        List<EchoRenderWindowEvent> events,
        boolean crashHandled,
        boolean closedSafely,
        String shutdownReason
) {
    public EchoRenderWindowLifecycleResult {
        Objects.requireNonNull(finalState, "finalState");
        Objects.requireNonNull(events, "events");
        events = List.copyOf(events);
        shutdownReason = shutdownReason == null ? "" : shutdownReason;
    }
}
