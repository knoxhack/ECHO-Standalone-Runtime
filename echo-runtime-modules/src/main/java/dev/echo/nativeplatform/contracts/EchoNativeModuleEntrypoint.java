package dev.echo.nativeplatform.contracts;

public interface EchoNativeModuleEntrypoint {
    default void discover(EchoNativeModuleLoadContext context) {
    }

    default void resolve(EchoNativeModuleLoadContext context) {
    }

    default void loadClasses(EchoNativeModuleLoadContext context) {
    }

    default void construct(EchoNativeModuleLoadContext context) {
    }

    default void registerServices(EchoNativeModuleLoadContext context) {
    }

    default void registerContent(EchoNativeModuleLoadContext context) {
    }

    default void commonSetup(EchoNativeModuleLoadContext context) {
    }

    default void clientSetup(EchoNativeModuleLoadContext context) {
    }

    default void serverSetup(EchoNativeModuleLoadContext context) {
    }

    default void ready(EchoNativeModuleLoadContext context) {
    }

    default void shutdown(EchoNativeModuleLoadContext context) {
    }
}
