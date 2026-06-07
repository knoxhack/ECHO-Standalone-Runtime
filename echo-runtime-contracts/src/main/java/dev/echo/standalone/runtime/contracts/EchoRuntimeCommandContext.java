package dev.echo.standalone.runtime.contracts;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EchoRuntimeCommandContext(
        String commandLine,
        String commandName,
        List<String> args,
        Map<String, String> attributes
) {
    public EchoRuntimeCommandContext {
        commandLine = commandLine == null ? "" : commandLine;
        commandName = commandName == null ? "" : commandName;
        Objects.requireNonNull(args, "args");
        Objects.requireNonNull(attributes, "attributes");
        args = List.copyOf(args);
        attributes = Map.copyOf(attributes);
    }
}
