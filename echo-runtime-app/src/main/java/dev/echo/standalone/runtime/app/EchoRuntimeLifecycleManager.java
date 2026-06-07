package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnostic;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSeverity;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.contracts.EchoRuntimeLifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoRuntimeLifecycleManager {
    private final EchoRuntimeDiagnosticSink diagnostics;
    private final List<EchoRuntimeLifecycle> trace = new ArrayList<>();
    private EchoRuntimeLifecycle current;

    public EchoRuntimeLifecycleManager(EchoRuntimeLifecycle initial, EchoRuntimeDiagnosticSink diagnostics) {
        this.current = Objects.requireNonNull(initial, "initial");
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
        trace.add(initial);
    }

    public synchronized EchoRuntimeLifecycle current() {
        return current;
    }

    public synchronized void transition(EchoRuntimeLifecycle next) {
        Objects.requireNonNull(next, "next");
        current = next;
        trace.add(next);
        diagnostics.emit(new EchoRuntimeDiagnostic(
                "ECHO-STANDALONE-LIFECYCLE-TRANSITION",
                EchoRuntimeDiagnosticSeverity.INFO,
                "app_runtime",
                "Runtime lifecycle transitioned.",
                next.id(),
                Map.of("state", next.name())
        ));
    }

    public synchronized List<EchoRuntimeLifecycle> trace() {
        return List.copyOf(trace);
    }
}
