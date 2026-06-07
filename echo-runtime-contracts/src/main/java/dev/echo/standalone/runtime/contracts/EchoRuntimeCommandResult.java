package dev.echo.standalone.runtime.contracts;

import java.util.List;
import java.util.Objects;

public record EchoRuntimeCommandResult(
        List<String> outputLines,
        boolean handled,
        boolean clearRequested,
        boolean closeRequested
) {
    public EchoRuntimeCommandResult {
        Objects.requireNonNull(outputLines, "outputLines");
        outputLines = List.copyOf(outputLines);
    }

    public static EchoRuntimeCommandResult output(String line) {
        return new EchoRuntimeCommandResult(List.of(line), true, false, false);
    }

    public static EchoRuntimeCommandResult output(List<String> lines) {
        return new EchoRuntimeCommandResult(lines, true, false, false);
    }

    public static EchoRuntimeCommandResult clear() {
        return new EchoRuntimeCommandResult(List.of(), true, true, false);
    }

    public static EchoRuntimeCommandResult close() {
        return new EchoRuntimeCommandResult(List.of(), true, false, true);
    }

    public static EchoRuntimeCommandResult unknown(String commandName) {
        return new EchoRuntimeCommandResult(List.of("unknown command: " + commandName), false, false, false);
    }
}
