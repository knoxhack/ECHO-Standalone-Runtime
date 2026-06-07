package dev.echo.standalone.runtime.contracts;

public interface EchoRuntimeCrashBoundary {
    <T> T guard(String operation, EchoRuntimeCrashCallable<T> callable);

    default void guard(String operation, EchoRuntimeCrashAction action) {
        guard(operation, () -> {
            action.run();
            return null;
        });
    }

    @FunctionalInterface
    interface EchoRuntimeCrashCallable<T> {
        T call() throws Exception;
    }

    @FunctionalInterface
    interface EchoRuntimeCrashAction {
        void run() throws Exception;
    }
}
