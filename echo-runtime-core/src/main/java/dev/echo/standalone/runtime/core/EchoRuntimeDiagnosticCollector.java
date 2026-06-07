package dev.echo.standalone.runtime.core;

import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnostic;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSeverity;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class EchoRuntimeDiagnosticCollector implements EchoRuntimeDiagnosticSink {
    private final java.util.ArrayList<EchoRuntimeDiagnostic> diagnostics = new java.util.ArrayList<>();

    @Override
    public synchronized void emit(EchoRuntimeDiagnostic diagnostic) {
        diagnostics.add(Objects.requireNonNull(diagnostic, "diagnostic"));
    }

    public synchronized List<EchoRuntimeDiagnostic> diagnostics() {
        return List.copyOf(diagnostics);
    }

    public synchronized int count() {
        return diagnostics.size();
    }

    public synchronized long count(EchoRuntimeDiagnosticSeverity severity) {
        Objects.requireNonNull(severity, "severity");
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == severity)
                .count();
    }

    public synchronized Map<String, Long> countsByLayer() {
        return sortedCounts(diagnostics.stream()
                .collect(Collectors.groupingBy(EchoRuntimeDiagnostic::runtimeLayer, Collectors.counting())));
    }

    public synchronized Map<String, Long> countsByCode() {
        return sortedCounts(diagnostics.stream()
                .collect(Collectors.groupingBy(EchoRuntimeDiagnostic::code, Collectors.counting())));
    }

    public synchronized List<EchoRuntimeDiagnostic> byLayer(String runtimeLayer) {
        String normalized = requireText(runtimeLayer, "runtimeLayer");
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.runtimeLayer().equals(normalized))
                .sorted(Comparator.comparing(EchoRuntimeDiagnostic::code))
                .toList();
    }

    public synchronized boolean hasFatal() {
        return diagnostics.stream().anyMatch(diagnostic -> diagnostic.severity() == EchoRuntimeDiagnosticSeverity.FATAL);
    }

    private static Map<String, Long> sortedCounts(Map<String, Long> counts) {
        return Map.copyOf(new TreeMap<>(counts));
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
