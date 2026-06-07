package dev.echo.standalone.runtime.contracts;

import java.util.Locale;
import java.util.Objects;

public record EchoRuntimeCommand(
        String name,
        String description,
        EchoRuntimeCommandHandler handler
) {
    public EchoRuntimeCommand {
        name = requireText(name, "name").toLowerCase(Locale.ROOT);
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
