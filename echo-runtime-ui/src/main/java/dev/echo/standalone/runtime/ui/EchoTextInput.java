package dev.echo.standalone.runtime.ui;

public record EchoTextInput(
        String id,
        String value
) {
    public EchoTextInput {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        value = value == null ? "" : value;
    }

    public EchoTextInput withValue(String nextValue) {
        return new EchoTextInput(id, nextValue);
    }
}
