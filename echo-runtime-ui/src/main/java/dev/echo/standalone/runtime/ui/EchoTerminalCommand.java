package dev.echo.standalone.runtime.ui;

import java.util.Objects;

public record EchoTerminalCommand(
        String name,
        String description,
        EchoTerminalCommandHandler handler
) {
    public EchoTerminalCommand {
        name = requireText(name, "name").toLowerCase(java.util.Locale.ROOT);
        description = requireText(description, "description");
        Objects.requireNonNull(handler, "handler");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
