package dev.echo.standalone.runtime.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoNetworkDiagnostics {
    private final ArrayList<EchoNetworkDiagnostic> diagnostics = new ArrayList<>();

    public synchronized void add(EchoNetworkDiagnostic diagnostic) {
        diagnostics.add(Objects.requireNonNull(diagnostic, "diagnostic"));
    }

    public void info(String message) {
        add(new EchoNetworkDiagnostic(EchoNetworkDiagnosticSeverity.INFO, message, "none"));
    }

    public void info(String message, EchoNetworkPacket packet) {
        Objects.requireNonNull(packet, "packet");
        add(new EchoNetworkDiagnostic(EchoNetworkDiagnosticSeverity.INFO, message, packet.packetId()));
    }

    public synchronized List<EchoNetworkDiagnostic> all() {
        return List.copyOf(diagnostics);
    }

    public synchronized int count() {
        return diagnostics.size();
    }

    public synchronized int warningCount() {
        return (int) diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == EchoNetworkDiagnosticSeverity.WARNING)
                .count();
    }

    public synchronized int errorCount() {
        return (int) diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == EchoNetworkDiagnosticSeverity.ERROR)
                .count();
    }
}
