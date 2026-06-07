package dev.echo.standalone.runtime.input;

import java.util.Objects;

public record EchoInputActionEvent(
        long sequence,
        EchoInputContext context,
        EchoInputAction action,
        EchoInputEvent source
) {
    public EchoInputActionEvent {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(source, "source");
        if (sequence < 0) {
            throw new IllegalArgumentException("sequence must not be negative");
        }
    }
}
