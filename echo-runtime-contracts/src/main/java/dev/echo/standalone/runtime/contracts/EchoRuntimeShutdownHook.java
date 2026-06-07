package dev.echo.standalone.runtime.contracts;

public record EchoRuntimeShutdownHook(String reason, boolean userRequested, boolean saveBeforeExit) {
    public EchoRuntimeShutdownHook {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
    }

    public static EchoRuntimeShutdownHook noop(String reason) {
        return new EchoRuntimeShutdownHook(reason, false, false);
    }

    public static EchoRuntimeShutdownHook userRequested(String reason) {
        return new EchoRuntimeShutdownHook(reason, true, true);
    }
}
