package dev.echo.standalone.runtime.ui;

import java.util.List;

public record EchoTerminalBuffer(
        List<String> lines
) {
    public EchoTerminalBuffer {
        lines = List.copyOf(lines);
    }

    public boolean contains(String token) {
        return lines.stream().anyMatch(line -> line.contains(token));
    }
}
