package dev.echo.standalone.runtime.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoCompatDiagnostics {
    private final ArrayList<EchoCompatDiagnostic> diagnostics = new ArrayList<>();

    public synchronized void add(EchoCompatDiagnostic diagnostic) {
        diagnostics.add(Objects.requireNonNull(diagnostic, "diagnostic"));
    }

    public void info(String subject, String message) {
        add(new EchoCompatDiagnostic(EchoCompatDiagnosticSeverity.INFO, subject, message));
    }

    public void warning(String subject, String message) {
        add(new EchoCompatDiagnostic(EchoCompatDiagnosticSeverity.WARNING, subject, message));
    }

    public void error(String subject, String message) {
        add(new EchoCompatDiagnostic(EchoCompatDiagnosticSeverity.ERROR, subject, message));
    }

    public synchronized List<EchoCompatDiagnostic> all() {
        return List.copyOf(diagnostics);
    }

    public synchronized int count() {
        return diagnostics.size();
    }

    public synchronized int warningCount() {
        return (int) diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == EchoCompatDiagnosticSeverity.WARNING)
                .count();
    }

    public synchronized int errorCount() {
        return (int) diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == EchoCompatDiagnosticSeverity.ERROR)
                .count();
    }
}
