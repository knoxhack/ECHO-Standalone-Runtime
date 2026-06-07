package dev.echo.standalone.runtime.devtools;

import dev.echo.standalone.runtime.contracts.EchoRuntimeCapabilities;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnostic;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSeverity;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.contracts.EchoRuntimeMode;
import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

public final class EchoRuntimeDevToolsInspector {
    public static final String DIAGNOSTIC_CODE = "ECHO-STANDALONE-DEVTOOLS-SNAPSHOT";

    public EchoRuntimeDevToolsSnapshot inspect(
            String runtimeId,
            EchoRuntimeMode mode,
            EchoRuntimeCapabilities capabilities,
            EchoRuntimeServiceRegistry services,
            EchoRuntimeDiagnosticSink diagnostics
    ) {
        runtimeId = requireText(runtimeId, "runtimeId");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(capabilities, "capabilities");
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(diagnostics, "diagnostics");

        var serviceTypeNames = services.snapshot().keySet().stream()
                .map(Class::getName)
                .sorted()
                .toList();
        var capabilityFlags = capabilities.flags().stream()
                .sorted(Comparator.naturalOrder())
                .toList();
        diagnostics.emit(new EchoRuntimeDiagnostic(
                DIAGNOSTIC_CODE,
                EchoRuntimeDiagnosticSeverity.INFO,
                "devtools",
                "Runtime devtools service snapshot captured.",
                "Contract-only devtools inspector recorded runtime services and capabilities.",
                Map.of(
                        "runtimeId", runtimeId,
                        "runtimeMode", mode.id(),
                        "serviceCount", Integer.toString(serviceTypeNames.size())
                )
        ));
        return new EchoRuntimeDevToolsSnapshot(
                runtimeId,
                mode.id(),
                capabilityFlags,
                serviceTypeNames,
                serviceTypeNames.size(),
                true
        );
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
