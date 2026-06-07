package dev.echo.standalone.runtime.contracts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class EchoRuntimeCommandRegistry {
    private final LinkedHashMap<String, EchoRuntimeCommand> commands = new LinkedHashMap<>();

    public void register(EchoRuntimeCommand command) {
        Objects.requireNonNull(command, "command");
        commands.put(command.name(), command);
    }

    public Optional<EchoRuntimeCommand> find(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(commands.get(name.toLowerCase(Locale.ROOT)));
    }

    public List<EchoRuntimeCommand> commands() {
        return List.copyOf(commands.values());
    }

    public EchoRuntimeCommandResult execute(EchoRuntimeCommandContext context) {
        Objects.requireNonNull(context, "context");
        Optional<EchoRuntimeCommand> command = find(context.commandName());
        if (command.isEmpty()) {
            return EchoRuntimeCommandResult.unknown(context.commandName());
        }
        return command.get().handler().execute(context);
    }

    public static EchoRuntimeCommandRegistry withDefaults() {
        EchoRuntimeCommandRegistry registry = new EchoRuntimeCommandRegistry();
        registry.register(new EchoRuntimeCommand("help", "List available commands", context -> {
            ArrayList<String> lines = new ArrayList<>();
            lines.add("available commands:");
            registry.commands.values().forEach(command -> lines.add(command.name() + " - " + command.description()));
            return EchoRuntimeCommandResult.output(lines);
        }));
        registry.register(new EchoRuntimeCommand("echo", "Print text", context ->
                EchoRuntimeCommandResult.output(String.join(" ", context.args()))));
        registry.register(new EchoRuntimeCommand("clear", "Clear command output", context ->
                EchoRuntimeCommandResult.clear()));
        registry.register(new EchoRuntimeCommand("status", "Print runtime command status", context ->
                EchoRuntimeCommandResult.output("runtime command registry ready")));
        return registry;
    }
}
