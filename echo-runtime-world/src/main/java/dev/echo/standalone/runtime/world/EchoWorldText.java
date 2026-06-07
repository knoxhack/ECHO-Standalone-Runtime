package dev.echo.standalone.runtime.world;

final class EchoWorldText {
    private EchoWorldText() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
