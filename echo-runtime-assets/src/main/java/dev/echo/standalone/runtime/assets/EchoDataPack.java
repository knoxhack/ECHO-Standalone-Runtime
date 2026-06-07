package dev.echo.standalone.runtime.assets;

import java.nio.file.Path;

public record EchoDataPack(String id, Path root) {
    public EchoDataPack {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (root == null) {
            throw new IllegalArgumentException("root must not be null");
        }
        root = root.toAbsolutePath().normalize();
    }
}
