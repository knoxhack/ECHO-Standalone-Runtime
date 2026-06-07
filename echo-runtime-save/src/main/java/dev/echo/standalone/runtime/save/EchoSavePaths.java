package dev.echo.standalone.runtime.save;

import java.nio.file.Path;

final class EchoSavePaths {
    private EchoSavePaths() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    static String requireRelativePath(String value, String name) {
        String normalized = requireText(value, name).replace('\\', '/');
        Path path = Path.of(normalized);
        if (path.isAbsolute()) {
            throw new IllegalArgumentException(name + " must be relative: " + value);
        }
        for (Path part : path) {
            if ("..".equals(part.toString())) {
                throw new IllegalArgumentException(name + " must not escape the save root: " + value);
            }
        }
        return normalized;
    }
}
