package dev.echo.standalone.runtime.contracts;

import java.nio.file.Path;
import java.util.Objects;

public record EchoRuntimeEnvironment(
        String runtimeId,
        EchoRuntimeMode mode,
        EchoRuntimeLifecycle initialLifecycle,
        Path workspaceRoot,
        Path reportsRoot,
        boolean development
) {
    public EchoRuntimeEnvironment {
        runtimeId = requireText(runtimeId, "runtimeId");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(initialLifecycle, "initialLifecycle");
        Objects.requireNonNull(workspaceRoot, "workspaceRoot");
        Objects.requireNonNull(reportsRoot, "reportsRoot");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
