package dev.echo.standalone.runtime.modules;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
            if (Files.isRegularFile(root) && isArchivePath(root)) {
                scanArchive(root, descriptors, issues);
                continue;
            }
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
                        .filter(path -> !isGeneratedBuildOutput(root, path))
                        .filter(Files::isRegularFile)
                        .sorted(Comparator.comparing(Path::toString))
                        .toList()) {
                    if (isDescriptorPath(descriptorPath)) {
                        parseDescriptorFile(descriptorPath, descriptors, issues);
                    } else if (isArchivePath(descriptorPath)) {
                        scanArchive(descriptorPath, descriptors, issues);
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

    private void parseDescriptorFile(
            Path descriptorPath,
            List<EchoRuntimeModuleDescriptor> descriptors,
            List<EchoRuntimeModuleIssue> issues
    ) {
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

    private void scanArchive(
            Path archivePath,
            List<EchoRuntimeModuleDescriptor> descriptors,
            List<EchoRuntimeModuleIssue> issues
    ) {
        try (ZipFile archive = new ZipFile(archivePath.toFile())) {
            List<? extends ZipEntry> descriptorEntries = archive.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry -> isDescriptorEntry(entry.getName()))
                    .sorted(Comparator.comparing(ZipEntry::getName))
                    .toList();
            for (ZipEntry entry : descriptorEntries) {
                try (var input = archive.getInputStream(entry)) {
                    String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                    descriptors.add(parser.parse(archivePath, json, archivePath));
                } catch (RuntimeException | IOException exception) {
                    issues.add(EchoRuntimeModuleIssue.error(
                            "ECHO-STANDALONE-MODULE-DESCRIPTOR-PARSE-FAILED",
                            null,
                            archivePath + "!/" + entry.getName() + ": " + exception.getMessage()
                    ));
                }
            }
        } catch (IOException exception) {
            issues.add(EchoRuntimeModuleIssue.error(
                    "ECHO-STANDALONE-MODULE-ARCHIVE-SCAN-FAILED",
                    null,
                    archivePath + ": " + exception.getMessage()
            ));
        }
    }

    private static boolean isDescriptorPath(Path path) {
        Path parent = path.getParent();
        return parent != null
                && "META-INF".equals(parent.getFileName().toString())
                && DESCRIPTOR_NAMES.contains(path.getFileName().toString());
    }

    private static boolean isDescriptorEntry(String entryName) {
        String normalized = entryName.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        String fileName = slash < 0 ? normalized : normalized.substring(slash + 1);
        String parent = slash < 0 ? "" : normalized.substring(0, slash);
        int parentSlash = parent.lastIndexOf('/');
        String parentName = parentSlash < 0 ? parent : parent.substring(parentSlash + 1);
        return "META-INF".equals(parentName) && DESCRIPTOR_NAMES.contains(fileName);
    }

    private static boolean isArchivePath(Path path) {
        String name = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        return name.endsWith(".jar") || name.endsWith(".zip") || name.endsWith(".echo-addon");
    }

    private static boolean isGeneratedBuildOutput(Path root, Path path) {
        Path relative = root.toAbsolutePath().normalize().relativize(path.toAbsolutePath().normalize());
        for (Path segment : relative) {
            String name = segment.toString();
            if (name.equals("build") || name.equals(".gradle")) {
                return true;
            }
        }
        return false;
    }
}
