package dev.echo.standalone.runtime.item;

final class EchoItemText {
    private EchoItemText() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
