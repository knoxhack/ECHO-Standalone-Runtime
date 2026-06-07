package dev.echo.standalone.runtime.compat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class EchoNeoForgeMetadataScanner {
    public static final String METADATA_SOURCE = "META-INF/neoforge.mods.toml";

    private final EchoNeoForgeMetadataParser parser;

    public EchoNeoForgeMetadataScanner() {
        this(new EchoNeoForgeMetadataParser());
    }

    public EchoNeoForgeMetadataScanner(EchoNeoForgeMetadataParser parser) {
        this.parser = Objects.requireNonNull(parser, "parser");
    }

    public EchoNeoForgeMetadataScanResult scan(List<Path> roots) {
        Objects.requireNonNull(roots, "roots");
        ArrayList<EchoNeoForgeModCandidate> candidates = new ArrayList<>();
        ArrayList<EchoCompatDiagnostic> diagnostics = new ArrayList<>();
        int metadataFiles = 0;
        for (Path root : roots.stream().map(path -> path.toAbsolutePath().normalize()).sorted().toList()) {
            if (!Files.isDirectory(root)) {
                diagnostics.add(new EchoCompatDiagnostic(
                        EchoCompatDiagnosticSeverity.WARNING,
                        "neoforge-metadata",
                        "metadata scan root does not exist: " + root
                ));
                continue;
            }
            try (var stream = Files.walk(root)) {
                for (Path metadataPath : stream
                        .filter(Files::isRegularFile)
                        .filter(EchoNeoForgeMetadataScanner::isMetadataPath)
                        .sorted(Comparator.comparing(Path::toString))
                        .toList()) {
                    metadataFiles++;
                    try {
                        candidates.addAll(parser.parse(metadataPath));
                    } catch (RuntimeException | IOException exception) {
                        diagnostics.add(new EchoCompatDiagnostic(
                                EchoCompatDiagnosticSeverity.ERROR,
                                "neoforge-metadata",
                                metadataPath + ": " + exception.getMessage()
                        ));
                    }
                }
            } catch (IOException exception) {
                diagnostics.add(new EchoCompatDiagnostic(
                        EchoCompatDiagnosticSeverity.ERROR,
                        "neoforge-metadata",
                        root + ": " + exception.getMessage()
                ));
            }
        }
        diagnostics.add(new EchoCompatDiagnostic(
                EchoCompatDiagnosticSeverity.INFO,
                "neoforge-metadata",
                "discovered " + candidates.size() + " compatibility candidates from " + metadataFiles
                        + " metadata files; candidates are diagnostics-only"
        ));
        return new EchoNeoForgeMetadataScanResult(candidates, diagnostics);
    }

    private static boolean isMetadataPath(Path path) {
        Path parent = path.getParent();
        return parent != null
                && !containsSegment(path, "build")
                && "META-INF".equals(parent.getFileName().toString())
                && "neoforge.mods.toml".equals(path.getFileName().toString());
    }

    private static boolean containsSegment(Path path, String segment) {
        for (Path name : path) {
            if (name.toString().equals(segment)) {
                return true;
            }
        }
        return false;
    }
}
