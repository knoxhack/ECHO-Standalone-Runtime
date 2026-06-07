package dev.echo.standalone.runtime.modules;

public interface EchoRuntimeAdapterCoreEntrypoint {
    void activate(EchoRuntimeModuleContext context) throws Exception;

    default void reloadData(EchoRuntimeModuleContext context) throws Exception {
    }

    default void deactivate(EchoRuntimeModuleContext context) throws Exception {
    }
}
