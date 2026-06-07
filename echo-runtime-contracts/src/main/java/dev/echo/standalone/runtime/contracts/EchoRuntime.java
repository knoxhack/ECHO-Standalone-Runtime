package dev.echo.standalone.runtime.contracts;

public interface EchoRuntime extends AutoCloseable {
    EchoRuntimeContext context();

    EchoRuntimeLifecycle lifecycle();

    default EchoRuntimeMode mode() {
        return context().environment().mode();
    }

    default EchoRuntimePlatform platform() {
        return context().platform();
    }

    default EchoRuntimeCapabilities capabilities() {
        return context().capabilities();
    }

    void start();

    void requestStop(EchoRuntimeShutdownHook shutdownHook);

    @Override
    default void close() {
        requestStop(EchoRuntimeShutdownHook.noop("close"));
    }
}
