package dev.echo.standalone.runtime.entity;

final class EchoEntityText {
    private EchoEntityText() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
