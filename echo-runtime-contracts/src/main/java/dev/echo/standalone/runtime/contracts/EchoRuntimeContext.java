package dev.echo.standalone.runtime.contracts;

import java.util.Objects;

public record EchoRuntimeContext(
        EchoRuntimeEnvironment environment,
        EchoRuntimeConfiguration configuration,
        EchoRuntimeServiceRegistry services,
        EchoRuntimeDiagnosticSink diagnostics,
        EchoRuntimeClock clock,
        EchoRuntimeCapabilities capabilities,
        EchoRuntimePlatform platform
) {
    public EchoRuntimeContext {
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(configuration, "configuration");
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(diagnostics, "diagnostics");
        Objects.requireNonNull(clock, "clock");
        Objects.requireNonNull(capabilities, "capabilities");
        Objects.requireNonNull(platform, "platform");
    }
}
