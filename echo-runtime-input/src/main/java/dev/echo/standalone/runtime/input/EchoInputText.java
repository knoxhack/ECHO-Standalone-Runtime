package dev.echo.standalone.runtime.input;

final class EchoInputText {
    private EchoInputText() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
