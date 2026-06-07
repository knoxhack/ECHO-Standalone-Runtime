package dev.echo.standalone.runtime.ui;

@FunctionalInterface
public interface EchoTerminalCommandHandler {
    EchoTerminalCommandResult execute(EchoTerminalCommandContext context);
}
