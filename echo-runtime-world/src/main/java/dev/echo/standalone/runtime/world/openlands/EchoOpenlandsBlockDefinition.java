package dev.echo.standalone.runtime.world.openlands;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Raw Openlands block definition loaded from module JSON.
 */
public record EchoOpenlandsBlockDefinition(
        String id,
        String displayName,
        String category,
        double hardness,
        String tool,
        List<EchoOpenlandsDrop> drops,
        List<String> tags,
        String model,
        String texture,
        List<String> biomePlacement,
        Map<String, Object> extras
) {
    public EchoOpenlandsBlockDefinition {
        id = requireText(id, "id");
        displayName = displayName == null || displayName.isBlank() ? id : displayName.trim();
        category = category == null ? "unknown" : category.trim();
        hardness = Math.max(0.0D, hardness);
        tool = tool == null ? "" : tool.trim();
        drops = drops == null ? List.of() : List.copyOf(drops);
        tags = tags == null ? List.of() : List.copyOf(tags);
        model = model == null ? "" : model.trim();
        texture = texture == null ? "" : texture.trim();
        biomePlacement = biomePlacement == null ? List.of() : List.copyOf(biomePlacement);
        extras = extras == null ? Map.of() : Map.copyOf(extras);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    public record EchoOpenlandsDrop(String item, int count, String fallback) {
        public EchoOpenlandsDrop {
            item = item == null ? "" : item.trim();
            count = Math.max(1, count);
            fallback = fallback == null ? "" : fallback.trim();
        }
    }
}
