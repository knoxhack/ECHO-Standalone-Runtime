package dev.echo.standalone.runtime.ui;

import java.util.List;
import java.util.Objects;

public record EchoUiSurface(
        String id,
        String title,
        List<String> lines,
        String focusPath
) {
    public EchoUiSurface {
        id = requireText(id, "id");
        title = requireText(title, "title");
        Objects.requireNonNull(lines, "lines");
        focusPath = focusPath == null ? "" : focusPath;
        lines = List.copyOf(lines);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
