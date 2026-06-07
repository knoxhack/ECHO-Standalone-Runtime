package dev.echo.standalone.runtime.ui;

import java.util.Map;

public record EchoHudLayer(
        String id,
        Map<String, Object> values
) {
    public EchoHudLayer {
        id = requireText(id, "id");
        values = Map.copyOf(values);
    }

    public boolean ready() {
        return values.values().stream().noneMatch(value -> String.valueOf(value).isBlank());
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
