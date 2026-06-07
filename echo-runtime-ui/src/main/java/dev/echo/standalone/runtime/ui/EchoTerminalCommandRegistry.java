package dev.echo.standalone.runtime.ui;

import dev.echo.standalone.runtime.contracts.EchoRuntimeCommand;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCommandContext;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCommandRegistry;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCommandResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoTerminalCommandRegistry {
    private final LinkedHashMap<String, EchoTerminalCommand> commands = new LinkedHashMap<>();
    private final EchoRuntimeCommandRegistry runtimeCommands;

    public EchoTerminalCommandRegistry() {
        this(new EchoRuntimeCommandRegistry());
    }

    public EchoTerminalCommandRegistry(EchoRuntimeCommandRegistry runtimeCommands) {
        this.runtimeCommands = Objects.requireNonNull(runtimeCommands, "runtimeCommands");
        registerDefaults();
    }

    public void register(EchoTerminalCommand command) {
        Objects.requireNonNull(command, "command");
        commands.put(command.name(), command);
        runtimeCommands.register(new EchoRuntimeCommand(command.name(), command.description(), context ->
                toRuntimeResult(command.handler().execute(new EchoTerminalCommandContext(
                        EchoUiTheme.defaultTerminal(),
                        context.commandLine(),
                        context.commandName(),
                        context.args()
                )))));
    }

    public void registerRuntime(EchoRuntimeCommand command) {
        runtimeCommands.register(command);
    }

    public Optional<EchoTerminalCommand> find(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(commands.get(name.toLowerCase(Locale.ROOT)));
    }

    public List<EchoTerminalCommand> commands() {
        return List.copyOf(commands.values());
    }

    public List<EchoRuntimeCommand> runtimeCommands() {
        return runtimeCommands.commands();
    }

    public EchoTerminalCommandResult execute(EchoTerminalCommandContext context) {
        Optional<EchoTerminalCommand> command = find(context.commandName());
        if (command.isEmpty()) {
            EchoRuntimeCommandResult runtimeResult = runtimeCommands.execute(new EchoRuntimeCommandContext(
                    context.commandLine(),
                    context.commandName(),
                    context.args(),
                    Map.of(
                            "ui.theme.id", context.theme().id(),
                            "ui.theme.density", context.theme().density()
                    )
            ));
            return toTerminalResult(runtimeResult);
        }
        return command.get().handler().execute(context);
    }

    public EchoRuntimeCommandRegistry runtimeRegistry() {
        return runtimeCommands;
    }

    private void registerDefaults() {
        register(new EchoTerminalCommand("help", "List available commands", context -> {
            ArrayList<String> lines = new ArrayList<>();
            lines.add("available commands:");
            commands.values().forEach(command -> lines.add(command.name() + " - " + command.description()));
            return EchoTerminalCommandResult.output(lines);
        }));
        register(new EchoTerminalCommand("echo", "Print text", context ->
                EchoTerminalCommandResult.output(String.join(" ", context.args()))));
        register(new EchoTerminalCommand("clear", "Clear terminal output", context ->
                EchoTerminalCommandResult.clear()));
        register(new EchoTerminalCommand("status", "Print runtime UI status", context ->
                EchoTerminalCommandResult.output(EchoAgent5UiDataSources.reference().terminalReadyLine())));
        register(new EchoTerminalCommand("theme", "Print active theme", context ->
                EchoTerminalCommandResult.output(List.of(
                        "theme.id=" + context.theme().id(),
                        "theme.accent=" + context.theme().accentColor(),
                        "theme.density=" + context.theme().density()
                ))));
    }

    private static EchoRuntimeCommandResult toRuntimeResult(EchoTerminalCommandResult result) {
        return new EchoRuntimeCommandResult(
                result.outputLines(),
                true,
                result.clearRequested(),
                result.closeRequested()
        );
    }

    private static EchoTerminalCommandResult toTerminalResult(EchoRuntimeCommandResult result) {
        return new EchoTerminalCommandResult(
                result.outputLines(),
                result.clearRequested(),
                result.closeRequested()
        );
    }
}
