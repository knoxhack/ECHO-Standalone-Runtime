package dev.echo.standalone.runtime.ui;

import java.util.List;
import java.util.Objects;

public record EchoTerminalCommandResult(
        List<String> outputLines,
        boolean clearRequested,
        boolean closeRequested
) {
    public EchoTerminalCommandResult {
        Objects.requireNonNull(outputLines, "outputLines");
        outputLines = List.copyOf(outputLines);
    }

    public static EchoTerminalCommandResult output(String line) {
        return new EchoTerminalCommandResult(List.of(line), false, false);
    }

    public static EchoTerminalCommandResult output(List<String> lines) {
        return new EchoTerminalCommandResult(lines, false, false);
    }

    public static EchoTerminalCommandResult clear() {
        return new EchoTerminalCommandResult(List.of(), true, false);
    }
}
