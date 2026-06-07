package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.contracts.EchoRuntimeCommand;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCommandContext;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCommandRegistry;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCommandResult;
import dev.echo.standalone.runtime.ui.EchoTerminalCommandRegistry;
import dev.echo.standalone.runtime.ui.EchoTerminalCommandResult;
import dev.echo.standalone.runtime.ui.EchoTerminalShell;
import dev.echo.standalone.runtime.ui.EchoUiTheme;

import java.util.List;
import java.util.Map;

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
}
