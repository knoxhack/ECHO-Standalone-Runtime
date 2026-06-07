package dev.echo.standalone.runtime.ui;

public record EchoTheme(
        String id,
        EchoUiTheme runtimeTheme
) {
    public EchoTheme {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (runtimeTheme == null) {
            throw new IllegalArgumentException("runtimeTheme must not be null");
        }
    }
}
