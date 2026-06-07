package dev.echo.standalone.runtime.ui;

import java.util.List;

public record EchoStaticScreen(
        String id,
        String title,
        List<String> lines,
        String focusPath
) implements EchoScreen {
    public EchoStaticScreen {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        lines = List.copyOf(lines);
        focusPath = focusPath == null ? "" : focusPath;
    }

    @Override
    public EchoUiSurface render(EchoUiContext context) {
        return new EchoUiSurface(id, title, lines, focusPath);
    }
}
