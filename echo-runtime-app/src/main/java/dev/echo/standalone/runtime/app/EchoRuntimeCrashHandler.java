package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntimeCrashBoundary;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnostic;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSeverity;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;

import java.util.Map;
import java.util.Objects;

public final class EchoRuntimeCrashHandler implements EchoRuntimeCrashBoundary {
    private final EchoRuntimeDiagnosticSink diagnostics;

    public EchoRuntimeCrashHandler(EchoRuntimeDiagnosticSink diagnostics) {
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
    }

    @Override
    public <T> T guard(String operation, EchoRuntimeCrashCallable<T> callable) {
        EchoRuntimeCrashReport<T> report = guardReport(operation, callable);
        if (!report.success()) {
            throw new IllegalStateException("Runtime operation crashed: " + operation, report.failure());
        }
        return report.value();
    }

    public <T> EchoRuntimeCrashReport<T> guardReport(String operation, EchoRuntimeCrashCallable<T> callable) {
        Objects.requireNonNull(callable, "callable");
        try {
            return EchoRuntimeCrashReport.success(operation, callable.call());
        } catch (Throwable throwable) {
            diagnostics.emit(new EchoRuntimeDiagnostic(
                    "ECHO-STANDALONE-RUNTIME-CRASH",
                    EchoRuntimeDiagnosticSeverity.FATAL,
                    "app_runtime",
                    "Runtime operation crashed inside crash boundary.",
                    throwable.getClass().getName() + ": " + throwable.getMessage(),
                    Map.of("operation", operation == null ? "unknown" : operation)
            ));
            return EchoRuntimeCrashReport.failure(operation, throwable);
        }
    }
}
