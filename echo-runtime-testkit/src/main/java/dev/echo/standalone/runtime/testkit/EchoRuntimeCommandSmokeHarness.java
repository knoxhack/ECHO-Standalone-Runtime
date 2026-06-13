package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.contracts.EchoRuntimeCommand;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCommandContext;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCommandRegistry;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCommandResult;
import dev.echo.standalone.runtime.ui.EchoTerminalCommandRegistry;
import dev.echo.standalone.runtime.ui.EchoTerminalCommandResult;
import dev.echo.standalone.runtime.ui.EchoTerminalShell;
import dev.echo.standalone.runtime.ui.EchoUiTheme;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class EchoRuntimeCommandSmokeHarness {
    private EchoRuntimeCommandSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoRuntimeCommandRegistry commands = EchoRuntimeCommandRegistry.withDefaults();
        commands.register(new EchoRuntimeCommand("ashfall_status", "Report Ashfall mission status", context ->
                EchoRuntimeCommandResult.output(List.of(
                        "mission=secure_crash_site",
                        "args=" + context.args().size(),
                        "source=" + context.attributes().getOrDefault("source", "runtime")
                ))));

        EchoRuntimeCommandResult status = commands.execute(new EchoRuntimeCommandContext(
                "ashfall_status route",
                "ashfall_status",
                List.of("route"),
                Map.of("source", "smoke")
        ));
        EchoRuntimeCommandResult echo = commands.execute(new EchoRuntimeCommandContext(
                "echo survivor online",
                "echo",
                List.of("survivor", "online"),
                Map.of()
        ));
        EchoRuntimeCommandResult unknown = commands.execute(new EchoRuntimeCommandContext(
                "missing",
                "missing",
                List.of(),
                Map.of()
        ));
        require(status.handled(), "custom command should be handled");
        require(status.outputLines().contains("mission=secure_crash_site"), "custom command should produce mission output");
        require(echo.outputLines().equals(List.of("survivor online")), "default echo command should join args");
        require(!unknown.handled(), "unknown command should be marked unhandled");
        require(unknown.outputLines().equals(List.of("unknown command: missing")), "unknown command should produce diagnostic output");

        EchoTerminalCommandRegistry terminalCommands = new EchoTerminalCommandRegistry(commands);
        EchoTerminalShell shell = new EchoTerminalShell(terminalCommands);
        EchoTerminalCommandResult bridged = shell.submit("ashfall_status terminal", EchoUiTheme.defaultTerminal());
        require(bridged.outputLines().contains("mission=secure_crash_site"),
                "terminal should execute runtime command registry commands");
        require(shell.outputLines().stream().anyMatch(line -> line.contains("mission=secure_crash_site")),
                "terminal shell should append runtime command output");
        require(terminalCommands.runtimeCommands().stream().anyMatch(command -> command.name().equals("ashfall_status")),
                "terminal command registry should expose runtime commands");
        writeReport(commands, status, echo, unknown, bridged, terminalCommands, shell);

        System.out.println("runtime command smoke PASS commands="
                + commands.commands().size()
                + " terminalBridge=true handled="
                + status.handled()
                + " unknownHandled="
                + unknown.handled());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void writeReport(
            EchoRuntimeCommandRegistry commands,
            EchoRuntimeCommandResult status,
            EchoRuntimeCommandResult echo,
            EchoRuntimeCommandResult unknown,
            EchoTerminalCommandResult bridged,
            EchoTerminalCommandRegistry terminalCommands,
            EchoTerminalShell shell
    ) {
        Path report = Path.of("reports", "echo", "standalone", "runtime-commands.json");
        try {
            Files.createDirectories(report.getParent());
            Files.writeString(report, """
                    {
                      "schema": "echo.standalone.runtime_commands.v1",
                      "status": "PASS",
                      "generatedAt": "1970-01-01T00:00:00Z",
                      "generator": "EchoRuntimeCommandSmokeHarness",
                      "commandCount": %d,
                      "commandNames": %s,
                      "customCommandHandled": %s,
                      "customCommandOutput": %s,
                      "defaultEchoHandled": %s,
                      "defaultEchoOutput": %s,
                      "unknownCommandHandled": %s,
                      "unknownCommandDiagnostic": %s,
                      "terminalBridgeExecuted": %s,
                      "terminalRuntimeCommandsExposed": %s,
                      "terminalHistoryCount": %d,
                      "terminalOutputContainsRuntimeCommand": %s
                    }
                    """.formatted(
                    commands.commands().size(),
                    stringArray(commands.commands().stream()
                            .map(EchoRuntimeCommand::name)
                            .sorted()
                            .toList()),
                    status.handled(),
                    stringArray(status.outputLines()),
                    echo.handled(),
                    stringArray(echo.outputLines()),
                    unknown.handled(),
                    stringArray(unknown.outputLines()),
                    bridged.outputLines().contains("mission=secure_crash_site"),
                    terminalCommands.runtimeCommands().stream().anyMatch(command -> command.name().equals("ashfall_status")),
                    shell.history().size(),
                    shell.outputLines().stream().anyMatch(line -> line.contains("mission=secure_crash_site"))
            ), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write runtime command report", exception);
        }
    }

    private static String stringArray(List<String> values) {
        return values.stream()
                .map(EchoRuntimeCommandSmokeHarness::quote)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
