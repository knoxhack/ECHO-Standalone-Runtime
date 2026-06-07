package dev.echo.standalone.runtime.contracts.story;

import java.util.List;

public record EchoArchiveEntry(
        String id,
        String title,
        String source,
        List<String> lines
) {
    public EchoArchiveEntry {
        id = EchoStoryText.requireText(id, "id");
        title = EchoStoryText.requireText(title, "title");
        source = EchoStoryText.requireText(source, "source");
        lines = List.copyOf(lines);
    }
}
