package dev.echo.standalone.runtime.contracts;

import java.util.Map;
import java.util.Objects;

public record EchoRuntimeConfiguration(
        String runtimeVersion,
        EchoRuntimeMode defaultMode,
        boolean headless,
        boolean developmentMode,
        Map<String, String> properties
) {
    public EchoRuntimeConfiguration {
        runtimeVersion = requireText(runtimeVersion, "runtimeVersion");
        Objects.requireNonNull(defaultMode, "defaultMode");
        Objects.requireNonNull(properties, "properties");
        properties = Map.copyOf(properties);
    }

    public static EchoRuntimeConfiguration minimal(String runtimeVersion, EchoRuntimeMode mode) {
        return new EchoRuntimeConfiguration(runtimeVersion, mode, true, false, Map.of());
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
