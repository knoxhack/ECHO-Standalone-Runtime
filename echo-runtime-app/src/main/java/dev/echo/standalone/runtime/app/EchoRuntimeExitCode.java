package dev.echo.standalone.runtime.app;

public enum EchoRuntimeExitCode {
    SUCCESS(0),
    CONFIGURATION_ERROR(10),
    CRASHED(70);

    private final int code;

    EchoRuntimeExitCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
