package dev.echo.standalone.runtime.contracts;

@FunctionalInterface
public interface EchoRuntimeCommandHandler {
    EchoRuntimeCommandResult execute(EchoRuntimeCommandContext context);
}
