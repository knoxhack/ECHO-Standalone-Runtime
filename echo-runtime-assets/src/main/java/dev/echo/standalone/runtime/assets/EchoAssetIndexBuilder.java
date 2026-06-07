package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class EchoAssetIndexBuilder {
    public EchoAssetIndex build(List<EchoAssetMount> mounts) throws IOException {
        Objects.requireNonNull(mounts, "mounts");
        ArrayList<EchoAssetEntry> entries = new ArrayList<>();
        for (EchoAssetMount mount : mounts.stream().sorted(Comparator.comparingInt(EchoAssetMount::order)).toList()) {
            if (Files.isDirectory(mount.root())) {
                try (var stream = Files.walk(mount.root())) {
                    for (Path file : stream.filter(Files::isRegularFile)
                            .sorted(Comparator.comparing(Path::toString))
                            .toList()) {
                        inferDirectoryEntry(mount, file).ifPresent(entries::add);
                    }
                }
            } else if (Files.isRegularFile(mount.root()) && archivePack(mount.root())) {
                entries.addAll(indexArchive(mount));
            }
        }
        return new EchoAssetIndex(entries, Map.of(), Map.of());
    }

    private static Optional<EchoAssetEntry> inferDirectoryEntry(EchoAssetMount mount, Path file) {
        Path relative = mount.root().toAbsolutePath().normalize().relativize(file.toAbsolutePath().normalize());
        return inferEntry(mount, relative, file, size(file), null);
    }

    private static List<EchoAssetEntry> indexArchive(EchoAssetMount mount) throws IOException {
        ArrayList<EchoAssetEntry> entries = new ArrayList<>();
        try (ZipFile zip = new ZipFile(mount.root().toFile())) {
            for (ZipEntry zipEntry : zip.stream()
                    .filter(entry -> !entry.isDirectory())
                    .sorted(Comparator.comparing(ZipEntry::getName))
                    .toList()) {
                String name = zipEntry.getName().replace('\\', '/');
                if (name.startsWith("/") || name.contains("../")) {
                    continue;
                }
                byte[] bytes;
                try (var stream = zip.getInputStream(zipEntry)) {
                    bytes = stream.readAllBytes();
                }
                inferEntry(mount, Path.of(name), mount.root().resolve(name), bytes.length, bytes)
                        .ifPresent(entries::add);
            }
        }
        return List.copyOf(entries);
    }

    private static Optional<EchoAssetEntry> inferEntry(
            EchoAssetMount mount,
            Path relative,
            Path file,
            long size,
            byte[] embeddedBytes
    ) {
        int rootKindIndex = assetRootIndex(relative);
        if (rootKindIndex < 0 || relative.getNameCount() < rootKindIndex + 3) {
            return Optional.empty();
        }
        String rootKind = relative.getName(rootKindIndex).toString();
        String namespace = relative.getName(rootKindIndex + 1).toString();
        String category = relative.getName(rootKindIndex + 2).toString();
        Path tail = relative.subpath(rootKindIndex + 2, relative.getNameCount());
        String logicalPath = tail.toString().replace('\\', '/');
        String logicalId = namespace + ":" + logicalPath;
        try {
            EchoAssetNamespace assetNamespace = new EchoAssetNamespace(namespace);
            return Optional.of(new EchoAssetEntry(
                    logicalId,
                    assetNamespace,
                    category,
                    logicalPath,
                    mount.withKind(rootKind),
                    file,
                    size,
                    embeddedBytes
            ));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static long size(Path file) {
        try {
            return Files.size(file);
        } catch (IOException exception) {
            return 0L;
        }
    }

    private static boolean archivePack(Path path) {
        String name = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        return name.endsWith(".zip") || name.endsWith(".jar");
    }

    private static int assetRootIndex(Path relative) {
        for (int index = 0; index < relative.getNameCount(); index++) {
            String segment = relative.getName(index).toString();
            if ("assets".equals(segment) || "data".equals(segment)) {
                return index;
            }
        }
        return -1;
    }
}
