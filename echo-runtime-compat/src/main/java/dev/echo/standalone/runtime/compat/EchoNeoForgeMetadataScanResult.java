package dev.echo.standalone.runtime.compat;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record EchoNeoForgeMetadataScanResult(
        List<EchoNeoForgeModCandidate> candidates,
        List<EchoCompatDiagnostic> diagnostics
) {
    public EchoNeoForgeMetadataScanResult {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(diagnostics, "diagnostics");
        candidates = candidates.stream()
                .sorted(Comparator.comparing(EchoNeoForgeModCandidate::modId)
                        .thenComparing(candidate -> candidate.metadataPath().toString()))
                .toList();
        diagnostics = List.copyOf(diagnostics);
    }

    public int candidateCount() {
        return candidates.size();
    }

    public int warningCount() {
        return (int) diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == EchoCompatDiagnosticSeverity.WARNING)
                .count();
    }

    public int errorCount() {
        return (int) diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == EchoCompatDiagnosticSeverity.ERROR)
                .count();
    }

    public Optional<EchoNeoForgeModCandidate> find(String modId) {
        return candidates.stream()
                .filter(candidate -> candidate.modId().equals(modId))
                .findFirst();
    }
}
