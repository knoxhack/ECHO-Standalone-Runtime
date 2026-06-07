package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

final class EchoUiThemeParser {
    private EchoUiThemeParser() {
    }

    @SuppressWarnings("unchecked")
    static EchoUiTheme parse(String fallbackId, String text) {
        Objects.requireNonNull(fallbackId, "fallbackId");
        Object value = EchoUiJson.parse(text);
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Theme JSON must be an object");
        }
        Map<String, Object> object = (Map<String, Object>) map;
        Map<String, String> tokens = new LinkedHashMap<>();
        Object tokenObject = object.get("tokens");
        if (tokenObject instanceof Map<?, ?> tokenMap) {
            for (Map.Entry<?, ?> entry : tokenMap.entrySet()) {
                tokens.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
        return new EchoUiTheme(
                string(object, "id", fallbackId),
                string(object, "displayName", fallbackId),
                string(object, "accentColor", "#5eead4"),
                string(object, "backgroundColor", "#05070a"),
                string(object, "foregroundColor", "#d7fff8"),
                string(object, "warningColor", "#facc15"),
                string(object, "fontFamily", "ECHO Mono"),
                string(object, "density", "compact"),
                tokens
        );
    }

    private static String string(Map<String, Object> object, String key, String fallback) {
        Object value = object.get(key);
        if (value == null) {
            return fallback;
        }
        return String.valueOf(value);
    }
}
