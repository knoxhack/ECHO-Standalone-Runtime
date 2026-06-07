package dev.echo.standalone.runtime.ui;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record EchoUiTheme(
        String id,
        String displayName,
        String accentColor,
        String backgroundColor,
        String foregroundColor,
        String warningColor,
        String fontFamily,
        String density,
        Map<String, String> tokens
) {
    public EchoUiTheme {
        id = requireText(id, "id");
        displayName = requireText(displayName, "displayName");
        accentColor = requireText(accentColor, "accentColor");
        backgroundColor = requireText(backgroundColor, "backgroundColor");
        foregroundColor = requireText(foregroundColor, "foregroundColor");
        warningColor = requireText(warningColor, "warningColor");
        fontFamily = requireText(fontFamily, "fontFamily");
        density = requireText(density, "density");
        Objects.requireNonNull(tokens, "tokens");
        tokens = Map.copyOf(new TreeMap<>(tokens));
    }

    public static EchoUiTheme defaultTerminal() {
        return new EchoUiTheme(
                "echo-terminal",
                "ECHO Terminal",
                "#5eead4",
                "#05070a",
                "#d7fff8",
                "#facc15",
                "ECHO Mono",
                "compact",
                Map.of(
                        "terminal.prompt", ">",
                        "terminal.cursor", "block",
                        "surface.border", "single"
                )
        );
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
