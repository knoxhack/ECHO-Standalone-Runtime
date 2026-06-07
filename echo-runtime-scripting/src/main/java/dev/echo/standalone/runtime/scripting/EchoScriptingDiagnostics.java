package dev.echo.standalone.runtime.scripting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoScriptingDiagnostics {
    private final ArrayList<EchoScriptingDiagnostic> diagnostics = new ArrayList<>();

    public synchronized void add(EchoScriptingDiagnostic diagnostic) {
        diagnostics.add(Objects.requireNonNull(diagnostic, "diagnostic"));
    }

    public void info(String ruleId, String message) {
        add(new EchoScriptingDiagnostic(EchoScriptingDiagnosticSeverity.INFO, ruleId, message));
    }

    public void warning(String ruleId, String message) {
        add(new EchoScriptingDiagnostic(EchoScriptingDiagnosticSeverity.WARNING, ruleId, message));
    }

    public synchronized List<EchoScriptingDiagnostic> all() {
        return List.copyOf(diagnostics);
    }

    public synchronized int count() {
        return diagnostics.size();
    }

    public synchronized int warningCount() {
        return (int) diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == EchoScriptingDiagnosticSeverity.WARNING)
                .count();
    }

    public synchronized int errorCount() {
        return (int) diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == EchoScriptingDiagnosticSeverity.ERROR)
                .count();
    }
}
