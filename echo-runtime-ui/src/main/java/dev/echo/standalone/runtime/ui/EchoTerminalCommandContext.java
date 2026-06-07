package dev.echo.standalone.runtime.ui;

import java.util.List;
import java.util.Objects;

public record EchoTerminalCommandContext(
        EchoUiTheme theme,
        String commandLine,
        String commandName,
        List<String> args
) {
    public EchoTerminalCommandContext {
        Objects.requireNonNull(theme, "theme");
        commandLine = commandLine == null ? "" : commandLine;
        commandName = commandName == null ? "" : commandName;
        Objects.requireNonNull(args, "args");
        args = List.copyOf(args);
    }
}
