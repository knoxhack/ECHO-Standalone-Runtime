package dev.echo.standalone.runtime.modules;

public interface EchoRuntimeModuleEntrypoint {
    void onLoad(EchoRuntimeModuleContext context) throws Exception;

    default void onDataReload(EchoRuntimeModuleContext context) throws Exception {
    }

    default void onUnload(EchoRuntimeModuleContext context) throws Exception {
    }
}
