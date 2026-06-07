package dev.echo.standalone.runtime.compat;

import java.util.Locale;
import java.util.Optional;

public enum EchoAdapterCoreRuntimeKind {
    NEOFORGE("neoforge"),
    ECHO_NATIVE_LOADER("echo_native"),
    ECHO_RUNTIME_STANDALONE("echo_runtime_standalone");

    private final String adapterId;

    EchoAdapterCoreRuntimeKind(String adapterId) {
        this.adapterId = adapterId;
    }

    public String adapterId() {
        return adapterId;
    }

    public static Optional<EchoAdapterCoreRuntimeKind> fromId(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        for (EchoAdapterCoreRuntimeKind runtimeKind : values()) {
            if (runtimeKind.adapterId.equals(normalized)
                    || runtimeKind.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return Optional.of(runtimeKind);
            }
        }
        return Optional.empty();
    }
}
