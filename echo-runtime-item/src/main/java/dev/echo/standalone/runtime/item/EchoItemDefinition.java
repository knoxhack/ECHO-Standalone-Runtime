package dev.echo.standalone.runtime.item;

import java.util.List;
import java.util.Objects;

public record EchoItemDefinition(
        EchoItemId id,
        String displayName,
        EchoItemCategory category,
        int maxStackSize,
        double weight,
        List<String> tags,
        List<String> tooltipLines
) {
    public EchoItemDefinition {
        Objects.requireNonNull(id, "id");
        displayName = EchoItemText.requireText(displayName, "displayName");
        Objects.requireNonNull(category, "category");
        if (maxStackSize <= 0) {
            throw new IllegalArgumentException("maxStackSize must be positive");
        }
        if (weight < 0.0D) {
            throw new IllegalArgumentException("weight must not be negative");
        }
        Objects.requireNonNull(tags, "tags");
        Objects.requireNonNull(tooltipLines, "tooltipLines");
        tags = tags.stream()
                .map(tag -> EchoItemText.requireText(tag, "tag"))
                .sorted()
                .toList();
        tooltipLines = tooltipLines.stream()
                .map(line -> EchoItemText.requireText(line, "tooltipLine"))
                .toList();
    }

    public boolean tagged(String tag) {
        String normalized = EchoItemText.requireText(tag, "tag");
        return tags.contains(normalized);
    }
}
