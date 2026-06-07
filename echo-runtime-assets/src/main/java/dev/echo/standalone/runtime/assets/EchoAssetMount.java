package dev.echo.standalone.runtime.assets;

import java.nio.file.Path;
import java.util.Objects;

public record EchoAssetMount(int order, String kind, Path root, String source) {
    public EchoAssetMount {
        if (order < 0) {
            throw new IllegalArgumentException("order must not be negative");
        }
        kind = requireText(kind, "kind");
        Objects.requireNonNull(root, "root");
        source = requireText(source, "source");
        root = root.toAbsolutePath().normalize();
    }

    public EchoAssetMount withKind(String nextKind) {
        return new EchoAssetMount(order, nextKind, root, source);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
