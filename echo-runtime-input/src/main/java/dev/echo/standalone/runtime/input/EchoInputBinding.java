package dev.echo.standalone.runtime.input;

import java.util.Objects;

public record EchoInputBinding(
        EchoInputContext context,
        EchoInputControl control,
        EchoInputAction action,
        String label
) {
    public EchoInputBinding {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(control, "control");
        Objects.requireNonNull(action, "action");
        label = label == null ? action.name().toLowerCase() : label;
    }
}
