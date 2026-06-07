package dev.echo.standalone.runtime.assets;

import java.nio.file.Path;

public record EchoAssetPack(String id, Path root, String source) {
    public EchoAssetPack {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (root == null) {
            throw new IllegalArgumentException("root must not be null");
        }
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        root = root.toAbsolutePath().normalize();
    }
}
