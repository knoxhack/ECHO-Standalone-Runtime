package dev.echo.standalone.runtime.render;

final class EchoRenderText {
    private EchoRenderText() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
