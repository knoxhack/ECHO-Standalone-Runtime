package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntimeShutdownHook;

import java.util.Optional;

public final class EchoRuntimeShutdownController {
    private EchoRuntimeShutdownHook shutdownHook;

    public synchronized void requestStop(EchoRuntimeShutdownHook hook) {
        shutdownHook = hook;
    }

    public synchronized boolean stopRequested() {
        return shutdownHook != null;
    }

    public synchronized Optional<EchoRuntimeShutdownHook> shutdownHook() {
        return Optional.ofNullable(shutdownHook);
    }
}
