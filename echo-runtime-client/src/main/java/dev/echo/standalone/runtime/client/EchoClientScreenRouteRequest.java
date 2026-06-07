package dev.echo.standalone.runtime.client;

record EchoClientScreenRouteRequest(
        EchoClientScreenCommand command,
        String targetId
) {
    static final EchoClientScreenRouteRequest NONE =
            new EchoClientScreenRouteRequest(EchoClientScreenCommand.NONE, "");

    EchoClientScreenRouteRequest(EchoClientScreenCommand command) {
        this(command, "");
    }

    EchoClientScreenRouteRequest {
        command = command == null ? EchoClientScreenCommand.NONE : command;
        targetId = targetId == null ? "" : targetId.trim();
    }

    boolean active() {
        return command != EchoClientScreenCommand.NONE;
    }
}
