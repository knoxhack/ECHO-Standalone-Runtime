package dev.echo.standalone.runtime.assets;

import java.nio.file.Path;
import java.util.Objects;

public record EchoAssetEntry(
        String logicalId,
        EchoAssetNamespace namespace,
        String category,
        String relativePath,
        EchoAssetMount mount,
        Path file,
        long size,
        byte[] embeddedBytes
) {
    public EchoAssetEntry {
        logicalId = requireText(logicalId, "logicalId");
        Objects.requireNonNull(namespace, "namespace");
        category = requireText(category, "category");
        relativePath = requireText(relativePath, "relativePath");
        Objects.requireNonNull(mount, "mount");
        Objects.requireNonNull(file, "file");
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
        file = file.toAbsolutePath().normalize();
        embeddedBytes = embeddedBytes == null ? null : embeddedBytes.clone();
    }

    public EchoAssetEntry(
            String logicalId,
            EchoAssetNamespace namespace,
            String category,
            String relativePath,
            EchoAssetMount mount,
            Path file,
            long size
    ) {
        this(logicalId, namespace, category, relativePath, mount, file, size, null);
    }

    @Override
    public byte[] embeddedBytes() {
        return embeddedBytes == null ? null : embeddedBytes.clone();
    }

    public String sourceKind() {
        return mount.kind();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.replace('\\', '/');
    }
}
