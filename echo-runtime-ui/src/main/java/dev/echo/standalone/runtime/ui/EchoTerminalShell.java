package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class EchoTerminalShell {
    private final EchoTerminalCommandRegistry commands;
    private final ArrayList<String> outputLines = new ArrayList<>();
    private final ArrayList<String> history = new ArrayList<>();
    private String promptPrefix = ">";

    public EchoTerminalShell() {
        this(new EchoTerminalCommandRegistry());
    }

    public EchoTerminalShell(EchoTerminalCommandRegistry commands) {
        this.commands = Objects.requireNonNull(commands, "commands");
        outputLines.add("ECHO Terminal ready");
    }

    public EchoTerminalCommandResult submit(String line, EchoUiTheme theme) {
        Objects.requireNonNull(theme, "theme");
        String commandLine = line == null ? "" : line.trim();
        if (commandLine.isBlank()) {
            return EchoTerminalCommandResult.output(List.of());
        }
        history.add(commandLine);
        outputLines.add(promptPrefix + " " + commandLine);
        ParsedCommand parsed = parse(commandLine);
        EchoTerminalCommandResult result = commands.execute(new EchoTerminalCommandContext(
                theme,
                commandLine,
                parsed.name(),
                parsed.args()
        ));
        if (result.clearRequested()) {
            outputLines.clear();
        }
        outputLines.addAll(result.outputLines());
        return result;
    }

    public List<String> outputLines() {
        return List.copyOf(outputLines);
    }

    public List<String> history() {
        return List.copyOf(history);
    }

    public EchoTerminalCommandRegistry commands() {
        return commands;
    }

    public void promptPrefix(String promptPrefix) {
        if (promptPrefix == null || promptPrefix.isBlank()) {
            throw new IllegalArgumentException("promptPrefix must not be blank");
        }
        this.promptPrefix = promptPrefix;
    }

    private static ParsedCommand parse(String commandLine) {
        String[] parts = commandLine.split("\\s+");
        String name = parts.length == 0 ? "" : parts[0];
        List<String> args = parts.length <= 1 ? List.of() : Arrays.asList(parts).subList(1, parts.length);
        return new ParsedCommand(name, args);
    }

    private record ParsedCommand(String name, List<String> args) {
        private ParsedCommand {
            name = name == null ? "" : name;
            args = List.copyOf(args);
        }
    }
}
