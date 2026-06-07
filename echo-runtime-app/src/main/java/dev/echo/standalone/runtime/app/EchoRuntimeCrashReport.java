package dev.echo.standalone.runtime.app;

public record EchoRuntimeCrashReport<T>(String operation, boolean success, T value, Throwable failure) {
    public static <T> EchoRuntimeCrashReport<T> success(String operation, T value) {
        return new EchoRuntimeCrashReport<>(operation, true, value, null);
    }

    public static <T> EchoRuntimeCrashReport<T> failure(String operation, Throwable failure) {
        return new EchoRuntimeCrashReport<>(operation, false, null, failure);
    }
}
