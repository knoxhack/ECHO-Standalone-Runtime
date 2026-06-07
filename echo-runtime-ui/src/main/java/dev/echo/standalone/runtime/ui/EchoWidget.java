package dev.echo.standalone.runtime.ui;

import java.util.Map;

public record EchoWidget(
        String id,
        String kind,
        Map<String, Object> state
) {
    public EchoWidget {
        id = requireText(id, "id");
        kind = requireText(kind, "kind");
        state = Map.copyOf(state);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
