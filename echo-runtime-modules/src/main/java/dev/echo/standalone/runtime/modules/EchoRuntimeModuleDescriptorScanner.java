package dev.echo.standalone.runtime.modules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class EchoRuntimeModuleDescriptorScanner {
    public static final Set<String> DESCRIPTOR_NAMES = EchoRuntimeModuleDescriptorSchema.DESCRIPTOR_SOURCES.stream()
            .map(source -> Path.of(source).getFileName().toString())
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    private final EchoRuntimeModuleDescriptorParser parser;

    public EchoRuntimeModuleDescriptorScanner(EchoRuntimeModuleDescriptorParser parser) {
        this.parser = Objects.requireNonNull(parser, "parser");
    }

    public EchoRuntimeModuleScanResult scan(List<Path> roots) {
        Objects.requireNonNull(roots, "roots");
        List<EchoRuntimeModuleDescriptor> descriptors = new ArrayList<>();
        List<EchoRuntimeModuleIssue> issues = new ArrayList<>();
        for (Path root : roots.stream().map(path -> path.toAbsolutePath().normalize()).sorted().toList()) {
            if (!Files.isDirectory(root)) {
                issues.add(EchoRuntimeModuleIssue.warning(
                        "ECHO-STANDALONE-MODULE-ROOT-MISSING",
                        null,
                        "Module scan root does not exist: " + root
                ));
                continue;
            }
            try (var stream = Files.walk(root)) {
                for (Path descriptorPath : stream
                        .filter(Files::isRegularFile)
                        .filter(EchoRuntimeModuleDescriptorScanner::isDescriptorPath)
                        .sorted(Comparator.comparing(Path::toString))
                        .toList()) {
                    try {
                        descriptors.add(parser.parse(descriptorPath));
                    } catch (RuntimeException | IOException exception) {
                        issues.add(EchoRuntimeModuleIssue.error(
                                "ECHO-STANDALONE-MODULE-DESCRIPTOR-PARSE-FAILED",
                                null,
                                descriptorPath + ": " + exception.getMessage()
                        ));
                    }
                }
            } catch (IOException exception) {
                issues.add(EchoRuntimeModuleIssue.error(
                        "ECHO-STANDALONE-MODULE-SCAN-FAILED",
                        null,
                        root + ": " + exception.getMessage()
                ));
            }
        }
        return new EchoRuntimeModuleScanResult(
                descriptors.stream().sorted(Comparator.comparing(EchoRuntimeModuleDescriptor::id)).toList(),
                issues
        );
    }

    private static boolean isDescriptorPath(Path path) {
        Path parent = path.getParent();
        return parent != null
                && "META-INF".equals(parent.getFileName().toString())
                && DESCRIPTOR_NAMES.contains(path.getFileName().toString());
    }
}
